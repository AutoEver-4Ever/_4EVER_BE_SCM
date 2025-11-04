package org.ever._4ever_be_scm.scm.pp.repository;

import org.ever._4ever_be_scm.scm.pp.entity.MesOperationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MesOperationLogRepository extends JpaRepository<MesOperationLog, String> {

    List<MesOperationLog> findByMesIdOrderBySequenceAsc(String mesId);

    List<MesOperationLog> findByMesIdAndStatus(String mesId, String status);
}
