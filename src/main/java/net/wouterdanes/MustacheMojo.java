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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The entry class for the maven plugin
 */
@Mojo(name = "mustache")
public class MustacheMojo extends AbstractMojo {

    private static final String FILE_PREFIX = "file:";
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    @Parameter(required = true)
    private List<TemplateRunConfiguration> templates;

    @Parameter
    private String context;

    @Parameter(defaultValue = "${project.build.sourceEncoding}")
    private String encoding;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Charset charset;
        if (StringUtils.isEmpty(encoding)) {
            getLog().warn("File encoding has not been set, using platform encoding " + Charset.defaultCharset()
                    + ", i.e. build is platform dependent!");
            charset = Charset.defaultCharset();
        } else {
            charset = Charset.forName(encoding);
        }
        Object parsedContext = createContext(context, charset);

        for (TemplateRunConfiguration configuration : templates) {
            getLog().info("Generating '" + configuration.getOutputPath() + "'");
            runTemplateConfiguration(parsedContext, configuration, charset);
        }
    }

    private void runTemplateConfiguration(Object globalContext, TemplateRunConfiguration configuration, Charset charset)
            throws MojoFailureException, MojoExecutionException {
        Object templateContext = createContext(configuration.getContext(), charset);
        if (templateContext == null) {
            if (globalContext == null) {
                throw new MojoFailureException("Template has no defined context and plugin context is also empty");
            }
            templateContext = globalContext;
        }

        Mustache mustache = createTemplate(configuration.getTemplateFile(), charset);
        File outputFile = new File(configuration.getOutputPath());
        File parent = outputFile.getParentFile();
        if (!parent.exists() && parent.mkdirs()) {
            throw new MojoFailureException("Output directory cannot be created.");
        }
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile), charset)) {
            mustache.execute(writer, templateContext);
        } catch (IOException e) {
            throw new MojoFailureException(e, "Cannot open output file", "Cannot open output file: " + e.getMessage());
        } catch (MustacheException e) {
            throw new MojoFailureException(e, "Cannot process template", "Cannot process template: " + e.getMessage());
        }
    }

    private Object createContext(String contextConfiguration, Charset charset) throws MojoFailureException {
        if (contextConfiguration == null) {
            return null;
        }

        Yaml yaml = new Yaml();

        String contextSource = obtainContextSource(contextConfiguration, charset);
        Matcher matcher = PROPERTY_PATTERN.matcher(contextSource);
        Set<String> props = new LinkedHashSet<>(10);
        while (matcher.find()) {
            props.add(matcher.group(1));
        }

        for (String prop : props) {
            if (project.getProperties().containsKey(prop)) {
                contextSource = contextSource.replace(String.format("${%s}", prop), project.getProperties().getProperty(prop));
            } else {
                getLog().warn(String.format("Property '%s' referenced in context, but doesn't exist in Maven Project...", prop));
            }
        }

        return yaml.load(contextSource);
    }

    private static String obtainContextSource(String contextConfiguration, Charset charset) throws MojoFailureException {
        if (contextConfiguration.startsWith("---\n")) {
            return contextConfiguration;
        }

        String trimmedContext = contextConfiguration.trim();
        if (trimmedContext.startsWith(FILE_PREFIX)) {
            String filename = trimmedContext.substring(FILE_PREFIX.length());
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(filename));
                return new String(bytes, charset);
            } catch (IOException e) {
                throw new MojoFailureException(e, "Cannot load yaml from file", "Cannot load yaml from file");
            }
        }

        throw new MojoFailureException("Cannot load context. Either pass a filename in the form 'file:[filename]' or " +
                "include a complete yaml document, prefied with '---\\n");
    }

    private static Mustache createTemplate(File template, Charset charset) throws MojoFailureException {
        DefaultMustacheFactory mf = new DefaultMustacheFactory(template.getParentFile());
        Mustache mustache;
        try (Reader reader = new InputStreamReader(new FileInputStream(template), charset)) {
            mustache = mf.compile(reader, "template");
        } catch (IOException e) {
            throw new MojoFailureException(e, "Cannot open template", "Cannot open template");
        }
        return mustache;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setTemplates(List<TemplateRunConfiguration> templates) {
        this.templates = templates;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }
}
