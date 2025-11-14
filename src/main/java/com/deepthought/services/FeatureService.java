package com.deepthought.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deepthought.data.models.Feature;
import com.deepthought.data.repository.FeatureRepository;

@Service
public class FeatureService {
	@Autowired
    private FeatureRepository featureRepository;

    @Transactional(readOnly = true)
    public Feature findByValue(String value) {
        return featureRepository.findByValue(value);
    }
}