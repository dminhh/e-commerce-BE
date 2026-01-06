//package com.prod.RAG.controllers;
//
//import com.common.DTO.ResponseObject;
//import com.prod.RAG.model.Ask;
//import com.prod.RAG.services.IFlaskService;
//import com.prod.facades.data.ProductInfo;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/ques")
//public class QuestionController {
//    @Autowired
//    private IFlaskService flaskService;
//
//    @GetMapping("/ask")
//    public ResponseEntity<ResponseObject<List<ProductInfo>>> handleQuestion(@RequestParam String question) {
//        if (question == null || question.isEmpty()) {
//            return ResponseEntity.badRequest().body(
//                    ResponseObject.<List<ProductInfo>>builder()
//                            .message("Câu hỏi không được để trống.")
//                            .build()
//            );
//        }
//
//        // Gửi câu hỏi tới Flask
//        Ask answer = flaskService.getAnswerFromFlask(question);
//        if (answer.getProducts() == null) {
//            return ResponseEntity.badRequest().body(
//                    ResponseObject.<List<ProductInfo>>builder()
//                            .message("Không tìm thấy sản phẩm phù hợp")
//                            .build()
//            );
//        } else {
//            return ResponseEntity.ok().body(
//                    ResponseObject.<List<ProductInfo>>builder()
//                            .data(answer.getProducts())
//                            .message(answer.getAnswer())
//                            .isSuccess(true)
//                            .build()
//            );
//        }
//    }
//}
