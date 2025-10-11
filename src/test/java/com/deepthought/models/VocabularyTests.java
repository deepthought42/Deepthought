package com.deepthought.models;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the Vocabulary class
 */
public class VocabularyTests {
    
    private Vocabulary vocabulary;
    
    @Before
    public void setUp() {
        vocabulary = new Vocabulary("test_vocabulary");
    }
    
    @Test
    public void testConstructorWithLabel() {
        Vocabulary vocab = new Vocabulary("test_label");
        assertEquals("test_label", vocab.getLabel());
        assertEquals(0, vocab.size());
        assertTrue(vocab.getValueList().isEmpty());
    }
    
    @Test
    public void testConstructorWithFeatures() {
        List<Feature> features = Arrays.asList(
            new Feature("word1"),
            new Feature("word2"),
            new Feature("word3")
        );
        
        Vocabulary vocab = new Vocabulary(features, "test_features");
        assertEquals("test_features", vocab.getLabel());
        assertEquals(3, vocab.size());
        assertTrue(vocab.contains("word1"));
        assertTrue(vocab.contains("word2"));
        assertTrue(vocab.contains("word3"));
    }
    
    @Test
    public void testAddWord() {
        int index1 = vocabulary.addWord("hello");
        int index2 = vocabulary.addWord("world");
        int index3 = vocabulary.addWord("hello"); // Duplicate
        
        assertEquals(0, index1);
        assertEquals(1, index2);
        assertEquals(0, index3); // Should return existing index
        
        assertEquals(2, vocabulary.size());
        assertTrue(vocabulary.contains("hello"));
        assertTrue(vocabulary.contains("world"));
    }
    
    @Test
    public void testAddWordCaseInsensitive() {
        vocabulary.addWord("Hello");
        vocabulary.addWord("HELLO");
        vocabulary.addWord("hello");
        
        assertEquals(1, vocabulary.size());
        assertEquals(0, vocabulary.getIndex("Hello"));
        assertEquals(0, vocabulary.getIndex("HELLO"));
        assertEquals(0, vocabulary.getIndex("hello"));
    }
    
