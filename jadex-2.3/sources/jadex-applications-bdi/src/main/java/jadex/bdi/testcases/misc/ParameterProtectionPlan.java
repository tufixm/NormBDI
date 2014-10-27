package jadex.bdi.testcases.misc;

import jadex.base.test.TestReport;
import jadex.bdi.runtime.GoalFailureException;
import jadex.bdi.runtime.IElement;
import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.IInternalEvent;
import jadex.bdi.runtime.Plan;

/**
 *  The print plan simply prints out the object
 *  it gets as parameter in the construtor.
 */
public class ParameterProtectionPlan extends Plan
{
	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		IInternalEvent event = createInternalEvent("test_event");
		testInit(event);
		// Use atomic block to avoid event being cleanupped before test has been performed.
		startAtomic();
		dispatchInternalEvent(event);
		testInProcess(event);
		endAtomic();

		IGoal subgoal = createGoal("test_goal");
		testInit(subgoal);
		// Use atomic block to avoid goal being processed before test has been performed.
		startAtomic();
		getWaitqueue().addGoal(subgoal);
		dispatchSubgoal(subgoal);
		testInProcess(subgoal);
		endAtomic();
		try
		{
			waitForGoal(subgoal, 1000);
		}
		catch(GoalFailureException e)
		{
			// Goal fails because of no plan.
		}
		getWaitqueue().removeGoal(subgoal);
		testInit(subgoal);

		// Todo: Test  parameter sets.
	}

	/**
	 *  Test init-mode access.
	 */
	protected void testInit(IElement elem)
	{
		TestReport	report	= new TestReport("init-read-in", "Testing init-protected read access of in parameter.");
		try
		{
			getParameterValue(elem, "param_in");
			report.setSucceeded(true);
		}
		catch(Exception e)
		{
			report.setReason("Could not read parameter.");
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);
		
		report	= new TestReport("init-read-inout", "Testing init-protected read access of inout parameter.");
		try
		{
			getParameterValue(elem, "param_inout");
			report.setSucceeded(true);
		}
		catch(Exception e)
		{
			report.setReason("Could not read unforbidden parameter.");
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);

		report	= new TestReport("init-read-out", "Testing init-protected read access of out parameter.");
		try
		{
			getParameterValue(elem, "param_out");
			report.setSucceeded(true);
		}
		catch(Exception e)
		{
			report.setReason("Could not read unforbidden parameter.");
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);

		String newvalue = "newvalue";

		report	= new TestReport("init-write-in", "Testing init-protected write access of in parameter.");
		try
		{
			setParameterValue(elem, "param_in", newvalue);
			report.setSucceeded(true);
		}
		catch(Exception e)
		{
			report.setReason("Could not write unforbidden parameter.");
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);

		report	= new TestReport("init-write-inout", "Testing init-protected write access of inout parameter.");
		try
		{
			setParameterValue(elem, "param_inout", newvalue);
			report.setSucceeded(true);
		}
		catch(Exception e)
		{
			report.setReason("Could not write unforbidden parameter.");
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);

		report	= new TestReport("init-write-out", "Testing init-protected write access of out parameter.");
		try
		{
			setParameterValue(elem, "param_out", newvalue);
			report.setReason("Could write forbidden parameter.");
		}
		catch(Exception e)
		{
			report.setSucceeded(true);
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);
	}

	/**
	 *  Test in-process access.
	 */
	protected void testInProcess(IElement elem)
	{
		TestReport	report	= new TestReport("process-read-in", "Testing process-protected read access of in parameter.");
		try
		{
			getParameterValue(elem, "param_in");
			report.setSucceeded(true);
		}
		catch(Exception e)
		{
			report.setReason("Could not read unforbidden parameter.");
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);
		
		report	= new TestReport("process-read-inout", "Testing process-protected read access of inout parameter.");
		try
		{
			getParameterValue(elem, "param_inout");
			report.setSucceeded(true);
		}
		catch(Exception e)
		{
			report.setReason("Could not read unforbidden parameter.");
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);

		report	= new TestReport("process-read-out", "Testing process-protected read access of out parameter.");
		try
		{
			getParameterValue(elem, "param_out");
			report.setSucceeded(true);
		}
		catch(Exception e)
		{
			report.setReason("Could not read unforbidden parameter.");
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);

		String newvalue = "newvalue";

		report	= new TestReport("process-write-in", "Testing process-protected write access of in parameter.");
		try
		{
			setParameterValue(elem, "param_in", newvalue);
			report.setReason("Could write forbidden parameter.");
		}
		catch(Exception e)
		{
			report.setSucceeded(true);
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);

		report	= new TestReport("process-write-inout", "Testing process-protected write access of inout parameter.");
		try
		{
			setParameterValue(elem, "param_inout", newvalue);
			report.setSucceeded(true);
		}
		catch(Exception e)
		{
			report.setReason("Could not write unforbidden parameter.");
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);

		report	= new TestReport("process-write-out", "Testing process-protected write access of out parameter.");
		try
		{
			setParameterValue(elem, "param_out", newvalue);
			report.setSucceeded(true);
		}
		catch(Exception e)
		{
			report.setReason("Could not write unforbidden parameter.");
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);
	}

	/**
	 *  Get a parameter value of an event or goal.
	 */
	protected Object	getParameterValue(IElement element, String name)
	{
		if(element instanceof IGoal)
			return ((IGoal)element).getParameter(name).getValue();
		else
			return ((IInternalEvent)element).getParameter(name).getValue();
	}

	/**
	 *  Get a parameter value of an event or goal.
	 */
	protected void	setParameterValue(IElement element, String name, Object value)
	{
		if(element instanceof IGoal)
			((IGoal)element).getParameter(name).setValue(value);
		else
			((IInternalEvent)element).getParameter(name).setValue(value);
	}

	/**
	 *  Get a parameter value of an event or goal.
	 */
	protected Object[]	getParameterSetValues(IElement element, String name)
	{
		if(element instanceof IGoal)
			return ((IGoal)element).getParameterSet(name).getValues();
		else
			return ((IInternalEvent)element).getParameterSet(name).getValues();
	}

	/**
	 *  Get a parameter value of an event or goal.
	 */
	protected void	addParameterSetValue(IElement element, String name, Object value)
	{
		if(element instanceof IGoal)
			((IGoal)element).getParameterSet(name).addValue(value);
		else
			((IInternalEvent)element).getParameterSet(name).addValue(value);
	}
}
