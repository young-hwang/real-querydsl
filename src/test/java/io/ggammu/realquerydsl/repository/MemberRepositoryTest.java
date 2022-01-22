package io.ggammu.realquerydsl.repository;

import io.ggammu.realquerydsl.dto.MemberSearchCondition;
import io.ggammu.realquerydsl.dto.MemberTeamDto;
import io.ggammu.realquerydsl.entity.Member;
import io.ggammu.realquerydsl.entity.Team;
import java.util.List;
import javax.persistence.EntityManager;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void basicTest() {
        //given
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        //when
        Member findMember = memberRepository.findById(member.getId()).get();

        //then
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    void searchTest() {
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();

        MemberSearchCondition memberSearchCondition = new MemberSearchCondition();
        memberSearchCondition.setAgeGoe(35);
        memberSearchCondition.setAgeLoe(40);
        memberSearchCondition.setTeamName("teamB");

        //when
        List<MemberTeamDto> memberTeamDtos = memberRepository.search(memberSearchCondition);

        //then
        assertThat(memberTeamDtos).extracting("username").containsExactly("member4");
    }

    @Test
    void searchPageSimple() {
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();

        MemberSearchCondition memberSearchCondition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);

        //when
        Page<MemberTeamDto> memberTeamDtos = memberRepository.searchPageSimple(memberSearchCondition, pageRequest);

        //then
        assertThat(memberTeamDtos.getSize()).isEqualTo(3);
        assertThat(memberTeamDtos.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
    }

}