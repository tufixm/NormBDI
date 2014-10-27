package jadex.micro.benchmarks;

import jadex.bridge.ComponentIdentifier;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.types.clock.IClockService;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.CounterResultListener;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.transformation.annotations.Classname;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Argument;
import jadex.micro.annotation.Arguments;
import jadex.micro.annotation.Description;

import java.util.HashMap;
import java.util.Map;

/**
 *  Agent creation benchmark. 
 */
@Description("This agents benchmarks parallel agent creation and termination.")
@Arguments(
{
	@Argument(name="num", clazz=Integer.class, defaultvalue="10000", description="Maximum number of agents to create.")
})
public class ParallelAgentCreationAgent extends MicroAgent
{
	//-------- methods --------
	
	/**
	 *  Execute an agent step.
	 */
	public IFuture<Void> executeBody()
	{
		Map arguments = getArguments();			
		final int num	= ((Integer)arguments.get("num")).intValue();
		if(num>0)
		{
			getServiceContainer().searchServiceUpwards(IComponentManagementService.class).addResultListener(new DefaultResultListener()
			{
				public void resultAvailable(Object result)
				{
					final IComponentManagementService	cms	= (IComponentManagementService)result;
					getServiceContainer().searchService(IClockService.class, RequiredServiceInfo.SCOPE_PLATFORM).addResultListener(new DefaultResultListener()
					{
						public void resultAvailable(Object result)
						{
							final IClockService	clock	= (IClockService)result;
							final long	startmem	= Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
							final long	starttime	= clock.getTime();
							final long[]	omem	= new long[1];
							final double[]	dur	= new double[1];
							final long[]	killstarttime	= new long[1];
							
							IResultListener	creationlis	= new CounterResultListener(num, new IResultListener()
							{
								public void resultAvailable(Object result)
								{
									scheduleStep(new IComponentStep<Void>()
									{
										@Classname("destroy1")
										public IFuture<Void> execute(IInternalAccess ia)
										{
											long used = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
											omem[0] = (used-startmem)/1024;
											double upera = ((long)(1000*omem[0]/num))/1000.0;
											System.out.println("Overall memory usage: "+omem[0]+"kB. Per agent: "+upera+" kB.");
											long end = clock.getTime();
											System.out.println("Last peer created. "+num+" agents started.");
											dur[0] = ((double)end-starttime)/1000.0;
											double pera = dur[0]/num;
											System.out.println("Needed: "+dur[0]+" secs. Per agent: "+pera+" sec. Corresponds to "+(1/pera)+" agents per sec.");
											
											killstarttime[0]	= clock.getTime();
											for(int i=num; i>0; i--)
											{
												String name = createPeerName(i);
//												IComponentIdentifier cid = cms.createComponentIdentifier(name, true, null);
												final IComponentIdentifier cid = new ComponentIdentifier(name, getComponentIdentifier().getRoot());
												cms.destroyComponent(cid).addResultListener(new IResultListener<Map<String, Object>>()
												{
													public void resultAvailable(Map<String, Object> result)
													{
														System.out.println("Successfully destroyed peer: "+cid);
													}
													
													public void exceptionOccurred(Exception exception)
													{
														// Ignore: Kill listener already added on create.	
													};
												});
											}		
											return IFuture.DONE;
										}
									});
								}
								
								public void exceptionOccurred(final Exception exception)
								{
									scheduleStep(new IComponentStep<Void>()
									{
										@Classname("destroy2")
										public IFuture<Void> execute(IInternalAccess ia)
										{
											if(exception instanceof RuntimeException)
												throw (RuntimeException)exception;
											else
												throw new RuntimeException(exception);
										}
									});
								}
							})
							{
								public void intermediateResultAvailable(Object result)
								{
									System.out.println("Created peer: "+getCnt());
								}
							};
							
							
							IResultListener	killlis	= new CounterResultListener(num, new IResultListener()
							{
								public void resultAvailable(Object result)
								{
									scheduleStep(new IComponentStep<Void>()
									{
										@Classname("last")
										public IFuture<Void> execute(IInternalAccess ia)
										{
											long killend = clock.getTime();
											System.out.println("Last peer destroyed. "+num+" agents killed.");
											double killdur = ((double)killend-killstarttime[0])/1000.0;
											double killpera = killdur/num;
											
											Runtime.getRuntime().gc();
											long stillused = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024;
											double pera = dur[0]/num;
											double upera = ((long)(1000*omem[0]/num))/1000.0;

											System.out.println("\nCumulated results:");
											System.out.println("Creation needed: "+dur[0]+" secs. Per agent: "+pera+" sec. Corresponds to "+(1/pera)+" agents per sec.");
											System.out.println("Killing needed:  "+killdur+" secs. Per agent: "+killpera+" sec. Corresponds to "+(1/killpera)+" agents per sec.");
											System.out.println("Overall memory usage: "+omem[0]+"kB. Per agent: "+upera+" kB.");
											System.out.println("Still used memory: "+stillused+"kB.");
									
											killAgent();
											
											return IFuture.DONE;
										}
									});
								}
								
								public void exceptionOccurred(final Exception exception)
								{
									scheduleStep(new IComponentStep<Void>()
									{
										@Classname("destroyMe")
										public IFuture<Void> execute(IInternalAccess ia)
										{
											if(exception instanceof RuntimeException)
												throw (RuntimeException)exception;
											else
												throw new RuntimeException(exception);
										}
									});
								}
							});
							
							Map	args	= new HashMap();
							args.put("num", new Integer(0));
							CreationInfo	cinfo	= new CreationInfo(null, args, getComponentDescription().getResourceIdentifier());
							for(int i=1; i<=num; i++)
							{
								cms.createComponent(createPeerName(i), ParallelAgentCreationAgent.this.getClass().getName()+".class", cinfo, killlis)
									.addResultListener(creationlis);
							}
						}
					});
				}
			});
		}
		
		return new Future<Void>(); // never kill?!
	}

	/**
	 *  Create a name for a peer with a given number.
	 */
	protected String createPeerName(int num)
	{
		return getComponentIdentifier().getLocalName() + "Peer_#" + num;
	}
	
//	/**
//	 *  Get the meta information about the agent.
//	 */
//	public static Object getMetaInfo()
//	{
//		return new MicroAgentMetaInfo("This agents benchmarks parallel agent creation and termination.", 
//			new String[0],
//			new IArgument[]{new Argument("num", "Number of agents to create.", "Integer", new Integer(10000))
//			{
//				public boolean validate(String input)
//				{
//					boolean ret = true;
//					try
//					{
//						Integer.parseInt(input);
//					}
//					catch(Exception e)
//					{
//						ret = false;
//					}
//					return ret;
//				}
//			}}, null, null, null);
//	}
}
