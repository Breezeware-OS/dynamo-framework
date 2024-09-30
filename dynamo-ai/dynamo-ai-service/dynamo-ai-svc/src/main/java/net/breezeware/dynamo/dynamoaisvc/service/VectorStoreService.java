package net.breezeware.dynamo.dynamoaisvc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.breezeware.dynamo.dynamoaisvc.dao.VectorStoreRepository;
import net.breezeware.dynamo.dynamoaisvc.entity.Model;
import net.breezeware.dynamo.dynamoaisvc.entity.KnowledgeArtifact;
import net.breezeware.dynamo.dynamoaisvc.entity.VectorStore;
import net.breezeware.dynamo.generics.crud.service.GenericService;
import net.breezeware.dynamo.utils.exception.DynamoException;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VectorStoreService extends GenericService<VectorStore> {

    private final OpenAiEmbeddingModel openAiEmbeddingClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VectorStoreRepository vectorStoreRepository;
    private final OpenAiChatModel openAiChatModel;
    private static final String STANDARD_SYSTEM_MESSAGE = """
            You are here to provide accurate answers based on the information from the DOCUMENTS section.
            If you are unsure about something or no document is available, reply politely that you don't
            know the answer and mention who you are. DOCUMENTS: {documents}
            """;
    @Value("${spring.ai.openai.chat.options.model}")
    private String openAiModel;
    @Value("${token-text-splitter.default-chunk-size}")
    private int defaultChunkSize;
    @Value("${token-text-splitter.min-chunk-size-chars}")
    private int minChunkSizeChars;
    @Value("${token-text-splitter.min-chunk-length-to-embed}")
    private int minChunkLengthToEmbed;
    @Value("${token-text-splitter.max-num-chunks}")
    private int maxNumChunks;

    public VectorStoreService(OpenAiEmbeddingModel openAiEmbeddingClient, VectorStoreRepository vectorStoreRepository,
            OpenAiChatModel openAiChatModel) {
        super(vectorStoreRepository);
        this.openAiEmbeddingClient = openAiEmbeddingClient;
        this.vectorStoreRepository = vectorStoreRepository;
        this.openAiChatModel = openAiChatModel;
    }

    /**
     * Adds a general resource to the knowledge artifact and AI model.
     * @param resourceUrl       URL of the resource.
     * @param knowledgeArtifact The knowledge artifact to which the document
     *                          belongs.
     * @param model             The AI model to use for embedding.
     */
    public void addResource(String resourceUrl, KnowledgeArtifact knowledgeArtifact, Model model) {
        log.info("Entering addResource()");
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resourceUrl);
        TokenTextSplitter textSplitter =
                new TokenTextSplitter(defaultChunkSize, minChunkSizeChars, minChunkLengthToEmbed, maxNumChunks, true);
        List<Document> textSplitterDocuments = textSplitter.apply(tikaDocumentReader.get());
        embedDocuments(textSplitterDocuments, knowledgeArtifact, model);
        log.info("Leaving addResource()");
    }

    /**
     * Embeds a list of documents using the specified AI model and stores them in
     * the vector store.
     * @param documents         List of documents to embed
     * @param knowledgeArtifact The knowledge artifact associated with the documents
     * @param model             The AI model to use for embedding
     */
    public void embedDocuments(List<Document> documents, KnowledgeArtifact knowledgeArtifact, Model model) {
        log.info("Entering embedDocuments()");

        documents.forEach(document -> {
            try {
                var documentUniqueId = UUID.fromString(document.getId());
                var content = document.getContent();
                var metadata = toJson(document.getMetadata());

                // Generate embeddings for the document using the AI model
                var embedding = openAiEmbeddingClient.embed(document);


                // Create a new VectorStore object with the document details, embeddings, AI
                // model, and knowledge artifact
                VectorStore vectorStore =
                        VectorStore.builder().uniqueId(documentUniqueId).content(content).metadata(metadata)
                                .embedding(embedding).model(model).knowledgeArtifact(knowledgeArtifact).build();

                // Store the VectorStore object in the repository
                create(vectorStore);
            } catch (ResourceAccessException e) {
                throw new DynamoException("Something went wrong.Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        });

        log.info("Leaving embedDocuments()");

    }

    /**
     * Converts a map to a JSON string.
     * @param  map Map to convert
     * @return     JSON string representation of the map
     */
    private String toJson(Map<String, Object> map) {
        log.info("Entering toJson()");
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new DynamoException("Failed to convert map to JSON", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    // /**
    // * Converts a list of Double values to a float array.
    // * @param embeddingDouble List of Double values
    // * @return Float array representation of the list
    // */
    // private float[] toFloatArray(List<Double> embeddingDouble) {
    // log.info("Entering toFloatArray()");
    // float[] embeddingFloat = new float[embeddingDouble.size()];
    // for (int i = 0; i < embeddingDouble.size(); i++) {
    // embeddingFloat[i] = embeddingDouble.get(i).floatValue();
    // }
    //
    // log.info("Leaving toFloatArray()");
    // return embeddingFloat;
    // }

    /**
     * Deletes documents by AI model and knowledge artifact unique IDs.
     * @param modelUniqueId             Unique ID of the AI model
     * @param knowledgeArtifactUniqueId Unique ID of the knowledge artifact
     */
    public void deleteDocumentsByModelAndArtifact(UUID modelUniqueId, UUID knowledgeArtifactUniqueId) {
        log.info("Entering deleteDocumentsByModelAndArtifact()");
        vectorStoreRepository.deleteByModelUniqueIdAndKnowledgeArtifactUniqueId(modelUniqueId,
                knowledgeArtifactUniqueId);
        log.info("Leaving deleteDocumentsByModelAndArtifact()");
    }

    /**
     * Creates a chat response based on the given input.
     * @param  modelId         The UUID of the model.
     * @param  message         The user message.
     * @param  systemPrompt    The system prompt.
     * @param  temperature     The temperature for sampling.
     * @param  topP            The top P value for nucleus sampling.
     * @return                 The chat response.
     * @throws DynamoException If the message is invalid.
     */
    public ChatResponse createChatResponse(UUID modelId, String message, String systemPrompt, float temperature,
            float topP) {
        log.debug("Entering createChatResponse()");

        if (message == null || message.isEmpty()) {
            throw new DynamoException("Invalid message", HttpStatus.BAD_REQUEST);
        }

        // Retrieve relevant documents based on the message and modelId
        List<VectorStore> documents = findRelevantDocumentsByQueryEmbedding(message, modelId);

        // Concatenate the content of all documents with a line separator
        String concatenatedDocuments =
                documents.stream().map(VectorStore::getContent).collect(Collectors.joining(System.lineSeparator()));

        // Create the final system message by combining the system prompt and a standard
        // system message
        String finalSystemMessage = systemPrompt + " " + STANDARD_SYSTEM_MESSAGE;
        // Create a system message object with the final system message
        Message systemMessage =
                new SystemPromptTemplate(finalSystemMessage).createMessage(Map.of("documents", concatenatedDocuments));

        // Create a user message object with the original user message
        UserMessage userMessage = new UserMessage(message);

        // Create options for the OpenAI chat, including the model, temperature, and
        // topP
        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder().withModel(openAiModel)
                .withTemperature((double) temperature).withTopP((double) topP).build();

        // Create a prompt with the system message, user message, and chat options
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), openAiChatOptions);

        // Call the chat client with the prompt and store the response
        ChatResponse chatResponse = openAiChatModel.call(prompt);

        log.debug("Leaving createChatResponse()");

        return chatResponse;
    }

    /**
     * Retrieves relevant documents based on the query embedding.
     * @param  message The user message.
     * @param  modelId The UUID of the model.
     * @return         The list of relevant documents.
     */
    public List<VectorStore> findRelevantDocumentsByQueryEmbedding(String message, UUID modelId) {
        log.info("Entering findRelevantDocumentsByQueryEmbedding");

        try {
            SearchRequest request = SearchRequest.query(message);
            // Get the embedding for the search query.
            float[] queryEmbedding = openAiEmbeddingClient.embed(request.getQuery());
            // A double value ranging from 0 to 1, where values closer to 1 indicate higher
            // similarity. Default threshold value is 0.0.
            double distance = 1 - request.getSimilarityThreshold();
            // An integer that specifies the maximum number of similar documents to
            // return. Default top k value is 1.
            int topK = 1;

            List<VectorStore> relevantDocuments =
                    vectorStoreRepository.findRelevantDocuments(queryEmbedding, modelId, distance, topK);
            log.info("Leaving findRelevantDocumentsByQueryEmbedding");
            return relevantDocuments;
        } catch (ResourceAccessException e) {
            throw new DynamoException("Something went wrong.Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
