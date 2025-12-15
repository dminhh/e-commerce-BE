package com.prod.RAG.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "Câu hỏi không được để trống")
    @Size(min = 1, max = 500, message = "Câu hỏi phải từ 1 đến 500 ký tự")
    private String question;

    @Builder.Default
    private Boolean debug = false;
}
