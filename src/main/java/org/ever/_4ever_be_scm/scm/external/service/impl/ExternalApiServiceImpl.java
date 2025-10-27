package org.ever._4ever_be_scm.scm.external.service.impl;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.scm.external.dto.*;
import org.ever._4ever_be_scm.scm.external.service.ExternalApiService;
import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStock;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierCompany;
import org.ever._4ever_be_scm.scm.iv.repository.ProductRepository;
import org.ever._4ever_be_scm.scm.iv.repository.ProductStockRepository;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierCompanyRepository;
import org.ever._4ever_be_scm.scm.mm.entity.ProductOrder;
import org.ever._4ever_be_scm.scm.mm.entity.ProductOrderItem;
import org.ever._4ever_be_scm.scm.mm.repository.ProductOrderItemRepository;
import org.ever._4ever_be_scm.scm.mm.repository.ProductOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.ever._4ever_be_scm.scm.mm.entity.QProductOrderItem.productOrderItem;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExternalApiServiceImpl implements ExternalApiService {
    
    private final ProductOrderRepository productOrderRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final SupplierCompanyRepository supplierCompanyRepository;
    private final ProductOrderItemRepository productOrderItemRepository;

    @Override
    public ProductOrderItemResponseDto getProductOrderItems(String productOrderId) {
        // ProductOrder 조회
        ProductOrder productOrder = productOrderRepository.findById(productOrderId)
                .orElseThrow(() -> new RuntimeException("ProductOrder not found: " + productOrderId));

        // ProductOrderItem 목록 조회
        List<ProductOrderItem> productOrderItems = productOrderItemRepository.findByProductOrderId(productOrder.getId());

        // DTO 변환
        List<ProductOrderItemResponseDto.ItemDto> items = productOrderItems.stream()
                .map(item -> {
                    // Product 조회
                    Product product = productRepository.findById(item.getProductId())
                            .orElse(null); // 없을 경우 null 허용 (안전 처리)

                    return ProductOrderItemResponseDto.ItemDto.builder()
                            .itemId(item.getId())
                            .itemName(product != null ? product.getProductName() : "Unknown Product")
                            .quantity(item.getCount())
                            .uomName(item.getUnit())
                            .unitPrice(item.getPrice())
                            .totalPrice(item.getPrice().multiply(item.getCount())) // 개별 품목 금액 계산
                            .build();
                })
                .collect(Collectors.toList());

        // 최종 DTO 반환
        return ProductOrderItemResponseDto.builder()
                .items(items)
                .totalPrice(productOrder.getTotalPrice()) // 전체 발주 금액
                .build();
    }



    @Override
    public List<ProductOrderInfoDto> getProductOrderInfos(List<String> productOrderIds) {
        return productOrderIds.stream()
                .map(id -> {
                    ProductOrder productOrder = productOrderRepository.findById(id)
                            .orElse(null);
                    if (productOrder != null) {
                        return ProductOrderInfoDto.builder()
                                .productOrderId(id)
                                .totalAmount(productOrder.getTotalPrice())
                                .build();
                    }
                    return ProductOrderInfoDto.builder()
                            .productOrderId(id)
                            .totalAmount(BigDecimal.ZERO)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public ProductMultipleResponseDto getProductsMultiple(List<String> productIds) {
        List<ProductMultipleResponseDto.ProductDto> products = productIds.stream()
                .map(id -> {
                    Product product = productRepository.findById(id).orElse(null);
                    if (product != null) {
                        return ProductMultipleResponseDto.ProductDto.builder()
                                .productId(id)
                                .productNumber(product.getProductCode())
                                .productName(product.getProductName())
                                .build();
                    }
                    return ProductMultipleResponseDto.ProductDto.builder()
                            .productId(id)
                            .productNumber("")
                            .productName("제품을 찾을 수 없습니다")
                            .build();
                })
                .collect(Collectors.toList());
        
        return ProductMultipleResponseDto.builder()
                .products(products)
                .build();
    }

    @Override
    public StockCheckResponseDto checkStock(List<StockCheckRequestDto.ItemRequest> items) {

        List<StockCheckResponseDto.ItemStockDto> itemStockList = items.stream()
                .map(itemRequest -> {
                    String itemId = itemRequest.getItemId();
                    BigDecimal requiredQuantity = itemRequest.getRequiredQuantity();

                    // Product 조회
                    Product product = productRepository.findById(itemId).orElse(null);
                    List<ProductStock> stockList = productStockRepository.findByProductId(itemId);
                    ProductStock productStock = stockList.isEmpty() ? null : stockList.get(0);

                    String itemName = product != null ? product.getProductName() : "알 수 없는 제품";
                    BigDecimal inventoryQuantity = productStock != null ? productStock.getAvailableCount() : BigDecimal.ZERO;

                    // 부족 수량 계산
                    BigDecimal shortageQuantity = requiredQuantity.subtract(inventoryQuantity);
                    if (shortageQuantity.compareTo(BigDecimal.ZERO) < 0) {
                        shortageQuantity = BigDecimal.ZERO;
                    }

                    String statusCode = shortageQuantity.compareTo(BigDecimal.ZERO) > 0 ? "SHORTAGE" : "FULFILLED";
                    boolean productionRequired = shortageQuantity.compareTo(BigDecimal.ZERO) > 0;

                    return StockCheckResponseDto.ItemStockDto.builder()
                            .itemId(itemId)
                            .itemName(itemName)
                            .requiredQuantity(requiredQuantity)
                            .inventoryQuantity(inventoryQuantity)
                            .shortageQuantity(shortageQuantity)
                            .statusCode(statusCode)
                            .productionRequired(productionRequired)
                            .build();
                })
                .collect(Collectors.toList());

        return StockCheckResponseDto.builder()
                .items(itemStockList)
                .build();
    }


    @Override
    public SupplierCompanySingleResponseDto getSupplierCompanySingle(String supplierCompanyId) {
        SupplierCompany supplierCompany = supplierCompanyRepository.findById(supplierCompanyId)
                .orElseThrow(() -> new RuntimeException("SupplierCompany not found: " + supplierCompanyId));
        
        return SupplierCompanySingleResponseDto.builder()
                .companyId(supplierCompany.getId())
                .companyNumber(supplierCompany.getCompanyCode())
                .companyName(supplierCompany.getCompanyName())
                .baseAddress(supplierCompany.getBaseAddress())
                .detailAddress(supplierCompany.getDetailAddress())
                .category(supplierCompany.getCategory())
                .officePhone(supplierCompany.getOfficePhone())
                .managerId(supplierCompany.getSupplierUser() != null ? 
                        supplierCompany.getSupplierUser().getId() : "")
                .build();
    }

    @Override
    public SupplierCompanyMultipleResponseDto getSupplierCompaniesMultiple(List<String> supplierCompanyIds) {
        List<SupplierCompanyMultipleResponseDto.SupplierCompanyDto> supplierCompanies = supplierCompanyIds.stream()
                .map(id -> {
                    SupplierCompany supplierCompany = supplierCompanyRepository.findById(id).orElse(null);
                    if (supplierCompany != null) {
                        return SupplierCompanyMultipleResponseDto.SupplierCompanyDto.builder()
                                .companyId(id)
                                .companyNumber(supplierCompany.getCompanyCode())
                                .companyName(supplierCompany.getCompanyName())
                                .baseAddress(supplierCompany.getBaseAddress())
                                .detailAddress(supplierCompany.getDetailAddress())
                                .category(supplierCompany.getCategory())
                                .officePhone(supplierCompany.getOfficePhone())
                                .managerId(supplierCompany.getSupplierUser() != null ? 
                                        supplierCompany.getSupplierUser().getId() : "")
                                .build();
                    }
                    return SupplierCompanyMultipleResponseDto.SupplierCompanyDto.builder()
                            .companyId(id)
                            .companyNumber("")
                            .companyName("공급업체를 찾을 수 없습니다")
                            .baseAddress("")
                            .detailAddress("")
                            .category("")
                            .officePhone("")
                            .managerId("")
                            .build();
                })
                .collect(Collectors.toList());
        
        return SupplierCompanyMultipleResponseDto.builder()
                .supplierCompanies(supplierCompanies)
                .build();
    }
}
