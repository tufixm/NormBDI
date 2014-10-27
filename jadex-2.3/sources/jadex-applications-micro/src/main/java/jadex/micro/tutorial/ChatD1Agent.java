package jadex.micro.tutorial;

import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IFuture;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

import java.util.Collection;
import java.util.Iterator;

/**
 *  Chat micro agent provides a basic chat service. 
 */
@Description("This agent provides a basic chat service.")
@Agent
@ProvidedServices(@ProvidedService(type=IChatService.class, 
	implementation=@Implementation(ChatServiceD1.class)))
@RequiredServices({
	@RequiredService(name="clockservice", type=IClockService.class, 
		binding=@Binding(scope=Binding.SCOPE_PLATFORM)),
	@RequiredService(name="chatservices", type=IChatService.class, multiple=true,
		binding=@Binding(dynamic=true, scope=Binding.SCOPE_PLATFORM))
})
public class ChatD1Agent
{
	/** The underlying micro agent. */
	@Agent
	protected MicroAgent agent;
	
	/**
	 *  Execute the functional body of the agent.
	 *  Is only called once.
	 */
	@AgentBody
	public void executeBody()
	{
		IFuture<Collection<IChatService>>	chatservices	= agent.getServiceContainer().getRequiredServices("chatservices");
		chatservices.addResultListener(new DefaultResultListener<Collection<IChatService>>()
		{
			public void resultAvailable(Collection<IChatService> result)
			{
				for(Iterator<IChatService> it=result.iterator(); it.hasNext(); )
				{
					IChatService cs = it.next();
					cs.message(agent.getComponentIdentifier().getName(), "Hello");
				}
			}
		});
	}
}