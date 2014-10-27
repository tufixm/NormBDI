package jadex.commons.transformation.binaryserializer;

import jadex.commons.SReflect;
import jadex.commons.transformation.traverser.IBeanIntrospector;
import jadex.commons.transformation.traverser.ITraverseProcessor;
import jadex.commons.transformation.traverser.Traverser;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

/**
 *  Codec for encoding and decoding exception objects.
 */
public class ThrowableCodec extends AbstractCodec
{
	/** Bean introspector for inspecting beans. */
	protected IBeanIntrospector intro = BeanIntrospectorFactory.getInstance().getBeanIntrospector(500);
	
	/**
	 *  Tests if the decoder can decode the class.
	 *  @param clazz The class.
	 *  @return True, if the decoder can decode this class.
	 */
	public boolean isApplicable(Class clazz)
	{
		return SReflect.isSupertype(Throwable.class, clazz);
	}
	
	/**
	 *  Creates the object during decoding.
	 *  
	 *  @param clazz The class of the object.
	 *  @param context The decoding context.
	 *  @return The created object.
	 */
	public Object createObject(Class clazz, DecodingContext context)
	{
		Object ret = null;
		String msg = (String)BinarySerializer.decodeObject(context);
		Throwable cause = (Throwable)BinarySerializer.decodeObject(context);

		try
		{
			Constructor<?> con = clazz.getConstructor(new Class<?>[]{String.class, Throwable.class});
			ret = con.newInstance(new Object[]{msg, cause});
		}
		catch(Exception e)
		{
		}
		
		if(ret==null)
		{
			try
			{
				Constructor<?> con = clazz.getConstructor(new Class<?>[]{String.class});
				ret = con.newInstance(new Object[]{msg});
			}
			catch(Exception e)
			{
			}
		}
		
		// Try find empty constructor
		if(ret==null)
		{
			try
			{
				Constructor<?> con = clazz.getConstructor(new Class<?>[0]);
				ret = con.newInstance(new Object[0]);
			}
			catch(Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
		BeanCodec.readBeanProperties(ret, clazz, context, intro);
		
		return ret;
	}
	
	
	/**
	 *  Test if the processor is applicable.
	 *  @param object The object.
	 *  @param targetcl	If not null, the traverser should make sure that the result object is compatible with the class loader,
	 *    e.g. by cloning the object using the class loaded from the target class loader.
	 *  @return True, if is applicable. 
	 */
	public boolean isApplicable(Object object, Class<?> clazz, boolean clone, ClassLoader targetcl)
	{
		return isApplicable(clazz);
	}
	
	/**
	 *  Encode the object.
	 */
	public Object encode(Object object, Class<?> clazz, List<ITraverseProcessor> processors, 
		Traverser traverser, Map<Object, Object> traversed, boolean clone, EncodingContext ec)
	{
		Throwable t = (Throwable)object;
		
		traverser.traverse(t.getMessage(), String.class, traversed, processors, clone, ec.getClassLoader(), ec);
	
		Object val = t.getCause();
		traverser.traverse(val, val!=null? val.getClass(): Throwable.class, 
			traversed, processors, clone, null, ec);

		BeanCodec.writeBeanProperties(object, clazz, processors, traverser, traversed, clone, ec, intro);
		
		return object;
	}
}

