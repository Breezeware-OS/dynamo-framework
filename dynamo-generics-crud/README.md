# Dynamo Generics CRUD Module Documentation



## Overview

This document provides an overview of the `dynamo-generics-crud` module, detailing relevant dependencies, plugins, and properties needed for a custom application build. It also explains the module’s structure, vocabulary, technologies used, sample use cases, and a guide to getting started.

### Relevant Dependencies, Plugins, and Properties

#### Dependencies
Ensure that the following dependencies are included in the `pom.xml` file:
- **Spring Boot:** Provides the core framework for building the module.
- **Spring Boot JPA:** Manages the ORM of entities.
- **PostgreSQL:** Database used for storing entities.
- 
- 

```xml
<dependencies>
    <!-- Spring Boot Dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Hibernate Dependency -->
    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-core</artifactId>
    </dependency>
    
    <!-- PostgreSQL Dependency -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
     
</dependencies>
```

#### Plugins
The following plugins are essential for building and packaging the module:
- **Maven Compiler Plugin**
- **Spring Boot Maven Plugin**

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <source>17</source>
                <target>17</target>
            </configuration>
        </plugin>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

### Structure of Dynamo Generics CRUD Module

#### Module Intro
The `dynamo-generics-crud` module provides a generic CRUD (Create, Read, Update, Delete) functionality, offering a reusable framework that can be extended for various entities across different applications.

#### Vocabulary/Terminology
- **CRUD:** Standard operations for data management: Create, Read, Update, Delete.
- **Entity:** A class representing a database table.
- **Repository:** Interface for managing CRUD operations.
- **Service:** Business logic layer that interacts with the repository.
- **Controller:** Exposes the CRUD operations through RESTful APIs.

#### Technologies/3rd Party Libraries Used
- **Spring Boot:** Core framework used for dependency injection, web services, and data access.
- **Hibernate:** ORM framework used to map Java classes to database tables.
- **PostgreSQL:** RDBMS used for data storage.

#### Sample Use Cases
- **Generic Entity Management:** Quickly set up CRUD operations for any entity by extending `GenericEntity`, `GenericRepository`, `GenericService`, and `GenericController`.
- **Database Operations:** Efficiently perform database operations without writing boilerplate code.

### Getting Started with the Module

1. **Extend the Generic Classes:**  
   Create your entity by extending `GenericEntity` and define your attributes.
   ```java
   public class MyEntity extends GenericEntity {
       private String name;
       // getters and setters
   }
   ```

2. **Create a Repository:**  
   Extend `GenericRepository` to manage the entity.
   ```java
   public interface MyEntityRepository extends GenericRepository<MyEntity, Long> {
   }
   ```

3. **Implement the Service:**  
   Extend `GenericService` to provide business logic.
   ```java
   public class MyEntityService extends GenericService<MyEntity, Long> {
   }
   ```

4. **Set Up the Controller:**  
   Create a REST controller by extending `GenericController`.
   ```java
   @RestController
   @RequestMapping("/api/my-entity")
   public class MyEntityController extends GenericController<MyEntity, Long> {
   }
   ```

5. **Build and Run the Application:**  
   Use Maven to build and run the application.
   ```bash
   mvn clean install
     ```

---

This documentation structure provides a clear and concise guide to understanding and utilizing the `dynamo-generics-crud` module, helping developers quickly get up to speed.