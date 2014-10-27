package jadex.bridge.service.component;

import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.IResourceIdentifier;
import jadex.bridge.modelinfo.UnparsedExpression;
import jadex.bridge.service.BasicService;
import jadex.bridge.service.IInternalService;
import jadex.bridge.service.IRequiredServiceFetcher;
import jadex.bridge.service.IService;
import jadex.bridge.service.IServiceIdentifier;
import jadex.bridge.service.RequiredServiceBinding;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.ServiceComponent;
import jadex.bridge.service.annotation.ServiceIdentifier;
import jadex.bridge.service.component.interceptors.AuthenticationInterceptor;
import jadex.bridge.service.component.interceptors.DecouplingInterceptor;
import jadex.bridge.service.component.interceptors.DecouplingReturnInterceptor;
import jadex.bridge.service.component.interceptors.DelegationInterceptor;
import jadex.bridge.service.component.interceptors.FutureFunctionality;
import jadex.bridge.service.component.interceptors.MethodInvocationInterceptor;
import jadex.bridge.service.component.interceptors.PrePostConditionInterceptor;
import jadex.bridge.service.component.interceptors.RecoveryInterceptor;
import jadex.bridge.service.component.interceptors.ResolveInterceptor;
import jadex.bridge.service.component.interceptors.ValidationInterceptor;
import jadex.bridge.service.types.factory.IComponentAdapter;
import jadex.commons.SReflect;
import jadex.commons.SUtil;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.FutureHelper;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.future.ThreadSuspendable;
import jadex.javaparser.SJavaParser;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
//import jadex.bridge.service.component.interceptors.AuthenticationInterceptor;

/**
 *  Basic service invocation interceptor.
 *  It has a multi collection of interceptors per method.
 *  Executes the list of interceptors one by one.
 *  In case no handler can be found a fallback handler is used.
 */
public class BasicServiceInvocationHandler implements InvocationHandler
{
	//-------- constants --------
	
	/** The raw proxy type (i.e. no proxy). */
	public static final String	PROXYTYPE_RAW	= "raw";
	
	/** The direct proxy type (supports custom interceptors, but uses caller thread). */
	public static final String	PROXYTYPE_DIRECT	= "direct";
	
	/** The (default) decoupled proxy type (decouples from caller thread to component thread). */
	public static final String	PROXYTYPE_DECOUPLED	= "decoupled";
	
	//-------- attributes --------

	/** The service identifier. */
	protected IServiceIdentifier sid;
	
	/** The service. */
	protected Object service;

	/** The logger for errors/warnings. */
	protected Logger logger;

	/** The realtime flag for call timeouts. */
	protected boolean realtime;

	/** The list of interceptors. */
	protected List<IServiceInvocationInterceptor> interceptors;
	
	/** The pojo service map (pojo -> proxy). */
	protected static Map<Object, IService>	pojoproxies;
	
	//-------- constructors --------
	
	/**
	 *  Create a new invocation handler.
	 */
	public BasicServiceInvocationHandler(IServiceIdentifier sid, Logger logger, boolean realtime)
	{
		this.sid = sid;
		this.logger	= logger;
		this.realtime	= realtime;
	}
	
	/**
	 *  Create a new invocation handler.
	 */
	public BasicServiceInvocationHandler(IService service, Logger logger, boolean realtime)
	{
		this.service = service;
//		this.sid = service.getServiceIdentifier();
		this.logger	= logger;
		this.realtime	= realtime;
	}
	
	/**
	 *  Create a new invocation handler.
	 */
	public BasicServiceInvocationHandler(ServiceInfo service, Logger logger, boolean realtime)
	{
		this.service = service;
//		this.sid = service.getManagementService().getServiceIdentifier();
		this.logger	= logger;
		this.realtime	= realtime;
	}
	
	//-------- methods --------
	
