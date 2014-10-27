package jadex.bdi.testcases.misc;

import jadex.base.test.TestReport;
import jadex.bdi.planlib.watchdog.ContactData;
import jadex.bdi.planlib.watchdog.ObservationDescription;
import jadex.bdi.runtime.GoalFailureException;
import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.TimeoutException;
import jadex.bdi.testcases.AbstractMultipleAgentsPlan;
import jadex.bridge.IComponentIdentifier;

import java.util.List;
import java.util.Map;

/**
 *  Test the watchdog capability.
 */
public class WatchdogTestPlan extends AbstractMultipleAgentsPlan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		// Create 1 participant.
		Map[] args = new Map[1];
		List agents = createAgents("/jadex/bdi/testcases/planlib/PingReceiver.agent.xml", args);	

		ObservationDescription desc = new ObservationDescription();
		desc.setComponentIdentifier((IComponentIdentifier)agents.get(0));
		desc.setContacts(new ContactData[]{new ContactData("Admin", "braubach@gmx.net", null, null)});
		desc.setPingDelay(500);
		
		TestReport tr = new TestReport("#1", "Test observing other agent.");
		if(assureTest(tr))
		{
			try
			{
				IGoal observe = createGoal("dogcap.observe_component");
				observe.getParameter("description").setValue(desc);
				dispatchSubgoalAndWait(observe, 3000);
			}
			catch(GoalFailureException e)
			{
				tr.setFailed("Goal failure occurred: "+e);
			}
			catch(TimeoutException e)
			{
				tr.setSucceeded(true);
			}
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(tr);
		
		destroyAgents();
		tr = new TestReport("#1", "Test observing other agent which has been destroyed.");
		try
		{
			IGoal observe = createGoal("dogcap.observe_component");
			observe.getParameter("description").setValue(desc);
			dispatchSubgoalAndWait(observe, 30000);
		}
		catch(GoalFailureException e)
		{
			tr.setSucceeded(true);
		}
		catch(TimeoutException e)
		{
			tr.setFailed("Timeout exception occurred: "+e);
		}
		getBeliefbase().getBeliefSet("testcap.reports").addFact(tr);
	}
}
