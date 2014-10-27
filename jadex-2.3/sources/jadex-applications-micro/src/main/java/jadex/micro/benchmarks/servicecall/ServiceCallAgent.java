package jadex.micro.benchmarks.servicecall;

import jadex.base.test.TestReport;
import jadex.base.test.Testcase;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.IService;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentArgument;
import jadex.micro.annotation.Argument;
import jadex.micro.annotation.Arguments;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;
import jadex.micro.testcases.TestAgent;

import java.util.HashMap;
import java.util.Map;

/**
 *  Agent providing a direct service.
 */
@RequiredServices({
	@RequiredService(name="raw", type=IServiceCallService.class, binding=@Binding(proxytype=Binding.PROXYTYPE_RAW, dynamic=true, scope=Binding.SCOPE_GLOBAL)),
	@RequiredService(name="direct", type=IServiceCallService.class, binding=@Binding(proxytype=Binding.PROXYTYPE_DIRECT, dynamic=true, scope=Binding.SCOPE_GLOBAL)),
	@RequiredService(name="decoupled", type=IServiceCallService.class, binding=@Binding(proxytype=Binding.PROXYTYPE_DECOUPLED, dynamic=true, scope=Binding.SCOPE_GLOBAL)),
	@RequiredService(name="cms", type=IComponentManagementService.class, binding=@Binding(scope=Binding.SCOPE_PLATFORM))
})
@Agent
@Arguments(replace=false,
	value=@Argument(name="max", clazz=int.class, defaultvalue="100"))
public class ServiceCallAgent	extends TestAgent
{
	//-------- attributes --------
	
	/** The agent. */
	@Agent
	protected IInternalAccess	agent;
	
	/** The invocation count. */
	@AgentArgument
	protected int	max;
	
	//-------- methods --------
	
	/**
	 *  The agent body.
	 */
	protected IFuture<Void> performTests(final Testcase tc)
	{
		final Future<Void>	ret	= new Future<Void>();
		
		IFuture<IComponentManagementService>	fut	= agent.getServiceContainer().getRequiredService("cms");
		fut.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, Void>(ret)
		{
			public void customResultAvailable(final IComponentManagementService cms)
			{
				test(cms, true).addResultListener(new ExceptionDelegationResultListener<TestReport, Void>(ret)
				{
					public void customResultAvailable(TestReport result)
					{
						tc.addReport(result);
						createPlatform(null).addResultListener(new ExceptionDelegationResultListener<IExternalAccess, Void>(ret)
						{
							public void customResultAvailable(final IExternalAccess exta)
							{
								createProxy(cms, exta).addResultListener(new ExceptionDelegationResultListener<IComponentIdentifier, Void>(ret)
								{
									public void customResultAvailable(IComponentIdentifier result)
									{
										SServiceProvider.getService(exta.getServiceProvider(), IComponentManagementService.class)
											.addResultListener(agent.createResultListener(new ExceptionDelegationResultListener<IComponentManagementService, Void>(ret)
										{
											public void customResultAvailable(IComponentManagementService cms2)
											{
												test(cms2, false).addResultListener(new ExceptionDelegationResultListener<TestReport, Void>(ret)
												{
													public void customResultAvailable(TestReport result)
													{
														tc.addReport(result);
														exta.killComponent();
														ret.setResult(null);
													}
												});
											}
										}));
									}
								});
							}
						});
					}
				});
			}
		});
		
