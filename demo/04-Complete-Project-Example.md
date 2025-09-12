# Complete MetaObjects Project Example

## Overview

This guide shows how to create a complete MetaObjects-powered project from scratch. We'll build a **Customer Management System** with entities, validation, persistence, and a REST API that demonstrates both code generation and runtime metadata usage.

## Project Structure

```
customer-management/
├── pom.xml                                 # Parent POM
├── domain/                                 # Domain layer with code generation
│   ├── pom.xml
│   └── src/main/
│       ├── java/
│       └── resources/
│           └── metadata/
│               ├── types/
│               │   └── customer-types.xml
│               ├── models/
│               │   ├── common.xml
│               │   ├── customer.xml
│               │   └── order.xml
│               └── overlays/
│                   ├── database.xml
│                   └── api.xml
├── service/                                # Service layer
│   ├── pom.xml
│   └── src/main/java/
└── web/                                    # Web layer
    ├── pom.xml
    └── src/main/java/
```

## Step 1: Parent POM Configuration

**File: `pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.example</groupId>
    <artifactId>customer-management</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    
    <name>Customer Management System</name>
    <description>MetaObjects-powered customer management application</description>
    
    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>${java.version}</maven.compiler.source>
        <maven.compiler.target>${java.version}</maven.compiler.target>
        <maven.compiler.release>${java.version}</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        
        <!-- Version Properties -->
        <metaobjects.version>5.1.0</metaobjects.version>
        <spring-boot.version>3.2.5</spring-boot.version>
        <hibernate.version>6.4.4.Final</hibernate.version>
    </properties>
    
    <modules>
        <module>domain</module>
        <module>service</module>
        <module>web</module>
    </modules>
    
    <dependencyManagement>
        <dependencies>
            <!-- Spring Boot BOM -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            
            <!-- MetaObjects Dependencies -->
            <dependency>
                <groupId>com.draagon</groupId>
                <artifactId>metaobjects-core</artifactId>
                <version>${metaobjects.version}</version>
            </dependency>
            
            <dependency>
                <groupId>com.draagon</groupId>
                <artifactId>metaobjects-metadata</artifactId>
                <version>${metaobjects.version}</version>
            </dependency>
            
            <dependency>
                <groupId>com.draagon</groupId>
                <artifactId>metaobjects-omdb</artifactId>
                <version>${metaobjects.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>com.draagon</groupId>
                    <artifactId>metaobjects-maven-plugin</artifactId>
                    <version>${metaobjects.version}</version>
                </plugin>
                
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <version>${spring-boot.version}</version>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>

</project>
```

## Step 2: Domain Module with Metadata

**File: `domain/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.example</groupId>
        <artifactId>customer-management</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    
    <artifactId>customer-management-domain</artifactId>
    
    <dependencies>
        <!-- MetaObjects -->
        <dependency>
            <groupId>com.draagon</groupId>
            <artifactId>metaobjects-core</artifactId>
        </dependency>
        
        <dependency>
            <groupId>com.draagon</groupId>
            <artifactId>metaobjects-metadata</artifactId>
        </dependency>
        
        <!-- JPA/Hibernate -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- JSON Processing -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <!-- MetaObjects Code Generation -->
            <plugin>
                <groupId>com.draagon</groupId>
                <artifactId>metaobjects-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>generate-entities</id>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                        <configuration>
                            <globals>
                                <verbose>false</verbose>
                                <strict>true</strict>
                            </globals>
                            <loader>
                                <classname>com.draagon.meta.loader.file.FileMetaDataLoader</classname>
                                <name>customer-domain</name>
                                <sources>
                                    <!-- Load metadata files in order -->
                                    <source>metadata/types/customer-types.xml</source>
                                    <source>metadata/models/common.xml</source>
                                    <source>metadata/models/customer.xml</source>
                                    <source>metadata/models/order.xml</source>
                                    <source>metadata/overlays/database.xml</source>
                                    <source>metadata/overlays/api.xml</source>
                                </sources>
                            </loader>
                            <generators>
                                <!-- Generate JPA Entities -->
                                <generator>
                                    <classname>com.example.generator.JPAEntityGenerator</classname>
                                    <args>
                                        <basePackage>com.example.domain.entity</basePackage>
                                        <outputDir>${project.build.directory}/generated-sources/java</outputDir>
                                    </args>
                                    <filters>
                                        <filter>customer::domain::*</filter>
                                    </filters>
                                </generator>
                                
                                <!-- Generate DTOs -->
                                <generator>
                                    <classname>com.example.generator.DTOGenerator</classname>
                                    <args>
                                        <basePackage>com.example.domain.dto</basePackage>
                                        <outputDir>${project.build.directory}/generated-sources/java</outputDir>
                                    </args>
                                    <filters>
                                        <filter>customer::domain::*</filter>
                                    </filters>
                                </generator>
                            </generators>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            
            <!-- Add generated sources -->
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>build-helper-maven-plugin</artifactId>
                <version>3.5.0</version>
                <executions>
                    <execution>
                        <id>add-generated-source</id>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>add-source</goal>
                        </goals>
                        <configuration>
                            <sources>
                                <source>${project.build.directory}/generated-sources/java</source>
                            </sources>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>
```

