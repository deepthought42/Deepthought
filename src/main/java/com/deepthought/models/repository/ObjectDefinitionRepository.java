package com.deepthought.models.repository;

import java.util.Collection;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import com.deepthought.models.ObjectDefinition;

/**
 * 
 */
public interface ObjectDefinitionRepository extends Neo4jRepository<ObjectDefinition, Long> {

	ObjectDefinition findByValue(@Param("value") String value);
	ObjectDefinition findByKey(@Param("key") String key);
	ObjectDefinition findByType(@Param("type") String type);
	
    @Query("MATCH (a:Action)<-[r:HAS_ACTION]-(o:ObjectDefinition) RETURN o,r,a LIMIT {limit}")
    Collection<ObjectDefinition> graph(@Param("limit") int limit);
}