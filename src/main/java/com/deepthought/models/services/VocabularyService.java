package com.deepthought.models.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deepthought.models.Feature;
import com.deepthought.models.Vocabulary;
import com.deepthought.models.edges.FeatureWeight;
import com.deepthought.models.repository.FeatureRepository;
import com.deepthought.models.repository.VocabularyRepository;

/**
 * Service for creating and relating vocabulary groups from learned feature data.
 */
@Service
public class VocabularyService {

    private static final double DEFAULT_STRONG_WEIGHT_THRESHOLD = 0.8d;
    private static final double DEFAULT_VOCAB_SIMILARITY_THRESHOLD = 0.6d;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private FeatureRepository featureRepository;

    @Transactional
    public Vocabulary learnFromTrainingFeatures(List<Feature> features, String labelHint) {
        if (features == null || features.isEmpty()) {
            return null;
        }

        String normalizedLabel = normalizeLabel(labelHint);
        Vocabulary baseVocabulary = upsertVocabulary(normalizedLabel, features);

        List<List<Feature>> clusters = clusterStronglyRelatedFeatures(features, DEFAULT_STRONG_WEIGHT_THRESHOLD);
        for (int idx = 0; idx < clusters.size(); idx++) {
            List<Feature> cluster = clusters.get(idx);
            if (cluster.size() <= 1) {
                continue;
            }
            String clusterLabel = normalizedLabel + "_cluster_" + idx;
            upsertVocabulary(clusterLabel, cluster);
        }

        createAggregateVocabularyIfSimilar(baseVocabulary, DEFAULT_VOCAB_SIMILARITY_THRESHOLD);
        return baseVocabulary;
    }

    Vocabulary upsertVocabulary(String label, List<Feature> features) {
        Vocabulary vocabulary = vocabularyRepository.findByLabel(label)
                .orElseGet(() -> new Vocabulary(label));

        for (Feature feature : features) {
            if (feature != null && feature.getValue() != null && !feature.getValue().trim().isEmpty()) {
                vocabulary.addWord(feature.getValue());
            }
        }
        return vocabularyRepository.save(vocabulary);
    }

    List<List<Feature>> clusterStronglyRelatedFeatures(List<Feature> features, double threshold) {
        if (features == null || features.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<Feature>> clusters = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for (Feature start : features) {
            if (start == null || start.getValue() == null || visited.contains(start.getValue())) {
                continue;
            }

            List<Feature> cluster = new ArrayList<>();
            List<Feature> queue = new ArrayList<>();
            queue.add(start);
            visited.add(start.getValue());

            while (!queue.isEmpty()) {
                Feature current = queue.remove(0);
                cluster.add(current);

                Feature persistedFeature = featureRepository.findByValue(current.getValue());
                if (persistedFeature == null || persistedFeature.getFeatureWeights() == null) {
                    continue;
                }

                for (FeatureWeight weight : persistedFeature.getFeatureWeights()) {
                    if (weight == null || weight.getEndFeature() == null || weight.getWeight() < threshold) {
                        continue;
                    }

                    Feature endFeature = weight.getEndFeature();
                    if (endFeature.getValue() == null || visited.contains(endFeature.getValue())) {
                        continue;
                    }

                    boolean belongsToInput = features.stream()
                            .anyMatch(f -> f != null && endFeature.getValue().equals(f.getValue()));
                    if (belongsToInput) {
                        visited.add(endFeature.getValue());
                        queue.add(endFeature);
                    }
                }
            }

            if (!cluster.isEmpty()) {
                clusters.add(cluster);
            }
        }

        return clusters;
    }

    void createAggregateVocabularyIfSimilar(Vocabulary sourceVocabulary, double similarityThreshold) {
        if (sourceVocabulary == null || sourceVocabulary.getId() == null) {
            return;
        }

        List<Vocabulary> similar = vocabularyRepository.findSimilarVocabularies(sourceVocabulary.getId(), 1);
        List<Vocabulary> matching = similar.stream()
                .filter(v -> calculateVocabularySimilarity(sourceVocabulary, v) >= similarityThreshold)
                .collect(Collectors.toList());

        if (matching.isEmpty()) {
            return;
        }

        String aggregateLabel = "aggregate_" + sourceVocabulary.getLabel() + "_" + System.currentTimeMillis();
        Vocabulary aggregate = new Vocabulary(aggregateLabel);

        for (String word : sourceVocabulary.getValueList()) {
            aggregate.addWord(word);
        }
        for (Vocabulary vocabulary : matching) {
            for (String word : vocabulary.getValueList()) {
                aggregate.addWord(word);
            }
        }

        Vocabulary savedAggregate = vocabularyRepository.save(aggregate);

        List<Long> childIds = new ArrayList<>();
        childIds.add(sourceVocabulary.getId());
        childIds.addAll(matching.stream().map(Vocabulary::getId).collect(Collectors.toList()));
        vocabularyRepository.attachChildVocabularies(savedAggregate.getId(), childIds);
    }

    double calculateVocabularySimilarity(Vocabulary first, Vocabulary second) {
        if (first == null || second == null) {
            return 0.0d;
        }

        Set<String> firstWords = new HashSet<>(first.getValueList());
        Set<String> secondWords = new HashSet<>(second.getValueList());

        if (firstWords.isEmpty() && secondWords.isEmpty()) {
            return 1.0d;
        }

        Set<String> intersection = new HashSet<>(firstWords);
        intersection.retainAll(secondWords);

        Set<String> union = new HashSet<>(firstWords);
        union.addAll(secondWords);

        if (union.isEmpty()) {
            return 0.0d;
        }

        return (double) intersection.size() / (double) union.size();
    }

    private String normalizeLabel(String labelHint) {
        if (labelHint == null || labelHint.trim().isEmpty()) {
            return "vocabulary_auto";
        }

        return labelHint.trim().toLowerCase().replaceAll("\\s+", "_");
    }
}
