package thut.essentials.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Configure
{
    String category();

    boolean needsMcRestart() default false;
}
