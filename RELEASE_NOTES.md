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
  -  IO package for reading/writing based on metadata models - XML & Json base classes
  -  IO Object readers/writers for object models based on the metadata models - XML & Json versions
  
* New MetaObjects
  -  MappedMetaObject that works with any object with a Map interface
  -  ProxyMetaObject that creates proxied implementations, not requiring any actual implementation

### Bug Fixes
*  Issues with auto-boxing and unboxing in the PojoMetaObject

### Improvements
*  Cleanup of core MetaData classes
*  Cleanup of Exception handling
*  Replaced parsing of metadata files and types configurations


