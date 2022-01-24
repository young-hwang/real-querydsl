package io.ggammu.realquerydsl.repository.support;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

/**
 * Querydsl 4.X 버전 Querydsl 지원 라이브러리
 *
 * @author Young Hwang
 * @see org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
 */
@Repository
public abstract class Querydsl4RepositorySupport {

//    private final PathBuilder<?> builder;

    private final Class domainClass;
    private EntityManager entityManager;
    private JPAQueryFactory queryFactory;
    private Querydsl querydsl;

    public Querydsl4RepositorySupport(Class<?> domainClass) {
        Assert.notNull(domainClass, "Domain class must not be null!");
        this.domainClass = domainClass;
        //this.builder = new PathBuilderFactory().create(domainClass);
    }

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        Assert.notNull(entityManager, "EntityManager must not be null!");
        JpaEntityInformation entityInformation = JpaEntityInformationSupport.getEntityInformation(domainClass, entityManager);
        SimpleEntityPathResolver resolver = SimpleEntityPathResolver.INSTANCE;
        EntityPath path = resolver.createPath(entityInformation.getJavaType());
        this.entityManager = entityManager;
        this.queryFactory = new JPAQueryFactory(entityManager);
        this.querydsl = new Querydsl(entityManager, new PathBuilder<>(path.getType(), path.getMetadata()));
    }

    /**
     * Callback to verify configuration. Used by containers.
     */
    @PostConstruct
    public void validate() {
        Assert.notNull(entityManager, "EntityManager must not be null!");
        Assert.notNull(querydsl, "Querydsl must not be null!");
        Assert.notNull(queryFactory, "QueryFactory must not be null!");
    }

    /**
     * Returns the {@link EntityManager}.
     *
     * @return the entityManager
     */
    @Nullable
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Returns the {@link JPAQueryFactory}
     *
     * @return the queryFactory
     */
    @Nullable
    protected JPAQueryFactory getQueryFactory() {
        return queryFactory;
    }

    /**
     * Returns the underlying Querydsl helper instance.
     *
     * @return
     */
    @Nullable
    protected Querydsl getQuerydsl() {
        return this.querydsl;
    }

    protected <T> JPAQuery<T> select(Expression<T> exprs) {
        return getQueryFactory().select(exprs);
    }

    protected <T> JPAQuery<T> selectFrom(EntityPath<T> from) {
        return getQueryFactory().selectFrom(from);
    }

    protected <T> Page<T> applyPagination(Pageable pageable, Function<JPAQueryFactory, JPAQuery> contentQuery) {
        JPAQuery jpaQuery = contentQuery.apply(getQueryFactory());
        List content = getQuerydsl().applyPagination(pageable, jpaQuery).fetch();
        return PageableExecutionUtils.getPage(content, pageable, jpaQuery::fetchCount);
    }

    protected <T> Page<T> applyPagination(Pageable pageable,
                                          Function<JPAQueryFactory, JPAQuery> contentQuery,
                                          Function<JPAQueryFactory, JPAQuery> countQuery) {
        JPAQuery jpaQuery = contentQuery.apply(getQueryFactory());
        List content = getQuerydsl().applyPagination(pageable, jpaQuery).fetch();
        JPAQuery jpaCountQuery = countQuery.apply(getQueryFactory());
        return PageableExecutionUtils.getPage(content, pageable, jpaCountQuery::fetchCount);
    }

}
