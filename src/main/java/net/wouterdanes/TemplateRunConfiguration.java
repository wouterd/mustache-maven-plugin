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
