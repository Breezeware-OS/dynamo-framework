package net.breezeware.dynamo.aws.dynamodb;

import java.time.Instant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import net.breezeware.dynamo.utils.exception.DynamoSdkException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AwsDynamoDbEntityManagerTest {

    static Customer customer = null;

    @Autowired
    AwsDynamoDbEntityManager<Customer> awsDynamoDbEntityManager;

    @BeforeAll
    static void setupData() {

        customer = new Customer();
        customer.setCustName("ABC Company");
        customer.setEmail("help@abccompany.com");
        customer.setId("ID-10001");
        customer.setRegistrationDate(Instant.now());
    }

    // @Test
    @Order(1)
    void testRetrieveItem_nonexistantTable_exception() {
        Assertions.assertThrows(DynamoSdkException.class, () -> {
            awsDynamoDbEntityManager.retrieveItem(Customer.class, customer.getId(), customer.getEmail());
        });
    }

    // @Test
    @Order(2)
    void testCreateTable_validTable_noException() {
        Assertions.assertDoesNotThrow(() -> {
            awsDynamoDbEntityManager.createTableForEntity(Customer.class);
        });
    }

    // @Test
    @Order(3)
    void testCreateTable_duplicateTable_throwsException() {
        Assertions.assertThrows(DynamoSdkException.class, () -> {
            awsDynamoDbEntityManager.createTableForEntity(Customer.class);
        });
    }

    // @Test
    @Order(4)
    void testCreateItem_validItem() {
        Assertions.assertDoesNotThrow(() -> {
            awsDynamoDbEntityManager.createItem(Customer.class, customer);
        });
    }

    // @Test
    // @Order(4)
    // void testCreateMultipleItems() {
    // fail("Not yet implemented");
    // }
    //

    // @Test
    @Order(5)
    void testRetrieveItem_validTable_validKey() {
        Assertions.assertAll("GroupedAssertionHeading",
                () -> Assertions.assertTrue(awsDynamoDbEntityManager
                        .retrieveItem(Customer.class, customer.getId(), customer.getEmail()).isPresent()),
                () -> Assertions.assertDoesNotThrow(() -> {
                    awsDynamoDbEntityManager.retrieveItem(Customer.class, customer.getId(), customer.getEmail());
                }));
    }

    // @Test
    @Order(6)
    void testRetrieveItem_validTable_invalidKey() {

        Assertions.assertAll("GroupedAssertionHeading",
                () -> Assertions.assertTrue(awsDynamoDbEntityManager
                        .retrieveItem(Customer.class, "invalid-key-value", customer.getEmail()).isPresent()),
                () -> Assertions.assertDoesNotThrow(() -> {
                    awsDynamoDbEntityManager.retrieveItem(Customer.class, customer.getId(), customer.getEmail());
                }));
    }

    // @Test
    @Order(7)
    void testDeleteItem_noExceptionIfValidId() {
        Assertions.assertDoesNotThrow(() -> {
            awsDynamoDbEntityManager.deleteItem(Customer.class, customer.getId(), customer.getEmail());
        });
    }

    // @Test
    @Order(8)
    void testDeleteItem_throwsExceptionIfInvalidId() {
        Assertions.assertThrows(DynamoSdkException.class, () -> {
            awsDynamoDbEntityManager.deleteItem(Customer.class, "invalid-key-value", customer.getEmail());
        });
    }

    // @Test
    // @Order(9)
    // void testDeleteTable_noException() {
    // Assertions.assertDoesNotThrow(() -> {
    // awsDynamoDbEntityManager.deleteTableForEntity(Customer.class);
    // });
    // }
    //
    // @Test
    // @Order(10)
    // void testDeleteTable_throwsException() {
    // Assertions.assertThrows(DynamoException.class, () -> {
    // awsDynamoDbEntityManager.deleteTableForEntity(Customer.class);
    // });
    // }
}
