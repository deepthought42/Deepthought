package com.deepthought.models.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import com.deepthought.models.Feature;
import com.deepthought.models.edges.FeatureWeight;

/**
 * 
 */
public interface FeatureRepository extends Neo4jRepository<Feature, Long> {

	public Feature findByValue(@Param("value") String value);
	
	@Query("MATCH (:Feature{value:{value}})-[fw:HAS_RELATED_FEATURE]->(:Feature) RETURN fw")
	public Set<FeatureWeight> getFeatureWeights(@Param("value") String value);
	
	@Query("MATCH p=(f1:Feature{value:{input_value}})-[fw:HAS_RELATED_FEATURE]->(f2:Feature{value:{output_value}}) RETURN f1,fw,f2")
	public List<Feature> getConnectedFeatures(@Param("input_value") String input_value,
										     @Param("output_value") String output_value);	
}