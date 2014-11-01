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

import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MustacheMojoTest {

    public static final String TEST_TEMPLATE = "test-template.mustache";

    private MustacheMojo mojo;

    @Before
    public void setUp() throws Exception {
        mojo = new MustacheMojo();
        mojo.setEncoding("UTF-8");
    }

    @Test
    public void testThatTemplateGetsExecuted() throws Exception {
        String context = "---\n{text : \"Hello test\"}\n";

        String outputPath = createTempFile();

        mojo.setTemplates(createTemplateRunConfigurations(context, outputPath));

        mojo.execute();

        checkOutput(outputPath);
    }

    @Test
    public void testThatTemplateGetsExecutedWhenItsAFile() throws Exception {
        File contextFile = getFileFromResource("test-context.yaml");
        String contextFilePath = contextFile.getPath();
        String context = "file:" + contextFilePath;

        String outputPath = createTempFile();

        mojo.setTemplates(createTemplateRunConfigurations(context, outputPath));

        mojo.execute();

        checkOutput(outputPath);
    }

    @Test(expected = MojoFailureException.class)
    public void testThatNoValidContextCausesException() throws Exception {
        mojo.setContext("{text : 'Hello test'}");

        mojo.execute();
    }

    @Test(expected = MojoFailureException.class)
    public void testThatUnknownFileNameCausesException() throws Exception {
        mojo.setContext("file:/some/where/over/therainbox/something.yaml");

        mojo.execute();
    }

    @Test
    public void testThatLocalContextOverridesGlobalContext() throws Exception {
        mojo.setContext("---\ntext: This should not be outputted");

        String outputPath = createTempFile();

        mojo.setTemplates(createTemplateRunConfigurations("---\ntext: Hello test", outputPath));

        mojo.execute();

        checkOutput(outputPath);
    }

    @Test
    public void testThatGlobalContextGetsInheritedWhenNoLocalContext() throws Exception {
        mojo.setContext("---\ntext: Hello test");

        String outputPath = createTempFile();

        mojo.setTemplates(createTemplateRunConfigurations(null, outputPath));

        mojo.execute();

        checkOutput(outputPath);
    }

    @Test
    public void testThatTwoTemplatesGetExecutedWhenConfigured() throws Exception {
        String firstOutput = createTempFile();
        String secondOutput = createTempFile();

        mojo.setContext("---\ntext: Hello test");

        List<TemplateRunConfiguration> runConfigurations = new ArrayList<>(2);
        runConfigurations.add(createTemplateConfiguration(TEST_TEMPLATE, firstOutput, null));
        runConfigurations.add(createTemplateConfiguration(TEST_TEMPLATE, secondOutput, null));

        mojo.setTemplates(runConfigurations);

        mojo.execute();

        checkOutput(firstOutput);
        checkOutput(secondOutput);
    }

    private static String createTempFile() throws IOException {
        File tempFile = File.createTempFile("mustache", "output");
        tempFile.deleteOnExit();
        return tempFile.getPath();
    }

    private List<TemplateRunConfiguration> createTemplateRunConfigurations(final String context,
                                                                           final String outputPath)
            throws URISyntaxException {
        TemplateRunConfiguration configuration = createTemplateConfiguration(TEST_TEMPLATE, outputPath, context);
        List<TemplateRunConfiguration> templateRunConfigurations = new ArrayList<>();
        templateRunConfigurations.add(configuration);
        return templateRunConfigurations;
    }

    private TemplateRunConfiguration createTemplateConfiguration(String templateFilename, String outputPath,
                                                                 String context) throws URISyntaxException {
        File template = getFileFromResource(templateFilename);
        return new TemplateRunConfiguration(outputPath, template, context);
    }

    private File getFileFromResource(final String filename) throws URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(filename);
        return new File(resource.toURI());
    }

    private void checkOutput(String outputPath) throws IOException {
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
