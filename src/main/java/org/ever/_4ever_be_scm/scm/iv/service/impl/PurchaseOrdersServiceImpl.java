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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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
     * 입고 준비 목록 조회 (RECEIVING 상태)
     * 
     * @param pageable 페이징 정보
     * @return 입고 준비 발주 목록
     */
    @Override
    public Page<PurchaseOrderDto> getReceivingPurchaseOrders(Pageable pageable) {
        // DB 조회 (페이징) - DELIVERING 상태만
        Page<ProductOrder> productOrders = productOrderRepository.findByApprovalId_ApprovalStatus("DELIVERING", pageable);

        // DTO 변환
        List<PurchaseOrderDto> purchaseOrderDtos = productOrders.getContent().stream()
                .map(po -> PurchaseOrderDto.builder()
                        .purchaseOrderId(po.getId())
                        .purchaseOrderNumber(po.getProductOrderCode())
                        .supplierCompanyName(po.getSupplierCompanyName())
                        .orderDate(po.getCreatedAt())
                        .dueDate(po.getDueDate())
                        .totalAmount(po.getTotalPrice())
                        .statusCode("RECEIVING")
                        .build())
                .collect(Collectors.toList());

        // Page 객체로 감싸서 반환
        return new PageImpl<>(purchaseOrderDtos, pageable, productOrders.getTotalElements());
    }
    
    /**
     * 입고 완료 목록 조회 (RECEIVED 상태) - 날짜 필터링 포함
     *
     * @param pageable 페이징 정보
     * @param startDate 시작일 (선택사항)
     * @param endDate 종료일 (선택사항)
     * @return 입고 완료 발주 목록
     */
    @Override
    public Page<PurchaseOrderDto> getReceivedPurchaseOrders(Pageable pageable, LocalDate startDate, LocalDate endDate) {
        Page<ProductOrder> productOrders;

        // 날짜 필터링 조건에 따라 다른 쿼리 실행
        if (startDate != null && endDate != null) {
            // 시작일과 종료일이 모두 있는 경우 - approval의 updatedAt 기준 필터링
            // LocalDate를 LocalDateTime으로 변환 (시작일은 00:00:00, 종료일은 23:59:59)
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            productOrders = productOrderRepository.findByApprovalId_ApprovalStatusAndApprovalId_UpdatedAtBetween(
                    "DELIVERED", startDateTime, endDateTime, pageable);
        } else {
            // 날짜 필터가 없는 경우 - DELIVERED 상태만 조회
            productOrders = productOrderRepository.findByApprovalId_ApprovalStatus("DELIVERED", pageable);
        }

        // DTO 변환
        List<PurchaseOrderDto> purchaseOrderDtos = productOrders.getContent().stream()
                .map(po -> PurchaseOrderDto.builder()
                        .purchaseOrderId(po.getId())
                        .purchaseOrderNumber(po.getProductOrderCode())
                        .supplierCompanyName(po.getSupplierCompanyName())
                        .orderDate(po.getCreatedAt())
                        .dueDate(po.getDueDate())
                        .totalAmount(po.getTotalPrice())
                        .statusCode("RECEIVED")
                        .build())
                .collect(Collectors.toList());

        // Page 객체로 감싸서 반환
        return new PageImpl<>(purchaseOrderDtos, pageable, productOrders.getTotalElements());
    }
}

