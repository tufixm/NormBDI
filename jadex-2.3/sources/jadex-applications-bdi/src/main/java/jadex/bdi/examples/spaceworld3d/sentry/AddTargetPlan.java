package jadex.bdi.examples.spaceworld3d.sentry;

import jadex.bdi.runtime.IMessageEvent;
import jadex.bdi.runtime.Plan;
import jadex.bridge.fipa.SFipa;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceObject;

/**
 *  Add a new unknown target to test.
 */
public class AddTargetPlan extends Plan
{
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public AddTargetPlan()
	{
		getLogger().info("Created: "+this+" "+getLogger().getName());
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		//System.out.println("AddPlan found");
		IEnvironmentSpace env = (IEnvironmentSpace)getBeliefbase().getBelief("move.environment").getFact();
		IMessageEvent req = (IMessageEvent)getReason();

		ISpaceObject ot = (ISpaceObject)req.getParameter(SFipa.CONTENT).getValue();
		ISpaceObject target = env.getSpaceObject(ot.getId());

		//if(ts.length>0)
		//	System.out.println("Sees: "+SUtil.arrayToString(ts));

		if(target!=null&& !getBeliefbase().getBeliefSet("my_targets").containsFact(target))
		{
			//System.out.println("Found a new target: "+target);
			getBeliefbase().getBeliefSet("my_targets").addFact(target);
		}
	}
}
