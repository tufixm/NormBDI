package jadex.bdiv3.model;

import jadex.bdiv3.annotation.PlanAborted;
import jadex.bdiv3.annotation.PlanBody;
import jadex.bdiv3.annotation.PlanContextCondition;
import jadex.bdiv3.annotation.PlanFailed;
import jadex.bdiv3.annotation.PlanPassed;
import jadex.bdiv3.annotation.PlanPrecondition;
import jadex.bridge.ClassInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 *  The plan mbody.
 */
public class MBody
{
	protected static final MethodInfo MI_NOTFOUND = new MethodInfo();
	
	/** The body as seperate class. */
	protected MethodInfo method;
	
	/** The body as seperate class. */
	protected ClassInfo clazz;

	/** The body as required service. */
	protected String servicename;
	
	/** The body as required service. */
	protected String servicemethodname;
	
	/** The parameter mapper. */
	protected ClassInfo mapperclass;
	
	
	/** The body method cached for speed. */
	protected MethodInfo bodymethod;

	/** The passed method cached for speed. */
	protected MethodInfo passedmethod;

	/** The failed method cached for speed. */
	protected MethodInfo failedmethod;

	/** The aborted method cached for speed. */
	protected MethodInfo abortedmethod;
	
	/** The precondition method cached for speed. */
	protected MethodInfo preconditionmethod;

	/** The precondition method cached for speed. */
	protected MethodInfo contextconditionmethod;

	
	/**
	 *  Create a new mbody.
	 */
	public MBody(MethodInfo method, ClassInfo clazz, String servicename,
		String servicemethodname, ClassInfo mapperclass)
	{
		this.method = method;
		this.clazz = clazz;
		this.servicename = servicename;
		this.servicemethodname = servicemethodname;
		this.mapperclass = mapperclass;
	}

	/**
	 *  Get the method.
	 *  @return The method.
	 */
	public MethodInfo getMethod()
	{
		return method;
	}

	/**
	 *  Set the method.
	 *  @param method The method to set.
	 */
	public void setMethod(MethodInfo method)
	{
		this.method = method;
	}

	/**
	 *  Get the clazz.
	 *  @return The clazz.
	 */
	public ClassInfo getClazz()
	{
		return clazz;
	}

	/**
	 *  Set the clazz.
	 *  @param clazz The clazz to set.
	 */
	public void setClazz(ClassInfo clazz)
	{
		this.clazz = clazz;
	}

	/**
	 *  Get the servicename.
	 *  @return The servicename.
	 */
	public String getServiceName()
	{
		return servicename;
	}

	/**
	 *  Set the servicename.
	 *  @param servicename The servicename to set.
	 */
	public void setServiceName(String servicename)
	{
		this.servicename = servicename;
	}

	/**
	 *  Get the servicemethodname.
	 *  @return The servicemethodname.
	 */
	public String getServiceMethodName()
	{
		return servicemethodname;
	}

	/**
	 *  Set the servicemethodname.
	 *  @param servicemethodname The servicemethodname to set.
	 */
	public void setServiceMethodName(String servicemethodname)
	{
		this.servicemethodname = servicemethodname;
	}
	
	/**
	 *  Get the mapperclass.
	 *  @return The mapperclass.
	 */
	public ClassInfo getMapperClass()
	{
		return mapperclass;
	}

	/**
	 *  Set the mapperclass.
	 *  @param mapperclass The mapperclass to set.
	 */
	public void setMapperclass(ClassInfo mapperclass)
	{
		this.mapperclass = mapperclass;
	}

	/**
	 * 
	 */
	public MethodInfo getBodyMethod(ClassLoader cl)
	{
		if(clazz!=null)
		{
			if(bodymethod==null)
			{
				synchronized(this)
				{
					if(bodymethod==null)
					{
						Class<?> body = clazz.getType(cl);
						bodymethod = getMethod(body, PlanBody.class);
						if(bodymethod==null)
							throw  new RuntimeException("Plan has no body method: "+body);
					}
				}
			}
		}
		
		return bodymethod;
	}
	
