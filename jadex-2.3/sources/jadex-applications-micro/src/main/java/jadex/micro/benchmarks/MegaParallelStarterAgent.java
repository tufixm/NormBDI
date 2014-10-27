package jadex.micro.benchmarks;

import jadex.bridge.ComponentIdentifier;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.service.types.clock.IClockService;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.Tuple;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Argument;
import jadex.micro.annotation.Arguments;

import java.util.HashMap;
import java.util.Map;

/**
 *  Agent creation benchmark. 
 */
@Arguments({
	@Argument(name="max", defaultvalue="20000", clazz=int.class)
})
public class MegaParallelStarterAgent extends MicroAgent 
{
	protected String subname;
	
	protected int agents;
	
	protected long startmem;
	protected long starttime;
	protected long omem;
	protected double dur;
	protected double pera;
	protected double upera;
	
	protected long killstarttime;
	
	//-------- methods --------
	
	/**
	 *  Execute an agent step.
	 */
	public IFuture<Void> executeBody()
	{
		Map arguments = getArguments();	
		if(arguments==null)
			arguments = new HashMap();
		final Map args = arguments;	

		System.out.println("Created starter: "+getComponentIdentifier());
		this.subname = "peer";
		
		getClock().addResultListener(new DefaultResultListener()
		{
			public void resultAvailable(Object result)
			{
				startmem = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
				starttime = ((IClockService)result).getTime();
				
				final int max = ((Integer)args.get("max")).intValue();
				
				getCMS().addResultListener(new DefaultResultListener()
				{
					public void resultAvailable(Object result)
					{
						IComponentManagementService cms = (IComponentManagementService)result;
						String model = MegaParallelCreationAgent.class.getName().replaceAll("\\.", "/")+".class";
						for(int i=1; i<=max; i++)
						{
							args.put("num", new Integer(i));
//							System.out.println("Created agent: "+i);
							cms.createComponent(subname+"_#"+i, model, new CreationInfo(new HashMap(args), getComponentIdentifier()), 
								createResultListener(new DefaultResultListener()
							{
								public void resultAvailable(Object result)
								{
									if(--agents==0)
									{
										getClock().addResultListener(new DefaultResultListener()
										{
											public void resultAvailable(final Object result)
											{
												IClockService cs = (IClockService)result;
												long killend = cs.getTime();
												System.out.println("Last peer destroyed. "+(max-1)+" agents killed.");
												double killdur = ((double)killend-killstarttime)/1000.0;
												final double killpera = killdur/(max-1);
												
												long stillused = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024;
												
												System.out.println("\nCumulated results:");
												System.out.println("Creation needed: "+dur+" secs. Per agent: "+pera+" sec. Corresponds to "+(1/pera)+" agents per sec.");
												System.out.println("Killing needed:  "+killdur+" secs. Per agent: "+killpera+" sec. Corresponds to "+(1/killpera)+" agents per sec.");
												System.out.println("Overall memory usage: "+omem+"kB. Per agent: "+upera+" kB.");
												System.out.println("Still used memory: "+stillused+"kB.");
												
												setResultValue("microcreationtime", new Tuple(""+pera, "s"));
												setResultValue("microkillingtime", new Tuple(""+killpera, "s"));
												setResultValue("micromem", new Tuple(""+upera, "kb"));
												killComponent();
											}
										});
									}
								}
							})).addResultListener(createResultListener(new IResultListener()
							{
								public void resultAvailable(Object result)
								{
									if(++agents==max)
									{
										getClock().addResultListener(new DefaultResultListener()
										{
											public void resultAvailable(final Object result)
											{
												final IClockService	clock	= (IClockService)result;
												final long end = clock.getTime();
												final long used = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
												omem = (used-startmem)/1024;
												upera = ((long)(1000*(used-startmem/max/1024)))/1000.0;
												System.out.println("Overall memory usage: "+omem+"kB. Per agent: "+upera+" kB.");
												System.out.println("Last peer created. "+max+" agents started.");
												dur = ((double)end-starttime)/1000.0;
												pera = dur/max;
												System.out.println("Needed: "+dur+" secs. Per agent: "+pera+" sec. Corresponds to "+(1/pera)+" agents per sec.");
		
												killstarttime = clock.getTime();
												deletePeers(max);
											}
										});
									}
								}
								public void exceptionOccurred(Exception exception)
								{
									// ignore
									// In case of ComponentStartTest the agent will be started
									// and immediately terminated but already has scheduled
									// all creation actions to the cms.
								}
							}));
						}
					}
				});
			}
		});
		
		return new Future<Void>(); // never kill
	}

	/**
	 *  Delete all peers from last-1 to first.
	 *  @param cnt The highest number of the agent to kill.
	 */
	protected void deletePeers(final int cnt)
	{
		final String name = subname+"_#"+cnt;
//		System.out.println("Destroying peer: "+name);
		getCMS().addResultListener(new DefaultResultListener()
		{
			public void resultAvailable(final Object result)
			{
				IComponentManagementService cms = (IComponentManagementService)result;
//				IComponentIdentifier aid = cms.createComponentIdentifier(name, getComponentIdentifier(), null);
				IComponentIdentifier aid = new ComponentIdentifier(name, getComponentIdentifier());
				IResultListener lis = new IResultListener()
				{
					public void resultAvailable(Object result)
					{
						System.out.println("Successfully destroyed peer: "+name);
						
						if(cnt>1)
						{
							deletePeers(cnt-1);
						}
					}
					public void exceptionOccurred(Exception exception)
					{
						exception.printStackTrace();
					}
				};
				IFuture ret = cms.destroyComponent(aid);
				ret.addResultListener(lis);
			}
		});
	}
	
	protected IFuture	getCMS()
	{
		IFuture cms = null;	// Uncomment for no caching.
		if(cms==null)
		{
			cms	= getServiceContainer().searchServiceUpwards(IComponentManagementService.class); // Raw service
//			cms	= getRequiredService("cmsservice");	// Required service proxy
		}
		return cms;
	}
	
	
	protected IFuture	getClock()
	{
		IFuture clock = null;	// Uncomment for no caching.
		if(clock==null)
		{
			clock	= getServiceContainer().searchServiceUpwards(IClockService.class); // Raw service
//			clock	= getRequiredService("clockservice");	// Required service proxy
		}
		return clock;
	}

}
