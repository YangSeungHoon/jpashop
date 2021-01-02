package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    @Test
    @Rollback(false) //위에 @Transactional때문에 insert문이 안나가는데, 이걸 붙여주면 나간다.
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("yang");

        //when
        Long saveId = memberService.join(member);
        em.flush();
        //then
        assertEquals(member, memberRepository.findOne(saveId));

    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예약() throws Exception {
        //given

        Member member1 = new Member();
        member1.setName("Yang");

        Member member2 = new Member();
        member2.setName("Yang");
        //when
        memberService.join(member1);
        //위에 @Test에다가 IllegalStateException을 넣어줘서 여기서 저 오류가 나오면
        // 자연스럽게 아래로 내려간다.
        memberService.join(member2);
        //then
        fail("예외가 발생해야 한다.");

    }

}