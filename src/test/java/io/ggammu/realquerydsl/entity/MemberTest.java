package io.ggammu.realquerydsl.entity;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.ggammu.realquerydsl.dto.MemberDto;
import io.ggammu.realquerydsl.dto.QMemberDto;
import io.ggammu.realquerydsl.dto.UserDto;
import static io.ggammu.realquerydsl.entity.QMember.member;
import static io.ggammu.realquerydsl.entity.QTeam.team;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class MemberTest {

    @PersistenceContext
    EntityManager entityManager;

    JPAQueryFactory query;

    @BeforeEach
    void before() {
        query = new JPAQueryFactory(entityManager);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        entityManager.persist(teamA);
        entityManager.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(member3);
        entityManager.persist(member4);

        entityManager.flush();
        entityManager.clear();

        List<Member> members = entityManager.createQuery("select m from Member m", Member.class).getResultList();
        members.forEach(System.out::println);
    }

    @Test
    void startJPQL() {
        Member findMember = entityManager.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void startQueryDsl() {
        Member findMember = query.selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void search() {
        Member findMember = query.selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void resultFetch() {
//        List<Member> fetch = query.selectFrom(member)
//                .fetch();
//
//        Member fetchOne = query.selectFrom(QMember.member)
//                .fetchOne();
//
//        Member fetchFirst = query.selectFrom(QMember.member)
//                .fetchFirst();

//        QueryResults<Member> fetchResults = query.selectFrom(member)
//                .fetchResults();
//
//        fetchResults.getTotal();
//        List<Member> results = fetchResults.getResults();

        long count = query.selectFrom(member)
                .fetchCount();

    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 오름차순(asc)
     * 단 2에서 회원 이름이 없을 시 마지막에 출력(nulls last)
     */
    @Test
    void sort() {
        entityManager.persist(new Member(null, 100));
        entityManager.persist(new Member("member5", 100));
        entityManager.persist(new Member("member6", 100));

        List<Member> result = query.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    void paging1() {
        List<Member> results = query.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(results.size()).isEqualTo(2);
    }

    @Test
    void paging2() {
        QueryResults<Member> fetchResults = query.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(fetchResults.getTotal()).isEqualTo(4);
        assertThat(fetchResults.getLimit()).isEqualTo(2);
        assertThat(fetchResults.getOffset()).isEqualTo(1);
        assertThat(fetchResults.getResults().size()).isEqualTo(2);
    }

    @Test
    void aggregation() {
        List<Tuple> result = query.select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    @Test
    public void group() throws Exception {
        //given
        List<Tuple> results = query.select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        //when
        Tuple teamA = results.get(0);
        Tuple teamB = results.get(1);

        //then
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    /**
     *
     */
    @Test
    public void join() {
        //given
        List<Member> result = query
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        List<Member> leftResult = query
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();
        //when

        //then
    }

    /**
     * 세타조인(연관관계가 없는 필드로 조인)
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join() {
        //given
        entityManager.persist(new Member("teamA"));
        entityManager.persist(new Member("teamB"));

        //when
        List<Member> result = query
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        //then
        result.forEach(r -> System.out.println("t=" + r));
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /**
     * 연관 관계 없는 엔티티 외부 조인
     * 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     */
    @Test
    public void join_on_no_relation() {
        //given
        entityManager.persist(new Member("teamA"));
        entityManager.persist(new Member("teamB"));

        //when
        List<Tuple> results = query
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        //then
        results.forEach(r -> System.out.println("t=" + r));
    }

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Test
    void fetchJoinNo() {
        //given
        entityManager.flush();
        entityManager.clear();

        Member findMember = query.selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        //when
        boolean loaded = entityManagerFactory.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        //then
        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

    @Test
    void fetchJoinUse() {
        //given
        entityManager.flush();
        entityManager.clear();

        Member findMember = query.selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        //when
        boolean loaded = entityManagerFactory.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        //then
        assertThat(loaded).as("페치 조인 적용").isTrue();
    }

    @Test
    void subQuery() {
        //given
        QMember memberSub = new QMember("memberSub");

        List<Member> result = query
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        //when

        //then
        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    @Test
    void subQueryGoe() {
        //given
        QMember memberSub = new QMember("memberSub");

        List<Member> result = query
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        //when

        //then
        assertThat(result).extracting("age")
                .containsExactly(30, 40);
    }

    @Test
    void subQueryIn() {
        //given
        QMember memberSub = new QMember("memberSub");

        List<Member> result = query
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        //when

        //then
        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);
    }

    @Test
    void selectSubQuery() {
        //given
        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = query
                .select(member.username,
                        JPAExpressions.select(memberSub.age.avg())
                                .from(memberSub)
                )
                .from(member)
                .fetch();

        //when

        //then
        result.forEach(System.out::println);
    }


    @Test
    void basicCase() {
        //given
        List<String> result = query
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        //when

        //then
        result.forEach(System.out::println);
    }

    @Test
    void complexCase() {
        //given
        List<String> result = query
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        //when

        //then
        result.forEach(System.out::println);
    }

    @Test
    void constant() {
        //given
        List<Tuple> result = query
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        //when

        //then
        result.forEach(System.out::println);
    }

    @Test
    void concat() {
        //given
        List<String> fetch = query.select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();
        //when

        //then
        fetch.forEach(System.out::println);
    }

    @Test
    void simpleProjection() {
        //given
        List<String> fetch = query
                .select(member.username)
                .from(member)
                .fetch();
        //when

        //then
        fetch.forEach(System.out::println);
    }

    @Test
    void tupleProjection() {
        //given
        List<Tuple> fetch = query
                .select(member.username, member.age)
                .from(member)
                .fetch();
        //when

        //then
        for (Tuple tuple : fetch) {
            String name = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username = " + name);
            System.out.println("age = " + age);
        }
    }

    @Test
    void findDtoByJpql() {
        //given
        List<MemberDto> resultList = entityManager.createQuery(
                        "select new io.ggammu.realquerydsl.dto.MemberDto(m.username, m.age) from Member m",
                        MemberDto.class)
                .getResultList();
        //when

        //then
        for (MemberDto memberDto : resultList) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    void findDtoBySetter() {
        //given
        List<MemberDto> fetch = query
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age
                ))
                .from(member)
                .fetch();

        //when

        //then
        for (MemberDto memberDto : fetch) {
            System.out.println("member = " + memberDto);
        }
    }

    @Test
    void findDtoByField() {
        //given
        List<MemberDto> fetch = query
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age
                ))
                .from(member)
                .fetch();

        //when

        //then
        for (MemberDto memberDto : fetch) {
            System.out.println("member = " + memberDto);
        }
    }

    @Test
    void findDtoByConstructor() {
        //given
        List<MemberDto> fetch = query
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age
                ))
                .from(member)
                .fetch();
        //when

        //then
        for (MemberDto memberDto : fetch) {
            System.out.println("member = " + memberDto);
        }
    }

    @Test
    void findUserDtoByField() {
        //given
        List<UserDto> fetch = query
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        member.age
                ))
                .from(member)
                .fetch();
        //when

        //then
        for (UserDto userDto : fetch) {
            System.out.println("member = " + userDto);
        }
    }

    @Test
    void findDtoByQueryProjection() {
        //given
        List<MemberDto> fetch = query
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        //when

        //then
        for (MemberDto memberDto : fetch) {
            System.out.println("member = " + memberDto);
        }
    }

    @Test
    void dynamicQueryByBooleanBuilder() {
        //given
        String usernameParam = "member1";
        Integer ageParam = null;

        List<Member> result = searchMember(usernameParam, ageParam);
        //when

        //then
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember(String usernameParam, Integer ageParam) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (usernameParam != null)
            booleanBuilder.and(member.username.eq(usernameParam));

        if (ageParam != null)
            booleanBuilder.and(member.age.eq(ageParam));

        List<Member> fetch = query
                .selectFrom(member)
                .where(booleanBuilder)
                .fetch();

        return fetch;
    }

    @Test
    void dynamicQueryByWhereParam() {
        //given
        String usernameParam = "member1";
        Integer ageParam = null;

        List<Member> result = searchMember2(usernameParam, ageParam);

        //when

        //then
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameParam, Integer ageParam) {
        List<Member> fetch = query
                .selectFrom(member)
                .where(usernameEq(usernameParam),
                        ageEq(ageParam))
                .fetch();

        return fetch;
    }

    private Predicate ageEq(Integer ageParam) {
        if (ageParam != null)
            return member.age.eq(ageParam);
        else
            return null;
    }

    private Predicate usernameEq(String usernameParam) {
        if (usernameParam != null)
            return member.username.eq(usernameParam);
        else
            return null;
    }

    @Commit
    @Test
    void update() {
        //given
        long count = query
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();

        //when

        List<Member> fetch = query
                .selectFrom(member)
                .fetch();

        fetch.forEach(System.out::println);
        //then
        assertThat(count).isEqualTo(2);
    }

}