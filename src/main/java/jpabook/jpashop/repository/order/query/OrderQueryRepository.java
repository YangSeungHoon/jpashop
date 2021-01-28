package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos() {

        List<OrderQueryDto> result = findOrders(); //Order가 두 개

        result.forEach(o -> { // 각각의 Order를 돌리면서 그 각각의 Order안에 존재하는 OrderItem을 OrderId를 이용하여 뽑는다.
           List<OrderItemQueryDto> orderItems =  findOrderItems(o.getOrderId());
           o.setOrderItems(orderItems);
        });
        return result;
    }



    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"+
                 " from OrderItem oi"+
                         " join oi.item i"+
                         " where oi.order.id = :orderId",OrderItemQueryDto.class)
                .setParameter("orderId",orderId)
                .getResultList();
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id,m.name,o.orderDate,o.status,d.address)" +
                        " from Order o"+
                        " join o.member m"+
                        " join o.delivery d",OrderQueryDto.class)
                .getResultList();
    }


    public List<OrderQueryDto> findAllByDto_optimization() {
        List<OrderQueryDto> result = findOrders(); //주문 다 가져와서

        List<Long> orderIds = toOrderIds(result);

        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderITemMap(orderIds);

        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderITemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"+
                        " from OrderItem oi"+
                        " join oi.item i"+
                        " where oi.order.id in :orderIds",OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
        return orderItemMap;
    }

    private List<Long> toOrderIds(List<OrderQueryDto> result) {
        List<Long> orderIds = result.stream() //orderIdList만들기
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
        return orderIds;
    }
}
