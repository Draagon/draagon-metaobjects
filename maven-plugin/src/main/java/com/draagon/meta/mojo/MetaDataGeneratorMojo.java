package com.draagon.meta.mojo;

import com.draagon.meta.generator.Generator;
import com.draagon.meta.loader.MetaDataLoader;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.util.*;

@Mojo(name="generate",
        requiresDependencyResolution= ResolutionScope.COMPILE_PLUS_RUNTIME,
        defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class MetaDataGeneratorMojo extends AbstractMetaDataMojo
{
    @Override
    protected void executeGenerators(MetaDataLoader loader, List<Generator> generatorImpls) {

        for( Generator gen : generatorImpls ) {
            gen.execute( loader );
        }
    }
}
