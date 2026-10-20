package io.micronaut.agentskills;

import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.type.Argument;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillFrontmatterIntrospectionTest {

    @Test
    void skillFrontmatterIsIntrospected() {
        BeanIntrospection<SkillFrontmatter> introspection = BeanIntrospection.getIntrospection(SkillFrontmatter.class);
        assertEquals(Set.of("name", "description", "license", "compatibility", "metadata", "allowedTools"),
            Set.of(introspection.getPropertyNames()));
    }

    @Test
    void constraintsAreAvailableAtRuntime() {
        BeanIntrospection<SkillFrontmatter> introspection = BeanIntrospection.getIntrospection(SkillFrontmatter.class);
        BeanProperty<SkillFrontmatter, Object> name = introspection.getRequiredProperty("name", Object.class);
        assertTrue(name.hasAnnotation(NotBlank.class));
        assertTrue(name.hasAnnotation(Size.class));
        assertTrue(name.hasAnnotation(Pattern.class));
        assertEquals(SkillFrontmatter.NAME_REGEX, name.stringValue(Pattern.class, "regexp").orElseThrow());

        BeanProperty<SkillFrontmatter, Map> metadata = introspection.getRequiredProperty("metadata", Map.class);
        Argument<?>[] typeParameters = metadata.asArgument().getTypeParameters();
        assertTrue(typeParameters[0].getAnnotationMetadata().hasAnnotation(NotBlank.class));
        assertTrue(typeParameters[1].getAnnotationMetadata().hasAnnotation(NotNull.class));
    }

    @Test
    void canInstantiateThroughIntrospection() {
        BeanIntrospection<SkillFrontmatter> introspection = BeanIntrospection.getIntrospection(SkillFrontmatter.class);
        SkillFrontmatter frontmatter = introspection.instantiate("n", "d", null, null, null, null);
        assertEquals(new SkillFrontmatter("n", "d", null, null, null, null), frontmatter);
    }

    @Test
    void skillIsIntrospected() {
        BeanIntrospection<Skill> introspection = BeanIntrospection.getIntrospection(Skill.class);
        assertEquals(2, introspection.getPropertyNames().length);
    }
}
