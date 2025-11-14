package com.deepthought.models.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import com.deepthought.edges.FeatureWeight;

/**
 * Spring Data Repository pattern to perform CRUD operations and other various queries
 *  on {@link FeatureWeight} 
 */
public interface FeatureWeightRepository extends Neo4jRepository<FeatureWeight, Long> {

}