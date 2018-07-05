package com.deepthought.models.repository;

import java.util.Collection;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import com.deepthought.models.Feature;

/**
 * 
 */
public interface FeatureRepository extends Neo4jRepository<Feature, Long> {

	Feature findByValue(@Param("value") String value);
	Feature findByKey(@Param("key") String key);
	Feature findByType(@Param("type") String type);
	
    @Query("MATCH (a:Action)<-[r:HAS_ACTION]-(o:Feature) RETURN o,r,a LIMIT {limit}")
    Collection<Feature> graph(@Param("limit") int limit);
}