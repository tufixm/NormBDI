package jadex.micro.tutorial;

import jadex.bridge.service.types.cms.IComponentDescription;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IFuture;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

/**
 *  Chat micro agent that uses the clock service. 
 */
@Description("This agent uses the component management service.")
@Agent
@RequiredServices(@RequiredService(name="cms", type=IComponentManagementService.class, 
	binding=@Binding(scope=Binding.SCOPE_PLATFORM)))
public class ChatC3Agent
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
		IFuture<IComponentManagementService>	cms	= agent.getServiceContainer().getRequiredService("cms");
		cms.addResultListener(new DefaultResultListener<IComponentManagementService>()
		{
			public void resultAvailable(final IComponentManagementService cms)
			{
				cms.getComponentDescriptions().addResultListener(
					new DefaultResultListener<IComponentDescription[]>()
				{
					public void resultAvailable(IComponentDescription[] descs)
					{
						for(int i=0; i<descs.length; i++)
						{
							System.out.println("Found: "+descs[i]);
						}
					}
				});
			}
		});
	}
}