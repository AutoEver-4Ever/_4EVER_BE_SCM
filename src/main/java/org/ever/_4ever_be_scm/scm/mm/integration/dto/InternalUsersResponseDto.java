package org.ever._4ever_be_scm.scm.mm.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InternalUsersResponseDto {
    private List<InternalUserResponseDto> internalUsers;
}
