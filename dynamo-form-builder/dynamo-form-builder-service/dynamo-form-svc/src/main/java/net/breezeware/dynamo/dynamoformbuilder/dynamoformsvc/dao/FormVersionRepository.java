package net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.Form;
import net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.entity.FormVersion;
import net.breezeware.dynamo.generics.crud.dao.GenericRepository;

/**
 * Repository interface for managing and accessing FormVersion entities. Extends
 * the GenericRepository interface for common CRUD operations and defines a
 * custom query methods.
 */
@Repository
public interface FormVersionRepository extends GenericRepository<FormVersion> {

    /**
     * Retrieves an optional FormVersion entity by matching a specific Form and
     * status.
     * @param  form   the Form entity to match.
     * @param  status the status of the FormVersion to search for.
     * @return        an Optional containing the retrieved FormVersion if found, or
     *                an empty Optional if not found.
     */
    Optional<FormVersion> findByFormAndStatus(Form form, String status);

    /**
     * Retrieves a list of FormVersion entities associated with the provided Form.
     * @param  form the Form entity to retrieve FormVersions for.
     * @return      a List containing the retrieved FormVersions.
     */
    List<FormVersion> findByForm(Form form);
}