    @Test
    public void testAddWordTrimsWhitespace() {
        vocabulary.addWord("  hello  ");
        vocabulary.addWord("world\t");
        vocabulary.addWord("\nfoo\r");
        
        assertEquals(3, vocabulary.size());
        assertEquals("hello", vocabulary.getWord(0));
        assertEquals("world", vocabulary.getWord(1));
        assertEquals("foo", vocabulary.getWord(2));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAddWordNullThrowsException() {
        vocabulary.addWord(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAddWordEmptyThrowsException() {
        vocabulary.addWord("");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAddWordWhitespaceOnlyThrowsException() {
        vocabulary.addWord("   ");
    }
    
    @Test
    public void testAppendToVocabulary() {
        Feature feature1 = new Feature("test_word");
        Feature feature2 = new Feature("another_word");
        
        int index1 = vocabulary.appendToVocabulary(feature1);
        int index2 = vocabulary.appendToVocabulary(feature2);
        
        assertEquals(0, index1);
        assertEquals(1, index2);
        assertEquals(2, vocabulary.size());
        assertTrue(vocabulary.contains("test_word"));
        assertTrue(vocabulary.contains("another_word"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAppendToVocabularyNullFeatureThrowsException() {
        vocabulary.appendToVocabulary(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testAppendToVocabularyNullValueThrowsException() {
        Feature feature = new Feature(null);
        vocabulary.appendToVocabulary(feature);
    }
    
    @Test
    public void testGetIndex() {
        vocabulary.addWord("hello");
        vocabulary.addWord("world");
        
        assertEquals(0, vocabulary.getIndex("hello"));
        assertEquals(1, vocabulary.getIndex("world"));
        assertEquals(-1, vocabulary.getIndex("nonexistent"));
        assertEquals(-1, vocabulary.getIndex(null));
    }
    
    @Test
    public void testGetWord() {
        vocabulary.addWord("hello");
        vocabulary.addWord("world");
        
        assertEquals("hello", vocabulary.getWord(0));
        assertEquals("world", vocabulary.getWord(1));
        assertNull(vocabulary.getWord(-1));
        assertNull(vocabulary.getWord(2));
        assertNull(vocabulary.getWord(100));
    }
    
    @Test
    public void testContains() {
        vocabulary.addWord("hello");
        vocabulary.addWord("world");
        
        assertTrue(vocabulary.contains("hello"));
        assertTrue(vocabulary.contains("world"));
        assertTrue(vocabulary.contains("HELLO")); // Case insensitive
        assertFalse(vocabulary.contains("nonexistent"));
        assertFalse(vocabulary.contains(null));
    }
    
    @Test
    public void testSize() {
        assertEquals(0, vocabulary.size());
        
        vocabulary.addWord("word1");
        assertEquals(1, vocabulary.size());
        
        vocabulary.addWord("word2");
        assertEquals(2, vocabulary.size());
        
        vocabulary.addWord("word1"); // Duplicate
        assertEquals(2, vocabulary.size()); // Size should not change
    }
    
    @Test
    public void testGetValueList() {
        vocabulary.addWord("hello");
        vocabulary.addWord("world");
        
        List<String> valueList = vocabulary.getValueList();
        assertEquals(2, valueList.size());
        assertEquals("hello", valueList.get(0));
        assertEquals("world", valueList.get(1));
        
        // Ensure it returns a copy
        valueList.add("modified");
        assertEquals(2, vocabulary.getValueList().size());
    }
    
    @Test
    public void testGetFeatures() {
        vocabulary.addWord("hello");
        vocabulary.addWord("world");
        
        List<Feature> features = vocabulary.getFeatures();
        assertEquals(2, features.size());
        assertEquals("hello", features.get(0).getValue());
        assertEquals("world", features.get(1).getValue());
    }
    
    @Test
    public void testCreateFeatureVector() {
        vocabulary.addWord("hello");
        vocabulary.addWord("world");
        vocabulary.addWord("foo");
        vocabulary.addWord("bar");
        
        List<String> inputWords = Arrays.asList("hello", "foo", "nonexistent");
        boolean[] vector = vocabulary.createFeatureVector(inputWords);
        
        assertEquals(4, vector.length);
        assertTrue(vector[0]);  // hello
        assertFalse(vector[1]); // world
        assertTrue(vector[2]);  // foo
        assertFalse(vector[3]); // bar
    }
    
    @Test
    public void testCreateSparseVector() {
        vocabulary.addWord("hello");
        vocabulary.addWord("world");
        vocabulary.addWord("foo");
        vocabulary.addWord("bar");
        
        List<String> inputWords = Arrays.asList("hello", "foo", "nonexistent");
        Map<Integer, Double> sparseVector = vocabulary.createSparseVector(inputWords);
        
        assertEquals(2, sparseVector.size());
        assertEquals(1.0, sparseVector.get(0), 0.001); // hello
        assertEquals(1.0, sparseVector.get(2), 0.001); // foo
        assertNull(sparseVector.get(1)); // world not present
        assertNull(sparseVector.get(3)); // bar not present
    }
    
    @Test
    public void testInitializeMappings() {
        // Simulate loading from Neo4j
        vocabulary.setValueList(Arrays.asList("word1", "word2", "word3"));
        vocabulary.initializeMappings();
        
        assertEquals(3, vocabulary.size());
        assertEquals(0, vocabulary.getIndex("word1"));
        assertEquals(1, vocabulary.getIndex("word2"));
        assertEquals(2, vocabulary.getIndex("word3"));
        assertEquals("word1", vocabulary.getWord(0));
        assertEquals("word2", vocabulary.getWord(1));
        assertEquals("word3", vocabulary.getWord(2));
    }
    
    @Test
    public void testClear() {
        vocabulary.addWord("hello");
        vocabulary.addWord("world");
        assertEquals(2, vocabulary.size());
        
        vocabulary.clear();
        assertEquals(0, vocabulary.size());
        assertTrue(vocabulary.getValueList().isEmpty());
        assertFalse(vocabulary.contains("hello"));
        assertFalse(vocabulary.contains("world"));
    }
    
    @Test
    public void testToString() {
        vocabulary.addWord("hello");
        vocabulary.addWord("world");
        
        String str = vocabulary.toString();
        assertTrue(str.contains("test_vocabulary"));
        assertTrue(str.contains("size=2"));
        assertTrue(str.contains("hello"));
        assertTrue(str.contains("world"));
    }
    
    @Test
    public void testEqualsAndHashCode() {
        Vocabulary vocab1 = new Vocabulary("test");
        Vocabulary vocab2 = new Vocabulary("test");
        Vocabulary vocab3 = new Vocabulary("different");
        
        assertEquals(vocab1, vocab2);
        assertNotEquals(vocab1, vocab3);
        assertEquals(vocab1.hashCode(), vocab2.hashCode());
        assertNotEquals(vocab1.hashCode(), vocab3.hashCode());
    }
    
    @Test
    public void testConcurrentAccess() throws InterruptedException {
        final int numThreads = 10;
        final int wordsPerThread = 100;
        Thread[] threads = new Thread[numThreads];
        
        // Create threads that add words concurrently
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < wordsPerThread; j++) {
                    vocabulary.addWord("word_" + threadId + "_" + j);
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Verify all words were added (no duplicates due to thread safety)
        assertEquals(numThreads * wordsPerThread, vocabulary.size());
        
        // Verify we can still access words correctly
        for (int i = 0; i < numThreads; i++) {
            for (int j = 0; j < wordsPerThread; j++) {
                String word = "word_" + i + "_" + j;
                assertTrue("Word should exist: " + word, vocabulary.contains(word));
                assertTrue("Index should be valid", vocabulary.getIndex(word) >= 0);
            }
        }
    }
}
