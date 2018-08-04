package com.deepthought.models.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deepthought.models.Feature;
import com.deepthought.models.repository.FeatureRepository;

@Service
public class FeatureService {
	@Autowired
    private FeatureRepository featureRepository;

    @Transactional(readOnly = true)
    public Feature findByValue(String value) {
        return featureRepository.findByValue(value);
    }
}