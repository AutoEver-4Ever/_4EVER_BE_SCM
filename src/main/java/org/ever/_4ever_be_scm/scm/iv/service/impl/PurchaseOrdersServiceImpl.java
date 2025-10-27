package org.ever._4ever_be_scm.scm.iv.service.impl;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.exception.BusinessException;
import org.ever._4ever_be_scm.scm.iv.dto.PurchaseOrderDto;
import org.ever._4ever_be_scm.scm.iv.service.PurchaseOrdersService;
import org.ever._4ever_be_scm.scm.mm.entity.ProductOrder;
import org.ever._4ever_be_scm.scm.mm.repository.ProductOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.ever._4ever_be_scm.common.exception.ErrorCode.INVALID_STATUS;

/**
 * 구매 발주 관리 서비스 구현
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseOrdersServiceImpl implements PurchaseOrdersService {
    private final ProductOrderRepository productOrderRepository;
    /**
     * 입고 상태별 발주 목록 조회
     *
     * @param status   상태 (입고 대기: RECEIVING / 입고 완료: RECEIVED)
     * @param pageable 페이징 정보
     * @return 상태별 발주 목록 (Page 형태)
     */
    @Override
    public Page<PurchaseOrderDto> getPurchaseOrdersByStatus(String status, Pageable pageable) {

        // 상태 값 검증
        if (status == null || (!status.equalsIgnoreCase("RECEIVING") && !status.equalsIgnoreCase("RECEIVED"))) {
            throw new BusinessException(INVALID_STATUS);
        }

        // DB 조회 (페이징)
        Page<ProductOrder> productOrders = productOrderRepository.findByApprovalId_ApprovalStatus(status.toUpperCase(), pageable);

        // DTO 변환
        List<PurchaseOrderDto> purchaseOrderDtos = productOrders.getContent().stream()
                .map(po -> PurchaseOrderDto.builder()
                        .purchaseOrderId(po.getId())
                        .purchaseOrderCode(po.getProductOrderCode())
                        .supplier(po.getSupplierCompanyName())
                        .orderDate(po.getCreatedAt())
                        .dueDate(po.getDueDate())
                        .totalAmount(po.getTotalPrice())
                        .status(po.getApprovalId().getApprovalStatus())
                        .build())
                .collect(Collectors.toList());

        // Page 객체로 감싸서 반환
        return new PageImpl<>(purchaseOrderDtos, pageable, productOrders.getTotalElements());
    }
}

