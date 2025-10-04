# MetaObjects Maven Archetype Design

## 🎯 **RECOMMENDED APPROACH: Maven Archetype**

Based on step-by-step analysis, **Maven Archetype is the superior solution** because:

✅ **Industry Standard**: Developers expect `mvn archetype:generate` for project scaffolding
✅ **Complete Project**: Creates entire working application structure, not just documentation
✅ **Zero Configuration**: All dependencies, plugins, and configuration included
✅ **Best Practices**: Demonstrates proper MetaObjects architecture through working code
✅ **Immediate Value**: Generated project compiles and runs without additional setup

---

## 🏗️ **ARCHETYPE STRUCTURE**

### **New Module: `metaobjects-archetype`**

```
metaobjects-archetype/
├── pom.xml                                    # Archetype build configuration
└── src/
    └── main/
        └── resources/
            ├── META-INF/
            │   └── maven/
            │       └── archetype-metadata.xml  # Archetype definition
            └── archetype-resources/             # Template project
                ├── pom.xml                      # Generated project POM
                ├── .claude/
                │   └── CLAUDE.md               # Complete application guide
                ├── src/
                │   ├── main/
                │   │   ├── java/
                │   │   │   └── __packageInPathFormat__/
                │   │   │       ├── Application.java
                │   │   │       ├── config/
                │   │   │       │   └── MetaObjectsConfiguration.java
                │   │   │       ├── service/
                │   │   │       │   ├── UserService.java
                │   │   │       │   └── OrderService.java
                │   │   │       ├── controller/
                │   │   │       │   ├── UserController.java
                │   │   │       │   └── OrderController.java
                │   │   │       └── dto/
                │   │   │           ├── CreateUserRequest.java
                │   │   │           └── OrderItemData.java
                │   │   └── resources/
                │   │       ├── application.properties
                │   │       ├── metadata/
                │   │       │   ├── application-metadata.json
                │   │       │   └── database-overlay.json
                │   │       └── templates/           # Custom Mustache templates
                │   │           └── java-domain-object.mustache
                │   └── test/
                │       └── java/
                │           └── __packageInPathFormat__/
                │               ├── ApplicationTest.java
                │               ├── service/
                │               │   ├── UserServiceTest.java
                │               │   └── OrderServiceTest.java
                │               └── integration/
                │                   └── DatabaseIntegrationTest.java
                └── README.md                    # Generated project readme
```

---

## 📋 **ARCHETYPE IMPLEMENTATION PLAN**

### **Phase 1: Archetype Module Creation**

1. **Create new module** `metaobjects-archetype` in main project
2. **Configure archetype POM** with proper parent and packaging
3. **Define archetype metadata** with parameter placeholders
4. **Create template project structure** with working examples

### **Phase 2: Template Content Development**

1. **Working Metadata Examples**: User/Order domain with proper Identity/Relationship patterns
2. **Complete Configuration**: Maven, Spring, Database setup
3. **Example Services**: CRUD operations using ObjectManagerDB
4. **REST Controllers**: Complete API endpoints
5. **Test Suite**: Unit and integration tests

### **Phase 3: Documentation Integration**

1. **Comprehensive CLAUDE.md**: Complete application guide (already created)
2. **Project README**: Getting started instructions
3. **Code Comments**: Extensive documentation in generated code
4. **Architecture Notes**: Inline explanations of MetaObjects patterns

### **Phase 4: Testing & Validation**

1. **Generate test projects** using archetype
2. **Verify complete build** without manual configuration
3. **Test all examples** compile and run successfully
4. **Validate documentation** accuracy and completeness

---

## ⚙️ **ARCHETYPE CONFIGURATION**

### **archetype-metadata.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<archetype-descriptor name="metaobjects-application">
    <requiredProperties>
        <requiredProperty key="groupId"/>
        <requiredProperty key="artifactId"/>
        <requiredProperty key="version"/>
        <requiredProperty key="package"/>
    </requiredProperties>

    <fileSets>
        <fileSet filtered="true" packaged="true">
            <directory>src/main/java</directory>
            <includes>
                <include>**/*.java</include>
            </includes>
        </fileSet>
        <fileSet filtered="true">
            <directory>src/main/resources</directory>
            <includes>
                <include>**/*.properties</include>
                <include>**/*.json</include>
                <include>**/*.mustache</include>
            </includes>
        </fileSet>
        <fileSet filtered="true" packaged="true">
            <directory>src/test/java</directory>
            <includes>
                <include>**/*.java</include>
            </includes>
        </fileSet>
        <fileSet filtered="false">
            <directory>.claude</directory>
            <includes>
                <include>**/*.md</include>
            </includes>
        </fileSet>
    </fileSets>
