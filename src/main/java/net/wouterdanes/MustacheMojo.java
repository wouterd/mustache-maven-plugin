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
import java.util.Map;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * The entry class for the maven plugin
 */
@Mojo(name = "mustache")
public class MustacheMojo extends AbstractMojo {

    @Parameter(required = true)
    private String outputPath;

    @Parameter(required = true)
    private File template;

    @Parameter(required = true)
    private Map context;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        DefaultMustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache;
        try (Reader reader = new FileReader(template)) {
            mustache = mf.compile(reader, "template");
        } catch (IOException e) {
            throw new MojoFailureException(e, "Cannot open template", "Cannot open template");
        }

        try (Writer writer = new FileWriter(outputPath)) {
            mustache.execute(writer, context);
        } catch (IOException e) {
            throw new MojoFailureException(e, "Cannot open output file", "Cannot open output file");
        } catch (MustacheException e) {
            throw new MojoFailureException(e, "Cannot process template", "Cannot process template");
        }
    }

    public void setOutputPath(final String outputPath) {
        this.outputPath = outputPath;
    }

    public void setTemplate(final File template) {
        this.template = template;
    }

    public void setContext(final Map context) {
        this.context = context;
    }
}
