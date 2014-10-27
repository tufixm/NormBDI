package jadex.bdi.planlib.iasteps;

import jadex.bdi.runtime.AgentEvent;
import jadex.bdi.runtime.IBDIInternalAccess;
import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.IGoalListener;
import jadex.bdi.runtime.IParameter;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DispatchGoalStep implements IComponentStep<Map<String, Object>>
{
	protected String goaltype;
	protected Map parameters;
	
	/**
	 *  Dispatches a goal.
	 *  @param goal The goal.
	 */
	public DispatchGoalStep(String goal)
	{
		this(goal, null);
	}
	
	/**
	 *  Dispatches a goal.
	 *  @param goal The goal.
	 *  @param parameterName Name of a goal parameter.
	 *  @param parameterValue Value of the goal parameter.
	 */
	public DispatchGoalStep(String goal, final String parameterName, final Object parameterValue)
	{
		this(goal, new HashMap() {{
			put(parameterName, parameterValue);
		}});
	}
	
	/**
	 *  Dispatches a goal.
	 *  @param goal The goal.
	 *  @param parameters The goal parameters.
	 */
	public DispatchGoalStep(String goal, Map parameters)
	{
		this.goaltype = goal;
		this.parameters = parameters;
	}
	
	public IFuture<Map<String, Object>> execute(IInternalAccess ia)
	{
		final IGoal goal = ((IBDIInternalAccess) ia).getGoalbase().createGoal(goaltype);
		if (parameters != null)
		{
			for (Iterator it = parameters.entrySet().iterator(); it.hasNext(); )
			{
				Map.Entry paramEntry = (Map.Entry) it.next();
				goal.getParameter((String) paramEntry.getKey()).setValue(paramEntry.getValue());
			}
		}
		
		final Future<IParameter[]> goalFuture = new Future<IParameter[]>();
		goal.addGoalListener(new IGoalListener()
		{
			public void goalFinished(AgentEvent ae)
			{
				goalFuture.setResult(goal.getParameters());
			}
			
			public void goalAdded(AgentEvent ae)
			{
			}
		});
		((IBDIInternalAccess) ia).getGoalbase().dispatchTopLevelGoal(goal);
		
		final Future<Map<String, Object>> ret = new Future<Map<String, Object>>();
		goalFuture.addResultListener(new DefaultResultListener<IParameter[]>()
		{
			public void resultAvailable(IParameter[] params)
			{
				Map<String, Object> results = new HashMap<String, Object>();
				for (int i = 0; i < params.length; ++i)
				{
//					String dir = ((IMParameter) params[i].getModelElement()).getDirection();
					//System.out.println(params[i].getName() + " " + params[i].getValue() + " " + dir);
					//if (OAVBDIMetaModel.PARAMETER_DIRECTION_INOUT.equals(dir) ||
						//OAVBDIMetaModel.PARAMETER_DIRECTION_OUT.equals(dir))
					results.put(params[i].getName(), params[i].getValue());
				}
				ret.setResult(results);
			}
		});
		
		return ret;
	}
}