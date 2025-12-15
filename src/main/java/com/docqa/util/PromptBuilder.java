package com.docqa.util;

import com.docqa.model.ChatMessage;
import com.docqa.model.ChatSession;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PromptBuilder {

    /**
     * Build a prompt that includes conversation history and document context
     */
    public static String buildContextualPrompt(ChatSession session, String currentQuestion) {
        StringBuilder promptBuilder = new StringBuilder();

        // Document context
        promptBuilder.append("You are a helpful assistant analyzing the following document:\n\n");
        promptBuilder.append("---DOCUMENT START---\n");
        promptBuilder.append(session.getExtractedText());
        promptBuilder.append("\n---DOCUMENT END---\n\n");

        // Conversation history
        if (!session.getMessages().isEmpty() && session.getMessages().size() > 1) {
            promptBuilder.append("Previous conversation:\n");
            // Include last 5 exchanges to keep context window manageable
            int startIndex = Math.max(0, session.getMessages().size() - 10);
            for (int i = startIndex; i < session.getMessages().size() - 1; i++) {
                ChatMessage msg = session.getMessages().get(i);
                promptBuilder.append(msg.getRole().equals("user") ? "User: " : "Assistant: ");
                promptBuilder.append(msg.getContent()).append("\n");
            }
            promptBuilder.append("\n");
        }

        // Current question
        promptBuilder.append("Current question:\n");
        promptBuilder.append(currentQuestion).append("\n\n");
        promptBuilder.append("Please provide a detailed answer based on the document and conversation history.");

        return promptBuilder.toString();
    }

}
