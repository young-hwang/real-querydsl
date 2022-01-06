package io.ggammu.realquerydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.ggammu.realquerydsl.entity.Hello;
import io.ggammu.realquerydsl.entity.QHello;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class RealQuerydslApplicationTests {

	@PersistenceContext
	EntityManager entityManager;

	@Test
	void contextLoads() {
		Hello hello = new Hello();
		entityManager.persist(hello);

		JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(entityManager);
		QHello qHello = new QHello("h");

		Hello result = jpaQueryFactory.selectFrom(qHello).fetchOne();

		assertThat(result).isEqualTo(hello);
		assertThat(result.getId()).isEqualTo(hello.getId());
	}

}
