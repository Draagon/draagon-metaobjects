package com.draagon.meta.mojo;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.generator.Generator;
import com.draagon.meta.loader.file.FileMetaDataLoader;
import com.draagon.meta.loader.file.LocalMetaDataSources;
import com.draagon.meta.loader.file.MetaDataSources;
import com.draagon.meta.loader.file.FileLoaderOptions;
import com.draagon.meta.loader.file.json.JsonMetaDataParser;
import com.draagon.meta.loader.file.xml.XMLMetaDataParser;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Execute MetaData maven plugins.
 *
 * @goal touch
 * 
 * @phase generate-sources
 */
@Mojo(name="generate",
        requiresDependencyResolution= ResolutionScope.COMPILE_PLUS_RUNTIME,
        defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class MetaDataMojo extends AbstractMojo
{
    /**
     * Location of the file.
     */
    @Parameter(required = true, property="project.build.directory", defaultValue="${project.build.directory}")
    private File outputDirectory;

    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    protected MavenProject project;

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

    public void execute()
        throws MojoExecutionException
    {
        if ( getLoader() == null ) {
            throw new MojoExecutionException( "No <loader> element was defined");
        }

        FileMetaDataLoader loader = createLoader();

        if ( getGenerators() != null ) {
            for ( GeneratorParam g : getGenerators() ) {
                try {
                    Generator impl = (Generator) Class.forName(g.getClassname()).newInstance();

                    // Merge generator args and global args
                    Map<String,String> allargs = new HashMap<>();
                    if ( g.getArgs() != null ) allargs.putAll(g.getArgs());
                    if ( globals != null ) allargs.putAll(globals);
                    impl.setArgs(allargs);

                    // Merge loader filters and generator filters
                    List<String> allFilters = new ArrayList<>();
                    if ( g.getFilters() != null ) allFilters.addAll( g.getFilters() );
                    if ( loaderConfig.getFilters() != null ) allFilters.addAll( loaderConfig.getFilters() );
                    impl.setFilters( allFilters );

                    // Set the scripts
                    if ( g.getScripts() != null ) impl.setScripts(g.getScripts());

                    impl.execute(loader);
                }
                catch( Exception e ) {
                    throw new MetaDataException( "Error running generator ["+g.getClassname()+"]: "+e, e );
                }
            }
        }
    }

    protected FileMetaDataLoader createLoader() {

        // TODO:  Clean this all up
        FileLoaderOptions config = new FileLoaderOptions()
            .setVerbose(true)
            .setStrict(false)
            .addParser( "*.xml", XMLMetaDataParser.class)
            .addParser( "*.json", JsonMetaDataParser.class);

        MetaDataSources sources = null;
        if ( loaderConfig.getSourceDir() != null )
            sources = new LocalMetaDataSources( loaderConfig.getSourceDir(), loaderConfig.getSources() );
        else
            sources = new LocalMetaDataSources( loaderConfig.getSources() );
        FileMetaDataLoader loader = null;


        if ( loaderConfig.getClassname() != null ) {

            // TODO:  Clean this up
            Constructor<FileMetaDataLoader> c = null;
            try {
                c = (Constructor<FileMetaDataLoader>) Class.forName( loaderConfig.getClassname() )
                        .getConstructor(FileLoaderOptions.class, String.class);
                loader = c.newInstance( config, loaderConfig.getName() );
            }
            catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                try {
                    c = (Constructor<FileMetaDataLoader>) Class.forName( loaderConfig.getClassname() )
                            .getConstructor(String.class);
                    loader = c.newInstance( loaderConfig.getName() ); //, loaderConfig.getName() );
                }
                catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new MetaDataException( "Could not create FileMetaDataLoader with class [" + loaderConfig.getClassname() + "]: " + e.getMessage(), e );
                }
            }
        }
        else {
            loader = new FileMetaDataLoader( config, loaderConfig.getName() );
        }

        loader.init( sources );
        return loader;
    }
}
