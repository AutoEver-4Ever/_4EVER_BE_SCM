package org.ever._4ever_be_scm.scm.pp.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessApiResponse<T> {
    private int status;
    private boolean success;
    private String message;
    private T data;
}
