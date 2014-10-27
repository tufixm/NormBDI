package jadex.bdi.planlib.protocols.cancelmeta;

import jadex.bdi.planlib.protocols.InteractionState;
import jadex.bdi.runtime.IMessageEvent;
import jadex.bdi.runtime.Plan;
import jadex.bdi.runtime.TimeoutException;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.fipa.SFipa;
import jadex.commons.SUtil;

import java.util.List;

/**
 *  Plan to handle protocol abortion according to FIPA cancel meta protocol
 */
public class CMInitiatorPlan extends Plan
{
	public void body()
	{
		// Try to abort the interaction using FIPA-Cancel-Meta-Protocol.
		// Results of cancellation will be stored in interaction state (if any).
		InteractionState	state	= null;
		if(hasParameter("interaction_state"))
		{
			state	= (InteractionState)getParameter("interaction_state").getValue();
		}

		// Copy message properties from initial message.
		IMessageEvent	message	= (IMessageEvent)getParameter("message").getValue();
		IMessageEvent	cancel	= createMessageEvent("cm_cancel");
		cancel.getParameterSet(SFipa.RECEIVERS).addValues(message.getParameterSet(SFipa.RECEIVERS).getValues());
		cancel.getParameter(SFipa.CONVERSATION_ID).setValue(message.getParameter(SFipa.CONVERSATION_ID).getValue());
		cancel.getParameter(SFipa.LANGUAGE).setValue(message.getParameter(SFipa.LANGUAGE).getValue());
		cancel.getParameter(SFipa.ONTOLOGY).setValue(message.getParameter(SFipa.ONTOLOGY).getValue());
		// Use extra reply_with to avoid intermingling with other protocol messages.
		cancel.getParameter(SFipa.REPLY_WITH).setValue(SUtil.createUniqueId(getComponentName()));

		// Send cancel message to participants.
		long	timeout	= ((Number)getParameter("timeout").getValue()).longValue();
		getWaitqueue().addReply(cancel);
		sendMessage(cancel);
		long time = getTime();
		List	rec	= SUtil.arrayToList(message.getParameterSet(SFipa.RECEIVERS).getValues());
		try
		{
			while(rec.size()>0)
			{
				// Wait for the replies.
				long wait_time = timeout + time - getTime();
				if(wait_time <= 0)
					break;

				IMessageEvent reply = waitForReply(cancel, wait_time);
				rec.remove(reply.getParameter(SFipa.SENDER).getValue());
				
				// Store result in interaction state.
				if(state!=null)
				{
					String	response	= "cm_inform".equals(reply.getType()) ? InteractionState.CANCELLATION_SUCCEEDED
						: "cm_failure".equals(reply.getType()) ? InteractionState.CANCELLATION_FAILED
						: InteractionState.CANCELLATION_UNKNOWN;
					state.addCancelResponse((IComponentIdentifier)reply.getParameter(SFipa.SENDER).getValue(),
						response, reply.getParameter(SFipa.CONTENT).getValue());
				}
			}
		}
		catch(TimeoutException e)
		{
			// Set result of non-responders to unknown.
			if(state!=null)
			{
				for(int i=0; i<rec.size(); i++)
				{
					state.addCancelResponse((IComponentIdentifier)rec.get(i),
						InteractionState.CANCELLATION_UNKNOWN, null);
				}
			}		
		}
	}
}
