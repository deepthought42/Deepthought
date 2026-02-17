package com.deepthought.models.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import com.deepthought.models.ImageMatrixNode;

/**
 * Handles CRUD operations for {@link ImageMatrixNode} entities in the graph.
 */
public interface ImageMatrixNodeRepository extends Neo4jRepository<ImageMatrixNode, Long> {

}
