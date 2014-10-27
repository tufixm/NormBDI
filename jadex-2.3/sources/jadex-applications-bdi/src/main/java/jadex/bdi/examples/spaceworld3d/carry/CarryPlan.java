package jadex.bdi.examples.spaceworld3d.carry;

import jadex.bdi.examples.spaceworld3d.RequestCarry;
import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.IMessageEvent;
import jadex.bdi.runtime.Plan;
import jadex.bridge.fipa.SFipa;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceObject;

/**
 *  This is the main plan for the different Carry Agents.
 *  It waits for an incoming request, extracts the sent location
 *  and dispatches a new (sub) Goal to carry the ore.
 */
public class CarryPlan extends Plan
{
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public CarryPlan()
	{
		getLogger().info("Created: "+this);
	}

	//-------- methods --------

	/**
	 *  Method body.
	 */
	public void body()
	{
		while(true)
		{
			// Wait for a request.
			IMessageEvent req = waitForMessageEvent("request_carry");

			ISpaceObject ot = ((RequestCarry)req.getParameter(SFipa.CONTENT).getValue()).getTarget();
			IEnvironmentSpace env = (IEnvironmentSpace)getBeliefbase().getBelief("move.environment").getFact();
			ISpaceObject target = env.getSpaceObject(ot.getId());

			// Producing ore here.
			IGoal carry_ore = createGoal("carry_ore");
			carry_ore.getParameter("target").setValue(target);
			dispatchSubgoalAndWait(carry_ore);
		}
	}
}