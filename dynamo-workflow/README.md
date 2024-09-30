# Dynamo WorkFlow Documentation

---

## Overview

This document provides an overview of the relevant dependencies, plugins, and properties required for building a custom application using the Dynamo workflow. 
It includes a detailed structure for each Dynamo module, terminology used, technologies integrated, sample use cases, and guidelines to get started.

## Prerequisites

- Java 17.x
- Maven 3.8.x
- PostgreSQL 12.x
- Dynamo Utils

### Relevant Dependencies, Plugins, and Properties
- **org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-webapp:7.21.0**
Provides integration between Camunda BPM and Spring Boot, allowing you to manage and deploy BPMN processes within a Spring Boot application.

- **spring-boot-starter-data-jpa**
A utility library specific to the Dynamo platform, excluding the Spring Boot Web starter to keep the module lightweight.

- **org.postgresql**
PostgreSQL JDBC driver for connecting and interacting with PostgreSQL databases.

- **io.hypersistence:hypersistence-utils-hibernate-63:3.7.4**
A utility library that enhances Hibernate capabilities, particularly for JPA and Hibernate users.

- **com.fasterxml.jackson.core**
Core Jackson library for processing JSON in Java, allowing for JSON serialization and deserialization.

- **com.fasterxml.jackson.module**
Adds support for Jakarta XML Binding annotations, enabling the seamless integration of XML data with Jackson

---

## Module: dynamo-workflow

### Module Introduction

The Dynamo workflow is designed to handle specific tasks in the overall process management system.

### Vocabulary/Terminology

- **DAO (Data Access Object)**: Objects responsible for accessing and managing data from the database.
- **DTO (Data Transfer Object)**: Objects that carry data between processes.
- **Entity**: An object representing a domain model, usually persisted in a database.
- **Service**: A layer responsible for business logic and operations.
- **Task Manager**: Component responsible for managing tasks within a process workflow

### Technologies/3rd Party Libraries

- **Camunda BPM**: For workflow and decision automation.
- **Spring Boot**: Application framework for building the service layer.
- **Hibernate**: ORM tool for database interactions.

###  Sample Use Cases Implemented/That Could Be Implemented
- **CamundaProcessManager**: Provides common functionality using Camunda.
- **Entity Generator**: The service parse and save the JSON and save the JSON in ProcessDomainEntity
- **ProcessService**: Provides concrete methods and abstract methods . 

### Getting Started

1. **Clone the repository**:
   ```bash
   git clone git@github.com:Breezeware-OS/dynamo.git
   ```
2. **Navigate to the module directory**:
   ```bash
   cd backend/dynamo-sdk-lib/dynamo-workflow
   ```
3. **Build the module**:
   ```bash
   mvn clean install
   ```
