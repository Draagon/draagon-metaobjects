# MetaObjects Release Notes
Latest update: May 22nd 2020

## Introduction

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
* None

# Upcoming Releases

## Version 4.4.0 

### Planned Features
* Native support for Abstract and Interface MetaData
  - Enforcement of rules around how abstract metadata and interfaces can be used
  - Interfaces implemented on metadata will pull over the associated metadata from the interface
  - Code generation support for abstracts and interfaces
  
* TypedMetaDataLoader
  - Support for more advanced control over the metadata models using the TypesConfig models.
  - Automatic naming of MetaData including auto names with prefixes, ex. key1, key2, key3
  
* MetaData IO Enhancements
  - Potential Support for YAML, HOCON, or TOML for configuration files
  - Support for Plugins in the IO Readers and Writers
  - Support for Namespaces within the XML Serialization

# Current Releases

## Version 4.3.0 
Release 4.3.0 is a major refactoring of core codebase, extracting out a separate metadata module in order to have the
more advanced capabilities in the core module that can be meta modelled themselves.  The maven metadata plugin module
is now built before core allowing for code generation of the TypesConfig models in core.  A significant amount of new
features and improves were done, which are listed below.

### Upgrade Steps
* If using the XMLMetaDataLoader

### Breaking Changes
*  Source files that define types must be split into separate XML files with <typeConfig> as the root element and 
   identified using a uri. Ex. types:resource:/example/path/example-types.xml
*  If using the XMLMetaDataLoader, everything should work as before.  If you extended the core MetaDataLoader some
   changes will need to be made.

### New Features
* MetaData IO Package
  - IO package for reading/writing based on metadata models - XML & Json support
  - IO Object readers/writers for object models based on the metadata models - XML & Json versions
  - Post read, the writers call validate() method on objects that implement the new Validatable interface
  - JsonSerializationHandler and XMLSerializationHandler for MetaData classes to support custom serialization
  
* New MetaObject Types
  - MappedMetaObject that works with any object with a Map interface
  - ProxyMetaObject that creates proxied implementations, not requiring any actual implementation
  - DataMetaObject support for DataObjects intended to be wrapped in code generated objects.  Underlying accessor
    fields are protected, creating a less open dynamic object, unlike ValueObject which has public methods
    exposing getters/setters.  
    <i>Note:ValueObject is more useful when there is very little direct access to the objects
    in custom code, such as services that are completely metadata-driven.</i>

* Class support for Attributes and Fields
  - ClassAttribute for handling attributes such as "object" and "class" on MetaModel files
  - ClassField for handling object fields that are Java Classes

* Generator Package
  - Support for Generators intended to be used for code generation and other outputs
  - Support for PlantUML diagrams
  - Support for XSD Generation based on TypesConfig for MetaModel files used for constructing MetaData
  
* MetaData Maven Plugin (Mojo)
  - Maven plugin that executes Generators for use in Maven pom.xml files
  - MojoSupport interface for MetaDataLoaders to work with the Maven Plugin
  
* New MetaDataLoaders
  - TypesConfigLoader for loading TypesConfig files
  - MetaModelLoader for loading MetaModel files
  - SimpleLoader for loading MetaModel files with a default simple.types.xml

* New Relational MetaData
  - New ObjectKey and ObjectReference MetaData types
  - Support for 1:1, 1:*, *:1, and *:* relationship types
  - Works with the [PlantUML](https://plantuml.com/) generator 

### Bug Fixes
*  Issues with auto-boxing and unboxing in the PojoMetaObject

### Improvements
*  Cleanup of core MetaData classes
*  Cleanup of Exception handling
*  Replaced parsing of metadata (MetaModel) files and types configurations (TypesConfig)
*  Refactored how TypesConfig and MetaData are loaded using MetaData themselves and the new IO package
*  New DataTypes Enum support for MetaAttribute and MetaField replacing old MetaFieldTypes statics
*  New DataConverter util for auto conversions between DataTypes
*  URI Support for identifying TypesConfig vs MetaModel files for loading and parsing
*  Support for URL loading of metadata files
*  MetaDataLoaders now have specific LoaderOptions for configuring options on behavior
*  New LoaderOption modes for strict rule enforcement, verbose output, and whether to register themselves in 
   the MetaDataRegistry