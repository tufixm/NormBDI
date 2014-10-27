package jadex.platform.service.awareness.discovery;

import jadex.bridge.ComponentTerminatedException;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.awareness.AwarenessInfo;
import jadex.bridge.service.types.awareness.IAwarenessManagementService;
import jadex.bridge.service.types.awareness.IDiscoveryService;
import jadex.bridge.service.types.message.ICodec;
import jadex.bridge.service.types.message.IMessageService;
import jadex.bridge.service.types.threadpool.IDaemonThreadPoolService;
import jadex.commons.SUtil;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentArgument;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.AgentKilled;
import jadex.micro.annotation.Argument;
import jadex.micro.annotation.Arguments;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;
import jadex.platform.service.message.MapSendTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

/**
 *  Base class for different kinds of discovery agents.
 */
@Agent
@Arguments({
	@Argument(name="delay", clazz=long.class, defaultvalue="10000", description="The delay between sending awareness infos (in milliseconds)."),
	@Argument(name="fast", clazz=boolean.class, defaultvalue="true", description="Flag for enabling fast startup awareness (pingpong send behavior)."),
	@Argument(name="includes", clazz=String[].class, description="A list of platforms/IPs/hostnames to include. Matches start of platform/IP/hostname."),
	@Argument(name="excludes", clazz=String[].class, description="A list of platforms/IPs/hostnames to exclude. Matches start of platform/IP/hostname.")
})
/*@Configurations(
{
	@Configuration(name="Frequent updates (10s)", arguments=@NameValue(name="delay", value="10000")),
	@Configuration(name="Medium updates (20s)", arguments=@NameValue(name="delay", value="20000")),
	@Configuration(name="Seldom updates (60s)", arguments=@NameValue(name="delay", value="60000"))
})*/
@ProvidedServices(
	@ProvidedService(type=IDiscoveryService.class, implementation=@Implementation(DiscoveryService.class))
)
@RequiredServices(
{
	@RequiredService(name="ms", type=IMessageService.class, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM)),
	@RequiredService(name="threadpool", type=IDaemonThreadPoolService.class, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM)),
	@RequiredService(name="management", type=IAwarenessManagementService.class, binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM))
})
public abstract class DiscoveryAgent
{
	//-------- attributes --------
	
	/** The agent. */
	@Agent
	protected MicroAgent agent;
	
	/** The send (remotes) delay. */
	@AgentArgument
	protected long delay;
	
	/** Flag for enabling fast startup awareness (pingpong send behavior). */
	@AgentArgument
	protected boolean fast;
	
	/** The includes list. */
	@AgentArgument
	protected String[] includes;
	
	/** The excludes list. */
	@AgentArgument
	protected String[] excludes;

	/** Flag indicating that the agent is started and the send behavior may be activated. */
	protected boolean started;

	/** Flag indicating agent killed. */
	protected boolean killed;

	/** The timer. */
	protected Timer	timer;

	/** The root component id. */
	protected IComponentIdentifier root;
	
	
	/** The send handler. */
	protected SendHandler sender;
	
	/** The receive handler. */
	protected ReceiveHandler receiver;
	
	/** Flag indicating that the agent has received its own discovery info. */
	protected boolean received_self;
	
//	/** The classloader. */
//	protected ClassLoader classloader;
	
	/** The default codecs. */
	protected ICodec[] defaultcodecs;
	
	/** The map of all codecs. */
	protected Map<Byte, ICodec> allcodecs;

	//-------- methods --------
	
	@AgentCreated
	public IFuture<Void> agentCreated()
	{
		final Future<Void> ret = new Future<Void>();
	
//		System.out.println("fast: "+fast);
		
//		System.out.println(agent.getComponentIdentifier()+" includes: "+SUtil.arrayToString(includes));
//		System.out.println(agent.getComponentIdentifier()+" excludes: "+SUtil.arrayToString(excludes));
		
//		System.out.println(getMicroAgent().getChildrenIdentifiers()+" delay: "+delay);
		
		SServiceProvider.getServiceUpwards(agent.getServiceProvider(), IMessageService.class)
			.addResultListener(agent.createResultListener(new ExceptionDelegationResultListener<IMessageService, Void>(ret)
		{
			public void customResultAvailable(final IMessageService msgser)
			{
				msgser.getDefaultCodecs().addResultListener(agent.createResultListener(new ExceptionDelegationResultListener<ICodec[], Void>(ret)
				{
					public void customResultAvailable(ICodec[] result)
					{
						defaultcodecs = result;
						msgser.getAllCodecs().addResultListener(agent.createResultListener(new ExceptionDelegationResultListener<Map<Byte, ICodec>, Void>(ret)
						{
							public void customResultAvailable(Map<Byte, ICodec> result)
							{
								allcodecs = result;
							}
						}));
					}
				}));
				ret.setResult(null);
			}	
		}));
		
		return ret;
	}
	