</archetype-descriptor>
```

### **Generated Project POM Template**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>${groupId}</groupId>
    <artifactId>${artifactId}</artifactId>
    <version>${version}</version>
    <packaging>jar</packaging>

    <name>${artifactId}</name>
    <description>MetaObjects Application: ${artifactId}</description>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <metaobjects.version>6.2.6</metaobjects.version>
        <spring.boot.version>3.2.0</spring.boot.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring.boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- MetaObjects Core -->
        <dependency>
            <groupId>com.metaobjects</groupId>
            <artifactId>metaobjects-core</artifactId>
            <version>${metaobjects.version}</version>
        </dependency>

        <!-- MetaObjects Database Persistence -->
        <dependency>
            <groupId>com.metaobjects</groupId>
            <artifactId>metaobjects-omdb</artifactId>
            <version>${metaobjects.version}</version>
        </dependency>

        <!-- MetaObjects Spring Integration -->
        <dependency>
            <groupId>com.metaobjects</groupId>
            <artifactId>metaobjects-core-spring</artifactId>
            <version>${metaobjects.version}</version>
        </dependency>

        <!-- Spring Boot Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Database (H2 for getting started, replace with your choice) -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- MetaObjects Code Generation -->
            <plugin>
                <groupId>com.metaobjects</groupId>
                <artifactId>metaobjects-maven-plugin</artifactId>
                <version>${metaobjects.version}</version>
                <executions>
                    <execution>
                        <id>generate-domain-objects</id>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                        <configuration>
                            <loader>
                                <classname>com.metaobjects.loader.file.FileMetaDataLoader</classname>
                                <name>app-metadata</name>
                                <sourceURIs>
                                    <sourceURI>classpath:metadata/application-metadata.json</sourceURI>
                                    <sourceURI>classpath:metadata/database-overlay.json</sourceURI>
                                </sourceURIs>
                            </loader>
                            <generators>
                                <generator>
                                    <classname>com.metaobjects.generator.mustache.java.JavaDomainObjectGenerator</classname>
                                    <args>
                                        <outputDir>${project.basedir}/src/main/java</outputDir>
                                        <packageName>${package}.domain</packageName>
                                    </args>
                                </generator>
                            </generators>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <!-- Spring Boot Maven Plugin -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring.boot.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 🚀 **USAGE EXPERIENCE**

### **Developer Workflow**

```bash
# 1. Generate new MetaObjects project
mvn archetype:generate \
  -DarchetypeGroupId=com.metaobjects \
  -DarchetypeArtifactId=metaobjects-application-archetype \
  -DarchetypeVersion=6.2.6 \
  -DgroupId=com.mycompany \
  -DartifactId=my-awesome-app \
  -Dversion=1.0.0-SNAPSHOT \
  -Dpackage=com.mycompany.myapp \
  -DinteractiveMode=false

# 2. Enter generated project
cd my-awesome-app

# 3. Build and run immediately (everything works out of the box)
mvn clean compile
mvn spring-boot:run

# 4. Generated project includes:
# - Complete working REST API (Users, Orders)
# - H2 database with auto-created tables
# - Sample data and CRUD operations
# - Comprehensive .claude/CLAUDE.md guide for Claude Code assistance
```

### **Generated Project Features**

✅ **Immediate Value**: Project runs without any configuration
✅ **Working Examples**: CRUD operations for User and Order entities
✅ **REST API**: Complete API endpoints with proper HTTP methods
✅ **Database Integration**: Auto-created tables with sample data
✅ **Best Practices**: Demonstrates proper MetaObjects architecture patterns
✅ **Testing**: Unit and integration tests included
✅ **Documentation**: Comprehensive Claude Code guide in `.claude/CLAUDE.md`

---

## 📈 **BENEFITS OVER BUNDLED GUIDE APPROACH**

| Aspect | Bundled CLAUDE.md | Maven Archetype |
|--------|-------------------|-----------------|
| **Discoverability** | Manual extraction needed | Standard `mvn archetype:generate` |
| **Immediate Value** | Documentation only | Complete working project |
| **Learning Curve** | Must build from scratch | See working examples immediately |
| **Best Practices** | Described in text | Demonstrated in code |
| **Configuration** | Manual setup required | All dependencies included |
| **Testing** | Must write from scratch | Sample tests provided |
| **Maintenance** | Guide can become outdated | Template validated with each release |

---

## 🎯 **IMPLEMENTATION RECOMMENDATION**

### **HYBRID APPROACH: Best of Both Worlds**

1. **Maven Archetype** (Primary): Complete project template with working examples
2. **Bundled Guide** (Secondary): Include comprehensive CLAUDE.md in core JAR for reference

### **Implementation Steps**

1. ✅ **Create comprehensive guide** (COMPLETED - saved to core module resources)
2. **Create archetype module** in main MetaObjects project
3. **Include guide in archetype** as `.claude/CLAUDE.md`
4. **Add working examples** with User/Order domain
5. **Test archetype generation** and verify complete functionality
6. **Publish archetype** to Maven Central with framework releases

### **Developer Experience**

```bash
# Quick start - get working project immediately
mvn archetype:generate -DarchetypeGroupId=com.metaobjects \
  -DarchetypeArtifactId=metaobjects-application-archetype

# Claude Code assistance - complete guide available
# Generated project includes .claude/CLAUDE.md with everything needed
```

---

## 🏆 **CONCLUSION**

**Maven Archetype is the definitive solution** because it provides:

✅ **Industry Standard**: Follows established Maven project generation patterns
✅ **Complete Project**: Working application, not just documentation
✅ **Best Practices**: Demonstrates proper architecture through code
✅ **Immediate Value**: Builds and runs without additional configuration
✅ **Claude Integration**: Includes comprehensive CLAUDE.md guide
✅ **Maintenance**: Template validated with each framework release

**Next Steps:**
1. Create `metaobjects-archetype` module
2. Implement template project with working examples
3. Test archetype generation process
4. Publish to Maven Central with v6.2.6 release

This approach will dramatically improve MetaObjects adoption by providing developers with a complete, working project template that demonstrates all framework capabilities.