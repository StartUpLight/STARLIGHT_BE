package starlight.adapter.backoffice.order.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.application.backoffice.order.required.dto.BackofficePaymentOrderLookupResult;
import starlight.domain.order.enumerate.OrderStatus;
import starlight.domain.order.order.Orders;

import java.util.List;

public interface BackofficeOrderRepository extends JpaRepository<Orders, Long> {

    @Query(
            value = """
                    select new starlight.application.backoffice.order.required.dto.BackofficePaymentOrderLookupResult(
                        o.id,
                        o.orderCode,
                        o.buyerId,
                        o.usageProductCode,
                        o.usageCount,
                        o.price,
                        o.status,
                        o.createdAt,
                        (select p.status
                         from PaymentRecords p
                         where p.order = o
                           and p.id = (select max(p2.id) from PaymentRecords p2 where p2.order = o)),
                        (select p.paymentKey
                         from PaymentRecords p
                         where p.order = o
                           and p.id = (select max(p2.id) from PaymentRecords p2 where p2.order = o)),
                        (select max(p.approvedAt)
                         from PaymentRecords p
                         where p.order = o
                           and p.status = 'DONE')
                    )
                    from Orders o
                    where (:status is null or o.status = :status)
                      and (
                          :keyword is null
                          or lower(o.orderCode) like lower(concat('%', :keyword, '%'))
                          or lower(o.usageProductCode) like lower(concat('%', :keyword, '%'))
                          or o.buyerId in :memberIds
                      )
                    """,
            countQuery = """
                    select count(o.id)
                    from Orders o
                    where (:status is null or o.status = :status)
                      and (
                          :keyword is null
                          or lower(o.orderCode) like lower(concat('%', :keyword, '%'))
                          or lower(o.usageProductCode) like lower(concat('%', :keyword, '%'))
                          or o.buyerId in :memberIds
                      )
                    """
    )
    Page<BackofficePaymentOrderLookupResult> findPaymentPage(
            @Param("status") OrderStatus status,
            @Param("keyword") String keyword,
            @Param("memberIds") List<Long> memberIds,
            Pageable pageable
    );
}
