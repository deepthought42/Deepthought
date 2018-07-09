package com.deepthought.models.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import com.deepthought.models.Feature;

/**
 * 
 */
@Component
public interface FeatureRepository extends Neo4jRepository<Feature, Long> {
	Feature findByKey(@Param("key") String key);
}