package org.ever._4ever_be_scm.scm.pp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.infrastructure.kafka.config.KafkaTopicConfig;
import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStock;
import org.ever._4ever_be_scm.scm.iv.repository.ProductRepository;
import org.ever._4ever_be_scm.scm.iv.repository.ProductStockRepository;
import org.ever._4ever_be_scm.scm.mm.integration.dto.InternalUserResponseDto;
import org.ever._4ever_be_scm.scm.mm.integration.port.InternalUserServicePort;
import org.ever._4ever_be_scm.scm.pp.dto.MesDetailResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.MesQueryResponseDto;
import org.ever._4ever_be_scm.scm.pp.entity.*;
import org.ever._4ever_be_scm.scm.pp.integration.dto.BusinessQuotationDto;
import org.ever._4ever_be_scm.scm.pp.integration.port.BusinessQuotationServicePort;
import org.ever._4ever_be_scm.scm.pp.repository.*;
import org.ever._4ever_be_scm.scm.pp.service.MesService;
import org.ever.event.MesCompleteEvent;
import org.ever.event.MesStartEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.async.DeferredResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class MesServiceImpl implements MesService {

    private final MesRepository mesRepository;
    private final MesOperationLogRepository mesOperationLogRepository;
    private final BomRepository bomRepository;
    private final BomItemRepository bomItemRepository;
    private final OperationRepository operationRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final MrpRepository mrpRepository;
    private final MrpRunRepository mrpRunRepository;
    private final BusinessQuotationServicePort businessQuotationServicePort;
    private final org.ever._4ever_be_scm.infrastructure.kafka.producer.KafkaProducerService kafkaProducerService;
    private final org.ever._4ever_be_scm.common.async.GenericAsyncResultManager<Void> asyncResultManager;
    private final InternalUserServicePort internalUserServicePort;

    @Override
    @Transactional(readOnly = true)
    public Page<MesQueryResponseDto.MesItemDto> getMesList(String quotationId, String status, Pageable pageable) {
        log.info("MES 목록 조회: quotationId={}, status={}, page={}, size={}",
                quotationId, status, pageable.getPageNumber(), pageable.getPageSize());

        // 1. 조건에 맞는 MES 조회
        Page<Mes> mesPage = mesRepository.findWithFilters(quotationId, status, pageable);

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
            Integer currentOperation = null;
            boolean hasInProgress = false;
            boolean allCompleted = true;

            for (MesOperationLog log : operationLogs) {
                Operation operation = operationRepository.findById(log.getOperationId()).orElse(null);
                if (operation != null) {
                    sequence.add(operation.getOpCode());

                    // 진행중인 공정이 있으면 그 공정의 sequence 번호 저장
                    if ("IN_PROGRESS".equals(log.getStatus())) {
                        currentOperation = log.getSequence();
                        hasInProgress = true;
                    }

                    // 완료되지 않은 공정이 있으면 allCompleted = false
                    if (!"COMPLETED".equals(log.getStatus())) {
                        allCompleted = false;
                    }
                }
            }

            // 진행중인 공정이 없으면 상태에 따라 결정
            if (!hasInProgress) {
                if (allCompleted && !operationLogs.isEmpty()) {
                    // 모두 완료되면 0
                    currentOperation = 0;
                } else {
                    // 모두 대기중이거나 일부 대기중이면 1
                    currentOperation = 1;
                }
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

        return new PageImpl<>(items, pageable, mesPage.getTotalElements());
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

//             매니저 정보 (주석 처리 - 추후 개발)
             MesDetailResponseDto.ManagerDto manager = null;
             if (log.getManagerId() != null) {
                 InternalUserResponseDto internalUser = internalUserServicePort.getInternalUserInfoById(log.getManagerId());
                 manager = MesDetailResponseDto.ManagerDto.builder()
                         .id(log.getManagerId())
                         .name(internalUser.getName())
                         .build();
             }

            MesDetailResponseDto.OperationDto operationDto = MesDetailResponseDto.OperationDto.builder()
                    .mesOperationLogId(log.getId())  // MesOperationLog의 ID 반환
                    .operationNumber(operation.getOpCode())
                    .operationName(operation.getOpName())
                    .sequence(log.getSequence())
                    .statusCode(log.getStatus())
                    .startedAt(startedAt)
                    .finishedAt(finishedAt)
                    .durationHours(log.getDurationHours())
                    .manager(manager)  // 주석 처리
                    .build();

            operations.add(operationDto);

            if ("IN_PROGRESS".equals(log.getStatus())) {
                currentOperation = operation.getOpCode();
            }
        }

        // 3-1. 각 공정의 버튼 활성화 여부 계산
        for (int i = 0; i < operations.size(); i++) {
            MesDetailResponseDto.OperationDto operation = operations.get(i);
            String status = operation.getStatusCode();

            // 공정 시작 버튼 활성화 조건
            boolean canStart = false;
            if ("PENDING".equals(status)) {
                if (i == 0) {
                    // 첫 번째 공정: MES가 IN_PROGRESS이면 시작 가능
                    canStart = "IN_PROGRESS".equals(mes.getStatus());
                } else {
                    // 나머지 공정: 이전 공정이 COMPLETED이면 시작 가능
                    MesDetailResponseDto.OperationDto prevOperation = operations.get(i - 1);
                    canStart = "COMPLETED".equals(prevOperation.getStatusCode());
                }
            }

            // 공정 완료 버튼 활성화 조건
            boolean canComplete = "IN_PROGRESS".equals(status);

            operation.setCanStart(canStart);
            operation.setCanComplete(canComplete);
        }

        // 3-2. MES 버튼 활성화 여부 계산
        boolean canStartMes = "PENDING".equals(mes.getStatus());

        boolean canCompleteMes = false;
        if ("IN_PROGRESS".equals(mes.getStatus())) {
            // 모든 공정이 COMPLETED인지 확인
            boolean allCompleted = operations.stream()
                    .allMatch(op -> "COMPLETED".equals(op.getStatusCode()));
            canCompleteMes = allCompleted;
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
                .canStartMes(canStartMes)
                .canCompleteMes(canCompleteMes)
                .build();
    }

    @Override
    @Transactional
    public DeferredResult<ResponseEntity<ApiResponse<Void>>> startMesAsync(String mesId) {
        log.info("MES 비동기 시작: mesId={}", mesId);

        // DeferredResult 생성 (타임아웃 30초)
        DeferredResult<ResponseEntity<ApiResponse<Void>>> deferredResult =
                new DeferredResult<>(30000L);

        // 타임아웃 처리
        deferredResult.onTimeout(() -> {
            log.warn("MES 시작 처리 타임아웃: mesId={}", mesId);
            deferredResult.setResult(ResponseEntity
                    .status(HttpStatus.REQUEST_TIMEOUT)
                    .body(ApiResponse.fail("처리 시간이 초과되었습니다.", HttpStatus.REQUEST_TIMEOUT)));
        });

        try {
            // 1. MES 조회
            Mes mes = mesRepository.findById(mesId)
                    .orElseThrow(() -> new RuntimeException("MES를 찾을 수 없습니다: " + mesId));

            if (!"PENDING".equals(mes.getStatus())) {
                throw new RuntimeException("PENDING 상태의 MES만 시작할 수 있습니다. 현재 상태: " + mes.getStatus());
            }

            // 2. 자재 검증
            validateMaterialsAvailability(mes);

            // 3. MES 상태 변경
            mes.setStatus("IN_PROGRESS");
            mes.setStartDate(LocalDate.now());
            mes.setEndDate(null);
            mesRepository.save(mes);

            // 4. 자재 소비
            consumeMaterials(mes);

            // 5. 트랜잭션 ID 생성 및 DeferredResult 등록
            String transactionId = java.util.UUID.randomUUID().toString();
            asyncResultManager.registerResult(transactionId, deferredResult);

            // 6. Business 서버로 Order 상태 변경 이벤트 발행
            MesStartEvent event = MesStartEvent.builder()
                    .transactionId(transactionId)
                    .mesId(mesId)
                    .quotationId(mes.getQuotationId())
                    .timestamp(System.currentTimeMillis())
                    .build();

            kafkaProducerService.sendToTopic(
                    KafkaTopicConfig.MES_START_TOPIC,
                    mesId, event);

            log.info("MES 시작 이벤트 발행: transactionId={}, mesId={}, quotationId={}",
                    transactionId, mesId, mes.getQuotationId());

        } catch (Exception e) {
            log.error("MES 시작 처리 중 오류 발생: mesId={}", mesId, e);
            deferredResult.setResult(ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("MES 시작 처리 실패: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR)));
        }

        return deferredResult;
    }

    @Override
    @Transactional
    public void startOperation(String mesId, String logId, String managerId) {
        log.info("공정 시작: mesId={}, logId={}, managerId={}", mesId, logId, managerId);

        // 1. MES 조회
        Mes mes = mesRepository.findById(mesId)
                .orElseThrow(() -> new RuntimeException("MES를 찾을 수 없습니다: " + mesId));

        // 2. MesOperationLog 직접 조회 (logId로)
        MesOperationLog targetLog = mesOperationLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("공정 로그를 찾을 수 없습니다: " + logId));

        // 3. mesId 검증
        if (!mesId.equals(targetLog.getMesId())) {
            throw new RuntimeException("해당 공정은 이 MES에 속하지 않습니다: mesId=" + mesId + ", logId=" + logId);
        }

        if (!"PENDING".equals(targetLog.getStatus())) {
            throw new RuntimeException("PENDING 상태의 공정만 시작할 수 있습니다. 현재 상태: " + targetLog.getStatus());
        }

        // 4. 이전 공정들이 모두 완료되었는지 확인
        List<MesOperationLog> operationLogs = mesOperationLogRepository.findByMesIdOrderBySequenceAsc(mesId);
        for (MesOperationLog log : operationLogs) {
            if (log.getSequence() < targetLog.getSequence() && !"COMPLETED".equals(log.getStatus())) {
                throw new RuntimeException("이전 공정이 완료되지 않았습니다. sequence: " + log.getSequence());
            }
        }

        // 5. 공정 시작
        targetLog.start(managerId);
        mesOperationLogRepository.save(targetLog);

        // 6. MES의 currentOperationId 업데이트
        mes.setCurrentOperationId(targetLog.getOperationId());
        mesRepository.save(mes);

        log.info("공정 시작 완료: logId={}, operationId={}, status=IN_PROGRESS", logId, targetLog.getOperationId());
    }

    @Override
    @Transactional
    public void completeOperation(String mesId, String logId) {
        log.info("공정 완료: mesId={}, logId={}", mesId, logId);

        // 1. MES 조회
        Mes mes = mesRepository.findById(mesId)
                .orElseThrow(() -> new RuntimeException("MES를 찾을 수 없습니다: " + mesId));

        // 2. MesOperationLog 직접 조회 (logId로)
        MesOperationLog targetLog = mesOperationLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("공정 로그를 찾을 수 없습니다: " + logId));

        // 3. mesId 검증
        if (!mesId.equals(targetLog.getMesId())) {
            throw new RuntimeException("해당 공정은 이 MES에 속하지 않습니다: mesId=" + mesId + ", logId=" + logId);
        }

        if (!"IN_PROGRESS".equals(targetLog.getStatus())) {
            throw new RuntimeException("IN_PROGRESS 상태의 공정만 완료할 수 있습니다. 현재 상태: " + targetLog.getStatus());
        }

        // 4. 공정 완료
        targetLog.complete();
        mesOperationLogRepository.save(targetLog);

        // 5. 진행률 계산 및 업데이트
        List<MesOperationLog> operationLogs = mesOperationLogRepository.findByMesIdOrderBySequenceAsc(mesId);
        long completedCount = operationLogs.stream()
                .filter(log -> "COMPLETED".equals(log.getStatus()))
                .count();
        int progressRate = (int) ((completedCount * 100) / operationLogs.size());
        mes.setProgressRate(progressRate);

        // 6. 다음 공정이 있으면 currentOperationId 업데이트, 없으면 null
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

        log.info("공정 완료: logId={}, operationId={}, status=COMPLETED, progressRate={}%",
                logId, targetLog.getOperationId(), progressRate);
    }

    @Override
    @Transactional
    public DeferredResult<ResponseEntity<ApiResponse<Void>>> completeMesAsync(String mesId) {
        log.info("MES 비동기 완료: mesId={}", mesId);

        // DeferredResult 생성 (타임아웃 30초)
        DeferredResult<ResponseEntity<ApiResponse<Void>>> deferredResult =
                new DeferredResult<>(30000L);

        // 타임아웃 처리
        deferredResult.onTimeout(() -> {
            log.warn("MES 완료 처리 타임아웃: mesId={}", mesId);
            deferredResult.setResult(ResponseEntity
                    .status(HttpStatus.REQUEST_TIMEOUT)
                    .body(ApiResponse.fail("처리 시간이 초과되었습니다.", HttpStatus.REQUEST_TIMEOUT)));
        });

        try {
            // 1. MES 조회
            Mes mes = mesRepository.findById(mesId)
                    .orElseThrow(() -> new RuntimeException("MES를 찾을 수 없습니다: " + mesId));

            if (!"IN_PROGRESS".equals(mes.getStatus())) {
                throw new RuntimeException("IN_PROGRESS 상태의 MES만 완료할 수 있습니다. 현재 상태: " + mes.getStatus());
            }

            // 2. 모든 공정 완료 확인
            List<MesOperationLog> operationLogs = mesOperationLogRepository.findByMesIdOrderBySequenceAsc(mesId);
            boolean allCompleted = operationLogs.stream()
                    .allMatch(log -> "COMPLETED".equals(log.getStatus()));

            if (!allCompleted) {
                throw new RuntimeException("모든 공정이 완료되지 않았습니다.");
            }

            // 3. MES 상태 변경
            mes.setStatus("COMPLETED");
            mes.setProgressRate(100);
            mesRepository.save(mes);

            // 4. 완제품 재고 증가
            increaseProductStock(mes);

            // 5. 해당 quotation의 모든 MES가 완료되었는지 확인
            String quotationId = mes.getQuotationId();
            List<Mes> allMesForQuotation = mesRepository.findAll().stream()
                    .filter(m -> m.getQuotationId().equals(quotationId))
                    .toList();

            boolean allMesCompleted = allMesForQuotation.stream()
                    .allMatch(m -> "COMPLETED".equals(m.getStatus()));

            log.info("Quotation MES 상태 확인: quotationId={}, 전체MES={}, 완료여부={}",
                    quotationId, allMesForQuotation.size(), allMesCompleted);

            // 6. 모든 MES가 완료되었다면 Business 서버로 이벤트 발행
            if (allMesCompleted) {
                String transactionId = java.util.UUID.randomUUID().toString();
                asyncResultManager.registerResult(transactionId, deferredResult);

                MesCompleteEvent event = MesCompleteEvent.builder()
                        .transactionId(transactionId)
                        .mesId(mesId)
                        .quotationId(quotationId)
                        .quantity(mes.getQuantity())
                        .productId(mes.getProductId())
                        .timestamp(System.currentTimeMillis())
                        .build();

                kafkaProducerService.sendToTopic(
                        KafkaTopicConfig.MES_COMPLETE_TOPIC,
                        mesId, event);

                log.info("MES 완료 이벤트 발행: transactionId={}, mesId={}, quotationId={}",
                        transactionId, mesId, quotationId);
            } else {
                // 아직 완료되지 않은 MES가 있으면 즉시 성공 응답
                deferredResult.setResult(ResponseEntity.ok(
                        ApiResponse.success(
                                null, "MES가 완료되었습니다. (다른 아이템의 생산이 진행 중입니다)", HttpStatus.OK)));
            }

        } catch (Exception e) {
            log.error("MES 완료 처리 중 오류 발생: mesId={}", mesId, e);
            deferredResult.setResult(ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("MES 완료 처리 실패: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR)));
        }

        return deferredResult;
    }

    /**
     * 자재 가용성 검증 (MES 시작 전)
     * - 견적별 MRP 할당량 추적
     * - MRP Run 입고 완료 확인
     * - 물리적 재고 확인
     */
    private void validateMaterialsAvailability(Mes mes) {
        log.info("자재 가용성 검증 시작: mesId={}, bomId={}, quotationId={}",
                mes.getId(), mes.getBomId(), mes.getQuotationId());

        // 1. BOM 조회
        Bom bom = bomRepository.findById(mes.getBomId()).orElse(null);
        if (bom == null) {
            log.warn("BOM을 찾을 수 없습니다: bomId={}", mes.getBomId());
            return;
        }

        // 2. BOM의 모든 원자재 검증 (재귀적, quotationId 전달)
        Set<String> processedProducts = new HashSet<>();
        List<String> shortageItems = new ArrayList<>();
        validateBomMaterials(bom, mes.getQuantity(), mes.getQuotationId(), processedProducts, shortageItems);

        // 3. 부족한 자재가 있으면 에러 발생
        if (!shortageItems.isEmpty()) {
            String errorMessage = "자재가 부족하여 생산을 시작할 수 없습니다: " + String.join(", ", shortageItems);
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }

        log.info("자재 가용성 검증 완료: mesId={}, quotationId={}, 모든 자재 충분",
                mes.getId(), mes.getQuotationId());
    }

    /**
     * BOM 자재 재귀적 검증 (견적별 MRP 할당량 추적)
     */
    private void validateBomMaterials(Bom bom, Integer quantity, String quotationId,
                                      Set<String> processedProducts, List<String> shortageItems) {
        List<BomItem> bomItems = bomItemRepository.findByBomId(bom.getId());

        for (BomItem bomItem : bomItems) {
            // 순환 참조 방지
            if (processedProducts.contains(bomItem.getComponentId())) {
                continue;
            }
            processedProducts.add(bomItem.getComponentId());

            BigDecimal requiredQuantity = bomItem.getCount().multiply(BigDecimal.valueOf(quantity));

            if ("MATERIAL".equals(bomItem.getComponentType())) {
                // 원자재: 견적별 MRP 기반 검증
                String productId = bomItem.getComponentId();
                Product product = productRepository.findById(productId).orElse(null);
                String productName = product != null ? product.getProductName() : productId;

                // 1. 해당 견적의 MRP 조회
                List<Mrp> mrpList = mrpRepository.findByQuotationIdAndProductId(quotationId, productId);

                if (mrpList.isEmpty()) {
                    shortageItems.add(productName + " (MRP 기록 없음)");
                    log.warn("MRP 기록 없음: quotationId={}, productId={}", quotationId, productId);
                    continue;
                }

                Mrp mrp = mrpList.get(0);  // quotationId + productId 조합은 유일

                // 2. 견적별 남은 할당량 확인
                BigDecimal allocatedQty = mrp.getRequiredCount() != null ? mrp.getRequiredCount() : BigDecimal.ZERO;
                BigDecimal alreadyConsumed = mrp.getConsumedCount() != null ? mrp.getConsumedCount() : BigDecimal.ZERO;
                BigDecimal remainingAllocation = allocatedQty.subtract(alreadyConsumed);

                if (remainingAllocation.compareTo(requiredQuantity) < 0) {
                    shortageItems.add(String.format("%s (할당량 초과: 필요=%s, 남은할당=%s)",
                            productName, requiredQuantity, remainingAllocation));
                    log.warn("할당량 초과: quotationId={}, productId={}, 필요량={}, 남은할당={}",
                            quotationId, productId, requiredQuantity, remainingAllocation);
                    continue;
                }

                // 3. INSUFFICIENT인 경우 MRP Run 입고 완료 확인
                if ("INSUFFICIENT".equals(mrp.getStatus())) {
                    List<MrpRun> completedRuns = mrpRunRepository
                            .findByQuotationIdAndProductIdAndStatus(quotationId, productId, "COMPLETED");

                    if (completedRuns.isEmpty()) {
                        shortageItems.add(productName + " (입고 미완료)");
                        log.warn("입고 미완료: quotationId={}, productId={}", quotationId, productId);
                        continue;
                    }

                    BigDecimal arrivedQty = completedRuns.stream()
                            .map(MrpRun::getQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal expectedArrival = mrp.getShortageQuantity() != null
                            ? mrp.getShortageQuantity() : BigDecimal.ZERO;

                    if (arrivedQty.compareTo(expectedArrival) < 0) {
                        shortageItems.add(String.format("%s (입고 부족: 필요=%s, 입고=%s)",
                                productName, expectedArrival, arrivedQty));
                        log.warn("입고 부족: quotationId={}, productId={}, 필요입고={}, 실제입고={}",
                                quotationId, productId, expectedArrival, arrivedQty);
                        continue;
                    }
                }

                // 4. 물리적 재고 최종 확인
                ProductStock stock = productStockRepository.findByProductId(productId).orElse(null);

                if (stock == null) {
                    shortageItems.add(productName + " (재고 없음)");
                    log.warn("재고 없음: productId={}", productId);
                    continue;
                }

                BigDecimal availableQty = stock.getAvailableCount() != null
                        ? stock.getAvailableCount() : BigDecimal.ZERO;

                if (availableQty.compareTo(requiredQuantity) < 0) {
                    shortageItems.add(String.format("%s (물리적 재고 부족: 필요=%s, 현재=%s)",
                            productName, requiredQuantity, availableQty));
                    log.warn("물리적 재고 부족: productId={}, 필요량={}, 현재재고={}",
                            productId, requiredQuantity, availableQty);
                }

            } else if ("ITEM".equals(bomItem.getComponentType())) {
                // 부품: 하위 BOM 검증
                Bom childBom = bomRepository.findByProductId(bomItem.getComponentId()).orElse(null);
                if (childBom != null) {
                    validateBomMaterials(childBom, requiredQuantity.intValue(), quotationId,
                            processedProducts, shortageItems);
                }
            }
        }
    }

    /**
     * 자재 소비 처리 (MES 시작 시)
     */
    private void consumeMaterials(Mes mes) {
        log.info("자재 소비 시작: mesId={}, bomId={}, quotationId={}",
                mes.getId(), mes.getBomId(), mes.getQuotationId());

        // 1. BOM 조회
        Bom bom = bomRepository.findById(mes.getBomId()).orElse(null);
        if (bom == null) {
            log.warn("BOM을 찾을 수 없습니다: bomId={}", mes.getBomId());
            return;
        }

        // 2. BOM의 모든 원자재 소비 (재귀적, quotationId 전달)
        Set<String> processedProducts = new HashSet<>();
        consumeBomMaterials(bom, mes.getQuantity(), mes.getQuotationId(), processedProducts);

        log.info("자재 소비 완료: mesId={}, quotationId={}", mes.getId(), mes.getQuotationId());
    }

    /**
     * BOM의 자재 재귀적 소비 (quotationId 전달하여 MRP 소비량 기록)
     */
    private void consumeBomMaterials(Bom bom, Integer quantity, String quotationId, Set<String> processedProducts) {
        List<BomItem> bomItems = bomItemRepository.findByBomId(bom.getId());

        for (BomItem bomItem : bomItems) {
            // 순환 참조 방지
            if (processedProducts.contains(bomItem.getComponentId())) {
                continue;
            }
            processedProducts.add(bomItem.getComponentId());

            BigDecimal requiredQuantity = bomItem.getCount().multiply(BigDecimal.valueOf(quantity));

            if ("MATERIAL".equals(bomItem.getComponentType())) {
                // 원자재: 재고 소비 + MRP 소비량 기록
                consumeStock(bomItem.getComponentId(), requiredQuantity, quotationId);

            } else if ("ITEM".equals(bomItem.getComponentType())) {
                // 부품: 하위 BOM 탐색
                Bom childBom = bomRepository.findByProductId(bomItem.getComponentId()).orElse(null);
                if (childBom != null) {
                    consumeBomMaterials(childBom, requiredQuantity.intValue(), quotationId, processedProducts);
                }
            }
        }
    }

    /**
     * 재고 소비 (availableCount 감소, reservedCount 해제)
     */
    private void consumeStock(String productId, BigDecimal quantity, String quotationId) {
        // 1. 물리적 재고 소비
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

        // 2. MRP의 consumedCount 증가 (견적별 소비 추적)
        List<Mrp> mrpList = mrpRepository.findByQuotationIdAndProductId(quotationId, productId);

        if (!mrpList.isEmpty()) {
            Mrp mrp = mrpList.get(0);
            BigDecimal currentConsumed = mrp.getConsumedCount() != null
                    ? mrp.getConsumedCount() : BigDecimal.ZERO;
            mrp.setConsumedCount(currentConsumed.add(quantity));
            mrpRepository.save(mrp);

            log.info("MRP 소비량 기록: quotationId={}, productId={}, 소비량={}, 총소비={}",
                    quotationId, productId, quantity, mrp.getConsumedCount());
        } else {
            log.warn("MRP 기록을 찾을 수 없습니다: quotationId={}, productId={}", quotationId, productId);
        }
    }

    /**
     * 출하할 제품 증가
     */
    private void increaseProductStock(Mes mes) {
        log.info("완제품 재고 증가: productId={}, quantity={}", mes.getProductId(), mes.getQuantity());

        ProductStock stock = productStockRepository.findByProductId(mes.getProductId()).orElse(null);

        if (stock == null) {
            // ProductStock이 없으면 생성
            log.warn("ProductStock이 없습니다. 새로 생성합니다: productId={}", mes.getProductId());

            throw new RuntimeException("재고가 없는 제품입니다.");
        } else {
            // 재고에 증가 (forShipmentCount 증가)
            BigDecimal forShipmentCount = stock.getForShipmentCount() != null
                    ? stock.getForShipmentCount()
                    : BigDecimal.ZERO;

            BigDecimal availableCount = stock.getAvailableCount() != null
                    ? stock.getAvailableCount()
                    : BigDecimal.ZERO;

            stock.setAvailableCount(availableCount.add(BigDecimal.valueOf(mes.getQuantity())));
            stock.setForShipmentCount(forShipmentCount.add(BigDecimal.valueOf(mes.getQuantity())));
            productStockRepository.save(stock);

            log.info("재고 증가 완료: productId={}, 이전={}, 증가={}, 현재={}",
                    mes.getProductId(), forShipmentCount, mes.getQuantity(), stock.getAvailableCount());
        }
    }
}
