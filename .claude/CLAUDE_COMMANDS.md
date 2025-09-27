# MetaObjects - Common Commands for Claude AI

## Build & Compilation

```bash
# Clean and compile entire project
mvn clean compile

# Package all modules
mvn clean package

# Install to local repository
mvn clean install

# Skip tests during build
mvn clean install -DskipTests

# Compile specific module
cd core && mvn compile
cd metadata && mvn compile
cd maven-plugin && mvn compile
```

## Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=FileMetaDataLoaderTestXml

# Run tests for specific module
cd core && mvn test

# Run tests with verbose output
mvn test -X
```

## MetaObjects Code Generation

```bash
# Generate code using MetaObjects Maven plugin
mvn metaobjects:generate

# Generate with specific configuration
mvn metaobjects:generate -Dloader.config=path/to/config.xml

# Future: Launch MetaObjects Editor (v4.4.0+)
mvn metaobjects:editor
```

## Development Utilities

```bash
# Check for dependency updates
mvn versions:display-dependency-updates

# Check plugin versions
mvn versions:display-plugin-updates

# Validate POM structure
mvn help:effective-pom

# Show dependency tree
mvn dependency:tree

# Analyze dependencies
mvn dependency:analyze
```

## Code Quality & Analysis

```bash
# Compile with detailed warnings
mvn compile -X

# Check for outdated dependencies
mvn versions:display-dependency-updates

# Generate Javadocs
mvn javadoc:javadoc

# Generate site documentation
mvn site
```

## OSGi Bundle Operations

```bash
# Generate OSGi manifest
mvn org.apache.felix:maven-bundle-plugin:manifest

# Bundle JAR with OSGi metadata
mvn package
```

## Distribution & Release

```bash
# Deploy to Draagon repository (default profile)
mvn deploy

# Deploy to Maven Central (nexus profile)
mvn deploy -Pnexus -Duse-nexus=true

# Prepare release
mvn release:prepare

# Perform release
mvn release:perform
```

## Module-Specific Commands

```bash
# Build in correct dependency order
cd metadata && mvn clean install
cd ../maven-plugin && mvn clean install  
cd ../core && mvn clean install
cd ../om && mvn clean install

# Generate XSD from TypesConfig
mvn metaobjects:generate -Dgenerator=xsd

# Generate PlantUML diagrams
mvn metaobjects:generate -Dgenerator=plantuml
```

## Debugging & Troubleshooting

```bash
# Enable debug output
mvn clean compile -X

# Show effective settings
mvn help:effective-settings

# Show active profiles
mvn help:active-profiles

# Resolve dependencies issues
mvn dependency:resolve

# Clean workspace thoroughly
mvn clean && rm -rf ~/.m2/repository/com/draagon/metaobjects
```

## Java 21 Specific

```bash
# Verify Java version compatibility
mvn clean compile -Djava.version=21

# Check compiler configuration
mvn help:effective-pom | grep -A5 "maven-compiler-plugin"
```

## Working with MetaData Files

```bash
# Validate MetaModel files
mvn validate

# Generate Java interfaces from MetaModel
mvn metaobjects:generate -Dgenerator=interfaces

# Convert XML metadata to JSON
mvn metaobjects:generate -Dgenerator=json
```

## IDE Integration

```bash
# Generate IDE project files
mvn idea:idea          # IntelliJ IDEA
mvn eclipse:eclipse    # Eclipse

# Import into IDE after generation
# File -> Open -> select root pom.xml
```

## Version Management Commands

```bash
# After version updates, verify all versions match
grep -r "<version>" */pom.xml | grep -v "plugin\|dependency"

# Check README version
grep "Current Release:" README.md

# Verify RELEASE_NOTES has new version  
head -20 RELEASE_NOTES.md | grep "Version"

# Build after version changes
mvn clean compile
```

## Notes for Claude AI

- **VERSION UPDATES**: When user requests version increment, automatically update ALL files (see CLAUDE_VERSION_MANAGEMENT.md)
- Always build `metadata` module before `core` due to code generation dependencies
- Use `-DskipTests` if tests are failing and you need to build quickly
- The project uses Java 21 features, ensure compatibility when making changes
- OSGi bundle generation is automatic during package phase
- MetaObjects plugin commands may not work until the plugin is built and installed locally