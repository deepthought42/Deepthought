package com.deepthought.models.repository;

import java.util.Collection;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import com.deepthought.models.ObjectDefinition;
import com.deepthought.models.Vocabulary;

/**
 * 
 */
public interface VocabularyRepository extends Neo4jRepository<Vocabulary, Long> {

	Vocabulary findByLabel(@Param("label") String laberl);
	Vocabulary findByKey(@Param("key") String key);

    @Query("MATCH (o:ObjectDefinition)<-[r:HAS_ACTION]-(a:Action) RETURN m,r,a LIMIT {limit}")
    Collection<ObjectDefinition> graph(@Param("limit") int limit);
}