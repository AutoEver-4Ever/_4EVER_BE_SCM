package org.ever._4ever_be_scm.scm.pp.repository;

import org.ever._4ever_be_scm.scm.pp.entity.MpsDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MpsDetailRepository extends JpaRepository<MpsDetail, String> {
    List<MpsDetail> findByMpsId(String mpsId);
    List<MpsDetail> findByMpsIdIn(List<String> mpsIds);
}
