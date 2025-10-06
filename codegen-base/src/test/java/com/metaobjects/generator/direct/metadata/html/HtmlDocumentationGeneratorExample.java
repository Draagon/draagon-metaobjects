package com.metaobjects.generator.direct.metadata.html;

import com.metaobjects.generator.GeneratorIOException;
import com.metaobjects.generator.GeneratorTestBase;
import com.metaobjects.loader.simple.SimpleLoader;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Example demonstrating usage of the MetaDataHtmlDocumentationGenerator.
 *
 * This shows how to generate professional HTML documentation for MetaObjects types.
 * The generated HTML includes:
 * - Modern responsive design with sidebar navigation
 * - Type hierarchy visualization
 * - Detailed type definitions with examples
 * - Extension/plugin development guide
 */
public class HtmlDocumentationGeneratorExample extends GeneratorTestBase {

    @Test
    public void demonstrateGeneratorConfiguration() {
        // Example showing how to configure the generator (as would be done in Maven plugin)
        MetaDataHtmlDocumentationGenerator generator = new MetaDataHtmlDocumentationGenerator();

        // Configure the generator
        generator.setTitle("Custom Framework Documentation");
        generator.setVersion("1.0.0");
        generator.setIncludeInheritance(true);
        generator.setIncludeExamples(false);  // Skip examples for faster generation
        generator.setIncludeExtensionGuide(true);

        // Log configuration
        System.out.println("\n" + generator.getConfigurationSummary());
        System.out.println("\nGenerator toString: " + generator.toString());
    }

    /**
     * Example Maven Plugin Configuration (for documentation)
     *
     * Add this to your pom.xml to generate HTML documentation:
     *
     * <plugin>
     *     <groupId>com.metaobjects</groupId>
     *     <artifactId>metaobjects-maven-plugin</artifactId>
     *     <version>${project.version}</version>
     *     <executions>
     *         <execution>
     *             <id>generate-html-docs</id>
     *             <phase>process-classes</phase>
     *             <goals>
     *                 <goal>generate</goal>
     *             </goals>
     *             <configuration>
     *                 <loader>
     *                     <classname>com.metaobjects.loader.file.FileMetaDataLoader</classname>
     *                     <name>html-doc-generator</name>
     *                     <sources/>
     *                 </loader>
     *                 <generators>
     *                     <generator>
     *                         <classname>com.metaobjects.generator.direct.metadata.html.MetaDataHtmlDocumentationGenerator</classname>
     *                         <args>
     *                             <outputDir>${project.build.directory}/generated-docs</outputDir>
     *                             <outputFilename>metaobjects-documentation.html</outputFilename>
     *                             <title>My Project MetaObjects Documentation</title>
     *                             <version>${project.version}</version>
     *                             <includeInheritance>true</includeInheritance>
     *                             <includeExamples>true</includeExamples>
     *                             <includeExtensionGuide>true</includeExtensionGuide>
     *                         </args>
     *                     </generator>
     *                 </generators>
     *             </configuration>
     *         </execution>
     *     </executions>
     * </plugin>
     */
    public void mavenPluginConfigurationExample() {
        // This method serves as documentation - see the JavaDoc above
    }

    @Test
    public void generateHtmlDocumentationToTargetFolder() throws GeneratorIOException, IOException, URISyntaxException {
        System.out.println("\n=== Generating HTML Documentation to target/html folder ===");

        // Step 1: Create target/html directory
        File targetDir = new File("target/html");
        if (!targetDir.exists()) {
            boolean created = targetDir.mkdirs();
            System.out.println("Created target/html directory: " + created);
        }

        // Step 2: Load test metadata to get actual content
        URI testMetadataUri = getClass().getClassLoader().getResource("template-test-metadata.json").toURI();
        SimpleLoader loader = initLoader(Arrays.asList(testMetadataUri));

        System.out.println("Loaded metadata from: " + testMetadataUri);
        System.out.println("Loader has " + loader.getChildren().size() + " root children");

        // Step 3: Create the HTML documentation file
        File htmlFile = new File(targetDir, "metaobjects-documentation.html");

        try (FileOutputStream fos = new FileOutputStream(htmlFile)) {
            MetaDataHtmlDocumentationWriter writer = new MetaDataHtmlDocumentationWriter(loader, fos)
                    .withTitle("MetaObjects Test Documentation")
                    .withVersion("6.2.0-TEST")
                    .withInheritance(true)
                    .withExamples(true)
                    .withExtensionGuide(true);

            // Generate the documentation
            writer.writeHtml();
            writer.close();
        }

        // Step 4: Verify the file was created and has content
        assert htmlFile.exists() : "HTML documentation file should exist";
        assert htmlFile.length() > 0 : "HTML documentation file should have content";

        System.out.println("Generated HTML documentation:");
        System.out.println("  File: " + htmlFile.getAbsolutePath());
        System.out.println("  Size: " + htmlFile.length() + " bytes");

        // Step 5: Sample content verification
        String content = java.nio.file.Files.readString(htmlFile.toPath());

        // Verify key HTML elements
        assert content.contains("<!DOCTYPE html>") : "Should contain DOCTYPE declaration";
        assert content.contains("MetaObjects Test Documentation") : "Should contain custom title";
        assert content.contains("Framework Overview") : "Should contain overview section";
        assert content.contains("Type Registry Statistics") : "Should contain statistics section";
        assert content.contains("Extension & Plugin Development Guide") : "Should contain extension guide";

        // Log some statistics
        String[] lines = content.split("\n");
        System.out.println("  Lines: " + lines.length);
        System.out.println("  Sections: " + countOccurrences(content, "<section"));
        System.out.println("  Type cards: " + countOccurrences(content, "type-card"));

        // Show first few lines as sample
        System.out.println("\nFirst 5 lines of generated HTML:");
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            System.out.println("  " + (i+1) + ": " + lines[i]);
        }

        System.out.println("\n✅ Successfully generated HTML documentation to: " + htmlFile.getAbsolutePath());
    }

    @Test
    public void generateMinimalHtmlDocumentationToByteArray() throws GeneratorIOException, IOException {
        System.out.println("\n=== Testing Minimal HTML Generation ===");

        // Use shared loader for minimal test (just registry types, no external metadata)
        SimpleLoader loader = getSharedLoader();

        // Generate HTML documentation to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MetaDataHtmlDocumentationWriter writer = new MetaDataHtmlDocumentationWriter(loader, outputStream)
                .withTitle("Registry-Only Documentation")
                .withVersion("6.2.0-MINIMAL")
                .withInheritance(true)
                .withExamples(false)  // Skip examples for minimal test
                .withExtensionGuide(false);

        // Generate the documentation
        writer.writeHtml();
        writer.close();

        // Get the generated HTML
        String generatedHtml = outputStream.toString();

        // Verify key elements are present
        assert generatedHtml.contains("<!DOCTYPE html>");
        assert generatedHtml.contains("Registry-Only Documentation");
        assert generatedHtml.contains("Framework Overview");
        assert generatedHtml.contains("Type Registry Statistics");

        // Log statistics
        System.out.println("Generated minimal HTML: " + generatedHtml.length() + " characters");
        System.out.println("Contains " + countOccurrences(generatedHtml, "<section") + " sections");

        System.out.println("✅ Minimal HTML generation successful");
    }

    private int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }

}