package com.classgo.backend;

import com.classgo.backend.application.learning.LearningSupport;
import com.classgo.backend.shared.exception.BusinessRuleViolationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LearningSupportTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LearningSupport support = new LearningSupport(objectMapper);

    @Test
    void shouldNormalizeLegacyTextOrderingToPhraseOrdering() throws Exception {
        JsonNode questions = objectMapper.readTree("""
            [
              {
                "id": "q1",
                "type": "text_ordering",
                "prompt": "Order the sentence",
                "items": [
                  { "id": "i1", "text": "I" },
                  { "id": "i2", "text": "am" }
                ]
              }
            ]
            """);

        JsonNode normalized = support.normalizeAndValidateQuestions(questions);

        assertEquals("phrase_ordering", normalized.get(0).get("type").asText());
    }

    @Test
    void shouldAcceptImageOrderingWithImageUrls() throws Exception {
        JsonNode questions = objectMapper.readTree("""
            [
              {
                "id": "q1",
                "type": "image_ordering",
                "prompt": "Order the life cycle",
                "items": [
                  { "id": "i1", "imageUrl": "https://example.com/egg.png", "text": "Egg" },
                  { "id": "i2", "imageUrl": "https://example.com/larva.png", "text": "Larva" }
                ]
              }
            ]
            """);

        JsonNode normalized = support.normalizeAndValidateQuestions(questions);

        assertEquals("image_ordering", normalized.get(0).get("type").asText());
    }

    @Test
    void shouldRejectSingleTextOrderingWithoutTwoItems() throws Exception {
        JsonNode questions = objectMapper.readTree("""
            [
              {
                "id": "q1",
                "type": "single_text_ordering",
                "prompt": "Order the letters",
                "items": [
                  { "id": "i1", "text": "A" }
                ]
              }
            ]
            """);

        assertThrows(BusinessRuleViolationException.class, () -> support.normalizeAndValidateQuestions(questions));
    }
}
