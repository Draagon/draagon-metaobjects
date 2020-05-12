package com.draagon.meta.generator.direct;

import com.draagon.meta.MetaData;
import com.draagon.meta.MetaDataException;
import com.draagon.meta.generator.Generator;
import com.draagon.meta.generator.GeneratorBase;
import com.draagon.meta.generator.GeneratorMetaException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class DirectGeneratorBase<T extends DirectGeneratorBase> extends GeneratorBase<T> {

    protected Log log = LogFactory.getLog( this.getClass() );

    @Override
    public T setScripts(List<String> scripts) {
        throw new GeneratorMetaException( "A Direct Generator does not support specifying scripts");
    }

    protected List<String> getUniquePackages(Collection<? extends MetaData> filtered ) throws IOException {
        List<String> pkgs = new ArrayList<>();

        filtered.forEach( md -> {
            if ( md instanceof MetaObject
                    && !pkgs.contains( md.getPackage() )) {
                pkgs.add( md.getPackage() );
            }
        });

        return pkgs;
    }
}