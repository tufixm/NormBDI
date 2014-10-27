package jadex.bdi.testcases.planlib;

import jadex.bdi.planlib.protocols.ParticipantProposal;
import jadex.bdi.runtime.Plan;

import java.util.ArrayList;
import java.util.List;

/**
 *  Evaluate the example proposals.
 */
public class CNPEvaluatorPlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		ParticipantProposal[] proposals = (ParticipantProposal[])getParameterSet("proposals").getValues();
		
		// Accept all proposals >= 5
		List acceptables	= new ArrayList();
		for(int i=0; i<proposals.length; i++)
		{
			if(((Number)proposals[i].getProposal()).intValue()>=5)
			{
				// Insert acceptable proposal in descending order.
				int j;
				for(j=0; j<acceptables.size(); j++)
				{
					if(((Number)proposals[i].getProposal()).intValue()
						> ((Number)((ParticipantProposal)acceptables.get(j)).getProposal()).intValue())
						break;
				}
				acceptables.add(j, proposals[i]);
			}
		}
		
		getParameterSet("acceptables").addValues(acceptables.toArray());
	}
}