	/**
	 *  A proxy method has been invoked.
	 */
	public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable
	{
		Object ret = null;
		
//		if(method.getName().indexOf("ma1")!=-1)
//			System.out.println("call ma1");
		
		if((args==null || args.length==0) && "getServiceIdentifier".equals(method.getName()))
		{
			ret	= getServiceIdentifier();
		}
		else if(args!=null && args.length==1 && args[0]!=null && "equals".equals(method.getName()) && Object.class.equals(method.getParameterTypes()[0]))
		{
			Object	cmp	= Proxy.isProxyClass(args[0].getClass()) ? Proxy.getInvocationHandler(args[0]) : args[0];
			ret	= equals(cmp);
		}
		else
		{
			final ServiceInvocationContext sic = new ServiceInvocationContext(proxy, method, getInterceptors(), getServiceIdentifier().getProviderId().getRoot(), realtime);
			
			List<Object> myargs = args!=null? SUtil.arrayToList(args): null;
			
			if(SReflect.isSupertype(IFuture.class, method.getReturnType()))
			{
				final Future<Object> fret = (Future<Object>)FutureFunctionality.getDelegationFuture(method.getReturnType(), new FutureFunctionality(logger));
				ret	= fret;
//				System.out.println("fret: "+fret+" "+method);
//				fret.addResultListener(new IResultListener()
//				{
//					public void resultAvailable(Object result)
//					{
//						System.out.println("fret res: "+result);
//					}
//					public void exceptionOccurred(Exception exception)
//					{
//						System.out.println("fret ex: "+exception);
//					}
//				});
				sic.invoke(service, method, myargs).addResultListener(new ExceptionDelegationResultListener<Void, Object>(fret)
				{
					public void customResultAvailable(Void result)
					{
						FutureFunctionality.connectDelegationFuture((Future<?>)fret, (IFuture<?>)sic.getResult());
					}
				});
			}
			else if(method.getReturnType().equals(void.class))
			{
				IFuture<Void>	myvoid	= sic.invoke(service, method, myargs);
				
				// Check result and propagate exception, if any.
				// Do not throw exception as user code should not defferentiate between local and remote case.
	//			if(myvoid.isDone())
	//			{
	//				myvoid.get(null);	// throws exception, if any.
	//			}
	//			else
				{
					myvoid.addResultListener(new IResultListener<Void>()
					{
						public void resultAvailable(Void result)
						{
						}
						
						public void exceptionOccurred(Exception exception)
						{
							logger.warning("Exception in void method call: "+method+" "+getServiceIdentifier()+" "+exception);
						}
					});
				}
			}
			else
			{
				IFuture<Void> fut = sic.invoke(service, method, myargs);
				if(fut.isDone())
				{
					ret = sic.getResult();
				}
				else
				{
					// Try again after triggering delayed notifications.
					FutureHelper.notifyStackedListeners();
					if(fut.isDone())
					{
//						System.out.println("stacked method: "+method);
						ret = sic.getResult();
					}
					else
					{
						logger.warning("Warning, blocking call: "+method.getName()+" "+getServiceIdentifier());
						ret = fut.get(new ThreadSuspendable());
					}
				}
			}
		}
		
		return ret;
	}
	
	/**
	 *  Get the sid.
	 *  @return the sid.
	 */
	public IServiceIdentifier getServiceIdentifier()
	{
		if(sid==null)
		{
			sid = service instanceof ServiceInfo? ((ServiceInfo)service).getManagementService().getServiceIdentifier():
				((IService)service).getServiceIdentifier();
		}
		return sid;
	}
	
	/**
	 *  Get the service.
	 *  @return The service.
	 */
	public Object getService()
	{
		return service;
	}
	
	/**
	 *  Get the domain service.
	 *  @return The domain service.
	 */
	public Object getDomainService()
	{
		return service instanceof ServiceInfo? ((ServiceInfo)service).getDomainService(): service;
	}

