
package org.ever._4ever_be_scm.scm.iv.repository;

import java.util.List;

import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
	List<Product> findByCategory(String category);
}
