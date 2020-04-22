package com.deepthought.models.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import com.deepthought.models.MemoryRecord;

/**
 * Spring Data Repository pattern to perform CRUD operations and other various queries
 *  on {@link MemoryRecord} 
 */
public interface MemoryRecordRepository extends Neo4jRepository<MemoryRecord, Long> {
}