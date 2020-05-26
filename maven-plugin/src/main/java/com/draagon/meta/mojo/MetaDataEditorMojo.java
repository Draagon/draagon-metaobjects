package com.draagon.meta.mojo;

import com.draagon.meta.generator.Generator;
import com.draagon.meta.loader.MetaDataLoader;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import javax.swing.*;;
import java.awt.*;
import java.util.List;

@Mojo(name="editor",
        requiresDirectInvocation = true,
        //threadSafe = true,
        //instantiationStrategy = InstantiationStrategy.KEEP_ALIVE,
        requiresDependencyResolution= ResolutionScope.RUNTIME_PLUS_SYSTEM,
        defaultPhase = LifecyclePhase.NONE)
public class MetaDataEditorMojo extends AbstractMetaDataMojo
{
     @Override
     protected void executeGenerators(MetaDataLoader loader, List<Generator> generatorImpls) {

         getLog().info( "Launching MetaData Editor with Loader ["+loader.getName() +"] "+
                 "and ("+generatorImpls.size()+") generators");

         if ( getLog().isDebugEnabled() ) {
             getLog().debug( "Loader:    " + loader );
             for (Generator gen : generatorImpls) {
                 getLog().debug("Generator: " + gen);
             }
         }

         JFrame f = displayEditor( loader );
         try {
             while ( f.isShowing() ) {
                 Thread.currentThread().sleep(1000);
             }

             getLog().info( "MetaData Editor was closed");
         }
         catch (InterruptedException e) {
             getLog().warn( "MetaData Editor was interrupted: " + e.getMessage(), e );
         }
     }

     protected JFrame displayEditor( MetaDataLoader loader ) {

         JFrame f = new JFrame("MetaData Editor - Loader ["+loader.getName()+"]");
         f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

         f.setPreferredSize(new Dimension(400, 300));
         f.pack();
         f.setLocationRelativeTo(null);
         f.setVisible(true);

         return f;
     }
}
