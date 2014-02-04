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
import java.net.URL;
import java.util.HashMap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MustacheMojoTest {

    @Test
    public void testThatTemplateGetsExecuted() throws Exception {
        MustacheMojo mojo = new MustacheMojo();
        HashMap<Object, Object> context = new HashMap<>();
        context.put("text", "Hello test");
        mojo.setContext(context);

        File tempFile = File.createTempFile("mustache", "output");
        tempFile.deleteOnExit();
        String outputPath = tempFile.getPath();
        mojo.setOutputPath(outputPath);

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("test-template.mustache");
        File template = new File(resource.toURI());

        mojo.setTemplate(template);

        mojo.execute();

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