## Step 3: Metadata Definitions

**File: `domain/src/main/resources/metadata/types/customer-types.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<typesConfig xmlns="http://draagon.com/schema/metamodel/v3">
    
    <!-- Import base MetaObjects types -->
    <import>com/draagon/meta/loader/json/metaobjects.types.json</import>
    
    <!-- Custom validators for customer domain -->
    <types>
        <type name="validator">
            <subTypes>
                <!-- Credit card validator -->
                <subType name="creditCard" class="com.example.validator.CreditCardValidator">
                    <children>
                        <child type="attr" subType="string" name="cardType"/>
                    </children>
                </subType>
                
                <!-- Phone number validator -->
                <subType name="phone" class="com.example.validator.PhoneNumberValidator">
                    <children>
                        <child type="attr" subType="string" name="region"/>
                    </children>
                </subType>
                
                <!-- Currency amount validator -->
                <subType name="currency" class="com.example.validator.CurrencyValidator">
                    <children>
                        <child type="attr" subType="string" name="currencyCode"/>
                        <child type="attr" subType="double" name="minAmount"/>
                        <child type="attr" subType="double" name="maxAmount"/>
                    </children>
                </subType>
            </subTypes>
        </type>
    </types>
</typesConfig>
```

**File: `domain/src/main/resources/metadata/models/common.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<metadata xmlns="http://draagon.com/schema/metamodel/v3" 
          package="customer::common">

    <!-- Standard ID field -->
    <field name="id" type="long" _isAbstract="true">
        <validator type="required"/>
        <attr name="isKey" type="boolean">true</attr>
    </field>
    
    <!-- Standard name field -->
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
            <attr name="mask">^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\.[A-Za-z]{2,})$</attr>
        </validator>
        <attr name="pii" type="boolean">true</attr>
    </field>
    
    <!-- Phone number field -->
    <field name="phone" type="string" _isAbstract="true">
        <validator type="phone">
            <attr name="region">US</attr>
        </validator>
        <attr name="pii" type="boolean">true</attr>
    </field>
    
    <!-- Address field -->
    <field name="address" type="string" _isAbstract="true">
        <validator type="length">
            <attr name="max" type="int">500</attr>
        </validator>
        <attr name="pii" type="boolean">true</attr>
    </field>
    
    <!-- Audit timestamp fields -->
    <field name="createdAt" type="date" _isAbstract="true">
        <attr name="auditField" type="boolean">true</attr>
        <attr name="insertable" type="boolean">true</attr>
        <attr name="updatable" type="boolean">false</attr>
    </field>
    
    <field name="updatedAt" type="date" _isAbstract="true">
        <attr name="auditField" type="boolean">true</attr>
    </field>
    
    <!-- Version field for optimistic locking -->
    <field name="version" type="int" _isAbstract="true">
        <attr name="versionField" type="boolean">true</attr>
    </field>
    
    <!-- Currency amount field -->
    <field name="amount" type="double" _isAbstract="true">
        <validator type="currency">
            <attr name="currencyCode">USD</attr>
            <attr name="minAmount" type="double">0.01</attr>
            <attr name="maxAmount" type="double">999999.99</attr>
        </validator>
    </field>

</metadata>
```