	/**
	 *  Add an interceptor.
	 *  
	 *  Must be synchronized as invoke() is called from arbitrary threads.
	 */
	public synchronized void addFirstServiceInterceptor(IServiceInvocationInterceptor interceptor)
	{
		if(interceptors==null)
			interceptors = new ArrayList<IServiceInvocationInterceptor>();
		interceptors.add(0, interceptor);
	}
	
	/**
	 *  Add an interceptor.
	 *  Must be synchronized as invoke() is called from arbitrary threads.
	 */
	public synchronized void addServiceInterceptor(IServiceInvocationInterceptor interceptor, int pos)
	{
		if(interceptors==null)
			interceptors = new ArrayList<IServiceInvocationInterceptor>();
		// Hack? -1 for default position one before method invocation interceptor
		interceptors.add(pos>-1? pos: interceptors.size()-1, interceptor);
	}
	
	/**
	 *  Add an interceptor.
	 *  Must be synchronized as invoke() is called from arbitrary threads.
	 */
	public synchronized void addServiceInterceptor(IServiceInvocationInterceptor interceptor)
	{
		addServiceInterceptor(interceptor, -1);
	}
	
	/**
	 *  Remove an interceptor.
	 *  Must be synchronized as invoke() is called from arbitrary threads.
	 */
	public synchronized void removeServiceInterceptor(IServiceInvocationInterceptor interceptor)
	{
		if(interceptors!=null)
			interceptors.remove(interceptor);
	}
	
	/**
	 *  Get interceptors.
	 *  Must be synchronized as invoke() is called from arbitrary threads.
	 */
	public synchronized IServiceInvocationInterceptor[] getInterceptors()
	{
		return interceptors==null || interceptors.size()==0? null://new IServiceInvocationInterceptor[]{fallback}: 
			(IServiceInvocationInterceptor[])interceptors.toArray(new IServiceInvocationInterceptor[interceptors.size()]);
	}
	
	//-------- replacement methods for service proxies --------
	
	/**
	 *  Return the hash code.
	 */
	public int hashCode()
	{
		return 31+getServiceIdentifier().hashCode();
	}
	
	/**
	 *  Test if two objects are equal.
	 */
	public boolean equals(Object obj)
	{
		return obj instanceof BasicServiceInvocationHandler && ((BasicServiceInvocationHandler)obj).getServiceIdentifier().equals(getServiceIdentifier());
	}
	
	/**
	 *  Get a string representation.
	 */
	public String toString()
	{
		return getServiceIdentifier().toString();
	}
	
	//-------- static methods --------
	
	/**
	 *  Static method for creating a standard service proxy for a provided service.
	 */
	public static IInternalService createProvidedServiceProxy(IInternalAccess ia, IComponentAdapter adapter, Object service, 
		String name, Class<?> type, String proxytype, IServiceInvocationInterceptor[] ics, boolean copy, 
		boolean realtime, IResourceIdentifier rid)
	{
		IInternalService	ret;
		
		if(!SReflect.isSupertype(type, service.getClass()))
		{
			throw new RuntimeException("Service implementation '"+service.getClass().getName()+"' does not implement service interface: "+type.getName());
		}
		
		if(service instanceof IInternalService)
		{
			((IInternalService)service).createServiceIdentifier(name, service.getClass(), rid, type);
		}
		
		if(!PROXYTYPE_RAW.equals(proxytype) || (ics!=null && ics.length>0))
		{
			BasicServiceInvocationHandler handler = createHandler(name, ia, type, service, realtime);
			BasicServiceInvocationHandler.addInterceptors(handler, service, ics, adapter, ia, proxytype, copy);
			ret	= (IInternalService)Proxy.newProxyInstance(ia.getClassLoader(), new Class[]{IInternalService.class, type}, handler);
//			ret	= (IInternalService)Proxy.newProxyInstance(ia.getExternalAccess()
//				.getModel().getClassLoader(), new Class[]{IInternalService.class, type}, handler);
			if(!(service instanceof IService))
			{
				if(!service.getClass().isAnnotationPresent(Service.class)
					// Hack!!! BPMN uses a proxy as service implementation.
					&& !(Proxy.isProxyClass(service.getClass())
					&& Proxy.getInvocationHandler(service).getClass().isAnnotationPresent(Service.class)))
				{
					throw new RuntimeException("Pojo service must declare @Service annotation: "+service.getClass());
				}
				addPojoServiceProxy(service, ret);
			}
		}
		else
		{
			if(service instanceof IInternalService)
			{
				ret	= (IInternalService)service;
			}
			else
			{
				throw new RuntimeException("Raw services must implement IInternalService (e.g. by extending BasicService): " + service.getClass().getCanonicalName());
			}
		}
		return ret;
	}
	
