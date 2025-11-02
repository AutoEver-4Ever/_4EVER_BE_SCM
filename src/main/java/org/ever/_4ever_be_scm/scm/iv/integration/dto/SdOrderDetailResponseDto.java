package org.ever._4ever_be_scm.scm.iv.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SdOrderDetailResponseDto {
    private SdOrderDetailDto order;
    private SdCustomerDto customer;
    private List<SdOrderItemDto> items;
    private String note;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SdOrderDetailDto {
        private String salesOrderId;
        private String salesOrderNumber;
        private String orderDate;
        private String dueDate;
        private String statusCode;
        private Integer totalAmount;
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SdCustomerDto {
        private String customerId;
        private String customerName;
        private String customerBaseAddress;
        private String customerDetailAddress;
        private SdManagerDto manager;
        
        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SdManagerDto {
            private String managerName;
            private String managerPhone;
            private String managerEmail;
        }
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SdOrderItemDto {
        private String itemId;
        private String itemName;
        private Integer quantity;
        private String uonName;
        private Integer unitPrice;
        private Integer amount;
    }
}
