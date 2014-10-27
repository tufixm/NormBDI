package jadex.bridge.service.component;

import jadex.bridge.ComponentTerminatedException;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.BasicServiceContainer;
import jadex.bridge.service.IInternalService;
import jadex.bridge.service.IRequiredServiceFetcher;
import jadex.bridge.service.IService;
import jadex.bridge.service.IServiceIdentifier;
import jadex.bridge.service.IServiceProvider;
import jadex.bridge.service.ProvidedServiceInfo;
import jadex.bridge.service.PublishInfo;
import jadex.bridge.service.RequiredServiceBinding;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.component.interceptors.FutureFunctionality;
import jadex.bridge.service.component.multiinvoke.MultiServiceInvocationHandler;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.search.ServiceNotFoundException;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.bridge.service.types.factory.IComponentAdapter;
import jadex.bridge.service.types.library.ILibraryService;
import jadex.bridge.service.types.publish.IPublishService;
import jadex.bridge.service.types.remote.IRemoteServiceManagementService;
import jadex.commons.IFilter;
import jadex.commons.future.CollectionResultListener;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IIntermediateFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.future.IntermediateDelegationResultListener;
import jadex.commons.future.IntermediateFuture;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 *  Service container for active components.
 */
public class ComponentServiceContainer	extends BasicServiceContainer
{
	//-------- attributes --------
	
	/** The component adapter. */
	protected IComponentAdapter adapter;
	
	/** The internal access. */
	protected IInternalAccess instance;
	
	/** The cms. */
	protected IComponentManagementService cms;
	
	/** The component type. */
	protected String type;
	
	/** The realtime flag for call timeouts. */
	protected boolean	realtime;
	
	//-------- constructors --------

	/**
	 *  Create a new service container.
	 */
	public ComponentServiceContainer(IComponentAdapter adapter, String type, IInternalAccess instance, boolean realtime)
	{
		super(adapter.getComponentIdentifier());
		
		if(instance==null)
			throw new IllegalArgumentException("Instance must not null.");
		
		this.adapter = adapter;
		this.type	= type;
		this.instance = instance;
		this.realtime	= realtime;
	}
	
	//-------- interface methods --------
	
	/**
	 *  Get a multi service.
	 *  @param reqname The required service name.
	 *  @param multitype The interface of the multi service.
	 */
	public <T> T getMultiService(String reqname, Class<T> multitype)
	{
		return (T)Proxy.newProxyInstance(instance.getClassLoader(), new Class[]{multitype}, 
			new MultiServiceInvocationHandler(instance, reqname, multitype));
	}
	
	/**
	 *  Get a required service.
	 *  @return The service.
	 */
	public <T> IFuture<T> getRequiredService(RequiredServiceInfo info, RequiredServiceBinding binding)
	{
		return getRequiredService(info, binding, false, null);
	}
	
	/**
	 *  Get a required service.
	 *  @return The service.
	 */
	public <T> IFuture<T> getRequiredService(RequiredServiceInfo info, RequiredServiceBinding binding, boolean rebind)
	{
		return getRequiredService(info, binding, rebind, null);
	}
	
	/**
	 *  Get a required service.
	 *  @return The service.
	 */
	public <T> IFuture<T> getRequiredService(RequiredServiceInfo info, RequiredServiceBinding binding, boolean rebind, IFilter<T> filter)
	{
		if(shutdowned)
		{
			return new Future<T>(new ComponentTerminatedException(id));
		}

		if(info.getMultiplexType()!=null)
		{
			T ms = getMultiService(info.getName(), (Class<T>)info.getMultiplexType().getType(instance.getClassLoader()));
			return new Future<T>(ms);
		}
		
		IFuture<T>	fut	= super.getRequiredService(info, binding, rebind, filter);
		
		return FutureFunctionality.getDelegationFuture(fut, new ComponentFutureFunctionality(instance.getExternalAccess(), adapter));
	}
	
	/**
	 *  Get required services.
	 *  @return The services.
	 */
	public <T> IIntermediateFuture<T> getRequiredServices(RequiredServiceInfo info, RequiredServiceBinding binding)
	{
		return getRequiredServices(info, binding, false, null);
	}
	
	/**
	 *  Get required services.
	 *  @return The services.
	 */
	public <T> IIntermediateFuture<T> getRequiredServices(RequiredServiceInfo info, RequiredServiceBinding binding, boolean rebind)
	{
		return getRequiredServices(info, binding, rebind, null);
	}
	
