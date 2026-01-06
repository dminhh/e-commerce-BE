//package com.prod.RAG.services.impl;
//
//import com.prod.RAG.model.Ask;
//import com.prod.RAG.services.IFlaskService;
//import com.prod.chains.Chain;
//import com.prod.chains.data.ChainData;
//import com.prod.chains.getProducts.*;
//import com.prod.facades.data.ProductInfo;
//import com.prod.models.products.Product;
//import com.prod.services.carts.IColorService;
//import com.prod.services.carts.IColorSizeProductService;
//import com.prod.services.carts.ISizeService;
//import com.prod.services.carts.ISmallQuantityService;
//import com.prod.services.details.ICategoryService;
//import com.prod.services.details.IDetailService;
//import com.prod.services.details.IQuantityService;
//import com.prod.services.details.ISeasonService;
//import com.prod.services.products.*;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//@Service
//@Slf4j
//public class FlaskService implements IFlaskService {
//    @Autowired
//    private ILabelService labelService;
//    @Autowired
//    private IProductService productService;
//    @Autowired
//    private ILabelProductService labelProductService;
//    @Autowired
//    private IImageService imageService;
//    @Autowired
//    private ISeasonService seasonService;
//    @Autowired
//    private ICategoryService categoryService;
//    @Autowired
//    private IColorService colorService;
//    @Autowired
//    private ISizeService sizeService;
//    @Autowired
//    private ISmallQuantityService smallQuantityService;
//    @Autowired
//    private IColorSizeProductService cspService;
//    @Autowired
//    private IDetailService detailService;
//    @Autowired
//    private IQuantityService quantityService;
//
//    private static final String FLASK_URL = "http://localhost:5000/query";
//
//    @Override
//    public Ask getAnswerFromFlask(String question) {
//        try {
//            // Tạo RestTemplate
//            RestTemplate restTemplate = new RestTemplate();
//
//            // Tạo request body
//            Map<String, String> requestBody = Map.of("question", question);
//
//            // Cấu hình headers
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
//
//            // Gửi POST request tới Flask
//            ResponseEntity<Map> response = restTemplate.exchange(
//                    FLASK_URL,
//                    HttpMethod.POST,
//                    entity,
//                    Map.class
//            );
//
//            // Xử lý kết quả trả về
//            if (response.getStatusCode() == HttpStatus.OK) {
//                Map<String, Object> responseBody = response.getBody();
//                List<ProductInfo> products = new ArrayList<>();
//                for (Integer id : (ArrayList<Integer>) responseBody.get("results")) {
//                    ChainData<ProductInfo> dto = getProductDTO(productService.getProductById(id).get());
//                    if (dto.isSuccess()) products.add(dto.getValue());
//                }
//                return Ask.builder()
//                        .answer((String) responseBody.get("answer"))
//                        .products(products)
//                        .build();
//
//            }
//
//            return Ask.builder().answer("Không nhận được câu trả lời từ Flask.").build();
//        } catch (Exception e) {
//            System.err.println("Lỗi khi gọi Flask API: " + e.getMessage());
//            return Ask.builder().answer("Lỗi khi kết nối tới Flask.").build();
//        }
//    }
//
//    private ChainData<ProductInfo> getProductDTO(Product product) {
//        ChainData<ProductInfo> chainData = setUpProduct(product);
//        Chain<ProductInfo> chain = new Chain<ProductInfo>()
//                .add(GetColorSizeQuantityByProdId.builder()
//                        .colorService(colorService)
//                        .sizeService(sizeService)
//                        .cspService(cspService)
//                        .smallQuantityService(smallQuantityService)
//                        .build())
//                .add(GetDetailByProdId.builder()
//                        .detailService(detailService)
//                        .quantityService(quantityService)
//                        .build())
//                .add(GetLabelByProdId.builder()
//                        .labelProductService(labelProductService)
//                        .labelService(labelService)
//                        .build())
//                .add(
//                        new GetImageByProdId(imageService)
//                )
//                .add(GetCateAndSeasonById.builder()
//                        .seasonService(seasonService)
//                        .categoryService(categoryService)
//                        .build())
//                .add(CreateSignature.builder()
//                        .productService(productService)
//                        .build());
//        chain.execute(chainData);
//        if (!chainData.isSuccess()) log.error(chainData.getMessage() + "/n" + chainData.getValue());
//        return chainData;
//    }
//
//    private ChainData<ProductInfo> setUpProduct(Product product) {
//        return ChainData.<ProductInfo>builder()
//                .value(ProductInfo.builder()
//                        .productId(product.getId())
//                        .title(product.getTitle())
//                        .price(product.getPrice())
//                        .score(product.getScore())
//                        .reviews(product.getReview())
//                        .description(product.getDescription())
//                        .build())
//                .build();
//    }
//}
