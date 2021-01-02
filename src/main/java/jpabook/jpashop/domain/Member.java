package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id
    @GeneratedValue
    @Column(name ="member_id")
    private Long id;

    private String name;

    @Embedded //값 타입을 사용하는 곳에 표시
    private Address address;

    @OneToMany(mappedBy = "member")// Order테이블에있는 Member필드에의해서 맵핑 되었다.
    private List<Order> orders = new ArrayList<>();
}
