package org.ever._4ever_be_scm.scm.pp.service;

import org.ever._4ever_be_scm.scm.pp.service.dto.DashboardWorkflowItemDto;

import java.util.List;

public interface DashboardService {
    List<DashboardWorkflowItemDto> getSupplierPurchaseOrders(String userId, int size);
    List<DashboardWorkflowItemDto> getPurchaseRequests(String userId, int size);
    List<DashboardWorkflowItemDto> getPurchaseRequestsOverall(int size);
    List<DashboardWorkflowItemDto> getMmPurchaseOrders(int size);
    List<DashboardWorkflowItemDto> getInboundDeliveries(String userId, int size);
    List<DashboardWorkflowItemDto> getOutboundDeliveries(String userId, int size);
    List<DashboardWorkflowItemDto> getQuotationsToProduction(String userId, int size);
    List<DashboardWorkflowItemDto> getProductionInProgress(String userId, int size);
}
