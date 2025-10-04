package starlight.application.member.required;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class ContextLoadTest {

    @Test
    @DisplayName("Spring ApplicationContext 로드 및 빈 확인")
    void printAllBeans(@Autowired ApplicationContext ctx) {
        System.out.println("=== ALL BEANS ===");
        for (String name : ctx.getBeanDefinitionNames()) {
            Object bean = ctx.getBean(name);
            System.out.println(name + " -> " + bean.getClass().getName());
        }

        // 특정 빈 확인
        System.out.println("\n=== CHECK SPECIFIC BEANS ===");
        System.out.println("spellCheckClient exists: " + ctx.containsBean("spellCheckClient"));
        System.out.println("spellCheckUtil exists: " + ctx.containsBean("spellCheckUtil"));
        System.out.println("daumSpellChecker exists: " + ctx.containsBean("daumSpellChecker"));
    }
}