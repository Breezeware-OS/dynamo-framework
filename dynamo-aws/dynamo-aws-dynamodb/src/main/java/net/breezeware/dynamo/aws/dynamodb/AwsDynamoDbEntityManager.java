package net.breezeware.dynamo.aws.dynamodb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.breezeware.dynamo.utils.exception.DynamoSdkException;

import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

@Service
@Slf4j
public class AwsDynamoDbEntityManager<T extends DynamoDbEntity> {

    @Autowired
    DynamoDbEnhancedClient dynamoDbEnhancedClient;

    public void createTableForEntity(Class<T> entityClass) throws DynamoSdkException {
        log.info("Entering createTableForEntity(). Entity type = {}", entityClass);

        String tableName = getTableName(entityClass);
        DynamoDbTable<T> entityTable = buildDynamoDbTable(entityClass, tableName);

        try {
            entityTable.createTable(builder -> builder
                    .provisionedThroughput(b -> b.readCapacityUnits(10L).writeCapacityUnits(10L).build()));
        } catch (Exception e) {
            log.error("Exception occured while creating DynamoDB table.");
            throw new DynamoSdkException(e.getMessage(), e);
        }

        log.info("Waiting for table creation...");

        try (DynamoDbWaiter waiter = DynamoDbWaiter.create()) { // DynamoDbWaiter is Autocloseable
            ResponseOrException<DescribeTableResponse> response =
                    waiter.waitUntilTableExists(builder -> builder.tableName(tableName).build()).matched();
            DescribeTableResponse tableDescription =
                    response.response().orElseThrow(() -> new RuntimeException(tableName + " table was not created."));

            // The actual error can be inspected in response.exception()
            log.info(tableDescription.table().tableName() + " was created.");
        }

        log.info("Leaving createTableForEntity()");
    }

    public T createItem(Class<T> entityClass, T item) throws DynamoSdkException {
        log.info("Entering createItem()");

        DynamoDbTable<T> entityTable = buildDynamoDbTable(entityClass, getTableName(entityClass));

        // Put the customer data into an Amazon DynamoDB table.
        try {
            entityTable.putItem(item);
        } catch (ResourceNotFoundException e) {
            log.error("Exception occured. E = {}", e);

            log.info("Table to insert item was not found. Creating the table first and attempting insert again.");
            // Try to create the table since it does not exist & then insert the item.
            try {
                this.createTableForEntity(entityClass);
                entityTable.putItem(item);
            } catch (DynamoSdkException e1) {
                log.error("Exception occured. E = {}", e1);
                throw new DynamoSdkException("Failed to create a table before inserting the item into it", e1);
            }

        }

        log.info("Leaving createItem(). Item created in table successfully.");
        return item;
    }

    public List<T> createMultipleItems(Class<T> entityClass, List<T> items) {
        log.info("Entering createMultipleItems()");

        log.info("Leaving createMultipleItems()");
        return items;
    }

    public Optional<T> retrieveItem(Class<T> entityClass, String partitionKeyVal) throws DynamoSdkException {
        log.info("Entering retrieveItem(). Partition Key Value = {}", partitionKeyVal);

        DynamoDbTable<T> entityTable = buildDynamoDbTable(entityClass, getTableName(entityClass));

        Key key = Key.builder().partitionValue(partitionKeyVal).build();

        // Get the item by using the key.
        T result = entityTable.getItem((GetItemEnhancedRequest.Builder requestBuilder) -> requestBuilder.key(key));

        log.info("Leaving retrieveItem()");

        if (result != null) {
            return Optional.of(result);
        } else {
            return Optional.empty();
        }

    }

    public Optional<T> retrieveItem(Class<T> entityClass, String partitionKeyVal, String sortKeyVal)
            throws DynamoSdkException {
        log.info("Entering retrieveItem(). Partition Key Value = {}, Sort Key Value = }", partitionKeyVal, sortKeyVal);

        DynamoDbTable<T> entityTable = buildDynamoDbTable(entityClass, getTableName(entityClass));

        Key key = Key.builder().partitionValue(partitionKeyVal).sortValue(sortKeyVal).build();

        // Get the item by using the key.
        T result = entityTable.getItem((GetItemEnhancedRequest.Builder requestBuilder) -> requestBuilder.key(key));

        log.info("Leaving retrieveItem()");

        if (result != null) {
            return Optional.of(result);
        } else {
            return Optional.empty();
        }

    }

    public void deleteItem(Class<T> entityClass, String partitionKeyVal, String sortKeyVal) throws DynamoSdkException {
        log.info("Entering deleteItem(). Partition Key Value = {}, Sort Key Value = {}", partitionKeyVal, sortKeyVal);

        Key key = Key.builder().partitionValue(partitionKeyVal).sortValue(sortKeyVal).build();
        DynamoDbTable<T> entityTable = buildDynamoDbTable(entityClass, getTableName(entityClass));
        T deletedItem = entityTable.deleteItem(key);
        log.info("Leaving deleteItem()");

        if (deletedItem == null) {
            log.error("Could not delete the item with ID = {}. Item may not exist in the table.", partitionKeyVal);
            throw new DynamoSdkException("Could not delete item with ID " + partitionKeyVal);
        }

    }

    private String getTableName(Class<T> entityClass) throws DynamoSdkException {

        String tableName = "";

        try {
            Method method = entityClass.getMethod("getDynamoDbEntityTableName");
            tableName = (String) method.invoke(entityClass.getDeclaredConstructor(null).newInstance(null));
            log.info("table name = {}", tableName);
        } catch (NoSuchMethodException | SecurityException e1) {
            e1.printStackTrace();
            throw new DynamoSdkException("Error while getting table name corresponding to the entity", e1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new DynamoSdkException("Error while getting table name corresponding to the entity", e);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new DynamoSdkException("Error while getting table name corresponding to the entity", e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new DynamoSdkException("Error while getting table name corresponding to the entity", e);
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new DynamoSdkException("Error while getting table name corresponding to the entity", e);
        }

        return tableName;
    }

    private DynamoDbTable<T> buildDynamoDbTable(Class<T> entityClass, String tableName) {
        DynamoDbTable<T> entityTable = dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(entityClass));

        return entityTable;
    }

    // public void deleteTableForEntity(Class<T> entityClass) throws DynamoException
    // {
    // log.info("Entering deleteTableForEntity(). Entity type = {}", entityClass);
    //
    // DynamoDbTable<T> entityTable =
    // dynamoDbEnhancedClient.table(entityClass.getName(),
    // TableSchema.fromBean(entityClass));
    //
    // // Delete the table
    // try {
    // entityTable.deleteTable();
    // } catch (ResourceInUseException e) {
    // log.error("Exception while deleting table. Exception = {}", e);
    // throw new DynamoException(e.getMessage());
    // }
    //
    // log.info("Waiting for table deletion...");
    //
    // try (DynamoDbWaiter waiter = DynamoDbWaiter.create()) { // DynamoDbWaiter is
    // Autocloseable
    // ResponseOrException<DescribeTableResponse> response = waiter
    // .waitUntilTableNotExists(builder ->
    // builder.tableName(entityClass.getName()).build()).matched();
    //
    // DescribeTableResponse tableDescription = response.response()
    // .orElseThrow(() -> new DynamoException(entityClass.getName() + " table was
    // not deleted."));
    // // The actual error can be inspected in response.exception()
    // log.info(tableDescription.table().tableName() + " was deleted.");
    // }
    // log.info("Leaving deleteTableForEntity()");
    // }
}