	/**
	 *  Create a basic invocation handler.
	 */
	protected static BasicServiceInvocationHandler createHandler(String name, IInternalAccess ia, Class<?> type, Object service, boolean realtime)
	{
		BasicServiceInvocationHandler handler;
		if(service instanceof IService)
		{
			IService ser = (IService)service;
			handler = new BasicServiceInvocationHandler(ser, ia.getLogger(), realtime);
//			if(type==null)
//			{
//				type = ser.getServiceIdentifier().getServiceType();
//			}
//			else if(!type.equals(ser.getServiceIdentifier().getServiceType()))
//			{
//				throw new RuntimeException("Service does not match its type: "+type+", "+ser.getServiceIdentifier().getServiceType());
//			}
		}
		else
		{
			if(type==null)
			{
				// Try to find service interface via annotation
				if(service.getClass().isAnnotationPresent(Service.class))
				{
					Service si = (Service)service.getClass().getAnnotation(Service.class);
					if(!si.value().equals(Object.class))
					{
						type = si.value();
					}
				}
				// Otherwise take interface if there is only one
				else
				{
					Class<?>[] types = service.getClass().getInterfaces();
					if(types.length!=1)
						throw new RuntimeException("Unknown service interface: "+SUtil.arrayToString(types));
					type = types[0];
				}
			}
			
			BasicService mgmntservice = new BasicService(ia.getExternalAccess().getServiceProvider().getId(), type, null);
			mgmntservice.createServiceIdentifier(name, service.getClass(), ia.getModel().getResourceIdentifier(), type);
						
			Class<?> serclass = service.getClass();
			// Do not try to call isAnnotationPresent for Proxy on Android
			// see http://code.google.com/p/android/issues/detail?id=24846
			if (!(SReflect.isAndroid() && (service instanceof Proxy))) 
			{
				while(!Object.class.equals(serclass))
				{
					Field[] fields = serclass.getDeclaredFields();
					for(int i=0; i<fields.length; i++)
					{
						if(fields[i].isAnnotationPresent(ServiceIdentifier.class))
						{
							ServiceIdentifier si = (ServiceIdentifier)fields[i].getAnnotation(ServiceIdentifier.class);
							if(si.value().equals(Object.class) || si.value().equals(type))
							{
								if(SReflect.isSupertype(IServiceIdentifier.class, fields[i].getType()))
								{
									try
									{
										fields[i].setAccessible(true);
										fields[i].set(service, mgmntservice.getServiceIdentifier());
									}
									catch(Exception e)
									{
										e.printStackTrace();
									}
								}
								else
								{
									System.out.println("Field cannot store IServiceIdentifer: "+fields[i]);
								}
							}
						}
						
						if(fields[i].isAnnotationPresent(ServiceComponent.class))
						{
							Object val = null;
							if(SReflect.isSupertype(IInternalAccess.class, fields[i].getType()))
							{
								val = ia;
							}
							else if(SReflect.isSupertype(IExternalAccess.class, fields[i].getType()))
							{
								val = ia.getExternalAccess();
							}
							else
							{
								System.out.println("Field cannot store component: "+fields[i].getName()+" "+fields[i].getType());
							}
							if(val!=null)
							{
								try
								{
									fields[i].setAccessible(true);
									fields[i].set(service, val);
								}
								catch(Exception e)
								{
									e.printStackTrace();
								}
							}
						}
					}
					serclass = serclass.getSuperclass();
				}
			}
			
			ServiceInfo si = new ServiceInfo(service, mgmntservice);
			handler = new BasicServiceInvocationHandler(si, ia.getLogger(), realtime);
//			addPojoServiceIdentifier(service, mgmntservice.getServiceIdentifier());
		}
		
		return handler;
	}
	
