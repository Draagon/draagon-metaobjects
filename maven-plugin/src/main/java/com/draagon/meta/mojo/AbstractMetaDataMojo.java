package com.draagon.meta.mojo;

import com.draagon.meta.MetaDataException;
import com.draagon.meta.generator.Generator;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.simple.SimpleLoader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractMetaDataMojo extends AbstractMojo
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

    public void execute() throws MojoExecutionException
    {
        if ( getLoader() == null ) {
            throw new MojoExecutionException( "No <loader> element was defined");
        }

        MetaDataLoader loader = createLoader();

        List<Generator> generatorImpls = new ArrayList<>();

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

                    generatorImpls.add( impl );
                }
                catch( Exception e ) {
                    throw new MetaDataException( "Error running generator ["+g.getClassname()+"]: "+e, e );
                }
            }
        }

        executeGenerators( loader, generatorImpls );
    }

    protected abstract void executeGenerators(MetaDataLoader loader, List<Generator> generatorImpls);

    protected MetaDataLoader createLoader() {

        MetaDataLoader loader = null;

        if ( loaderConfig.getClassname() != null ) {

            // TODO:  Clean this up
            Constructor<MetaDataLoader> c = null;
            try {
                c = (Constructor<MetaDataLoader>) Class.forName( loaderConfig.getClassname() )
                        .getConstructor(String.class);
                loader = c.newInstance( loaderConfig.getName() );
            }
            catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException
                    | InvocationTargetException ex) {
                throw new MetaDataException( "Could not create MetaDataLoader(name) with class "+
                        "[" + loaderConfig.getClassname() + "]: " + ex.getMessage(), ex );
            }
        }
        else {
            loader = new SimpleLoader( loaderConfig.getName() );
        }

        // Get the Source Directory if it is specified
        File srcDir = getSourceDir();
        if ( srcDir != null ) loader.mojoSetSourceDir( loaderConfig.getSourceDir() );
        loader.mojoSetSources( loaderConfig.getSources() );
        loader.mojoInit( getGlobals() );

        return loader;
    }

    public static final String URI_FILE = "file";
    public static final String URI_FILE_PREFIX = URI_FILE+"//";
    public static final String URI_CLASSPATH = "classpath";
    public static final String URI_CLASSPATH_PREFIX = URI_CLASSPATH+":";

    /*protected String toSourceUri( File srcDir, String s ) {

        if ( s.contains(":")) {
            if ( s.startsWith( URI_FILE_PREFIX )) {
                s = fileToSourceUri( new File( s.substring( URI_FILE_PREFIX.length() )));
            }
        }
        else if ( srcDir != null ) {
            s = fileToSourceUri( new File( srcDir, s ));
        }
        else {

        }

        return s;
    }

    protected String fileToSourceUri( File f ) {
        if (!f.exists()) {
            throw new IllegalArgumentException("Source file [" + f.getName() + "] does not exist");
        }
        else if (!f.canRead()) {
            throw new IllegalArgumentException("Source file [" + f.getName() + "] cannot be read");
        }

        return URI_FILE_PREFIX + f.getName();
    }*/

    protected File getSourceDir() {
        String srcDir = loaderConfig.getSourceDir();
        File sourceDir = null;
        if ( srcDir != null ) {
            sourceDir = new File( loaderConfig.getSourceDir() );
            if ( !sourceDir.exists() ) {
                getLog().error( "SourceDir ["+srcDir+"] did not exist: "+sourceDir.getPath() );
                throw new IllegalArgumentException( "SourceDir [" + srcDir + "] does not exist" );
            }
        }
        return sourceDir;
    }
}
