package jpabook.jpashop.api;


import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


//Order와 Member는 ManyToOne 관계
//Order와 Delivery는 OneToOne 관계이다.
//그러면 xToOne 관계의 성능을 최적화 시켜보자(ManyToOne, OneToOne)

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    //orders를 가져온다고 이렇게 해놓고 돌리면 무한 루프에 빠진다.
    //그 이유는, 먼저 Order에가면 Member가 존재한다. 그러면 Member를 가져오기위해 Member에 가는데, 또 거기에는 orders가 존재한다.
    //그래서 또 Member로 오는데, 이렇게 무한루프에 빠지는 것이다. 즉, 양방향 연관관계에 있기때문에 발생하는 것이다. 이것을 막으려면??
    //둘 중 하나에게 @JsonIgnore를 해줘야한다.
    //근데 또 다른 문제가 발생한다. Proxy관련 문제가 발생하는데, 이것은 Lazy로딩(지연로딩)때문에 발생하는 문제이다.
    //Order를 가져오는 순간에 Order에 존재하는 Member를 가져오는데 그 fetch전략이 Lazy라서 Member는 건들지 않게 된다.
    //그렇기 때문에 현재 Member에는 진짜 객체가 아닌, Proxy객체가 들어가 있는 상황이다. 그래서 이것을 또 해결해주는 Hibernate5Module을 @Bean으로
    //등록했다. 그러면 이제 데이터를 가져오게 되는데, 지연로딩으로 되어있는 부분은 Module5가 그냥 무시해버려서 null값이 나오게 된다.
    //이렇게 지연로딩으로 인해 null값이 나오는 것은 5Module의 옵션을 이용하여 로딩하게 만들 수 있다.

    //근데!!! 애초에 이 코드는 dto를 쓰지 않고, 직접적으로 List<Order>의 자료형으로 객체를 반환했다는 부분에서(엔티티를 노출하는 방법) 잘못된 방법이다.
    //더하여 예를들아, 내가 Order에서 Member만 필요했다면..?? 딱 Memeber만 알면되는건데, 지금 이 상황은 그냥 다 가져와서 처리하려고 하다보니 쿼리도
    //많이 나가기때문에 좋지 않다. 그렇게 때문에 그냥 위에는 이렇게 할 수 있다.. 정도로만 알고! 더 좋은 방법을 사용해야한다.
//    @GetMapping("/api/v1/simple-orders")
//    public List<Order> ordersV1() {
//        List<Order> all = orderRepository.findAllByString(new OrderSearch());
//        return all;
//    }

    //위에처럼 5Module의 옵션으로 하는 방법 외에도 다음과 같은 방법도있다.
    //Lazy(지연로딩)로딩이 진짜 객체를 가져오는 시점은 해당 객체를 필요로 할 때이다. 그 특성을 이용하여 다음과 같이 그냥 그 부분에 접근을
    //하게 만들어서 데이터를 넣는 방법이 있다.(강제 초기화 해버리기!)
    //근데 뭔가 안깔끔하다. 그러니까 객체를 노출하는 첫 단추부터 잘못되었다는 것이다..
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
        }
        return all;
    }

    //dto를 이용해보자
    //이렇게 해서 돌리면 쿼리가 5개가 날아간다. => N+1발생
    //기본적으로 Order를 한 번 조회하면 관련된 delivery와 member 각각 하나씩 총 두 개의 쿼리가 나가는데 현재 orders에는 두 건이 존재한다.
    //그래서 결과적으로 처음 Order를 가져오는데 1번,그 내부적으로 delivery와 Member에 해당하는 각각 2개씩 해서 총 다섯 개의 쿼리가 나가는 것이다.
    // Order 2개
    // 1+ 회원 N + 배송 N
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getMember().getAddress();
        }
    }

}
