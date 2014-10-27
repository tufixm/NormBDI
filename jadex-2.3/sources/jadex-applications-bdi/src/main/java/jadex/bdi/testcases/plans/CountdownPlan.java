package jadex.bdi.testcases.plans;

import jadex.bdi.runtime.Plan;

/**
 *  The countdown plan counts down to zero.
 */
public class CountdownPlan extends Plan
{
	//-------- constructors --------

	/**
	 *  Create a new countdown plan.
	 */
	public CountdownPlan()
	{
		getLogger().info("Created: "+this);
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		while(true)
		{
			int num = ((Integer)getBeliefbase().getBelief("num").getFact()).intValue();
			getLogger().info(""+num);
			getBeliefbase().getBelief("num").setFact(new Integer(num-1));
			waitFor(10);
		}
	}
}