	/**
	 *  Add the standard and custom interceptors.
	 */
	protected static void addInterceptors(BasicServiceInvocationHandler handler, Object service, 
		IServiceInvocationInterceptor[] ics, IComponentAdapter adapter, IInternalAccess ia, String proxytype, boolean copy)
	{
//		System.out.println("addI:"+service);

		// Only add standard interceptors if not raw.
		if(!PROXYTYPE_RAW.equals(proxytype))
		{
			handler.addFirstServiceInterceptor(new MethodInvocationInterceptor());
			handler.addFirstServiceInterceptor(new AuthenticationInterceptor(ia.getExternalAccess(), false));
			handler.addFirstServiceInterceptor(new PrePostConditionInterceptor());
			if(!(service instanceof IService))
			{
				handler.addFirstServiceInterceptor(new ResolveInterceptor());
			}
			handler.addFirstServiceInterceptor(new ValidationInterceptor());
			if(!PROXYTYPE_DIRECT.equals(proxytype))
			{
				handler.addFirstServiceInterceptor(new DecouplingInterceptor(ia.getExternalAccess(), adapter, copy));
			}
			handler.addFirstServiceInterceptor(new DecouplingReturnInterceptor());
		}
		
		if(ics!=null)
		{
			for(int i=0; i<ics.length; i++)
			{
				handler.addServiceInterceptor(ics[i], -1);
			}
		}
	}
	
	/**
	 *  Static method for creating a delegation service proxy for 
	 *  provided service that is not offered by the component itself.
	 */
	public static IInternalService createDelegationProvidedServiceProxy(IInternalAccess ia, IComponentAdapter adapter, IServiceIdentifier sid, 
		RequiredServiceInfo info, RequiredServiceBinding binding, ClassLoader classloader, boolean realtime)
	{
		BasicServiceInvocationHandler handler = new BasicServiceInvocationHandler(sid, adapter.getLogger(), realtime);
		handler.addFirstServiceInterceptor(new MethodInvocationInterceptor());
		handler.addFirstServiceInterceptor(new DelegationInterceptor(ia, info, binding, null, sid, realtime));
		handler.addFirstServiceInterceptor(new DecouplingReturnInterceptor(/*ea, null,*/));
//		return (IInternalService)Proxy.newProxyInstance(ea.getModel().getClassLoader(), new Class[]{IInternalService.class, sid.getServiceType()}, handler); 
		return (IInternalService)Proxy.newProxyInstance(classloader, new Class[]{IInternalService.class, info.getType().getType(classloader)}, handler); //sid.getServiceType()
	}

