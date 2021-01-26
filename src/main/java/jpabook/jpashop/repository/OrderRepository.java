package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.hibernate.Criteria;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class,id);
    }

//    //동적 쿼리가 필요한 부분
//    public List<Order> findAll(OrderSearch orderSearch){
//        em.createQuery("select o from Order o join o.member m"
//                +" where o.status =: status"+
//                " and m.name like :name"
//                ,Order.class)
//                .setParameter("status",orderSearch.getOrderStatus())
//                .setParameter("name",orderSearch.getMemberName())
//                .setMaxResults(1000) // 최대 1000건
//                .getResultList();
//
//    }

    public List<Order> findAllByString(OrderSearch orderSearch) {
        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    //쿼리 DSL
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();

        //주문 상태 검색
        if(orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),orderSearch.getOrderStatus());
            criteria.add(status);
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name = cb.like(m.<String>get("name"),"%" + orderSearch.getMemberName()+"%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }



    public List<Order> findAllWithMemberDelivery() {

        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
    }

    //Order를 기준으로 Order와 Member의 관계는 N:1이다.
    //               Order와 delivery의 관계는 1:1이다.
    //               Order와 orderItems의 관계는 1:N이다.
    //               추가적으로 OrderItems와 item의 관계는 N:1이다.
    //위의 관계중이에서 문제가 되는지점은 Order를 기준으로하여 1:N의 관계에 있는 orderITems와 join하면서 문제가 발생한다.
    //우선, 결과적으로는 내가 원하는 결과는 두 개의 Order를 원한다. 두 번의 주문이 있었고 그 각각의 주문에는 두 개의 item이 들어가 있기 때문이다.
    //근데 Order와 OrderItems를 join하게 되면 네 개의 Order가 나온다.
    // 조금 더 간단히 생각해보겠다. 내가 주문을 한 번 주문해서 책 두권을 샀다. 그래서 OrderId가 1인 주문서가 하나 만들어졌고, 그 하나의 주문서에
    //책이 두권이 들어있는 것이다. 근데 현재 아래의 결과물은 Join하는 과정에서 하나의 주문서에 하나의 물품, 그리고 같은 주문서에 또 다른 물품.
    //이렇게 결괏값이 나오니까 주문서는 하나인데 결과물은 두 개가 되는 상황인 것이다.

    //그래서 이러한 경우를 해결하기위해 distinct를 추가해주는 것이다. 근데 여기서 넣어주는 distinct는 DB에서의 distinct는 모든 행의 값이
    //다 같아야 중복을 제거해준다. 그러나 JPA에서(아래의 distinct)는 id값의 중복을 제거해주는 기능까지 있어서 우리가 원하는 결과를 얻을 수 있다.
    public List<Order> findAllWithItem() {
//        return em.createQuery(
//                "select o from Order o" +
//                        " join fetch o.member m" +
//                        " join fetch o.delivery d"+
//                        " join fetch o.orderItems oi"+
//                        " join fetch oi.item i", Order.class)
//                .getResultList();

        return em.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d"+
                        " join fetch o.orderItems oi"+
                        " join fetch oi.item i", Order.class)
                .getResultList();
    }
    // 알아둘 것!
    // 1:N(일대다) 관계에서는 절대로 페이징을 사용하면 안된다.
    // 왜냐하면 디비 입장에서 보면 일대다 관계를 Join하는 순간 앞서 설명한대로 데이터가 늘어난다. 그래서 페이징처리의 그 기준도 달라지고
    // 더하여 하이버네이트가 경고 로그를 남기면서 모든 데이터를 DB에서 읽어오고, 메모리에서 페이징 해버린다. 그래서 out of memory의 위험이있다.
    // 참고로, 컬렉션 패치 조인은 1개만 사용해야한다. 왜냐하면 컬렉션이 둘 이상에 패치 조인을 사용하면 데이터가 부정합하게 조회될 수 있다.



    //페이지에 영향을 주지 않는 xToOne의 관계는 그냥 다 패치 조인으로 가져온다.
    //페이징 처리는 Mapping쪽에서 @RequestParam으로 값을 받아와서 넣어주었다.
    public List<Order> findAllWithMemberDelivery(int offset,int limit) {

        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

}
