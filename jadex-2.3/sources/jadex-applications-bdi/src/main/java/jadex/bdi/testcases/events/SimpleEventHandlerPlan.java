package jadex.bdi.testcases.events;

import jadex.base.test.TestReport;
import jadex.bdi.runtime.IParameterElement;
import jadex.bdi.runtime.Plan;

/**
 *  This plan stores a test report in the reports belief set.
 */
public class SimpleEventHandlerPlan extends Plan
{
	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		String	test	= (String)((IParameterElement)getReason()).getParameter("param").getValue();
		boolean	success	= ((Boolean)getParameter("success").getValue()).booleanValue();
		TestReport	report	= new TestReport(test, "Test reaction to an initial event");
		if(success)
			report.setSucceeded(true);
		else
			report.setFailed("Event did not match");
		getBeliefbase().getBeliefSet("testcap.reports").addFact(report);
	}
}
