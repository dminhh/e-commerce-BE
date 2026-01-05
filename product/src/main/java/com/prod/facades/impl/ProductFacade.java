package com.prod.facades.impl;


import com.common.DTO.ResponseObject;
import com.prod.chains.Chain;
import com.prod.chains.createProduct.*;
import com.prod.chains.data.ChainData;
import com.prod.chains.getProducts.*;
import com.prod.facades.IProductFacade;
import com.prod.facades.data.ProductInfo;
import com.prod.facades.flaskAPIs.UpdateFAISSIndex;
import com.prod.models.details.Detail;
import com.prod.models.elk.ESProducts;
import com.prod.models.products.*;
import com.prod.services.carts.*;
import com.prod.services.details.*;
import com.prod.services.elk.IESProductService;
import com.prod.services.products.*;
import com.prod.utils.ConvertESToProductDTO;
import com.prod.utils.ConvertListPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Slf4j
public class ProductFacade implements IProductFacade {
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
    @Autowired
    private ConvertListPage<ProductInfo> convertListPage;
    @Autowired
    private UpdateFAISSIndex updateFAISSIndex;
    @Autowired
    private IESProductService esService;
    @Autowired
    private ConvertESToProductDTO esToProductDTO;

    @Override
    public ResponseObject<ProductInfo> createProduct(ProductInfo product) {
        try {
            ChainData<ProductInfo> dataDTO = ChainData.<ProductInfo>builder()
                    .value(product)
                    .build();
            Chain<ProductInfo> chain = new Chain<ProductInfo>()
                    .add(new GetSeasonById(seasonService))
                    .add(new GetCategoryById(categoryService))
                    .add(CreateProduct.builder()
                            .productService(productService)
                            .detailService(detailService)
                            .quantityService(quantityService)
                            .build())
                    .add(GetLabelByName.builder()
                            .labelService(labelService)
                            .labelProductService(labelProductService)
                            .build())
                    .add(new CreateImages(imageService))
                    .add(CreateCSPs.builder()
                            .colorService(colorService)
                            .sizeService(sizeService)
                            .smallQuantityService(smallQuantityService)
                            .colorSizeProductService(cspService)
                            .quantityService(quantityService)
                            .detailService(detailService)
                            .productService(productService)
                            .build())
                    .add(CreateSignature.builder()
                            .productService(productService)
                            .build());
            chain.execute(dataDTO);
            if (dataDTO.isSuccess()) {
                try {
                    ESProducts esProducts = esToProductDTO.getES(dataDTO.getValue());
                    esProducts.setScore(1000);
                    esProducts.setCreateAt(new Date());
                    esProducts.setUpdateAt(new Date());
                    esService.createProduct(esProducts);
                } catch (Exception e){
                    log.error(e.getMessage());
                }
                try {
                    updateFAISSIndex.updateIndex();
                } catch (Exception e){
                    log.error("Lỗi khi cập nhật product index: " + e.getMessage());
                }
                return ResponseObject.<ProductInfo>builder()
                        .data(product)
                        .isSuccess(true)
                        .message("Tao san pham thanh cong")
                        .build();
            } else return ResponseObject.<ProductInfo>builder()
                    .message("Khong the tao moi san pham")
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseObject.<ProductInfo>builder()
                    .message("Gap loi server")
                    .build();
        }
    }