	/**
	 *  Execute the functional body of the agent.
	 *  Is only called once.
	 */
	@AgentBody
	public void executeBody()
	{
		// Wait before starting send behavior to not miss fast awareness pingpong replies,
		// because receiver thread is not yet running. (hack???)
		
		this.sender = createSendHandler();
		this.receiver = createReceiveHandler();
		if(receiver!=null)
		{
			receiver.startReceiving().addResultListener(getMicroAgent()
				.createResultListener(new IResultListener<Void>()
			{
				public void resultAvailable(Void result)
				{
					setStarted(true);
					if(sender!=null)
					{
						sender.startSendBehavior();
					}
				}
				
				public void exceptionOccurred(Exception exception)
				{
					// Send also when receiving does not work?
					setStarted(true);
					if(sender!=null)
					{
						sender.startSendBehavior();
					}
				}
			}));
		}
		else
		{
			setStarted(true);			
			if(sender!=null)
			{
				sender.startSendBehavior();
			}
		}
	}
	
	/**
	 *  Called just before the agent is removed from the platform.
	 *  @return The result of the component.
	 */
	@AgentKilled
	public IFuture<Void> agentKilled()
	{
		final Future<Void> ret = new Future<Void>();
		setKilled(true);
		
		if(sender!=null)
		{
			createAwarenessInfo(AwarenessInfo.STATE_OFFLINE, createMasterId())
				.addResultListener(agent.createResultListener(new ExceptionDelegationResultListener<AwarenessInfo, Void>(ret)
			{
				public void customResultAvailable(AwarenessInfo info)
				{
					sender.send(info);
					terminateNetworkRessource();
					ret.setResult(null);
				}
			}));
		}
		else
		{
			ret.setResult(null);
		}
		
//		System.out.println("killed set to true: "+getComponentIdentifier());
		
		return ret;
	}
	
	/**
	 *  Create the master id.
	 */
	protected String createMasterId()
	{
		return null;
	}
	
	/**
	 *  Create the send handler.
	 */
	public abstract SendHandler createSendHandler();
	
	/**
	 *  Create the receive handler.
	 */
	public abstract ReceiveHandler createReceiveHandler();
	
	/**
	 *  Get the includes.
	 *  @return the includes.
	 */
	public String[] getIncludes()
	{
		return includes;
	}

	/**
	 *  Set the includes.
	 *  @param includes The includes.
	 */
	public void setIncludes(String[] includes)
	{
		this.includes = includes.clone();
	}
	
	/**
	 *  Get the excludes.
	 *  @return the excludes.
	 */
	public String[] getExcludes()
	{
		return excludes;
	}
	
	/**
	 *  Set the excludes.
	 *  @param excludes The excludes.
	 */
	public void setExcludes(String[] excludes)
	{
		this.excludes = excludes.clone();
	}

	/**
	 *  Get the started.
	 *  @return the started.
	 */
	public boolean isStarted()
	{
		return started;
	}

	/**
	 *  Set the started.
	 *  @param started The started to set.
	 */
	public void setStarted(boolean started)
	{
		this.started = started;
	}

	/**
	 *  Get the killed.
	 *  @return the killed.
	 */
	public boolean isKilled()
	{
		return killed;
	}

	/**
	 *  Set the killed.
	 *  @param killed The killed to set.
	 */
	public void setKilled(boolean killed)
	{
		this.killed = killed;
	}

	/**
	 *  Get the timer.
	 *  @return the timer.
	 */
	public Timer getTimer()
	{
		return timer;
	}

	/**
	 *  Set the timer.
	 *  @param timer The timer to set.
	 */
	public void setTimer(Timer timer)
	{
		this.timer = timer;
	}

	/**
	 *  Get the root.
	 *  @return the root.
	 */
	public IComponentIdentifier getRoot()
	{
		if(root==null)
			this.root = agent.getComponentIdentifier().getRoot();
		return root;
	}
	
	/**
	 *  Set the root.
	 *  @param root The root to set.
	 */
	public void setRoot(IComponentIdentifier root)
	{
		this.root = root;
	}
	
//	/**
//	 *  Get the access.
//	 *  @return the access.
//	 */
//	public IExternalAccess getExternalAccess()
//	{
//		return access;
//	}
	
	/**
	 *  Get the delay.
	 *  @return the delay.
	 */
	public long getDelay()
	{
		return delay;
	}

	/**
	 *  Set the delay.
	 *  @param delay The delay to set.
	 */
	public void setDelay(long delay)
	{
//		System.out.println("setDelay: "+delay+" "+getComponentIdentifier());
//		if(this.delay>=0 && delay>0)
//			scheduleStep(send);
		if(getDelay()!=delay)
		{
			this.delay = delay;
			if(sender!=null)
			{
				sender.startSendBehavior();
			}
		}
	}

	/**
	 *  Set the fast startup awareness flag
	 */
	public void setFast(boolean fast)
	{
		this.fast = fast;
	}
	
	/**
	 *  Get the fast startup awareness flag.
	 *  @return The fast flag.
	 */
	public boolean isFast()
	{
		return this.fast;
	}
	
	/**
	 *  Get the current time.
	 */
	public long getClockTime()
	{
//		return clock.getTime();
		return System.currentTimeMillis();
	}
	
