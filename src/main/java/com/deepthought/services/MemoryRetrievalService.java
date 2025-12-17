package com.deepthought.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.deepthought.api.v2.dto.MemoryRetrievalRequest;
import com.deepthought.api.v2.dto.MemoryRetrievalResponse;
import com.deepthought.data.db.DataDecomposer;
import com.deepthought.data.models.Feature;
import com.deepthought.data.models.MemoryRecord;
import com.deepthought.data.repository.MemoryRecordRepository;

import lombok.Getter;
import lombok.Setter;

/**
 * Service that encapsulates memory retrieval logic used by the SignalsController.
 * 
 * Preconditions:
 * - A {@link MemoryRecordRepository} bean must be available and injected.
 * - Callers provide non-null service instances managed by the Spring container.
 * 
 * Postconditions:
 * - The service does not mutate persistent state beyond reading {@link MemoryRecord} entities.
 * - Returned responses clearly indicate success or validation errors through the {@code success} flag and message.
 */
@Service
@Getter
@Setter
public class MemoryRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(MemoryRetrievalService.class);

    @Autowired
    private MemoryRecordRepository memoryRecordRepository;

    /**
     * Retrieves memories that fall within the supplied time range and optionally match a prompt.
     * 
     * Preconditions:
     * - The request parameter is non-null.
     * - request.getFrom() and request.getTo() represent inclusive time bounds when both are non-null.
     * 
     * Postconditions:
     * - If from or to is null, the returned response has success set to false and an explanatory message.
     * - If from is after to, the returned response has success set to false and an explanatory message.
     * - On success, the returned response contains a list of memory items limited by the request limit or a default of 50.
     * - No new {@link MemoryRecord} entities are created or modified by this method.
     * 
     * @param request memory retrieval parameters, including prompt, time range and optional result limit
     * @return a {@link MemoryRetrievalResponse} describing either an error or the retrieved memory items
     */
    public MemoryRetrievalResponse retrieveMemories(MemoryRetrievalRequest request) {
        Date from = request.getFrom();
        Date to = request.getTo();

        if(from == null || to == null) {
            return MemoryRetrievalResponse.error("Both 'from' and 'to' times are required");
        }

        if(from.after(to)) {
            return MemoryRetrievalResponse.error("'from' must be before 'to'");
        }

        // 1. Fetch memories in the given time window
        List<MemoryRecord> candidates = new ArrayList<>();
        memoryRecordRepository.findAll().forEach(candidates::add);

        List<MemoryRecord> timeFiltered = new ArrayList<>();
        for(MemoryRecord record : candidates) {
            Date d = record.getDate();
            if(d != null && !d.before(from) && !d.after(to)) {
                timeFiltered.add(record);
            }
        }

        // 2. If a prompt is provided, use it to score basic relevance
        String prompt = request.getPrompt();
        List<String> promptTokens = new ArrayList<>();
        if(prompt != null && !prompt.isEmpty()) {
            try {
                for(Feature f : DataDecomposer.decompose(prompt)) {
                    if(f.getValue() != null) {
                        promptTokens.add(f.getValue());
                    }
                }
            } catch(Exception e) {
                // If prompt cannot be decomposed, fall back to time-based filtering only
                log.error("Error decomposing prompt for memory retrieval", e);
            }
        }

        List<MemoryRetrievalResponse.MemoryItem> items = new ArrayList<>();
        for(MemoryRecord record : timeFiltered) {
            double relevance = 0.0;

            if(!promptTokens.isEmpty() && record.getInputFeatureValues() != null) {
                for(String token : promptTokens) {
                    if(record.getInputFeatureValues().contains(token)) {
                        relevance += 1.0;
                    }
                }
            }

            MemoryRetrievalResponse.MemoryItem item = new MemoryRetrievalResponse.MemoryItem();
            item.setId(record.getID());
            item.setTimestamp(record.getDate());
            item.setInputFeatures(record.getInputFeatureValues());
            item.setPredictedValue(
                record.getPredictedFeature() != null ? record.getPredictedFeature().getValue() : null
            );
            item.setRelevanceScore(relevance);

            items.add(item);
        }

        // 3. Optionally limit number of results
        int limit = request.getLimit() != null ? request.getLimit() : 50;
        if(items.size() > limit) {
            items = items.subList(0, limit);
        }

        return MemoryRetrievalResponse.success(items);
    }
}
