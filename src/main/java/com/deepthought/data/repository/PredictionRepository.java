package com.deepthought.data.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import com.deepthought.data.edges.Prediction;

/**
 * Handles crud operations for {@link Prediction} data objects in database
 */
public interface PredictionRepository extends Neo4jRepository<Prediction, Long> {

}