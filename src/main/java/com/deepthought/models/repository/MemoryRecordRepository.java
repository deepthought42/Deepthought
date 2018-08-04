package com.deepthought.models.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import com.deepthought.models.MemoryRecord;

/**
 * 
 */
public interface MemoryRecordRepository extends Neo4jRepository<MemoryRecord, Long> {
}