package net.breezeware.dynamo.dynamodocswebbff.service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import com.querydsl.core.BooleanBuilder;

import net.breezeware.dynamo.dynamodocssvc.entity.Collection;
import net.breezeware.dynamo.dynamodocssvc.entity.Document;
import net.breezeware.dynamo.dynamodocssvc.entity.QDocument;
import net.breezeware.dynamo.dynamodocssvc.entity.QRevision;
import net.breezeware.dynamo.dynamodocssvc.entity.Revision;
import net.breezeware.dynamo.dynamodocssvc.enumeration.DocumentPermission;
import net.breezeware.dynamo.dynamodocssvc.enumeration.DocumentStatus;
import net.breezeware.dynamo.dynamodocssvc.service.CollectionService;
import net.breezeware.dynamo.dynamodocssvc.service.DocumentService;
import net.breezeware.dynamo.dynamodocssvc.service.RevisionService;
import net.breezeware.dynamo.dynamodocswebbff.dto.DocumentDto;
import net.breezeware.dynamo.dynamodocswebbff.dto.DocumentViewDto;
import net.breezeware.dynamo.dynamodocswebbff.dto.RevisionDto;
import net.breezeware.dynamo.dynamodocswebbff.dto.UpdateDocumentResponseDto;
import net.breezeware.dynamo.dynamodocswebbff.mapper.DocumentMapper;
import net.breezeware.dynamo.dynamodocswebbff.mapper.RevisionMapper;
import net.breezeware.dynamo.dynamodocswebbff.utils.CustomHtmlRender;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagement.service.UserService;
import net.breezeware.dynamo.utils.exception.DynamoException;
import net.breezeware.dynamo.utils.exception.ValidationExceptionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarkdownDocumentService {

    private static final Set<String> DEFAULT_VALID_PARAMETERS = Set.of("page-no", "page-size", "sort", "search");

    private final Validator fieldValidator;

    private final UserService userService;

    private final CollectionService collectionService;

    private final DocumentService documentService;

    private final RevisionService revisionService;

    private final DocumentMapper documentMapper;

    private final RevisionMapper revisionMapper;
    private final CustomHtmlRender customHtmlRender;

    /**
     * Creates a new document based on the provided document DTO.
     * @param  documentDto The document DTO containing information about the
     *                     document.
     * @return             The DTO representing the newly created document.
     */
    @Transactional
    public DocumentDto createDocument(DocumentDto documentDto) {
        log.info("Entering createDocument()");

        // Field constraint violation validation
        Set<ConstraintViolation<DocumentDto>> fieldViolations = fieldValidator.validate(documentDto);

        // Field constraint violation handling
        ValidationExceptionUtils.handleException(fieldViolations);

        // Validate and Retrieve Collection
        Collection collection = collectionService.retrieveByUniqueId(documentDto.getCollectionId())
                .orElseThrow(() -> new DynamoException(
                        String.format("Collection with unique-id '%s' not found", documentDto.getCollectionId()),
                        HttpStatus.NOT_FOUND));

        // Retrieve User
        User user = validateAndRetrieveAuthenticatedUser();

        // Validate and Retrieve Parent Document if parent id present
        Document parentDocument = null;
        if (Objects.nonNull(documentDto.getParentId())) {
            parentDocument = documentService.retrieveByUniqueId(documentDto.getParentId())
                    .orElseThrow(() -> new DynamoException(
                            String.format("Parent Document with unique-id '%s' not found", documentDto.getParentId()),
                            HttpStatus.NOT_FOUND));
        }

        // Map DocumentDto to Document
        Document document = documentMapper.documentDtoToDocument(documentDto);
        document.setCreatedByUser(user);

        // Build and Save Document and Revision
        Document savedDocument = buildAndPersistsDocument(document, collection, parentDocument);
        buildAndPersistsRevision(savedDocument, DocumentStatus.DRAFTED);

        // Map Document to DocumentDto;
        DocumentDto savedDocumentDto = documentMapper.documentToDocumentDto(savedDocument);

        log.info("Leaving createDocument()");

        return savedDocumentDto;
    }

    /**
     * Validates and retrieves the authenticated user.
     * @return The authenticated user.
     */
    public User validateAndRetrieveAuthenticatedUser() {
        log.info("Entering validateAndRetrieveAuthenticatedUser()");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new DynamoException("User not authenticated.", HttpStatus.UNAUTHORIZED);
        }

        String userId = authentication.getPrincipal().toString();
        log.info("userId = {}", userId);
        if ("anonymousUser".equalsIgnoreCase(userId)) {
            throw new DynamoException("User not authenticated.", HttpStatus.UNAUTHORIZED);
        }

        User user = userService.retrieveUserByIdmUserId(userId)
                .orElseThrow(() -> new DynamoException("User not found in the system.", HttpStatus.NOT_FOUND));

        log.info("Leaving validateAndRetrieveAuthenticatedUser()");
        return user;
    }

    /**
     * Builds and persists a revision for the given document with the specified
     * status.
     * @param document       The document for which the revision is created.
     * @param documentStatus The status of the revision.
     */
    private void buildAndPersistsRevision(Document document, DocumentStatus documentStatus) {
        log.info("Entering buildAndPersistsRevision()");
        Revision revision = new Revision();
        revision.setDocument(document);
        revision.setTitle(document.getTitle());
        revision.setContent(document.getContent());
        revision.setStatus(documentStatus.getValue());
        revision.setVersion(document.getVersion());
        revision.setUniqueId(UUID.randomUUID());
        revision.setEditedByUser(document.getCreatedByUser());
        revision.setEditedOn(Instant.now());
        log.info("Leaving buildAndPersistsRevision()");
        revisionService.create(revision);
    }

    /**
     * Builds and persists a document with the provided collection and parent
     * document.
     * @param  document       The document to be built and persisted.
     * @param  collection     The collection to which the document belongs.
     * @param  parentDocument The parent document of the document, if any.
     * @return                The persisted document.
     */
    private Document buildAndPersistsDocument(Document document, Collection collection, Document parentDocument) {
        log.info("Entering buildAndPersistsDocument()");
        document.setUniqueId(UUID.randomUUID());
        document.setCollection(collection);
        document.setParent(parentDocument);
        document.setStatus(DocumentStatus.DRAFTED.getValue());
        document.setVersion(1);
        Document savedDocument = documentService.create(document);
        log.info("Leaving buildAndPersistsDocument()");
        return savedDocument;
    }

    /**
     * Publishes the specified document.
     * @param documentDto The document DTO to be published.
     */
    @Transactional
    public void publishDocument(DocumentDto documentDto) {
        log.info("Entering publishDocument()");

        // Field constraint violation validation
        Set<ConstraintViolation<DocumentDto>> fieldViolations = fieldValidator.validate(documentDto);

        // Field constraint violation handling
        ValidationExceptionUtils.handleException(fieldViolations);

        // Validate and Retrieve Document
        if (documentDto.getId() <= 0) {
            throw new DynamoException("Invalid Document ID. Please provide a valid ID.", HttpStatus.BAD_REQUEST);
        }

        if (Objects.isNull(documentDto.getUniqueId())) {
            throw new DynamoException("Missing Document Unique ID. Please provide a valid unique ID.",
                    HttpStatus.BAD_REQUEST);
        }

        if (Objects.isNull(documentDto.getTitle()) || documentDto.getTitle().isEmpty()) {
            throw new DynamoException("Missing Document Title. Please provide a title for the document.",
                    HttpStatus.BAD_REQUEST);
        }

        Document document = documentService.retrieveByUniqueId(documentDto.getUniqueId())
                .orElseThrow(() -> new DynamoException(
                        String.format("Document with unique-id '%s' not found", documentDto.getUniqueId()),
                        HttpStatus.NOT_FOUND));

        // Retrieve User
        User user = validateAndRetrieveAuthenticatedUser();

        // Retrieve Revision
        Optional<Revision> optionalRevision =
                revisionService.retrieveByDocumentAndStatus(document, DocumentStatus.FORKED);
        if (optionalRevision.isPresent()) {
            if (user.equals(optionalRevision.get().getEditedByUser())) {
                // Build and Save Document and Revision
                Revision revision = optionalRevision.get();
                Revision savedRevision = buildAndPersistsRevision(documentDto, revision, DocumentStatus.PUBLISHED);
                buildAndPersistsDocument(document, savedRevision);
            } else {
                throw new DynamoException("You do not have permission to publish this document", HttpStatus.FORBIDDEN);
            }

        } else {
            // Build and Save Document and Revision
            Document updatedDocument = buildAndPersistsDocument(documentDto, document);
            Revision revision =
                    revisionService.retrieveByDocumentAndStatus(updatedDocument, DocumentStatus.DRAFTED)
                            .orElseThrow(
                                    () -> new DynamoException(
                                            "Revision with Document '%s' and status '%s' not found".formatted(
                                                    updatedDocument, DocumentStatus.DRAFTED.getValue()),
                                            HttpStatus.NOT_FOUND));
            buildAndPersistsRevision(revision, updatedDocument);
        }

        log.info("Leaving publishDocument()");
    }

    /**
     * Builds and persists the revision for the updated document.
     * @param revision        The revision to be updated.
     * @param updatedDocument The updated document.
     */
    private void buildAndPersistsRevision(Revision revision, Document updatedDocument) {
        log.info("Entering buildAndPersistsRevision()");
        revision.setDocument(updatedDocument);
        revision.setTitle(updatedDocument.getTitle());
        revision.setContent(updatedDocument.getContent());
        revision.setStatus(updatedDocument.getStatus());
        revision.setEditedOn(Instant.now());
        log.info("Leaving buildAndPersistsRevision()");
        revisionService.update(revision);
    }

    /**
     * Builds and persists the document based on the provided DocumentDto.
     * @param  documentDto The DocumentDto containing the document data.
     * @param  document    The existing document to be updated.
     * @return             The updated document after persistence.
     */
    private Document buildAndPersistsDocument(DocumentDto documentDto, Document document) {
        log.info("Entering buildAndPersistsDocument()");
        document.setTitle(documentDto.getTitle());
        document.setContent(documentDto.getContent());
        document.setStatus(DocumentStatus.PUBLISHED.getValue());
        document.setPublishedOn(Instant.now());
        Document updatedDocument = documentService.update(document);
        log.info("Leaving buildAndPersistsDocument()");
        return updatedDocument;
    }

    /**
     * Builds and persists the document based on the provided Revision.
     * @param document The document to be updated.
     * @param revision The saved revision containing updated information.
     */
    private void buildAndPersistsDocument(Document document, Revision revision) {
        log.info("Entering buildAndPersistsDocument()");
        document.setTitle(revision.getTitle());
        document.setContent(revision.getContent());
        document.setStatus(DocumentStatus.PUBLISHED.getValue());
        document.setPublishedOn(Instant.now());
        document.setVersion(revision.getVersion());
        documentService.update(document);
        log.info("Leaving buildAndPersistsDocument()");
    }

    /**
     * Builds and persists the revision based on the provided DocumentDto.
     * @param  documentDto    The document DTO containing updated information.
     * @param  revision       The revision to be updated.
     * @param  documentStatus The status of the document.
     * @return                The saved revision with updated information.
     */
    private Revision buildAndPersistsRevision(DocumentDto documentDto, Revision revision,
            DocumentStatus documentStatus) {
        log.info("Entering buildAndPersistsRevision()");
        revision.setTitle(documentDto.getTitle());
        revision.setContent(documentDto.getContent());
        revision.setEditedOn(Instant.now());
        revision.setStatus(documentStatus.getValue());
        Revision savedRevision = revisionService.update(revision);
        log.info("Leaving buildAndPersistsRevision()");
        return savedRevision;
    }

    /**
     * Updates the document with fork check and returns the response DTO.
     * @param  documentId The unique ID of the document to be updated.
     * @return            The response DTO containing the updated document
     *                    information and fork status.
     */
    @Transactional
    public UpdateDocumentResponseDto updateDocumentWithForkCheck(UUID documentId) {
        log.info("Entering updateDocumentWithForkCheck()");

        // Validate and Retrieve Document
        if (Objects.isNull(documentId)) {
            throw new DynamoException("Missing Document Unique ID. Please provide a valid unique ID.",
                    HttpStatus.BAD_REQUEST);
        }

        Document retrievedDocument = documentService.retrieveByUniqueId(documentId).orElseThrow(
                () -> new DynamoException("Document with unique-id '%s' not found".formatted(documentId),
                        HttpStatus.NOT_FOUND));

        // Retrieve User
        User user = validateAndRetrieveAuthenticatedUser();

        UpdateDocumentResponseDto updateDocumentResponseDto = new UpdateDocumentResponseDto();

        // Retrieve Revision
        Optional<Revision> optionalRevision =
                revisionService.retrieveByDocumentAndStatus(retrievedDocument, DocumentStatus.FORKED);
        if (optionalRevision.isPresent()) {
            if (user.equals(optionalRevision.get().getEditedByUser())) {
                Revision revision = optionalRevision.get();
                DocumentDto documentDto = buildDocumentDto(revision);
                updateDocumentResponseDto.setDocumentDto(documentDto);
                updateDocumentResponseDto.setForked(true);
            } else {
                // Build DocumentDto
                Revision revision = optionalRevision.get();
                DocumentDto documentDto = buildDocumentDto(revision);
                updateDocumentResponseDto.setDocumentDto(documentDto);
                updateDocumentResponseDto.setForked(false);
            }

        } else {
            // Retrieve Document
            Document document =
                    documentService.retrieveByUniqueIdAndStatus(documentId, DocumentStatus.PUBLISHED)
                            .orElseThrow(
                                    () -> new DynamoException(
                                            "Document with unique-id '%s' and status '%s' not found".formatted(
                                                    documentId, DocumentStatus.PUBLISHED.getValue()),
                                            HttpStatus.NOT_FOUND));
            // Build DocumentDto and Save Revision
            DocumentDto documentDto = documentMapper.documentToDocumentDto(document);
            updateDocumentResponseDto.setDocumentDto(documentDto);
            updateDocumentResponseDto.setForked(true);
            document.setVersion(document.getVersion() + 1);
            document.setCreatedByUser(user);
            buildAndPersistsRevision(document, DocumentStatus.FORKED);
        }

        log.info("Leaving updateDocumentWithForkCheck()");

        return updateDocumentResponseDto;
    }

    /**
     * Builds a DocumentDto based on the provided Revision information.
     * @param  revision The Revision from which to extract information.
     * @return          The updated DocumentDto.
     */
    private DocumentDto buildDocumentDto(Revision revision) {
        log.info("Entering buildDocumentDto()");
        DocumentDto documentDto = documentMapper.documentToDocumentDto(revision.getDocument());
        documentDto.setTitle(revision.getTitle());
        documentDto.setContent(revision.getContent());
        documentDto.setStatus(revision.getStatus());
        documentDto.setCreatedUserFirstName(revision.getEditedByUser().getFirstName());
        documentDto.setCreatedUserLastName(revision.getEditedByUser().getLastName());
        log.info("Leaving buildDocumentDto()");
        return documentDto;
    }

    /**
     * Updates a document based on the provided DocumentDto.
     * @param updateDocumentDto The DocumentDto containing the updated information.
     */
    @Transactional
    public void updateDocument(DocumentDto updateDocumentDto) {
        log.info("Entering updateDocument()");

        // Field constraint violation validation
        Set<ConstraintViolation<DocumentDto>> fieldViolations = fieldValidator.validate(updateDocumentDto);

        // Field constraint violation handling
        ValidationExceptionUtils.handleException(fieldViolations);

        // Validate and Retrieve Document
        if (updateDocumentDto.getId() <= 0) {
            throw new DynamoException("Invalid Document ID. Please provide a valid ID.", HttpStatus.BAD_REQUEST);
        }

        if (Objects.isNull(updateDocumentDto.getUniqueId())) {
            throw new DynamoException("Missing Document Unique ID. Please provide a valid unique ID.",
                    HttpStatus.BAD_REQUEST);
        }

        Document document = documentService.retrieveByUniqueId(updateDocumentDto.getUniqueId())
                .orElseThrow(() -> new DynamoException(
                        String.format("Document with unique-id '%s' not found", updateDocumentDto.getUniqueId()),
                        HttpStatus.NOT_FOUND));

        // Retrieve User
        User user = validateAndRetrieveAuthenticatedUser();

        // Retrieve Revision
        Optional<Revision> optionalRevision =
                revisionService.retrieveByDocumentAndStatus(document, DocumentStatus.FORKED);
        if (optionalRevision.isPresent()) {
            if (user.equals(optionalRevision.get().getEditedByUser())) {
                // Build and Save Revision
                Revision revision = optionalRevision.get();
                buildAndPersistsRevision(updateDocumentDto, revision, DocumentStatus.FORKED);
            } else {
                throw new DynamoException("You do not have permission to update this document", HttpStatus.FORBIDDEN);
            }

        } else {
            if (user.equals(document.getCreatedByUser())) {
                // Build and Save Document
                document.setTitle(updateDocumentDto.getTitle());
                document.setContent(updateDocumentDto.getContent());
                documentService.update(document);

                // Build and Save Revision
                Revision revision =
                        revisionService.retrieveByDocumentAndStatus(document, DocumentStatus.DRAFTED)
                                .orElseThrow(
                                        () -> new DynamoException(
                                                "Revision with Document '%s' and status '%s' not found".formatted(
                                                        document, DocumentStatus.DRAFTED.getValue()),
                                                HttpStatus.NOT_FOUND));
                buildAndPersistsRevision(revision, document);
            } else {
                throw new DynamoException("You do not have permission to update this document", HttpStatus.FORBIDDEN);
            }

        }

        log.info("Leaving updateDocument()");

    }

    /**
     * Retrieves documents based on the provided pageable, collection IDs, and
     * search parameters.
     * @param  pageable         the Pageable object specifying pagination
     *                          information.
     * @param  collectionIds    the list of collection IDs to filter documents.
     * @param  searchParameters the MultiValueMap containing additional search
     *                          parameters.
     * @return                  a DocumentViewDto containing the retrieved documents
     *                          or document pages.
     * @throws DynamoException  if there's an error processing the request.
     */
    public DocumentViewDto retrieveDocuments(Pageable pageable, List<String> collectionIds,
            MultiValueMap<String, String> searchParameters) {
        log.info("Entering retrieveDocuments()");

        // Set of valid parameters for validation
        Set<String> validParameters = new HashSet<>(DEFAULT_VALID_PARAMETERS);
        validParameters.add("paged");
        validParameters.add("collection-ids");
        validParameters.add("document-id");
        validParameters.add("status");

        // Validate the provided parameters
        validateParameters(validParameters, searchParameters.keySet());

        // Retrieve User
        User user = validateAndRetrieveAuthenticatedUser();

        // Build a BooleanBuilder predicate based on the provided search parameters
        BooleanBuilder booleanBuilder = buildSearchOrFilterPredicate(collectionIds, user, searchParameters);

        DocumentViewDto documentViewDto = new DocumentViewDto();

        // Get the 'paged' parameter value from search parameters
        String pagedParameter = searchParameters.getOrDefault("paged", List.of(Boolean.TRUE.toString())).get(0);

        // Validate the 'paged' parameter value
        if (!pagedParameter.equalsIgnoreCase("true") && !pagedParameter.equalsIgnoreCase("false")) {
            throw new DynamoException("Invalid value '%s' for parameter 'paged'.".formatted(pagedParameter),
                    HttpStatus.BAD_REQUEST);
        }

        // Parse the 'paged' parameter value to boolean
        boolean paged = Boolean.parseBoolean(pagedParameter);

        if (paged) {
            // Retrieve paged pageOfDocumentDto
            Page<DocumentDto> pageOfDocumentDto;
            try {
                Page<Document> pageOfDocuments =
                        documentService.retrievePageEntitiesWithPredicate(booleanBuilder, pageable);

                // Process documents and map to documentDto
                List<DocumentDto> documentDtos =
                        processDocumentsAndMapToDocumentDto(pageable.getSort(), booleanBuilder, user);

                // Build list of documentDto to page of documentDto
                pageOfDocumentDto = new PageImpl<>(documentDtos, pageable, pageOfDocuments.getTotalElements());

            } catch (PropertyReferenceException e) {
                throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
            }

            documentViewDto.setData(pageOfDocumentDto);
        } else {
            // Process documents and map to documentDto
            List<DocumentDto> documentDtos =
                    processDocumentsAndMapToDocumentDto(pageable.getSort(), booleanBuilder, user);
            documentViewDto.setData(documentDtos);
            documentViewDto.setPaged(false);
        }

        log.info("Leaving retrieveDocuments()");

        return documentViewDto;
    }

    /**
     * Processes a page of documents, applies necessary modifications, and maps them
     * to DocumentDto objects.
     * @param  sort           the sort object specifying the sorting information.
     * @param  booleanBuilder the BooleanBuilder containing the predicate for
     *                        filtering documents.
     * @param  user           the user whose documents are being processed.
     * @return                a list of DocumentDto objects representing the
     *                        processed documents.
     */
    private List<DocumentDto> processDocumentsAndMapToDocumentDto(Sort sort, BooleanBuilder booleanBuilder, User user) {
        log.info("Entering processDocumentsAndMapToDocumentDto()");

        // Retrieve Documents
        List<Document> documents =
                documentService.retrieveSortedEntitiesWithPredicate(booleanBuilder, sort).stream().map(document -> {
                    if (Objects.nonNull(document.getCollection())
                            && !document.getCollection().getCreatedByUser().getUniqueId().equals(user.getUniqueId())) {
                        if (document.getCollection().getPermission().equals(DocumentPermission.NO_ACCESS.getValue())) {
                            return null;
                        }

                        return document;
                    }

                    return document;
                }).filter(Objects::nonNull).toList();

        List<DocumentDto> documentDtos = new ArrayList<>();
        for (Document document : documents) {
            // Retrieve Revision
            Optional<Revision> optionalRevision =
                    revisionService.retrieveByDocumentAndStatus(document, DocumentStatus.FORKED);
            if (optionalRevision.isPresent() && user.equals(optionalRevision.get().getEditedByUser())) {
                Revision revision = optionalRevision.get();
                document.setTitle(revision.getTitle());
                document.setContent(revision.getContent());
            }

            // Create Extensions
            Iterable<Extension> extensions = Arrays.asList(TablesExtension.create(), ImageAttributesExtension.create());

            // Pares Content
            Parser parser = Parser.builder().extensions(extensions).build();
            Node node = parser.parse(document.getContent() != null ? document.getContent() : "");

            // Modify the AST to add <button> tag before each code block
            node.accept(customHtmlRender);

            // Build Html Render
            HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
            String htmlContent = renderer.render(node);

            // Map Document to DocumentDto
            DocumentDto documentDto = documentMapper.documentToDocumentDto(document);
            documentDto.setHtmlContent(htmlContent);
            documentDto.getChildren().removeIf(
                    documentDto1 -> !documentDto1.getStatus().equalsIgnoreCase(DocumentStatus.PUBLISHED.getValue()));

            documentDtos.add(documentDto);

        }

        log.info("Leaving processDocumentsAndMapToDocumentDto()");

        return documentDtos;
    }

    /**
     * Validates that the actual parameters are among the allowed parameters.
     * @param  allowedParameters A Set of allowed parameter names.
     * @param  actualParameters  A Set of actual parameter names.
     * @throws DynamoException   if unknown parameters are found.
     */
    private void validateParameters(Set<String> allowedParameters, Set<String> actualParameters)
            throws DynamoException {
        log.info("Entering validateParameters()");

        List<String> invalidParameters =
                actualParameters.stream().filter(param -> !allowedParameters.contains(param)).toList();
        if (!invalidParameters.isEmpty()) {
            log.error("Parameter(s) {} not supported!", invalidParameters);
            throw new DynamoException("Unknown parameter(s) %s found".formatted(invalidParameters),
                    HttpStatus.BAD_REQUEST);
        }

        log.info("Leaving validateParameters()");
    }

    /**
     * Builds a BooleanBuilder predicate based on the provided search and filter
     * parameters.
     * @param  collectionIds            List of collection IDs to filter documents.
     * @param  user                     The authenticated user.
     * @param  searchOrFilterParameters Multi-value map of search parameters.
     * @return                          BooleanBuilder predicate for filtering
     *                                  documents.
     * @throws DynamoException          if there is an error in building the
     *                                  predicate.
     */
    private BooleanBuilder buildSearchOrFilterPredicate(List<String> collectionIds, User user,
            MultiValueMap<String, String> searchOrFilterParameters) throws DynamoException {
        log.info("Entering buildSearchPredicate()");

        QDocument document = QDocument.document;

        // filter by Status value
        String filterByStatusValue = searchOrFilterParameters.getFirst("status");
        BooleanBuilder statusBooleanBuilder = new BooleanBuilder();
        if (Objects.nonNull(filterByStatusValue) && !filterByStatusValue.isBlank()) {
            log.info("Adding filter by Status predicate for value '{}'", filterByStatusValue);

            Optional<DocumentStatus> optionalDocumentStatus =
                    DocumentStatus.retrieveDocumentStatus(filterByStatusValue);
            if (optionalDocumentStatus.isPresent()) {
                DocumentStatus documentStatus = optionalDocumentStatus.get();
                switch (documentStatus) {
                    case PUBLISHED:
                        statusBooleanBuilder.and(document.status.equalsIgnoreCase(DocumentStatus.PUBLISHED.getValue()));
                        break;
                    case DRAFTED:
                        statusBooleanBuilder.and(document.status.equalsIgnoreCase(DocumentStatus.DRAFTED.getValue()))
                                .and(document.createdByUser.eq(user));
                        break;
                    case ARCHIVED:
                        statusBooleanBuilder.and(document.status.equalsIgnoreCase(DocumentStatus.ARCHIVED.getValue()));
                        break;
                    case DELETED:
                        statusBooleanBuilder.and(document.status.equalsIgnoreCase(DocumentStatus.DELETED.getValue()));
                        break;
                    default:
                        break;
                }

            }

        }

        // filter by Search value
        String filterBySearchValue = searchOrFilterParameters.getFirst("search");
        BooleanBuilder searchBooleanBuilder = new BooleanBuilder();
        if (Objects.nonNull(filterBySearchValue) && !filterBySearchValue.isBlank()) {
            log.info("Adding filter by Title and Content predicate for value '{}'", filterBySearchValue);
            searchBooleanBuilder.or(document.title.containsIgnoreCase(filterBySearchValue))
                    .or(document.content.containsIgnoreCase(filterBySearchValue));
        }

        // filter by Document Id
        String filterByDocument = searchOrFilterParameters.getFirst("document-id");
        BooleanBuilder documentBooleanBuilder = new BooleanBuilder();
        if (Objects.nonNull(filterByDocument) && !filterByDocument.isBlank()) {
            log.info("Adding filter by Document unique id predicate for value '{}'", filterBySearchValue);
            documentBooleanBuilder.and(document.uniqueId.eq(UUID.fromString(filterByDocument)));
        }

        BooleanBuilder collectionBooleanBuilder = new BooleanBuilder();
        collectionIds.forEach(collection -> collectionBooleanBuilder
                .or(document.collection.uniqueId.eq(UUID.fromString(collection))));

        log.info("Leaving buildSearchPredicate()");
        return statusBooleanBuilder.and(searchBooleanBuilder).and(documentBooleanBuilder).and(collectionBooleanBuilder);
    }

    /**
     * Archives a document with the given unique ID.
     * @param  documentId      The unique ID of the document to be archived.
     * @throws DynamoException if the document ID is missing, or if the document is
     *                         not found.
     */
    @Transactional
    public void archiveDocument(UUID documentId) {
        log.info("Entering archiveDocument()");

        // Validate and Retrieve Document
        if (Objects.isNull(documentId)) {
            throw new DynamoException("Missing Document Unique ID. Please provide a valid unique ID.",
                    HttpStatus.BAD_REQUEST);
        }

        Document retrievedDocument = documentService.retrieveByUniqueId(documentId).orElseThrow(
                () -> new DynamoException("Document with unique-id '%s' not found".formatted(documentId),
                        HttpStatus.NOT_FOUND));

        // Archive children recursively
        archiveChildren(retrievedDocument);

        // Archive the current document
        retrievedDocument.setArchivedOn(Instant.now());
        retrievedDocument.setStatus(DocumentStatus.ARCHIVED.getValue());
        documentService.update(retrievedDocument);

        log.info("Leaving archiveDocument()");
    }

    /**
     * Archives the children of the given document recursively.
     * @param document The document whose children are to be archived.
     */
    private void archiveChildren(Document document) {
        log.info("Entering archiveChildren()");
        document.getChildren().forEach(child -> {
            Optional<Revision> optionalRevision =
                    revisionService.retrieveByDocumentAndStatus(child, DocumentStatus.FORKED);
            optionalRevision.ifPresent(revision -> {
                revision.setStatus(DocumentStatus.ARCHIVED.getValue());
                revisionService.update(revision);
            });
            archiveDocument(child.getUniqueId());
        });
        log.info("Leaving archiveChildren()");
    }

    /**
     * Deletes the document with the given ID, optionally permanently.
     * @param documentId  The unique ID of the document to delete.
     * @param isPermanent Whether the deletion should be permanent or not.
     */
    @Transactional
    public void deleteDocument(UUID documentId, boolean isPermanent) {
        log.info("Entering deleteDocument()");

        // Validate Document
        if (Objects.isNull(documentId)) {
            throw new DynamoException("Missing Document Unique ID. Please provide a valid unique ID.",
                    HttpStatus.BAD_REQUEST);
        }

        Document retrievedDocument = documentService.retrieveByUniqueId(documentId).orElseThrow(
                () -> new DynamoException("Document with unique-id '%s' not found".formatted(documentId),
                        HttpStatus.NOT_FOUND));

        // Permanent Delete
        if (isPermanent) {
            List<Revision> revisions = revisionService.retrieveByDocument(retrievedDocument);
            revisions.forEach(revision -> revisionService.delete(revision.getId()));
            documentService.delete(retrievedDocument.getId());
        } else {
            // Delete children recursively
            deleteChildren(retrievedDocument);

            // Archive the current document
            retrievedDocument.setDeletedOn(Instant.now());
            retrievedDocument.setStatus(DocumentStatus.DELETED.getValue());
            documentService.update(retrievedDocument);
        }

        log.info("Leaving deleteDocument()");
    }

    /**
     * Recursively deletes all children of the given document.
     * @param document The document whose children are to be deleted.
     */
    private void deleteChildren(Document document) {
        log.info("Entering deleteChildren()");
        document.getChildren().forEach(child -> {
            Optional<Revision> optionalRevision =
                    revisionService.retrieveByDocumentAndStatus(child, DocumentStatus.FORKED);
            optionalRevision.ifPresent(revision -> {
                revision.setStatus(DocumentStatus.DELETED.getValue());
                revisionService.update(revision);
            });
            deleteDocument(child.getUniqueId(), false);
        });
        log.info("Leaving deleteChildren()");
    }

    /**
     * Downloads the document with the given unique ID as a Markdown file.
     * @param documentId          The unique ID of the document to download.
     * @param httpServletResponse The HTTP servlet response to write the file
     *                            content to.
     */
    @Transactional
    public void downloadDocument(UUID documentId, HttpServletResponse httpServletResponse) {
        log.info("Entering downloadDocument()");

        // validate and Retrieve Document
        if (Objects.isNull(documentId)) {
            throw new DynamoException("Missing Document Unique ID. Please provide a valid unique ID.",
                    HttpStatus.BAD_REQUEST);
        }

        Document document = documentService.retrieveByUniqueId(documentId).orElseThrow(() -> new DynamoException(
                "Document with unique-id '%s' not found".formatted(documentId), HttpStatus.NOT_FOUND));

        // Retrieve User
        User user = validateAndRetrieveAuthenticatedUser();

        // Retrieve Revision
        Optional<Revision> optionalRevision =
                revisionService.retrieveByDocumentAndStatus(document, DocumentStatus.FORKED);
        if (optionalRevision.isPresent() && user.equals(optionalRevision.get().getEditedByUser())) {
            Revision revision = optionalRevision.get();
            document = revision.getDocument();
            document.setTitle(revision.getTitle());
            document.setContent(revision.getContent());
        }

        // Build Document Name
        String name = document.getTitle() != null && !document.getTitle().isBlank() ? document.getTitle() : "Untitled";
        httpServletResponse.setContentType(MediaType.TEXT_MARKDOWN_VALUE);
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=" + name + ".md");

        // Prepend title to content
        String documentContentWithHeader = document.getContent() != null ? document.getContent() : "";
        if (Objects.nonNull(document.getTitle()) && !document.getTitle().isBlank()) {
            documentContentWithHeader = "# " + document.getTitle() + "\n\n" + document.getContent();
        }

        byte[] contentBytes = documentContentWithHeader.getBytes();

        try (OutputStream outputStream = httpServletResponse.getOutputStream()) {
            outputStream.write(contentBytes);
        } catch (IOException e) {
            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        log.info("Leaving downloadDocument()");
    }

    /**
     * Retrieves the revisions of the document with the given unique ID.
     * @param  documentId               The unique ID of the document.
     * @param  sort                     The sort criteria for the revisions.
     * @param  searchOrFilterParameters The search or filter parameters to apply.
     * @return                          The list of revision DTOs.
     */
    public List<RevisionDto> retrieveDocumentRevisions(UUID documentId, Sort sort,
            MultiValueMap<String, String> searchOrFilterParameters) {
        log.info("Entering retrieveDocumentRevisions()");

        // Validate and Retrieve Document
        if (Objects.isNull(documentId)) {
            throw new DynamoException("Missing Document Unique ID. Please provide a valid unique ID.",
                    HttpStatus.BAD_REQUEST);
        }

        // Set of valid parameters for validation
        Set<String> validParameters = new HashSet<>(DEFAULT_VALID_PARAMETERS);
        validParameters.add("revision-id");
        validParameters.add("username");
        validParameters.add("revision-date");

        // Validate the provided parameters
        validateParameters(validParameters, searchOrFilterParameters.keySet());

        Document document = documentService.retrieveByUniqueId(documentId).orElseThrow(() -> new DynamoException(
                "Document with unique-id '%s' not found".formatted(documentId), HttpStatus.NOT_FOUND));

        // Build Boolean builder for filter
        BooleanBuilder booleanBuilder = buildSearchOrFilterPredicate(document, searchOrFilterParameters);

        // Retrieve Revisions
        List<Revision> revisions = revisionService.retrieveSortedEntitiesWithPredicate(booleanBuilder, sort);
        revisions.forEach(revisionMapper::revisionToRevisionDto);

        // Map Revision objects to RevisionDto objects
        List<RevisionDto> revisionDtos =
                revisions.stream().map(revisionMapper::revisionToRevisionDto).map(revisionDto -> {
                    // Create Extensions
                    Iterable<Extension> extensions =
                            Arrays.asList(TablesExtension.create(), ImageAttributesExtension.create());

                    // Parse Content
                    Parser parser = Parser.builder().extensions(extensions).build();
                    Node node = parser.parse(revisionDto.getContent() != null ? revisionDto.getContent() : "");

                    // Modify the AST to add <button> tag before each code block
                    node.accept(customHtmlRender);

                    // Build Html Render
                    HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
                    String htmlContent = renderer.render(node);
                    revisionDto.setHtmlContent(htmlContent);
                    return revisionDto;
                }).toList();

        log.info("Leaving retrieveDocumentRevisions()");
        return revisionDtos;
    }

    /**
     * Builds a BooleanBuilder predicate based on the provided search or filter
     * parameters for the given document.
     * @param  document                 The document to filter revisions for.
     * @param  searchOrFilterParameters The search or filter parameters to apply.
     * @return                          The BooleanBuilder predicate.
     * @throws DynamoException          If there's an error processing the search
     *                                  parameters.
     */
    private BooleanBuilder buildSearchOrFilterPredicate(Document document,
            MultiValueMap<String, String> searchOrFilterParameters) throws DynamoException {
        log.info("Entering buildSearchPredicate()");

        // Filter by Document
        QRevision revision = QRevision.revision;
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(revision.document.eq(document));

        // filter by Revision Id
        String filterByRevision = searchOrFilterParameters.getFirst("revision-id");
        if (Objects.nonNull(filterByRevision) && !filterByRevision.isBlank()) {
            log.info("Adding filter by Revision Unique Id predicate for value '{}'", filterByRevision);
            booleanBuilder.and(revision.uniqueId.eq(UUID.fromString(filterByRevision)));
        }

        // Filter by user name
        String filterByEmployeeName = searchOrFilterParameters.getFirst("username");
        if (Objects.nonNull(filterByEmployeeName) && !filterByEmployeeName.isBlank()) {
            log.info("Adding filter by username predicate for value '{}'", filterByEmployeeName);

            // Create a custom query using SQL-like syntax
            String fullNameQuery = "%" + filterByEmployeeName + "%";

            booleanBuilder.and(revision.editedByUser.firstName.concat(" ").concat(revision.editedByUser.lastName)
                    .likeIgnoreCase(fullNameQuery));
        }

        String createdDateValue = searchOrFilterParameters.getFirst("revision-date");
        if (Objects.nonNull(createdDateValue) && !createdDateValue.isBlank()) {

            try {
                Instant startDate = Instant.parse(createdDateValue).truncatedTo(ChronoUnit.DAYS);
                booleanBuilder.and(revision.editedOn.goe(startDate))
                        .and(revision.editedOn.lt(startDate.plus(1, ChronoUnit.DAYS)));
            } catch (DateTimeException e) {
                log.error("Error processing start date: {}", createdDateValue);
                throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
            }

        }

        log.info("Leaving buildSearchPredicate()");
        return booleanBuilder;
    }

}
