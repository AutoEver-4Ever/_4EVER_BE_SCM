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
            String quotationId = item.getQuotationId();
            String productId = item.getItemId();
            BigDecimal quantity = item.getQuantity();

            // 1. 해당 견적+원자재의 MRP 조회
            List<Mrp> mrpList = mrpRepository.findByQuotationIdAndProductId(quotationId, productId);

            if (mrpList.isEmpty()) {
                log.warn("MRP를 찾을 수 없습니다: quotationId={}, productId={}", quotationId, productId);
                continue;
            }

            Mrp mrp = mrpList.get(0);  // quotationId + productId 조합은 유일

            // 2. 중복 체크: 이미 convert된 MRP인지 확인
            List<MrpRun> existingRuns = mrpRunRepository.findByMrpId(mrp.getId());
            if (!existingRuns.isEmpty()) {
                log.warn("이미 구매 요청으로 전환된 MRP입니다: mrpId={}, existingMrpRunId={}",
                    mrp.getId(), existingRuns.get(0).getId());
                throw new RuntimeException("이미 구매 요청으로 전환된 MRP입니다: " + mrp.getId());
            }

            // 3. 견적 번호 조회 (표시용)
            String quotationNumber = null;
            try {
                BusinessQuotationDto quotation = businessQuotationServicePort.getQuotationById(quotationId);
                quotationNumber = quotation != null ? quotation.getQuotationNumber() : quotationId;
            } catch (Exception e) {
                log.warn("견적 정보 조회 실패: quotationId={}", quotationId);
                quotationNumber = quotationId;
            }

            // 4. MRP_RUN 생성
            MrpRun mrpRun = MrpRun.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .quotationId(quotationId)
                    .procurementStart(mrp.getProcurementStart())
                    .expectedArrival(mrp.getExpectedArrival())
                    .mrpId(mrp.getId())   // MRP ID 저장
                    .status("INITIAL")    // 초기 상태는 INITIAL (구매요청 생성 전)
                    .build();

            mrpRunRepository.save(mrpRun);

            log.info("MRP_RUN 생성 완료: mrpId={}, quotationId={}, productId={}, quantity={}, status=INITIAL",
                    mrp.getId(), quotationId, productId, quantity);
        }

        log.info("MRP → MRP_RUN 전환 완료");
    }

    @Override
    @Transactional(readOnly = true)
    public MrpRunQueryResponseDto getMrpRunList(String status, String quotationId, int page, int size) {
        log.info("MRP 계획주문 목록 조회: status={}, quotationId={}, page={}, size={}", status, quotationId, page, size);

        // 1. 상태 및 견적 필터링
        Page<MrpRun> mrpRunPage;
        if (quotationId != null && !quotationId.isEmpty()) {
            // quotationId로 필터링
            List<MrpRun> filtered = mrpRunRepository.findByQuotationId(quotationId);

            // status 추가 필터링
            if (status != null && !"ALL".equalsIgnoreCase(status)) {
                filtered = filtered.stream()
                    .filter(run -> status.equals(run.getStatus()))
                    .toList();
            }

            // 페이징 처리
            int start = page * size;
            int end = Math.min(start + size, filtered.size());
            List<MrpRun> pagedContent = start < filtered.size() ? filtered.subList(start, end) : new ArrayList<>();
            mrpRunPage = new org.springframework.data.domain.PageImpl<>(
                pagedContent,
                PageRequest.of(page, size),
                filtered.size()
            );
        } else if (status == null || "ALL".equalsIgnoreCase(status)) {
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
            String itemId = product != null ?product.getId() : "없는 제품";

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
                    .itemId(itemId)
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
    @Transactional(readOnly = true)
    public List<org.ever._4ever_be_scm.scm.mm.dto.ToggleCodeLabelDto> getMrpRunQuotationList() {
        log.info("MRP Run 견적 목록 조회");

        // 1. MRP Run에 존재하는 모든 quotationId 조회 (중복 제거)
        List<String> quotationIds = mrpRunRepository.findAll().stream()
            .map(MrpRun::getQuotationId)
            .distinct()
            .toList();

        // 2. 각 quotationId에 대해 quotationNumber 조회
        List<org.ever._4ever_be_scm.scm.mm.dto.ToggleCodeLabelDto> result = new ArrayList<>();
        for (String quotationId : quotationIds) {
            String quotationNumber = quotationId;
            try {
                BusinessQuotationDto quotation = businessQuotationServicePort.getQuotationById(quotationId);
                if (quotation != null && quotation.getQuotationNumber() != null) {
                    quotationNumber = quotation.getQuotationNumber();
                }
            } catch (Exception e) {
                log.warn("견적 정보 조회 실패: quotationId={}", quotationId);
            }

            result.add(new org.ever._4ever_be_scm.scm.mm.dto.ToggleCodeLabelDto(quotationNumber, quotationId));
        }

        return result;
    }
}
