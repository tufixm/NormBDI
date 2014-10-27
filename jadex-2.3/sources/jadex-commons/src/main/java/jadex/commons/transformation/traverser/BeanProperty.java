package jadex.commons.transformation.traverser;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *  This class is a struct for saving data about an inspected bean property.
 */
public class BeanProperty
{
	//-------- attributes --------

	/** The name. */
	protected String name;

	/** The type. */
	protected Class<?>	type;

	/** The getter. */
	protected Method getter;

	/** The setter. */
	protected Method setter;
	
	/** The setter type. */
	protected Class	settertype;

	/** The field. */
	protected Field field;
	
	/** Accessor delegate provider. */
	protected IBeanDelegateProvider delegateprovider;

	
	//-------- constructors --------
	
	/**
	 *  Create a new bean property.
	 */
	public BeanProperty() 
	{ 
	}
	
	/**
	 *  Create a new bean property.
	 */
	public BeanProperty(String name, Class type, Method getter, Method setter, Class settertype, IBeanDelegateProvider delegateprovider)
	{
		this.name = name;
		this.type = type;
		this.getter = getter;
		this.setter = setter;
		this.settertype = settertype;
		this.delegateprovider = delegateprovider;
	}
	
	/**
	 *  Create a new bean property.
	 */
	public BeanProperty(String name, Field field, IBeanDelegateProvider delegateprovider)
	{
		this.name = name;
		this.type = field.getType();
		this.settertype = type;
		this.field = field;
		this.delegateprovider = delegateprovider;
	}

	//-------- methods --------
	
	/**
	 *  Get the name.
	 *  @return The name.
	 */
	public String getName()
	{
		return this.name;
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
	public Class getType()
	{
		return this.type;
	}

	/**
	 *  Set the type.
	 *  @param type The type to set.
	 */
	public void setType(Class type)
	{
		this.type = type;
	}

	/**
	 *  Get the getter.
	 *  @return The getter.
	 */
	public Method getGetter()
	{
		return this.getter;
	}

	/**
	 *  Set the getter.
	 *  @param getter The getter to set.
	 */
	public void setGetter(Method getter)
	{
		this.getter = getter;
	}

	/**
	 *  Get the setter.
	 *  @return The setter.
	 */
	public Method getSetter()
	{
		return this.setter;
	}

	/**
	 *  Set the setter.
	 *  @param setter The setter to set.
	 */
	public void setSetter(Method setter)
	{
		this.setter = setter;
	}

	/**
	 *  Get the setter_type.
	 *  @return The setterttype.
	 */
	public Class getSetterType()
	{
		return this.settertype;
	}

	/**
	 *  Set the setter type.
	 *  @param settertype The setter type to set.
	 */
	public void setSetterType(Class settertype)
	{
		this.settertype = settertype;
	}

	/**
	 *  Get the field.
	 *  @return The field.
	 */
	public Field getField()
	{
		return this.field;
	}

	/**
	 *  Set the field.
	 *  @param field The field to set.
	 */
	public void setField(Field field)
	{
		this.field = field;
	}
	
	/**
	 *  Retrieves the bean property value for the given object.
	 *  
	 *  @param object The object containing the bean property.
	 *  @param property The name of the property.
	 *  
	 *  @return The value of the bean property.
	 */
	public Object getPropertyValue(Object object)
	{
		Object ret = null;
		IBeanAccessorDelegate accdel = delegateprovider!=null ? delegateprovider.getDelegate(object.getClass()) : null;
		if(accdel!=null)
		{
			try
			{
				ret = accdel.getPropertyValue(object, name);
			}
			catch(Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
		else if (getGetter() != null)
		{
			try
			{
				ret = getGetter().invoke(object, new Object[0]);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			try
			{
				Field field = getField();
				if (!field.isAccessible())
					field.setAccessible(true);
				ret = field.get(object);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
		return ret;
	}
	
	/**
	 *  Sets the bean property value for the given object.
	 *  
	 *  @param object The object containing the bean property.
	 *  @param property The name of the property.
	 *  @param value The new value.
	 */
	public void setPropertyValue(Object object, Object value)
	{
		IBeanAccessorDelegate accdel = delegateprovider!=null ? delegateprovider.getDelegate(object.getClass()) : null;
		if(accdel!=null)
		{
			accdel.setPropertyValue(object, name, value);
		}
		else if (getGetter() != null)
		{
			try
			{
				getSetter().invoke(object, new Object[] { value });
			}
			catch(Exception e)
			{
				Throwable	ex	= e;
				if(ex instanceof InvocationTargetException)
					ex	= ((InvocationTargetException)e).getTargetException();
				
				throw (ex instanceof RuntimeException) ? (RuntimeException)ex : new RuntimeException(ex);
			}
		}
		else
		{
			try
			{
				Field field = getField();
				if (!field.isAccessible())
					field.setAccessible(true);
				field.set(object, value);
			}
			catch (Exception e)
			{
				Throwable	ex	= e;
				if(ex instanceof InvocationTargetException)
					ex	= ((InvocationTargetException)e).getTargetException();
				
				throw (ex instanceof RuntimeException) ? (RuntimeException)ex : new RuntimeException(ex);
			}
		}
	}
}