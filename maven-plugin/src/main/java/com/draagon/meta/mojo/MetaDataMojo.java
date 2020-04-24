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

import com.draagon.meta.MetaException;
import com.draagon.meta.generator.Generator;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.draagon.meta.loader.file.LocalMetaDataSources;
import com.draagon.meta.loader.file.MetaDataSources;
import com.draagon.meta.loader.file.config.FileLoaderConfig;
import com.draagon.meta.loader.file.json.JsonMetaDataParser;
import com.draagon.meta.loader.file.xml.XMLMetaDataParser;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * Execute MetaData maven plugins.
 *
 * @goal touch
 * 
 * @phase generate-sources
 */
@Mojo(name="metadata",
        requiresDependencyResolution= ResolutionScope.RUNTIME_PLUS_SYSTEM,
        defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class MetaDataMojo extends AbstractMojo
{
    /**
     * Location of the file.
     */
    @Parameter(required = true, property="project.build.directory", defaultValue="${project.build.directory}")
    private File outputDirectory;

    @Parameter(name="loader")
    private LoaderParam loaderConfig = null;
    public LoaderParam getLoader() {
        return loaderConfig;
    }
    public void setLoader(LoaderParam loaderConfig) {
        this.loaderConfig = loaderConfig;
    }

    @Parameter
    public Map<String,String> globals;
    public Map<String, String> getGlobals() {
        return globals;
    }
    public void setGlobals(Map<String, String> globalArgs) {
        this.globals = globalArgs;
    }

    @Parameter(name="generators")
    public List<GeneratorParam> generators;
    public List<GeneratorParam> getGenerators() {
        return generators;
    }
    public void setGenerators(List<GeneratorParam> generators) {
        this.generators = generators;
    }

    /*@Parameter
    public String rootPkg = null;

    @Parameter
    private String sourceDir = null;

    @Parameter
    private String template = null;

    @Parameter
    private List<String> metaData = null;

    @Parameter
    String output = null;

    @Parameter
    private String suffix = null;*/

    public void execute()
        throws MojoExecutionException
    {
        if ( getLoader() == null ) {
            getLog().error( "No <loade> block was defined");
        }

        FileMetaDataLoader loader = createLoader();

        if ( getGenerators() != null ) {
            for ( GeneratorParam g : getGenerators() ) {
                try {
                    Generator impl = (Generator) Class.forName(g.getClassname()).newInstance();
                    Map<String,String> allargs = g.getArgs();
                    allargs.putAll(globals);
                    impl.setArgs(allargs);
                    impl.setFilter(g.getFilter());
                    impl.setScripts(g.getScripts());
                    impl.execute(loader);
                }
                catch( Exception e ) {
                    throw new MetaException( "Error running generator ["+g.getClassname()+"]: "+e, e );
                }
            }
        }
    }

    protected FileMetaDataLoader createLoader() {

        // TODO:  Clean this all up
        FileLoaderConfig config = new FileLoaderConfig()
            .setVerbose(true)
            .setStrict(true)
            .addParser( "*.xml", XMLMetaDataParser.class)
            .addParser( "*.json", JsonMetaDataParser.class);

        MetaDataSources sources = new LocalMetaDataSources( loaderConfig.getSources() );
        FileMetaDataLoader loader = null;

        if ( loaderConfig.getClassname() != null ) {
            Constructor<FileMetaDataLoader> c = null;
            try {
                c = (Constructor<FileMetaDataLoader>) Class.forName( loaderConfig.getClassname() )
                        .getConstructor(FileLoaderConfig.class, String.class);
                loader = c.newInstance( config, loaderConfig.getName() );
            } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new MetaException( "Could not create FileMetaDataLoader with class [" + loaderConfig.getClassname() + "]: " + e.getMessage(), e );
            }
        } else {
            loader = new FileMetaDataLoader( config, loaderConfig.getName() );
        }

        loader.init( sources );
        return loader;
    }
}
