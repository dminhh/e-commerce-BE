package com.prod.facades.impl;

import com.common.DTO.ResponseObject;
import com.prod.chains.Chain;
import com.prod.chains.checkQuantityProduct.*;
import com.prod.chains.data.ChainData;
import com.prod.chains.updateCart.*;
import com.prod.facades.ICartFacade;
import com.prod.facades.data.CartInfo;
import com.prod.facades.data.CartProductInfo;
import com.prod.mappers.CartMapper;
import com.prod.models.carts.Cart;
import com.prod.models.carts.Cart_Product;
import com.prod.models.carts.Color_Size_Product;
import com.prod.redis.AccountRedis;
import com.prod.services.caches.IEmailSender;
import com.prod.services.carts.*;
import com.prod.services.details.IDetailService;
import com.prod.services.details.IQuantityService;
import com.prod.services.products.IImageService;
import com.prod.services.products.IProductService;
import com.prod.utils.ConvertListPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CartFacade implements ICartFacade {
    @Autowired
    private ICartService cartService;
    @Autowired
    private IImageService imageService;
    @Autowired
    private ICartProductService cartProductService;
    @Autowired
    private IColorSizeProductService cspService;
    @Autowired
    private IProductService productService;
    @Autowired
    private IDetailService detailService;
    @Autowired
    private IQuantityService quantityService;
    @Autowired
    private ISmallQuantityService smallQuantityService;
    @Autowired
    private IEmailSender emailSender;
    @Autowired
    private ISizeService sizeService;
    @Autowired
    private IColorService colorService;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ConvertListPage<CartInfo> convertListPage;

    @Override
    public ResponseObject<Page<CartInfo>> getCartByUserId(int userId, AccountRedis accountRedis, int page, int size) {
        try {
            Optional<Cart> cart = cartService.getCartByUserId(userId);
            if (cart.isPresent()) {
                List<Cart_Product> cspList = cartProductService.getCartProductsByCartId(cart.get().getId());
                if (cspList == null) {
                    return ResponseObject.<Page<CartInfo>>builder()
                            .message("Gio hang trong")
                            .build();
                } else {
                    List<CartInfo> list = new ArrayList<>();
                    cspList.sort(Comparator.comparing(Cart_Product::getUpdate_at).reversed());
                    for (Cart_Product csp : cspList) {
                        list.add(alertEmptyQuantity(csp, accountRedis.getEmail()));
                    }
                    return ResponseObject.<Page<CartInfo>>builder()
                            .data(convertListPage.listToPage(list, page, size))
                            .message("Da tim thay gio hang")
                            .isSuccess(true)
                            .build();
                }
            } else {
                return ResponseObject.<Page<CartInfo>>builder()
                        .message("Khong tim thay gio hang")
                        .build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseObject.<Page<CartInfo>>builder()
                    .message("Get Cart Products By Cart Id - Gap loi server")
                    .build();
        }
    }

    @Override
    public ResponseObject<Page<Color_Size_Product>> getCSP(int cartId, int page, int size, String sortField, String sortDirection) {
        return null;
    }

    @Override
    public ResponseObject<Page<CartInfo>> updateCartByUserId(List<CartProductInfo> cartProductInfo, int userId, int page, int size) {
        try {
            Cart cart = cartService.getCartByUserId(userId).isEmpty() ? cartService.createCart(Cart.builder()
                    .user_id(userId)
                    .build()) : cartService.getCartByUserId(userId).get();
            List<CartInfo> res = new ArrayList<>();
            for (CartProductInfo cartDTO : cartProductInfo) {
                ChainData<CartInfo> dto = ChainData.<CartInfo>builder()
                        .userId(userId)
                        .value(
                                CartInfo.builder()
                                        .quantity(cartDTO.getQuantity())
                                        .productId(cartDTO.getProductId())
                                        .colorId(cartDTO.getColorId())
                                        .sizeId(cartDTO.getSizeId())
                                        .build()
                        )
                        .cartId(cart.getId())
                        .build();
                Chain<CartInfo> chain = new Chain<CartInfo>()
                        .add(new CheckIfColorExist(colorService))
                        .add(new CheckIfSizeExist(sizeService))
                        .add(new GetFirstImageByProdId(imageService))
                        .add(new CheckIfProductExist(cspService, productService))
                        .add(new CheckIfQuantityEnough(smallQuantityService, cartProductService, cartService));
                chain.execute(dto);
                if (!dto.isSuccess()) log.error(dto.getMessage());
                else res.add(dto.getValue());
            }
            if (res.isEmpty()) {
                return ResponseObject.<Page<CartInfo>>builder()
                        .message("Khong the tao gio hang")
                        .build();
            } else {
                return ResponseObject.<Page<CartInfo>>builder()
                        .isSuccess(true)
                        .message("Cap nhat gio hang thanh cong")
                        .data(convertListPage.listToPage(res, page, size))
                        .build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseObject.<Page<CartInfo>>builder()
                    .message("Gap loi server")
                    .build();
        }
    }

    private CartInfo alertEmptyQuantity(Cart_Product cartProduct, String email) {
        Optional<Color_Size_Product> csp = cspService.getColorSizeProductById(cartProduct.getColor_size_product_id());
        if (csp.isPresent()) {
            ChainData<CartInfo> dataDTO = ChainData.<CartInfo>builder()
                    .value(CartInfo.builder()
                            .cspId(csp.get().getId())
                            .sizeId(csp.get().getSize_id())
                            .colorId(csp.get().getColor_id())
                            .cart_product_id(cartProduct.getId())
                            .productId(csp.get().getProduct_id())
                            .build())
                    .userEmail(email)
                    .build();
            Chain<CartInfo> chain = new Chain<CartInfo>()
                    .add(new GetSizeById(sizeService))
                    .add(new GetColorById(colorService))
                    .add(new GetFirstImageByProdId(imageService))
                    .add(new GetCartProductById(productService, cartProductService))
                    .add(new GetSmallQuantityByCSPId(smallQuantityService, emailSender));
            chain.execute(dataDTO);
            if (dataDTO.isSuccess()) {
                return dataDTO.getValue();
            } else {
                log.error(dataDTO.getMessage());
                return null;
            }
        } else {
            log.error("Khong tim thay id color size product");
            return null;
        }
    }
    @Override
    public ResponseObject<Page<CartInfo>> deleteCartProduct(int cartProductId, int userId, int page, int size) {
        try {
            // Kiểm tra cart product có tồn tại không
            Optional<Cart_Product> cartProduct = cartProductService.getCartProductById(cartProductId);

            if (cartProduct.isEmpty()) {
                return ResponseObject.<Page<CartInfo>>builder()
                        .message("Khong tim thay san pham trong gio hang")
                        .build();
            }

            // Kiểm tra cart có thuộc về user không
            Optional<Cart> cart = cartService.getCartByUserId(userId);
            if (cart.isEmpty() || cart.get().getId() != cartProduct.get().getCart_id()) {
                return ResponseObject.<Page<CartInfo>>builder()
                        .message("Khong co quyen xoa san pham nay")
                        .build();
            }

            // Xóa sản phẩm
            cartProductService.deleteCartProduct(cartProductId);

            // Trả về cart mới sau khi xóa
            return getCartByUserId(userId, null, page, size);

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseObject.<Page<CartInfo>>builder()
                    .message("Gap loi khi xoa san pham khoi gio hang")
                    .build();
        }
    }

    @Override
    public ResponseObject<Page<CartInfo>> updateCartProductQuantity(int cartProductId, int quantity, int userId, int page, int size) {
        try {
            // Validate quantity
            if (quantity <= 0) {
                return ResponseObject.<Page<CartInfo>>builder()
                        .message("So luong phai lon hon 0")
                        .build();
            }

            // Kiểm tra cart product có tồn tại không
            Optional<Cart_Product> cartProductOpt = cartProductService.getCartProductById(cartProductId);

            if (cartProductOpt.isEmpty()) {
                return ResponseObject.<Page<CartInfo>>builder()
                        .message("Khong tim thay san pham trong gio hang")
                        .build();
            }

            Cart_Product cartProduct = cartProductOpt.get();

            // Kiểm tra cart có thuộc về user không
            Optional<Cart> cart = cartService.getCartByUserId(userId);
            if (cart.isEmpty() || cart.get().getId() != cartProduct.getCart_id()) {
                return ResponseObject.<Page<CartInfo>>builder()
                        .message("Khong co quyen cap nhat san pham nay")
                        .build();
            }

            // Kiểm tra số lượng còn đủ trong kho không
            Optional<Color_Size_Product> csp = cspService.getColorSizeProductById(cartProduct.getColor_size_product_id());
            if (csp.isEmpty()) {
                return ResponseObject.<Page<CartInfo>>builder()
                        .message("Khong tim thay san pham")
                        .build();
            }

            // Lấy số lượng còn lại trong kho
            int availableQuantity = smallQuantityService.getByCSProductId(csp.get().getId())
                    .map(sq -> sq.getQuantity())
                    .orElse(0);

            if (quantity > availableQuantity) {
                return ResponseObject.<Page<CartInfo>>builder()
                        .message("So luong vuot qua ton kho. Chi con " + availableQuantity + " san pham")
                        .build();
            }

            // Cập nhật quantity
            cartProduct.setQuantity(quantity);
            cartProductService.createCartProduct(cartProduct); // save/update

            // Trả về cart mới sau khi cập nhật
            return getCartByUserId(userId, null, page, size);

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseObject.<Page<CartInfo>>builder()
                    .message("Gap loi khi cap nhat so luong")
                    .build();
        }
    }
}
