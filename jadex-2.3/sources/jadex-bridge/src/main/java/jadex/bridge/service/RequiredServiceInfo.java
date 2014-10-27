package jadex.bridge.service;

import jadex.bridge.ClassInfo;
import jadex.bridge.modelinfo.UnparsedExpression;
import jadex.commons.SReflect;

import java.util.ArrayList;
import java.util.List;

/**
 *  Struct for information about a required service.
 */
public class RequiredServiceInfo
{
	//-------- constants --------
	
	/** Local component scope. */
	public static final String SCOPE_LOCAL = "local";
	
	/** Component scope. */
	public static final String SCOPE_COMPONENT = "component";
	
	/** Application scope. */
	public static final String SCOPE_APPLICATION = "application";

	/** Platform scope. */
	public static final String SCOPE_PLATFORM = "platform";

	/** Global scope. */
	public static final String SCOPE_GLOBAL = "global";
	
	/** Upwards scope. */
	public static final String SCOPE_UPWARDS = "upwards";
	
	/** Parent scope. */
	public static final String SCOPE_PARENT = "parent";
	
	//-------- attributes --------

	// service description
	
	/** The component internal service name. */
	protected String name;
	
	/** The type. */
	protected ClassInfo type;
	
	/** Flag if multiple services should be returned. */
	protected boolean multiple;
	
	/** The multiplex type. */
	protected ClassInfo multiplextype;

	// binding specification
	
	/** The default binding. */
	protected RequiredServiceBinding binding;
	
	/** The list of interceptors. */
	protected List<UnparsedExpression> interceptors;
	
	//-------- constructors --------
	
	/**
	 *  Create a new service info.
	 */
	public RequiredServiceInfo()
	{
		// bean constructor
		
		// Hack!!! Initialize with default values to resemble annotation behavior.
		this(null, null);
	}
	
	/**
	 *  Create a new service info.
	 */
	public RequiredServiceInfo(String name, Class type)
	{
		this(name, type, RequiredServiceInfo.SCOPE_APPLICATION, null);
	}
	
	/**
	 *  Create a new service info.
	 */
	public RequiredServiceInfo(Class<?> type)
	{
		this(null, type, RequiredServiceInfo.SCOPE_APPLICATION, null);
	}
	
	/**
	 *  Create a new service info.
	 */
	public RequiredServiceInfo(String name, Class<?> type, String scope, Class<?> multiplextype)
	{
		this(name, type, false, multiplextype, new RequiredServiceBinding(name, scope));
	}
	
	/**
	 *  Create a new service info.
	 */
	public RequiredServiceInfo(String name, Class<?> type, boolean multiple, 
		Class<?> multiplextype, RequiredServiceBinding binding)
	{
		this.name = name;
		if(type!=null)
			setType(new ClassInfo(SReflect.getClassName(type)));
		if(multiplextype!=null)
			setMultiplexType(new ClassInfo(SReflect.getClassName(multiplextype)));
		this.multiple = multiple;
		this.binding = binding;
	}

	//-------- methods --------

	/**
	 *  Get the name.
	 *  @return the name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 *  Set the name.
	 *  @param name The name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 *  Get the type.
	 *  @return The type.
	 */
	public ClassInfo getType()
	{
		return type;
	}

	/**
	 *  Set the type.
	 *  @param type The type to set.
	 */
	public void setType(ClassInfo type)
	{
		this.type = type;
	}
	
	/**
	 *  Get the multiplextype.
	 *  @return The multiplextype.
	 */
	public ClassInfo getMultiplexType()
	{
		return multiplextype;
	}

	/**
	 *  Set the multiplextype.
	 *  @param multiplextype The multiplextype to set.
	 */
	public void setMultiplexType(ClassInfo multiplextype)
	{
		this.multiplextype = multiplextype;
	}

	/**
	 *  Get the multiple.
	 *  @return the multiple.
	 */
	public boolean isMultiple()
	{
		return multiple;
	}

	/**
	 *  Set the multiple.
	 *  @param multiple The multiple to set.
	 */
	public void setMultiple(boolean multiple)
	{
		this.multiple = multiple;
	}
	
	/**
	 *  Get the binding.
	 *  @return the binding.
	 */
	public RequiredServiceBinding getDefaultBinding()
	{
		return binding;
	}

	/**
	 *  Set the binding.
	 *  @param binding The binding to set.
	 */
	public void setDefaultBinding(RequiredServiceBinding binding)
	{
		this.binding = binding;
	}
	
	/**
	 *  Add an interceptor.
	 *  @param interceptor The interceptor.
	 */
	public void addInterceptor(UnparsedExpression interceptor)
	{
		if(interceptors==null)
			interceptors = new ArrayList<UnparsedExpression>();
		interceptors.add(interceptor);
	}
	
	/**
	 *  Remove an interceptor.
	 *  @param interceptor The interceptor.
	 */
	public void removeInterceptor(UnparsedExpression interceptor)
	{
		interceptors.remove(interceptor);
	}
	
	/**
	 *  Get the interceptors.
	 *  @return All interceptors.
	 */
	public UnparsedExpression[] getInterceptors()
	{
		return interceptors==null? new UnparsedExpression[0]: 
			interceptors.toArray(new UnparsedExpression[interceptors.size()]);
	}
}
