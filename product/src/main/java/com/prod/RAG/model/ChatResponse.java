package com.prod.RAG.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private Boolean success;

    private ChatData data;

    private String error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatData {

        private String question;

        private String answer;

        private Boolean isConfident;

        private List<SearchResult> searchResults;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResult {

        private String content;

        private Double score;

        private Integer index;
    }
}
