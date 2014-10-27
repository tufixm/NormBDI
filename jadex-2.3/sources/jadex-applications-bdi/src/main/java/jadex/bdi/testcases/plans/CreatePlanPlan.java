package jadex.bdi.testcases.plans;

import jadex.bdi.model.IMPlan;
import jadex.bdi.model.IMPlanbase;
import jadex.bdi.runtime.IPlan;
import jadex.bdi.runtime.Plan;

/**
 *  Plan that programmatically creates a plan.
 */
public class CreatePlanPlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		IMPlan mplan = ((IMPlanbase)getPlanbase().getModelElement()).getPlan("startplan");
		IPlan plan = getPlanbase().createPlan(mplan);
		plan.startPlan();
	}
}

