package com.draagon.meta.generator.direct;

import com.draagon.meta.DataTypes;
import com.draagon.meta.MetaData;
import com.draagon.meta.attr.MetaAttribute;
import com.draagon.meta.attr.MetaAttributeNotFoundException;
import com.draagon.meta.field.MetaField;
import com.draagon.meta.generator.GeneratorMetaException;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.object.MetaObject;
import com.draagon.meta.relation.ref.*;
import com.draagon.meta.util.MetaDataUtil;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class MetaDataXSDGenerator extends SingleFileDirectGeneratorBase {

    @Override
    protected void writeFile(Context c, String filename ) throws IOException {

        log.info("Writing MetaDataXSD file: " + filename );

        // Write start of UML file
        //drawFileStart(c);

        // Write end of UML file
        //drawFileEnd(c);
    }
}
