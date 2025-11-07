
package org.ever._4ever_be_scm.scm.iv.repository;

import java.util.List;

import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
	List<Product> findByCategory(String category);
	
	/**
	 * ProductStock에 존재하지 않는 Product 목록 조회
	 * 
	 * @return ProductStock에 없는 Product 목록
	 */
	@Query("SELECT p FROM Product p WHERE p.id NOT IN (SELECT ps.product.id FROM ProductStock ps)")
	List<Product> findProductsNotInStock();
	
	/**
	 * 특정 공급사에 속한 첫 번째 제품 조회
	 *
	 * @param supplierCompanyId 공급사 ID
	 * @return 공급사에 속한 제품 (없으면 null)
	 */
	Product findFirstBySupplierCompany_Id(String supplierCompanyId);
}
