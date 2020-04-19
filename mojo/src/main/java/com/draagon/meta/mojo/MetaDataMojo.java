package com.draagon.meta.mojo;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Execute MetaData maven plugins.
 *
 * @goal touch
 * 
 * @phase generate-sources
 */
@Mojo(name="metadata",
        requiresDependencyResolution= ResolutionScope.COMPILE_PLUS_RUNTIME,
        defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class MetaDataMojo extends AbstractMojo
{
    /**
     * Location of the file.
     */
    @Parameter(required = true, property="project.build.directory", defaultValue="${project.build.directory}")
    private File outputDirectory;

    @Parameter
    private String loaderName = null;

    @Parameter
    private String rootPkg = null;

    @Parameter
    private String sourceDir = null;

    @Parameter
    private String template = null;

    @Parameter
    private List<String> metaData = null;

    @Parameter
    String output = null;

    @Parameter
    private String suffix = null;

    public void execute()
        throws MojoExecutionException
    {
        File f = outputDirectory;
        if ( f==null ) {
            //throw new MojoExecutionException( "Output Directory was null! ");
            f = new File("./target/" );
        }

        if ( !f.exists() )
        {
            f.mkdirs();
        }

        File touch = new File( f, "metadata.txt" );

        FileWriter w = null;
        try
        {
            w = new FileWriter( touch );

            w.write( "metadata.txt" );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error creating file " + touch, e );
        }
        finally
        {
            if ( w != null )
            {
                try
                {
                    w.close();
                }
                catch ( IOException e )
                {
                    // ignore
                }
            }
        }
    }
}