	/**
	 *  Get required services.
	 *  @return The services.
	 */
	public <T> IIntermediateFuture<T> getRequiredServices(RequiredServiceInfo info, RequiredServiceBinding binding, boolean rebind, IFilter<T> filter)
	{
		if(shutdowned)
		{
			return new IntermediateFuture<T>(new ComponentTerminatedException(id));
		}


		IIntermediateFuture<T>	fut	= super.getRequiredServices(info, binding, rebind, filter);
		
		return (IIntermediateFuture<T>)FutureFunctionality.getDelegationFuture(fut, new ComponentFutureFunctionality(instance.getExternalAccess(), adapter));
	}
	
//	/**
//	 *  Get a service of a specific component.
//	 */
//	public <T> IFuture<T> getService(Class<T> type, IComponentIdentifier cid)
//	{
//		IFuture<T>	fut	= super.getService(type, cid);
//		return FutureFunctionality.getDelegationFuture(fut, new ComponentFutureFunctionality(instance.getExternalAccess(), adapter));
//	}
	
	/**
	 *  Get one service of a type.
	 *  @param type The class.
	 *  @return The corresponding service.
	 */
	public <T> IFuture<T> searchService(final Class<T> type)
	{
		if(shutdowned)
		{
			return new Future<T>(new ComponentTerminatedException(id));
		}

		final Future<T>	fut	= new Future<T>();
		SServiceProvider.getService(this, type).addResultListener(new DelegationResultListener(fut)
		{
			public void customResultAvailable(Object result)
			{
				if(shutdowned)
				{
					fut.setException(new ComponentTerminatedException(id));
				}
				else
				{
					fut.setResult((T)BasicServiceInvocationHandler.createRequiredServiceProxy(instance, 
						instance.getExternalAccess(), adapter, (IService)result, null, new RequiredServiceInfo(type), null, realtime));
				}
			}
		});
		
		return (IFuture<T>)FutureFunctionality.getDelegationFuture(fut, new ComponentFutureFunctionality(instance.getExternalAccess(), adapter));
	}
	
	/**
	 *  Get one service of a type.
	 *  @param type The class.
	 *  @return The corresponding service.
	 */
	public <T> IFuture<T> searchService(final Class<T> type, String scope)
	{
		if(shutdowned)
		{
			return new Future<T>(new ComponentTerminatedException(id));
		}

		final Future<T>	fut	= new Future<T>();
		SServiceProvider.getService(this, type, scope).addResultListener(new DelegationResultListener(fut)
		{
			public void customResultAvailable(Object result)
			{
				if(shutdowned)
				{
					fut.setException(new ComponentTerminatedException(id));
				}
				else
				{
					fut.setResult((T)BasicServiceInvocationHandler.createRequiredServiceProxy(instance, 
						instance.getExternalAccess(), adapter, (IService)result, null, new RequiredServiceInfo(type), null, realtime));
				}
			}
		});
		return (IFuture<T>)FutureFunctionality.getDelegationFuture(fut, new ComponentFutureFunctionality(instance.getExternalAccess(), adapter));
	}
	
	// todo: remove
	/**
	 *  Get one service of a type and only search upwards (parents).
	 *  @param type The class.
	 *  @return The corresponding service.
	 */
	public <T> IFuture<T> searchServiceUpwards(final Class<T> type)
	{
		if(shutdowned)
		{
			return new Future<T>(new ComponentTerminatedException(id));
		}

		final Future<T>	fut	= new Future<T>();
		SServiceProvider.getServiceUpwards(this, type).addResultListener(new DelegationResultListener(fut)
		{
			public void customResultAvailable(Object result)
			{
				if(shutdowned)
				{
					fut.setException(new ComponentTerminatedException(id));
				}
				else
				{
					fut.setResult((T)BasicServiceInvocationHandler.createRequiredServiceProxy(instance, 
						instance.getExternalAccess(), adapter, (IService)result, null, new RequiredServiceInfo(type), null, realtime));
				}
			}
		});
		return (IFuture<T>)FutureFunctionality.getDelegationFuture(fut, new ComponentFutureFunctionality(instance.getExternalAccess(), adapter));
	}

	/**
	 *  Get all services of a type.
	 *  @param type The class.
	 *  @return The corresponding services.
	 */
	public <T> IIntermediateFuture<T> searchServices(final Class<T> type)
	{
		if(shutdowned)
		{
			return new IntermediateFuture<T>(new ComponentTerminatedException(id));
		}

		final IntermediateFuture<T>	fut	= new IntermediateFuture<T>();
		SServiceProvider.getServices(this, type).addResultListener(new IntermediateDelegationResultListener(fut)
		{
			public void customIntermediateResultAvailable(Object result)
			{
				if(shutdowned)
				{
					fut.setException(new ComponentTerminatedException(id));
				}
				else
				{
					fut.addIntermediateResult((T)BasicServiceInvocationHandler.createRequiredServiceProxy(instance, instance.getExternalAccess(), 
						adapter, (IService)result, null, new RequiredServiceInfo(type), null, realtime));
				}
			}
		});
		return (IIntermediateFuture<T>)FutureFunctionality.getDelegationFuture(fut, new ComponentFutureFunctionality(instance.getExternalAccess(), adapter));
	}
	