**File: `domain/src/main/resources/metadata/models/customer.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<metadata xmlns="http://draagon.com/schema/metamodel/v3" 
          package="customer::domain">

    <!-- Customer Entity -->
    <object name="Customer" type="pojo">
        <attr name="object">com.example.domain.entity.Customer</attr>
        <attr name="description">Represents a customer in the system</attr>
        
        <!-- Primary Key -->
        <key name="primary" keys="id"/>
        
        <!-- Standard fields -->
        <field name="id" super="..::common::id"/>
        <field name="createdAt" super="..::common::createdAt"/>
        <field name="updatedAt" super="..::common::updatedAt"/>
        <field name="version" super="..::common::version"/>
        
        <!-- Customer-specific fields -->
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
        
        <field name="email" super="..::common::email"/>
        <field name="phone" super="..::common::phone"/>
        <field name="address" super="..::common::address"/>
        
        <field name="customerType" type="string" defaultValue="INDIVIDUAL">
            <validator type="regex">
                <attr name="mask">^(INDIVIDUAL|BUSINESS|PREMIUM)$</attr>
            </validator>
        </field>
        
        <field name="isActive" type="boolean" defaultValue="true"/>
        
        <field name="creditLimit" super="..::common::amount">
            <validator type="currency">
                <attr name="minAmount" type="double">0</attr>
                <attr name="maxAmount" type="double">100000</attr>
            </validator>
        </field>
    </object>
    
    <!-- CustomerAddress Entity (One-to-Many) -->
    <object name="CustomerAddress" type="pojo">
        <attr name="object">com.example.domain.entity.CustomerAddress</attr>
        
        <!-- Primary Key -->
        <key name="primary" keys="id"/>
        
        <!-- Foreign Key to Customer -->
        <key type="foreign" name="customerKey" keys="customerId" foreignObjectRef="Customer"/>
        
        <!-- Fields -->
        <field name="id" super="..::common::id"/>
        <field name="customerId" type="long" objectRef="Customer"/>
        <field name="createdAt" super="..::common::createdAt"/>
        <field name="updatedAt" super="..::common::updatedAt"/>
        
        <field name="addressType" type="string">
            <validator type="required"/>
            <validator type="regex">
                <attr name="mask">^(BILLING|SHIPPING|BUSINESS)$</attr>
            </validator>
        </field>
        
        <field name="streetAddress" type="string">
            <validator type="required"/>
            <validator type="length">
                <attr name="max" type="int">200</attr>
            </validator>
            <attr name="pii" type="boolean">true</attr>
        </field>
        
        <field name="city" type="string">
            <validator type="required"/>
            <validator type="length">
                <attr name="max" type="int">100</attr>
            </validator>
        </field>
        
        <field name="state" type="string">
            <validator type="required"/>
            <validator type="length">
                <attr name="min" type="int">2</attr>
                <attr name="max" type="int">50</attr>
            </validator>
        </field>
        
        <field name="zipCode" type="string">
            <validator type="required"/>
            <validator type="regex">
                <attr name="mask">^\d{5}(-\d{4})?$</attr>
            </validator>
        </field>
        
        <field name="country" type="string" defaultValue="US">
            <validator type="length">
                <attr name="min" type="int">2</attr>
                <attr name="max" type="int">3</attr>
            </validator>
        </field>
        
        <field name="isPrimary" type="boolean" defaultValue="false"/>
    </object>

</metadata>
```

