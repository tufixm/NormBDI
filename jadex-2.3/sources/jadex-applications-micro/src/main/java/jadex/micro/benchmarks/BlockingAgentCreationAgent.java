package jadex.micro.benchmarks;

import jadex.bridge.ComponentIdentifier;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.types.clock.IClockService;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.Tuple;
import jadex.commons.future.IFuture;
import jadex.commons.transformation.annotations.Classname;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentArgument;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.Argument;
import jadex.micro.annotation.Arguments;
import jadex.micro.annotation.Description;

import java.util.HashMap;
import java.util.Map;

/**
 *  Agent creation benchmark using threaded components
 */
@Agent
@Description("This agents benchmarks agent creation and termination.")
@Arguments({
	@Argument(name="max", description="Maximum number of agents to create.", clazz=int.class, defaultvalue="10000"),
	@Argument(name="num", description="Number of agents already created.", clazz=int.class),
	@Argument(name="startime", description="Time when the first agent was started.", clazz=long.class),
	@Argument(name="startmem", description="Memory usage when the first agent was started", clazz=long.class)
})
public class BlockingAgentCreationAgent
{
	//-------- attributes --------
	
	/** The agent. */
	@Agent
	protected MicroAgent	agent;
	
	/** Maximum number of agents to create. */
	@AgentArgument
	protected int	max;
	
	/** Remaining number of agents to create (-1 for start agent). */
	@AgentArgument
	protected int	num;
	
	/** Time when the first agent was started. */
	@AgentArgument
	protected long	starttime;
	
	/** Memory usage when the first agent was started. */
	@AgentArgument
	protected long	startmem;
	
	//-------- methods --------
		
	/**
	 *  Execute an agent step.
	 */
	@AgentBody
	public void executeBody()
	{
		if(num==0)
		{
			IClockService	clock	= getClock();
			System.gc();
			try
			{
				Thread.sleep(500);
			}
			catch(InterruptedException e){}
			
			startmem = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
			starttime = clock.getTime();
		}
		
		num++;
		System.out.println("Created peer: "+num);
		
		if(num<max)
		{
			IComponentManagementService	cms	=  getCMS();
			Map<String, Object>	args	= new HashMap<String, Object>();
			args.put("max", new Integer(max));
			args.put("num", new Integer(num));
			args.put("starttime", new Long(starttime));
			args.put("startmem", new Long(startmem));
			cms.createComponent(createPeerName(num+1, agent.getComponentIdentifier()),
				BlockingAgentCreationAgent.this.getClass().getName().replaceAll("\\.", "/")+".class",
				new CreationInfo(null, args, agent.getComponentDescription().getResourceIdentifier()), null);
		}
		else
		{
			final IClockService	clock	= getClock();
			long	end	= clock.getTime();
			
			System.gc();
			try
			{
				Thread.sleep(500);
			}
			catch(InterruptedException e){}
			long used = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
			
			final long omem = (used-startmem)/1024;
			final double upera = ((long)(1000*(used-startmem)/max/1024))/1000.0;
			System.out.println("Overall memory usage: "+omem+"kB. Per agent: "+upera+" kB.");
			System.out.println("Last peer created. "+max+" agents started.");
			final double dur = ((double)end-starttime)/1000.0;
			final double pera = dur/max;
			System.out.println("Needed: "+dur+" secs. Per agent: "+pera+" sec. Corresponds to "+(1/pera)+" agents per sec.");
		
			// Use initial component to kill others
			IComponentManagementService cms	= getCMS();
			String	initial	= createPeerName(1, agent.getComponentIdentifier());
			IComponentIdentifier	cid	= new ComponentIdentifier(initial, agent.getComponentIdentifier().getRoot());
			IExternalAccess exta	= cms.getExternalAccess(cid).get();
			exta.scheduleStep(new IComponentStep<Void>()
			{
				@Classname("deletePeers")
				public IFuture<Void> execute(IInternalAccess ia)
				{
					long	killstarttime	= clock.getTime();
					IComponentManagementService	cms	= getCMS();
					for(int i=max; i>1; i--)
					{
						String name = createPeerName(i, ia.getComponentIdentifier());
						IComponentIdentifier cid = new ComponentIdentifier(name, ia.getComponentIdentifier().getRoot());
						cms.destroyComponent(cid).get();
						System.out.println("Successfully destroyed peer: "+name);
					}
					
					long killend = clock.getTime();
					System.out.println("Last peer destroyed. "+(max-1)+" agents killed.");
					double killdur = ((double)killend-killstarttime)/1000.0;
					final double killpera = killdur/(max-1);
					
					Runtime.getRuntime().gc();
					try
					{
						Thread.sleep(500);
					}
					catch(InterruptedException e){}
					long stillused = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024;
					
					System.out.println("\nCumulated results:");
					System.out.println("Creation needed: "+dur+" secs. Per agent: "+pera+" sec. Corresponds to "+(1/pera)+" agents per sec.");
					System.out.println("Killing needed:  "+killdur+" secs. Per agent: "+killpera+" sec. Corresponds to "+(1/killpera)+" agents per sec.");
					System.out.println("Overall memory usage: "+omem+"kB. Per agent: "+upera+" kB.");
					System.out.println("Still used memory: "+stillused+"kB.");
					
					ia.setResultValue("microcreationtime", new Tuple(""+pera, "s"));
					ia.setResultValue("microkillingtime", new Tuple(""+killpera, "s"));
					ia.setResultValue("micromem", new Tuple(""+upera, "kb"));
					ia.killComponent();

					return IFuture.DONE;
				}
			});
		}
	}

	/**
	 *  Create a name for a peer with a given number.
	 */
	protected String createPeerName(int num, IComponentIdentifier cid)
	{
		String	name = cid.getLocalName();
		int	index	= name.indexOf("Peer_#");
		if(index!=-1)
		{
			name	= name.substring(0, index);
		}
		if(num!=1)
		{
			name	+= "Peer_#"+num;
		}
		return name;
	}
	
	protected IComponentManagementService	getCMS()
	{
		return agent.getServiceContainer().searchServiceUpwards(IComponentManagementService.class).get();
	}
	
	
	protected IClockService getClock()
	{
		return agent.getServiceContainer().searchServiceUpwards(IClockService.class).get();
	}
}
