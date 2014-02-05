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

import org.apache.maven.plugins.annotations.Parameter;

/**
 * Class to store the configuration of a template run. Context is optional, if not specified the configured context of
 * the Plugin will be used.
 */
public class TemplateRunConfiguration {

    @Parameter(required = true)
    private String outputPath;

    @Parameter(required = true)
    private File templateFile;

    @Parameter
    private String context;

    @SuppressWarnings("UnusedDeclaration")
    public TemplateRunConfiguration() {
    }

    public TemplateRunConfiguration(final String outputPath, final File templateFile) {
        this.outputPath = outputPath;
        this.templateFile = templateFile;
    }

    public TemplateRunConfiguration(final String outputPath, final File templateFile, final String context) {
        this(outputPath, templateFile);
        this.context = context;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public File getTemplateFile() {
        return templateFile;
    }

    public String getContext() {
        return context;
    }
}
