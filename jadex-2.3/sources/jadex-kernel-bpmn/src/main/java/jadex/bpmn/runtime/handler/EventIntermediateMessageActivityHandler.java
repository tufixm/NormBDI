package jadex.bpmn.runtime.handler;

import jadex.bpmn.model.MActivity;
import jadex.bpmn.runtime.BpmnInterpreter;
import jadex.bpmn.runtime.ProcessThread;
import jadex.bridge.ComponentIdentifier;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IMessageAdapter;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.bridge.service.types.message.IMessageService;
import jadex.bridge.service.types.message.MessageType;
import jadex.commons.IFilter;
import jadex.commons.SReflect;
import jadex.commons.SUtil;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.IResultListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *  Handler for message events.
 */
public class EventIntermediateMessageActivityHandler	extends DefaultActivityHandler
{
	//-------- constants --------
	
	/** The isThrowing property name (distinguishes send/receive events). */
	public static final String	PROPERTY_THROWING = "isThrowing";	
	
	/** The type property message type identifies the meta type (e.g. fipa). */
	public static final String	PROPERTY_MESSAGETYPE = "messagetype";
	
	/** The filter property describes the filter for receiving a message. */
	public static final String	PROPERTY_FILTER	= "filter";
	
	/** The property message is the message to be sent. */
	public static final String	PROPERTY_MESSAGE = "message";
	
	/** The property message is the message to be sent. */
	public static final String	PROPERTY_CODECIDS = "codecids";
	
	//-------- methods --------
	
	/**
	 *  Execute an activity.
	 *  @param activity	The activity to execute.
	 *  @param instance	The process instance.
	 *  @param thread	The process thread.
	 */
	public void execute(final MActivity activity, final BpmnInterpreter instance, final ProcessThread thread)
	{
		boolean	send = thread.hasPropertyValue(PROPERTY_THROWING)? ((Boolean)thread.getPropertyValue(PROPERTY_THROWING)).booleanValue() : false;
				
		if(send)
		{
			sendMessage(activity, instance, thread);
		}
		else
		{
			receiveMessage(activity, instance, thread);
		}
	}
	
