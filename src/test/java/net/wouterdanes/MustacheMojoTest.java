/*
 Copyright 2014 Wouter Danes

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package net.wouterdanes;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MustacheMojoTest {

    private MustacheMojo mojo;
    private String outputPath;

    @Before
    public void setUp() throws Exception {
        mojo = new MustacheMojo();

        File tempFile = File.createTempFile("mustache", "output");
        tempFile.deleteOnExit();
        outputPath = tempFile.getPath();
        mojo.setOutputPath(outputPath);

        String filename = "test-template.mustache";
        File template = getFileFromResource(filename);
        mojo.setTemplate(template);
    }

    @Test
    public void testThatTemplateGetsExecuted() throws Exception {
        String context = "---\n{text : \"Hello test\"}\n";
        mojo.setContext(context);

        mojo.execute();

        checkOutput();
    }

    @Test
    public void testThatTemplateGetsExecutedWhenItsAFile() throws Exception {
        File contextFile = getFileFromResource("test-context.yaml");
        String contextFilePath = contextFile.getPath();
        mojo.setContext("file:" + contextFilePath);

        mojo.execute();

        checkOutput();
    }

    @Test(expected = MojoFailureException.class)
    public void testThatNoValidContextCausesException() throws Exception {
        mojo.setContext("{text : 'Hello test'}");

        mojo.execute();

        checkOutput();
    }

    @Test(expected = MojoFailureException.class)
    public void testThatUnknownFileNameCausesException() throws Exception {
        mojo.setContext("file:/some/where/over/therainbox/something.yaml");

        mojo.execute();

        checkOutput();
    }

    private File getFileFromResource(final String filename) throws URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(filename);
        return new File(resource.toURI());
    }

    private void checkOutput() throws IOException {
        File file = new File(outputPath);
        final char[] fileContents;
        try (FileReader fileReader = new FileReader(file)) {
            long length = file.length();
            fileContents = new char[(int) length];
            fileReader.read(fileContents);
        }

        String output = new String(fileContents);

        assertEquals("Output not the expected output", "Hello test", output);
    }
}
