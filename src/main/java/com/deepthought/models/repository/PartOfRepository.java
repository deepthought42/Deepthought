package com.deepthought.models.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import com.deepthought.models.edges.PartOf;

/**
 * Handles CRUD operations for {@link PartOf} relationship entities in the graph.
 */
public interface PartOfRepository extends Neo4jRepository<PartOf, Long> {

}
