package org.ever._4ever_be_scm.scm.pp.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.scm.pp.entity.Operation;
import org.ever._4ever_be_scm.scm.pp.repository.OperationRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OperationInitializer {
    private final OperationRepository operationRepository;

    @PostConstruct
    public void initOperations() {
        if (operationRepository.count() == 0) {
            List<Operation> ops = Arrays.asList(
                Operation.builder().id("1").opCode("OP-001").opName("절단").description("자재 절단 공정").requiredTime(BigDecimal.valueOf(120)).build(),
                Operation.builder().id("2").opCode("OP-002").opName("가공").description("가공 공정").requiredTime(BigDecimal.valueOf(170)).build(),
                Operation.builder().id("3").opCode("OP-003").opName("조립").description("조립 공정").requiredTime(BigDecimal.valueOf(180)).build(),
                Operation.builder().id("4").opCode("OP-004").opName("용접").description("용접 공정").requiredTime(BigDecimal.valueOf(200)).build(),
                Operation.builder().id("5").opCode("OP-005").opName("도장").description("도장 공정").requiredTime(BigDecimal.valueOf(360)).build(),
                Operation.builder().id("6").opCode("OP-006").opName("포장").description("포장 공정").requiredTime(BigDecimal.valueOf(400)).build()
            );
            operationRepository.saveAll(ops);
        }
    }
}
