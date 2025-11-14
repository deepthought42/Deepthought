package com.deepthought.data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.deepthought.data.models.Vocabulary;

/**
 * Repository interface for Vocabulary entities in Neo4j
 * Provides CRUD operations and custom queries for vocabulary management
 */
@Repository
public interface VocabularyRepository extends Neo4jRepository<Vocabulary, Long> {
    
    /**
     * Finds a vocabulary by its label
     * 
     * @param label The vocabulary label to search for
     * @return Optional containing the vocabulary if found
     */
    Optional<Vocabulary> findByLabel(String label);
    
    /**
     * Checks if a vocabulary exists with the given label
     * 
     * @param label The vocabulary label to check
     * @return true if a vocabulary with this label exists
     */
    boolean existsByLabel(String label);
    
    /**
     * Finds all vocabularies that contain a specific word
     * 
     * @param word The word to search for
     * @return List of vocabularies containing the word
     */
    @Query("MATCH (v:Vocabulary) WHERE $word IN v.valueList RETURN v")
    List<Vocabulary> findByWord(@Param("word") String word);
    
    /**
     * Finds vocabularies with size greater than the specified value
     * 
     * @param minSize The minimum size threshold
     * @return List of vocabularies meeting the size criteria
     */
    List<Vocabulary> findBySizeGreaterThan(int minSize);
    
    /**
     * Finds vocabularies with size between min and max (inclusive)
     * 
     * @param minSize The minimum size
     * @param maxSize The maximum size
     * @return List of vocabularies within the size range
     */
    List<Vocabulary> findBySizeBetween(int minSize, int maxSize);
    
    /**
     * Finds vocabularies by label pattern (case-insensitive)
     * 
     * @param pattern The pattern to match (can include wildcards)
     * @return List of vocabularies matching the pattern
     */
    @Query("MATCH (v:Vocabulary) WHERE toLower(v.label) CONTAINS toLower($pattern) RETURN v")
    List<Vocabulary> findByLabelContainingIgnoreCase(@Param("pattern") String pattern);
    
    /**
     * Gets the total count of vocabularies
     * 
     * @return The total number of vocabularies
     */
    @Query("MATCH (v:Vocabulary) RETURN count(v)")
    long countAllVocabularies();
    
    /**
     * Gets the average size of all vocabularies
     * 
     * @return The average vocabulary size
     */
    @Query("MATCH (v:Vocabulary) RETURN avg(v.size)")
    double getAverageVocabularySize();
    
    /**
     * Finds the largest vocabulary by size
     * 
     * @return Optional containing the largest vocabulary
     */
    @Query("MATCH (v:Vocabulary) RETURN v ORDER BY v.size DESC LIMIT 1")
    Optional<Vocabulary> findLargestVocabulary();
    
    /**
     * Finds vocabularies that are similar to the given vocabulary
     * (based on shared words)
     * 
     * @param vocabularyId The ID of the vocabulary to compare against
     * @param minSharedWords The minimum number of shared words
     * @return List of similar vocabularies
     */
    @Query("MATCH (v1:Vocabulary) WHERE id(v1) = $vocabularyId " +
           "MATCH (v2:Vocabulary) WHERE id(v2) <> $vocabularyId " +
           "WITH v1, v2, [word IN v1.valueList WHERE word IN v2.valueList] as shared " +
           "WHERE size(shared) >= $minSharedWords " +
           "RETURN v2 ORDER BY size(shared) DESC")
    List<Vocabulary> findSimilarVocabularies(@Param("vocabularyId") Long vocabularyId, 
                                           @Param("minSharedWords") int minSharedWords);
    
    /**
     * Deletes vocabularies with size less than the specified value
     * 
     * @param maxSize The maximum size for deletion
     * @return Number of vocabularies deleted
     */
    @Query("MATCH (v:Vocabulary) WHERE v.size < $maxSize DETACH DELETE v RETURN count(v)")
    long deleteSmallVocabularies(@Param("maxSize") int maxSize);
    
    /**
     * Updates the size field for all vocabularies based on their valueList
     * This is useful for data consistency after manual updates
     */
    @Query("MATCH (v:Vocabulary) SET v.size = size(v.valueList)")
    void updateAllSizes();
}