**File: `domain/src/main/resources/metadata/models/order.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<metadata xmlns="http://draagon.com/schema/metamodel/v3" 
          package="customer::domain">

    <!-- Order Entity -->
    <object name="Order" type="pojo">
        <attr name="object">com.example.domain.entity.Order</attr>
        
        <!-- Primary Key -->
        <key name="primary" keys="id"/>
        
        <!-- Foreign Key to Customer -->
        <key type="foreign" name="customerKey" keys="customerId" foreignObjectRef="Customer"/>
        
        <!-- Fields -->
        <field name="id" super="..::common::id"/>
        <field name="customerId" type="long" objectRef="Customer"/>
        <field name="createdAt" super="..::common::createdAt"/>
        <field name="updatedAt" super="..::common::updatedAt"/>
        <field name="version" super="..::common::version"/>
        
        <field name="orderNumber" type="string">
            <validator type="required"/>
            <validator type="length">
                <attr name="min" type="int">5</attr>
                <attr name="max" type="int">20</attr>
            </validator>
            <attr name="unique" type="boolean">true</attr>
        </field>
        
        <field name="orderDate" type="date">
            <validator type="required"/>
        </field>
        
        <field name="status" type="string" defaultValue="PENDING">
            <validator type="required"/>
            <validator type="regex">
                <attr name="mask">^(PENDING|CONFIRMED|SHIPPED|DELIVERED|CANCELLED)$</attr>
            </validator>
        </field>
        
        <field name="totalAmount" super="..::common::amount">
            <validator type="required"/>
        </field>
        
        <field name="taxAmount" super="..::common::amount"/>
        
        <field name="shippingAmount" super="..::common::amount"/>
        
        <field name="notes" type="string">
            <validator type="length">
                <attr name="max" type="int">1000</attr>
            </validator>
        </field>
    </object>
    
    <!-- OrderItem Entity (One-to-Many) -->
    <object name="OrderItem" type="pojo">
        <attr name="object">com.example.domain.entity.OrderItem</attr>
        
        <!-- Primary Key -->
        <key name="primary" keys="id"/>
        
        <!-- Foreign Key to Order -->
        <key type="foreign" name="orderKey" keys="orderId" foreignObjectRef="Order"/>
        
        <!-- Fields -->
        <field name="id" super="..::common::id"/>
        <field name="orderId" type="long" objectRef="Order"/>
        
        <field name="productSku" type="string">
            <validator type="required"/>
            <validator type="length">
                <attr name="max" type="int">50</attr>
            </validator>
        </field>
        
        <field name="productName" type="string">
            <validator type="required"/>
            <validator type="length">
                <attr name="max" type="int">200</attr>
            </validator>
        </field>
        
        <field name="quantity" type="int">
            <validator type="required"/>
            <validator type="numeric">
                <attr name="min" type="int">1</attr>
                <attr name="max" type="int">999</attr>
            </validator>
        </field>
        
        <field name="unitPrice" super="..::common::amount">
            <validator type="required"/>
        </field>
        
        <field name="totalPrice" super="..::common::amount">
            <validator type="required"/>
        </field>
    </object>

</metadata>
```

## Step 4: Database Overlay

