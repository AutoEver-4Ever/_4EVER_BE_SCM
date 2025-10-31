package org.ever._4ever_be_scm.scm.mm.integration.port;

import org.ever._4ever_be_scm.scm.mm.integration.dto.InternalUserResponseDto;

import java.util.List;

public interface InternalUserServicePort {
    /**
     * UserID로 InternalUser 정보 조회
     */
    InternalUserResponseDto getInternalUserInfoById(String InternalUserId);

    /**
     * 여러 UserID로 InternalUser 정보 조회
     */
    List<InternalUserResponseDto> getInternalUserInfosByIds(List<String> InternalUserId);
}
