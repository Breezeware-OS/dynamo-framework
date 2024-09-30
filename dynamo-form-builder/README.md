# Dynamo Form Builder Documentation


---

## Overview

This document provides an overview of the **dynamo-form-builder** project, including relevant dependencies, plugins, and
properties for custom application builds. The project is divided into multiple modules, each serving a distinct purpose.

## Prerequisites

- Java 17.x
- Maven 3.8.x
- PostgreSQL 12.x
- Dynamo Generics Crud

### Relevant Dependencies, Plugins, and Properties

- **Jackson**: For JSON processing.
- **Flyway**: For DB Migration

---

## Module: dynamo-form-builder-bff

### Module Introduction

The `dynamo-form-builder-bff` is the backend-for-frontend module responsible for handling client requests and
communicating with the service layer. It provides RESTful APIs to manage forms, invitations, and responses.

### Vocabulary/Terminology

- **BFF (Backend for Frontend)**: A pattern where a specific backend service is created to serve a particular frontend.
- **DTO (Data Transfer Object)**: Objects used to transfer data between different parts of the application.

### Technologies/3rd Party Libraries

- **Spring Web**: For building web applications.
- **OpenAPI (springdoc-openapi)**: For API documentation.

### Sample Use Cases

- **Form Management**: Create, update, delete, and retrieve forms.
- **Invitation Handling**: Manage form invitations, including sending, updating, and tracking responses.
- **Response Management**: Collect and process form responses.

## Module: dynamo-form-builder-service

### Module Introduction

The `dynamo-form-builder-service` module is responsible for the core business logic and data management. It interacts
with the database to perform CRUD operations on forms, invitations, and responses.

### Vocabulary/Terminology

- **DAO (Data Access Object)**: A pattern that provides an abstract interface to some type of database or other
  persistence mechanism.
- **Entity**: A lightweight persistent domain object.

### Technologies/3rd Party Libraries

- **Spring Data JPA**: For database interactions.
- **Hibernate**: ORM (Object-Relational Mapping) framework.
- **Jackson**: For JSON serialization and deserialization.

### Sample Use Cases

- **CRUD Operations**: Perform create, read, update, and delete operations on forms, invitations, and responses.
- **Version Management**: Handle multiple versions of a form, including versioning and version comparison.

### Getting Started

1. **Clone the repository**:
   ```bash
   git clone git@github.com:Breezeware-OS/dynamo.git
   ```
2. **Navigate to the module directory**:
   ```bash
   cd backend/dynamo-sdk-lib/dynamo-form-builder
   ```
3. **Build the module**:
   ```bash
   mvn clean install
   ```
