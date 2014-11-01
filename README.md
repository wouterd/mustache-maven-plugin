mustache-maven-plugin
=====================
[![Build Status](https://travis-ci.org/wouterd/mustache-maven-plugin.png?branch=master)](https://travis-ci.org/wouterd/mustache-maven-plugin)

A maven plugin to process mustache templates in a maven build

# Description
This maven plugin allows you to define one or more contexts in YAML and push those through one or more mustache templates 
during your maven build. I've been using this to make my application configuration files (context.xml f.ex) generic and 
generate these during the deploys to various environments. (Using ruby-mustache)

# About this document
As the documentation evolves with different plugin versions, be sure that before you read on that:
- You check that you are watching the right tag
- Switch to the right tag to read the right documentation.

The README of the master branch will cover the current development version and not cover the current released version.

# Usage
Include the following code snippet in your pom as a plugin:
The &lt;configuration&gt; element is the most interesting, here you can specify a global context and a sequence of 
templates. These templates are executed in order of appearance. If you want, you can use files generated in a next step. 
Each template has three fields: templateFile, outputPath and context. Context is optional and specifies a YAML formated 
context. templateFile points to the mustache template to render. outputPath tells the plugin where to write the 
generated output. The context element can either contain valid YAML, starting with a line containing just:

    ---
    
Or it can contain file:[filename], where filename points to a file with valid YAML markup.

The `<encoding>` element specifies the encoding for the input and output files. This defaults to 
`project.build.sourceEncoding` set in Maven.
                    
    <plugin>
        <groupId>net.wouterdanes</groupId>
        <artifactId>mustache-maven-plugin</artifactId>
        <version>1.1</version>
        <executions>
            <execution>
                <id>run-mustache-template</id>
                <phase>validate</phase>
                <goals>
                    <goal>mustache</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <encoding>UTF-8</encoding>
            <context>file:${project.basedir}/local/server-configuration.yml</context>
            <templates>
                <template>
                    <templateFile>${project.basedir}/common/src/main/conf/context.xml.mustache</templateFile>
                    <outputPath>${generated.context.xml.location}</outputPath>
                </template>
                <template>
                    <templateFile>${project.basedir}/common/src/main/conf/hst-config-environment-specific.properties.mustache</templateFile>
                    <outputPath>${generated.hst.properties.env.specific.location}</outputPath>
                </template>
            </templates>
        </configuration>
    </plugin>
