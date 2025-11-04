package org.ever._4ever_be_scm.scm.pp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStock;
import org.ever._4ever_be_scm.scm.iv.repository.ProductRepository;
import org.ever._4ever_be_scm.scm.iv.repository.ProductStockRepository;
import org.ever._4ever_be_scm.scm.pp.dto.MesDetailResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.MesQueryResponseDto;
import org.ever._4ever_be_scm.scm.pp.entity.*;
import org.ever._4ever_be_scm.scm.pp.integration.dto.BusinessQuotationDto;
import org.ever._4ever_be_scm.scm.pp.integration.port.BusinessQuotationServicePort;
import org.ever._4ever_be_scm.scm.pp.repository.*;
import org.ever._4ever_be_scm.scm.pp.service.MesService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MesServiceImpl implements MesService {

    private final MesRepository mesRepository;
    private final MesOperationLogRepository mesOperationLogRepository;
    private final BomRepository bomRepository;
    private final BomItemRepository bomItemRepository;
    private final RoutingRepository routingRepository;
    private final OperationRepository operationRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final BusinessQuotationServicePort businessQuotationServicePort;

    @Override
    @Transactional(readOnly = true)
    public MesQueryResponseDto getMesList(String quotationId, String status, int page, int size) {
        log.info("MES 목록 조회: quotationId={}, status={}, page={}, size={}",
                quotationId, status, page, size);

        // 1. 조건에 맞는 MES 조회
        Page<Mes> mesPage = mesRepository.findWithFilters(quotationId, status, PageRequest.of(page, size));

        // 2. DTO 변환
        List<MesQueryResponseDto.MesItemDto> items = new ArrayList<>();

        for (Mes mes : mesPage.getContent()) {
            // 제품 정보 조회
            Product product = productRepository.findById(mes.getProductId()).orElse(null);
            String productName = product != null ? product.getProductName() : "알 수 없는 제품";
            String uomName = product != null ? product.getUnit() : "EA";

            // 견적 번호 조회
            String quotationNumber = null;
            try {
                BusinessQuotationDto quotation = businessQuotationServicePort.getQuotationById(mes.getQuotationId());
                quotationNumber = quotation != null ? quotation.getQuotationNumber() : mes.getQuotationId();
            } catch (Exception e) {
                quotationNumber = mes.getQuotationId();
            }

            // 공정 순서 조회
            List<MesOperationLog> operationLogs = mesOperationLogRepository
                    .findByMesIdOrderBySequenceAsc(mes.getId());

            List<String> sequence = new ArrayList<>();
            String currentOperation = null;

            for (MesOperationLog log : operationLogs) {
                Operation operation = operationRepository.findById(log.getOperationId()).orElse(null);
                if (operation != null) {
                    sequence.add(operation.getOpCode());

                    if ("IN_PROGRESS".equals(log.getStatus())) {
                        currentOperation = operation.getOpCode();
                    }
                }
            }

            // 현재 공정이 없고 PENDING 상태면 첫 공정
            if (currentOperation == null && !sequence.isEmpty() && "PENDING".equals(mes.getStatus())) {
                currentOperation = sequence.get(0);
            }

            MesQueryResponseDto.MesItemDto itemDto = MesQueryResponseDto.MesItemDto.builder()
                    .mesId(mes.getId())
                    .mesNumber(mes.getMesNumber())
                    .productId(mes.getProductId())
                    .productName(productName)
                    .quantity(mes.getQuantity())
                    .uomName(uomName)
                    .quotationId(mes.getQuotationId())
                    .quotationNumber(quotationNumber)
                    .status(mes.getStatus())
                    .currentOperation(currentOperation)
                    .startDate(mes.getStartDate())
                    .endDate(mes.getEndDate())
                    .progressRate(mes.getProgressRate())
                    .sequence(sequence)
                    .build();

            items.add(itemDto);
        }

        return MesQueryResponseDto.builder()
                .size(size)
                .totalPages(mesPage.getTotalPages())
                .page(page)
                .totalElements((int) mesPage.getTotalElements())
                .content(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MesDetailResponseDto getMesDetail(String mesId) {
        log.info("MES 상세 조회: mesId={}", mesId);

        // 1. MES 조회
        Mes mes = mesRepository.findById(mesId)
                .orElseThrow(() -> new RuntimeException("MES를 찾을 수 없습니다: " + mesId));

        // 2. 제품 정보 조회
        Product product = productRepository.findById(mes.getProductId()).orElse(null);
        String productName = product != null ? product.getProductName() : "알 수 없는 제품";
        String uomName = product != null ? product.getUnit() : "EA";

        // 3. 공정 로그 조회
        List<MesOperationLog> operationLogs = mesOperationLogRepository
                .findByMesIdOrderBySequenceAsc(mes.getId());

        List<MesDetailResponseDto.OperationDto> operations = new ArrayList<>();
        String currentOperation = null;

        for (MesOperationLog log : operationLogs) {
            Operation operation = operationRepository.findById(log.getOperationId()).orElse(null);
            if (operation == null) continue;

            // 시간 포맷팅 (HH:mm)
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            String startedAt = log.getStartedAt() != null ?
                    log.getStartedAt().format(timeFormatter) : null;
            String finishedAt = log.getFinishedAt() != null ?
                    log.getFinishedAt().format(timeFormatter) : null;

            // 매니저 정보 (주석 처리 - 추후 개발)
            // MesDetailResponseDto.ManagerDto manager = null;
            // if (log.getManagerId() != null) {
            //     manager = MesDetailResponseDto.ManagerDto.builder()
            //             .id(log.getManagerId())
            //             .name("매니저명")
            //             .build();
            // }

            MesDetailResponseDto.OperationDto operationDto = MesDetailResponseDto.OperationDto.builder()
                    .operationNumber(operation.getOpCode())
                    .operationName(operation.getOpName())
                    .sequence(log.getSequence())
                    .statusCode(log.getStatus())
                    .startedAt(startedAt)
                    .finishedAt(finishedAt)
                    .durationHours(log.getDurationHours())
                    .manager(null)  // 주석 처리
                    .build();

            operations.add(operationDto);

            if ("IN_PROGRESS".equals(log.getStatus())) {
                currentOperation = operation.getOpCode();
            }
        }

        // 4. Plan 정보
        MesDetailResponseDto.PlanDto plan = MesDetailResponseDto.PlanDto.builder()
                .startDate(mes.getStartDate())
                .dueDate(mes.getEndDate())
                .build();

        return MesDetailResponseDto.builder()
                .mesId(mes.getId())
                .mesNumber(mes.getMesNumber())
                .productId(mes.getProductId())
                .productName(productName)
                .quantity(mes.getQuantity())
                .uomName(uomName)
                .progressPercent(mes.getProgressRate())
                .statusCode(mes.getStatus())
                .plan(plan)
                .currentOperation(currentOperation)
                .operations(operations)
                .build();
    }

    @Override
    @Transactional
    public void startMes(String mesId) {
        log.info("MES 시작: mesId={}", mesId);

        // 1. MES 조회
        Mes mes = mesRepository.findById(mesId)
                .orElseThrow(() -> new RuntimeException("MES를 찾을 수 없습니다: " + mesId));

        if (!"PENDING".equals(mes.getStatus())) {
            throw new RuntimeException("PENDING 상태의 MES만 시작할 수 있습니다. 현재 상태: " + mes.getStatus());
        }

        // 2. 자재가 충분한지 검증 (중요!)
        validateMaterialsAvailability(mes);

        // 3. MES 상태를 IN_PROGRESS로 변경
        mes.setStatus("IN_PROGRESS");
        mesRepository.save(mes);

        // 4. 자재 소비 처리 (BOM의 모든 원자재 소비)
        consumeMaterials(mes);

        log.info("MES 시작 완료: mesId={}, status=IN_PROGRESS", mesId);
    }

    @Override
    @Transactional
    public void startOperation(String mesId, String operationId, String managerId) {
        log.info("공정 시작: mesId={}, operationId={}, managerId={}", mesId, operationId, managerId);

        // 1. MES 조회
        Mes mes = mesRepository.findById(mesId)
                .orElseThrow(() -> new RuntimeException("MES를 찾을 수 없습니다: " + mesId));

        // 2. MesOperationLog 조회
        List<MesOperationLog> operationLogs = mesOperationLogRepository.findByMesIdOrderBySequenceAsc(mesId);
        MesOperationLog targetLog = operationLogs.stream()
                .filter(log -> operationId.equals(log.getOperationId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("공정을 찾을 수 없습니다: " + operationId));

        if (!"PENDING".equals(targetLog.getStatus())) {
            throw new RuntimeException("PENDING 상태의 공정만 시작할 수 있습니다. 현재 상태: " + targetLog.getStatus());
        }

        // 3. 이전 공정들이 모두 완료되었는지 확인
        for (MesOperationLog log : operationLogs) {
            if (log.getSequence() < targetLog.getSequence() && !"COMPLETED".equals(log.getStatus())) {
                throw new RuntimeException("이전 공정이 완료되지 않았습니다. sequence: " + log.getSequence());
            }
        }

        // 4. 공정 시작
        targetLog.start(managerId);
        mesOperationLogRepository.save(targetLog);

        // 5. MES의 currentOperationId 업데이트
        mes.setCurrentOperationId(operationId);
        mesRepository.save(mes);

        log.info("공정 시작 완료: operationId={}, status=IN_PROGRESS", operationId);
    }

    @Override
    @Transactional
    public void completeOperation(String mesId, String operationId) {
        log.info("공정 완료: mesId={}, operationId={}", mesId, operationId);

        // 1. MES 조회
        Mes mes = mesRepository.findById(mesId)
                .orElseThrow(() -> new RuntimeException("MES를 찾을 수 없습니다: " + mesId));

        // 2. MesOperationLog 조회
        List<MesOperationLog> operationLogs = mesOperationLogRepository.findByMesIdOrderBySequenceAsc(mesId);
        MesOperationLog targetLog = operationLogs.stream()
                .filter(log -> operationId.equals(log.getOperationId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("공정을 찾을 수 없습니다: " + operationId));

        if (!"IN_PROGRESS".equals(targetLog.getStatus())) {
            throw new RuntimeException("IN_PROGRESS 상태의 공정만 완료할 수 있습니다. 현재 상태: " + targetLog.getStatus());
        }

        // 3. 공정 완료
        targetLog.complete();
        mesOperationLogRepository.save(targetLog);

        // 4. 진행률 계산 및 업데이트
        long completedCount = operationLogs.stream()
                .filter(log -> "COMPLETED".equals(log.getStatus()))
                .count();
        int progressRate = (int) ((completedCount * 100) / operationLogs.size());
        mes.setProgressRate(progressRate);

        // 5. 다음 공정이 있으면 currentOperationId 업데이트, 없으면 null
        MesOperationLog nextLog = operationLogs.stream()
                .filter(log -> log.getSequence() > targetLog.getSequence())
                .filter(log -> "PENDING".equals(log.getStatus()))
                .findFirst()
                .orElse(null);

        if (nextLog != null) {
            mes.setCurrentOperationId(nextLog.getOperationId());
        } else {
            mes.setCurrentOperationId(null);
        }

        mesRepository.save(mes);

        log.info("공정 완료: operationId={}, status=COMPLETED, progressRate={}%", operationId, progressRate);
    }

    @Override
    @Transactional
    public void completeMes(String mesId) {
        log.info("MES 완료: mesId={}", mesId);

        // 1. MES 조회
        Mes mes = mesRepository.findById(mesId)
                .orElseThrow(() -> new RuntimeException("MES를 찾을 수 없습니다: " + mesId));

        if (!"IN_PROGRESS".equals(mes.getStatus())) {
            throw new RuntimeException("IN_PROGRESS 상태의 MES만 완료할 수 있습니다. 현재 상태: " + mes.getStatus());
        }

        // 2. 모든 공정이 완료되었는지 확인
        List<MesOperationLog> operationLogs = mesOperationLogRepository.findByMesIdOrderBySequenceAsc(mesId);
        boolean allCompleted = operationLogs.stream()
                .allMatch(log -> "COMPLETED".equals(log.getStatus()));

        if (!allCompleted) {
            throw new RuntimeException("모든 공정이 완료되지 않았습니다.");
        }

        // 3. MES 상태를 COMPLETED로 변경
        mes.setStatus("COMPLETED");
        mes.setProgressRate(100);
        mesRepository.save(mes);

        // 4. 완제품 재고 증가
        increaseProductStock(mes);

        // /PPtodo: Business 서버로 견적 상태 업데이트 (출고준비완료)
        // String quotationId = mes.getQuotationId();
        // try {
        //     businessQuotationServicePort.updateQuotationStatus(quotationId, "READY_FOR_DELIVERY");
        //     log.info("견적 상태 업데이트 완료: quotationId={}, status=READY_FOR_DELIVERY", quotationId);
        // } catch (Exception e) {
        //     log.error("견적 상태 업데이트 실패: quotationId={}", quotationId, e);
        // }

        log.info("MES 완료: mesId={}, status=COMPLETED", mesId);
    }

    /**
     * 자재 가용성 검증 (MES 시작 전)
     */
    private void validateMaterialsAvailability(Mes mes) {
        log.info("자재 가용성 검증 시작: mesId={}, bomId={}", mes.getId(), mes.getBomId());

        // 1. BOM 조회
        Bom bom = bomRepository.findById(mes.getBomId()).orElse(null);
        if (bom == null) {
            log.warn("BOM을 찾을 수 없습니다: bomId={}", mes.getBomId());
            return;
        }

        // 2. BOM의 모든 원자재 검증 (재귀적)
        Set<String> processedProducts = new HashSet<>();
        List<String> shortageItems = new ArrayList<>();
        validateBomMaterials(bom, mes.getQuantity(), processedProducts, shortageItems);

        // 3. 부족한 자재가 있으면 에러 발생
        if (!shortageItems.isEmpty()) {
            String errorMessage = "자재가 부족하여 생산을 시작할 수 없습니다: " + String.join(", ", shortageItems);
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }

        log.info("자재 가용성 검증 완료: mesId={}, 모든 자재 충분", mes.getId());
    }

    /**
     * BOM 자재 재귀적 검증
     */
    private void validateBomMaterials(Bom bom, Integer quantity, Set<String> processedProducts, List<String> shortageItems) {
        List<BomItem> bomItems = bomItemRepository.findByBomId(bom.getId());

        for (BomItem bomItem : bomItems) {
            // 순환 참조 방지
            if (processedProducts.contains(bomItem.getComponentId())) {
                continue;
            }
            processedProducts.add(bomItem.getComponentId());

            BigDecimal requiredQuantity = bomItem.getCount().multiply(BigDecimal.valueOf(quantity));

            if ("ITEM".equals(bomItem.getComponentType())) {
                // 원자재: 재고 확인
                ProductStock stock = productStockRepository.findByProductId(bomItem.getComponentId()).orElse(null);

                if (stock == null) {
                    Product product = productRepository.findById(bomItem.getComponentId()).orElse(null);
                    String productName = product != null ? product.getProductName() : bomItem.getComponentId();
                    shortageItems.add(productName + " (재고 없음)");
                    log.warn("재고가 없습니다: productId={}, 필요량={}", bomItem.getComponentId(), requiredQuantity);
                    continue;
                }

                // 실제 재고(availableCount)가 충분한지 확인
                // 예약된 것이든 아니든, 물리적으로 존재하는지만 확인
                BigDecimal currentAvailable = stock.getAvailableCount() != null
                        ? stock.getAvailableCount()
                        : BigDecimal.ZERO;

                if (currentAvailable.compareTo(requiredQuantity) < 0) {
                    Product product = productRepository.findById(bomItem.getComponentId()).orElse(null);
                    String productName = product != null ? product.getProductName() : bomItem.getComponentId();
                    shortageItems.add(String.format("%s (필요: %s, 현재: %s)",
                            productName, requiredQuantity, currentAvailable));
                    log.warn("재고 부족: productId={}, 필요량={}, 현재재고={}",
                            bomItem.getComponentId(), requiredQuantity, currentAvailable);
                }

            } else if ("PRODUCT".equals(bomItem.getComponentType())) {
                // 부품: 하위 BOM 검증
                Bom childBom = bomRepository.findByProductId(bomItem.getComponentId()).orElse(null);
                if (childBom != null) {
                    validateBomMaterials(childBom, requiredQuantity.intValue(), processedProducts, shortageItems);
                }
            }
        }
    }

    /**
     * 자재 소비 처리 (MES 시작 시)
     */
    private void consumeMaterials(Mes mes) {
        log.info("자재 소비 시작: mesId={}, bomId={}", mes.getId(), mes.getBomId());

        // 1. BOM 조회
        Bom bom = bomRepository.findById(mes.getBomId()).orElse(null);
        if (bom == null) {
            log.warn("BOM을 찾을 수 없습니다: bomId={}", mes.getBomId());
            return;
        }

        // 2. BOM의 모든 원자재 소비 (재귀적)
        Set<String> processedProducts = new HashSet<>();
        consumeBomMaterials(bom, mes.getQuantity(), processedProducts);

        log.info("자재 소비 완료: mesId={}", mes.getId());
    }

    /**
     * BOM의 자재 재귀적 소비
     */
    private void consumeBomMaterials(Bom bom, Integer quantity, Set<String> processedProducts) {
        List<BomItem> bomItems = bomItemRepository.findByBomId(bom.getId());

        for (BomItem bomItem : bomItems) {
            // 순환 참조 방지
            if (processedProducts.contains(bomItem.getComponentId())) {
                continue;
            }
            processedProducts.add(bomItem.getComponentId());

            BigDecimal requiredQuantity = bomItem.getCount().multiply(BigDecimal.valueOf(quantity));

            if ("ITEM".equals(bomItem.getComponentType())) {
                // 원자재: 재고 소비
                consumeStock(bomItem.getComponentId(), requiredQuantity);

            } else if ("PRODUCT".equals(bomItem.getComponentType())) {
                // 부품: 하위 BOM 탐색
                Bom childBom = bomRepository.findByProductId(bomItem.getComponentId()).orElse(null);
                if (childBom != null) {
                    consumeBomMaterials(childBom, requiredQuantity.intValue(), processedProducts);
                }
            }
        }
    }

    /**
     * 재고 소비 (availableCount 감소, reservedCount 해제)
     */
    private void consumeStock(String productId, BigDecimal quantity) {
        ProductStock stock = productStockRepository.findByProductId(productId).orElse(null);

        if (stock == null) {
            log.warn("재고가 없습니다: productId={}", productId);
            return;
        }

        // consumeReservedStock 메서드 사용 (예약 해제 + 실제 재고 감소)
        stock.consumeReservedStock(quantity);
        productStockRepository.save(stock);

        log.info("재고 소비: productId={}, 소비량={}, 현재={}, 예약={}",
                productId, quantity, stock.getAvailableCount(), stock.getReservedCount());
    }

    /**
     * 완제품 재고 증가 (MES 완료 시)
     */
    private void increaseProductStock(Mes mes) {
        log.info("완제품 재고 증가: productId={}, quantity={}", mes.getProductId(), mes.getQuantity());

        ProductStock stock = productStockRepository.findByProductId(mes.getProductId()).orElse(null);

        if (stock == null) {
            // ProductStock이 없으면 생성
            log.warn("ProductStock이 없습니다. 새로 생성합니다: productId={}", mes.getProductId());

            throw new RuntimeException("재고가 없는 제품입니다.");
        } else {
            // 재고에 증가
            BigDecimal currentAvailable = stock.getAvailableCount() != null
                    ? stock.getAvailableCount()
                    : BigDecimal.ZERO;

            stock.setAvailableCount(currentAvailable.add(BigDecimal.valueOf(mes.getQuantity())));
            productStockRepository.save(stock);

            log.info("재고 증가 완료: productId={}, 이전={}, 증가={}, 현재={}",
                    mes.getProductId(), currentAvailable, mes.getQuantity(), stock.getAvailableCount());
        }
    }
}
