package jadex.micro.testcases;

import jadex.base.Starter;
import jadex.base.test.Testcase;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.IResourceIdentifier;
import jadex.bridge.LocalResourceIdentifier;
import jadex.bridge.ResourceIdentifier;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.types.clock.IClockService;
import jadex.bridge.service.types.clock.ITimedObject;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.bridge.service.types.message.IMessageService;
import jadex.commons.Tuple2;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentKilled;
import jadex.micro.annotation.Argument;
import jadex.micro.annotation.Arguments;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;
import jadex.micro.annotation.Result;
import jadex.micro.annotation.Results;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Agent
@RequiredServices(
{
	@RequiredService(name="msgservice", type=IMessageService.class, 
		binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM)),
	@RequiredService(name="cms", type=IComponentManagementService.class, 
		binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM)),
	@RequiredService(name="clock", type=IClockService.class, 
		binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM))
})
//@ComponentTypes(
//	@ComponentType(name="receiver", filename="jadex/micro/testcases/stream/ReceiverAgent.class")
//)
@Arguments(
{
	@Argument(name="testcnt", clazz=int.class, defaultvalue="2")
})
@Results(@Result(name="testresults", clazz=Testcase.class))
public abstract class TestAgent
{
	@Agent
	protected MicroAgent agent;
	
	protected Set<IExternalAccess>	platforms	= new LinkedHashSet<IExternalAccess>();
	
	
	/**
	 *  Cleanup created platforms.
	 */
	@AgentKilled
	public IFuture<Void>	cleanup()
	{
		final Future<Void>	ret	= new Future<Void>();
		
		for(IExternalAccess platform: platforms)
		{
			platform.killComponent();
		}
		
		// Give platforms time to terminate.
		agent.waitForDelay(100, new IComponentStep<Void>()
		{
			public IFuture<Void> execute(IInternalAccess ia)
			{
				ret.setResult(null);
				return IFuture.DONE;
			}
		});
		
		return ret;
	}
	
	/**
	 *  The agent body.
	 */
	@AgentBody
	public IFuture<Void> body()
	{
		final Future<Void> ret = new Future<Void>();
		
		final Testcase tc = new Testcase();
		tc.setTestCount(((Integer)agent.getArgument("testcnt")).intValue());
		
		performTests(tc).addResultListener(agent.createResultListener(new IResultListener<Void>()
		{
			public void resultAvailable(Void result)
			{
//				System.out.println("tests finished: "+agent.getComponentIdentifier());

				agent.setResultValue("testresults", tc);
				ret.setResult(null);
//				agent.killAgent();				
			}
			
			public void exceptionOccurred(Exception exception)
			{
//				System.out.println("tests failed: "+agent.getComponentIdentifier());
				
				exception.printStackTrace();
				
				agent.setResultValue("testresults", tc);
				ret.setResult(null);
//				agent.killAgent();	
			}
		}));
		
		return ret;
	}
	
	/**
	 * 
	 */
	protected abstract IFuture<Void> performTests(Testcase tc);
	
	/**
	 * 
	 */
	protected IFuture<IExternalAccess> createPlatform(String[] args)
	{
		final Future<IExternalAccess> ret = new Future<IExternalAccess>();
		
		// Start platform
		String url	= "new String[]{\"../jadex-applications-micro/target/classes\"}";	// Todo: support RID for all loaded models.
//		String url	= process.getModel().getResourceIdentifier().getLocalIdentifier().getUrl().toString();
//		Starter.createPlatform(new String[]{"-platformname", "testi_1", "-libpath", url,
		String[] defargs = new String[]{"-libpath", url, "-platformname", agent.getComponentIdentifier().getPlatformPrefix()+"_*",
			"-saveonexit", "false", "-welcome", "false", "-autoshutdown", "false", "-awareness", "false",
//			"-logging", "true",
//			"-relaytransport", "false",
			"-niotcptransport", "false",	// Use tcp instead of nio to test both transports (original testcase platform uses nio)
			"-tcptransport", "true",	// Todo: make autoterminate work also with niotcp
//				"-gui", "false", "-usepass", "false", "-simulation", "false"
//			"-binarymessages", "false",
			"-gui", "false",
			"-cli", "false",
			"-simulation", "false", "-printpass", "false"};
		
		if(args!=null && args.length>0)
		{
			Map<String, String> argsmap = new HashMap<String, String>();
			for(int i=0; i<defargs.length; i++)
			{
				argsmap.put(defargs[i], defargs[++i]);
			}
			for(int i=0; i<args.length; i++)
			{
				argsmap.put(args[i], args[++i]);
			}
			defargs = new String[argsmap.size()*2];
			int i=0;
			for(String key: argsmap.keySet())
			{
				defargs[i*2]= key; 
				defargs[i*2+1] = argsmap.get(key);
				i++;
			}
		}

//		System.out.println("platform args: "+SUtil.arrayToString(defargs));
		
		Starter.createPlatform(defargs).addResultListener(agent.createResultListener(
			new DelegationResultListener<IExternalAccess>(ret)
		{
			public void customResultAvailable(IExternalAccess result)
			{
				platforms.add(result);
				super.customResultAvailable(result);
			}
		}));
		
		return ret;
	}
	
