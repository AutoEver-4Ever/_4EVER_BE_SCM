package org.ever._4ever_be_scm.scm.mm.service.impl;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierCompany;
import org.ever._4ever_be_scm.scm.iv.repository.ProductRepository;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierCompanyRepository;
import org.ever._4ever_be_scm.scm.mm.dto.*;
import org.ever._4ever_be_scm.scm.mm.entity.*;
import org.ever._4ever_be_scm.scm.mm.repository.*;
import org.ever._4ever_be_scm.scm.mm.service.PurchaseRequisitionService;
import org.ever._4ever_be_scm.scm.mm.vo.PurchaseRequisitionCreateVo;
import org.ever._4ever_be_scm.scm.mm.vo.PurchaseRequisitionSearchVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PurchaseRequisitionServiceImpl implements PurchaseRequisitionService {
    
    private final ProductRequestRepository productRequestRepository;
    private final ProductRequestItemRepository productRequestItemRepository;
    private final ProductRequestApprovalRepository productRequestApprovalRepository;
    private final ProductOrderRepository productOrderRepository;
    private final ProductOrderItemRepository productOrderItemRepository;
    private final ProductOrderApprovalRepository productOrderApprovalRepository;
    private final ProductRepository productRepository;
    private final SupplierCompanyRepository supplierCompanyRepository;

    @Override
    @Transactional
    public Page<PurchaseRequisitionListResponseDto> getPurchaseRequisitionList(PurchaseRequisitionSearchVo searchVo) {
        Pageable pageable = PageRequest.of(searchVo.getPage(), searchVo.getSize());
        
        // 날짜 범위 설정
        final LocalDateTime startDateTime = searchVo.getStartDate() != null 
                ? searchVo.getStartDate().atStartOfDay() 
                : null;
        final LocalDateTime endDateTime = searchVo.getEndDate() != null 
                ? searchVo.getEndDate().atTime(LocalTime.MAX) 
                : null;

        final String statusCode = searchVo.getStatusCode();


        // 조건에 따른 필터링
        List<ProductRequest> allRequests = productRequestRepository.findAll();
        List<ProductRequest> filteredRequests = allRequests.stream()
                .filter(request -> {
                    if (statusCode != null && !"ALL".equalsIgnoreCase(statusCode)) {
                        if (request.getApprovalId().getApprovalStatus() == null || !request.getApprovalId().getApprovalStatus().equalsIgnoreCase(statusCode)) {
                            return false;
                        }
                    }
                    // 간단한 필터링만 적용 (실제 필드명 부족으로 인해 기본적인 필터링만)
                    // 날짜 범위 필터링
                    if (startDateTime != null && request.getCreatedAt().isBefore(startDateTime)) {
                        return false;
                    }
                    if (endDateTime != null && request.getCreatedAt().isAfter(endDateTime)) {
                        return false;
                    }
                    return true;
                })
                .filter(request -> {
                    // Type 기반 키워드 검색 지원
                    String type = searchVo.getType();
                    String keyword = searchVo.getKeyword();
                    if (keyword != null && !keyword.isEmpty()) {
                        if ("requesterName".equalsIgnoreCase(type)) {
                            // 현재 DB에 requesterName이 없으므로 requesterId로 대체 검색
                            if (request.getRequesterId() == null || !request.getRequesterId().toLowerCase().contains(keyword.toLowerCase())) {
                                return false;
                            }
                        } else if ("departmentName".equalsIgnoreCase(type)) {
                            // department 정보가 ProductRequest에 없으므로 검색 불가 -> skip 필터(보존)
                            // TODO: department 엔티티 연동 시 여기에 구현
                        } else if ("productRequestNumber".equalsIgnoreCase(type)) {
                            if (request.getProductRequestCode() == null || !request.getProductRequestCode().contains(keyword.toLowerCase())) {
                                return false;
                            }
                        }
                    }

                    // 날짜 범위 필터링
                    if (startDateTime != null && request.getCreatedAt().isBefore(startDateTime)) {
                        return false;
                    }
                    if (endDateTime != null && request.getCreatedAt().isAfter(endDateTime)) {
                        return false;
                    }
                    return true;
                })
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();

        // 페이징 처리
        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = Math.min(start + pageable.getPageSize(), filteredRequests.size());
        List<ProductRequest> pagedRequests = filteredRequests.subList(start, end);
        
        // DTO 변환
        List<PurchaseRequisitionListResponseDto> responseDtos = pagedRequests.stream()
                .map(request -> PurchaseRequisitionListResponseDto.builder()
                        .purchaseRequisitionId(request.getId())
                        .purchaseRequisitionNumber(request.getProductRequestCode())
                        .requesterId(request.getRequesterId())
                        .requesterName("김철수") // Mock 데이터
                        .requestDate(request.getCreatedAt())
                        .totalAmount(request.getTotalPrice())
                        .statusCode(request.getApprovalId().getApprovalStatus())
                        .build())
                .toList();
                
        return new PageImpl<>(responseDtos, pageable, filteredRequests.size());
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseRequisitionDetailResponseDto getPurchaseRequisitionDetail(String purchaseRequisitionId) {
        ProductRequest productRequest = productRequestRepository.findById(purchaseRequisitionId)
                .orElseThrow(() -> new IllegalArgumentException("구매요청서를 찾을 수 없습니다."));
        
        List<ProductRequestItem> items = productRequestItemRepository.findByProductRequestId(purchaseRequisitionId);

        String statusCode = productRequest.getApprovalId().getApprovalStatus();

        List<PurchaseRequisitionDetailResponseDto.ItemDto> itemDtos = new ArrayList<>();
        for (ProductRequestItem item : items) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            itemDtos.add(PurchaseRequisitionDetailResponseDto.ItemDto.builder()
                    .itemId(item.getProductId())
                    .itemName(product.getProductName())
                    .quantity(item.getCount())
                    .dueDate(item.getPreferredDeliveryDate())
                    .uomCode(item.getUnit())
                    .unitPrice(item.getPrice())
                    .amount(item.getPrice().multiply(item.getCount()))
                    .build());
        }
        
        return PurchaseRequisitionDetailResponseDto.builder()
                .id(productRequest.getId())
                .purchaseRequisitionNumber(productRequest.getProductRequestCode())
                .requesterId(productRequest.getRequesterId())
                .requesterName("김철수") // Mock 데이터
                .departmentId("77") // Mock 데이터
                .departmentName("생산팀") // Mock 데이터
                .requestDate(productRequest.getCreatedAt())
                .statusCode(statusCode)
                .items(itemDtos)
                .totalAmount(productRequest.getTotalPrice())
                .build();
    }

    @Override
    @Transactional
    public void createPurchaseRequisition(PurchaseRequisitionCreateVo createVo) {
        // 1. 승인 정보 생성
        ProductRequestApproval approval = ProductRequestApproval.builder()
                .approvalStatus("PENDING")
                .build();
        approval = productRequestApprovalRepository.save(approval);
        
        // 2. 총 금액 계산
        BigDecimal totalPrice = createVo.getItems().stream()
                .map(item -> item.getExpectedUnitPrice().multiply(item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 3. 구매요청서 생성
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String prCode = "PR-" + uuid.substring(uuid.length() - 6);
        
        ProductRequest productRequest = ProductRequest.builder()
                .productRequestCode(prCode)
                .productRequestType("NON_STOCK")
                .requesterId(createVo.getRequesterId())
                .totalPrice(totalPrice)
                .approvalId(approval)
                .build();
        productRequest = productRequestRepository.save(productRequest);

        // 4. 각 아이템에 대한 Product 및 Supplier 생성
        for (PurchaseRequisitionCreateVo.ItemVo itemVo : createVo.getItems()) {
            SupplierCompany supplier = supplierCompanyRepository.findByCompanyName(itemVo.getPreferredSupplierName())
                    .orElseGet(() -> {
                        SupplierCompany newSupplier = SupplierCompany.builder()
                                .companyCode("SUP-" + UUID.randomUUID().toString().substring(0, 6))
                                .companyName(itemVo.getPreferredSupplierName())
                                .category("ETC")
                                .status("ACTIVE")
                                .build();
                        return supplierCompanyRepository.save(newSupplier);
                    });

            // Product 생성 (NON_STOCK 타입)
            String productCode = "ITEM-" + UUID.randomUUID().toString().substring(0, 6);
            Product product = Product.builder()
                    .productCode(productCode)
                    .category("NON_STOCK")
                    .productName(itemVo.getItemName())
                    .unit(itemVo.getUomName())
                    .supplierCompany(supplier)
                    .originPrice(itemVo.getExpectedUnitPrice())
                    .build();
            product = productRepository.save(product);
            
            // ProductRequestItem 생성
            ProductRequestItem requestItem = ProductRequestItem.builder()
                    .productRequestId(productRequest.getId())
                    .productId(product.getId())
                    .count(itemVo.getQuantity())
                    .unit(itemVo.getUomName())
                    .price(itemVo.getExpectedUnitPrice())
                    .preferredDeliveryDate(itemVo.getDueDate())
                    .purpose(itemVo.getPurpose())
                    .etc(itemVo.getNote())
                    .build();
            productRequestItemRepository.save(requestItem);
        }
    }

    @Override
    @Transactional
    public void approvePurchaseRequisition(String purchaseRequisitionId) {
        ProductRequest productRequest = productRequestRepository.findById(purchaseRequisitionId)
                .orElseThrow(() -> new IllegalArgumentException("구매요청서를 찾을 수 없습니다."));
        
        // 1. 승인 상태 변경
        ProductRequestApproval approval = productRequest.getApprovalId();
        
        approval.setApprovalStatus("APPROVED");
        approval.setApprovedAt(LocalDate.now());
        approval.setApprovedBy("system");
        productRequestApprovalRepository.save(approval);
        
        // 2. 발주서 자동 생성
        createPurchaseOrderFromRequest(productRequest);
    }

    @Override
    @Transactional
    public void rejectPurchaseRequisition(String purchaseRequisitionId, PurchaseRequisitionRejectRequestDto requestDto) {
        ProductRequest productRequest = productRequestRepository.findById(purchaseRequisitionId)
                .orElseThrow(() -> new IllegalArgumentException("구매요청서를 찾을 수 없습니다."));
        
        ProductRequestApproval approval = productRequest.getApprovalId();
        
        approval.setApprovalStatus("REJECTED");
        approval.setRejectedReason(requestDto.getComment());
        approval.setApprovedAt(LocalDate.now());
        approval.setApprovedBy("system");

        productRequestApprovalRepository.save(approval);
    }

    private void createPurchaseOrderFromRequest(ProductRequest productRequest) {
        // 구매요청서의 아이템들을 조회
        List<ProductRequestItem> requestItems = productRequestItemRepository.findByProductRequestId(productRequest.getId());
        
        if (requestItems.isEmpty()) {
            return; // 아이템이 없으면 발주서 생성하지 않음
        }
        
        // 공급사별로 아이템들을 그룹핑
        Map<String, List<ProductRequestItem>> itemsBySupplier = new HashMap<>();
        
        for (ProductRequestItem requestItem : requestItems) {
            // Product를 통해 공급사 정보 조회
            Product product = productRepository.findById(requestItem.getProductId()).orElse(null);
            String supplierId = null;
            
            if (product != null && product.getSupplierCompany() != null) {
                supplierId = product.getSupplierCompany().getId();
            }
            
            // supplierId가 null인 경우 "UNKNOWN"으로 처리
            String supplierKey = supplierId;
            
            itemsBySupplier.computeIfAbsent(supplierKey, k -> new ArrayList<>()).add(requestItem);
        }
        
        // 각 공급사별로 별도의 발주서 생성
        for (Map.Entry<String, List<ProductRequestItem>> entry : itemsBySupplier.entrySet()) {
            List<ProductRequestItem> supplierItems = entry.getValue();

            // 해당 공급사의 총 금액 계산
            BigDecimal supplierTotalPrice = supplierItems.stream()
                    .map(item -> item.getPrice().multiply(item.getCount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 발주서 승인 정보 생성 (각 발주서마다 별도 생성)
            ProductOrderApproval orderApproval = ProductOrderApproval.builder()
                    .approvalStatus("PENDING")
                    .build();
            orderApproval = productOrderApprovalRepository.save(orderApproval);

            // 발주서 생성 (각 공급사별로)
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String poCode = "PO-" + uuid.substring(uuid.length() - 6);

            // 공급사명 설정 (첫 번째 아이템의 Product에서 가져옴)
            String supplierName = null;

            LocalDateTime dueDate = null;
            if (!supplierItems.isEmpty()) {
                Product firstProduct = productRepository.findById(supplierItems.get(0).getProductId()).orElse(null);
                if (firstProduct != null && firstProduct.getSupplierCompany() != null) {
                    supplierName = firstProduct.getSupplierCompany().getCompanyName();
                    int deliveryDays = Optional.ofNullable(firstProduct.getSupplierCompany().getDeliveryDays())
                            .orElse(4);
                    dueDate = LocalDateTime.now().plusDays(deliveryDays+1);
                }
            }

            ProductOrder productOrder = ProductOrder.builder()
                    .productOrderCode(poCode)
                    .productOrderType(productRequest.getProductRequestType())
                    .productRequestId(productRequest.getId())
                    .requesterId(productRequest.getRequesterId())
                    .supplierCompanyName(supplierName)
                    .approvalId(orderApproval)
                    .totalPrice(supplierTotalPrice)
                    .dueDate(dueDate)
                    .etc("구매요청서 " + productRequest.getProductRequestCode() + "에서 자동 생성 (공급사: " + (supplierName != null ? supplierName : "미지정") + ")")
                    .build();
            productOrder = productOrderRepository.save(productOrder);

            // 해당 공급사의 발주서 아이템들 생성
            for (ProductRequestItem requestItem : supplierItems) {
                ProductOrderItem orderItem = ProductOrderItem.builder()
                        .productOrderId(productOrder.getId())
                        .productId(requestItem.getProductId())
                        .count(requestItem.getCount())
                        .unit(requestItem.getUnit())
                        .price(requestItem.getPrice())
                        .build();
                productOrderItemRepository.save(orderItem);
            }
        }
    }
}
