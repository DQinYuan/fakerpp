package org.testany.fakerpp.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "xsd")
public class XsdMojo extends AbstractMojo {

    @Parameter(defaultValue = "Hello world")
    private String message;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("hello " + message);
    }
}
