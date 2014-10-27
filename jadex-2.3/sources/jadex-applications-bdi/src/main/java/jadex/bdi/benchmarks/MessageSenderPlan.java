package jadex.bdi.benchmarks;

import jadex.bdi.runtime.IMessageEvent;
import jadex.bdi.runtime.Plan;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.fipa.SFipa;

/**
 *  Send a specified amount of messages.
 */
public class MessageSenderPlan	extends Plan
{
	//-------- methods --------

	/**
	 * The body of the plan.
	 */
	public void body()
	{
		int msgcnt = ((Integer)getBeliefbase().getBelief("msg_cnt").getFact()).intValue();
		IComponentIdentifier receiver = (IComponentIdentifier)getBeliefbase().getBelief("receiver").getFact();
		
		System.out.println("Now sending " + msgcnt + " messages to " + receiver);
		
		getBeliefbase().getBelief("starttime").setFact(new Long(getTime()));		
		
		// Send messages.
		for(int i=1; i<=msgcnt; i++)
		{
			IMessageEvent request = createMessageEvent("inform");
			request.getParameterSet(SFipa.RECEIVERS).addValue(receiver);
			request.getParameter(SFipa.REPLY_WITH).setValue("some reply id");
			sendMessage(request);
			
			getBeliefbase().getBelief("sent").setFact(new Integer(i));
			
			if(i % 10 == 0)
			{
				//System.out.print('.');
				waitFor(0);
			}
		}
	}
}

