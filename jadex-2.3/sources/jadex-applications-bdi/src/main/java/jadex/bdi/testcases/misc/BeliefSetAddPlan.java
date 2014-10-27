package jadex.bdi.testcases.misc;

import jadex.bdi.runtime.Plan;

/**
 *  Modify a beliefset continuously by adding values.
 */
public class BeliefSetAddPlan extends Plan
{
	//-------- attributes --------

	/** The belief name. */
	protected String belsetname;

	/** The values. */
	protected Object[] values;
	
	/** The wait time. */
	protected long time;
	
	/** The flag for adding values over and over again. */
	protected boolean loop = true;
	
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public BeliefSetAddPlan()
	{
		if(hasParameter("beliefsetname"))
			this.belsetname = (String)getParameter("beliefsetname").getValue();
		else
			throw new RuntimeException("Parameter value 'beliefsetname' required.");
		
		if(hasParameterSet("values"))
			this.values = getParameterSet("values").getValues();
		if(hasParameter("time"))
			this.time = ((Long)getParameter("time").getValue()).longValue();
		if(hasParameter("loop"))
			this.loop = ((Boolean)getParameter("loop").getValue()).booleanValue();
	}

	//-------- methods --------

	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		long cnt = 0;
		do
		{
			if(values!=null)
			{
				for(int i=0; i<values.length; i++)
				{
					getLogger().info("Adding beliefset value: "+belsetname+" val :"+values[i]);
					getBeliefbase().getBeliefSet(belsetname).addFact(values[i]);
					waitFor(time);
				}
			}
			else
			{
				getLogger().info("Adding beliefset value: "+belsetname+" val :"+cnt);
				getBeliefbase().getBeliefSet(belsetname).addFact(new Long(cnt++));
				waitFor(time);
			}	
		}
		while(loop);
	}
}

