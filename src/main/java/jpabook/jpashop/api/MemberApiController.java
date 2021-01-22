package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

//@Controller
//@ResponseBody
@RestController //이 녀석은 Controller와 ResponseBody를 합친 거.
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;


    //이렇게 직접 Entity를 반환하는 방법은 좋은 방법이 아니다.  더하여 이렇게 Array를 그대로 반환하면 스팩을 확장 할 수도 없다.
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T>{
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }



    //Member에서 name에다가 @NotEmpty를 설정해놨는데, 이 녀석이 javax.validation인데 여기 아래에 @Valid가 붙어있으면
    //해당 Member Entity에서 유효성을 검증한다. 그래서 만일 name에 이름을 안넣어 주면 오류를 발생시켜준다.
    //근데 다음의 방법은 Entity의 데이터가 바뀌면 api의 스팩 또한 문제가 발생할 수 있기 때문에 이렇게 Member Entity를 직접 넣어 사용하는
    //방식은 추천하지 않는 방식이다. 그러니까 아래 v2처럼 사용하자.
    @PostMapping("/api/v1/members")//@RequestBody 어노테이션을 이용하면 HTTP 요청 Body를 자바 객체로 전달받을 수 있다.
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    //Member Entity의 스팩을 바꾸면 여기서 컴파일 오류가 나오기 때문에 여기서 수정하면 된다. 즉, Entity가 변경되어도 api의 스팩은 변하지
    //않는다는 장점이 있다. 더하여 위의 경우에는 어떠한 값이 들어오는지를 알 수 없지만, 아래의 코드는 딱 보면 name만 받게 되어 있다는 것을
    //한 눈에 알아볼 수 있다.
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {

        Member member = new Member();
        member.setName(request.getName());
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse saveMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id,request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(),findMember.getName());
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse{
        private Long id;
        private String name;
    }

    @Data
    static class CreateMemberRequest{
        @NotEmpty
        private String name;
    }

    @Data
//    @Data : 클래스안의 모든 private 필드에 대해 @Getter와 @Setter를 적용하여 세터/게터를 만들어주고
//    클래스내에 @ToString 과 @EqualsAndHashCode를 적용시켜 메소드를 오버라이드 해주며
//    @RequiredArgsConstructor를 지정해 준다.
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }


}
