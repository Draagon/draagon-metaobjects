package com.draagon.meta.generator.direct.metadata.overlay;

import com.draagon.meta.attr.*;
import com.draagon.meta.generator.GeneratorIOException;
import com.draagon.meta.generator.direct.metadata.xml.XMLDirectWriter;
import com.draagon.meta.loader.MetaDataLoader;
import com.draagon.meta.loader.model.MetaModel;
import com.draagon.meta.object.MetaObject;
import org.w3c.dom.Element;

import java.io.OutputStream;
import java.util.Map;

public class JavaCodeOverlayXMLWriter extends XMLDirectWriter<JavaCodeOverlayXMLWriter> {

    //private String nameSpace;
    protected Map<MetaObject,String> objectNameMap = null;

    public JavaCodeOverlayXMLWriter(MetaDataLoader loader, OutputStream out ) throws GeneratorIOException {
        super(loader,out);
    }

    /////////////////////////////////////////////////////////////////////////
    // Options

    public JavaCodeOverlayXMLWriter forObjects(Map<MetaObject,String> objectNameMap ) {
        this.objectNameMap = objectNameMap;
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // XMLDirectWriter Methods

    @Override
    public void writeXML() throws GeneratorIOException {

        if (objectNameMap == null) throw new GeneratorIOException(this,
                "No MetaObject->Name map was specified for generating XML Overlay file ["+getFilename()+"]");

        Element rootElement = doc().createElement(MetaModel.OBJECT_NAME);
        rootElement.setAttribute(MetaModel.FIELD_PACKAGE, "");
        doc().appendChild(rootElement);

        writeObjects( rootElement );
    }

    protected void writeObjects( Element rootEl ) throws GeneratorIOException {

        //rootEl.appendChild(doc().createCDATASection("\n"));

        for (MetaObject mo : objectNameMap.keySet()) {

            Element el = doc().createElement(MetaObject.TYPE_OBJECT);
            el.setAttribute( MetaModel.FIELD_PACKAGE, mo.getPackage());
            el.setAttribute( MetaModel.FIELD_NAME, mo.getShortName());
            rootEl.appendChild(el);

            Element attr = doc().createElement(StringAttribute.TYPE_ATTR);
            attr.setAttribute(MetaModel.FIELD_NAME, MetaObject.ATTR_OBJECT);
            attr.setAttribute(MetaModel.FIELD_TYPE,StringAttribute.SUBTYPE_STRING);
            el.appendChild(attr);

            attr.appendChild(doc().createTextNode( objectNameMap.get(mo)));

            //rootEl.appendChild(doc().createCDATASection("\n"));
        }
    }
}
