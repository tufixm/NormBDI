package jadex.bdi.examples.blackjack.player;

import jadex.bdi.runtime.IMessageEvent;
import jadex.bdi.runtime.Plan;
import jadex.bridge.fipa.SFipa;

/**
 *  This plan is executed when the agent wants to decline a game.
 */
public class PlayerDeclineGamePlan extends Plan
{
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public PlayerDeclineGamePlan()
	{
		getLogger().info("created: " + this);
	}

	//-------- attributes --------
	
	/**
	 *  Execute a plan.
	 */
	public void body()
	{
		IMessageEvent	querybet	= (IMessageEvent)getReason();

		// Reply to dealer.
		IMessageEvent	msg	= getEventbase().createReply(querybet, "refuse_bet");
		msg.getParameter(SFipa.CONTENT).setValue(querybet.getParameter(SFipa.CONTENT).getValue());
		getLogger().info("sending decline to the dealer...");
		sendMessage(msg);
	}
}
