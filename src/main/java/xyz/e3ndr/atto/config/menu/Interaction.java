package xyz.e3ndr.atto.config.menu;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
public @interface Interaction {

    int upperBound() default Integer.MAX_VALUE;

    int lowerBound() default Integer.MIN_VALUE;

}
