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
        //System.out.println( "BASEDIR: " + PlexusTestCase.getBasedir() );

        final Properties properties = new Properties();
        final MavenProject mavenProject = Mockito.mock(MavenProject.class);
        Mockito.when(mavenProject.getProperties()).thenReturn(properties);

        PlexusConfiguration configuration = rule.extractPluginConfiguration("metaobjects-maven-plugin", pom);
        assertNotNull( configuration );
        //System.out.println( "configuration: " + configuration );
        //System.out.println( "loader: " + configuration.getChild("loader").getChild("name").getValue() );

        assertEquals("mojo-test-pom", configuration.getChild("loader").getChild("name").getValue());
        assertEquals("com.draagon.meta.loader.file.FileMetaDataLoader", configuration.getChild("loader").getChild("classname").getValue());
        assertEquals(2, configuration.getChild("loader").getChild("sources").getChildren().length);

        //assertEquals("rootPkg", configuration.getChild("loader").getChild("rootPkg").getValue());
        //assertEquals("sourceDir", configuration.getChild("sourceDir").getValue());
        //assertEquals("template", configuration.getChild("template").getValue());
        //assertEquals("output", configuration.getChild("output").getValue());
        //assertEquals("suffix", configuration.getChild("suffix").getValue());

        //System.out.println( "rule: " + pom );
        //System.out.println( "rule: " + rule );

        MetaDataMojo metaDataMojo = (MetaDataMojo) rule.lookupMojo("generate", pom);
        assertNotNull(metaDataMojo);

        //ToDo check output upon execution
        metaDataMojo.execute();
    }
}
