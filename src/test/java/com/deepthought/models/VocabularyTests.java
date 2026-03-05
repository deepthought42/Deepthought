package com.deepthought.models;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = "Regression")
public class VocabularyTests {

    private Vocabulary vocabulary;

    @BeforeMethod
    public void setUp() {
        vocabulary = new Vocabulary("test_vocabulary");
    }

    public void constructorAndAddWord_supportNormalizationAndDuplicates() {
        assertEquals(vocabulary.getLabel(), "test_vocabulary");
        assertEquals(vocabulary.size(), 0);

        assertEquals(vocabulary.addWord("  Hello  "), 0);
        assertEquals(vocabulary.addWord("hello"), 0);
        assertEquals(vocabulary.addWord("WORLD"), 1);

        assertEquals(vocabulary.size(), 2);
        assertTrue(vocabulary.contains("hello"));
        assertEquals(vocabulary.getWord(1), "world");
    }

    public void addWord_rejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> vocabulary.addWord(null));
        assertThrows(IllegalArgumentException.class, () -> vocabulary.addWord(""));
        assertThrows(IllegalArgumentException.class, () -> vocabulary.addWord("   "));
    }

    public void appendAndVectorMethods_workForKnownWords() {
        vocabulary.appendToVocabulary(new Feature("alpha"));
        vocabulary.appendToVocabulary(new Feature("beta"));

        boolean[] featureVector = vocabulary.createFeatureVector(Arrays.asList("beta", "missing"));
        assertEquals(featureVector.length, 2);
        assertFalse(featureVector[0]);
        assertTrue(featureVector[1]);

        Map<Integer, Double> sparse = vocabulary.createSparseVector(Arrays.asList("alpha", "beta", "missing"));
        assertEquals(sparse.size(), 2);
        assertEquals(sparse.get(0), Double.valueOf(1.0));
        assertEquals(sparse.get(1), Double.valueOf(1.0));
    }

    public void appendToVocabulary_rejectsNullFeatureOrValue() {
        assertThrows(IllegalArgumentException.class, () -> vocabulary.appendToVocabulary(null));
        assertThrows(IllegalArgumentException.class, () -> vocabulary.appendToVocabulary(new Feature(null)));
    }

    public void initializeMappingsAndSetValueList_rebuildIndexes() {
        vocabulary.setValueList(Arrays.asList("first", "second"));

        assertEquals(vocabulary.size(), 2);
        assertEquals(vocabulary.getIndex("second"), 1);
        assertTrue(vocabulary.contains("first"));

        vocabulary.addWord("third");
        assertEquals(vocabulary.getIndex("third"), 2);
    }

    public void getWordsAndGetFeatures_returnCopies() {
        vocabulary.addWord("one");
        vocabulary.addWord("two");

        List<String> words = vocabulary.getWords();
        words.add("three");
        assertEquals(vocabulary.size(), 2);

        List<Feature> features = vocabulary.getFeatures();
        assertEquals(features.size(), 2);
        assertEquals(features.get(0).getValue(), "one");
    }

    public void clearToStringEqualsAndHashCode_behaveAsExpected() {
        Vocabulary first = new Vocabulary("same");
        Vocabulary second = new Vocabulary("same");
        Vocabulary third = new Vocabulary("different");

        first.setValueList(Collections.singletonList("word"));
        assertTrue(first.toString().contains("same"));
        assertTrue(first.equals(second));
        assertFalse(first.equals(third));
        assertEquals(first.hashCode(), second.hashCode());

        first.clear();
        assertEquals(first.size(), 0);
        assertFalse(first.contains("word"));
    }
}
