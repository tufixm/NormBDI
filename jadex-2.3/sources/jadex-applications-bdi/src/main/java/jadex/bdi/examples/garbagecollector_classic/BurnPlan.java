package jadex.bdi.examples.garbagecollector_classic;

import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.Plan;

/**
 *  Burn a piece of garbage.
 */
public class BurnPlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		//System.out.println("Burn plan activated!");
		
		Environment env = (Environment)getBeliefbase().getBelief("env").getFact();

		// Pickup the garbarge.
		IGoal pickup = createGoal("pick");
		dispatchSubgoalAndWait(pickup);

		// Burn the waste.
		waitFor(100);
		env.burn(getComponentName());
	}
}
