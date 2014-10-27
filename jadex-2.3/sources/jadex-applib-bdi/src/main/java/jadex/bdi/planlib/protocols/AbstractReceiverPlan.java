package jadex.bdi.planlib.protocols;

import jadex.bdi.runtime.IGoal;
import jadex.bdi.runtime.IMessageEvent;
import jadex.bdi.runtime.Plan;
import jadex.bridge.fipa.SFipa;

/**
 *  This plan serves as a base class for receiver plans.
 *  It creates the interaction state object and
 *  updates it when the plan terminates.
 */
public abstract class AbstractReceiverPlan extends Plan
{
	//-------- constructors --------
	
	/**
	 *  Initialize the plan
	 */
	public AbstractReceiverPlan()
	{
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

		// Store interaction description
		if(hasParameter("interaction_description") && hasParameter("message"))
		{
			getParameter("interaction_description").setValue(((IMessageEvent)getParameter("message").getValue()).getParameter(SFipa.CONTENT).getValue());
		}
		
		// Create cancel handler
		IGoal	cmhandler	= createGoal("cmcap.cm_handler");
		cmhandler.getParameter("interaction_goal").setValue(getReason());
		dispatchSubgoal(cmhandler);
	}
	
	//-------- methods --------

	/**
	 *  Called when the plan is finished, i.e. when
	 *  the interaction is completed.
	 */
	public void passed()
	{
		if(hasParameter("interaction_state"))
		{
			InteractionState	state	= (InteractionState)getParameter("interaction_state").getValue();
			if(InteractionState.INTERACTION_RUNNING.equals(state.getInteractionState()))
			{
				state.setInteractionState(InteractionState.INTERACTION_FINISHED);
			}
		}
	}
	
	/**
	 *  Called when the plan fails, i.e.,
	 *  a problem occurred during protocol execution.
	 */
	public void failed()
	{
		getLogger().severe("Problem during interaction: "+this+", "+getException());
	}
}
