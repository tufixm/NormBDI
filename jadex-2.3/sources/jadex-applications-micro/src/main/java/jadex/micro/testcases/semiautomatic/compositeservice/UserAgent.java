package jadex.micro.testcases.semiautomatic.compositeservice;

import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

/**
 *  The user agent uses services for testing them.
 */
@Description("This agent uses an add service.")
@RequiredServices(@RequiredService(name="addservice", type=IAddService.class, binding=@Binding(scope=Binding.SCOPE_PLATFORM)))
public class UserAgent extends MicroAgent
{
	/**
	 *  Execute the functional body of the agent.
	 *  Is only called once.
	 */
	public IFuture<Void> executeBody()
	{
		getRequiredService("addservice").addResultListener(new DefaultResultListener()
		{
			public void resultAvailable(Object result)
			{
				IAddService addser = (IAddService)result;
				addser.add(1, 1).addResultListener(new IResultListener()
				{
					public void resultAvailable(Object result)
					{
						System.out.println("add service result: "+result+" "+getComponentIdentifier().getLocalName());
					}
					
					public void exceptionOccurred(Exception exception)
					{
						System.out.println("invocation failed: "+exception);
					}
				});
			}
		});
		
		return new Future<Void>(); // never kill?!
	}
	
	//-------- static methods --------

//	/**
//	 *  Get the meta information about the agent.
//	 */
//	public static MicroAgentMetaInfo getMetaInfo()
//	{
//		return new MicroAgentMetaInfo("This agent uses an add service.", null, 
//			new IArgument[]{}, null, null, SUtil.createHashMap(new String[]{"componentviewer.viewerclass"}, new Object[]{"jadex.micro.examples.chat.ChatPanel"}),
//			new RequiredServiceInfo[]{new RequiredServiceInfo("addservice", IAddService.class)}, new ProvidedServiceInfo[]{new ProvidedServiceInfo(IChatService.class)});
//	}
}
