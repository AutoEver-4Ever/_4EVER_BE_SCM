package org.ever._4ever_be_scm.scm.mm.service.impl;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierCompany;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierUser;
import org.ever._4ever_be_scm.scm.iv.repository.ProductRepository;
import org.ever._4ever_be_scm.scm.mm.dto.PurchaseOrderDetailResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.PurchaseOrderListResponseDto;
import org.ever._4ever_be_scm.scm.mm.entity.ProductOrder;
import org.ever._4ever_be_scm.scm.mm.entity.ProductOrderApproval;
import org.ever._4ever_be_scm.scm.mm.entity.ProductOrderItem;
import org.ever._4ever_be_scm.scm.mm.entity.ProductRequestApproval;
import org.ever._4ever_be_scm.scm.mm.repository.ProductOrderApprovalRepository;
import org.ever._4ever_be_scm.scm.mm.repository.ProductOrderItemRepository;
import org.ever._4ever_be_scm.scm.mm.repository.ProductOrderRepository;
import org.ever._4ever_be_scm.scm.mm.service.PurchaseOrderService;
import org.ever._4ever_be_scm.scm.mm.vo.PurchaseOrderSearchVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    
    private final ProductOrderRepository productOrderRepository;
    private final ProductOrderItemRepository productOrderItemRepository;
    private final ProductRepository productRepository;
    private final ProductOrderApprovalRepository productOrderApprovalRepository;

    @Override
    @Transactional(readOnly = true)  
    public Page<PurchaseOrderListResponseDto> getPurchaseOrderList(PurchaseOrderSearchVo searchVo) {
        PageRequest pageRequest = PageRequest.of(searchVo.getPage(), searchVo.getSize());
        
        // 날짜 범위 설정
        final LocalDateTime startDateTime = searchVo.getStartDate() != null 
                ? searchVo.getStartDate().atStartOfDay() 
                : null;
        final LocalDateTime endDateTime = searchVo.getEndDate() != null 
                ? searchVo.getEndDate().atTime(LocalTime.MAX) 
                : null;
        
        // 모든 발주서 조회 후 필터링
        List<ProductOrder> allOrders = productOrderRepository.findAll();
        List<ProductOrder> filteredOrders = allOrders.stream()
                .filter(order -> {
                    // type/keyword 필터링
                    String type = searchVo.getType();
                    String keyword = searchVo.getKeyword();
                    if (keyword != null && !keyword.isEmpty()) {
                        if ("supplierName".equalsIgnoreCase(type)) {
                            // 첫 번째 아이템의 제품에서 supplierCompany를 찾아서 이름으로 매칭
                            List<ProductOrderItem> items = productOrderItemRepository.findByProductOrderId(order.getId());
                            if (items.isEmpty()) return false;
                            ProductOrderItem first = items.get(0);
                            Product product = productRepository.findById(first.getProductId()).orElse(null);
                            String supplierName = null;
                            if (product != null && product.getSupplierCompany() != null) {
                                supplierName = product.getSupplierCompany().getCompanyName();
                            }
                            if (supplierName == null || !supplierName.toLowerCase().contains(keyword.toLowerCase())) return false;
                        } else if ("purchaseOrderNumber".equalsIgnoreCase(type)) {
                            if (order.getProductOrderCode() == null || !order.getProductOrderCode().toLowerCase().contains(keyword.toLowerCase())) return false;
                        }
                    }

                    // 날짜 범위 필터링
                    if (startDateTime != null && order.getCreatedAt().isBefore(startDateTime)) {
                        return false;
                    }
                    if (endDateTime != null && order.getCreatedAt().isAfter(endDateTime)) {
                        return false;
                    }
                    return true;
                })
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
        
        // 페이징 처리
        int start = pageRequest.getPageNumber() * pageRequest.getPageSize();
        int end = Math.min(start + pageRequest.getPageSize(), filteredOrders.size());
        List<ProductOrder> pagedOrders = filteredOrders.subList(start, end);
        
        List<PurchaseOrderListResponseDto> dtoList = new ArrayList<>();
        for (ProductOrder productOrder : pagedOrders) {
            // 발주서 아이템 조회하여 요약 생성
            List<ProductOrderItem> items = productOrderItemRepository.findByProductOrderId(productOrder.getId());
            String itemsSummary = generateItemsSummary(items);
            
            // 승인 상태 조회
            String statusCodeValue = productOrder.getApprovalId().getApprovalStatus();
            if (statusCodeValue == null) {
                statusCodeValue = "PENDING";
            }
            
            // supplierName: first item's product -> supplierCompany
            String supplierName = null;
            if (!items.isEmpty()) {
                ProductOrderItem first = items.get(0);
                Product product = productRepository.findById(first.getProductId()).orElse(null);
                if (product != null && product.getSupplierCompany() != null) {
                    supplierName = product.getSupplierCompany().getCompanyName();
                }
            }

            dtoList.add(PurchaseOrderListResponseDto.builder()
                    .purchaseOrderId(productOrder.getId())
                    .purchaseOrderNumber(productOrder.getProductOrderCode())
                    .supplierName(supplierName)
                    .itemsSummary(itemsSummary)
                    .orderDate(productOrder.getCreatedAt())
                    .dueDate(productOrder.getDueDate())
                    .totalAmount(productOrder.getTotalPrice())
                    .statusCode(statusCodeValue)
                    .build());
        }
        
        return new PageImpl<>(dtoList, pageRequest, filteredOrders.size());
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDetailResponseDto getPurchaseOrderDetail(String purchaseOrderId) {
        ProductOrder productOrder = productOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("발주서를 찾을 수 없습니다."));
        
        List<ProductOrderItem> items = productOrderItemRepository.findByProductOrderId(purchaseOrderId);
        
        // 승인 상태 조회
        String statusCode = productOrder.getApprovalId().getApprovalStatus();
        
        List<PurchaseOrderDetailResponseDto.ItemDto> itemDtos = new ArrayList<>();
        for (ProductOrderItem item : items) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            itemDtos.add(PurchaseOrderDetailResponseDto.ItemDto.builder()
                    .itemId(item.getProductId())
                    .itemName(product != null ? product.getProductName() : "알 수 없는 제품")
                    .quantity(item.getCount())
                    .uomName(item.getUnit())
                    .unitPrice(item.getPrice())
                    .totalPrice(item.getPrice().multiply(item.getCount()))
                    .build());
        }

        // supplierName: first item's product -> supplierCompany
        SupplierCompany supplierCompany = null;
        if (!items.isEmpty()) {
            ProductOrderItem first = items.get(0);
            Product product = productRepository.findById(first.getProductId()).orElse(null);
            if (product != null && product.getSupplierCompany() != null) {
                supplierCompany = product.getSupplierCompany();
            }
        }
        
        return PurchaseOrderDetailResponseDto.builder()
                .statusCode(statusCode)
                .dueDate(productOrder.getDueDate())
                .purchaseOrderId(productOrder.getId())
                .purchaseOrderNumber(productOrder.getProductOrderCode())
                .orderDate(productOrder.getCreatedAt())
                .supplierId(Optional.ofNullable(supplierCompany)
                        .map(SupplierCompany::getId)
                        .orElse(null))
                .supplierName(Optional.ofNullable(supplierCompany)
                        .map(SupplierCompany::getCompanyName)
                        .orElse(null))
                .supplierNumber(Optional.ofNullable(supplierCompany)
                        .map(SupplierCompany::getCompanyCode)
                        .orElse(null))
                .managerPhone(Optional.ofNullable(supplierCompany)
                        .map(SupplierCompany::getSupplierUser)
                        .map(SupplierUser::getSupplierUserPhoneNumber)
                        .orElse(null))
                .managerEmail(Optional.ofNullable(supplierCompany)
                        .map(SupplierCompany::getSupplierUser)
                        .map(SupplierUser::getSupplierUserEmail)
                        .orElse(null))

                .items(itemDtos)
                .totalAmount(productOrder.getTotalPrice())
                .note(productOrder.getEtc())
                .build();
    }

    private String generateItemsSummary(List<ProductOrderItem> items) {
        if (items.isEmpty()) {
            return "아이템 없음";
        }

        int displayLimit = 3; // 최대 표시할 아이템 수
        List<String> names = items.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    String productName = product != null ? product.getProductName() : "알 수 없는 제품";
                    return productName + " " + item.getCount() + item.getUnit();
                })
                .toList();

        String result;
        if (names.size() > displayLimit) {
            result = String.join(", ", names.subList(0, displayLimit)) + ", ...";
        } else {
            result = String.join(", ", names);
        }

        return result;
    }

    @Override
    @Transactional
    public void approvePurchaseOrder(String purchaseOrderId, String requesterId) {
        ProductOrder productOrder = productOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new RuntimeException("발주서를 찾을 수 없습니다: " + purchaseOrderId));
        
        ProductOrderApproval approval = productOrder.getApprovalId();
        if (approval == null) {
            throw new RuntimeException("승인 정보가 없습니다.");
        }

        ProductOrderApproval updatedApproval = approval.toBuilder()
                .approvalStatus("APPROVAL")
                .approvedAt(LocalDateTime.now())
                .approvedBy(requesterId)
                .build();

        productOrderApprovalRepository.save(updatedApproval);
    }

    @Override
    @Transactional
    public void rejectPurchaseOrder(String purchaseOrderId, String requesterId, String reason) {
        ProductOrder productOrder = productOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new RuntimeException("발주서를 찾을 수 없습니다: " + purchaseOrderId));
        
        ProductOrderApproval approval = productOrder.getApprovalId();
        if (approval == null) {
            throw new RuntimeException("승인 정보가 없습니다.");
        }


        ProductOrderApproval updatedApproval = approval.toBuilder()
                .approvalStatus("REJECTED")
                .approvedAt(LocalDateTime.now())
                .rejectedReason(reason)
                .approvedBy(requesterId)
                .build();

        productOrderApprovalRepository.save(updatedApproval);
    }
}
