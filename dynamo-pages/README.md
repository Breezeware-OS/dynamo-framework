# Dynamo Pages with Spring Boot using CommonMark


## Overview

The Dynamo Pages module integrates Spring Boot with CommonMark to handle HTML to Markdown conversions. This setup is crucial for generating and managing Markdown documentation efficiently within a Spring Boot application. The module supports various CRUD operations for documents and collections, and provides services for managing and retrieving these entities.

## Prerequisites

- **Java 17.x**
- **Maven 3.8.x**
- **PostgreSQL 12.x**

## Dependencies

- **commonmark**: Library for parsing and rendering Markdown.
- **commonmark-ext-gfm-tables**: Extension for GitHub-flavored Markdown tables.
- **commonmark-ext-image-attributes**: Extension for handling image attributes in Markdown.
- **spring-boot-starter-validation**: Provides validation support.
- **dynamo-utils**: Utility functions for Dynamo.
- **hibernate-validator**: Implementation for Bean Validation.
- **jakarta.validation-api**: API for Bean Validation.
- **jakarta.persistence-api**: API for JPA.
- **flyway-core**: Database migration tool.
- **springdoc-openapi-starter-webmvc-ui**: For API documentation and Swagger UI.
- **PostgreSQL**: Database for storing documentation data.
- **spring-boot-starter-data-jpa**: Provides JPA support.
- **dynamo-generics-crud**: For generic CRUD operations.

## Plugins

- **apt-maven-plugin**: For annotation processing.

## Configuration Properties

- **`spring.datasource.url`**: `jdbc:postgresql://${DB_URL:localhost}:${DB_PORT:5432}/${DB_NAME:dynamo_db}` - URL for PostgreSQL database.
- **`spring.datasource.username`**: `${DB_USERNAME:dynamo_usr}` - Database username.
- **`spring.datasource.password`**: `${DB_PASSWORD:dynamo123}` - Database password.

## Vocabulary/Terminology

- **CRUD (Create, Read, Update, Delete)**: Standard operations for managing entities.
- **DTO (Data Transfer Object)**: Objects used to transfer data between different layers or services.
- **BFF (Backend for Frontend)**: A specialized backend service tailored for specific frontend needs.

## Technologies/3rd Party Libraries

- **Spring Boot**: Framework for building Java applications.
- **CommonMark**: Library for Markdown parsing and rendering.
- **PostgreSQL**: Relational database management system.
- **Spring Data JPA**: For data access and persistence.

## Sample Use Cases

- **Document Management**: Create, retrieve, update, and delete documents.
- **Collection Management**: Manage collections of documents, including CRUD operations.
- **Revision Management**: Handle revisions of documents, including retrieval and updates.
- **Markdown Conversion**: Convert HTML to Markdown for documentation purposes.

## Modules

### dynamo-pages-svc

- **AttachmentService**
  - **`create(T entity)`**: Creates a new attachment record.
  - **`saveAll(List<T> entities)`**: Saves a list of attachment records.
  - **`deleteAll(List<T> entities)`**: Deletes a list of attachment records.
  - **`retrieveById(Long id)`**: Retrieves an attachment record by its unique identifier, returning an `Optional` that may be empty if not found.
  - **`update(T updatedItem)`**: Updates an existing attachment record with the provided data.

- **CollectionService**
  - **`create(T entity)`**: Creates a new collection record.
  - **`saveAll(List<T> entities)`**: Saves a list of collection records.
  - **`deleteAll(List<T> entities)`**: Deletes a list of collection records.
  - **`retrieveById(Long id)`**: Retrieves a collection record by its unique identifier, returning an `Optional` that may be empty if not found.
  - **`update(T updatedItem)`**: Updates an existing collection record with the provided data.
  - **`retrieveByUniqueId(UUID uniqueId)`**: Retrieves a collection by its unique identifier.

- **DocumentService**
  - **`create(T entity)`**: Creates a new document record.
  - **`saveAll(List<T> entities)`**: Saves a list of document records.
  - **`deleteAll(List<T> entities)`**: Deletes a list of document records.
  - **`retrieveById(Long id)`**: Retrieves a document record by its unique identifier, returning an `Optional` that may be empty if not found.
  - **`update(T updatedItem)`**: Updates an existing document record with the provided data.
  - **`retrieveByUniqueIdAndStatus(UUID uniqueId, DocumentStatus documentStatus)`**: Retrieves a document by its unique identifier and status.
  - **`retrieveByUniqueId(UUID uniqueId)`**: Retrieves a document by its unique identifier.
  - **`retrieveByCollection(Collection collection)`**: Retrieves documents associated with a given collection.

- **RevisionService**
  - **`create(T entity)`**: Creates a new revision record.
  - **`saveAll(List<T> entities)`**: Saves a list of revision records.
  - **`deleteAll(List<T> entities)`**: Deletes a list of revision records.
  - **`retrieveById(Long id)`**: Retrieves a revision record by its unique identifier, returning an `Optional` that may be empty if not found.
  - **`update(T updatedItem)`**: Updates an existing revision record with the provided data.
  - **`retrieveByDocument(Document updatedDocument)`**: Retrieves revisions associated with a specific document.
  - **`retrieveByDocumentAndStatus(Document updatedDocument, DocumentStatus documentStatus)`**: Retrieves revisions for a document based on its status.

### dynamo-pages-web-bff

- **MarkdownCollectionService**
  - **`createCollection(CollectionDto collectionDto)`**: Creates a new collection based on the provided DTO.
  - **`updateCollection(CollectionDto collectionDto)`**: Updates an existing collection with new data.
  - **`deleteCollection(UUID collectionId)`**: Deletes a collection by its ID.
  - **`retrieveCollections(Pageable pageable, MultiValueMap<String, String> searchParameters)`**: Retrieves a paginated list of collections based on search parameters.
  - **`uploadDocument(UUID collectionId, UUID parentDocumentId, MultipartFile file)`**: Uploads a document to a specified collection.

- **MarkdownDocumentService**
  - **`createDocument(DocumentDto documentDto)`**: Creates a new document based on the provided DTO.
  - **`publishDocument(DocumentDto documentDto)`**: Publishes a document for public access.
  - **`updateDocumentWithForkCheck(UUID documentId)`**: Updates a document with a check for existing forks.
  - **`updateDocument(DocumentDto updateDocumentDto)`**: Updates an existing document with new data.
  - **`retrieveDocuments(Pageable pageable, List<String> collectionIds, MultiValueMap<String, String> searchParameters)`**: Retrieves documents based on collection IDs and search parameters with pagination.
  - **`archiveDocument(UUID documentId)`**: Archives a document, moving it out of the active view.
  - **`deleteDocument(UUID documentId, boolean isPermanent)`**: Deletes a document, optionally permanently.
  - **`downloadDocument(UUID documentId, HttpServletResponse httpServletResponse)`**: Handles the download of a document.
  - **`retrieveDocumentRevisions(UUID documentId, Sort sort, MultiValueMap<String, String> searchOrFilterParameters)`**: Retrieves revisions for a document with sorting and filtering options.

## Summary

The Dynamo Pages module with Spring Boot and CommonMark provides a robust solution for managing documentation data. This module supports comprehensive CRUD operations for attachments, collections, documents, and revisions. It also includes services for managing collections and documents through a web backend, and integrates Markdown conversion to ensure effective documentation formatting. This setup is designed to handle both the creation and management of documentation data and its representation in Markdown format efficiently.


