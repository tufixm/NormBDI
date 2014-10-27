package jadex.micro.tutorial;

import jadex.bridge.service.component.ComponentFactorySelector;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.factory.IComponentFactory;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.micro.MicroAgent;
import jadex.micro.MicroAgentFactory;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.Description;

/**
 *  Chat micro agent that search the factory for micro agents. 
 */
@Description("This agent search the factory for micro agents.")
@Agent
public class ChatC4Agent
{
	/** The underlying mirco agent. */
	@Agent
	protected MicroAgent agent;
	
	/**
	 *  Execute the functional body of the agent.
	 *  Is only called once.
	 */
	@AgentBody
	public void executeBody()
	{
		IFuture<IComponentFactory>	factory	= SServiceProvider.getService(agent.getServiceContainer(), 
			new ComponentFactorySelector(MicroAgentFactory.FILETYPE_MICROAGENT));
		factory.addResultListener(agent.createResultListener(new IResultListener<IComponentFactory>()
		{
			public void resultAvailable(IComponentFactory result)
			{
				System.out.println("Found: "+result);
			}
			public void	exceptionOccurred(Exception e)
			{
				System.out.println("Not found: "+e);				
			}
		}));
	}
}