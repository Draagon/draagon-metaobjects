import groovy.text.GStringTemplateEngine
import com.draagon.meta.loader.xml.XMLFileMetaDataLoader;

String template = project.properties['template']
String input = project.properties['input']
String output = project.properties['output']
String prefix = project.properties['prefix']
String suffix = project.properties['suffix']
String pkg = project.properties['package']

prefix = prefix==null?"":prefix
suffix = suffix==null?"":suffix
pkg = pkg==null?"":pkg

def fout = new File(output);
if ( !fout.exists() ) fout.mkdirs();

// Load the metadata
xl = new XMLFileMetaDataLoader();
xl.setSource( input );
xl.init();

// Iterate the packages
xl.getMetaObjects().each {

    println "Writing: ${prefix}${it.getShortName()}${suffix}"
}

//def model = new XmlParser().parse( input )
/* model.objects.object.each {
    println "Writing: ${prefix}${it.'@name'}${suffix}"

    def binding = ["object":it, "model":model, "prefix":prefix, "suffix":suffix, "pkg":pkg]
    def f = new File(template)
    engine = new GStringTemplateEngine()
    gtemp = engine.createTemplate(f).make(binding)

    PrintWriter pw = new PrintWriter("${output}/${prefix}${it.'@name'}${suffix}")
    gtemp.writeTo( pw )
    pw.close()
}*/

println "Done with ${input}!"
