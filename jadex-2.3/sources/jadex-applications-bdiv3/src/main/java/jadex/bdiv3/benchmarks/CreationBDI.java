package jadex.bdiv3.benchmarks;

import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.PojoBDIAgent;
import jadex.bdiv3.annotation.BDIConfiguration;
import jadex.bdiv3.annotation.BDIConfigurations;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.runtime.impl.RPlan;
import jadex.bridge.ComponentIdentifier;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.types.clock.IClockService;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.Tuple;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IFuture;
import jadex.commons.transformation.annotations.Classname;
import jadex.micro.PojoMicroAgent;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentArgument;
import jadex.micro.annotation.Argument;
import jadex.micro.annotation.Arguments;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.NameValue;

import java.util.HashMap;
import java.util.Map;

/**
 *  Agent creation benchmark BDI V3.
 */
@Agent
@Description("This agents benchmarks BDI V3 agent creation and termination.")
@Arguments(
{
	@Argument(name="max", description="Maximum number of agents to create.", clazz=int.class, defaultvalue="10000"),
	@Argument(name="num", description="Number of agents already created.", clazz=int.class),
	@Argument(name="startime", description="Time when the first agent was started.", clazz=long.class),
	@Argument(name="startmem", description="Memory usage when the first agent was started", clazz=long.class)
})
@BDIConfigurations(
	@BDIConfiguration(name="first", initialplans=@NameValue(name="startPeer"))
)
public class CreationBDI
{
	//-------- attributes --------
	
	/** The component. */
	@Agent
	protected BDIAgent agent;
	
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
		if(num>0)
		{
			name	+= "Peer_#"+num;
		}
		return name;
	}
	
	// todo: plan creation condition
	@Plan
	protected void startPeer(RPlan rplan)
	{
		if(starttime==0)
		{
			getClock().addResultListener(new DefaultResultListener<IClockService>()
			{
				public void resultAvailable(IClockService result)
				{
					System.gc();
					try
					{
						Thread.sleep(500);
					}
					catch(InterruptedException e){}
					
					startmem = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
					starttime = ((IClockService)result).getTime();
					
					step1();
				}
			});
		}
		else
		{
			step1();
		}
	}
	
	protected void	step1()
	{
		System.out.println("Created peer: "+num);
		
		if(num<max)
		{
			final Map<String, Object> args = new HashMap<String, Object>();
			args.put("num", new Integer(num+1));
			args.put("max", new Integer(max));
			args.put("starttime", new Long(starttime));
			args.put("startmem", new Long(startmem));
//			System.out.println("Args: "+num+" "+args);

			agent.getServiceContainer().searchServiceUpwards(IComponentManagementService.class)
				.addResultListener(new DefaultResultListener<IComponentManagementService>()
			{
				public void resultAvailable(IComponentManagementService result)
				{
					((IComponentManagementService)result).createComponent(createPeerName(num+1, agent.getComponentIdentifier()), CreationBDI.class.getName().replaceAll("\\.", "/")+".class",
						new CreationInfo(null, args, null, null, null, null, null, null, null, agent.getComponentDescription().getResourceIdentifier()), null);
				}
			});
		}
		else
		{
			getClock().addResultListener(new DefaultResultListener<IClockService>()
			{
				public void resultAvailable(final IClockService clock)
				{
					final long end = clock.getTime();
					
					System.gc();
					try
					{
						Thread.sleep(500);
//						Thread.sleep(500000);
					}
					catch(InterruptedException e){}
					final long used = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
					
					final long omem = (used-startmem)/1024;
					final double upera = ((long)(1000*(used-startmem)/max/1024))/1000.0;
					System.out.println("Overall memory usage: "+omem+"kB. Per agent: "+upera+" kB.");
					System.out.println("Last peer created. "+max+" agents started.");
					final double dur = ((double)end-starttime)/1000.0;
					final double pera = dur/max;
					System.out.println("Needed: "+dur+" secs. Per agent: "+pera+" sec. Corresponds to "+(1/pera)+" agents per sec.");
				
					// Use initial component to kill others
					getCMS().addResultListener(new DefaultResultListener<IComponentManagementService>()
					{
						public void resultAvailable(IComponentManagementService cms)
						{
							String	initial	= createPeerName(0, agent.getComponentIdentifier());
							IComponentIdentifier	cid	= new ComponentIdentifier(initial, agent.getComponentIdentifier().getRoot());
							cms.getExternalAccess(cid).addResultListener(new DefaultResultListener<IExternalAccess>()
							{
								public void resultAvailable(IExternalAccess exta)
								{
									exta.scheduleStep(new IComponentStep<Void>()
									{
										@Classname("deletePeers")
										public IFuture<Void> execute(IInternalAccess ia)
										{
											((CreationBDI)((PojoBDIAgent)ia).getPojoAgent())
												.deletePeers(max, clock.getTime(), dur, pera, omem, upera);
											return IFuture.DONE;
										}
									});
								}
							});
						}
					});
				}
			});
		}
	}
	
	/**
	 *  Delete all peers from last-1 to first.
	 *  @param cnt The highest number of the agent to kill.
	 */
	protected void deletePeers(final int cnt, final long killstarttime, final double dur, final double pera,
		final long omem, final double upera)
	{
		final String name = createPeerName(cnt, agent.getComponentIdentifier());
		getCMS().addResultListener(new DefaultResultListener<IComponentManagementService>()
		{
			public void resultAvailable(IComponentManagementService cms)
			{
				IComponentIdentifier aid = new ComponentIdentifier(name, agent.getComponentIdentifier().getRoot());
				cms.destroyComponent(aid).addResultListener(new DefaultResultListener<Map<String, Object>>()
				{
					public void resultAvailable(Map<String, Object> result)
					{
						System.out.println("Successfully destroyed peer: "+name);
						
						if(cnt>1)
						{
							deletePeers(cnt-1, killstarttime, dur, pera, omem, upera);
						}
						else
						{
							killLastPeer(killstarttime, dur, pera, omem, upera);
						}
					}
				});
			}
		});
	}

	/**
	 *  Kill the last peer and print out the results.
	 */
	protected void killLastPeer(final long killstarttime, final double dur, final double pera, 
		final long omem, final double upera)
	{
		getClock().addResultListener(new DefaultResultListener<IClockService>()
		{
			public void resultAvailable(IClockService cs)
			{
				long killend = cs.getTime();
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
				
				agent.setResultValue("microcreationtime", new Tuple(""+pera, "s"));
				agent.setResultValue("microkillingtime", new Tuple(""+killpera, "s"));
				agent.setResultValue("micromem", new Tuple(""+upera, "kb"));
				agent.killComponent();
			}
		});
	}
	
	/**
	 *  Get the clock service.
	 */
	protected IFuture<IClockService> getClock()
	{
		return agent.getServiceContainer().searchServiceUpwards(IClockService.class);
	}

	/**
	 *  Get the cms.
	 */
	protected IFuture<IComponentManagementService>	getCMS()
	{
		return agent.getServiceContainer().searchServiceUpwards(IComponentManagementService.class);
	}

	
//	public static void main(String[] args) throws Exception
//	{
//		Field f = CreationBDI.class.getDeclaredField("num");
//		f.setAccessible(true);
//		f.set(null, new Integer(1));
//		System.out.println(f.get(null));
//	}
}
