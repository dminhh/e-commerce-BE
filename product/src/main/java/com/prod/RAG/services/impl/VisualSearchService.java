package com.prod.RAG.services.impl;

import com.prod.RAG.model.VisualSearchResult;
import com.prod.RAG.services.IVisualSearchService;
import com.prod.chains.Chain;
import com.prod.chains.data.ChainData;
import com.prod.chains.getProducts.*;
import com.prod.facades.data.ProductInfo;
import com.prod.models.products.Product;
import com.prod.services.carts.IColorService;
import com.prod.services.carts.IColorSizeProductService;
import com.prod.services.carts.ISizeService;
import com.prod.services.carts.ISmallQuantityService;
import com.prod.services.details.ICategoryService;
import com.prod.services.details.IDetailService;
import com.prod.services.details.IQuantityService;
import com.prod.services.details.ISeasonService;
import com.prod.services.products.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@Slf4j
public class VisualSearchService implements IVisualSearchService {

    @Autowired
    private ILabelService labelService;
    @Autowired
    private IProductService productService;
    @Autowired
    private ILabelProductService labelProductService;
    @Autowired
    private IImageService imageService;
    @Autowired
    private ISeasonService seasonService;
    @Autowired
    private ICategoryService categoryService;
    @Autowired
    private IColorService colorService;
    @Autowired
    private ISizeService sizeService;
    @Autowired
    private ISmallQuantityService smallQuantityService;
    @Autowired
    private IColorSizeProductService cspService;
    @Autowired
    private IDetailService detailService;
    @Autowired
    private IQuantityService quantityService;

    private static final String FLASK_URL = "http://localhost:5001/visual_search";

    @Override
    public VisualSearchResult searchByImage(MultipartFile image, int topK) {
        try {
            // Convert image to Base64
            byte[] imageBytes = image.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            log.info("Đã convert image sang Base64: originalSize={}KB, base64Length={}KB, preview={}...",
                    imageBytes.length / 1024,
                    base64Image.length() / 1024,
                    base64Image.substring(0, Math.min(50, base64Image.length())));

            // Create RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image_base64", base64Image);
            requestBody.put("top_k", topK);

            // Configure headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Gửi request visual search tới Flask với top_k={}", topK);

            // Send POST request to Flask
            ResponseEntity<Map> response = restTemplate.exchange(
                    FLASK_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // Process response
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();

                Boolean success = (Boolean) responseBody.get("success");

                if (success == null || !success) {
                    String error = (String) responseBody.get("error");
                    log.warn("Flask không tìm thấy sản phẩm: {}", error);
                    return VisualSearchResult.builder()
                            .message(error != null ? error : "Không tìm thấy sản phẩm phù hợp")
                            .build();
                }

                // Get results
                List<Map<String, Object>> results =
                        (List<Map<String, Object>>) responseBody.get("results");
                Integer totalResults = (Integer) responseBody.get("total_results");

                if (results == null || results.isEmpty()) {
                    return VisualSearchResult.builder()
                            .message("Không tìm thấy sản phẩm phù hợp")
                            .build();
                }

                // Enrich product data
                List<ProductInfo> products = new ArrayList<>();
                for (Map<String, Object> result : results) {
                    Integer productId = (Integer) result.get("product_id");

                    Optional<Product> productOpt = productService.getProductById(productId);
                    if (productOpt.isPresent()) {
                        ChainData<ProductInfo> dto = getProductDTO(productOpt.get());
                        if (dto.isSuccess()) {
                            products.add(dto.getValue());
                        }
                    } else {
                        log.warn("Không tìm thấy product với id={}", productId);
                    }
                }

                String message = String.format("Tìm thấy %d sản phẩm tương tự", products.size());

                return VisualSearchResult.builder()
                        .message(message)
                        .products(products)
                        .build();
            }

            return VisualSearchResult.builder()
                    .message("Không nhận được kết quả từ Flask.")
                    .build();

        } catch (Exception e) {
            log.error("Lỗi khi gọi Flask Visual Search API: " + e.getMessage(), e);
            return VisualSearchResult.builder()
                    .message("Lỗi khi kết nối tới Flask: " + e.getMessage())
                    .build();
        }
    }

    private ChainData<ProductInfo> getProductDTO(Product product) {
        ChainData<ProductInfo> chainData = setUpProduct(product);
        Chain<ProductInfo> chain = new Chain<ProductInfo>()
                .add(GetColorSizeQuantityByProdId.builder()
                        .colorService(colorService)
                        .sizeService(sizeService)
                        .cspService(cspService)
                        .smallQuantityService(smallQuantityService)
                        .build())
                .add(GetDetailByProdId.builder()
                        .detailService(detailService)
                        .quantityService(quantityService)
                        .build())
                .add(GetLabelByProdId.builder()
                        .labelProductService(labelProductService)
                        .labelService(labelService)
                        .build())
                .add(
                        new GetImageByProdId(imageService)
                )
                .add(GetCateAndSeasonById.builder()
                        .seasonService(seasonService)
                        .categoryService(categoryService)
                        .build())
                .add(CreateSignature.builder()
                        .productService(productService)
                        .build());
        chain.execute(chainData);
        if (!chainData.isSuccess()) log.error(chainData.getMessage() + "\n" + chainData.getValue());
        return chainData;
    }

    private ChainData<ProductInfo> setUpProduct(Product product) {
        return ChainData.<ProductInfo>builder()
                .value(ProductInfo.builder()
                        .productId(product.getId())
                        .title(product.getTitle())
                        .price(product.getPrice())
                        .score(product.getScore())
                        .reviews(product.getReview())
                        .description(product.getDescription())
                        .build())
                .build();
    }
}
