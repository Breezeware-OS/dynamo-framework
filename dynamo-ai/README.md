# Dynamo AI Module Documentation


---

## Overview

The **Dynamo AI Module** is designed to manage AI models, knowledge artifacts, and document embeddings. It integrates with OpenAI services to provide functionalities such as embedding documents, creating chat responses, and managing knowledge artifacts associated with AI models. The module is divided into several components, each serving a specific purpose.

## Prerequisites

- **Java 17.x**
- **Maven 3.8.x**
- **PostgreSQL 12.x**

## Relevant Dependencies, Plugins, and Properties

### Dependencies

- **`dynamo-generics-crud`**: Generic CRUD operations.
- **`springdoc-openapi-starter-webmvc-ui`**: OpenAPI documentation and Swagger UI.
- **`spring-ai-openai`**: OpenAI services integration.
- **`spring-ai-pgvector-store`**: Vector storage in PostgreSQL.
- **`spring-ai-tika-document-reader`**: Document processing with Apache Tika.
- **`dynamo-utils`**: Utility functions for Dynamo.
- **`hibernate-vector`**: Vector-based data support in Hibernate.
- **`Flyway`**: For DB Migration.

### Dependency Management

- **`spring-ai-bom`**: BOM for managing Spring AI dependencies.

### Repositories

- **`Spring Milestones`**: Access to Spring milestones and snapshots.

### Plugins

- **`apt-maven-plugin`**: Annotation processing.

### Properties

- **`spring-ai.version`**: Version for Spring AI BOM.

---

## Module: dynamo-ai-bff

### Module Introduction

The `dynamo-ai-bff` module is the backend-for-frontend service that manages AI models and their associated knowledge artifacts. It provides a range of RESTful APIs for creating, retrieving, updating, and managing AI models, as well as handling knowledge artifacts and conducting model conversations.

### Vocabulary/Terminology

- **BFF (Backend for Frontend)**: A specialized backend service tailored to meet the needs of a specific frontend.
- **DTO (Data Transfer Object)**: Objects used to transfer data between different parts of the application.

### Technologies/3rd Party Libraries

- **Spring Web**: For building web applications.
- **OpenAPI (springdoc-openapi)**: For API documentation.
- **Spring AI**: For integrating AI capabilities into the application.

### Sample Use Cases

- **Model Management**: Create, retrieve, update, and manage AI models.
- **Knowledge Artifacts**: Upload, retrieve, delete, and embed knowledge artifacts associated with AI models.
- **Model Conversations**: Test and conduct conversations with AI models.
- **Model Training**: Complete the training process for AI models.
- **Document Management**: Check the availability and embedding status of documents related to AI models.

---

## Module: dynamo-ai-service

### Module Introduction

The `dynamo-ai-service` module encompasses services related to AI models and knowledge artifacts. It provides functionality for managing knowledge artifacts, handling AI model operations, and interacting with vector stores for document embedding and chat responses.

### Module: `KnowledgeArtifactService`

#### Module Introduction

The `KnowledgeArtifactService` module handles operations related to knowledge artifacts, including their retrieval and deletion based on unique IDs and model IDs.

#### Methods

- **`retrieveKnowledgeArtifactByUniqueId(UUID uniqueId)`**: Retrieves a knowledge artifact by its unique ID.
- **`deleteKnowledgeArtifactByUniqueIdAndModelUniqueId(UUID uniqueId, UUID modelUniqueId)`**: Deletes a knowledge artifact by its unique ID and model unique ID.
- **`retrieveKnowledgeArtifactByUniqueIdAndModelUniqueId(UUID uniqueId, UUID modelUniqueId)`**: Retrieves a knowledge artifact by its unique ID and the model's unique ID.
- **`findByModelUniqueIdAndStatus(UUID modelUniqueId, String status)`**: Finds knowledge artifacts by the model's unique ID and status.
- **`findByModelUniqueId(UUID modelUniqueId)`**: Retrieves all knowledge artifacts associated with a specific AI model.

### Module: `ModelService`

#### Module Introduction

The `ModelService` module handles operations related to AI models, including retrieval by unique ID.

#### Methods

- **`retrieveModelByUniqueId(UUID uniqueId)`**: Retrieves an AI model by its unique ID.

### Module: `VectorStoreService`

#### Module Introduction

The `VectorStoreService` module manages document embedding and chat responses using AI models. It integrates with OpenAI services for embedding and chat functionalities.

#### Methods

- **`addResource(String resourceUrl, KnowledgeArtifact knowledgeArtifact, Model model)`**: Adds a general resource to the knowledge artifact and AI model.
- **`embedDocuments(List<Document> documents, KnowledgeArtifact knowledgeArtifact, Model model)`**: Embeds documents using the specified AI model and stores them in the vector store.
- **`toJson(Map<String, Object> map)`**: Converts a map to a JSON string.
- **`toFloatArray(List<Double> embeddingDouble)`**: Converts a list of Double values to a float array.
- **`deleteDocumentsByModelAndArtifact(UUID modelUniqueId, UUID knowledgeArtifactUniqueId)`**: Deletes documents by AI model and knowledge artifact unique IDs.
- **`createChatResponse(UUID modelId, String message, String systemPrompt, float temperature, float topP)`**: Creates a chat response based on the input.
- **`findRelevantDocumentsByQueryEmbedding(String message, UUID modelId)`**: Retrieves relevant documents based on the query embedding.

#### Configuration Properties

- **`spring.ai.openai.api-key`**: The API key for OpenAI services.
- **`spring.ai.openai.chat.options.model`**: The AI model to use for chat responses.
- **`spring.ai.openai.embedding.options.model`**: The AI model to use for embedding.
- **`token-text-splitter.default-chunk-size`**: Default chunk size for text splitting.
- **`token-text-splitter.min-chunk-size-chars`**: Minimum chunk size in characters for text splitting.
- **`token-text-splitter.min-chunk-length-to-embed`**: Minimum chunk length to embed.
- **`token-text-splitter.max-num-chunks`**: Maximum number of chunks for text splitting.

---

## Summary

The **Dynamo AI Module** provides a comprehensive framework for managing AI models and knowledge artifacts. By leveraging OpenAI services, it enables sophisticated document embeddings, chat responses, and knowledge management. The module is organized into several key components: `dynamo-ai-bff`, which handles the frontend interactions and API exposure, and `dynamo-ai-service`, which manages backend operations related to AI models, knowledge artifacts, and document embeddings. Key services include `KnowledgeArtifactService`, `ModelService`, and `VectorStoreService`, each offering specific functionalities to facilitate efficient management and utilization of AI resources.

