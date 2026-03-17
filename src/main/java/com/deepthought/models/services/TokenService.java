package com.deepthought.models.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.deepthought.models.Token;
import com.deepthought.models.repository.TokenRepository;

@Service
public class TokenService {
	@Autowired
    private TokenRepository tokenRepository;

    @Transactional(readOnly = true)
    public Token findByValue(String value) {
        return tokenRepository.findByValue(value);
    }
}
