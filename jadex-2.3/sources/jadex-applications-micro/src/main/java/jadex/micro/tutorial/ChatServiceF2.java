package jadex.micro.tutorial;

import jadex.bridge.IInternalAccess;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceComponent;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IFuture;
import jadex.micro.IPojoMicroAgent;

import java.util.Collection;
import java.util.Iterator;

/**
 *  Chat service implementation.
 */
@Service
public class ChatServiceF2 implements IChatService
{
	//-------- attributes --------
	
	/** The agent. */
	@ServiceComponent
	protected IInternalAccess agent;
			
	//-------- attributes --------	
	
	/**
	 *  Receives a chat message.
	 *  @param sender The sender's name.
	 *  @param text The message text.
	 */
	public void message(final String sender, final String text)
	{
		// Reply if the message contains the keyword.
		final ChatBotF2Agent	chatbot	= (ChatBotF2Agent)((IPojoMicroAgent)agent).getPojoAgent();
		if(text.toLowerCase().indexOf(chatbot.getKeyword().toLowerCase())!=-1)
		{
			IFuture<Collection<IChatService>> fut = agent.getServiceContainer().getRequiredServices("chatservices");
			fut.addResultListener(new DefaultResultListener<Collection<IChatService>>()
			{
				public void resultAvailable(Collection<IChatService> result)
				{
					for(Iterator<IChatService> it=result.iterator(); it.hasNext(); )
					{
						IChatService cs = it.next();
						cs.message(agent.getComponentIdentifier().getName(), chatbot.getReply()+": "+sender);
					}
				}
			});
		}
	}
}
