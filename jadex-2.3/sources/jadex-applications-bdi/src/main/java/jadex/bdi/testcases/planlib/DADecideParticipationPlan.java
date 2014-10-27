package jadex.bdi.testcases.planlib;

import jadex.bdi.planlib.protocols.AuctionDescription;
import jadex.bdi.runtime.Plan;


/**
 * Decide on participation.
 */
public class DADecideParticipationPlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		boolean participate = ((Boolean)getBeliefbase().getBelief("participate").getFact()).booleanValue();
		getLogger().info(getComponentName()+" deciding on participation in auction "
			+((AuctionDescription)getParameter("auction_description").getValue()).getTopic()+" "+participate);
		getParameter("participate").setValue(new Boolean(participate));
	}
}
