package jadex.bpmn.model.task.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  Meta information for a task,
 *  e.g. used by editor to fill in
 *  parameters.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Task
{
	/**
	 *  A human readable description of the task.
	 */
	String description() default "";
	
	/**
	 *  The parameters of the task.
	 */
	TaskParameter[] parameters() default {};
}
