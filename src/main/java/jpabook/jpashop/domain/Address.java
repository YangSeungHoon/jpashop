package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable//값 타입을 정의한느 곳에 표시
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    protected Address() {
    }

    //값 타입은 생성시에 값이 들어가고 변경하면 안되니까 생성자로 만들어놓고 Setter를 두지 않는다.
    protected Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
