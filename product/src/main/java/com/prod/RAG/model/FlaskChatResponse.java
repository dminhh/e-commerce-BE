package com.prod.RAG.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlaskChatResponse {

    private Boolean success;

    private FlaskChatData data;

    private String error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlaskChatData {

        private String question;

        private String answer;

        @JsonProperty("is_confident")
        private Boolean isConfident;

        @JsonProperty("search_results")
        private List<FlaskSearchResult> searchResults;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlaskSearchResult {

        private String content;

        private Double score;

        private Integer index;
    }
}
