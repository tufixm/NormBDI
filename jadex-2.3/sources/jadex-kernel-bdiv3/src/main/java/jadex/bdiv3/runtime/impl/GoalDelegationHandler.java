package jadex.bdiv3.runtime.impl;

import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.model.MCapability;
import jadex.bdiv3.model.MGoal;
import jadex.bridge.service.annotation.Service;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.micro.IPojoMicroAgent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 *  Handler used for service-goal delegation.
 *  Creates specific goal for an incoming service request.
 *  Goal must have constructor that exactly fits to
 *  service invocation parameters
 */
@Service
public class GoalDelegationHandler  implements InvocationHandler
{
	//-------- attributes --------
	
	/** The agent. */
	protected BDIAgent agent;
	
	/** The goal name. */
	protected Map<String, String> goalnames;
	
	//-------- constructors --------
	
	/**
	 *  Create a new service wrapper invocation handler.
	 *  @param agent The internal access of the agent.
	 */
	public GoalDelegationHandler(BDIAgent agent, Map<String, String> goalnames)
	{
		if(agent==null)
			throw new IllegalArgumentException("Agent must not null.");
		if(goalnames==null)
			throw new IllegalArgumentException("Goal names must not null.");
		
		this.agent = agent;
		this.goalnames = goalnames;
	}
	
	//-------- methods --------
	
	/**
	 *  Called when a wrapper method is invoked.
	 *  Uses the cms to create a new invocation agent and lets this
	 *  agent call the web service. The result is transferred back
	 *  into the result future of the caller.
	 */
	public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable
	{
		final Future<Object> ret = new Future<Object>();
		
		String goalname = goalnames.get(method.getName());
		
		if(goalname==null)
			throw new RuntimeException("No method-goal mapping found: "+method.getName()+" "+goalnames);
		
		MCapability mcapa = (MCapability)agent.getCapability().getModelElement();
		final MGoal mgoal = mcapa.getGoal(goalname);
		
		Class<?> goalcl = mgoal.getTargetClass(agent.getClassLoader());
		
		Class<?>[] mptypes = method.getParameterTypes();
		
		Object goal;
		
		try
		{
			Constructor<?> c = goalcl.getConstructor(mptypes);
			goal = c.newInstance(args);
		}
		catch(Exception e)
		{
			Class<?>[] mptypes2 = new Class<?>[mptypes.length+1];
			System.arraycopy(mptypes, 0, mptypes2, 1, mptypes.length);
			Object pojo = ((IPojoMicroAgent)agent).getPojoAgent();
			mptypes2[0] = pojo.getClass();
			Constructor<?> c = goalcl.getConstructor(mptypes2);
			Object[] args2 = new Object[args.length+1];
			System.arraycopy(args, 0, args2, 1, args.length);
			args2[0] = pojo;
			goal = c.newInstance(args2);
		}
		
		final Object fgoal = goal;
		agent.dispatchTopLevelGoal(fgoal).addResultListener(new ExceptionDelegationResultListener<Object, Object>(ret)
		{
			public void customResultAvailable(Object result)
			{
				ret.setResult(RGoal.getGoalResult(fgoal, mgoal, agent.getClassLoader()));
			}
		});
	
		return ret;
	}
}
