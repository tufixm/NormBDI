package jadex.commons.transformation.binaryserializer;

import jadex.commons.SReflect;
import jadex.commons.Tuple;
import jadex.commons.Tuple2;
import jadex.commons.Tuple3;
import jadex.commons.transformation.traverser.ITraverseProcessor;
import jadex.commons.transformation.traverser.Traverser;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 *  Codec for encoding and decoding URL objects.
 */
public class TupleCodec extends AbstractCodec
{
	/**
	 *  Tests if the decoder can decode the class.
	 *  @param clazz The class.
	 *  @return True, if the decoder can decode this class.
	 */
	public boolean isApplicable(Class clazz)
	{
		return SReflect.isSupertype(Tuple.class, clazz);
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
		Tuple ret = null;
		if(clazz.equals(Tuple3.class))
		{
			ret = new Tuple3(null, null, null);
		}
		else if (clazz.equals(Tuple2.class))
		{
			ret = new Tuple2(null, null);
		}
		else
		{
			ret =  new Tuple(null);
		}
		return ret;
	}
	
	/**
	 *  Decodes and adds sub-objects during decoding.
	 *  
	 *  @param object The instantiated object.
	 *  @param clazz The class of the object.
	 *  @param context The decoding context.
	 *  @return The finished object.
	 */
	public Object decodeSubObjects(Object object, Class clazz, DecodingContext context)
	{
		Object[] entities = (Object[])BinarySerializer.decodeObject(context);
		try
		{
			Field fentities = SReflect.getField(object.getClass(), "entities");
			fentities.setAccessible(true);
			fentities.set(object, entities);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		return object;
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
		Object[] entities = ((Tuple)object).getEntities();
		traverser.traverse(entities, entities.getClass(), traversed, processors, clone, null, ec);
		return object;
	}
}