	/**
	 * 
	 */
	protected IFuture<IComponentIdentifier> createComponent(final String filename,
		final IComponentIdentifier root, final IResultListener<Collection<Tuple2<String,Object>>> reslis)
	{
		return createComponent(filename, null, null, root, reslis);
	}
	
	/**
	 * 
	 */
	protected IFuture<IComponentIdentifier> createComponent(final String filename, final Map<String, Object> args, 
		final String config, final IComponentIdentifier root, final IResultListener<Collection<Tuple2<String,Object>>> reslis)
	{
		final Future<IComponentIdentifier> ret = new Future<IComponentIdentifier>();
		
		agent.getServiceContainer().searchService(IComponentManagementService.class, RequiredServiceInfo.SCOPE_PLATFORM)
			.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, IComponentIdentifier>(ret)
		{
			public void customResultAvailable(final IComponentManagementService cms)
			{
				IResourceIdentifier	rid	= new ResourceIdentifier(
					new LocalResourceIdentifier(root, agent.getModel().getResourceIdentifier().getLocalIdentifier().getUrl()), null);
				boolean	local = root.equals(agent.getComponentIdentifier().getRoot());
				CreationInfo ci	= new CreationInfo(local? agent.getComponentIdentifier(): root, rid);
				ci.setArguments(args);
				ci.setConfiguration(config);
				cms.createComponent(null, filename, ci, reslis)
					.addResultListener(new DelegationResultListener<IComponentIdentifier>(ret)
				{
					public void customResultAvailable(IComponentIdentifier result)
					{
						super.customResultAvailable(result);
					}
					
					public void exceptionOccurred(Exception exception)
					{
						exception.printStackTrace();
						super.exceptionOccurred(exception);
					}
				}
				);
			}
		});
		
		return ret;
	}
	
	/**
	 * 
	 */
	protected IFuture<Map<String, Object>> destroyComponent(final IComponentIdentifier cid)
	{
		final Future<Map<String, Object>> ret = new Future<Map<String, Object>>();
		
		agent.getServiceContainer().searchService(IComponentManagementService.class, RequiredServiceInfo.SCOPE_PLATFORM)
			.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, Map<String, Object>>(ret)
		{
			public void customResultAvailable(final IComponentManagementService cms)
			{
				cms.destroyComponent(cid).addResultListener(new DelegationResultListener<Map<String, Object>>(ret));
			}
		});
		
		return ret;
	}
	
	/**
	 *  Setup a local test.
	 */
	protected IFuture<IComponentIdentifier>	setupLocalTest(String filename, IResultListener<Collection<Tuple2<String,Object>>> reslis)
	{
		return createComponent(filename, agent.getComponentIdentifier().getRoot(), reslis);
	}
	
	/**
	 *  Setup a remote test.
	 */
	protected IFuture<IComponentIdentifier>	setupRemoteTest(final String filename, final String config,
		final IResultListener<Collection<Tuple2<String,Object>>> reslis)
	{
		final Future<IComponentIdentifier>	ret	= new Future<IComponentIdentifier>();
		
		createPlatform(null).addResultListener(new ExceptionDelegationResultListener<IExternalAccess, IComponentIdentifier>(ret)
		{
			public void customResultAvailable(final IExternalAccess exta)
			{
				createProxy(agent.getComponentIdentifier().getRoot(), exta.getComponentIdentifier()).addResultListener(new DelegationResultListener<IComponentIdentifier>(ret)
				{
					public void customResultAvailable(IComponentIdentifier result)
					{
						// inverse proxy from remote to local.
						createProxy(exta.getComponentIdentifier(), agent.getComponentIdentifier().getRoot())
							.addResultListener(new DelegationResultListener<IComponentIdentifier>(ret)
						{
							public void customResultAvailable(IComponentIdentifier result)
							{
								createComponent(filename, null, config, exta.getComponentIdentifier(), reslis)
									.addResultListener(new DelegationResultListener<IComponentIdentifier>(ret));
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
	protected IFuture<IComponentIdentifier>	createProxy(IComponentIdentifier root, IComponentIdentifier remote)
	{
		Map<String, Object>	args = new HashMap<String, Object>();
		args.put("component", remote);
		return createComponent("jadex/platform/service/remote/ProxyAgent.class", args, null, root, null);
	}

	
	public <T> IFuture<T>	waitForRealtimeDelay(final long delay, final IComponentStep<T> step)
	{
		final Future<T>	ret	= new Future<T>();
		IFuture<IClockService>	clockfut	= agent.getServiceContainer().getRequiredService("clock");
		clockfut.addResultListener(new ExceptionDelegationResultListener<IClockService, T>(ret)
		{
			public void customResultAvailable(IClockService clock)
			{
				clock.createRealtimeTimer(delay, new ITimedObject()
				{
					public void timeEventOccurred(long currenttime)
					{
						agent.scheduleStep(step).addResultListener(new DelegationResultListener<T>(ret));
					}
				});
			}
		});
		return ret;
	}
}
