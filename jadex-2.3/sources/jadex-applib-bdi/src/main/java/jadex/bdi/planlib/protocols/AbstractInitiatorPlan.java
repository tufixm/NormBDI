package jadex.bdi.planlib.protocols;

import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.IMessageEvent;
import jadex.bdi.runtime.Plan;

/**
 *  This plan serves as a base class for initiator plans.
 *  It automatically handles protocol cancellation when
 *  the plan is aborted. 
 */
public abstract class AbstractInitiatorPlan extends Plan
{
	//-------- attributes --------
	
	/** The timeout. */
	private long	timeout;
	
	//-------- constructors --------
	
	/**
	 *  Initialize the plan
	 */
	public AbstractInitiatorPlan()
	{
		// Determine timeout.
		if(hasParameter("timeout") && getParameter("timeout").getValue()!=null)
		{
			timeout = ((Long)getParameter("timeout").getValue()).longValue();
		}
		else if(getBeliefbase().containsBelief("timeout") && getBeliefbase().getBelief("timeout").getFact()!=null)
		{
			timeout = ((Long)getBeliefbase().getBelief("timeout").getFact()).longValue();
		}
		else
		{
			timeout = -1;
		}
		
		// Update interaction state
		if(hasParameter("interaction_state"))
		{
			InteractionState	state	= (InteractionState)getParameter("interaction_state").getValue();
			if(state==null)
			{
				state	= new InteractionState();
			}
			state.setInteractionState(InteractionState.INTERACTION_RUNNING);
		}
	}
	
	//-------- methods --------
	
	/**
	 *  Get the timeout.
	 */
	public long	getTimeout()
	{
		return timeout;
	}
	
	/**
	 *  Called when the plan is aborted, i.e.,
	 *  when the corresponding interaction goal
	 *  is dropped.
	 *  Terminates the interaction using the 
	 *  FIPA-Cancel-Meta-Protocol.
	 */
	public void aborted()
	{
		getLogger().info("Initiator aborting");
		IGoal	cancel	= createGoal("cmcap.cm_initiate");
		cancel.getParameter("message").setValue(getInitialMessage());
		cancel.getParameter("timeout").setValue(new Long(getTimeout()));
		if(hasParameter("interaction_state"))
		{
			cancel.getParameter("interaction_state").setValue(getParameter("interaction_state").getValue());
		}
		dispatchSubgoalAndWait(cancel);		
		getLogger().info("Initiator aborted");
	}
	
	//-------- template methods --------

	/**
	 *  Get the initial message.
	 *  Has to be provided by subclasses.
	 *  Needed for sending cancel message in
	 *  correct conversation.
	 */
	protected abstract IMessageEvent	getInitialMessage();
}
