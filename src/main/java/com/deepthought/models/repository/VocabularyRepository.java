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

	Vocabulary findByLabel(@Param("label") String label);

	@Query("MATCH (:Vocabulary{label:{vocab_label}})-[:HAS_FEATURE]-(f:Feature{key:{feature_key}}) RETURN f")
	Feature findFeatureByKey(@Param("vocab_label") String vocab_label, @Param("feature_key") String feature_key);
}