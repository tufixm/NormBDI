package jadex.commons.transformation.binaryserializer;

import jadex.commons.SReflect;
import jadex.commons.transformation.annotations.Classname;
import jadex.commons.transformation.traverser.BeanProperty;
import jadex.commons.transformation.traverser.IBeanIntrospector;
import jadex.commons.transformation.traverser.ITraverseProcessor;
import jadex.commons.transformation.traverser.Traverser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Codec for encoding and decoding Java Beans.
 *
 */
public class BeanCodec extends AbstractCodec
{
	protected static final int INTROSPECTOR_CACHE_SIZE = 5000;
	
	/** Bean introspector for inspecting beans. */
	protected IBeanIntrospector intro = BeanIntrospectorFactory.getInstance().getBeanIntrospector(INTROSPECTOR_CACHE_SIZE);
	
	/**
	 *  Tests if the decoder can decode the class.
	 *  @param clazz The class.
	 *  @return True, if the decoder can decode this class.
	 */
	public boolean isApplicable(Class clazz)
	{
		return true;
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
		Object bean = null;
		boolean isanonclass = context.readBoolean();
		if(isanonclass)
		{
			String correctcl = context.readString();
			
			Classname cl = getAnonClassName(clazz);
			if (cl == null || (!correctcl.equals(cl.value())))
			{
				clazz = findCorrectInnerClass(0, SReflect.getClassName(clazz), correctcl, context.getClassloader());
			}
			if (clazz != null)
			{
				Constructor	c	= clazz.getDeclaredConstructors()[0];
				c.setAccessible(true);
				Class[] paramtypes = c.getParameterTypes();
				Object[] paramvalues = new Object[paramtypes.length];
				for(int i=0; i<paramtypes.length; i++)
				{
					if(paramtypes[i].equals(boolean.class))
					{
						paramvalues[i] = Boolean.FALSE;
					}
					else if(SReflect.isBasicType(paramtypes[i]))
					{
						paramvalues[i] = 0;
					}
				}
				
				try
				{
					bean = c.newInstance(paramvalues);
				}
				catch (Exception e)
				{
					context.getErrorReporter().exceptionOccurred(e);
					//throw new RuntimeException(e);
				}
			}
			else
			{
				context.getErrorReporter().exceptionOccurred(new ClassNotFoundException("Inner Class not found: " + context.getCurrentClassName() + " Converted: " + cl));
			}
			
		}
		else
		{
			if (clazz != null)
			{
				try
				{
					bean = clazz.newInstance();
				}
				catch (Exception e)
				{
					context.getErrorReporter().exceptionOccurred(e);
					//throw new RuntimeException(e);
				}
			}
			else
			{
				context.getErrorReporter().exceptionOccurred(new ClassNotFoundException("Class not found: " + context.getCurrentClassName()));
			}
		}
		
		return bean;
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
		if(object != null)
		{
			readBeanProperties(object, clazz, context, intro);
//			Map props = intro.getBeanProperties(clazz, true, false);
//			int size = (int) context.readVarInt();
//			for (int i = 0; i < size; ++i)
//			{
//				String name = context.readString();
//				Object val = null;
//				val = BinarySerializer.decodeObject(context);
//				if (object != null)
//				{
//					try
//					{
//						((BeanProperty) props.get(name)).setPropertyValue(object, val);
//					}
//					catch (Exception e)
//					{
//						context.getErrorReporter().exceptionOccurred(e);
//					}
//				}
//			}
		}
		else
		{
			// Object failed to instantiate, skip sub-objects.
			int size = (int) context.readVarInt();
			for (int i = 0; i < size; ++i)
			{
				context.readString();
				BinarySerializer.decodeObject(context);
			}
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
		return object != null;
	}
	
	protected Set<Class> nonanonclasscache = new HashSet<Class>();
	
	/**
	 *  Encode the object.
	 */
	public Object encode(Object object, Class<?> clazz, List<ITraverseProcessor> processors, 
		Traverser traverser, Map<Object, Object> traversed, boolean clone, EncodingContext ec)
	{
		if (!nonanonclasscache.contains(clazz))
		{
			if (clazz != null && clazz.isAnonymousClass())
			{
				// Flag class is inner class.
				ec.writeBoolean(true);
				
				Classname cn = getAnonClassName(clazz);
				
				if (cn == null)
					throw new RuntimeException("Anonymous Class without Classname identifier not supported: " + String.valueOf(clazz));
				
				ec.writeString(cn.value());
				
				
			}
			else
			{
				ec.writeBoolean(false);
				nonanonclasscache.add(clazz);
			}
		}
		else
		{
			ec.writeBoolean(false);
		}
		
		writeBeanProperties(object, clazz, processors, traverser, traversed, clone, ec, intro);
		
//		Map props = intro.getBeanProperties(clazz, true, false);
//		
//		List<String> names = new ArrayList<String>();
//		List<Object> values = new ArrayList<Object>();
//		List<Class> clazzes = new ArrayList<Class>();
//		for(Iterator it=props.keySet().iterator(); it.hasNext(); )
//		{
//			BeanProperty prop = (BeanProperty)props.get(it.next());
//			Object val = prop.getPropertyValue(object);
//			if (val != null)
//			{
//				names.add(prop.getName());
//				clazzes.add(prop.getType());
//				values.add(val);
//			}
//		}
//		ec.writeVarInt(names.size());
//		
//		for (int i = 0; i < names.size(); ++i)
//		{
//			ec.writeString(names.get(i));
//			Object val = values.get(i);
//			traverser.traverse(val, clazzes.get(i), traversed, processors, clone, null, ec);
//		}
		
		/*for(Iterator it=props.keySet().iterator(); it.hasNext(); )
		{
			try
			{
				String name = (String)it.next();
				BeanProperty prop = (BeanProperty)props.get(name);
				if (prop.getGetter() != null && prop.getSetter() != null)
				{
					Object val = prop.getGetter().invoke(object, new Object[0]);
					//System.out.println(val);
					//System.out.println(prop.getName());
					//if (val == null)
						//BinarySerializer.NULL_HANDLER.process(val, prop.getType(), null, null, null, false, context);
					//else
						//traverser.traverse(val, prop.getType(), traversed, processors, clone, context);
					if (val != null)
					{
						ec.writeString(name);
						traverser.traverse(val, prop.getType(), traversed, processors, clone, context);
					}
				}
			}
			catch(Exception e)
			{
				throw (e instanceof RuntimeException) ? (RuntimeException)e : new RuntimeException(e);
			}
		}*/
		
		return object;
	}
	
	/**
	 * 
	 */
	public static void writeBeanProperties(Object object, Class<?> clazz, List<ITraverseProcessor> processors, 
		Traverser traverser, Map<Object, Object> traversed, boolean clone, EncodingContext ec, IBeanIntrospector intro)
	{
		Map props = intro.getBeanProperties(clazz, true, false);
		
		List<String> names = new ArrayList<String>();
		List<Object> values = new ArrayList<Object>();
		List<Class> clazzes = new ArrayList<Class>();
		for(Iterator it=props.keySet().iterator(); it.hasNext(); )
		{
			BeanProperty prop = (BeanProperty)props.get(it.next());
			Object val = prop.getPropertyValue(object);
			if(val != null)
			{
				names.add(prop.getName());
				clazzes.add(prop.getType());
				values.add(val);
			}
		}
		ec.writeVarInt(names.size());
		
		for(int i = 0; i < names.size(); ++i)
		{
			ec.writeString(names.get(i));
			Object val = values.get(i);
			traverser.traverse(val, clazzes.get(i), traversed, processors, clone, null, ec);
		}
	}
	
	/**
	 * 
	 */
	public static void readBeanProperties(Object object, Class clazz, DecodingContext context, IBeanIntrospector intro)
	{
		Map props = intro.getBeanProperties(clazz, true, false);
		int size = (int) context.readVarInt();
		for (int i = 0; i < size; ++i)
		{
			String name = context.readString();
			Object val = null;
			val = BinarySerializer.decodeObject(context);
			if(object != null)
			{
				try
				{
					((BeanProperty) props.get(name)).setPropertyValue(object, val);
				}
				catch (Exception e)
				{
					context.getErrorReporter().exceptionOccurred(e);
				}
			}
		}
	}
	
	/**
	 *  Attempts to find the correct inner class (compilers have different ways enumerating anonymous inner classes).
	 *  
	 * 	@param level Enclosement level being searched, 0 being the level of the target class.
	 * 	@param startname The name as originally encoded.
	 * 	@param annotatedname Annotation marker for the correct class.
	 * 	@param classloader The classloader.
	 * 	@return The targeted inner class or null if not found.
	 */
	private static final Class findCorrectInnerClass(int level, String startname, String annotatedname, ClassLoader classloader)
	{
		int marker = 0;
		String basename = startname;
		
		for (int i = -1; i < level; ++i)
		{
			marker = basename.lastIndexOf('$');
			if (marker == -1)
				return null;
			basename = basename.substring(0, marker);
		}
		basename += "$";
		int exclude = Integer.valueOf(startname.substring(marker + 1, startname.length()));
		Class ret = null;
		
		int classindex = 0;
		boolean searching = true;
		while (searching)
		{
			if (classindex != exclude)
			{
				String candidatename = basename + classindex;
				
				try
				{
					Class candclass = SReflect.findClass(candidatename, null, classloader);
					
					if (level == 0)
					{
						Classname candclassname = getAnonClassName(candclass);
						if (candclassname != null && annotatedname.equals(candclassname.value()))
						{
							ret = candclass;
							searching = false;
						}
					}
					else
					{
						ret = findCorrectInnerClass(level - 1, startname, annotatedname, classloader);
						if (ret != null)
							searching = false;
					}
				}
				catch (ClassNotFoundException e)
				{
					if (classindex != 0)
					{
						searching = false;
					}
				}
			}
			++classindex;
		}
		
		if (ret == null)
		{
			ret = findCorrectInnerClass(level + 1, startname, annotatedname, classloader);
		}
		
		return ret;
	}
	
	/**
	 *  Attempts to find the "Classname" annotation for an anonymous inner class.
	 *  @param clazz The class.
	 *  @return The identifier or null if none was found.
	 */
	private static final Classname getAnonClassName(Class clazz)
	{
		Field[] fields = clazz.getFields();
		Classname cn = null;
		for (int i = 0; i < fields.length && cn == null; ++i)
		{
			if (fields[i].isAnnotationPresent(Classname.class))
			{
				cn = fields[i].getAnnotation(Classname.class);
			}
		}
		
		Method[] methods = clazz.getMethods();
		for (int i = 0; i < methods.length && cn == null; ++i)
		{
			if (methods[i].isAnnotationPresent(Classname.class))
			{
				cn = methods[i].getAnnotation(Classname.class);
			}
		}
		
		return cn;
	}
}
