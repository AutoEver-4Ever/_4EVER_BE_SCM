package org.ever.event.alarm;

/**
 * 링크된 문서 유형
 */
public enum LinkType { // ReferenceTypeEnum 과 매핑됨
    // PR (구매부)
    PURCHASE_REQUISITION,
    PURCHASE_ORDER,
    PR_ETC,

    // SD (영업부)
    QUOTATION,
    SALES_ORDER,
    SD_ETC,

    // IM (재고부)
    IM_ETC,

    // FCM (재무부)
    SALES_INVOICE,
    PURCHASE_INVOICE,
    FCM_ETC,

    // HRM (인사부)
    HRM_ETC,

    // PP (생산부)
    ESTIMATE,
    INSUFFICIENT_STOCK,
    PP_ETC,

    UNKNOWN // 그 외
}