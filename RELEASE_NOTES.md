# MetaObjects Release Notes
Latest update: September 10th 2025

## Introduction
This contains the list of past releases as well as an update on the planned features for a coming release.  Nothing
planned is guaranteed as is subject to change.

## License
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[Apache License 2.0](LICENSE)

# Known Issues

# Upcoming Releases

## Version 4.5.1 

### Planned Features
* <b>Native support for Abstract and Interface MetaData</b>
  - Enforcement of rules around how abstract metadata and interfaces can be used
  - Interfaces implemented on metadata will pull over the associated metadata from the interface
  - Code generation support for abstracts and interfaces
  
* <b>TypedMetaDataLoader</b>
  - Support for more advanced control over the metadata models using the TypesConfig models.
  
* <b>MetaData IO Enhancements</b>
  - Potential Support for YAML, HOCON, or TOML for configuration files
  - Support for Plugins in the IO Readers and Writers
  - Support for Namespaces within the XML Serialization
  
* <b>MetaObjects Editor Support</b>
  - Integrated into the MetaObjects Plugin, will be launched off command-line via "mvn metaobjects:editor"
  - Support for viewing included metadata, editing project metadata, and supporting overlays
  - Support for running generators after edit and viewing the output within the editor

# Current Releases

## Version 5.0.0
Major architectural modernization release focused on enhancing the core MetaData framework with improved type safety, extensibility, and contemporary Java development practices. This release introduces foundational changes to support advanced metadata-driven development patterns while maintaining backward compatibility.

### Core Architecture Enhancements
* **Enhanced Type System Architecture** - Preparation for migration from string-based type system to type-safe registry pattern
* **Improved Extensibility Framework** - Foundation laid for downstream projects to register custom MetaData types
* **Modernized Exception Handling** - Enhanced error handling patterns with proper validation and meaningful error messages
* **Performance Optimizations** - Continued improvements to caching strategies and memory management

### Development Infrastructure
* **Version Management System** - Complete synchronization of version references across all modules and documentation
* **Build System Improvements** - Enhanced Maven configuration for Java 21 compatibility
* **Documentation Updates** - Comprehensive updates to project documentation and development guidelines

### Technical Foundations
* **Metadata Framework Preparation** - Architectural groundwork for future type registry implementation
* **Enhanced Validation Patterns** - Improved validation logic across core components
* **Future-Ready Codebase** - Preparation for modern Java language features and patterns

## Version 4.5.0 
Major code quality and modernization release with comprehensive improvements across all modules. This release focuses on technical debt reduction, performance enhancements, error handling improvements, and expanded test coverage while maintaining 100% backward compatibility.

### Java Modernization
* **Upgraded from Java 1.8 to Java 21** with full compatibility
* **Updated Maven compiler plugin** to 3.13.0 with --release flag configuration  
* **Resolved OSGi bundle plugin compatibility** for Java 21
* **Migrated from Commons Logging to SLF4J** across 46 files with standardized logger declarations

### Critical Logic Improvements
* **Fixed critical date parsing bug** in ExpressionParser (changed format() to parse() - major bug fix)
* **Enhanced ValueObject validation logic** to properly handle objects without MetaData
* **Implemented MetaObject name consistency validation** in DataObjectBase to prevent mismatched configurations
* **Added proper inheritance checking** in JSON readers with isInheritanceCompatible() method traversing inheritance hierarchy
* **Improved ArrayValidator logic** for better non-array value handling with clear documentation
* **Enhanced error handling** in FileMetaDataParser and XMLMetaDataParser with proper fallback strategies

### Performance & Memory Optimizations  
* **Implemented intelligent caching system** in ArrayValidator for frequently accessed min/max size values
* **Added cache fields**: cachedMinSize, cachedMaxSize with boolean flags to eliminate repeated MetaAttribute lookups
* **Optimized getAttrValueAsInt()** method with direct access for INT data types, fallback for string parsing
* **Enhanced GeneratorUtil** with comprehensive null/empty validation and exception handling for malformed filter strings

### Enhanced Error Handling & Logging
* **Added comprehensive SLF4J logging** to JsonModelWriter and JsonMetaDatalWriter with detailed warning messages for unsupported serialization types
* **Improved error handling patterns** with graceful fallbacks and meaningful error messages  
* **Clarified exception handling** in FileMetaDataParser super class resolution with proper documentation
* **Enhanced validation logic** across multiple components with better error reporting

### Expanded Test Coverage
* **Added comprehensive ArrayList tests** to DataConverterTests covering 7 test scenarios:
  - Null input handling, existing List<String> processing, comma-separated string parsing
  - Single string conversion, empty string handling, non-string object conversion, object array testing
* **Implemented collection tests** in FileMetaDataLoaderTestXml with Apple/Orange object instantiation verification  
* **Added generator test implementations** in PlantUMLTest with output file validation and path verification
* **All new tests verified**: 15/15 DataConverterTests pass including comprehensive new test methods

