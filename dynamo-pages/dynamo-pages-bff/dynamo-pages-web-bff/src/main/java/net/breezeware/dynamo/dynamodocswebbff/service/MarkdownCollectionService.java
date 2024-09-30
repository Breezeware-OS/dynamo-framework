package net.breezeware.dynamo.dynamodocswebbff.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import com.querydsl.core.BooleanBuilder;

import net.breezeware.dynamo.dynamodocssvc.entity.Collection;
import net.breezeware.dynamo.dynamodocssvc.entity.Document;
import net.breezeware.dynamo.dynamodocssvc.entity.QCollection;
import net.breezeware.dynamo.dynamodocssvc.entity.QDocument;
import net.breezeware.dynamo.dynamodocssvc.entity.Revision;
import net.breezeware.dynamo.dynamodocssvc.enumeration.DocumentPermission;
import net.breezeware.dynamo.dynamodocssvc.enumeration.DocumentStatus;
import net.breezeware.dynamo.dynamodocssvc.service.CollectionService;
import net.breezeware.dynamo.dynamodocssvc.service.DocumentService;
import net.breezeware.dynamo.dynamodocssvc.service.RevisionService;
import net.breezeware.dynamo.dynamodocswebbff.dto.CollectionDto;
import net.breezeware.dynamo.dynamodocswebbff.dto.CollectionViewDto;
import net.breezeware.dynamo.dynamodocswebbff.dto.DocumentDto;
import net.breezeware.dynamo.dynamodocswebbff.mapper.CollectionMapper;
import net.breezeware.dynamo.dynamodocswebbff.mapper.DocumentMapper;
import net.breezeware.dynamo.dynamodocswebbff.utils.CustomHtmlRender;
import net.breezeware.dynamo.usermanagement.entity.User;
import net.breezeware.dynamo.usermanagement.service.UserService;
import net.breezeware.dynamo.utils.bean.BeanUtils;
import net.breezeware.dynamo.utils.exception.DynamoException;
import net.breezeware.dynamo.utils.exception.ValidationExceptionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarkdownCollectionService {

    private static final Set<String> DEFAULT_VALID_PARAMETERS = Set.of("page-no", "page-size", "sort", "search");

    private final Validator fieldValidator;

    private final UserService userService;

    private final CollectionService collectionService;

    private final DocumentService documentService;

    private final RevisionService revisionService;

    private final CollectionMapper collectionMapper;

    private final DocumentMapper documentMapper;

    private final CustomHtmlRender customHtmlRender;

    /**
     * Creates a new collection based on the provided CollectionDto.
     * @param  collectionDto   The CollectionDto containing information about the
     *                         collection to be created.
     * @return                 The CollectionDto representing the newly created
     *                         collection.
     * @throws DynamoException If there's an issue with the operation.
     */
    @Transactional
    public CollectionDto createCollection(CollectionDto collectionDto) {
        log.info("Entering createCollection()");

        // Field constraint violation validation
        Set<ConstraintViolation<CollectionDto>> fieldViolations = fieldValidator.validate(collectionDto);

        // Field constraint violation handling
        ValidationExceptionUtils.handleException(fieldViolations);

        // Validate Collection permission
        DocumentPermission.retrievePermission(collectionDto.getPermission())
                .orElseThrow(() -> new DynamoException(
                        String.format(
                                """
                                Invalid Permission '%s' for Collection'. \
                                Possible Collection Permission types are: '%s'\
                                """,
                                collectionDto.getPermission(), DocumentPermission.retrieveAllPermissions()),
                        HttpStatus.BAD_REQUEST));

        // Retrieve User
        User user = validateAndRetrieveAuthenticatedUser();

        // Map CollectionDto to Collection
        Collection collection = collectionMapper.collectionDtoToCollection(collectionDto);
        collection.setUniqueId(UUID.randomUUID());
        collection.setCreatedByUser(user);
        Collection savedCollection = collectionService.create(collection);

        // Map Collection to CollectionDto
        CollectionDto savedCollectionDto = collectionMapper.collectionToCollectionDto(savedCollection);

        log.info("Leaving createCollection()");

        return savedCollectionDto;

    }

    /**
     * Validates the current authentication and retrieves the authenticated user.
     * @return                 The authenticated User.
     * @throws DynamoException If the user is not authenticated or not found.
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
     * Updates an existing collection.
     * @param  collectionDto   The CollectionDto object containing the updated
     *                         information.
     * @return                 The updated CollectionDto object.
     * @throws DynamoException If there are validation errors or the collection is
     *                         not found.
     */
    @Transactional
    public CollectionDto updateCollection(CollectionDto collectionDto) {
        log.info("Entering updateCollection()");

        // Field constraint violation validation
        Set<ConstraintViolation<CollectionDto>> fieldViolations = fieldValidator.validate(collectionDto);

        // Field constraint violation handling
        ValidationExceptionUtils.handleException(fieldViolations);

        // Validate Collection
        if (collectionDto.getId() <= 0) {
            throw new DynamoException("Invalid Collection ID. Please provide a valid ID.", HttpStatus.BAD_REQUEST);
        }

        if (Objects.isNull(collectionDto.getUniqueId())) {
            throw new DynamoException("Missing Collection Unique ID. Please provide a valid unique ID.",
                    HttpStatus.BAD_REQUEST);
        }

        Collection retrievedCollection = collectionService.retrieveByUniqueId(collectionDto.getUniqueId())
                .orElseThrow(() -> new DynamoException(
                        String.format("Collection with unique-id '%s' not found", collectionDto.getUniqueId()),
                        HttpStatus.NOT_FOUND));

        // Validate Collection permission
        DocumentPermission.retrievePermission(collectionDto.getPermission())
                .orElseThrow(() -> new DynamoException(
                        String.format(
                                """
                                Invalid Permission '%s' for Collection'. \
                                Possible Collection Permission types are: '%s'\
                                """,
                                collectionDto.getPermission(), DocumentPermission.retrieveAllPermissions()),
                        HttpStatus.BAD_REQUEST));

        // Map CollectionDto to Collection
        Collection collection = collectionMapper.collectionDtoToCollection(collectionDto);
        retrievedCollection.setCreatedByUser(retrievedCollection.getCreatedByUser());

        // Update Collection
        Collection updatedCollection = collectionService.update(collection);

        // Map Collection to CollectionDto
        CollectionDto updatedCollectionDto = collectionMapper.collectionToCollectionDto(updatedCollection);

        log.info("Leaving updateCollection()");

        return updatedCollectionDto;
    }

    /**
     * Deletes a collection and its associated documents.
     * @param  collectionId    The unique identifier of the collection to delete.
     * @throws DynamoException If the collection or associated documents are not
     *                         found.
     */
    @Transactional
    public void deleteCollection(UUID collectionId) {
        log.info("Entering deleteCollection()");

        // Validate Collection
        if (Objects.isNull(collectionId)) {
            throw new DynamoException("Missing Collection Unique ID. Please provide a valid unique ID.",
                    HttpStatus.BAD_REQUEST);
        }

        Collection collection = collectionService.retrieveByUniqueId(collectionId).orElseThrow(
                () -> new DynamoException("Collection with unique-id '%s' not found".formatted(collectionId),
                        HttpStatus.NOT_FOUND));

        // Retrieve User
        User user = validateAndRetrieveAuthenticatedUser();

        // Update Collection Associated Document Status to Deleted
        documentService.retrieveByCollection(collection).forEach(document -> {
            Optional<Revision> optionalRevision =
                    revisionService.retrieveByDocumentAndStatus(document, DocumentStatus.FORKED);
            if (optionalRevision.isPresent()) {
                Revision revision = optionalRevision.get();
                revision.setStatus(DocumentStatus.DELETED.getValue());
                revision.setEditedByUser(user);
                revision.setEditedOn(Instant.now());
                revisionService.update(revision);
            }

            document.setStatus(DocumentStatus.DELETED.getValue());
            document.setDeletedOn(Instant.now());
            document.setCollection(null);
            documentService.update(document);
        });

        // Delete Collection
        collectionService.delete(collection.getId());

        log.info("Leaving deleteCollection()");
    }

    /**
     * Retrieves collections based on provided search parameters and pageable
     * information.
     * @param  pageable         The pagination information.
     * @param  searchParameters The search parameters to filter collections.
     * @return                  A CollectionViewDto containing the retrieved
     *                          collections.
     */
    public CollectionViewDto retrieveCollections(Pageable pageable, MultiValueMap<String, String> searchParameters) {
        log.info("Entering retrieveCollections()");

        // Set of valid parameters for validation
        Set<String> validParameters = new HashSet<>(DEFAULT_VALID_PARAMETERS);
        validParameters.add("collection-id");
        validParameters.add("fields");

        // Validate the provided parameters
        validateParameters(validParameters, searchParameters.keySet());

        // Retrieve User
        User user = validateAndRetrieveAuthenticatedUser();

        // Filter by CollectionId
        String filterByCollectionId = searchParameters.getFirst("collection-id");
        QCollection collections = QCollection.collection;
        BooleanBuilder collectionBooleanBuilder = new BooleanBuilder();
        if (Objects.nonNull(filterByCollectionId) && !filterByCollectionId.isBlank()) {
            collectionBooleanBuilder.and(collections.uniqueId.eq(UUID.fromString(filterByCollectionId)));
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.unsorted());
        }

        CollectionViewDto collectionViewDto = new CollectionViewDto();
        List<CollectionDto> collectionDtos = new ArrayList<>();

        List<Collection> collectionList =
                collectionService.retrieveSortedEntitiesWithPredicate(collectionBooleanBuilder, pageable.getSort())
                        .stream().map(collection -> {
                            if (!collection.getCreatedByUser().getUniqueId().equals(user.getUniqueId())) {
                                if (collection.getPermission().equals(DocumentPermission.NO_ACCESS.getValue())) {
                                    return null;
                                }

                                return collection;
                            }

                            return collection;
                        }).filter(Objects::nonNull).collect(Collectors.toList());

        if (searchParameters.containsKey("fields")) {
            List<Map<Object, Object>> collectionsWithSpecificFields =
                    retrieveCollectionsWithSpecificFields(searchParameters, collectionList);

            collectionViewDto.setData(collectionsWithSpecificFields);
            collectionViewDto.setPaged(false);
        } else {
            for (Collection collection : collectionList) {
                // Build a BooleanBuilder predicate based on the provided search parameters
                BooleanBuilder booleanBuilder = buildSearchOrFilterPredicate(collection, user, searchParameters);

                // Map Collection to CollectionDto
                CollectionDto collectionDto = collectionMapper.collectionToCollectionDto(collection);

                // Retrieve Documents
                List<Document> documents =
                        documentService.retrieveSortedEntitiesWithPredicate(booleanBuilder, pageable.getSort());

                // Build NestedDocument
                List<DocumentDto> documentList = new ArrayList<>();
                for (Document document : documents) {
                    if (document.getParent() == null) {
                        documentList.add(buildNestedDocument(document, documents));
                    }

                }

                collectionDto.setDocumentList(documentList);
                collectionDtos.add(collectionDto);
            }

            collectionViewDto.setData(collectionDtos);
            collectionViewDto.setPaged(false);

        }

        log.info("Leaving retrieveCollections()");

        return collectionViewDto;
    }

    /**
     * Retrieves collections with specific fields based on provided search
     * parameters.
     * @param  searchParameters The search parameters containing fields and
     *                          unique-values.
     * @param  collections      The list of collections to retrieve fields from.
     * @return                  A list of maps containing collections with specific
     *                          fields.
     */
    private List<Map<Object, Object>> retrieveCollectionsWithSpecificFields(
            MultiValueMap<String, String> searchParameters, List<Collection> collections) {
        log.info("Entering retrieveCollectionsWithSpecificFields()");

        // Validate whether the requested fields are valid and known
        String fieldsParam = searchParameters.getFirst("fields");
        String[] requestedFields = Objects.requireNonNull(fieldsParam).split(",");
        List<String> unknownRequestedFields = new ArrayList<>(requestedFields.length);
        for (String field : requestedFields) {
            boolean hasField = BeanUtils.hasField(Collection.class, field);
            if (!hasField) {
                unknownRequestedFields.add(field);
            }

        }

        // Throw exception if unknown fields are requested
        if (!unknownRequestedFields.isEmpty()) {
            log.info("Invalid fields requested, fields: {}", unknownRequestedFields);
            throw new DynamoException("Invalid field(s) %s requested".formatted(unknownRequestedFields),
                    HttpStatus.BAD_REQUEST);
        }

        String uniqueValue = searchParameters.getFirst("unique-values");
        List<Map<Object, Object>> collectionsWithSpecificFields;
        if (Objects.nonNull(uniqueValue) && !uniqueValue.isBlank() && uniqueValue.equalsIgnoreCase("true")) {
            // Reduce collectionsWithSpecificFields with only requested fields with unique
            Set<Map<Object, Object>> uniqueCollectionsWithSpecificFields = new HashSet<>();
            for (Collection fleet : collections) {
                Map<Object, Object> fieldMap = BeanUtils.getBeanWithFields(fleet, requestedFields);
                uniqueCollectionsWithSpecificFields.add(fieldMap);
            }

            // Set the collectionsWithSpecificFields data
            collectionsWithSpecificFields = new ArrayList<>(uniqueCollectionsWithSpecificFields);
        } else {
            // Reduce collectionsWithSpecificFields with only requested fields
            collectionsWithSpecificFields = BeanUtils.getBeansWithFields(collections, requestedFields);
        }

        log.info("Leaving retrieveCollectionsWithSpecificFields()");
        return collectionsWithSpecificFields;
    }

    /**
     * Builds a nested document DTO from a parent document and its associated
     * documents.
     * @param  parentDocument The parent document.
     * @param  documents      The list of all documents associated with the parent
     *                        document.
     * @return                The nested document DTO.
     */
    private DocumentDto buildNestedDocument(Document parentDocument, List<Document> documents) {
        log.info("Entering buildNestedDocument()");

        // Retrieve User
        User user = validateAndRetrieveAuthenticatedUser();

        // Retrieve Document Forked Revision
        Optional<Revision> revision =
                revisionService.retrieveByDocumentAndStatus(parentDocument, DocumentStatus.FORKED);
        if (revision.isPresent() && user.equals(revision.get().getEditedByUser())) {
            Document revisionDocument = revision.get().getDocument();
            parentDocument = revisionDocument;
            parentDocument.setTitle(revisionDocument.getTitle());
            parentDocument.setContent(revisionDocument.getContent());
        }

        // Build and Parse Common mark Extensions
        Iterable<Extension> extensions = Arrays.asList(TablesExtension.create(), ImageAttributesExtension.create());
        Parser parser = Parser.builder().extensions(extensions).build();

        // Parse Content
        Node node = parser.parse(parentDocument.getContent() != null ? parentDocument.getContent() : "");

        // Modify the AST to add <button> tag before each code block
        node.accept(customHtmlRender);

        // Build HtmlRenderer
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
        String htmlContent = renderer.render(node);

        // Map Parent Document to Parent DocumentDto
        DocumentDto parentDocumentDto = documentMapper.documentToDocumentDto(parentDocument);
        parentDocumentDto.setHtmlContent(htmlContent);

        // Build Children Document
        List<DocumentDto> children = new ArrayList<>();
        for (Document document : documents) {
            if (document.getParent() != null && Objects.equals(document.getParent().getId(), parentDocument.getId())) {
                children.add(buildNestedDocument(document, documents));
            }

        }

        parentDocumentDto.setChildren(children);

        log.info("Leaving buildNestedDocument()");
        return parentDocumentDto;
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
     * Builds a predicate for searching or filtering documents associated with a
     * collection.
     * @param  collection               The collection to filter documents by.
     * @param  user                     The authenticated user.
     * @param  searchOrFilterParameters The search or filter parameters.
     * @return                          The BooleanBuilder predicate.
     * @throws DynamoException          If an error occurs during predicate
     *                                  building.
     */
    private BooleanBuilder buildSearchOrFilterPredicate(Collection collection, User user,
            MultiValueMap<String, String> searchOrFilterParameters) throws DynamoException {
        log.info("Entering buildSearchPredicate()");

        QDocument document = QDocument.document;
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        // Filter by Collection
        if (Objects.nonNull(collection)) {
            booleanBuilder.and(document.collection.eq(collection));
            if (!collection.getCreatedByUser().getUniqueId().equals(user.getUniqueId())) {
                booleanBuilder.and(document.collection.permission.ne(DocumentPermission.NO_ACCESS.getValue()));
            }

        }

        // Filter by Search value
        String filterBySearchValue = searchOrFilterParameters.getFirst("search");
        BooleanBuilder searchBooleanBuilder = new BooleanBuilder();
        if (Objects.nonNull(filterBySearchValue) && !filterBySearchValue.isBlank()) {
            log.info("Adding filter by Document Title and Content predicate for value '{}'", filterBySearchValue);
            searchBooleanBuilder.or(document.title.containsIgnoreCase(filterBySearchValue))
                    .or(document.content.containsIgnoreCase(filterBySearchValue));
        }

        // Filter by Status
        BooleanBuilder statusBooleanBuilder = new BooleanBuilder();
        statusBooleanBuilder.and(document.status.ne(DocumentStatus.DELETED.getValue()))
                .and(document.status.ne(DocumentStatus.ARCHIVED.getValue()))
                .and(document.status.ne(DocumentStatus.DRAFTED.getValue()));

        log.info("Leaving buildSearchPredicate()");
        return booleanBuilder.and(searchBooleanBuilder).and(statusBooleanBuilder);
    }

    /**
     * Uploads a document to a collection.
     * @param  collectionId     The unique identifier of the collection.
     * @param  parentDocumentId The unique identifier of the parent document, if
     *                          applicable.
     * @param  file             The multipart file to upload.
     * @throws DynamoException  If an error occurs during the upload process.
     */
    @Transactional
    public void uploadDocument(UUID collectionId, UUID parentDocumentId, MultipartFile file) {
        log.info("Entering uploadDocument()");

        // Validate Collection
        if (Objects.isNull(collectionId)) {
            throw new DynamoException("Missing Collection Unique ID. Please provide a valid unique ID.",
                    HttpStatus.BAD_REQUEST);
        }

        Collection collection = collectionService.retrieveByUniqueId(collectionId).orElseThrow(
                () -> new DynamoException("Collection with unique-id '%s' not found".formatted(collectionId),
                        HttpStatus.NOT_FOUND));

        // Validate File
        validateUploadedFile(file);

        // Retrieve User
        User user = validateAndRetrieveAuthenticatedUser();

        // Parse File content
        String content = parseFile(file);

        // Build Title and Content
        Map<String, String> titleInfo = getTitleAndContent(file, content);
        content = titleInfo.get("content");
        String title = titleInfo.get("title");

        // Build and Save document
        Document savedDocument = buildAndPersistsDocument(parentDocumentId, title, content, collection, user);

        // Build and save revision
        buildAndPersistsRevision(savedDocument);
        log.info("Leaving uploadDocument()");
    }

    /**
     * Parses the content of a multipart file.
     * @param  file            The multipart file to parse.
     * @return                 The content of the file as a string.
     * @throws DynamoException If an error occurs during parsing.
     */
    private String parseFile(MultipartFile file) {
        log.info("Entering parseFile()");
        try {
            byte[] bytes = file.getBytes();
            String input = new String(bytes, StandardCharsets.UTF_8);
            log.info("Leaving parseFile()");
            return input;
        } catch (IOException e) {
            throw new DynamoException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Extracts the title and content from a multipart file and its content.
     * @param  file            The multipart file from which to extract the title.
     * @param  content         The content of the file.
     * @return                 A map containing the extracted title and content.
     * @throws DynamoException If the title exceeds 100 characters.
     */
    private Map<String, String> getTitleAndContent(MultipartFile file, String content) {
        log.info("Entering getTitleAndContent()");

        Parser parser = Parser.builder().build();
        Node node = parser.parse(content);
        String heading = findHeading(node);
        if (Objects.nonNull(heading)) {
            if (heading.length() >= 100) {
                throw new DynamoException("Uploaded file Title must be less than 100 characters",
                        HttpStatus.BAD_REQUEST);
            }

            String modifiedInput = removeHeading(content);
            String title = heading.trim();
            Map<String, String> titleInfo = new HashMap<>();
            titleInfo.put("content", modifiedInput);
            titleInfo.put("title", title);
            log.info("Leaving getTitleAndContent()");
            return titleInfo;
        } else {
            String title = extractNameWithoutExtension(file.getOriginalFilename());
            Map<String, String> titleInfo = new HashMap<>();
            titleInfo.put("content", content);
            titleInfo.put("title", title);
            log.info("Leaving getTitleAndContent()");
            return titleInfo;
        }

    }

    /**
     * Builds and persists a revision for the given document.
     * @param savedDocument The document for which to build and persist a revision.
     */
    private void buildAndPersistsRevision(Document savedDocument) {
        log.info("Entering buildAndPersistsRevision()");
        Revision revision = new Revision();
        revision.setDocument(savedDocument);
        revision.setTitle(savedDocument.getTitle());
        revision.setContent(savedDocument.getContent());
        revision.setEditedByUser(savedDocument.getCreatedByUser());
        revision.setEditedOn(Instant.now());
        revision.setVersion(savedDocument.getVersion());
        revision.setUniqueId(UUID.randomUUID());
        revision.setStatus(DocumentStatus.PUBLISHED.getValue());
        revisionService.create(revision);
        log.info("Leaving buildAndPersistsRevision()");
    }

    /**
     * Builds and persists a document with the provided attributes.
     * @param  parentDocumentId The unique identifier of the parent document, if
     *                          applicable.
     * @param  title            The title of the document.
     * @param  input            The content of the document.
     * @param  collection       The collection to which the document belongs.
     * @param  user             The user who created the document.
     * @return                  The saved document entity.
     */
    private Document buildAndPersistsDocument(UUID parentDocumentId, String title, String input, Collection collection,
            User user) {
        log.info("Entering buildAndPersistsDocument()");
        Document document = new Document();
        document.setTitle(title);
        document.setContent(input);
        document.setUniqueId(UUID.randomUUID());
        document.setCollection(collection);
        document.setVersion(1);
        document.setCreatedByUser(user);
        document.setStatus(DocumentStatus.PUBLISHED.getValue());
        document.setPublishedOn(Instant.now());
        if (Objects.nonNull(parentDocumentId)) {
            Optional<Document> retrievedMarkdownDocument = documentService.retrieveByUniqueId(parentDocumentId);
            retrievedMarkdownDocument.ifPresent(document::setParent);

        }

        Document savedDocument = documentService.create(document);
        log.info("Leaving buildAndPersistsDocument()");
        return savedDocument;
    }

    /**
     * Validates the uploaded file.
     * @param file The multipart file to be validated.
     */
    private void validateUploadedFile(MultipartFile file) {
        log.info("Entering validateUploadedFile()");

        if (file.isEmpty()) {
            throw new DynamoException("File is Empty", HttpStatus.BAD_REQUEST);
        }

        if (!MediaType.TEXT_MARKDOWN_VALUE.equalsIgnoreCase(Objects.requireNonNull(file.getContentType()))) {
            throw new DynamoException("Uploaded file must be of .md type", HttpStatus.BAD_REQUEST);
        }

        log.info("Leaving validateUploadedFile()");

    }

    /**
     * Removes the heading from the content.
     * @param  content The content from which the heading will be removed.
     * @return         The content without the heading.
     */
    public String removeHeading(String content) {
        log.info("Entering removeHeading()");

        Pattern pattern = Pattern.compile("^# .+", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            log.info("Leaving removeHeading()");
            return matcher.replaceFirst("");
        } else {
            log.info("Leaving removeHeading()");
            return content;
        }

    }

    /**
     * Finds the heading text in the given Node.
     * @param  node The node to search for the heading.
     * @return      The text of the first heading found, or null if no heading is
     *              found.
     */
    public String findHeading(Node node) {
        log.info("Entering findHeading()");

        Node firstHeading = findFirstHeading(node);
        if (firstHeading instanceof Heading) {
            Node textNode = firstHeading.getFirstChild();
            if (textNode instanceof Text text) {
                return text.getLiteral();
            }

        }

        log.info("Leaving findHeading()");
        return null;
    }

    /**
     * Finds the first heading with level 1 in the given Node.
     * @param  node The node to search for the first heading.
     * @return      The first heading node found with level 1, or null if no such
     *              heading is found.
     */
    public Node findFirstHeading(Node node) {
        log.info("Entering findFirstHeading()");

        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof Heading heading && heading.getLevel() == 1) {
                return child;
            } else if (child instanceof Heading) {
                return null;
            }

            Node result = findFirstHeading(child);
            if (result != null) {
                return result;
            }

        }

        log.info("Leaving findFirstHeading()");

        return null;
    }

    /**
     * Extracts the name without extension from the given filename.
     * @param  filename The filename from which to extract the name.
     * @return          The name without extension extracted from the filename.
     */
    private String extractNameWithoutExtension(String filename) {
        log.info("Entering extractNameWithoutExtension()");

        Pattern pattern = Pattern.compile("^(.*?)\\.md$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(filename);
        if (matcher.matches()) {
            log.info("Leaving extractNameWithoutExtension()");
            return matcher.group(1);
        } else {
            log.info("Leaving extractNameWithoutExtension()");
            return filename;
        }

    }

}
