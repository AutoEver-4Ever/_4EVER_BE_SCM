package org.ever._4ever_be_scm.scm.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "supplier_user")
@Getter
public class SupplierUser {
    /**
     * 공급업체 고유 식별자 (UUID)
     */
    @Id
    @Column(length = 36)
    private String id;

    @Column(name="user_id")
    private String userId;

    @Column(name = "supplier_user_name")
    private String supplierUserName;

    @Column(name="customer_user_code")
    private String customerUserCode;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());  // UUID v7 생성
        }
    }
}
