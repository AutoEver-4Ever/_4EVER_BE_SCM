package org.ever._4ever_be_scm.scm.external.service;

import org.ever._4ever_be_scm.scm.external.dto.*;

import java.util.List;

public interface ExternalApiService {

    /**
     * ProductOrderId로 ProductOrderItem들 가져오기
     */
    ProductOrderItemResponseDto getProductOrderItems(String productOrderId);

    /**
     * ProductOrderId들로 ProductOrder 정보 제공
     */
    List<ProductOrderInfoDto> getProductOrderInfos(List<String> productOrderIds);

    /**
     * ProductId들로 Product 정보들 제공
     */
    ProductMultipleResponseDto getProductsMultiple(List<String> productIds);

    /**
     * ItemId를 통해 부족재고 파악
     */
    StockCheckResponseDto checkStock( List<StockCheckRequestDto.ItemRequest> items);

    /**
     * SupplierCompanyId로 SupplierCompany 정보 가져오기(단건)
     */
    SupplierCompanySingleResponseDto getSupplierCompanySingle(String supplierCompanyId);

    /**
     * SupplierCompanyId들로 SupplierCompany 정보 가져오기(다수)
     */
    SupplierCompanyMultipleResponseDto getSupplierCompaniesMultiple(List<String> supplierCompanyIds);

    /**
     * 카테고리가 ITEM인 Product 목록 반환 (productId, productName, uomName, unitPrice)
     */
    ProductMultipleResponseDto getItemCategoryProducts();
}
