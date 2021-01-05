package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class MemberForm {

    //이름을 필수로 받겠다. @PostMapping에서 @Valid를 사용하면 이것을 인지해준다.
    @NotEmpty(message="회웡 이름은 필수 입니다.")
    private String name;

    private String city;
    private String street;
    private String zipcode;
}
