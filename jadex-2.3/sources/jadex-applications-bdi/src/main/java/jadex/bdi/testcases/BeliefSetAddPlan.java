package jadex.bdi.testcases;

import jadex.bdi.runtime.Plan;

/**
 *  Add a value to a belief set.
 */
public class BeliefSetAddPlan extends Plan
{
	//-------- attributes --------

	/** The belief set name. */
	protected String belsetname;

	/** The new value. */
	protected Object value;
	
	/** The wait time (if any). */
	protected long wait;

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public BeliefSetAddPlan()
	{
		this.belsetname = (String)getParameter("beliefsetname").getValue();
		if(belsetname==null)
			throw new RuntimeException("Beliefsetname must not null.");

		this.value = getParameter("value").getValue();
		
		if(hasParameter("wait"))
			this.wait	= ((Number)getParameter("wait").getValue()).longValue();
		else
			this.wait	= -1;
	}

	//-------- methods --------

	/**
	 * The body method.
	 */
	public void body()
	{
		getLogger().info("waiting for: "+wait);
		if(wait!=-1)
			waitFor(wait);
		
		getLogger().info("Adding to beliefset: "+belsetname+" value :"+value);
		getBeliefbase().getBeliefSet(belsetname).addFact(value);
	}
}
