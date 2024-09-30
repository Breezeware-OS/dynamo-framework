package net.breezeware.dynamo.generics.crud.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import net.breezeware.dynamo.generics.crud.entity.GenericEntity;

@NoRepositoryBean
public interface GenericRepository<T extends GenericEntity>
        extends JpaRepository<T, Long>, QuerydslPredicateExecutor<T> {
}
