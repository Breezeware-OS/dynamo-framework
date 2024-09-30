package net.breezeware.dynamo.generics.crud.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import net.breezeware.dynamo.generics.crud.entity.GenericEntity;
import net.breezeware.dynamo.generics.crud.service.GenericService;

public abstract class GenericController<T extends GenericEntity> {

    private final GenericService<T> service;

    public GenericController(GenericService<T> genericService) {
        this.service = genericService;
    }

    @GetMapping
    public Page<T> getPage(Pageable pageable) {
        return service.getPage(pageable);
    }

    @PostMapping("")
    public ResponseEntity<T> create(@RequestBody T created) {
        return ResponseEntity.ok(service.create(created));
    }

    @PutMapping("")
    public ResponseEntity<T> update(@RequestBody T updated) {
        return ResponseEntity.ok(service.update(updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.retrieveById(id).get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Ok");
    }
}