	/**
	 *  Get all services of a type.
	 *  @param type The class.
	 *  @return The corresponding services.
	 */
	public <T> IIntermediateFuture<T> searchServices(final Class<T> type, String scope)
	{
		if(shutdowned)
		{
			return new IntermediateFuture<T>(new ComponentTerminatedException(id));
		}

		final IntermediateFuture<T>	fut	= new IntermediateFuture<T>();
		SServiceProvider.getServices(this, type, scope).addResultListener(new IntermediateDelegationResultListener(fut)
		{
			public void customIntermediateResultAvailable(Object result)
			{
				if(shutdowned)
				{
					fut.setException(new ComponentTerminatedException(id));
				}
				else
				{
					fut.addIntermediateResult((T)BasicServiceInvocationHandler.createRequiredServiceProxy(instance, instance.getExternalAccess(), 
						adapter, (IService)result, null, new RequiredServiceInfo(type), null, realtime));
				}
			}
		});
		return (IIntermediateFuture<T>)FutureFunctionality.getDelegationFuture(fut, new ComponentFutureFunctionality(instance.getExternalAccess(), adapter));
	}
	
	/**
	 *  Get one service of a type from a specific component.
	 *  @param type The class.
	 *  @param cid The component identifier of the target component.
	 *  @return The corresponding service.
	 */
	public <T> IFuture<T> getService(final Class<T> type, final IComponentIdentifier cid)
	{
		if(shutdowned)
		{
			return new Future<T>(new ComponentTerminatedException(id));
		}

		final Future<T> ret = new Future<T>();
		// Local?
		if(cid.getPlatformName().equals(id.getPlatformName()))
		{
			SServiceProvider.getServiceUpwards(this, IComponentManagementService.class)
				.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, T>(ret)
			{
				public void customResultAvailable(IComponentManagementService cms)
				{
					cms.getExternalAccess(cid).addResultListener(new ExceptionDelegationResultListener<IExternalAccess, T>(ret)
					{
						public void customResultAvailable(IExternalAccess ea)
						{
							SServiceProvider.getDeclaredService(ea.getServiceProvider(), type)
								.addResultListener(new DelegationResultListener<T>(ret)
							{
								public void customResultAvailable(T result)
								{
									if(shutdowned)
									{
										ret.setException(new ComponentTerminatedException(id));
									}
									else
									{
										ret.setResult((T)BasicServiceInvocationHandler.createRequiredServiceProxy(instance, instance.getExternalAccess(), 
											adapter, (IService)result, null, new RequiredServiceInfo(type), null, realtime));
									}
								}
							});
						}
					});
				}
			});
		}
		else
		{
			SServiceProvider.getService(this, IRemoteServiceManagementService.class, RequiredServiceInfo.SCOPE_PLATFORM)
				.addResultListener(new ExceptionDelegationResultListener<IRemoteServiceManagementService, T>(ret)
			{
				public void customResultAvailable(IRemoteServiceManagementService rms)
				{
					rms.getServiceProxy(cid, type, RequiredServiceInfo.SCOPE_LOCAL)
						.addResultListener(new DelegationResultListener<T>(ret)
					{
						public void customResultAvailable(T result)
						{
							if(shutdowned)
							{
								ret.setException(new ComponentTerminatedException(id));
							}
							else
							{
								ret.setResult((T)BasicServiceInvocationHandler.createRequiredServiceProxy(instance, instance.getExternalAccess(), 
									adapter, (IService)result, null, new RequiredServiceInfo(type), null, realtime));
							}
						}
					});
				}
			});
		}
		return ret;
	}
	
	/**
	 *  Get the parent service container.
	 *  @return The parent container.
	 */
	public IFuture<IServiceProvider>	getParent()
	{
		if(shutdowned)
		{
			return new Future<IServiceProvider>(new ComponentTerminatedException(id));
		}

		final Future<IServiceProvider> ret = new Future<IServiceProvider>();
		
		ret.setResult(adapter!=null && adapter.getParent()!=null ? adapter.getParent().getServiceProvider() : null);
		
		return ret;
	}
	
	/**
	 *  Get the children service containers.
	 *  @return The children containers.
	 */
	public IFuture<Collection<IServiceProvider>> getChildren()
	{
		if(shutdowned)
		{
			return new Future<Collection<IServiceProvider>>(new ComponentTerminatedException(id));
		}

		final Future<Collection<IServiceProvider>> ret = new Future<Collection<IServiceProvider>>();
//		ComponentFuture ret = new ComponentFuture(ea, adapter, oldret);
		
		if(cms!=null)
		{
			cms.getChildren(adapter.getComponentIdentifier())
				.addResultListener(new IResultListener<IComponentIdentifier[]>()
			{
				public void resultAvailable(IComponentIdentifier[] children)
				{
					if(children!=null)
					{
	//					System.out.println("childs: "+adapter.getComponentIdentifier()+" "+SUtil.arrayToString(childs));
						final IResultListener<IServiceProvider> lis = new CollectionResultListener<IServiceProvider>(
							children.length, true, new DelegationResultListener<Collection<IServiceProvider>>(ret));
						for(int i=0; i<children.length; i++)
						{
							if(cms!=null)
							{
								cms.getExternalAccess(children[i]).addResultListener(new IResultListener<IExternalAccess>()
								{
									public void resultAvailable(IExternalAccess exta)
									{
										lis.resultAvailable(exta.getServiceProvider());
									}
									
									public void exceptionOccurred(Exception exception)
									{
										lis.exceptionOccurred(exception);
									}
								});
							}
							else
							{
								lis.exceptionOccurred(new ComponentTerminatedException(id));
							}
						}
					}
					else
					{
						List<IServiceProvider>	res	= Collections.emptyList();
						ret.setResult(res);
					}
				}
				public void exceptionOccurred(Exception exception)
				{
					ret.setException(exception);
				}
			});
		}
		else
		{
			ret.setException(new ComponentTerminatedException(id));			
		}
		
		return ret;
	}
	
	/**
	 *  Create a service fetcher.
	 */
	public IRequiredServiceFetcher createServiceFetcher(String name)
	{
		if(shutdowned)
		{
			throw new ComponentTerminatedException(id);
		}
			
		return new DefaultServiceFetcher(this, instance, realtime);
	}
	
	/**
	 *  Get the type of the service provider (e.g. enclosing component type).
	 *  @return The type of this provider.
	 */
	public String	getType()
	{
		return type;
	}	
	
	/**
	 *  Start the service.
	 *  @return A future that is done when the service has completed starting.  
	 */
	public IFuture<Void> start()
	{
		final Future<Void> ret = new Future<Void>();
		
//		System.out.println("search clock: "+getId());
		SServiceProvider.getServiceUpwards(ComponentServiceContainer.this, IComponentManagementService.class)
			.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, Void>(ret)
		{
			public void customResultAvailable(IComponentManagementService result)
			{
				cms = result;
//				System.out.println("Has cms: "+getId()+" "+cms);
				
				// Services may need other services and thus need to be able to search
				// the container.
				ComponentServiceContainer.super.start().addResultListener(new DelegationResultListener<Void>(ret));
			}
		});
		
		return ret;
	}
	
	/**
	 *  Shutdown the container.
	 */
	public IFuture<Void> shutdown()
	{
		Future<Void>	ret	= new Future<Void>();
		super.shutdown().addResultListener(new DelegationResultListener<Void>(ret)
		{
			public void customResultAvailable(Void result)
			{
				adapter	= null;
				cms	= null;
				instance	= null;
				super.customResultAvailable(result);
			}
		});
		return ret;
	}
	
	/**
	 *  Called after a service has been started.
	 */
	public IFuture<Void> serviceStarted(final IInternalService service)
	{
		final Future<Void> ret = new Future<Void>();
		ProvidedServiceInfo info = getProvidedServiceInfo(service.getServiceIdentifier());
		final PublishInfo pi = info==null? null: info.getPublish();
		if(pi!=null)
		{
			getPublishService(instance, pi.getPublishType(), (Iterator<IPublishService>)null)
				.addResultListener(instance.createResultListener(new ExceptionDelegationResultListener<IPublishService, Void>(ret)
			{
				public void customResultAvailable(IPublishService ps)
				{
					ps.publishService(instance.getClassLoader(), service, pi)
						.addResultListener(instance.createResultListener(new DelegationResultListener<Void>(ret)));
				}
				public void exceptionOccurred(Exception exception)
				{
//					exception.printStackTrace();
					instance.getLogger().severe("Could not publish: "+service.getServiceIdentifier()+" "+exception.getMessage());
					ret.setResult(null);
				}
			}));
		}
		else
		{
			ret.setResult(null);
		}
		return ret;
	}
	
	/**
	 *  Called after a service has been shutdowned.
	 */
	public IFuture<Void> serviceShutdowned(final IInternalService service)
	{
		final Future<Void> ret = new Future<Void>();
//		adapter.invokeLater(new Runnable()
//		{
//			public void run()
//			{
				ProvidedServiceInfo info = getProvidedServiceInfo(service.getServiceIdentifier());
				final PublishInfo pi = info==null? null: info.getPublish();
//				System.out.println("shutdown ser: "+service.getServiceIdentifier());
				if(pi!=null)
				{
					final IServiceIdentifier sid = service.getServiceIdentifier();
//					getPublishService(instance, pi.getPublishType(), null).addResultListener(instance.createResultListener(new IResultListener<IPublishService>()
					getPublishService(instance, pi.getPublishType(), null).addResultListener(new IResultListener<IPublishService>()
					{
						public void resultAvailable(IPublishService ps)
						{
							ps.unpublishService(sid).addResultListener(new DelegationResultListener<Void>(ret));
						}
						
						public void exceptionOccurred(Exception exception)
						{
			//				instance.getLogger().severe("Could not unpublish: "+sid+" "+exception.getMessage());
							
							// ignore, if no publish info
							ret.setResult(null);
							// todo: what if publish info but no publish service?
						}
					});
				}
				else
				{
					ret.setResult(null);
				}				
//			}
//		});
		return ret;
	}
	
	/**
	 *  Get the publish service for a publish type (e.g. web service).
	 *  @param type The type.
	 *  @param services The iterator of publish services (can be null).
	 *  @return The publish service.
	 */
	public static IFuture<IPublishService> getPublishService(final IInternalAccess instance, final String type, final Iterator<IPublishService> services)
	{
		final Future<IPublishService> ret = new Future<IPublishService>();
		
		if(services==null)
		{
			IFuture<Collection<IPublishService>> fut = SServiceProvider.getServices(instance.getServiceContainer(), IPublishService.class, RequiredServiceInfo.SCOPE_PLATFORM);
			fut.addResultListener(instance.createResultListener(new ExceptionDelegationResultListener<Collection<IPublishService>, IPublishService>(ret)
			{
				public void customResultAvailable(Collection<IPublishService> result)
				{
					getPublishService(instance, type, result.iterator()).addResultListener(new DelegationResultListener<IPublishService>(ret));
				}
			}));
		}
		else
		{
			if(services.hasNext())
			{
				final IPublishService ps = (IPublishService)services.next();
				ps.isSupported(type).addResultListener(instance.createResultListener(new ExceptionDelegationResultListener<Boolean, IPublishService>(ret)
				{
					public void customResultAvailable(Boolean supported)
					{
						if(supported.booleanValue())
						{
							ret.setResult(ps);
						}
						else
						{
							getPublishService(instance, type, services).addResultListener(new DelegationResultListener<IPublishService>(ret));
						}
					}
				}));
			}
			else
			{
//				ret.setResult(null);
				ret.setException(new ServiceNotFoundException("IPublishService not found."));
			}
		}
		
		return ret;
	}
	
	/**
	 * 
	 */
	public IFuture<Class<?>> getServiceType(final IServiceIdentifier sid)
	{
		if(shutdowned)
		{
			return new Future<Class<?>>(new ComponentTerminatedException(id));
		}

		final Future<Class<?>> ret = new Future<Class<?>>();
		if(sid.getServiceType().getType(instance.getClassLoader())!=null)
		{
			ret.setResult(sid.getServiceType().getType(instance.getClassLoader())); // todo: only local? remote would cause nullpointer
		}
		else
		{
			SServiceProvider.getService(instance.getServiceContainer(), ILibraryService.class, RequiredServiceInfo.SCOPE_PLATFORM)
				.addResultListener(new ExceptionDelegationResultListener<ILibraryService, Class<?>>(ret)
			{
				public void customResultAvailable(ILibraryService ls)
				{
					ls.getClassLoader(sid.getResourceIdentifier())
						.addResultListener(new ExceptionDelegationResultListener<ClassLoader, Class<?>>(ret)
					{
						public void customResultAvailable(ClassLoader cl)
						{
							ret.setResult(sid.getServiceType().getType(cl));
						}
					});
	//				ls.loadClass(sid.getServiceTypeId()).addResultListener(new DelegationResultListener<Class>(ret));
				}
			});
		}
		return ret;
	}
	
	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public String toString()
	{
		return "ComponentServiceContainer(name="+getId()+")";
	}
	
	/**
	 *  Get the logger.
	 */
	protected Logger getLogger()
	{
		return adapter.getLogger();
	}
}
