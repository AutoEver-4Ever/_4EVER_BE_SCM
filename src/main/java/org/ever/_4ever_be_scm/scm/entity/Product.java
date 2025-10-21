package org.ever._4ever_be_scm.scm.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import jakarta.persistence.*;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@Table(name = "product")
public class Product extends TimeStamp {

    /**
     * 제품 고유 식별자 (UUID)
     */
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "product_code", nullable = false)
    private String productCode;

    @Column(name = "category")
    private String category;

    /**
     * 공급업체 회사 ID (UUID)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_company_id")
    private SupplierCompany supplierCompany;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "unit")
    private String unit;

    @Column(name = "origin_price")
    private BigDecimal originPrice;

    @Column(name = "selling_price")
    private BigDecimal sellingPrice;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());  // UUID v7 생성
        }
    }
}
