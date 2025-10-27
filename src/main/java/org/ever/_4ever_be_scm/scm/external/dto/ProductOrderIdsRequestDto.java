package org.ever._4ever_be_scm.scm.external.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductOrderIdsRequestDto {
    private List<String> productOrderIds;
}