	/**
	 * 
	 */
	public MethodInfo getPassedMethod(ClassLoader cl)
	{
		if(clazz!=null)
		{
			if(passedmethod==null && !MI_NOTFOUND.equals(passedmethod))
			{
				synchronized(this)
				{
					if(passedmethod==null && !MI_NOTFOUND.equals(passedmethod))
					{
						Class<?> body = clazz.getType(cl);
						passedmethod = getMethod(body, PlanPassed.class);
						if(passedmethod==null)
							passedmethod = MI_NOTFOUND;
					}
				}
			}
		}
		
		return MI_NOTFOUND.equals(passedmethod)? null: passedmethod;
	}
	
	/**
	 * 
	 */
	public MethodInfo getFailedMethod(ClassLoader cl)
	{
		if(clazz!=null)
		{
			if(failedmethod==null && !MI_NOTFOUND.equals(failedmethod))
			{
				synchronized(this)
				{
					if(failedmethod==null && !MI_NOTFOUND.equals(failedmethod))
					{
						Class<?> body = clazz.getType(cl);
						failedmethod = getMethod(body, PlanFailed.class);
						if(failedmethod==null)
							failedmethod = MI_NOTFOUND;
					}
				}
			}
		}
		
		return MI_NOTFOUND.equals(failedmethod)? null: failedmethod;
	}
	
	/**
	 * 
	 */
	public MethodInfo getAbortedMethod(ClassLoader cl)
	{
		if(clazz!=null)
		{
			if(abortedmethod==null && !MI_NOTFOUND.equals(abortedmethod))
			{
				synchronized(this)
				{
					if(abortedmethod==null && !MI_NOTFOUND.equals(abortedmethod))
					{
						Class<?> body = clazz.getType(cl);
						abortedmethod = getMethod(body, PlanAborted.class);
						if(abortedmethod==null)
							abortedmethod = MI_NOTFOUND;
					}
				}
			}
		}
		
		return MI_NOTFOUND.equals(abortedmethod)? null: abortedmethod;
	}
	
	/**
	 * 
	 */
	public MethodInfo getPreconditionMethod(ClassLoader cl)
	{
		if(clazz!=null)
		{
			if(preconditionmethod==null && !MI_NOTFOUND.equals(preconditionmethod))
			{
				synchronized(this)
				{
					if(preconditionmethod==null && !MI_NOTFOUND.equals(preconditionmethod))
					{
						Class<?> body = clazz.getType(cl);
						preconditionmethod = getMethod(body, PlanPrecondition.class);
						if(preconditionmethod==null)
							preconditionmethod = MI_NOTFOUND;
					}
				}
			}
		}
		
		return MI_NOTFOUND.equals(preconditionmethod)? null: preconditionmethod;
	}
	
	/**
	 * 
	 */
	public MethodInfo getContextConditionMethod(ClassLoader cl)
	{
		if(clazz!=null)
		{
			if(contextconditionmethod==null && !MI_NOTFOUND.equals(contextconditionmethod))
			{
				synchronized(this)
				{
					if(contextconditionmethod==null && !MI_NOTFOUND.equals(contextconditionmethod))
					{
						Class<?> body = clazz.getType(cl);
						contextconditionmethod = getMethod(body, PlanContextCondition.class);
						if(contextconditionmethod==null)
							contextconditionmethod = MI_NOTFOUND;
					}
				}
			}
		}
		return MI_NOTFOUND.equals(contextconditionmethod)? null: contextconditionmethod;
	}
	
	/**
	 * 
	 */
	public static MethodInfo getMethod(Class<?> body, Class<? extends Annotation> type)
	{
		MethodInfo ret = null;
		
		Method[] ms = body.getDeclaredMethods();
		for(Method m: ms)
		{
			if(m.isAnnotationPresent(type))
			{
				ret = new MethodInfo(m);
				break;
			}
		}
		
		return ret;
	}
}
