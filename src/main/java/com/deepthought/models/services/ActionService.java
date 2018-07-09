package com.deepthought.models.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.deepthought.models.Action;
import com.deepthought.models.repository.ActionRepository;

@Service
public class ActionService {
	@Autowired
    private ActionRepository actionRepository;

    @Transactional(readOnly = true)
    public Action findByName(String name) {
        return actionRepository.findByKey(name);
    }

    @Transactional(readOnly = true)
    public Action findByKey(String title) {
        return actionRepository.findByKey(title);
    }
}