		return ret;
	}
	
	/**
	 *  Create a proxy for the remote platform.
	 */
	protected IFuture<IComponentIdentifier>	createProxy(IComponentManagementService local, IExternalAccess remote)
	{
		Map<String, Object>	args = new HashMap<String, Object>();
		args.put("component", remote.getComponentIdentifier());
		CreationInfo ci = new CreationInfo(args);
		return local.createComponent(null, "jadex/platform/service/remote/ProxyAgent.class", ci, null);

	}
	
	/**
	 *  Perform tests.
	 */
	protected IFuture<TestReport>	test(final IComponentManagementService cms, final boolean local)
	{
		final Future<TestReport>	ret	= new Future<TestReport>();
		
		performTests(cms, RawServiceAgent.class.getName()+".class", local ? 2000 : 1).addResultListener(new ExceptionDelegationResultListener<Void, TestReport>(ret)
		{
			public void customResultAvailable(Void result)
			{
				performTests(cms, DirectServiceAgent.class.getName()+".class", local ? 200 : 1).addResultListener(new ExceptionDelegationResultListener<Void, TestReport>(ret)
				{
					public void customResultAvailable(Void result)
					{
						performTests(cms, DecoupledServiceAgent.class.getName()+".class", local ? 100 : 1).addResultListener(new ExceptionDelegationResultListener<Void, TestReport>(ret)
						{
							public void customResultAvailable(Void result)
							{
								ret.setResult(new TestReport("#1", "test", true, null));
							}
						});
					}
				});
			}
		});
		
		return ret;
	}

	/**
	 *  Perform all tests with the given agent.
	 */
	protected IFuture<Void>	performTests(final IComponentManagementService cms, final String agentname, final int factor)
	{
		final Future<Void> ret	= new Future<Void>();
		CreationInfo	ci	= ((IService)cms).getServiceIdentifier().getProviderId().getPlatformName().equals(agent.getComponentIdentifier().getPlatformName())
			? new CreationInfo(agent.getComponentIdentifier()) : null;
		cms.createComponent(null, agentname, ci, null)
			.addResultListener(new ExceptionDelegationResultListener<IComponentIdentifier, Void>(ret)
		{
			public void customResultAvailable(final IComponentIdentifier cid)
			{
				final Future<Void>	ret2	= new Future<Void>();
				performSingleTest("raw", 5*factor).addResultListener(new DelegationResultListener<Void>(ret2)
				{
					public void customResultAvailable(Void result)
					{
						performSingleTest("direct", 2*factor).addResultListener(new DelegationResultListener<Void>(ret2)
						{
							public void customResultAvailable(Void result)
							{
								performSingleTest("decoupled", 1*factor).addResultListener(new DelegationResultListener<Void>(ret2));
							}
						});
					}
				});
				
				ret2.addResultListener(new IResultListener<Void>()
				{
					public void exceptionOccurred(Exception exception)
					{
						cms.destroyComponent(cid);
						ret.setException(exception);
					}
					
					public void resultAvailable(Void result)
					{
						cms.destroyComponent(cid).addResultListener(new ExceptionDelegationResultListener<Map<String, Object>, Void>(ret)
						{
							public void customResultAvailable(Map<String, Object> result)
							{
								ret.setResult(null);
							}
						});
					}
				});
			}
		});
		
		return ret;
	}
	
	/**
	 *  Perform a number of calls on one required service.
	 */
	protected IFuture<Void>	performSingleTest(final String servicename, final int factor)
	{
		final Future<Void> ret	= new Future<Void>();
		IFuture<IServiceCallService>	fut	= agent.getServiceContainer().getRequiredService(servicename);
		fut.addResultListener(new ExceptionDelegationResultListener<IServiceCallService, Void>(ret)
		{
			public void customResultAvailable(final IServiceCallService service)
			{
				IResultListener<Void>	lis	= new DelegationResultListener<Void>(ret)
				{
					int	count	= max*factor;
					long	start	= System.currentTimeMillis();
					
					public void customResultAvailable(Void result)
					{
						count--;
						if(count==0)
						{
							long	end	= System.currentTimeMillis();
							System.out.println(servicename+" service call on "+service+" took "+((end-start)*10000/(max*factor))/10.0+" microseconds per call ("+(max*factor)+" calls in "+(end-start)+" millis).");
							ret.setResult(null);
						}
						else
						{
							service.call().addResultListener(this);
						}
					}
				};
				service.call().addResultListener(lis);
			}
		});
		
		return ret;
	}
}
