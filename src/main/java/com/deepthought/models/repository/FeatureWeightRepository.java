package com.deepthought.models.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import com.deepthought.models.edges.FeatureWeight;

/**
 * 
 */
public interface FeatureWeightRepository extends Neo4jRepository<FeatureWeight, Long> {

}