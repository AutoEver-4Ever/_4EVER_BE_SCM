package org.ever._4ever_be_scm.scm.iv.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "제품 정보 조회 요청")
public class ItemInfoRequest {
    
    private List<String> itemIds;
}
