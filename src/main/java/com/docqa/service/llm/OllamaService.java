package com.docqa.service.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OllamaService {

    private final OllamaChatModel ollama;

    public String generateText(String prompt) {
        try {
            log.info("Generating text from Ollama model with prompt length: {}", prompt.length());
            return ollama.call(prompt);
        } catch (Exception e) {
            log.error("Error generating text from Ollama model", e);
            throw new RuntimeException("Failed to generate text from Ollama model", e);
        }
    }
}

