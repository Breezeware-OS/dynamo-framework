package net.breezeware.dynamo.aws.dynamodb;

import java.time.Instant;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class Customer extends DynamoDbEntity {

    private String id;

    private String name;

    private String email;

    private Instant regDate;

    public static Customer getSampleCustomer() {
        Customer c = new Customer();

        c.setCustName("ABC Company");
        c.setEmail("help@abccompany.com");
        c.setId("ID-10001");
        c.setRegistrationDate(Instant.now());

        return c;
    }

    @DynamoDbPartitionKey
    public String getId() {
        return this.id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getCustName() {
        return this.name;
    }

    public void setCustName(String name) {
        this.name = name;
    }

    @DynamoDbSortKey
    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getRegistrationDate() {
        return regDate;
    }

    public void setRegistrationDate(Instant registrationDate) {
        this.regDate = registrationDate;
    }

    @Override
    public String toString() {
        return "Customer [id=" + id + ", name=" + name + ", email=" + email + ", regDate=" + regDate + "]";
    }

    @Override
    public String getDynamoDbEntityTableName() {
        return "customer-db-entity-table";
    }
}