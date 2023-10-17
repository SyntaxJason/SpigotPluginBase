package me.lauriichan.minecraft.pluginbase.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ExtensionCondition.ExtensionConditions.class)
public @interface ExtensionCondition {

    String name();

    boolean condition() default true;

    boolean activeByDefault() default false;

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    static @interface ExtensionConditions {

        ExtensionCondition[] value() default {};

    }

}