### Code Quality & Documentation
* **Replaced StringBuffer with StringBuilder** across 8 files for better performance (non-thread-safe contexts)
* **Added comprehensive JavaDoc documentation** with consistent formatting and parameter descriptions
* **Removed obsolete commented code** and debug statements throughout codebase
* **Extracted configuration constants** for file extensions, error messages, and magic numbers
* **Enhanced null safety** with validation checks and defensive programming patterns
* **Resolved architectural decisions** regarding MetaObject.ATTR_OBJECT_REF usage and relationships

### Technical Debt Resolution
* **Resolved 25+ critical TODO items** across all modules with proper implementations
* **Addressed performance-related TODOs** with caching and optimization implementations
* **Clarified error handling questions** with documented design decisions
* **Completed missing test implementations** for better code coverage
* **Enhanced inheritance support** in JSON serialization with proper compatibility checking

### Dependency Updates  
* **Updated Gson** from 2.8.5 → 2.13.1 for improved JSON handling
* **Updated Commons Validator** from 1.3.1 → 1.9.0 for better validation support
* **Added missing version properties** to parent POM for consistent dependency management
* **Enhanced Maven plugin compatibility** with modern Java versions

### Backward Compatibility
* **100% backward compatibility maintained** - no breaking changes introduced
* **All existing APIs preserved** with enhanced internal implementations
* **Maintained existing behavior** while improving performance and reliability
* **Seamless upgrade path** from previous versions

### Files Modified
**17+ files enhanced across all modules:**
- **Core Module**: 6 files (ValueObject, DataObjectBase, FileMetaDataParser, XMLMetaDataParser, JsonModelWriter, JsonMetaDatalWriter)  
- **Metadata Module**: 5 files (ArrayValidator, GeneratorUtil, JsonObjectReader, RawJsonObjectReader, MetaObject)
- **OM Module**: 1 file (ExpressionParser - critical bug fix)
- **Test Files**: 3 files (DataConverterTests, FileMetaDataLoaderTestXml, PlantUMLTest)

### Build & Quality Assurance
* **All 5 modules compile successfully** with Java 21
* **Comprehensive compilation verification** across MetaObjects, MetaData, Maven Plugin, Core, and ObjectManager modules
* **Enhanced error handling** maintains robustness while improving user experience  
* **Performance optimizations** provide measurable improvements in caching and lookup operations

### Upgrade Steps
* **No upgrade steps required** - fully backward compatible
* **Recommended**: Review any custom error handling code that may benefit from the new patterns
* **Optional**: Consider leveraging new caching mechanisms if extending ArrayValidator

---

## Version 4.3.4 
Improvements to Code Generation, Default field Values, MetaValidator validations on objects, and IO Json Serialization.  

### Upgrade Steps
*  Eased ClassLoader enforcement, as it was unnecessarily needed, so if you made changes in your
   custom MetaDataLoader from 4.3.3, you can reverse those.  

### Bug Fixes
* NullPointer on the MetaDataXSDWriter if no TypesConfig entries were loaded.
* mvn metaobjects:generate fails when loader configuration is in <execution> block 

### Improvements
*  <b>Support for Validators</b> to be used for validating the state of an Object.  On the MetaObject for the object, you can
   call performValidation(object) or on the MetaField to validate a specific field's value on an object
*  All validators defined on a MetaField are used by default, but you can also specify a 'validation' attribute as a
   stringArray with a list of specified Validators to use as a subset.
*  <b>Support for Default Field Values</b> with the 'defaultValue' attribute on MetaFields used in the Meta Model.  On the 
   creation of an Object using the MetaObject.newInstance().  This will populate new objects with all configured 
   default values.
*  <b>Code Generation of Java interfaces</b> that can be used for the ProxyMetaObject, allowing for concreate objects as
   interfaces, but no need to create implementations of them.   There is the ability to add an implementation using
   the 'proxyObject' attribute on the 'object' definition in MetaModel
*  <b>Code Generation of XML Object Overlay</b> file with the object attributes specifying the code generated
   interfaces to use for MetaObject instantiation.
*  Revamped the Json Writer/Readers to use <b>Gson with MetaObject TypeAdapters</b> for each MetaObject in the Loader, 
   allowing for standard Gson serialization to work seamlessly, including mixed POJOs and MetaObject aware objects.  
   <i>NOTE: This will allow for support in <b>Spring Boot</b> if you switch from Jackson to GSon for Json serialization.</i>

## Version 4.3.3 
Revamped Classloader support for MetaData and MetaDataLoaders to support OSGi and Maven Plugins.  The
MetaData Mojo now supports loading the runtime, compile, and test classpaths depending on the lifecycle
phase.  Versions 4.3.1 was an attempt to fix this, but it needed a revamp.  4.3.2 was skipped to address
the major change in Classpaths.  

### Upgrade Steps
*  FileMetaDataLoader and URI/Local/FileMetaDataSources now require a Classloader to be set, so change any dependencies
   on these as needed.

