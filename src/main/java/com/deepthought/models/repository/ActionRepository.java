package com.deepthought.models.repository;

import java.util.Collection;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import com.deepthought.models.Action;


public interface ActionRepository extends Neo4jRepository<Action, Long> {

	Action findByName(@Param("name") String name);

	Action findByKey(@Param("key") String key);

    @Query("MATCH (o:ObjectDefinition)-[r:HAS_ACTION]->(a:Action) RETURN o,r,a LIMIT {limit}")
    Collection<Action> graph(@Param("limit") int limit);
}