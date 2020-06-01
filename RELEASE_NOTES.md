# MetaObjects Release Notes
Latest update: May 22nd 2020

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

## Version 4.4.0 

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