    @Override
    public ResponseObject<ProductInfo> updateProduct(ProductInfo productInfo) {
        try {
            ChainData<ProductInfo> dataDTO = ChainData.<ProductInfo>builder()
                    .value(productInfo)
                    .build();
            Chain<ProductInfo> chain = new Chain<ProductInfo>()
                    .add(new GetSeasonById(seasonService))
                    .add(new GetCategoryById(categoryService))
                    .add(UpdateProduct.builder()
                            .productService(productService)
                            .detailService(detailService)
                            .quantityService(quantityService)
                            .build())
                    .add(GetLabelByName.builder()
                            .labelService(labelService)
                            .labelProductService(labelProductService)
                            .build())
                    .add(new CreateImages(imageService))
                    .add(CreateCSPs.builder()
                            .colorService(colorService)
                            .sizeService(sizeService)
                            .smallQuantityService(smallQuantityService)
                            .colorSizeProductService(cspService)
                            .detailService(detailService)
                            .quantityService(quantityService)
                            .productService(productService)
                            .build())
                    .add(CreateSignature.builder()
                            .productService(productService)
                            .build());
            chain.execute(dataDTO);
            if (dataDTO.isSuccess()) {
                try {
                    esService.updateProduct(productInfo.getProductId() + "", esToProductDTO.getES(dataDTO.getValue()));
                } catch (Exception e){
                    log.error(e.getMessage());
                }
                try {
                    updateFAISSIndex.updateIndex();
                } catch (Exception e){
                    log.error("Lỗi khi cập nhật product index: " + e.getMessage());
                }
                return ResponseObject.<ProductInfo>builder()
                        .data(productInfo)
                        .isSuccess(true)
                        .message("Cap nhat san pham thanh cong")
                        .build();
            } else {
                log.error(dataDTO.getMessage());
                return ResponseObject.<ProductInfo>builder()
                        .message("Khong the tao moi san pham")
                        .build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseObject.<ProductInfo>builder()
                    .message("Gap loi server")
                    .build();
        }
    }


    @Override
    public ResponseObject<Page<ProductInfo>> getProducts(int page, int size, int category, int season, List<Integer> label, String sortField, String sortDirection) {
        Page<Detail> details = detailService.getPageDetailsByPageWithMultiCondition
                (page, size, category, season, label, sortDirection);
        long total = details.getTotalElements();
        List<Product> products = new ArrayList<>();
        for (Detail detail : details) {
            productService.getProductById(detail.getProduct_id()).ifPresent(products::add);
        }
        List<ProductInfo> productInfos = new ArrayList<>();
        for (Product product : products) {
            ChainData<ProductInfo> chainData = getProductDTO(product);
            productInfos.add(chainData.getValue());
        }
        if (productInfos.isEmpty())
            return emptyProduct();
        else {
            sort(sortField, sortDirection, productInfos);
            Page<ProductInfo> res = new PageImpl<>(productInfos, PageRequest.of(page, size), total);
            return successP(res);
        }
    }


    private void sort(String sortField, String sortDirection, List<ProductInfo> productInfos) {
        Comparator<ProductInfo> comparator;


        switch (sortField) {
            case "price" -> comparator = Comparator.comparing(ProductInfo::getPrice);
            case "title" -> comparator = Comparator.comparing(ProductInfo::getTitle);
            case "score" -> comparator = Comparator.comparing(ProductInfo::getScore);
            case "id" -> comparator = Comparator.comparing(ProductInfo::getProductId);
            default -> throw new IllegalArgumentException("Invalid sort field: " + sortField);
        }


        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }


        productInfos.sort(comparator);
    }


    @Override
    public ResponseObject<Page<ProductInfo>> getProductsByLabelId(int id, int page, int size, String sortField, String sortDirect) {
        List<Label_Product> list = labelProductService.getPageLabelProductsByLabelId(id, page, size, "id", sortDirect).getContent();
        Optional<Label> _label = labelService.getLabelById(id);
        if (!list.isEmpty() && _label.isPresent()) {
            List<ProductInfo> products = new ArrayList<>();
            list.forEach(product -> {
                products.add(ProductInfo.builder()
                        .productId(product.getProduct_id())
                        .label(List.of(_label.get().getName()))
                        .build());
            });
            List<ProductInfo> res = new ArrayList<>();
            for (ProductInfo productInfo : products) {
                ChainData<ProductInfo> chainData = ChainData.<ProductInfo>builder()
                        .value(productInfo)
                        .build();
                Chain<ProductInfo> chain = getProductDTOByDetail();
                chain.execute(chainData);
                res.add(chainData.getValue());
            }
            if (res.isEmpty()) {
                return emptyProduct();
            } else {
                Page<ProductInfo> done = convertListPage.listToPage(res, page, size);
                return successP(done);
            }
        } else return serverError();
    }


    @Override
    public ResponseObject<Page<ProductInfo>> getProductsBySeasonId(int id, int page, int size, String sortField, String sortDirect) {
        List<Detail> details = detailService.getPageDetailsBySeasonId(id, page, size, "id", sortDirect).getContent();
        if (!details.isEmpty()) {
            List<ProductInfo> products = new ArrayList<>();
            details.forEach(detail -> {
                products.add(ProductInfo.builder()
                        .season_id(id)
                        .productId(detail.getProduct_id())
                        .category(detail.getCategory_id())
                        .build());
            });
            List<ProductInfo> res = new ArrayList<>();
            for (ProductInfo productInfo : products) {
                ChainData<ProductInfo> chainData = ChainData.<ProductInfo>builder()
                        .value(productInfo)
                        .build();
                Chain<ProductInfo> chain = getProductDTOByDetail();
                chain.add(GetLabelByProdId.builder()
                        .labelService(labelService)
                        .labelProductService(labelProductService)
                        .build());
                chain.execute(chainData);
                res.add(chainData.getValue());
            }
            if (res.isEmpty()) {
                return emptyProduct();
            } else {
                Page<ProductInfo> done = convertListPage.listToPage(res, page, size);
                return successP(done);
            }
        } else return serverError();
    }


