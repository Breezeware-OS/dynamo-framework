package net.breezeware.dynamo.dynamoaisvc.service;

import lombok.extern.slf4j.Slf4j;
import net.breezeware.dynamo.dynamoaisvc.dao.ModelRepository;
import net.breezeware.dynamo.dynamoaisvc.entity.Model;
import net.breezeware.dynamo.generics.crud.service.GenericService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ModelService extends GenericService<Model> {
    private final ModelRepository modelRepository;

    public ModelService(ModelRepository modelRepository) {
        super(modelRepository);
        this.modelRepository = modelRepository;
    }

    /**
     * Retrieves an AI model by its unique ID.
     * @param  uniqueId the unique ID of the AI model
     * @return          an {@code Optional} containing the AI model if found, or an
     *                  empty {@code Optional} if not found
     */
    public Optional<Model> retrieveModelByUniqueId(UUID uniqueId) {
        log.info("Entering retrieveModelByUniqueId()");
        Optional<Model> optionalModel = modelRepository.findByUniqueId(uniqueId);
        log.info("Leaving retrieveModelByUniqueId()");
        return optionalModel;
    }

}
