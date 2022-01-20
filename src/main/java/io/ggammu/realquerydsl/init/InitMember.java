package io.ggammu.realquerydsl.init;

import io.ggammu.realquerydsl.entity.Member;
import io.ggammu.realquerydsl.entity.Team;
import java.util.Arrays;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("local")
@RequiredArgsConstructor
@Component
public class InitMember {

    private final InitMemberService initMemberService;

    @PostConstruct
    public void init() {
        initMemberService.init();
    }

    @Component
    static class InitMemberService {

        @PersistenceContext
        private EntityManager em;

        @Transactional
        public void init() {
            Team teamA = new Team("TeamA");
            Team teamB = new Team("TeamB");

            em.persist(teamA);
            em.persist(teamB);

            IntStream range = IntStream.range(0, 100);
            range.forEach(i -> {
                Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                em.persist(new Member("member" + i, i, selectedTeam));
            });

        }

    }


}
