package com.prod.chains.getOrders;

import com.prod.chains.Chain;
import com.prod.chains.ChainHandler;
import com.prod.chains.data.ChainData;
import com.prod.facades.data.OrderInfo;
import com.prod.facades.data.OrderProductInfo;
import com.prod.models.ENUM.Type_Image;
import com.prod.models.carts.Color;
import com.prod.models.carts.Color_Size_Product;
import com.prod.models.carts.Size;
import com.prod.models.products.Image;
import com.prod.models.products.Product;
import com.prod.services.carts.IColorService;
import com.prod.services.carts.IColorSizeProductService;
import com.prod.services.carts.ISizeService;
import com.prod.services.products.IImageService;
import com.prod.services.products.IProductService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
@Builder
@Slf4j
public class GetProductByOrderProduct implements ChainHandler<OrderInfo> {
    private final IProductService productService;
    private final IColorService colorService;
    private final IColorSizeProductService cspService;
    private final ISizeService sizeService;
    private final IImageService imageService;
    @Override
    public Chain<OrderInfo> handle(ChainData<OrderInfo> chainData) {
        if (chainData.isSuccess()) {
            OrderInfo dto = chainData.getValue();
            List<OrderProductInfo> products = new ArrayList<>();
            log.info("Processing order_id: {} with {} products", dto.getId(), dto.getProducts().size());

            for (OrderProductInfo opDTO : dto.getProducts()) {
                log.info("Processing order_product with csp_id: {}", opDTO.getCsp_id());

                Optional<Color_Size_Product> csp = cspService.getColorSizeProductById(opDTO.getCsp_id());
                if (csp.isEmpty()) {
                    log.error("Color_Size_Product not found for csp_id: {}", opDTO.getCsp_id());
                    continue;
                }
                log.info("Found CSP for csp_id: {}, product_id: {}, color_id: {}, size_id: {}",
                    opDTO.getCsp_id(), csp.get().getProduct_id(), csp.get().getColor_id(), csp.get().getSize_id());

                Optional<Product> product = productService.getProductById(csp.get().getProduct_id());
                if (product.isEmpty()) {
                    log.error("Product not found for product_id: {}", csp.get().getProduct_id());
                    continue;
                }

                Optional<Color> color = colorService.getColorById(csp.get().getColor_id());
                if (color.isEmpty()) {
                    log.error("Color not found for color_id: {}", csp.get().getColor_id());
                    continue;
                }

                Optional<Size> size = sizeService.getSizeById(csp.get().getSize_id());
                if (size.isEmpty()) {
                    log.error("Size not found for size_id: {}", csp.get().getSize_id());
                    continue;
                }

                Optional<Image> image = imageService.getImageByProdIdAndType(product.get().getId(), Type_Image.ANH_NEN);
                if (image.isEmpty()) {
                    log.error("Image not found for product_id: {}", product.get().getId());
                    continue;
                }

                OrderProductInfo dto1 = updateOrderProduct(opDTO, opDTO.getQuantity(), product.get(), color.get(), size.get(), image.get());
                products.add(dto1);
                log.info("Successfully processed order_product with csp_id: {}", opDTO.getCsp_id());
            }

            if (products.isEmpty()) {
                log.error("No valid products found for order_id: {}", dto.getId());
                chainData.setMessage("Gap loi khi tim danh sach san pham").setSuccess(false);
            } else {
                log.info("Successfully processed {} products for order_id: {}", products.size(), dto.getId());
                dto.setProducts(products);
                chainData.setValue(dto).setSuccess(true);
            }
        }
        return new Chain<>(this);
    }

    private OrderProductInfo updateOrderProduct(OrderProductInfo dto,
                                                int quantity,
                                                Product product,
                                                Color color,
                                                Size size,
                                                Image image) {
        dto.setProduct_id(product.getId());
        dto.setName(product.getTitle());
        dto.setPrice(product.getPrice());
        dto.setColor(color.getValue());
        dto.setSize(size.getValue());
        dto.setQuantity(quantity);
        dto.setImage(image.getSrc());
        return dto;
    }
}