    @Override
    public ResponseObject<ProductInfo> getProductById(int id) {
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            ChainData<ProductInfo> chainData = getProductDTO(product.get());
            if (chainData.isSuccess()) {
                return ResponseObject.<ProductInfo>builder()
                        .message("Tim san pham thanh cong")
                        .isSuccess(true)
                        .data(chainData.getValue())
                        .build();
            } else {
                log.error(chainData.getMessage());
                return ResponseObject.<ProductInfo>builder()
                        .message("Khong the lay thong tin san pham")
                        .build();
            }
        } else
            return ResponseObject.<ProductInfo>builder()
                    .message("Gap loi khi tim san pham")
                    .build();
    }


    @Override
    public ResponseObject<Page<ProductInfo>> findProduct(String key, int category, int season, List<Integer> labels, int page, int size, String sortField, String sortDirection) {
        List<Product> products;
        if (Objects.equals(key, "null"))
            products = productService.getProductsByPage(page, size, sortField, sortDirection).getContent();
        else {
            // Đơn giản hóa: Chỉ search theo title, bỏ category/season/labels
            // Split keyword thành các từ riêng biệt
            List<String> keys = new ArrayList<>(List.of(key.split(" ")));
            Set<String> setKey = new LinkedHashSet<>();
            for (String key1 : keys) {
                // Normalize: Capitalize first letter, lowercase rest
                setKey.add(key1.substring(0, 1).toUpperCase() + key1.substring(1).toLowerCase());
            }
            products = productService.findProducts(setKey, page, size, sortField, sortDirection).getContent();
        }
        List<ProductInfo> res = new ArrayList<>();
        for (Product product : products) {
            ChainData<ProductInfo> chainData = getProductDTO(product);
            res.add(chainData.getValue());
        }
        if (res.isEmpty()) {
            return emptyProduct();
        } else {
            sort(sortField, sortDirection, res);
            Page<ProductInfo> resDTO = new PageImpl<>(res, PageRequest.of(page, size), products.size());
            return successP(resDTO);
        }
    }


    @Override
    public ResponseObject<Page<ProductInfo>> getProductsByCategoryId(int id, int page, int size, String sortField, String sortDirect) {
//        List<Detail> details = detailService.getDetailsByCategoryId(id);
        List<Detail> details = detailService.getPageDetailsByCategoryId(id, page, size, "id", sortDirect).getContent();
        if (!details.isEmpty()) {
            List<ProductInfo> products = new ArrayList<>();
            details.forEach(detail -> {
                products.add(ProductInfo.builder()
                        .season_id(detail.getSeason_id())
                        .productId(detail.getProduct_id())
                        .category(id)
                        .build());
            });
            List<ProductInfo> res = new ArrayList<>();
            for (ProductInfo productInfo : products) {
                ChainData<ProductInfo> chainData = ChainData.<ProductInfo>builder()
                        .value(productInfo)
                        .build();
                Chain<ProductInfo> chain = getProductDTOByDetail();
                chain.add(GetLabelByProdId.builder()
                                .labelService(labelService)
                                .labelProductService(labelProductService)
                                .build())
                        .add(new GetSeasonById(seasonService));
                chain.execute(chainData);
                res.add(chainData.getValue());
            }
            if (res.isEmpty()) {
                return emptyProduct();
            } else {
                Page<ProductInfo> done = convertListPage.listToPage(res, page, size);
                return successP(done);
            }
        } else return serverError();
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
        if (!chainData.isSuccess()) log.error(chainData.getMessage() + "/n" + chainData.getValue());
        return chainData;
    }


    private Chain<ProductInfo> getProductDTOByDetail() {
        return new Chain<ProductInfo>()
                .add(new GetProdById(productService))
                .add(GetColorSizeQuantityByProdId.builder()
                        .colorService(colorService)
                        .sizeService(sizeService)
                        .cspService(cspService)
                        .smallQuantityService(smallQuantityService)
                        .build())
                .add(new GetImageByProdId(imageService))
                .add(GetDetailByProdId.builder()
                        .detailService(detailService)
                        .quantityService(quantityService)
                        .build())
                .add(GetCateAndSeasonById.builder()
                        .categoryService(categoryService)
                        .seasonService(seasonService)
                        .build())
                .add(GetLabelByProdId.builder()
                        .labelProductService(labelProductService)
                        .labelService(labelService)
                        .build());
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


    private ResponseObject<Page<ProductInfo>> successP(Page<ProductInfo> res) {
        return ResponseObject.<Page<ProductInfo>>builder()
                .data(res)
                .isSuccess(true)
                .message("Lay danh sach san pham thanh cong")
                .build();
    }


    private ResponseObject<Page<ProductInfo>> emptyProduct() {
        return ResponseObject.<Page<ProductInfo>>builder()
                .message("Khong the lay danh sach san pham")
                .build();
    }


    private ResponseObject<Page<ProductInfo>> serverError() {
        return ResponseObject.<Page<ProductInfo>>builder()
                .message("Khong tim duoc nhan san pham")
                .build();
    }
}

