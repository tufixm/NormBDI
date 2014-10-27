package jadex.commons.transformation.traverser;

import jadex.commons.SReflect;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 *  Processor that clones cloneable objects.
 */
public class CloneProcessor implements ITraverseProcessor
{
	/**
	 *  Test if the processor is applicable.
	 *  @param object The object.
	 *  @param targetcl	If not null, the traverser should make sure that the result object is compatible with the class loader,
	 *    e.g. by cloning the object using the class loaded from the target class loader.
	 *  @return True, if is applicable. 
	 */
	public boolean isApplicable(Object object, Class<?> clazz, boolean clone, ClassLoader targetcl)
	{
		return clone && (object instanceof Cloneable) && !clazz.isArray()
			&& (targetcl==null || clazz.equals(SReflect.classForName0(clazz.getName(), targetcl)));
	}
	
	/**
	 *  Process an object.
	 *  @param object The object.
	 *  @param targetcl	If not null, the traverser should make sure that the result object is compatible with the class loader,
	 *    e.g. by cloning the object using the class loaded from the target class loader.
	 *  @return The processed object.
	 */
	public Object process(Object object, Class<?> clazz, List<ITraverseProcessor> processors, 
		Traverser traverser, Map<Object, Object> traversed, boolean clone, ClassLoader targetcl, Object context)
	{
		try
		{
			Method	m = clazz.getMethod("clone", new Class[0]);
			Object ret = m.invoke(object, new Object[0]);
			traversed.put(object, ret);
			return ret;
		}
		catch(Exception e)
		{
			throw (e instanceof RuntimeException) ? (RuntimeException)e : new RuntimeException(e);
		}
	}
}
