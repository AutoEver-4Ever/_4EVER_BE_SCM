package org.ever._4ever_be_scm.common.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 하나의 트랜잭션에 포함된 모든 엔티티 변경 내역을 담는 컨테이너 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionChanges {
    /**
     * 트랜잭션 내의 모든 변경 내역 목록
     */
    @Builder.Default
    private List<EntityChange> changes = new ArrayList<>();
    
    /**
     * 변경 내역 추가
     * @param change 추가할 엔티티 변경 내역
     */
    public void addChange(EntityChange change) {
        changes.add(change);
    }
}
