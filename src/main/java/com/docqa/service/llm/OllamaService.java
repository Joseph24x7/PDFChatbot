package com.docqa.service.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Service
@Slf4j
public class OllamaService {

    private final OllamaChatModel ollama;

    public OllamaService(OllamaChatModel ollama) {
        this.ollama = ollama;
    }

    public String generateText(String prompt) {
        try {
            log.info("Generating text from Ollama model with prompt length: {}", prompt.length());
            return ollama.call(prompt);
        } catch (Exception e) {
            log.error("Error generating text from Ollama model", e);
            throw new RuntimeException("Failed to generate text from Ollama model", e);
        }
    }

    public void streamText(String prompt, Consumer<String> onToken, Consumer<String> onComplete, Consumer<Throwable> onError) {
        try {
            log.info("Starting streaming text generation from Ollama model with prompt length: {}", prompt.length());

            Prompt chatPrompt = new Prompt(new UserMessage(prompt));
            Flux<ChatResponse> responseFlux = ollama.stream(chatPrompt);

            StringBuilder fullResponse = new StringBuilder();

            responseFlux.subscribe(
                chatResponse -> {
                    String chunk = chatResponse.getResult().getOutput().getText();
                    if (chunk != null && !chunk.isEmpty()) {
                        fullResponse.append(chunk);
                        onToken.accept(chunk);
                    }
                },
                error -> {
                    log.error("Error during streaming text generation", error);
                    onError.accept(error);
                },
                () -> {
                    String completeText = fullResponse.toString();
                    log.info("Streaming completed. Total response length: {} characters", completeText.length());
                    onComplete.accept(completeText);
                }
            );

        } catch (Exception e) {
            log.error("Error initializing streaming from Ollama model", e);
            onError.accept(e);
        }
    }
}

