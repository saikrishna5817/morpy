package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.AnswerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class AnswerDao {

    @PersistenceContext
    private EntityManager entityManager;

    public AnswerEntity getAnswerByUuid(final String uuid){
        try{
            return entityManager.createNamedQuery("AnswerEntity.answerByUuid", AnswerEntity.class)
                    .setParameter("uuid", uuid).getSingleResult();
        }catch(NoResultException nre){
            return null;
        }
    }
	   public AnswerEntity createAnswer(AnswerEntity answerEntity) {
       entityManager.persist(answerEntity);
       return answerEntity;
   }
   public AnswerEntity editAnswer(AnswerEntity answerEntity) {
       entityManager.merge(answerEntity);
       return answerEntity;
   }

    public void delete(AnswerEntity answerEntity) {
        //answerEntity = entityManager.find(AnswerEntity.class, answerEntity.getId());
        entityManager.remove(answerEntity);
    }
}
