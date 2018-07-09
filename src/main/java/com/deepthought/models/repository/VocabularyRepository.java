package com.deepthought.models.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import com.deepthought.models.Feature;
import com.deepthought.models.Vocabulary;

/**
 * 
 */
public interface VocabularyRepository extends Neo4jRepository<Vocabulary, Long> {

	Vocabulary findByLabel(@Param("label") String laberl);
	Vocabulary findByKey(@Param("key") String key);
    
    @Query("MATCH (v:Vocabulary{name:{end_vocab_name}})-[:HAS_FEATURE]->(f1:Feature) RETURN f1")
	Iterable<Feature> findAllFeatures(@Param("end_vocab_name") String end_vocab_name);
}