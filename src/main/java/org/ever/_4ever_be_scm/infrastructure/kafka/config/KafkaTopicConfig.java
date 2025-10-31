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
}
