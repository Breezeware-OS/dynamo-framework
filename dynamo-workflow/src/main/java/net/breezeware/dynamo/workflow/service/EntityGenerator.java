package net.breezeware.dynamo.workflow.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.breezeware.dynamo.workflow.dto.TaskForm;
import net.breezeware.dynamo.workflow.entity.ProcessDomainEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EntityGenerator {

    private final ObjectMapper objectMapper;

    private final ProcessDomainEntityService processDomainEntityService;

    @Transactional
    public List<ProcessDomainEntity> generateAndPersistEntities(TaskForm taskForm) {
        log.info("Entering generateAndPersistEntities() {}", taskForm.getFormSchemaAndDataJson());
        List<ProcessDomainEntity> processDomainEntityList = new ArrayList<>();
        Map<String, JsonNode> entityNodes = buildJsonNodes(taskForm.getFormSchemaAndDataJson());
        for (String entityName : entityNodes.keySet()) {
            Optional<ProcessDomainEntity> optProcessDomainEntity = processDomainEntityService
                    .retrieveProcessDomainEntity(taskForm.getProcessInstanceUserDefinedKey(), entityName);
            JsonNode entityProperties = entityNodes.get(entityName);
            ProcessDomainEntity processDomainEntity;
            if (optProcessDomainEntity.isPresent()) {
                processDomainEntity = optProcessDomainEntity.get();
                JsonNode entityProp = processDomainEntity.getEntityProperties();
                ((ObjectNode) entityProp).setAll((ObjectNode) entityProperties);
            } else {
                processDomainEntity = ProcessDomainEntity.builder().entityName(entityName)
                        .processInstanceUserDefinitionKey(taskForm.getProcessInstanceUserDefinedKey())
                        .entityProperties(entityProperties).createdOn(Instant.now()).modifiedOn(Instant.now()).build();
            }

            processDomainEntityList.add(processDomainEntityService.saveProcessDomainEntity(processDomainEntity));
        }

        log.info("Leaving generateAndPersistEntities()");
        return processDomainEntityList;
    }

    private Map<String, JsonNode> buildJsonNodes(String jsonString) {
        log.info("Entering buildJsonNodes()");
        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);
            Map<String, JsonNode> transformedMap = new HashMap<>();

            JsonNode components = rootNode.get("components");
            for (JsonNode component : components) {
                JsonNode jsonNodeKey = component.get("key");
                JsonNode jsonNodeValue = component.get("value");
                JsonNode sectionNameNode = component.path("properties").path("section_name");
                JsonNode displayNameNode = component.path("properties").path("display_name");

                if (jsonNodeKey != null && jsonNodeValue != null && sectionNameNode.isTextual()
                        && displayNameNode.isTextual()) {
                    String key = jsonNodeKey.asText();
                    String keyPrefix = getKeyPrefix(key);
                    String keyPrefixLastIndexValue = getKeyPrefixLastIndexValue(key);
                    String sectionName = sectionNameNode.asText();

                    // get existing entity node
                    ObjectNode entityNode;
                    if (transformedMap.containsKey(keyPrefix)) {
                        entityNode = (ObjectNode) transformedMap.get(keyPrefix);
                    } else {
                        entityNode = objectMapper.createObjectNode();
                        transformedMap.put(keyPrefix, entityNode);
                    }

                    // get existing section array node
                    ArrayNode sectionNode;
                    if (entityNode.has(sectionName)) {
                        sectionNode = (ArrayNode) entityNode.get(sectionName);
                    } else {
                        sectionNode = objectMapper.createArrayNode();
                        entityNode.set(sectionName, sectionNode);
                    }

                    String displayName = displayNameNode.asText();
                    String value = jsonNodeValue.asText();
                    // create property object node
                    ObjectNode propertyNode = objectMapper.createObjectNode();
                    propertyNode.put("key", keyPrefixLastIndexValue);
                    propertyNode.put("display_name", displayName);
                    propertyNode.put("value", value);

                    sectionNode.add(propertyNode);
                }

            }

            log.info("Leaving buildJsonNodes()");
            return transformedMap;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    private String getKeyPrefix(String key) {
        int firstIndex = key.indexOf('.');
        if (firstIndex != -1) {
            return key.substring(0, firstIndex);
        }

        return null;
    }

    private String getKeyPrefixLastIndexValue(String key) {
        int lastIndex = key.lastIndexOf('.');
        if (lastIndex != -1) {
            return key.substring(lastIndex + 1);
        }

        return key;
    }
}