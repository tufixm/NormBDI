package jadex.bdi.examples.cleanerworld_classic.cleaner;

import jadex.bdi.runtime.GoalFailureException;
import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.Plan;
import jadex.bdi.runtime.PlanFailureException;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.fipa.IComponentAction;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.df.IDF;
import jadex.bridge.service.types.df.IDFComponentDescription;
import jadex.bridge.service.types.df.IDFServiceDescription;


/**
 *  Offers a methods for interacting with the cleanerworld environment.
 */
public abstract class RemoteActionPlan extends Plan
{
	/**
	 *  Request an action from the environment. 
	 *  @param action The action.
	 *  @return The finished goal.
	 *  @throws GoalFailureException	when the request goal fails.
	 */
	public IGoal requestAction(IComponentAction action)
		throws GoalFailureException
	{
		// Search and store the environment agent.
		IComponentIdentifier	env	= searchEnvironmentAgent();
		IGoal rg = createGoal("rp_initiate");
		rg.getParameter("receiver").setValue(env);
		rg.getParameter("action").setValue(action);
		rg.getParameter("ontology").setValue("cleaner_ontology");
		//rg.getParameter("language").setValue(SFipa.JADEX_XML);

		dispatchSubgoalAndWait(rg);
		
		return rg;
	}

	/**
	 *  When the plan has failed, assume that environment is down.
	 *  Remove fact to enable new search for environment.
	 */
	public void failed()
	{
		// Received a timeout. Probably the environment agent has died.
		getBeliefbase().getBelief("environmentagent").setFact(null);
	}
	
	//--------- helper methods --------
	
	/**
	 *  Search the environent agent and store its AID in the beliefbase.
	 */
	protected IComponentIdentifier	searchEnvironmentAgent()
	{
		IComponentIdentifier	res	= (IComponentIdentifier)getBeliefbase().getBelief("environmentagent").getFact();

		if(res==null)
		{
			IDF df = (IDF)SServiceProvider.getServiceUpwards(getServiceContainer(), IDF.class).get(this);
			IDFServiceDescription sd = df.createDFServiceDescription(null, "dispatch vision", null);
			IDFComponentDescription ad = df.createDFComponentDescription(null, sd);
				
			// Use a subgoal to search for a translation agent
			IGoal ft = createGoal("df_search");
			ft.getParameter("description").setValue(ad);
			if(getBeliefbase().containsBelief("df"))
				ft.getParameter("df").setValue(getBeliefbase().getBelief("df").getFact());
			dispatchSubgoalAndWait(ft);
			//Object result = ft.getResult();
			IDFComponentDescription[] tas = (IDFComponentDescription[])ft.getParameterSet("result").getValues();

			if(tas.length!=0)
			{
				// Found.
				res	= tas[0].getName();
				getBeliefbase().getBelief("environmentagent").setFact(res);
				if(tas.length>1)
					getLogger().warning("More than environment agent found.");
			}
			else
			{
				// Not found.
				throw new PlanFailureException();
			}
		}

		return res;
	}

}
