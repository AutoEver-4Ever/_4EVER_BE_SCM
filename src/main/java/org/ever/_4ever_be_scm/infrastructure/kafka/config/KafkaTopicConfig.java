package org.ever._4ever_be_scm.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // SCM 서비스 토픽
    public static final String SCM_STOCK_RESERVE_TOPIC = "scm-stock-reserve";
    public static final String SCM_STOCK_RELEASE_TOPIC = "scm-stock-release";
    public static final String SCM_SHIPMENT_CREATE_TOPIC = "scm-shipment-create";
    public static final String SCM_SHIPMENT_COMPLETE_TOPIC = "scm-shipment-complete";

    // 다른 서비스 토픽
    public static final String USER_EVENT_TOPIC = "user-event";
    public static final String SCM_EVENT_TOPIC = "scm-event";
    public static final String BUSINESS_EVENT_TOPIC = "business-event";
    public static final String ALARM_EVENT_TOPIC = "alarm-event";
    public static final String CREATE_SUPPLIER_USER_TOPIC = "create-supplier-user";
    public static final String SUPPLIER_USER_RESULT_TOPIC = "supplier-user-result";
    public static final String USER_ROLLBACK_TOPIC = "user-rollback";

    // PP 모듈 - 견적 업데이트 토픽
    public static final String QUOTATION_UPDATE_TOPIC = "quotation-update";
    public static final String QUOTATION_UPDATE_COMPLETION_TOPIC = "quotation-update-completion";

    // PP 모듈 - MES 관련 토픽
    public static final String MES_START_TOPIC = "mes-start";
    public static final String MES_START_COMPLETION_TOPIC = "mes-start-completion";
    public static final String MES_COMPLETE_TOPIC = "mes-complete";
    public static final String MES_COMPLETE_COMPLETION_TOPIC = "mes-complete-completion";

    // IV 모듈 - 판매주문 상태 변경 토픽
    public static final String SALES_ORDER_STATUS_CHANGE_TOPIC = "sales-order-status-change";
    public static final String SALES_ORDER_STATUS_CHANGE_COMPLETION_TOPIC = "sales-order-status-change-completion";

    @Bean
    public NewTopic scmStockReserveTopic() {
        return TopicBuilder.name(SCM_STOCK_RESERVE_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic scmStockReleaseTopic() {
        return TopicBuilder.name(SCM_STOCK_RELEASE_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic scmShipmentCreateTopic() {
        return TopicBuilder.name(SCM_SHIPMENT_CREATE_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic scmShipmentCompleteTopic() {
        return TopicBuilder.name(SCM_SHIPMENT_COMPLETE_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic userEventTopic() {
        return TopicBuilder.name(USER_EVENT_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic scmEventTopic() {
        return TopicBuilder.name(SCM_EVENT_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic businessEventTopic() {
        return TopicBuilder.name(BUSINESS_EVENT_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic alarmEventTopic() {
        return TopicBuilder.name(ALARM_EVENT_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic createSupplierUserTopic() {
        return TopicBuilder.name(CREATE_SUPPLIER_USER_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic supplierUserResultTopic() {
        return TopicBuilder.name(SUPPLIER_USER_RESULT_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic userRollbackTopic() {
        return TopicBuilder.name(USER_ROLLBACK_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic quotationUpdateTopic() {
        return TopicBuilder.name(QUOTATION_UPDATE_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic quotationUpdateCompletionTopic() {
        return TopicBuilder.name(QUOTATION_UPDATE_COMPLETION_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic mesStartTopic() {
        return TopicBuilder.name(MES_START_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic mesStartCompletionTopic() {
        return TopicBuilder.name(MES_START_COMPLETION_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic mesCompleteTopic() {
        return TopicBuilder.name(MES_COMPLETE_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic mesCompleteCompletionTopic() {
        return TopicBuilder.name(MES_COMPLETE_COMPLETION_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic salesOrderStatusChangeTopic() {
        return TopicBuilder.name(SALES_ORDER_STATUS_CHANGE_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic salesOrderStatusChangeCompletionTopic() {
        return TopicBuilder.name(SALES_ORDER_STATUS_CHANGE_COMPLETION_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }
}