	/**
	 *  Send a message.
	 *  @param activity	The activity to execute.
	 *  @param instance	The process instance.
	 *  @param thread	The process thread.
	 */
	protected void sendMessage(final MActivity activity, final BpmnInterpreter instance, final ProcessThread thread)
	{
		SServiceProvider.getService(instance.getServiceContainer(), IMessageService.class, RequiredServiceInfo.SCOPE_PLATFORM)
			.addResultListener(instance.createResultListener(new DefaultResultListener()
		{
			public void resultAvailable(Object result)
			{
				final IMessageService	ms	= (IMessageService)result;
				SServiceProvider.getService(instance.getServiceContainer(), IComponentManagementService.class, RequiredServiceInfo.SCOPE_PLATFORM)
					.addResultListener(instance.createResultListener(new DefaultResultListener()
				{
					public void resultAvailable(Object result)
					{
						IComponentManagementService cms = (IComponentManagementService)result;
						String mtname = (String)thread.getPropertyValue(PROPERTY_MESSAGETYPE, activity);
						MessageType mt = mtname!=null? ms.getMessageType(mtname): ms.getMessageType("fipa");
						
						Map msg;
						
						if(thread.hasPropertyValue(PROPERTY_MESSAGE))
						{
							msg = (Map)thread.getPropertyValue(PROPERTY_MESSAGE);
						}
						else
						{
							msg = new HashMap();
						}
							
						// Convenience conversion of strings to component identifiers for receivers.
						String ri = mt.getReceiverIdentifier();
						if(thread.hasPropertyValue(ri))
						{
							Object recs = thread.getPropertyValue(ri);
							List newrecs = new ArrayList();
							if(SReflect.isIterable(recs))
							{
								for(Iterator it=SReflect.getIterator(recs); it.hasNext(); )
								{
									Object rec = it.next();
									if(rec instanceof String)
									{
//										newrecs.add(cms.createComponentIdentifier((String)rec, instance.getComponentIdentifier().getParent(), null));
										newrecs.add(new ComponentIdentifier((String)rec, instance.getComponentIdentifier().getParent()));
									}
									else
									{
										newrecs.add(rec);
									}
								}
							}
							else
							{
								if(recs instanceof String)
								{
//									newrecs.add(cms.createComponentIdentifier((String)recs, instance.getComponentIdentifier().getParent(), null));
									newrecs.add(new ComponentIdentifier((String)recs, instance.getComponentIdentifier().getParent()));
								}
								else if(recs!=null)
								{
									newrecs.add(recs);
								}
								else
								{
									throw new RuntimeException("Receiver nulls.");
								}
							}
							msg.put(ri, newrecs);
						}
						
						String[] params	= mt.getParameterNames();
						for(int i=0; params!=null && i<params.length; i++)
						{
							if(thread.hasPropertyValue(params[i]) && !params[i].equals(ri))
							{
								msg.put(params[i], thread.getPropertyValue(params[i]));
							}
						}
						
						String[] paramsets	= mt.getParameterSetNames();
						for(int i=0; paramsets!=null && i<paramsets.length; i++)
						{
							if(thread.hasPropertyValue(paramsets[i]) && !paramsets[i].equals(ri))
							{
								msg.put(paramsets[i], thread.getPropertyValue(paramsets[i]));
							}
						}
						
						// todo: implement me on gui layer
						byte[] codecids;
						String tmp = (String)thread.getPropertyValue(PROPERTY_CODECIDS);
						if(tmp!=null)
						{
							StringTokenizer stok = new StringTokenizer(tmp, ",");
							codecids = new byte[stok.countTokens()];
							for(int i=0; stok.hasMoreTokens(); i++)
								codecids[i] = Byte.parseByte(stok.nextToken());
						}
						else
						{
							codecids	= null;
						}
						
						thread.setWaiting(true);
						ms.sendMessage(msg, mt, instance.getComponentAdapter().getComponentIdentifier(), 
							instance.getModel().getResourceIdentifier(), null, codecids)
							.addResultListener(new IResultListener()
						{
							public void resultAvailable(Object result)
							{
								instance.notify(activity, thread, null);
							}
							
							public void exceptionOccurred(Exception exception)
							{
								thread.setException(exception);
								instance.notify(activity, thread, null);
							}
						});
					}
				}));
			}
		}));
	}
	
	/**
	 *  Receive a message.
	 *  @param activity	The activity to execute.
	 *  @param instance	The process instance.
	 *  @param thread	The process thread.
	 */
	protected void receiveMessage(final MActivity activity, final BpmnInterpreter instance, final ProcessThread thread)
	{
		thread.setWaiting(true);
//		thread.setWaitInfo(type);
		IFilter filter = (IFilter)thread.getPropertyValue(PROPERTY_FILTER, activity);
		if(filter==null)
		{
			filter	= new IFilter()
			{
				public boolean filter(Object obj)
				{
					boolean	ret	= obj instanceof IMessageAdapter;
					if(ret)
					{
						try
						{
							IMessageAdapter	msg	= (IMessageAdapter)obj;
							String[]	params	= activity.getPropertyNames();
							for(int i=0; ret && params!=null && i<params.length; i++)
							{
								// Todo: distinguish message activity properties and message parameters
								if(!params[i].equals("isThrowing"))
								{
									// Fetch property from message event activity, because current activity might be multiple event.
									Object propval = thread.getPropertyValue(params[i], activity);
									Object msgval = msg.getValue(params[i]);
									
									// Hack to support string component names and component identifiers
									if(propval instanceof String && msgval instanceof IComponentIdentifier)
									{
										msgval = ((IComponentIdentifier)msgval).getLocalName();
									}
									else if(msgval instanceof String && propval instanceof IComponentIdentifier)
									{
										propval = ((IComponentIdentifier)propval).getLocalName();
									}
									
									ret	= SUtil.equals(propval, msgval);
								}
							}
						}
						catch(RuntimeException e)
						{
							instance.getLogger().warning("Error during message matching: "+instance+", "+thread+", "+obj+", "+e);
							ret	= false;
						}
					}
					return ret;
				}
			};
		}
		thread.setWaitFilter(filter);
		
//		System.out.println("Waiting for message: "+filter);
	}
}
