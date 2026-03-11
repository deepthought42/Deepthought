package com.deepthought.models.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.deepthought.models.Feature;
import com.deepthought.models.Vocabulary;
import com.deepthought.models.edges.FeatureWeight;
import com.deepthought.models.repository.FeatureRepository;
import com.deepthought.models.repository.VocabularyRepository;

@RunWith(MockitoJUnitRunner.class)
public class VocabularyServiceTests {

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private FeatureRepository featureRepository;

    @InjectMocks
    private VocabularyService vocabularyService;

    @Before
    public void setup() {
        when(vocabularyRepository.save(any(Vocabulary.class))).thenAnswer(invocation -> {
            Vocabulary vocabulary = invocation.getArgument(0);
            if (vocabulary.getId() == null) {
                vocabulary.setId(100L);
            }
            return vocabulary;
        });
    }

    @Test
    public void calculateVocabularySimilarityUsesJaccardOverlap() {
        Vocabulary first = new Vocabulary("first");
        first.addWord("alpha");
        first.addWord("beta");

        Vocabulary second = new Vocabulary("second");
        second.addWord("beta");
        second.addWord("gamma");

        double similarity = vocabularyService.calculateVocabularySimilarity(first, second);

        assertEquals(1.0d / 3.0d, similarity, 0.0001d);
    }

    @Test
    public void clusterStronglyRelatedFeaturesBuildsConnectedComponents() {
        Feature alpha = new Feature("alpha");
        Feature beta = new Feature("beta");
        Feature gamma = new Feature("gamma");

        FeatureWeight alphaToBeta = new FeatureWeight();
        alphaToBeta.setEndFeature(beta);
        alphaToBeta.setWeight(0.95d);
        alpha.getFeatureWeights().add(alphaToBeta);

        FeatureWeight betaToGamma = new FeatureWeight();
        betaToGamma.setEndFeature(gamma);
        betaToGamma.setWeight(0.25d);
        beta.getFeatureWeights().add(betaToGamma);

        when(featureRepository.findByValue("alpha")).thenReturn(alpha);
        when(featureRepository.findByValue("beta")).thenReturn(beta);
        when(featureRepository.findByValue("gamma")).thenReturn(gamma);

        List<List<Feature>> clusters = vocabularyService.clusterStronglyRelatedFeatures(
                Arrays.asList(alpha, beta, gamma),
                0.8d);

        assertEquals(2, clusters.size());
        assertEquals(2, clusters.get(0).size());
        assertTrue(clusters.get(0).stream().anyMatch(f -> "alpha".equals(f.getValue())));
        assertTrue(clusters.get(0).stream().anyMatch(f -> "beta".equals(f.getValue())));
    }

    @Test
    public void learnFromTrainingFeaturesCreatesAggregateForSimilarVocabulary() {
        Feature alpha = new Feature("alpha");
        Feature beta = new Feature("beta");

        when(vocabularyRepository.findByLabel("test_label")).thenReturn(Optional.empty());

        Vocabulary similar = new Vocabulary("existing_group");
        similar.setId(23L);
        similar.setValueList(Arrays.asList("alpha", "beta", "theta"));

        when(vocabularyRepository.findSimilarVocabularies(eq(100L), eq(1)))
                .thenReturn(Collections.singletonList(similar));

        Vocabulary learned = vocabularyService.learnFromTrainingFeatures(Arrays.asList(alpha, beta), "Test Label");

        assertNotNull(learned);

        verify(vocabularyRepository, times(2)).save(any(Vocabulary.class));
        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        verify(vocabularyRepository).attachChildVocabularies(eq(100L), captor.capture());
        List<Long> childIds = captor.getValue();
        assertTrue(childIds.contains(100L));
        assertTrue(childIds.contains(23L));
    }
}
