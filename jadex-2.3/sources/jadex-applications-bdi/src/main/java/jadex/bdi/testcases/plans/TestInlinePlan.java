package jadex.bdi.testcases.plans;

import jadex.base.test.TestReport;
import jadex.bdi.runtime.GoalFailureException;
import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.Plan;

/**
 *  Create a goal and wait for the result.
 */
public class TestInlinePlan extends Plan
{
	public void body()
	{
		TestReport	report	= new TestReport("test_inline", "Dispatch a goal handled by the inline plan");
		try
		{
			IGoal	agoal	= createGoal("testgoal");
			dispatchSubgoalAndWait(agoal);
			report.setSucceeded(true);
		}
		catch(GoalFailureException gfe)
		{
			report.setReason(gfe.toString());
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);
	}
}
