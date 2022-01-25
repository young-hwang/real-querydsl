package io.ggammu.realquerydsl.repository;

import io.ggammu.realquerydsl.dto.MemberSearchCondition;
import io.ggammu.realquerydsl.entity.Member;
import io.ggammu.realquerydsl.entity.Team;
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
class MemberTestRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberTestRepository memberTestRepository;

    @Test
    void searchApplyPagination() {
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
        Page<Member> results = memberTestRepository.searchApplyPagination(memberSearchCondition, pageRequest);

        //then
        assertThat(results.getSize()).isEqualTo(3);
        assertThat(results.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
    }

    @Test
    void searchApplyComplexPagination() {
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
        Page<Member> results = memberTestRepository.searchApplyComplexPagination(memberSearchCondition, pageRequest);

        //then
        assertThat(results.getSize()).isEqualTo(3);
        assertThat(results.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
    }
}