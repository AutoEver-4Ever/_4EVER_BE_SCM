package org.ever._4ever_be_scm.scm.pp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStock;
import org.ever._4ever_be_scm.scm.iv.repository.ProductRepository;
import org.ever._4ever_be_scm.scm.iv.repository.ProductStockRepository;
import org.ever._4ever_be_scm.scm.pp.dto.MrpRunConvertRequestDto;
import org.ever._4ever_be_scm.scm.pp.dto.MrpRunQueryResponseDto;
import org.ever._4ever_be_scm.scm.pp.entity.Mrp;
import org.ever._4ever_be_scm.scm.pp.entity.MrpRun;
import org.ever._4ever_be_scm.scm.pp.integration.dto.BusinessQuotationDto;
import org.ever._4ever_be_scm.scm.pp.integration.port.BusinessQuotationServicePort;
import org.ever._4ever_be_scm.scm.pp.repository.MrpRepository;
import org.ever._4ever_be_scm.scm.pp.repository.MrpRunRepository;
import org.ever._4ever_be_scm.scm.pp.service.MrpService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MrpServiceImpl implements MrpService {

    private final MrpRepository mrpRepository;
    private final MrpRunRepository mrpRunRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final BusinessQuotationServicePort businessQuotationServicePort;

    @Override
    @Transactional
    public void convertToMrpRun(MrpRunConvertRequestDto requestDto) {
        log.info("MRP → MRP_RUN 전환 시작: items={}", requestDto.getItems().size());

        for (MrpRunConvertRequestDto.MrpItemRequest item : requestDto.getItems()) {
            String productId = item.getItemId();
            BigDecimal quantity = item.getQuantity();

            // 1. 해당 원자재에 대한 MRP 조회 (가장 빠른 조달일 기준)
            List<Mrp> mrpList = mrpRepository.findAll().stream()
                    .filter(mrp -> productId.equals(mrp.getProductId()))
                    .sorted((m1, m2) -> {
                        if (m1.getProcurementStart() == null) return 1;
                        if (m2.getProcurementStart() == null) return -1;
                        return m1.getProcurementStart().compareTo(m2.getProcurementStart());
                    })
                    .collect(Collectors.toList());

            if (mrpList.isEmpty()) {
                log.warn("MRP를 찾을 수 없습니다: productId={}", productId);
                continue;
            }

            Mrp earliestMrp = mrpList.get(0);

            // 2. 견적 번호 조회
            String quotationId = earliestMrp.getQuotationId();
            String quotationNumber = null;
            try {
                BusinessQuotationDto quotation = businessQuotationServicePort.getQuotationById(quotationId);
                quotationNumber = quotation != null ? quotation.getQuotationNumber() : quotationId;
            } catch (Exception e) {
                log.warn("견적 정보 조회 실패: quotationId={}", quotationId);
                quotationNumber = quotationId;
            }

            // 3. MRP_RUN 생성
            MrpRun mrpRun = MrpRun.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .quotationId(quotationId)
                    .procurementStart(earliestMrp.getProcurementStart())
                    .expectedArrival(earliestMrp.getExpectedArrival())
                    .status("PENDING")  // 초기 상태는 PENDING
                    .build();

            mrpRunRepository.save(mrpRun);

            log.info("MRP_RUN 생성 완료: productId={}, quantity={}, status=PENDING",
                    productId, quantity);
        }

        log.info("MRP → MRP_RUN 전환 완료");
    }

    @Override
    @Transactional(readOnly = true)
    public MrpRunQueryResponseDto getMrpRunList(String status, int page, int size) {
        log.info("MRP 계획주문 목록 조회: status={}, page={}, size={}", status, page, size);

        // 1. 상태 필터링
        Page<MrpRun> mrpRunPage;
        if (status == null || "ALL".equalsIgnoreCase(status)) {
            mrpRunPage = mrpRunRepository.findAll(PageRequest.of(page, size));
        } else {
            mrpRunPage = mrpRunRepository.findByStatus(status, PageRequest.of(page, size));
        }

        // 2. DTO 변환
        List<MrpRunQueryResponseDto.MrpRunItemDto> items = new ArrayList<>();

        for (MrpRun mrpRun : mrpRunPage.getContent()) {
            // 제품 정보 조회
            Product product = productRepository.findById(mrpRun.getProductId()).orElse(null);
            String itemName = product != null ? product.getProductName() : "알 수 없는 제품";

            // 견적 번호 조회
            String quotationNumber = null;
            try {
                BusinessQuotationDto quotation = businessQuotationServicePort.getQuotationById(mrpRun.getQuotationId());
                quotationNumber = quotation != null ? quotation.getQuotationNumber() : mrpRun.getQuotationId();
            } catch (Exception e) {
                quotationNumber = mrpRun.getQuotationId();
            }

            MrpRunQueryResponseDto.MrpRunItemDto itemDto = MrpRunQueryResponseDto.MrpRunItemDto.builder()
                    .mrpRunId(mrpRun.getId())
                    .quotationNumber(quotationNumber)
                    .itemName(itemName)
                    .quantity(mrpRun.getQuantity())
                    .status(mrpRun.getStatus())
                    .procurementStartDate(mrpRun.getProcurementStart())
                    .expectedArrivalDate(mrpRun.getExpectedArrival())
                    .build();

            items.add(itemDto);
        }

        // 3. 페이지 정보
        MrpRunQueryResponseDto.PageInfo pageInfo = MrpRunQueryResponseDto.PageInfo.builder()
                .number(page)
                .size(size)
                .totalElements((int) mrpRunPage.getTotalElements())
                .totalPages(mrpRunPage.getTotalPages())
                .hasNext(mrpRunPage.hasNext())
                .build();

        return MrpRunQueryResponseDto.builder()
                .page(pageInfo)
                .content(items)
                .build();
    }

    @Override
    @Transactional
    public void approveMrpRun(String mrpRunId) {
        log.info("MRP 계획주문 승인: mrpRunId={}", mrpRunId);

        MrpRun mrpRun = mrpRunRepository.findById(mrpRunId)
                .orElseThrow(() -> new RuntimeException("MRP_RUN을 찾을 수 없습니다: " + mrpRunId));

        if (!"PENDING".equals(mrpRun.getStatus())) {
            throw new RuntimeException("PENDING 상태의 계획주문만 승인할 수 있습니다. 현재 상태: " + mrpRun.getStatus());
        }

        mrpRun.setStatus("APPROVAL");
        mrpRunRepository.save(mrpRun);

        log.info("MRP 계획주문 승인 완료: mrpRunId={}, status=APPROVAL", mrpRunId);
    }

    @Override
    @Transactional
    public void rejectMrpRun(String mrpRunId) {
        log.info("MRP 계획주문 거부: mrpRunId={}", mrpRunId);

        MrpRun mrpRun = mrpRunRepository.findById(mrpRunId)
                .orElseThrow(() -> new RuntimeException("MRP_RUN을 찾을 수 없습니다: " + mrpRunId));

        if (!"PENDING".equals(mrpRun.getStatus())) {
            throw new RuntimeException("PENDING 상태의 계획주문만 거부할 수 있습니다. 현재 상태: " + mrpRun.getStatus());
        }

        mrpRun.setStatus("REJECTED");
        mrpRunRepository.save(mrpRun);

        log.info("MRP 계획주문 거부 완료: mrpRunId={}, status=REJECTED", mrpRunId);
    }

    @Override
    @Transactional
    public void receiveMrpRun(String mrpRunId) {
        log.info("MRP 계획주문 입고 처리: mrpRunId={}", mrpRunId);

        MrpRun mrpRun = mrpRunRepository.findById(mrpRunId)
                .orElseThrow(() -> new RuntimeException("MRP_RUN을 찾을 수 없습니다: " + mrpRunId));

        if (!"APPROVAL".equals(mrpRun.getStatus())) {
            throw new RuntimeException("APPROVAL 상태의 계획주문만 입고할 수 있습니다. 현재 상태: " + mrpRun.getStatus());
        }

        // 1. MRP_RUN 상태를 COMPLETED로 변경
        mrpRun.setStatus("COMPLETED");
        mrpRunRepository.save(mrpRun);

        // 2. 재고 자동 증가
        String productId = mrpRun.getProductId();
        BigDecimal quantity = mrpRun.getQuantity();

        ProductStock stock = productStockRepository.findByProductId(productId).orElse(null);

        if (stock == null) {
            // ProductStock이 없으면 생성 (기본 창고에)
            log.warn("ProductStock이 없습니다. 새로 생성합니다: productId={}", productId);

            throw new RuntimeException("재고가 없는 제품입니다.");
        } else {
            // 재고에 증가
            BigDecimal currentAvailable = stock.getAvailableCount() != null
                    ? stock.getAvailableCount()
                    : BigDecimal.ZERO;

            stock.setAvailableCount(currentAvailable.add(quantity));
            productStockRepository.save(stock);

            log.info("재고 증가 완료: productId={}, 이전={}, 증가={}, 현재={}",
                    productId, currentAvailable, quantity, stock.getAvailableCount());
        }

        log.info("MRP 계획주문 입고 완료: mrpRunId={}, status=COMPLETED", mrpRunId);
    }
}
