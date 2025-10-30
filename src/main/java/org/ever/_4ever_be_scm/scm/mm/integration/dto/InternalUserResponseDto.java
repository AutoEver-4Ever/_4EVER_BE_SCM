package org.ever._4ever_be_scm.scm.mm.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserResponseDto {
    private String userId;
    private String name;
    private String departmentId;
    private String departmentName;
    private String phoneNumber;
    private String email;
}