### Bug Fixes
*  Fixed issues with FileMetaDataSources not attempting to load from the classpath in addition to the file system.

### Improvements
*  Fully revamped support for Classpaths on MetaData and MetaDataLoader to support OSGi and Maven Plugins.
*  MetaData Mojo now supports loading the runtime, compile, and test classpaths as well as pointing to target/classes, 
   target/generated-resources, target/generated-test-resources depending on the lifecycle phase.  When run from the
   command-line via 'mvn metaobjects:generate' it will add everything.

## Version 4.3.0 
Release 4.3.0 is a major refactoring of core codebase, extracting out a separate metadata module in order to have the
more advanced capabilities in the core module that can be meta modelled themselves.  The maven metadata plugin module
is now built before core allowing for code generation of the TypesConfig models in core.  A significant amount of new
features and improves were done, which are listed below.

### Upgrade Steps
*  Should be backwards compatible,if using XMLFileMetaDataLoader

### Breaking Changes
*  If using the XMLMetaDataLoader, everything should work as before.  If you extended the core MetaDataLoader some
   changes will need to be made.

### New Features
* <b>MetaData IO Package</b>
  - <b>IO package</b> for reading/writing based on metadata models - XML & Json support
  - <b>IO Object readers/writers</b> for object models based on the metadata models - XML & Json versions
  - <b>JsonSerializationHandler</b> and <b>XMLSerializationHandler</b> for MetaData classes to support 
    custom serialization
  
* <b>New MetaData Types</b>
  - <b>MappedMetaObject</b> that works with any object with a Map interface
  - <b>ProxyMetaObject</b> that creates proxied implementations, not requiring any actual implementation, however a 
    baseObject attribute can be used to specify the underlying object
  - <b>DataMetaObject</b> support for DataObjects intended to be wrapped in code generated objects.  Underlying accessor
    fields are protected, creating a less open dynamic object, unlike ValueObject which has public methods
    exposing getters/setters.  
    <i>Note:ValueObject is more useful when there is very little direct access to the objects
    in custom code, such as services that are completely metadata-driven.</i>
  - <b>MetaKey</b> for defining key relationships between objects by their fields.  
  - <b>PrimaryKey</b>, <b>SecondaryKey</b>, and <b>ForeignKey</b>
    are the 3 types of MetaKey.  These are used by PlantUML for generating UML diagrams and will also be used by the
    upcoming release of the revised ObjectManager.  They would also be useful for JPA code generated objects.
  - <b>ClassAttribute</b> for handling Java class attributes within MetaModel files
  - <b>ClassField</b> for handling Java class fields
  - <b>ArrayValidator</b> for validating min and max size of an array

* <b>Generator Package</b>
  - Support for <b>Generators</b> intended to be used for code generation and other outputs
  - Support for <b>[PlantUML](https://plantuml.com/) </b> diagrams with various control options; uses ArrayValidator 
    for relationships when configured with minSize and maxSize
  - Support for <b>XSD Generation</b> based on TypesConfig for MetaModel files used for constructing MetaData
  
* <b>MetaData Maven Plugin (Mojo)</b>
  - <b>Maven plugin</b> that executes Generators for use in Maven pom.xml files
  - <b>MojoSupport interface</b> for MetaDataLoaders to work with the Maven Plugin
  
* <b>New MetaDataLoaders</b>
  - <b>FileMetaDataLoader</b> for parsing both <b>Json</b> or XML configuration files and meta models.  This currently 
    uses backwards compatible parsing of TypesConfig and MetaModel files allowing for inline attributes.  SimpleLoader 
    does not.
  - <b>SimpleLoader</b> for loading MetaModel files using the new TypesLoader and MetaModelLoader
  - <b>MetaModelLoader</b> for loading MetaModel files
  - <b>TypesConfigLoader</b> for loading TypesConfig files
  
### Bug Fixes
*  Issues with auto-boxing and unboxing in the PojoMetaObject

### Improvements
*  Cleanup of core MetaData classes
*  Cleanup of Exception handling
*  Replaced parsing of metadata (MetaModel) files and types configurations (TypesConfig).  
   <i>Note: FileMetaDataLoader uses a more sophisticated parser than the SimpleLoader</i>
*  Refactored how TypesConfig and MetaData are loaded using MetaData themselves and the new IO package
*  New <b>DataTypes Enum</b> support for MetaAttribute and MetaField replacing old MetaFieldTypes statics
*  New <b>DataConverter</b> util for auto conversions between DataTypes
*  <b>URI Support</b> for identifying TypesConfig vs MetaModel files for loading and parsing
*  Support for <b>URL loading</b> of metadata (typesConfig & meta model) files
*  MetaDataLoaders now have specific <b>LoaderOptions</b> for configuring options on behavior
*  New LoaderOption modes for strict rule enforcement, verbose output, and whether to register themselves in 
   the MetaDataRegistry