**File: `domain/src/main/resources/metadata/overlays/database.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<metadata xmlns="http://draagon.com/schema/metamodel/v3" 
          package="customer::database">

    <!-- Customer database mapping -->
    <object name="Customer">
        <attr name="tableName">customers</attr>
        <attr name="schema">public</attr>
        
        <!-- Column mappings -->
        <field name="firstName">
            <attr name="columnName">first_name</attr>
        </field>
        
        <field name="lastName">
            <attr name="columnName">last_name</attr>
        </field>
        
        <field name="customerType">
            <attr name="columnName">customer_type</attr>
            <attr name="enumType">CustomerType</attr>
        </field>
        
        <field name="isActive">
            <attr name="columnName">is_active</attr>
        </field>
        
        <field name="creditLimit">
            <attr name="columnName">credit_limit</attr>
            <attr name="precision" type="int">10</attr>
            <attr name="scale" type="int">2</attr>
        </field>
        
        <field name="createdAt">
            <attr name="columnName">created_at</attr>
            <attr name="insertable" type="boolean">true</attr>
            <attr name="updatable" type="boolean">false</attr>
        </field>
        
        <field name="updatedAt">
            <attr name="columnName">updated_at</attr>
        </field>
    </object>
    
    <!-- CustomerAddress database mapping -->
    <object name="CustomerAddress">
        <attr name="tableName">customer_addresses</attr>
        <attr name="schema">public</attr>
        
        <field name="customerId">
            <attr name="columnName">customer_id</attr>
            <attr name="foreignKey">fk_address_customer</attr>
        </field>
        
        <field name="addressType">
            <attr name="columnName">address_type</attr>
            <attr name="enumType">AddressType</attr>
        </field>
        
        <field name="streetAddress">
            <attr name="columnName">street_address</attr>
        </field>
        
        <field name="zipCode">
            <attr name="columnName">zip_code</attr>
        </field>
        
        <field name="isPrimary">
            <attr name="columnName">is_primary</attr>
        </field>
        
        <field name="createdAt">
            <attr name="columnName">created_at</attr>
        </field>
        
        <field name="updatedAt">
            <attr name="columnName">updated_at</attr>
        </field>
    </object>
    
    <!-- Order database mapping -->
    <object name="Order">
        <attr name="tableName">orders</attr>
        <attr name="schema">public</attr>
        
        <field name="customerId">
            <attr name="columnName">customer_id</attr>
            <attr name="foreignKey">fk_order_customer</attr>
        </field>
        
        <field name="orderNumber">
            <attr name="columnName">order_number</attr>
            <attr name="unique" type="boolean">true</attr>
            <attr name="index">idx_order_number</attr>
        </field>
        
        <field name="orderDate">
            <attr name="columnName">order_date</attr>
        </field>
        
        <field name="totalAmount">
            <attr name="columnName">total_amount</attr>
            <attr name="precision" type="int">10</attr>
            <attr name="scale" type="int">2</attr>
        </field>
        
        <field name="taxAmount">
            <attr name="columnName">tax_amount</attr>
            <attr name="precision" type="int">10</attr>
            <attr name="scale" type="int">2</attr>
        </field>
        
        <field name="shippingAmount">
            <attr name="columnName">shipping_amount</attr>
            <attr name="precision" type="int">10</attr>
            <attr name="scale" type="int">2</attr>
        </field>
        
        <field name="createdAt">
            <attr name="columnName">created_at</attr>
        </field>
        
        <field name="updatedAt">
            <attr name="columnName">updated_at</attr>
        </field>
    </object>
    
    <!-- OrderItem database mapping -->
    <object name="OrderItem">
        <attr name="tableName">order_items</attr>
        <attr name="schema">public</attr>
        
        <field name="orderId">
            <attr name="columnName">order_id</attr>
            <attr name="foreignKey">fk_item_order</attr>
        </field>
        
        <field name="productSku">
            <attr name="columnName">product_sku</attr>
        </field>
        
        <field name="productName">
            <attr name="columnName">product_name</attr>
        </field>
        
        <field name="unitPrice">
            <attr name="columnName">unit_price</attr>
            <attr name="precision" type="int">10</attr>
            <attr name="scale" type="int">2</attr>
        </field>
        
        <field name="totalPrice">
            <attr name="columnName">total_price</attr>
            <attr name="precision" type="int">10</attr>
            <attr name="scale" type="int">2</attr>
        </field>
    </object>

</metadata>
```

## Step 5: API Overlay

