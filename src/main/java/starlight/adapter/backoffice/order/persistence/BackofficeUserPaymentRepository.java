package starlight.adapter.backoffice.order.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.application.backoffice.member.required.dto.BackofficeUserPaymentLookupResult;
import starlight.domain.order.order.Orders;

public interface BackofficeUserPaymentRepository extends JpaRepository<Orders, Long> {

    @Query(
            value = """
                    select new starlight.application.backoffice.member.required.dto.BackofficeUserPaymentLookupResult(
                        o.id,
                        o.orderCode,
                        o.usageProductCode,
                        o.usageCount,
                        o.price,
                        o.status,
                        (select p.status
                         from PaymentRecords p
                         where p.order = o
                           and p.id = (select max(p2.id) from PaymentRecords p2 where p2.order = o)),
                        (select p.paymentKey
                         from PaymentRecords p
                         where p.order = o
                           and p.id = (select max(p2.id) from PaymentRecords p2 where p2.order = o)),
                        o.createdAt,
                        (select max(p.approvedAt)
                         from PaymentRecords p
                         where p.order = o
                           and p.status = 'DONE')
                    )
                    from Orders o
                    where o.buyerId = :userId
                    """,
            countQuery = """
                    select count(o.id)
                    from Orders o
                    where o.buyerId = :userId
                    """
    )
    Page<BackofficeUserPaymentLookupResult> findUserPaymentPage(@Param("userId") Long userId, Pageable pageable);
}
