package com.prod.controllers;

import com.common.DTO.ResponseObject;
import com.common.controllers.ControllerUtil;
import com.prod.facades.IProductFacade;
import com.prod.facades.data.ProductInfo;
import com.prod.models.elk.ESProducts;
import com.prod.services.elk.IESProductService;
import com.prod.utils.ConvertESToProductDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/product-elk")
@AllArgsConstructor
@Slf4j
public class ESProductController {
    @Autowired
    private IESProductService iesProductService;
    @Autowired
    private IProductFacade productFacade;
    @Autowired
    private ConvertESToProductDTO esToProductDTO;

    @GetMapping("/sorted")
    public ResponseEntity<Object> getSortedProducts(@RequestParam("page") int page, @RequestParam("size") int size) {
        return ControllerUtil.ok(iesProductService.getSortedProducts(page, size));
//        return elkProductService.getSortedProducts(page, size);
    }

    @GetMapping("/insert")
    public boolean insert() {
        try {
            ResponseObject<Page<ProductInfo>> products = productFacade.getProducts(1, 10000, 0, 0, new ArrayList<>(), "id", "incr");
            Page<ProductInfo> pro = products.getData();
            List<ESProducts> elkProducts = new ArrayList<>();
            for (ProductInfo x : pro.getContent()) {
                ESProducts sampleProduct = esToProductDTO.getES(x);
                sampleProduct.setScore(1000);
                sampleProduct.setCreateAt(new Date());
                sampleProduct.setUpdateAt(new Date());
                elkProducts.add(sampleProduct);
            }
            return iesProductService.createListProduct(elkProducts);
        } catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
    }
    @GetMapping("/find")
    public ResponseEntity<ResponseObject<Page<ProductInfo>>> findProduct(
            @RequestParam(required = false, defaultValue = "") String key,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ESProducts> esResult = iesProductService.searchProducts(key, page, size);

            // --- THÊM LOG Ở ĐÂY ---
            // Kiểm tra nếu kết quả không rỗng (có sản phẩm)
            if (esResult.hasContent()) {
                log.info("Found {} products for keyword '{}'", esResult.getTotalElements(), key);

                // Nếu bạn muốn log chi tiết ID hoặc tên từng sản phẩm tìm được:
                // esResult.forEach(p -> log.info("Product found: ID={}, Name={}", p.getId(), p.getTitle()));
            } else {
                log.info("No products found for keyword '{}'", key);
            }
            // ----------------------

            Page<ProductInfo> productInfoPage = esResult.map(esToProductDTO::toProductInfo);

            return ResponseEntity.ok(new ResponseObject<>(true, productInfoPage, "Found products successfully"));

        } catch (Exception e) {
            log.error("Search Error: ", e);
            return ResponseEntity.status(500).body(new ResponseObject<>(false, null, "Internal Server Error"));
        }
    }
}
