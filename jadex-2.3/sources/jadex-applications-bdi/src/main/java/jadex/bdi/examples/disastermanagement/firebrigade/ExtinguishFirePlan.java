package jadex.bdi.examples.disastermanagement.firebrigade;

import jadex.bdi.examples.disastermanagement.DisasterType;
import jadex.bdi.examples.disastermanagement.ExtinguishFireTask;
import jadex.bdi.planlib.PlanFinishedTaskCondition;
import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.Plan;
import jadex.extension.envsupport.environment.AbstractTask;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.IVector2;

import java.util.HashMap;
import java.util.Map;

/**
 *  Move to fire and extinguish it.
 */
public class ExtinguishFirePlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		Space2D	space	= (Space2D)getBeliefbase().getBelief("environment").getFact();
		ISpaceObject	myself	= (ISpaceObject)getBeliefbase().getBelief("myself").getFact();
		ISpaceObject	disaster	= (ISpaceObject)getParameter("disaster").getValue();
		
		// Move to disaster location
		myself.setProperty("state", "moving_to_disaster");
		IVector2	targetpos	= DisasterType.getFireLocation(disaster);
		IGoal move = createGoal("move");
		move.getParameter("destination").setValue(targetpos);
		dispatchSubgoalAndWait(move);
		
		// Extinguish fire
		myself.setProperty("state", "extinguishing_fire");
		Map props = new HashMap();
		props.put(ExtinguishFireTask.PROPERTY_DISASTER, disaster);
		props.put(AbstractTask.PROPERTY_CONDITION, new PlanFinishedTaskCondition(getPlanElement()));
		Object taskid = space.createObjectTask(ExtinguishFireTask.PROPERTY_TYPENAME, props, myself.getId());
		SyncResultListener	res	= new SyncResultListener();
		space.addTaskListener(taskid, myself.getId(), res);
		res.waitForResult();
	}
	
	/**
	 *  Called when a plan fails.
	 */
	public void failed()
	{
		System.err.println("Plan failed: "+this);
		getException().printStackTrace();
	}
}
