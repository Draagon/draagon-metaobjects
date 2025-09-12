# MetaObjects Developer Guide for Claude AI

## Overview

This guide teaches Claude AI how to use MetaObjects for metadata-driven development. MetaObjects enables creating entity definitions once that generate consistent implementations across multiple programming languages (Java, C#, TypeScript, Python, etc.) with runtime metadata loading capabilities.

## Core Concepts

### 1. MetaObjects Architecture

**MetaObjects is NOT just a code generation framework** - it's a **runtime metadata-driven platform**:

- **Metadata Definition**: Define entities, fields, validation, and behavior in XML/JSON
- **Code Generation**: Generate DTOs, repositories, services, and UI components
- **Runtime Loading**: Services load metadata at runtime and adapt dynamically
- **Cross-Language Support**: Same metadata generates consistent implementations across languages

### 2. Key Components

#### Module Structure (Current v5.1.0)
```
metaobjects/
├── metadata/           # Core metadata types and loading
├── maven-plugin/       # Maven plugin for code generation  
├── core/              # Core functionality and generators
├── om/                # Object Manager for persistence
├── omdb/              # Database Object Manager
├── omnosql/           # NoSQL Object Manager
└── (demo - deprecated) # Use other modules for modern patterns
```

#### MetaObject Types
- **POJO**: Standard Java objects with getters/setters
- **Value**: Dynamic objects with public accessor methods  
- **Data**: Wrapped objects with protected accessors
- **Proxy**: Proxy implementations without concrete classes
- **Mapped**: Works with Map interface objects

## Creating Metadata Models

### 1. Project Structure

**Recommended directory structure for new projects:**
```
your-project/
├── pom.xml
├── src/main/
│   ├── java/
│   └── resources/
│       └── metadata/
│           ├── types/
│           │   └── your-project-types.xml
│           └── models/
│               ├── common.xml
│               ├── user.xml
│               ├── order.xml
│               └── overlays/
│                   ├── database.xml
│                   └── ui.xml
└── target/
    └── generated-sources/
```

### 2. Types Configuration

**Always start with a types configuration file** (`your-project-types.xml`):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<typesConfig xmlns="http://draagon.com/schema/metamodel/v3">
    
    <!-- Import base MetaObjects types -->
    <import>com/draagon/meta/loader/json/metaobjects.types.json</import>
    
    <!-- Project-specific type extensions (optional) -->
    <types>
        <!-- Custom validators -->
        <type name="validator">
            <subTypes>
                <subType name="creditCard" class="com.yourproject.validator.CreditCardValidator">
                    <children>
                        <child type="attr" subType="string" name="cardType"/>
                    </children>
                </subType>
                <subType name="phoneNumber" class="com.yourproject.validator.PhoneNumberValidator">
                    <children>
                        <child type="attr" subType="string" name="countryCode"/>
                    </children>
                </subType>
            </subTypes>
        </type>
    </types>
</typesConfig>
```

### 3. Common Fields Definition

**Create reusable common fields** (`common.xml`):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<metadata xmlns="http://draagon.com/schema/metamodel/v3" 
          package="yourproject::common">

    <!-- Standard ID field -->
    <field name="id" type="long" _isAbstract="true">
        <validator type="required"/>
        <attr name="isKey" type="boolean">true</attr>
    </field>
    
    <!-- Standard name field with validation -->
    <field name="name" type="string" _isAbstract="true">
        <validator type="required"/>
        <validator type="length">
            <attr name="min" type="int">2</attr>
            <attr name="max" type="int">100</attr>
        </validator>
    </field>
    
    <!-- Email field with validation -->
    <field name="email" type="string" _isAbstract="true">
        <validator type="required"/>
        <validator type="regex">
            <attr name="mask">^[A-Za-z0-9+_.-]+@(.+)$</attr>
        </validator>
        <attr name="pii" type="boolean">true</attr>
    </field>
    
    <!-- Audit fields -->
    <field name="createdDate" type="date" _isAbstract="true">
        <attr name="auditField" type="boolean">true</attr>
    </field>
    
    <field name="updatedDate" type="date" _isAbstract="true">
        <attr name="auditField" type="boolean">true</attr>
    </field>
    
    <!-- Version field for optimistic locking -->
    <field name="version" type="int" _isAbstract="true">
        <attr name="versionField" type="boolean">true</attr>
    </field>

</metadata>
```

### 4. Entity Definition

**Create your main entities** (`user.xml`):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<metadata xmlns="http://draagon.com/schema/metamodel/v3" 
          package="yourproject::domain">

    <!-- User Entity -->
    <object name="User" type="pojo">
        
        <!-- Specify the Java class to generate -->
        <attr name="object">com.yourproject.domain.User</attr>
        
        <!-- Primary Key -->
        <key name="primary" keys="id"/>
        
        <!-- Common fields inheritance -->
        <field name="id" super="..::common::id"/>
        <field name="name" super="..::common::name"/>
        <field name="email" super="..::common::email"/>
        <field name="createdDate" super="..::common::createdDate"/>
        <field name="updatedDate" super="..::common::updatedDate"/>
        <field name="version" super="..::common::version"/>
        
        <!-- User-specific fields -->
        <field name="firstName" type="string">
            <validator type="required"/>
            <validator type="length">
                <attr name="min" type="int">1</attr>
                <attr name="max" type="int">50</attr>
            </validator>
            <attr name="pii" type="boolean">true</attr>
        </field>
        
        <field name="lastName" type="string">
            <validator type="required"/>
            <validator type="length">
                <attr name="min" type="int">1</attr>
                <attr name="max" type="int">50</attr>
            </validator>
            <attr name="pii" type="boolean">true</attr>
        </field>
        
        <field name="phoneNumber" type="string">
            <validator type="phoneNumber">
                <attr name="countryCode">US</attr>
            </validator>
            <attr name="pii" type="boolean">true</attr>
        </field>
        
        <field name="isActive" type="boolean" defaultValue="true"/>
        
        <!-- Relationship field -->
        <field name="profileId" type="long" objectRef="UserProfile"/>
        
    </object>
    
    <!-- UserProfile Entity -->
    <object name="UserProfile" type="pojo">
        
        <attr name="object">com.yourproject.domain.UserProfile</attr>
        
        <!-- Primary Key -->
        <key name="primary" keys="id"/>
        
        <!-- Foreign Key back to User -->
        <key type="foreign" name="userKey" keys="userId" foreignObjectRef="User"/>
        
        <!-- Standard fields -->
        <field name="id" super="..::common::id"/>
        <field name="userId" type="long" objectRef="User"/>
        <field name="createdDate" super="..::common::createdDate"/>
        <field name="updatedDate" super="..::common::updatedDate"/>
        <field name="version" super="..::common::version"/>
        
        <!-- Profile-specific fields -->
        <field name="bio" type="string">
            <validator type="length">
                <attr name="max" type="int">500</attr>
            </validator>
        </field>
        
        <field name="avatarUrl" type="string">
            <validator type="regex">
                <attr name="mask">^https?://.*\.(jpg|jpeg|png|gif)$</attr>
            </validator>
        </field>
        
        <field name="preferences" type="stringArray"/>
        
    </object>

</metadata>
```

### 5. Overlay System for Different Concerns

**Database Overlay** (`overlays/database.xml`):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<metadata xmlns="http://draagon.com/schema/metamodel/v3" 
          package="yourproject::database">

    <!-- Database-specific attributes for User -->
    <object name="User">
        <attr name="tableName">users</attr>
        <attr name="schema">public</attr>
        
        <!-- Database column mappings -->
        <field name="email">
            <attr name="columnName">email_address</attr>
            <attr name="unique" type="boolean">true</attr>
            <attr name="index">idx_user_email</attr>
        </field>
        
        <field name="firstName">
            <attr name="columnName">first_name</attr>
        </field>
        
        <field name="lastName">
            <attr name="columnName">last_name</attr>
        </field>
        
        <field name="phoneNumber">
            <attr name="columnName">phone_number</attr>
        </field>
        
        <field name="isActive">
            <attr name="columnName">is_active</attr>
            <attr name="defaultValue">true</attr>
        </field>
        
        <field name="createdDate">
            <attr name="columnName">created_date</attr>
            <attr name="insertable" type="boolean">true</attr>
            <attr name="updatable" type="boolean">false</attr>
        </field>
        
        <field name="updatedDate">
            <attr name="columnName">updated_date</attr>
        </field>
    </object>
    
    <!-- Database attributes for UserProfile -->
    <object name="UserProfile">
        <attr name="tableName">user_profiles</attr>
        <attr name="schema">public</attr>
        
        <field name="userId">
            <attr name="columnName">user_id</attr>
            <attr name="foreignKey">fk_profile_user</attr>
        </field>
        
        <field name="avatarUrl">
            <attr name="columnName">avatar_url</attr>
        </field>
    </object>

</metadata>
```

**UI Overlay** (`overlays/ui.xml`):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<metadata xmlns="http://draagon.com/schema/metamodel/v3" 
          package="yourproject::ui">

    <!-- UI-specific attributes for User -->
    <object name="User">
        <attr name="uiTitle">User Management</attr>
        <attr name="uiIcon">user</attr>
        
        <field name="email">
            <attr name="uiLabel">Email Address</attr>
            <attr name="uiType">email</attr>
            <attr name="uiPlaceholder">Enter email address</attr>
            <attr name="uiValidationMessage">Please enter a valid email address</attr>
            <attr name="uiRequired" type="boolean">true</attr>
        </field>
        
        <field name="firstName">
            <attr name="uiLabel">First Name</attr>
            <attr name="uiType">text</attr>
            <attr name="uiPlaceholder">Enter first name</attr>
            <attr name="uiRequired" type="boolean">true</attr>
        </field>
        
        <field name="lastName">
            <attr name="uiLabel">Last Name</attr>
            <attr name="uiType">text</attr>
            <attr name="uiPlaceholder">Enter last name</attr>
            <attr name="uiRequired" type="boolean">true</attr>
        </field>
        
        <field name="phoneNumber">
            <attr name="uiLabel">Phone Number</attr>
            <attr name="uiType">tel</attr>
            <attr name="uiPlaceholder">(555) 123-4567</attr>
            <attr name="uiPattern">\(\d{3}\) \d{3}-\d{4}</attr>
        </field>
        
        <field name="isActive">
            <attr name="uiLabel">Active User</attr>
            <attr name="uiType">checkbox</attr>
            <attr name="uiHelp">Inactive users cannot log in</attr>
        </field>
    </object>
    
    <!-- UI attributes for UserProfile -->
    <object name="UserProfile">
        <attr name="uiTitle">User Profile</attr>
        <attr name="uiIcon">id-card</attr>
        
        <field name="bio">
            <attr name="uiLabel">Biography</attr>
            <attr name="uiType">textarea</attr>
            <attr name="uiPlaceholder">Tell us about yourself...</attr>
            <attr name="uiRows" type="int">4</attr>
        </field>
        
        <field name="avatarUrl">
            <attr name="uiLabel">Profile Picture</attr>
            <attr name="uiType">file</attr>
            <attr name="uiAccept">image/*</attr>
        </field>
        
        <field name="preferences">
            <attr name="uiLabel">Preferences</attr>
            <attr name="uiType">multi-select</attr>
            <attr name="uiOptions">["notifications", "marketing", "newsletter", "updates"]</attr>
        </field>
    </object>

</metadata>
```

## Best Practices for Metadata Design

### 1. Package Organization
```
yourproject::common          # Reusable fields and components
yourproject::domain          # Core business entities  
yourproject::database        # Database-specific overlays
yourproject::ui              # User interface overlays
yourproject::api             # API-specific overlays
yourproject::integration     # External system integration
```

### 2. Field Design Patterns

**Always use inheritance for common fields:**
```xml
<!-- DON'T repeat field definitions -->
<field name="id" type="long">
    <validator type="required"/>
    <attr name="isKey" type="boolean">true</attr>
</field>

<!-- DO inherit from common definitions -->
<field name="id" super="..::common::id"/>
```

**Use validation consistently:**
```xml
<!-- Combine built-in and custom validators -->
<field name="creditCardNumber" type="string">
    <validator type="required"/>
    <validator type="creditCard">
        <attr name="cardType">visa</attr>
    </validator>
    <attr name="pii" type="boolean">true</attr>
</field>
```

### 3. Relationship Modeling

**Use proper key relationships:**
```xml
<!-- Parent entity -->
<object name="Customer">
    <key name="primary" keys="id"/>
    <!-- fields... -->
</object>

<!-- Child entity with foreign key -->
<object name="Order">
    <key name="primary" keys="id"/>
    <key type="foreign" name="customerKey" keys="customerId" foreignObjectRef="Customer"/>
    
    <field name="customerId" type="long" objectRef="Customer"/>
    <!-- other fields... -->
</object>
```

### 4. PII and Security

**Always mark PII fields:**
```xml
<field name="socialSecurityNumber" type="string">
    <validator type="required"/>
    <attr name="pii" type="boolean">true</attr>
    <attr name="encrypted" type="boolean">true</attr>
    <attr name="accessLevel">restricted</attr>
</field>
```

This guide provides the foundation for creating metadata models. Continue with the Maven configuration guide for setting up code generation.