**File: `domain/src/main/resources/metadata/overlays/api.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<metadata xmlns="http://draagon.com/schema/metamodel/v3" 
          package="customer::api">

    <!-- Customer API representation -->
    <object name="Customer">
        <attr name="apiPath">/api/v1/customers</attr>
        <attr name="apiDescription">Customer management endpoints</attr>
        
        <!-- API serialization settings -->
        <field name="id">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiReadOnly" type="boolean">true</attr>
        </field>
        
        <field name="firstName">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiLabel">First Name</attr>
            <attr name="apiExample">John</attr>
        </field>
        
        <field name="lastName">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiLabel">Last Name</attr>
            <attr name="apiExample">Doe</attr>
        </field>
        
        <field name="email">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiLabel">Email Address</attr>
            <attr name="apiExample">john.doe@example.com</attr>
        </field>
        
        <field name="phone">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiLabel">Phone Number</attr>
            <attr name="apiExample">(555) 123-4567</attr>
        </field>
        
        <field name="address">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiLabel">Primary Address</attr>
        </field>
        
        <field name="customerType">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiLabel">Customer Type</attr>
            <attr name="apiEnum">["INDIVIDUAL", "BUSINESS", "PREMIUM"]</attr>
        </field>
        
        <field name="isActive">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiLabel">Active Status</attr>
        </field>
        
        <field name="creditLimit">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiLabel">Credit Limit</attr>
            <attr name="apiFormat">currency</attr>
        </field>
        
        <field name="createdAt">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiReadOnly" type="boolean">true</attr>
            <attr name="apiFormat">date-time</attr>
        </field>
        
        <field name="updatedAt">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiReadOnly" type="boolean">true</attr>
            <attr name="apiFormat">date-time</attr>
        </field>
        
        <!-- Version field excluded from API -->
        <field name="version">
            <attr name="apiSerialize" type="boolean">false</attr>
        </field>
    </object>
    
    <!-- Order API representation -->
    <object name="Order">
        <attr name="apiPath">/api/v1/orders</attr>
        <attr name="apiDescription">Order management endpoints</attr>
        
        <field name="id">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiReadOnly" type="boolean">true</attr>
        </field>
        
        <field name="customerId">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiLabel">Customer ID</attr>
        </field>
        
        <field name="orderNumber">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiReadOnly" type="boolean">true</attr>
            <attr name="apiLabel">Order Number</attr>
        </field>
        
        <field name="orderDate">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiLabel">Order Date</attr>
            <attr name="apiFormat">date</attr>
        </field>
        
        <field name="status">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiLabel">Order Status</attr>
            <attr name="apiEnum">["PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"]</attr>
        </field>
        
        <field name="totalAmount">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiLabel">Total Amount</attr>
            <attr name="apiFormat">currency</attr>
        </field>
        
        <field name="taxAmount">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiLabel">Tax Amount</attr>
            <attr name="apiFormat">currency</attr>
        </field>
        
        <field name="shippingAmount">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiLabel">Shipping Amount</attr>
            <attr name="apiFormat">currency</attr>
        </field>
        
        <field name="notes">
            <attr name="apiSerialize" type="boolean">true</attr>
            <attr name="apiLabel">Order Notes</attr>
        </field>
    </object>

</metadata>
```

## Step 6: Build and Generate

**Execute the build to generate all code:**

```bash
# From the root directory
mvn clean compile

# This will:
# 1. Load all metadata files in the specified order
# 2. Generate JPA entities with proper annotations
# 3. Generate DTOs for API usage
# 4. Generate repository interfaces
# 5. Compile all generated and hand-written code
```

## Step 7: Generated Code Examples

**Generated JPA Entity (example output):**

```java
// target/generated-sources/java/com/example/domain/entity/Customer.java
package com.example.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "customers", schema = "public")
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "first_name", nullable = false, length = 50)
    @NotNull
    @Size(min = 1, max = 50)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 50)
    @NotNull
    @Size(min = 1, max = 50)
    private String lastName;
    
    @Column(nullable = false, unique = true)
    @NotNull
    @Email
    private String email;
    
    @Column
    private String phone;
    
    @Column
    private String address;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false)
    private CustomerType customerType = CustomerType.INDIVIDUAL;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "credit_limit", precision = 10, scale = 2)
    private Double creditLimit;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Integer version;
    
    // Constructors, getters, setters, equals, hashCode generated...
}
```

This example shows how to create a complete MetaObjects project with:

1. **Proper Maven configuration** with code generation
2. **Comprehensive metadata models** with validation and relationships  
3. **Overlay system** for database and API concerns
4. **Generated JPA entities** with full annotations
5. **Generated DTOs** for API usage
6. **Runtime metadata loading** capabilities for dynamic behavior

The generated code provides compile-time safety while the metadata enables runtime adaptation - the best of both approaches!