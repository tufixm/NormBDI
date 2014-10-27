package jadex.bdiv3.runtime.impl;

import java.lang.reflect.Method;

/**
 *  Used as part of a service plan, i.e. a plan
 *  that has a required service method as body.
 *   
 *  Interface that is used for mapping from a goal
 *  to the service parameters.
 */
public interface IServiceParameterMapper<T>
{
	/**
	 *  Create service parameters.
	 *  @param obj The pojo goal.
	 *  @param m The service method called.
	 *  @return The parameter array for the service call.
	 */
	public Object[] createServiceParameters(T obj, Method m);
	
	/**
	 *  Create service result.
	 *  @param obj The goal.
	 *  @param m The method.
	 *  @param result The service call result.
	 */
	public void handleServiceResult(T obj, Method m, Object result);
}
