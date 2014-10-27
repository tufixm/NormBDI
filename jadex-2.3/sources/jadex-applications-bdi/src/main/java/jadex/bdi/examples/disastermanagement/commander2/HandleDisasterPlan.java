package jadex.bdi.examples.disastermanagement.commander2;

import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.Plan;
import jadex.extension.envsupport.environment.ISpaceObject;

import java.util.ArrayList;
import java.util.List;

/**
 *  Handle a disaster by assigning units.
 */
public class HandleDisasterPlan extends Plan
{
	
	/**
	 *  The body method is called on the
	 *  instantiated plan instance from the scheduler.
	 */
	public void	body()
	{		
		ISpaceObject disaster = (ISpaceObject)getParameter("disaster").getValue();
		int chemicals = ((Integer)disaster.getProperty("chemicals")).intValue();
		int fire = ((Integer)disaster.getProperty("fire")).intValue();
		int victims = ((Integer)disaster.getProperty("victims")).intValue();
		
		List<IGoal>	subgoals	= new ArrayList<IGoal>();
	
		for(int i=0; i<chemicals; i++)
		{
			IGoal	cc	= createGoal("clear_chemicals"); 
			cc.getParameter("disaster").setValue(disaster);
			dispatchSubgoal(cc);
			subgoals.add(cc);
		}

		for(int i=0; i<fire; i++)
		{
			IGoal	ef	= createGoal("extinguish_fires"); 
			ef.getParameter("disaster").setValue(disaster);
			dispatchSubgoal(ef);
			subgoals.add(ef);
		}

		for(int i=0; i<victims; i++)
		{
			IGoal	tv	= createGoal("treat_victims"); 
			tv.getParameter("disaster").setValue(disaster);
			dispatchSubgoal(tv);
			subgoals.add(tv);
		}
		
		for(IGoal subgoal: subgoals)
		{
			waitForGoal(subgoal);
		}
	}
}