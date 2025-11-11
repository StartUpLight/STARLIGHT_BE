package starlight.shared;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import(BaseAndAbstractEntityJpaTest.AuditingTestConfig.class)
class BaseAndAbstractEntityJpaTest {

    @TestConfiguration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(name = "jpaAuditingHandler")
    @EnableJpaAuditing
    static class AuditingTestConfig {}

    @Autowired TestEntityManager entityManager;

    @Entity
    @Table(name = "t_sample_for_base_abstract_test")
    static class SampleEntity extends AbstractEntity {
        @Column(nullable = false)
        private String name;
        protected SampleEntity() {}
        public SampleEntity(String name) { this.name = name; }
        public void setName(String name) { this.name = name; }
    }

    @Test
    void 새_엔티티_저장시_IDENTITY_전략으로_ID_부여() {
        SampleEntity entity = new SampleEntity("first");
        assertThat(getId(entity)).isNull();

        entityManager.persist(entity);
        entityManager.flush();

        assertThat(getId(entity)).isNotNull();
    }

    @Test
    void 저장시_createdAt_modifiedAt_세팅되고_isDeleted_false_기본값() {
        SampleEntity entity = new SampleEntity("audit");
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();

        SampleEntity found = entityManager.find(SampleEntity.class, getId(entity));
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getModifiedAt()).isNotNull();
        assertThat(found.getIsDeleted()).isFalse();
    }

    @Test
    void 수정시_modifiedAt_갱신된다() throws Exception {
        SampleEntity entity = new SampleEntity("before");
        entityManager.persist(entity);
        entityManager.flush();
        entityManager.clear();

        SampleEntity f1 = entityManager.find(SampleEntity.class, getId(entity));
        LocalDateTime before = f1.getModifiedAt();

        Thread.sleep(75);

        f1.setName("after");
        entityManager.merge(f1);
        entityManager.flush();
        entityManager.clear();

        SampleEntity f2 = entityManager.find(SampleEntity.class, getId(entity));
        assertThat(f2.getModifiedAt()).isAfter(before);
    }

    private Long getId(SampleEntity entity) {
        Object id = entityManager.getId(entity);
        return (id == null) ? null : ((Number) id).longValue();
    }
}
