package org.ever._4ever_be_scm.scm.iv.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import jakarta.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "supplier_company")
@Getter
public class SupplierCompany extends TimeStamp {

    /**
     * 공급업체 고유 식별자 (UUID)
     */
    @Id
    @Column(length = 36)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_user_id")
    private SupplierUser supplierUser;

    @Column(name = "company_code", nullable = false)
    private String companyCode;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name="status")
    private String status;

    @Column(name = "base_address")
    private String baseAddress;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column(name = "category")
    private String category;

    @Column(name = "office_phone")
    private String officePhone;

    @Column(name = "delivery_days")
    private Integer deliveryDays;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());  // UUID v7 생성
        }
    }
}
