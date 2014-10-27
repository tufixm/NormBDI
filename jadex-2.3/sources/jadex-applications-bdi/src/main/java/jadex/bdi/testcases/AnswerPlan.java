package jadex.bdi.testcases;

import jadex.bdi.runtime.IMessageEvent;
import jadex.bdi.runtime.Plan;
import jadex.bridge.fipa.SFipa;


/**
 *  A plan thats sends one or more answers to a request.
 */
public class AnswerPlan	extends Plan
{
	//-------- attributes --------

	/** The number of answers to send. */
	protected int	num;

	/** The delay between the answers. */
	protected long	delay;

	//-------- constructors --------

	/**
	 *  Create an answer plan.
	 *  @param num	The number of answers to send.
	 *  @param delay	The delay between the answers.
	 */
	public AnswerPlan()
	{
		if(hasParameter("number"))
			this.num	= ((Number)getParameter("number").getValue()).intValue();
		else
			this.num	= 1;

		if(hasParameter("delay"))
			this.delay	= ((Number)getParameter("delay").getValue()).longValue();
	}

	//-------- methods --------

	/**
	 *  The body of the plan.
	 */
	public void	body()
	{
		IMessageEvent	event	= (IMessageEvent)getReason();
		for(int i=1; i<=num; i++)
		{
			IMessageEvent	answer	= getEventbase().createReply(event, "inform");
			answer.getParameter(SFipa.CONTENT).setValue(""+i);
			getLogger().info("Sending answer "+i+".");
			sendMessage(answer).get(this);
			if(delay>0)
			{
				waitFor(delay);
			}
		}
	}
}

