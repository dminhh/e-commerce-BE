package com.prod.JPARepositories.orders;

import com.prod.models.orders.Bill;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@EnableJpaRepositories
@Repository
public interface BillRepository extends JpaRepository<Bill, Integer>,
        JpaSpecificationExecutor<Bill> {
    interface Specs {
        static Specification<Bill> byOrderId(int orderId) {
            return (root, query, cb) -> cb.equal(root.get("order_id"), orderId);
        }
        static Specification<Bill> byAddressId(int addressId) {
            return (root, query, cb) -> cb.equal(root.get("address_id"), addressId);
        }
        static Specification<Bill> byStatus(String status) {
            return (root, query, cb) -> cb.equal(root.get("status"), status);
        }
        static Specification<Bill> byListOrderId(List<Integer> listOrderId) {
            return (root, query, criteriaBuilder)
                    -> root.get("order_id").in(listOrderId);
        }
        static Specification<Bill> byUser(String user) {
            return (root, query, cb)
                    -> {
                assert query != null;
                query.orderBy(cb.desc(root.get("create_at")));
                return cb.like(cb.upper(root.get("user")), "%" + user.toUpperCase() + "%");
            };
        }
        static Specification<Bill> beforeDate(LocalDateTime date) {
            return (root, query, cb) -> cb.lessThan(root.get("create_at"), date);
        }
        static Specification<Bill> afterDate(LocalDateTime date) {
            return (root, query, cb) -> cb.greaterThan(root.get("create_at"), date);
        }
    }
}
