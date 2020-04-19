package com.draagon.meta.mojo;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MetaDataMojoTest {

    @Rule
    public MojoRule rule = new MojoRule();


    @Test
    public void testDocGenMojo() throws Exception {
        File pom = new File(PlexusTestCase.getBasedir(), "src/test/resources/mojo/pom.xml" );

        final Properties properties = new Properties();
        final MavenProject mavenProject = Mockito.mock(MavenProject.class);
        Mockito.when(mavenProject.getProperties()).thenReturn(properties);

        PlexusConfiguration configuration = rule.extractPluginConfiguration("metaobjects-maven-plugin", pom);
        assertNotNull( configuration );
        assertEquals("loaderName", configuration.getChild("loaderName").getValue());
        assertEquals("rootPkg", configuration.getChild("rootPkg").getValue());
        assertEquals("sourceDir", configuration.getChild("sourceDir").getValue());
        assertEquals("template", configuration.getChild("template").getValue());
        assertEquals("output", configuration.getChild("output").getValue());
        assertEquals("suffix", configuration.getChild("suffix").getValue());

        MetaDataMojo metaDataMojo = (MetaDataMojo) rule.lookupMojo("metadata", pom);
        assertNotNull(metaDataMojo);

        //ToDo check output upon execution
        metaDataMojo.execute();
    }
}
