package org.noear.solon.extend.validation.annotation;

import org.noear.solon.annotation.XNote;

import java.lang.annotation.*;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DecimalMax {
    /**
     * param names
     * */
    @XNote("param names")
    String[] check();

    double value();
}
