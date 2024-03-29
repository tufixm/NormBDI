package jadex.bdiv3.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GoalDropCondition
{
	/**
	 *  The events this condition should react to.
	 */
	public String[] events() default {};
}
