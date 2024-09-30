package net.breezeware.dynamo.dynamodocssvc.dao;

import org.springframework.stereotype.Repository;

import net.breezeware.dynamo.dynamodocssvc.entity.Attachment;
import net.breezeware.dynamo.generics.crud.dao.GenericRepository;

@Repository
public interface AttachmentRepository extends GenericRepository<Attachment> {
}
