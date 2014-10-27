package jadex.bdi.examples.marsworld.producer;

import jadex.bdi.examples.marsworld.RequestCarry;
import jadex.bdi.examples.marsworld.RequestProduction;
import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.IMessageEvent;
import jadex.bdi.runtime.Plan;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.fipa.SFipa;
import jadex.extension.agr.AGRSpace;
import jadex.extension.agr.Group;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceObject;

/**
 *  The main plan for the Producer Agent. <br>
 *  first the Agent waits for an incoming request.
 *  It can be called to move home or to a given location.
 *  Being called to a location it will dispatch a subgoal to produce
 *  the ore there look up available carry agents and call one to collect it.
 */
public class ProducerPlan extends Plan
{
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public ProducerPlan()
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
			IMessageEvent req = waitForMessageEvent("request_production");

			ISpaceObject ot = ((RequestProduction)req.getParameter(SFipa.CONTENT).getValue()).getTarget();
			IEnvironmentSpace env = (IEnvironmentSpace)getBeliefbase().getBelief("move.environment").getFact();
			ISpaceObject target = env.getSpaceObject(ot.getId());

			// Producing ore here.
			IGoal produce_ore = createGoal("produce_ore");
			produce_ore.getParameter("target").setValue(target);
			dispatchSubgoalAndWait(produce_ore);

			//System.out.println("Production of ore has finished....");
			//System.out.println("Calling Carry Agent....");
			callCarryAgent(target);
		}
	}

	/**
	 *  Call carry agents to location.
	 *  @param target	The target to call carries to.
	 */
	protected void callCarryAgent(ISpaceObject target)
	{
		AGRSpace agrs = (AGRSpace)((IExternalAccess)getScope().getParentAccess()).getExtension("myagrspace").get(this);

		Group group = agrs.getGroup("mymarsteam");
		IComponentIdentifier[]	carriers	= group.getAgentsForRole("carrier");
		
		if(carriers!=null && carriers.length>0)
		{
			//System.out.println("Carry Agent: Found Carry Agents: "+carriers.length);

			RequestCarry rc = new RequestCarry();
			rc.setTarget(target);
			//Action action = new Action();
			//action.setAction(rc);
			//action.setActor(new AID("dummy", true)); // Hack!! What to do with more than one receiver?
			IMessageEvent mevent = createMessageEvent("request_carries");
			mevent.getParameterSet(SFipa.RECEIVERS).addValues(carriers);
			mevent.getParameter(SFipa.CONTENT).setValue(rc);
			sendMessage(mevent);
			//System.out.println("Production Agent sent target to: "+carriers.length);
		}
	}
}
