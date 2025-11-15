package com.deepthought.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deepthought.data.models.Vocabulary;
import com.deepthought.data.repository.VocabularyRepository;

/**
 * Service for interacting with the database layer to retrieve and update Vocabularies
 * in the Neo4j database.
 */
@Service
public class VocabularyService {
	
	@Autowired
	private VocabularyRepository vocabularyRepository;
	
	/**
	 * Loads a vocabulary by label. If the vocabulary doesn't exist, creates a new one.
	 * 
	 * @param label The label of the vocabulary to load
	 * @return The vocabulary (existing or newly created)
	 */
	@Transactional
	public Vocabulary load(String label) {
		if (label == null || label.trim().isEmpty()) {
			throw new IllegalArgumentException("Label cannot be null or empty");
		}
		
		Optional<Vocabulary> vocabularyOpt = vocabularyRepository.findByLabel(label);
		
		if (vocabularyOpt.isPresent()) {
			Vocabulary vocabulary = vocabularyOpt.get();
			// Initialize mappings after loading from Neo4j
			vocabulary.initializeMappings();
			return vocabulary;
		} else {
			// Create a new vocabulary if it doesn't exist
			Vocabulary vocabulary = new Vocabulary(label);
			return vocabularyRepository.save(vocabulary);
		}
	}
	
	/**
	 * Saves or updates a vocabulary in the database.
	 * 
	 * @param vocabulary The vocabulary to save
	 * @return The saved vocabulary
	 */
	@Transactional
	public Vocabulary save(Vocabulary vocabulary) {
		if (vocabulary == null) {
			throw new IllegalArgumentException("Vocabulary cannot be null");
		}
		
		// Ensure size is synchronized with valueList
		if (vocabulary.getValueList() != null) {
			vocabulary.setSize(vocabulary.getValueList().size());
		}
		
		return vocabularyRepository.save(vocabulary);
	}
	
	/**
	 * Updates an existing vocabulary in the database.
	 * 
	 * @param vocabulary The vocabulary to update
	 * @return The updated vocabulary
	 * @throws IllegalArgumentException if the vocabulary doesn't exist
	 */
	@Transactional
	public Vocabulary update(Vocabulary vocabulary) {
		if (vocabulary == null || vocabulary.getId() == null) {
			throw new IllegalArgumentException("Vocabulary and its ID cannot be null");
		}
		
		if (!vocabularyRepository.existsById(vocabulary.getId())) {
			throw new IllegalArgumentException("Vocabulary with ID " + vocabulary.getId() + " does not exist");
		}
		
		// Ensure size is synchronized with valueList
		if (vocabulary.getValueList() != null) {
			vocabulary.setSize(vocabulary.getValueList().size());
		}
		
		return vocabularyRepository.save(vocabulary);
	}
	
	/**
	 * Finds a vocabulary by its label.
	 * 
	 * @param label The label to search for
	 * @return Optional containing the vocabulary if found
	 */
	@Transactional(readOnly = true)
	public Optional<Vocabulary> findByLabel(String label) {
		if (label == null || label.trim().isEmpty()) {
			return Optional.empty();
		}
		
		Optional<Vocabulary> vocabularyOpt = vocabularyRepository.findByLabel(label);
		if (vocabularyOpt.isPresent()) {
			Vocabulary vocabulary = vocabularyOpt.get();
			vocabulary.initializeMappings();
		}
		return vocabularyOpt;
	}
	
	/**
	 * Checks if a vocabulary exists with the given label.
	 * 
	 * @param label The label to check
	 * @return true if a vocabulary with this label exists
	 */
	@Transactional(readOnly = true)
	public boolean existsByLabel(String label) {
		if (label == null || label.trim().isEmpty()) {
			return false;
		}
		return vocabularyRepository.existsByLabel(label);
	}
	
	/**
	 * Deletes a vocabulary by its ID.
	 * 
	 * @param id The ID of the vocabulary to delete
	 */
	@Transactional
	public void deleteById(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("ID cannot be null");
		}
		vocabularyRepository.deleteById(id);
	}
}

