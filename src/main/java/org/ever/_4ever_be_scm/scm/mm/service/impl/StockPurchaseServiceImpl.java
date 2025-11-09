package org.ever._4ever_be_scm.scm.mm.service.impl;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierCompany;
import org.ever._4ever_be_scm.scm.iv.repository.ProductRepository;
import org.ever._4ever_be_scm.scm.mm.dto.StockPurchaseRequestDto;
import org.ever._4ever_be_scm.scm.mm.entity.ProductRequest;
import org.ever._4ever_be_scm.scm.mm.entity.ProductRequestApproval;
import org.ever._4ever_be_scm.scm.mm.entity.ProductRequestItem;
import org.ever._4ever_be_scm.scm.mm.repository.ProductRequestApprovalRepository;
import org.ever._4ever_be_scm.scm.mm.repository.ProductRequestItemRepository;
import org.ever._4ever_be_scm.scm.mm.repository.ProductRequestRepository;
import org.ever._4ever_be_scm.scm.mm.service.StockPurchaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StockPurchaseServiceImpl implements StockPurchaseService {

    private final ProductRepository productRepository;
    private final ProductRequestRepository productRequestRepository;
    private final ProductRequestApprovalRepository productRequestApprovalRepository;
    private final ProductRequestItemRepository productRequestItemRepository;
    private final org.ever._4ever_be_scm.scm.pp.repository.MrpRunRepository mrpRunRepository;

    @Override
    public String createStockPurchaseRequest(StockPurchaseRequestDto requestDto, String requesterId) {
    if (requestDto.getItems() == null || requestDto.getItems().isEmpty()) {
        throw new IllegalArgumentException("요청할 제품 목록이 없습니다.");
    }

    // 1. ProductRequestApproval 생성
    ProductRequestApproval approval = ProductRequestApproval.builder()
        .approvalStatus("PENDING")
        .build();
    productRequestApprovalRepository.save(approval);

    // 2. ProductRequest 생성 (하나의 요청서에 다수 아이템 포함)
    String uuid = UUID.randomUUID().toString().replace("-", "");
    String prCode = "PR-" + uuid.substring(uuid.length() - 6);
    ProductRequest productRequest = ProductRequest.builder()
        .productRequestCode(prCode)
        .productRequestType("STOCK")
        .requesterId(requesterId)
        .approvalId(approval)
        .build();
    productRequestRepository.save(productRequest);

    // 3. 각 아이템에 대해 Product 검사 및 ProductRequestItem 생성
    BigDecimal totalPrice = BigDecimal.ZERO;

    for (StockPurchaseRequestDto.Item it : requestDto.getItems()) {
        Product product = productRepository.findById(it.getProductId())
            .orElseThrow(() -> new RuntimeException("제품을 찾을 수 없습니다: " + it.getProductId()));

        if (!"MATERIAL".equals(product.getCategory())) {
        throw new RuntimeException("재고성 제품이 아닙니다: " + it.getProductId());
        }

        SupplierCompany supplierCompany = product.getSupplierCompany();
        if (supplierCompany == null) {
        throw new RuntimeException("제품에 공급업체 정보가 없습니다: " + it.getProductId());
        }

        // 가격 계산 (단가 * 수량)
        BigDecimal itemPrice = product.getOriginPrice();
        BigDecimal itemTotalPrice = itemPrice.multiply(it.getQuantity());
        totalPrice = totalPrice.add(itemTotalPrice);

        ProductRequestItem requestItem = ProductRequestItem.builder()
            .productRequestId(productRequest.getId())
            .productId(product.getId())
            .unit(product.getUnit())
            .count(it.getQuantity())
            .unit(product.getUnit())
            .price(product.getOriginPrice())
            .preferredDeliveryDate(LocalDateTime.now().plusSeconds(
                supplierCompany.getDeliveryDays() != null ? supplierCompany.getDeliveryDays().getSeconds() : 4L * 86_400))
            .build();
        productRequestItemRepository.save(requestItem);

        // MRP Run 상태 업데이트 (mrpRunId가 있는 경우만)
        if (it.getMrpRunId() != null) {
            org.ever._4ever_be_scm.scm.pp.entity.MrpRun mrpRun = mrpRunRepository.findById(it.getMrpRunId())
                .orElseThrow(() -> new RuntimeException("MRP Run을 찾을 수 없습니다: " + it.getMrpRunId()));

            // 상태 검증
            if (!"INITIAL".equals(mrpRun.getStatus())) {
                throw new RuntimeException("이미 구매요청이 생성된 MRP Run입니다: " + it.getMrpRunId());
            }

            mrpRun.setStatus("PENDING");  // 구매요청 생성됨, 승인 대기
            mrpRunRepository.save(mrpRun);
        }
    }

    // 4. 계산된 totalPrice를 ProductRequest에 설정
    productRequest.setTotalPrice(totalPrice);
    productRequestRepository.save(productRequest);

    return productRequest.getId();
    }
}