	/**
	 *  Static method for creating a standard service proxy for a required service.
	 */
	public static IService createRequiredServiceProxy(IInternalAccess ia, IExternalAccess ea, IComponentAdapter adapter, IService service, 
		IRequiredServiceFetcher fetcher, RequiredServiceInfo info, RequiredServiceBinding binding, boolean realtime)
	{
//		System.out.println("cRSP:"+service.getServiceIdentifier());
		IService ret = service;
		
		if(binding==null || !PROXYTYPE_RAW.equals(binding.getProxytype()))
		{
	//		System.out.println("create: "+service.getServiceIdentifier().getServiceType());
			BasicServiceInvocationHandler handler = new BasicServiceInvocationHandler(service, adapter.getLogger(), realtime);
			handler.addFirstServiceInterceptor(new MethodInvocationInterceptor());
			handler.addFirstServiceInterceptor(new AuthenticationInterceptor(ia.getExternalAccess(), true));
			if(binding!=null && binding.isRecover())
				handler.addFirstServiceInterceptor(new RecoveryInterceptor(ea, info, binding, fetcher));
			if(binding==null || PROXYTYPE_DECOUPLED.equals(binding.getProxytype())) // done on provided side
				handler.addFirstServiceInterceptor(new DecouplingReturnInterceptor());
			UnparsedExpression[] interceptors = binding!=null ? binding.getInterceptors() : null;
			if(interceptors!=null && interceptors.length>0)
			{
				for(int i=0; i<interceptors.length; i++)
				{
					IServiceInvocationInterceptor interceptor = (IServiceInvocationInterceptor)SJavaParser.evaluateExpression(
//						interceptors[i].getValue(), ea.getModel().getAllImports(), ia.getFetcher(), ea.getModel().getClassLoader());
						interceptors[i].getValue(), ea.getModel().getAllImports(), ia.getFetcher(), ia.getClassLoader());
					handler.addServiceInterceptor(interceptor);
				}
			}
//			ret = (IService)Proxy.newProxyInstance(ea.getModel().getClassLoader(), new Class[]{IService.class, 
			// service.getServiceIdentifier().getServiceType()
			ret = (IService)Proxy.newProxyInstance(ia.getClassLoader(), new Class[]{IService.class, info.getType().getType(ia.getClassLoader())}, handler); 
		}
		
		return ret;
	}
	
	/**
	 *  Add a service proxy.
	 *  @param pojo The pojo.
	 *  @param proxy The proxy.
	 */
	public static void addPojoServiceProxy(Object pojo, IService proxy)
	{
		synchronized(BasicServiceInvocationHandler.class)
		{
			if(pojoproxies==null)
			{
				pojoproxies = new IdentityHashMap<Object, IService>();
			}
			pojoproxies.put(pojo, proxy);
		}
//		System.out.println("add: "+pojosids.size());
	}
	
	/**
	 *  Remove a pojo - proxy pair.
	 *  @param sid The service identifier.
	 */
	public static void removePojoServiceProxy(IServiceIdentifier sid)
	{
		synchronized(BasicServiceInvocationHandler.class)
		{
			for(Iterator<IService> it=pojoproxies.values().iterator(); it.hasNext(); )
			{
				IService proxy = it.next();
				if(sid.equals(proxy.getServiceIdentifier()))
				{
					it.remove();
//					System.out.println("rem: "+pojosids.size());	
				}
			}
		}
	}
	
	/**
	 *  Get the proxy of a pojo service.
	 *  @param pojo The pojo service.
	 *  @return The proxy of the service.
	 */
	public static IService getPojoServiceProxy(Object pojo)
	{
		synchronized(BasicServiceInvocationHandler.class)
		{
			return (IService)pojoproxies.get(pojo);
		}
	}
	
//	/**
//	 * 
//	 */
//	public static void addPojoServiceIdentifier(Object pojo, IServiceIdentifier sid)
//	{
//		if(pojosids==null)
//		{
//			synchronized(BasicServiceInvocationHandler.class)
//			{
//				if(pojosids==null)
//				{
//					pojosids = Collections.synchronizedMap(new HashMap());
//				}
//			}
//		}
//		pojosids.put(pojo, sid);
////		System.out.println("add: "+pojosids.size());
//	}
//	
//	/**
//	 * 
//	 */
//	public static void removePojoServiceIdentifier(IServiceIdentifier sid)
//	{
//		if(pojosids!=null)
//		{
//			pojosids.values().remove(sid);
////			System.out.println("rem: "+pojosids.size());
//		}
//	}
//	
//	/**
//	 * 
//	 */
//	public static IServiceIdentifier getPojoServiceIdentifier(Object pojo)
//	{
//		return (IServiceIdentifier)pojosids.get(pojo);
//	}
}


