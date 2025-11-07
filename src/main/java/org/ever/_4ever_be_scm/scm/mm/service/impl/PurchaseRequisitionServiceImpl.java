package org.ever._4ever_be_scm.scm.mm.service.impl;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierCompany;
import org.ever._4ever_be_scm.scm.iv.repository.ProductRepository;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierCompanyRepository;
import org.ever._4ever_be_scm.scm.mm.dto.*;
import org.ever._4ever_be_scm.scm.mm.entity.*;
import org.ever._4ever_be_scm.scm.mm.integration.dto.InternalUserResponseDto;
import org.ever._4ever_be_scm.scm.mm.integration.port.InternalUserServicePort;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final InternalUserServicePort internalUserServicePort;
    private final org.ever._4ever_be_scm.scm.pp.repository.MrpRunRepository mrpRunRepository;

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

        // 모든 구매요청서 조회
        List<ProductRequest> allRequests = productRequestRepository.findAll();
        
        // 모든 요청자 ID 수집 (중복 제거)
        List<String> requesterIds = allRequests.stream()
                .map(ProductRequest::getRequesterId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        
        // 한 번에 모든 사용자 정보 조회
        Map<String, InternalUserResponseDto> userInfoMap = new HashMap<>();
        if (!requesterIds.isEmpty()) {
            try {
                List<InternalUserResponseDto> userInfos = internalUserServicePort.getInternalUserInfosByIds(requesterIds);

                if (userInfos != null && !userInfos.isEmpty()) {
                    userInfoMap = userInfos.stream()
                            .collect(Collectors.toMap(
                                    InternalUserResponseDto::getUserId,
                                    user -> user
                            ));
            }
            } catch (Exception e) {
                // 외부 서비스 호출 실패 시 빈 맵으로 처리
                userInfoMap = new HashMap<>();
            }
        }
        
        final Map<String, InternalUserResponseDto> finalUserInfoMap = userInfoMap;

        // 조건에 따른 필터링
        List<ProductRequest> filteredRequests = allRequests.stream()
                .filter(request -> {
                    // 상태 필터링
                    if (statusCode != null && !"ALL".equalsIgnoreCase(statusCode)) {
                        if (request.getApprovalId().getApprovalStatus() == null || !request.getApprovalId().getApprovalStatus().equalsIgnoreCase(statusCode)) {
                            return false;
                        }
                    }
                    
                    // 날짜 범위 필터링
                    if (startDateTime != null && request.getCreatedAt().isBefore(startDateTime)) {
                        return false;
                    }
                    if (endDateTime != null && request.getCreatedAt().isAfter(endDateTime)) {
                        return false;
                    }
                    
                    // Type 기반 키워드 검색
                    String type = searchVo.getType();
                    String keyword = searchVo.getKeyword();
                    if (keyword != null && !keyword.isEmpty()) {
                        if ("requesterName".equalsIgnoreCase(type)) {
                            // 외부에서 받아온 사용자 정보에서 이름 검색
                            var userInfo = finalUserInfoMap.get(request.getRequesterId());
                            if (userInfo == null || userInfo.getName() == null ||
                                !userInfo.getName().toLowerCase().contains(keyword.toLowerCase())) {
                                return false;
                            }
                        } else if ("departmentName".equalsIgnoreCase(type)) {
                            // 외부에서 받아온 사용자 정보에서 부서명 검색
                            var userInfo = finalUserInfoMap.get(request.getRequesterId());
                            if (userInfo == null || userInfo.getDepartmentName() == null || 
                                !userInfo.getDepartmentName().toLowerCase().contains(keyword.toLowerCase())) {
                                return false;
                            }
                        } else if ("productRequestNumber".equalsIgnoreCase(type)) {
                            if (request.getProductRequestCode() == null || 
                                !request.getProductRequestCode().toLowerCase().contains(keyword.toLowerCase())) {
                                return false;
                            }
                        }
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
                .map(request -> {
                    var userInfo = finalUserInfoMap.get(request.getRequesterId());
                    return PurchaseRequisitionListResponseDto.builder()
                            .purchaseRequisitionId(request.getId())
                            .purchaseRequisitionNumber(request.getProductRequestCode())
                            .requesterId(request.getRequesterId())
                            .requesterName(userInfo != null ? userInfo.getName() : "알 수 없음")
                            .requestDate(request.getCreatedAt())
                            .departmentId(userInfo != null ? userInfo.getDepartmentId() : null)
                            .departmentName(userInfo != null ? userInfo.getDepartmentName() : null)
                            .totalAmount(request.getTotalPrice())
                            .statusCode(request.getApprovalId().getApprovalStatus())
                            .build();
                })
                .toList();
                
        return new PageImpl<>(responseDtos, pageable, filteredRequests.size());
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseRequisitionDetailResponseDto getPurchaseRequisitionDetail(String purchaseRequisitionId) {
        ProductRequest productRequest = productRequestRepository.findById(purchaseRequisitionId)
                .orElseThrow(() -> new IllegalArgumentException("구매요청서를 찾을 수 없습니다."));
        
        List<ProductRequestItem> items = productRequestItemRepository.findByProductRequestId(purchaseRequisitionId);

        InternalUserResponseDto userInfo = internalUserServicePort.getInternalUserInfoById(productRequest.getRequesterId());

        String statusCode = productRequest.getApprovalId().getApprovalStatus();

        List<PurchaseRequisitionDetailResponseDto.ItemDto> itemDtos = new ArrayList<>();
        for (ProductRequestItem item : items) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);

            String productName = "";
            if (product != null && product.getProductName() != null) {
                productName = product.getProductName();
            }

            itemDtos.add(PurchaseRequisitionDetailResponseDto.ItemDto.builder()
                    .itemId(item.getProductId())
                    .itemName(productName)
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
                .requesterName(userInfo.getName())
                .departmentId(userInfo.getDepartmentId()) // Mock 데이터
                .departmentName(userInfo.getDepartmentName())
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
    public void approvePurchaseRequisition(String purchaseRequisitionId,String requesterId) {
        ProductRequest productRequest = productRequestRepository.findById(purchaseRequisitionId)
                .orElseThrow(() -> new IllegalArgumentException("구매요청서를 찾을 수 없습니다."));

        // 1. 승인 상태 변경
        ProductRequestApproval approval = productRequest.getApprovalId();

        ProductRequestApproval updatedApproval = approval.toBuilder()
                .approvalStatus("APPROVAL")
                .approvedAt(LocalDateTime.now())
                .approvedBy(requesterId)
                .build();

        productRequestApprovalRepository.save(updatedApproval);

        // 2. ✅ MRP Run 상태 업데이트 (mrpRunId가 있는 경우만)
        List<ProductRequestItem> items = productRequestItemRepository.findByProductRequestId(purchaseRequisitionId);
        for (ProductRequestItem item : items) {
            if (item.getMrpRunId() != null) {
                org.ever._4ever_be_scm.scm.pp.entity.MrpRun mrpRun = mrpRunRepository.findById(item.getMrpRunId())
                    .orElseThrow(() -> new RuntimeException("MRP Run을 찾을 수 없습니다: " + item.getMrpRunId()));
                mrpRun.setStatus("REQUEST_APPROVED");  // 구매요청 승인됨
                mrpRunRepository.save(mrpRun);
            }
        }

        // 3. 발주서 자동 생성
        createPurchaseOrderFromRequest(productRequest);
    }

    @Override
    @Transactional
    public void rejectPurchaseRequisition(String purchaseRequisitionId, PurchaseRequisitionRejectRequestDto requestDto, String requesterId) {
        ProductRequest productRequest = productRequestRepository.findById(purchaseRequisitionId)
                .orElseThrow(() -> new IllegalArgumentException("구매요청서를 찾을 수 없습니다."));

        ProductRequestApproval approval = productRequest.getApprovalId();

        //빌더를 이용해 새 객체 생성 (기존 값 유지하면서 일부 필드만 변경)
        ProductRequestApproval updatedApproval = approval.toBuilder()
                .approvalStatus("REJECTED")
                .rejectedReason(requestDto.getComment())
                .approvedAt(LocalDateTime.now())
                .approvedBy(requesterId)
                .build();

        productRequestApprovalRepository.save(updatedApproval);
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
                    long deliverySeconds = Optional.ofNullable(firstProduct.getSupplierCompany().getDeliveryDays())
                            .map(java.time.Duration::getSeconds)
                            .orElse(4L * 86_400);
                    dueDate = LocalDateTime.now().plusSeconds(deliverySeconds).plusDays(1);
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
                        .mrpRunId(requestItem.getMrpRunId())  // ✅ MRP Run ID 전달 (nullable)
                        .build();
                productOrderItemRepository.save(orderItem);
            }
        }
    }
}
