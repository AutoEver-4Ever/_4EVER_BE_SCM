package org.ever.event.alarm;

/**
 * 알림 유형
 */
public enum AlarmType {
    PR,     // Purchase Request, 구매부
    SD,     // Sales Document, 영업부
    IM,     // Inventory Management, 재고부
    FCM,    // Financial Management, 재무부
    HRM,    // Human Resource Management, 인사부
    PP,     // Production Planning, 생산부
    CUS,    // Customer, 고객사
    SUP,    // Supplier, 공급사
    UNKNOWN // 알 수 없음
}