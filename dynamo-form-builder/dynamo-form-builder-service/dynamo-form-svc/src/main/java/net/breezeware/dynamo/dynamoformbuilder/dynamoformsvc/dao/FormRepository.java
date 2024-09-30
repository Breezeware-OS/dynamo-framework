package net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.dao;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.Form;
import net.breezeware.dynamo.generics.crud.dao.GenericRepository;

/**
 * Repository interface for managing and accessing Form entities. Extends the
 * GenericRepository interface for common CRUD operations.
 */
@Repository
public interface FormRepository extends GenericRepository<Form> {

    Optional<Form> findByUniqueId(String uniqueId);
}