	/**
	 *  Overriden wait for to not use platform clock.
	 */
	public void	doWaitFor(long delay, final IComponentStep<?> step)
	{
//		waitFor(delay, step);
		
		if(timer==null)
			timer	= new Timer(true);
		
		timer.schedule(new TimerTask()
		{
			public void run()
			{
				try
				{
					agent.scheduleStep(step);
				}
				catch(ComponentTerminatedException e)
				{
					// ignore
				}
			}
		}, delay);
	}
	
	/**
	 *  Encode an object.
	 *  @param object The object.
	 *  @return The byte array.
	 */
	public static byte[] encodeObject(Object object, ICodec[] codecs, ClassLoader classloader)
	{
		return MapSendTask.encodeMessage(object, codecs, classloader);
//		return GZIPCodec.encodeBytes(JavaWriter.objectToByteArray(object, 
//			classloader), classloader);
	}
	
	/**
	 *  Decode an object.
	 *  @param data The byte array.
	 *  @return The object.
	 */
	public static Object decodeObject(byte[] data, Map<Byte, ICodec> codecs, ClassLoader classloader)
	{
//		System.out.println("size: "+data.length);
		return MapSendTask.decodeMessage(data, codecs, classloader);
//		return JavaReader.objectFromByteArray(GZIPCodec.decodeBytes(data, 
//			classloader), classloader);
//		return Reader.objectFromByteArray(reader, GZIPCodec.decodeBytes(data, 
//			classloader), classloader);
	}
	
//	/**
//	 *  Decode a datagram packet.
//	 *  @param sent The byte array.
//	 *  @return The object.
//	 */
//	public static Object decodePacket(DatagramPacket pack, ClassLoader classloader)
//	{
//		byte[] data = new byte[pack.getLength()];
//		System.arraycopy(pack.getData(), 0, data, 0, pack.getLength());
//		return decodeObject(data, classloader);
//	}
	
	/**
	 *  Get the allcodecs.
	 *  @return the allcodecs.
	 */
	public Map<Byte, ICodec> getAllCodecs()
	{
		return allcodecs;
	}
	
	/**
	 *  Get the defaultcodecs.
	 *  @return the defaultcodecs.
	 */
	public ICodec[] getDefaultCodecs()
	{
		return defaultcodecs;
	}

	/**
	 *  Create awareness info of myself.
	 */
	public IFuture<AwarenessInfo> createAwarenessInfo()
	{
		final Future<AwarenessInfo> ret = new Future<AwarenessInfo>();
		IFuture<IMessageService> fut = agent.getServiceContainer().getRequiredService("ms");
		fut.addResultListener(agent.createResultListener(new ExceptionDelegationResultListener<IMessageService, AwarenessInfo>(ret)
		{
			public void customResultAvailable(IMessageService cms)
			{
				cms.updateComponentIdentifier(getRoot()).addResultListener(agent.createResultListener(
					new ExceptionDelegationResultListener<IComponentIdentifier, AwarenessInfo>(ret)
				{
					public void customResultAvailable(IComponentIdentifier root)
					{
						AwarenessInfo info = new AwarenessInfo(root, AwarenessInfo.STATE_ONLINE, getDelay(), getIncludes(), getExcludes(), null);
						ret.setResult(info);
					}
				}));
			}
		}));
		return ret;
	}
	
	/**
	 *  Create awareness info of myself.
	 */
	public IFuture<AwarenessInfo> createAwarenessInfo(final String state, final String masterid)
	{
		final Future<AwarenessInfo> ret = new Future<AwarenessInfo>();
		IFuture<IMessageService> fut = agent.getServiceContainer().getRequiredService("ms");
		fut.addResultListener(agent.createResultListener(new ExceptionDelegationResultListener<IMessageService, AwarenessInfo>(ret)
		{
			public void customResultAvailable(IMessageService cms)
			{
				cms.updateComponentIdentifier(getRoot()).addResultListener(agent.createResultListener(
					new ExceptionDelegationResultListener<IComponentIdentifier, AwarenessInfo>(ret)
				{
					public void customResultAvailable(IComponentIdentifier root)
					{
						AwarenessInfo info = new AwarenessInfo(root, state, getDelay(), getIncludes(), getExcludes(), masterid);
						ret.setResult(info);
					}
				}));
			}
		}));
		return ret;
	}

	/**
	 *  Get the agent.
	 *  @return the agent.
	 */
	public MicroAgent getMicroAgent()
	{
		return agent;
	}
	
	/**
	 *  Get the sender.
	 *  @return the sender.
	 */
	public SendHandler getSender()
	{
		return sender;
	}

	/**
	 *  Get the receiver.
	 *  @return the receiver.
	 */
	public ReceiveHandler getReceiver()
	{
		return receiver;
	}

//	/**
//	 *  Get the classloader.
//	 *  @return the classloader.
//	 */
//	public ClassLoader getMyClassLoader()
//	{
//		return classloader;
//	}

	/**
	 *  (Re)init sending/receiving ressource.
	 */
	protected abstract void initNetworkRessource();
	
	/**
	 *  Terminate sending/receiving ressource.
	 */
	protected abstract void terminateNetworkRessource();
}
