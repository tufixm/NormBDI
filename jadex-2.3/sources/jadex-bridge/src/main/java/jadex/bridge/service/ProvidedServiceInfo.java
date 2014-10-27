package jadex.bridge.service;

import jadex.bridge.ClassInfo;
import jadex.commons.SReflect;


/**
 *  Info for provided services.
 */
public class ProvidedServiceInfo
{
	//-------- attributes --------

	/** The name (used for referencing). */
	protected String name;
	
	/** The service interface type as string. */
	protected String typename;
	
	/** The type. */
	protected ClassInfo type;
	
	/** The service implementation. */
	protected ProvidedServiceImplementation implementation;
	
	/** Publish information. */
	protected PublishInfo publish;
		
	//-------- constructors --------
	
	/**
	 *  Create a new service info.
	 */
	public ProvidedServiceInfo()
	{
		// bean constructor
	}
	
	/**
	 *  Create a new service info.
	 */
	public ProvidedServiceInfo(String name, Class<?> type, ProvidedServiceImplementation implementation, PublishInfo publish)
	{
		this.name = name;
		this.implementation = implementation;
		this.publish = publish;
		setType(new ClassInfo(SReflect.getClassName(type)));
	}
	
	/**
	 *  Create a new service info.
	 */
	public ProvidedServiceInfo(String name, ClassInfo type, ProvidedServiceImplementation implementation, PublishInfo publish)
	{
		this.name = name;
		this.implementation = implementation;
		this.publish = publish;
		setType(type);
	}
	
//	/**
//	 *  Create a new service info.
//	 */
//	public ProvidedServiceInfo(ProvidedServiceInfo orig)
//	{
//		this(orig.getType(), new ProvidedServiceImplementation(orig.getImplementation()));
//	}
	
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
	 *  Get the implementation.
	 *  @return The implementation.
	 */
	public ProvidedServiceImplementation getImplementation()
	{
		return implementation;
	}

	/**
	 *  Set the implementation.
	 *  @param implementation The implementation to set.
	 */
	public void setImplementation(ProvidedServiceImplementation implementation)
	{
		this.implementation = implementation;
	}
	
	/**
	 *  Get the publish.
	 *  @return The publish.
	 */
	public PublishInfo getPublish()
	{
		return publish;
	}

	/**
	 *  Set the publish.
	 *  @param publish The publish to set.
	 */
	public void setPublish(PublishInfo publish)
	{
		this.publish = publish;
	}

	/**
	 *  Get the string representation.
	 */
	public String toString()
	{
		return "ProvidedServiceInfo(name="+name+", type="+ type + ", implementation="+ implementation + ")";
	}
}
