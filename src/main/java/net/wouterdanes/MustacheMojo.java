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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.yaml.snakeyaml.Yaml;

/**
 * The entry class for the maven plugin
 */
@Mojo(name = "mustache")
public class MustacheMojo extends AbstractMojo {

    public static final String FILE_PREFIX = "file:";

    @Parameter(required = true)
    private List<TemplateRunConfiguration> templates;

    @Parameter
    private String context;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Object parsedContext = createContext(context);

        for (TemplateRunConfiguration configuration : templates) {
            getLog().info("Generating '" + configuration.getOutputPath() + "'");
            runTemplateConfiguration(parsedContext, configuration);
        }
    }

    private void runTemplateConfiguration(Object globalContext, TemplateRunConfiguration configuration)
            throws MojoFailureException, MojoExecutionException {
        Object templateContext = createContext(configuration.getContext());
        if (templateContext == null) {
            if (globalContext == null) {
                throw new MojoFailureException("Template has no defined context and plugin context is also empty");
            }
            templateContext = globalContext;
        }

        Mustache mustache = createTemplate(configuration.getTemplateFile());
        File outputFile = new File(configuration.getOutputPath());
        File parent = outputFile.getParentFile();
        parent.mkdirs();
        try (Writer writer = new FileWriter(outputFile)) {
            mustache.execute(writer, templateContext);
        } catch (IOException e) {
            throw new MojoFailureException(e, "Cannot open output file", "Cannot open output file: " + e.getMessage());
        } catch (MustacheException e) {
            throw new MojoFailureException(e, "Cannot process template", "Cannot process template: " + e.getMessage());
        }
    }

    private static Object createContext(String contextConfiguration) throws MojoFailureException {
        if (contextConfiguration == null) {
            return null;
        }

        Yaml yaml = new Yaml();

        if (contextConfiguration.startsWith("---\n")) {
            return yaml.load(contextConfiguration);
        }

        String trimmedContext = contextConfiguration.trim();
        if (trimmedContext.startsWith(FILE_PREFIX)) {
            String filename = trimmedContext.substring(FILE_PREFIX.length());
            try (FileReader reader = new FileReader(filename)) {
                return yaml.load(reader);
            } catch (IOException e) {
                throw new MojoFailureException(e, "Cannot load yaml from file", "Cannot load yaml from file");
            }
        }

        throw new MojoFailureException("Cannot load context. Either pass a filename in the form 'file:[filename]' or " +
                "include a complete yaml document, prefied with '---\\n");
    }

    private static Mustache createTemplate(File template) throws MojoFailureException {
        DefaultMustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache;
        try (Reader reader = new FileReader(template)) {
            mustache = mf.compile(reader, "template");
        } catch (IOException e) {
            throw new MojoFailureException(e, "Cannot open template", "Cannot open template");
        }
        return mustache;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setTemplates(List<TemplateRunConfiguration> templates) {
        this.templates = templates;
    }
}
