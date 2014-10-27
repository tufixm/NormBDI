package jadex.bdi.testcases.goals;

import jadex.base.test.TestReport;
import jadex.bdi.runtime.GoalFailureException;
import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.Plan;
import jadex.bdi.runtime.interpreter.OAVBDIRuntimeModel;

/**
 *  Plan to test goal conditions.
 *  Note: the planbase content cannot be used here, because micro plansteps
 *  currently do not guarantee that all consequences will be executed (as in old tree agenda).
 */
public class GoalConditionsPlan	extends Plan
{
	/**
	 *  The plan body.
	 */
	public void	body()
	{
		// Initially there should be no goal and no plan (except this one).
		TestReport	report	= new TestReport("test_setup", "No goal and plan should exist at start", true, null);
		if(getGoalbase().getGoals("test").length!=0)
		{
			report.setFailed("Goal already exists");
		}
		else if(getPlanbase().getPlans().length!=1)
		{
			report.setFailed("Wrong planbase contents");
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);
		
		
		// Now triggering goal creation (still no plan due to invalid context).
		getBeliefbase().getBelief("creation").setFact(new Boolean(true));
		report	= new TestReport("trigger_creation", "Triggering goal creation", true, null);
		if(getGoalbase().getGoals("test").length!=1)
		{
			report.setFailed("Goal does not exist");
		}
		else if(getPlanbase().getPlans().length!=1)
		{
			report.setFailed("Wrong planbase contents");
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);
		
		
		// Now triggering goal context to start plan.		
		IGoal goal = getGoalbase().getGoals("test")[0];
		getBeliefbase().getBelief("context").setFact(new Boolean(true));
		report	= new TestReport("trigger_context", "Triggering goal context", true, null);	
		if(!OAVBDIRuntimeModel.GOALLIFECYCLESTATE_OPTION.equals(goal.getLifecycleState()))
		{
			report.setFailed("Goal not option: "+goal.getLifecycleState());
		}
//		else if(getPlanbase().getPlans().length!=2)
//		{
//			report.setFailed("Wrong planbase contents");
//		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);

		
		// Now triggering goal drop condition (goal and plan will be removed).
		getBeliefbase().getBelief("drop").setFact(new Boolean(true));
		try
		{
			waitForGoal(goal); // wait till goal is dropped.
		}
		catch(GoalFailureException gfe){}
		report	= new TestReport("trigger_drop", "Triggering goal drop condition", true, null);
		if(!OAVBDIRuntimeModel.GOALLIFECYCLESTATE_DROPPED.equals(goal.getLifecycleState()))
		{	
			report.setFailed("Goal not dropped: "+goal.getLifecycleState());
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);
		
		// Now triggering goal creation again (plan will also be created).
		getBeliefbase().getBelief("drop").setFact(new Boolean(false));
		getBeliefbase().getBelief("creation").setFact(new Boolean(false));
		getBeliefbase().getBelief("creation").setFact(new Boolean(true));
		goal = getGoalbase().getGoals("test")[0];
		report	= new TestReport("trigger_creation2", "Triggering goal creation again", true, null);
		if(getGoalbase().getGoals("test").length!=1)
		{
			report.setFailed("Goal does not exist");
		}
//		else if(getPlanbase().getPlans().length!=2)
//		{
//			report.setFailed("Wrong planbase contents");
//		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);
		
		// Now invalidating goal context to abort plan.
		getBeliefbase().getBelief("context").setFact(new Boolean(false));
		report	= new TestReport("trigger_context2", "Invalidating goal context", true, null);
		if(getGoalbase().getGoals("test").length!=1)
		{
			report.setFailed("Goal does not exist");
		}
		else if(!OAVBDIRuntimeModel.GOALLIFECYCLESTATE_SUSPENDED.equals(goal.getLifecycleState()))
		{
			report.setFailed("Goal not option: "+goal.getLifecycleState());
		}
//		else if(getPlanbase().getPlans().length!=1)
//		{
//			report.setFailed("Wrong planbase contents");
//		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);

		// Now triggering goal context again to restart plan.
		getBeliefbase().getBelief("context").setFact(new Boolean(true));
		report	= new TestReport("trigger_context3", "Triggering goal context again", true, null);
		if(getGoalbase().getGoals("test").length!=1)
		{
			report.setFailed("Goal does not exist");
		}
//		else if(getPlanbase().getPlans().length!=2)
//		{
//			report.setFailed("Wrong goalbase contents");
//		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);
	}
}
