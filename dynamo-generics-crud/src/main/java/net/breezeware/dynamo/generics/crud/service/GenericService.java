package net.breezeware.dynamo.generics.crud.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import jakarta.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.querydsl.core.types.Predicate;

import net.breezeware.dynamo.generics.crud.dao.GenericRepository;
import net.breezeware.dynamo.generics.crud.entity.GenericEntity;

/**
 * A generic service class that provides common CRUD operations for entities.
 * @param <T> the type of the entity
 */
public abstract class GenericService<T extends GenericEntity> {

    private final GenericRepository<T> repository;

    /**
     * Constructs a new GenericService with the provided GenericRepository.
     * @param repository the repository for accessing and managing entity data.
     */
    public GenericService(GenericRepository<T> repository) {
        this.repository = repository;
    }

    /**
     * Retrieves a page of entities based on the provided Pageable.
     * @param  pageable the pagination information.
     * @return          a page of entities.
     */
    @Transactional
    public Page<T> getPage(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /**
     * Retrieves a page of entities based on the provided Predicate and Pageable.
     * @param  predicate the predicate containing filter/search criteria.
     * @param  pageable  the pagination information.
     * @return           a page of entities matching the filter/search criteria.
     */
    @Transactional
    public Page<T> retrievePageEntitiesWithPredicate(Predicate predicate, Pageable pageable) {
        return repository.findAll(predicate, pageable);
    }

    /**
     * Retrieves a list of entities based on the provided {@link Predicate} and
     * {@link Sort}.
     * @param  predicate the predicate containing filter/search criteria.
     * @param  sort      the sorting information.
     * @return           a list of sorted entities matching the filter/search
     *                   criteria.
     */
    @Transactional
    public List<T> retrieveSortedEntitiesWithPredicate(Predicate predicate, Sort sort) {
        return (List<T>) repository.findAll(predicate, sort);
    }

    /**
     * Retrieves a list of entities based on the provided {@link Sort}.
     * @param  sort the sorting information.
     * @return      a list of sorted entities.
     */
    @Transactional
    public List<T> retrieveSortedEntities(Sort sort) {
        return repository.findAll(sort);
    }

    /**
     * Retrieves a count of entities based on the provided {@link Predicate}.
     * @param  predicate the predicate containing filter criteria.
     * @return           a count of entities matching the filter criteria.
     */
    @Transactional
    public long retrieveEntitiesCount(Predicate predicate) {
        return repository.count(predicate);
    }

    /**
     * Retrieves a list of entities.
     * @return a list of entities.
     */
    @Transactional
    public List<T> retrieveEntities() {
        return repository.findAll();
    }

    /**
     * Creates a new entity.
     * @param  entity                   the entity to create.
     * @return                          the created entity.
     * @throws IllegalArgumentException if the entity to be created is null.
     */
    @Transactional
    public T create(T entity) {
        Optional.ofNullable(entity)
                .orElseThrow(() -> new IllegalArgumentException("Entity to be created cannot be 'null'"));
        entity.setCreatedOn(Instant.now());
        entity.setModifiedOn(Instant.now());
        return repository.save(entity);
    }

    /**
     * Persists all the entities.
     * @param  entities entities to be saved/persisted.
     * @return          {@link List} of saved entities.
     */
    @Transactional
    public List<T> saveAll(List<T> entities) {
        return repository.saveAll(entities);
    }

    /**
     * Deletes all the entities.
     * @param entities entities to be delete.
     */
    @Transactional
    public void deleteAll(List<T> entities) {
        repository.deleteAll(entities);
    }

    /**
     * Retrieves an entity by its ID.
     * @param  id the ID of the entity to retrieve.
     * @return    an Optional containing the retrieved entity, or an empty Optional
     *            if not found.
     */
    @Transactional
    public Optional<T> retrieveById(Long id) {
        return repository.findById(id);
    }

    /**
     * Updates an existing entity with the provided updated data.
     * @param  updatedItem            the updated data for the entity.
     * @return                        the updated entity.
     * @throws NoSuchElementException if entity with id not found.
     */
    @Transactional
    public T update(T updatedItem) {
        Optional.ofNullable(updatedItem)
                .orElseThrow(() -> new IllegalArgumentException("Entity to be updated cannot be 'null'"));
        Long updatedItemId = updatedItem.getId();
        T actualItem = retrieveById(updatedItemId).orElseThrow(
                () -> new NoSuchElementException("Entity with id '%d' not found".formatted(updatedItemId)));
        // copy information from updatedItem to the actualItem
        BeanUtils.copyProperties(updatedItem, actualItem, getNullPropertyNames(updatedItem));
        actualItem.setModifiedOn(Instant.now());
        return repository.save(actualItem);
    }

    /**
     * Deletes an entity by its ID.
     * @param id the ID of the entity to delete.
     */
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    /**
     * Gets the names of properties with null values in the provided object.
     * @param  source the object to check for null properties.
     * @return        an array of property names with null values.
     */
    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }

        }

        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

}
