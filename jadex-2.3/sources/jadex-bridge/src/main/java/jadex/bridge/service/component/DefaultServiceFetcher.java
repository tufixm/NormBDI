package jadex.bridge.service.component;

import jadex.bridge.ComponentCreationException;
import jadex.bridge.ComponentIdentifier;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.modelinfo.ComponentInstanceInfo;
import jadex.bridge.modelinfo.UnparsedExpression;
import jadex.bridge.service.IRequiredServiceFetcher;
import jadex.bridge.service.IService;
import jadex.bridge.service.IServiceProvider;
import jadex.bridge.service.RequiredServiceBinding;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.search.ServiceNotFoundException;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.bridge.service.types.factory.IComponentAdapter;
import jadex.commons.IFilter;
import jadex.commons.IValueFetcher;
import jadex.commons.future.CollectionResultListener;
import jadex.commons.future.CounterResultListener;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.FutureFinishChecker;
import jadex.commons.future.IFuture;
import jadex.commons.future.IIntermediateFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.future.IntermediateDelegationResultListener;
import jadex.commons.future.IntermediateFuture;
import jadex.javaparser.SJavaParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *  The default service fetcher realizes the default 
 *  strategy for fetching a required service.
 *  Allows for:
 *  
 *  - binding by searching service(s)
 *  - binding by name
 *  - binding by type
 *  
 *  - dynamic or static binding
 *  - creation of components
 *  
 *  - recovery of failed services cannot be done here because failure occurs at time of service call
 */
public class DefaultServiceFetcher implements IRequiredServiceFetcher
{
	//-------- attributes --------
	
	/** The provider. */
	protected IServiceProvider provider;
	
	/** The internal access. */
	protected IInternalAccess	ia;
	
	/** The external access. */
	protected IExternalAccess	access;

	
	/** The result. */
	protected Object result;
	
	/** The realtime flag for call timeouts. */
	protected boolean	realtime;
	
	//-------- constructors --------
	
	/**
	 *  Create a new required service fetcher.
	 */
	public DefaultServiceFetcher(IServiceProvider provider, IInternalAccess ia, boolean realtime)
	{
		this.ia	= ia;
		this.access	= ia.getExternalAccess();
		this.provider = provider;
		this.realtime	= realtime;
	}
	
	//-------- methods --------

	/**
	 *  Get a required service.
	 */
	public <T> IFuture<T> getService(final RequiredServiceInfo info, RequiredServiceBinding bd, final boolean rebind, final IFilter<T> filter)
	{
		// Hack!!! Only works for local infos, but DefaultServiceFetcher only used internally!?
		final Class<T> type = (Class<T>)info.getType().getType(ia.getClassLoader());
		
//		System.out.println(info.getType().getTypeName().toString());
//		if(info.getType().getTypeName().indexOf("IDis")!=-1)
//			System.out.println("diss" );
		
		final Future<T> ret = new Future<T>();
		final RequiredServiceBinding binding = bd!=null? bd: info.getDefaultBinding();
		
		if(rebind)
			result = null;
		
		checkResult(result).addResultListener(new ExceptionDelegationResultListener<Object, T>(ret)
		{
			public void customResultAvailable(Object result)
			{
				// Test if already bound.
				if(result==null)
				{
					// Search component.
					if(binding.getComponentName()!=null)
					{
//						System.out.println("searching: "+binding.getComponentName());
						// Search service by component name.
						getExternalAccessByName(provider, info, binding).addResultListener(new ExceptionDelegationResultListener<IExternalAccess, T>(ret)
						{
							public void customResultAvailable(IExternalAccess ea)
							{
								IFuture<T> fut = SServiceProvider.getService(ea.getServiceProvider(), type, RequiredServiceInfo.SCOPE_LOCAL, filter);
								fut.addResultListener(new StoreDelegationResultListener<T>(ret, provider, info, binding));
							}
//							public void exceptionOccurred(Exception exception)
//							{
//								System.out.println("not found: "+binding.getComponentName()+" "+exception);
//								super.exceptionOccurred(exception);
//							}
						});
					}
					else if(binding.getComponentType()!=null)
					{
						// Search service by component type.
						getExternalAccessesByType(provider, info, binding).addResultListener(new ExceptionDelegationResultListener<Collection<IExternalAccess>, T>(ret)
						{
							public void customResultAvailable(Collection<IExternalAccess> coll)
							{
								if(coll!=null && coll.size()>0)
								{
									IExternalAccess ea = coll.iterator().next();
									SServiceProvider.getService(ea.getServiceProvider(), type, RequiredServiceInfo.SCOPE_LOCAL, filter)
										.addResultListener(new StoreDelegationResultListener<T>(ret, provider, info, binding));
								}
								else
								{
									ret.setException(new RuntimeException("No component found."));
								}
							}
						});
					}
					else
					{
						// Search service using search specification.
						SServiceProvider.getService(provider, type, binding.getScope(), filter)
							.addResultListener(new StoreDelegationResultListener<T>(ret, provider, info, binding)
						{
//							public void customResultAvailable(Object result)
//							{
//								System.out.println("type ra: "+type);
//								if(info.getType().getTypeName().indexOf("ICro")!=-1)
//									System.out.println("diss" );
//								super.customResultAvailable(result);
//							}
							public void exceptionOccurred(Exception exception)
							{
//								System.out.println("type ex: "+type);
//								if(info.getType().getTypeName().indexOf("ICro")!=-1)
//									System.out.println("diss" );
								createComponent(provider, info, binding).addResultListener(
									new ExceptionDelegationResultListener<IExternalAccess, T>(ret)
								{
									public void customResultAvailable(IExternalAccess ea)
									{
										SServiceProvider.getService(ea.getServiceProvider(), type, RequiredServiceInfo.SCOPE_LOCAL, filter)
											.addResultListener(new StoreDelegationResultListener<T>(ret, provider, info, binding));
									}
								});
							}
						});
					}
				}
				else
				{
					ret.setResult((T)result);
				}
			}
		});
		
		return ret;
	}
	
	/**
	 *  Get a required multi service.
	 *  
	 *  todo: should also create component(s) when no service could be found
	 */
	public <T> IIntermediateFuture<T> getServices(final RequiredServiceInfo info, 
		final RequiredServiceBinding bd, boolean rebind, final IFilter<T> filter)
	{
		// Hack!!! Only works for local infos, but DefaultServiceFetcher only used internal!?
		final Class<T> type = (Class<T>)info.getType().getType(ia.getClassLoader());
		
		final IntermediateFuture<T> ret = new IntermediateFuture<T>();
		final RequiredServiceBinding binding = bd!=null? bd: info.getDefaultBinding();
		
		if(rebind)
			result = null;
		
		checkResults((List<T>)result).addResultListener(new IntermediateDelegationResultListener<T>(ret)
		{
		    public void finished()
		    {
		    	if(ret.getIntermediateResults().size()!=0)
		    	{	
		    		super.finished();
		    	}
		    	else
		    	{
			    	// Search component.
					if(binding.getComponentName()!=null)
					{
						// Search service by component name.
						IFuture<IExternalAccess> fut = getExternalAccessByName(provider, info, binding);
						fut.addResultListener(new ExceptionDelegationResultListener<IExternalAccess, Collection<T>>(ret)
						{
							public void customResultAvailable(IExternalAccess ea)
							{
								IFuture<Collection<T>> fut = SServiceProvider.getServices(ea.getServiceProvider(), type, RequiredServiceInfo.SCOPE_LOCAL, filter);
								fut.addResultListener(new StoreIntermediateDelegationResultListener<T>(ret, provider, info, binding));
							}
						});
					}
					else if(binding.getComponentType()!=null)
					{
						// Search service by component type.
						
						getExternalAccessesByType(provider, info, binding).addResultListener(new ExceptionDelegationResultListener<Collection<IExternalAccess>, Collection<T>>(ret)
						{
							public void customResultAvailable(Collection<IExternalAccess> coll)
							{
//								final Collection coll = (Collection)result;
								if(coll!=null && coll.size()>0)
								{
									final CounterResultListener<T> lis = new CounterResultListener<T>(
										coll.size(), true, new IResultListener<Void>()
									{
										public void resultAvailable(Void result)
										{
											if(!binding.isDynamic())
												DefaultServiceFetcher.this.result = ret.getIntermediateResults();
											ret.setFinished();
										}
										
										public void exceptionOccurred(Exception exception)
										{
											if(!binding.isDynamic())
												DefaultServiceFetcher.this.result = ret.getIntermediateResults();
											ret.setFinished();
										}
									});
									
//									SServiceProvider.getService(provider, IComponentManagementService.class, RequiredServiceInfo.SCOPE_GLOBAL)
//										.addResultListener(new DelegationResultListener(future)
//									{
//										public void customResultAvailable(Object result)
//										{
//											final IComponentManagementService cms = (IComponentManagementService)result;
	
											for(Iterator it=coll.iterator(); it.hasNext(); )
											{
												final IExternalAccess ea = (IExternalAccess)it.next();
//												final IComponentAdapter adapter = cms.getComponentAdapter((IComponentIdentifier)provider.getId());
	
												SServiceProvider.getService(ea.getServiceProvider(), type, RequiredServiceInfo.SCOPE_LOCAL, filter)
													.addResultListener(new IResultListener<T>()
												{
													public void resultAvailable(final T result)
													{
														IFuture<T> fut = createProxy((IService)result, info, binding);
														fut.addResultListener(new IResultListener<T>()
														{
															public void resultAvailable(T result)
															{
																lis.intermediateResultAvailable(result);
															}
															
															public void exceptionOccurred(Exception exception)
															{
																lis.exceptionOccurred(exception);
															}
														});
													}
													
													public void exceptionOccurred(Exception exception)
													{
														lis.intermediateExceptionOccurred(exception);
													}
												});
											}
//										}
//									});
								}
								else
								{
									ret.setException(new RuntimeException("No component found."));
								}
							}
						});
					}
					else
					{
						// Search service using search specification.
						IIntermediateFuture<T>	ifut	= SServiceProvider.getServices(provider, type, binding.getScope(), filter);
						ifut.addResultListener(new StoreIntermediateDelegationResultListener<T>(ret, provider, info, binding)
						{
							public void exceptionOccurred(Exception exception)
							{
								createComponent(provider, info, binding).addResultListener(new ExceptionDelegationResultListener<IExternalAccess, Collection<T>>(ret)
								{
									public void customResultAvailable(IExternalAccess ea)
									{
										SServiceProvider.getServices(ea.getServiceProvider(), type, RequiredServiceInfo.SCOPE_LOCAL, filter)
											.addResultListener(new StoreIntermediateDelegationResultListener<T>(ret, provider, info, binding));
									}
									
									public void exceptionOccurred(Exception exception)
									{
//										exception.printStackTrace();
										super.exceptionOccurred(exception);
									}
								});
							}
						});
					}
		    	}
			}
		});
		
		return ret;
	}
	
	/**
	 * 
	 */
	protected <T> IIntermediateFuture<T> checkResults(final List<T> results)
	{
		final IntermediateFuture<T> ret = new IntermediateFuture<T>();
		
		if(results==null || results.size()==0)
		{
			ret.setFinished();
		}
		else
		{
			CounterResultListener<T> lis = new CounterResultListener<T>(results.size(), true, new IResultListener<Void>()
			{
				public void resultAvailable(Void result)
				{
					ret.setFinished();
				}
				
				public void exceptionOccurred(Exception exception)
				{
					ret.setException(exception);
				}
			})
			{
				public void intermediateResultAvailable(T result)
				{
					// only post result, if valid.
					if(result!=null)
						ret.addIntermediateResult(result);
					
					super.intermediateResultAvailable(result);
				}
			};

			for(int i=0; i<results.size(); i++)
			{
				checkResult(results.get(i)).addResultListener(lis);
			}
		}
		
		return ret;
	}
	
	/**
	 * 
	 */
	protected <T> IFuture<T> checkResult(T result)
	{
		final Future<T> ret = new Future<T>();
		final T res = result;
		
		if(result instanceof IService)
		{
//			final String sname = ((IService)result).getServiceIdentifier().getServiceName();
//			if(sname.indexOf("Add")!=-1)
//				System.out.println("checkRes: "+((IService)result).getServiceIdentifier());
			((IService)result).isValid().addResultListener(new ExceptionDelegationResultListener<Boolean, T>(ret)
			{
				public void customResultAvailable(Boolean result)
				{
//					if(sname.indexOf("Add")!=-1)
//						System.out.println("checkRes2: "+sname);
					ret.setResult(result.booleanValue()? res: null);
				}
				public void exceptionOccurred(Exception exception)
				{
//					System.out.println("kaputtt: "+exception);
//					super.exceptionOccurred(exception);
					ret.setResult(null);
				}
			});
		}
		else
		{
			ret.setResult(null);
		}
		
		return ret;
	}
	
	/**
	 *  Get the external access of a component by its name.
	 */
	protected IFuture<IExternalAccess> getExternalAccessByName(final IServiceProvider provider, final RequiredServiceInfo info, 
		final RequiredServiceBinding binding)
	{
		final Future<IExternalAccess> ret = new Future<IExternalAccess>();
		
		IComponentIdentifier parent = RequiredServiceInfo.SCOPE_PARENT.equals(binding.getScope())? ((IComponentIdentifier)provider.getId()).getParent(): (IComponentIdentifier)provider.getId(); 
		getExternalAccess(provider, binding.getComponentName(), parent).addResultListener(new DelegationResultListener<IExternalAccess>(ret)
		{
			public void exceptionOccurred(Exception exception)
			{
				// No component found with cid -> create.
//				System.out.println("creating: "+binding.getComponentName());
				if(binding.isCreate() && binding.getCreationInfo()!=null)
				{
					createComponent(provider, info, binding).addResultListener(new DelegationResultListener<IExternalAccess>(ret));
//					{
//						public void exceptionOccurred(Exception exception)
//						{
//							System.out.println("kuku");
//							super.exceptionOccurred(exception);
//						}
//					});				
				}
				else
				{
					ret.setException(exception);
				}
				
			}
		});
		return ret;
	}
	
	/**
	 *  Get the external access of a component by type.
	 */
	protected IFuture<Collection<IExternalAccess>> getExternalAccessesByType(final IServiceProvider provider, final RequiredServiceInfo info, 
		final RequiredServiceBinding binding)
	{
		final Future<Collection<IExternalAccess>> ret = new Future<Collection<IExternalAccess>>();
		
		if(RequiredServiceInfo.SCOPE_PARENT.equals(binding.getScope()))
		{
			SServiceProvider.getService(provider, IComponentManagementService.class, RequiredServiceInfo.SCOPE_GLOBAL)
				.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, Collection<IExternalAccess>>(ret)
			{
				public void customResultAvailable(final IComponentManagementService cms)
				{
					cms.getParent((IComponentIdentifier)provider.getId()).addResultListener(
						new ExceptionDelegationResultListener<IComponentIdentifier, Collection<IExternalAccess>>(ret)
					{
						public void customResultAvailable(final IComponentIdentifier cid)
						{
							getChildExternalAccesses(cid, provider, info, binding)
								.addResultListener(new DelegationResultListener<Collection<IExternalAccess>>(ret));
						}
					});
				}
			});
		}
		else //if(RequiredServiceInfo.SCOPE_LOCAL.equals(binding.getScope()))
		{
			getChildExternalAccesses((IComponentIdentifier)provider.getId(), provider, info, binding)
				.addResultListener(new DelegationResultListener<Collection<IExternalAccess>>(ret));
		}
//		else
//		{
//			ret.setException(new RuntimeException("Only parent or local scopes allowed."));
//		}
		
		return ret;
	}
	
	/**
	 *  Get a fitting (of given type) child component.
	 */
	public IFuture<Collection<IExternalAccess>> getChildExternalAccesses(final IComponentIdentifier cid, final IServiceProvider provider, 
		final RequiredServiceInfo info, final RequiredServiceBinding binding)
	{
		final Future<Collection<IExternalAccess>> ret = new Future<Collection<IExternalAccess>>();
		
		SServiceProvider.getService(provider, IComponentManagementService.class, RequiredServiceInfo.SCOPE_GLOBAL)
			.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, Collection<IExternalAccess>>(ret)
		{
			public void customResultAvailable(final IComponentManagementService cms)
			{
				cms.getExternalAccess(cid).addResultListener(new ExceptionDelegationResultListener<IExternalAccess, Collection<IExternalAccess>>(ret)
				{
					public void customResultAvailable(IExternalAccess exta)
					{
//						System.out.println("exta: "+exta.getComponentIdentifier()+" "+binding.getComponentType());
						exta.getChildren(binding.getComponentType()).addResultListener(new IResultListener<IComponentIdentifier[]>()
						{
							public void resultAvailable(IComponentIdentifier[] result)
							{
								if(result!=null && result.length>0)
								{
									CollectionResultListener<IExternalAccess> lis = new CollectionResultListener<IExternalAccess>(result.length, true, new DefaultResultListener<Collection<IExternalAccess>>()
									{
										public void resultAvailable(Collection<IExternalAccess> result)
										{
											ret.setResult(result);
										}
									});
									for(int i=0; i<result.length; i++ )
									{
										cms.getExternalAccess(result[i]).addResultListener(lis);
									}
								}
								else if(binding.isCreate() && binding.getCreationInfo()!=null)
								{
									createComponent(provider, info, binding)
										.addResultListener(new ExceptionDelegationResultListener<IExternalAccess, Collection<IExternalAccess>>(ret)
									{
										public void customResultAvailable(IExternalAccess result)
										{
											List<IExternalAccess> res = new ArrayList<IExternalAccess>();
											res.add(result);
											ret.setResult(res);
//											super.customResultAvailable((Collection<IExternalAccess>)ret);
										}
									});
								}
								else
								{
									ret.setException(new ServiceNotFoundException(binding.getName()));
								}
							}
							
							public void exceptionOccurred(Exception exception)
							{
								if(binding.isCreate() && binding.getCreationInfo()!=null)
								{
									createComponent(provider, info, binding).addResultListener(new ExceptionDelegationResultListener<IExternalAccess, Collection<IExternalAccess>>(ret)
									{
										public void customResultAvailable(IExternalAccess result)
										{
											List<IExternalAccess> res = new ArrayList<IExternalAccess>();
											res.add(result);
											ret.setResult(res);
	//										super.customResultAvailable(ret);
										}
									});
								}
								else
								{
									ret.setException(exception);
								}
							}
						});
					}
				});
			}
		});
		return ret;
	}
	
	/**
	 *  Create component identifier from name.
	 */
	protected IFuture<IComponentIdentifier> createComponentIdentifier(final IServiceProvider provider, final String name, final IComponentIdentifier parent)
	{
		final Future<IComponentIdentifier> ret = new Future<IComponentIdentifier>();
		
		SServiceProvider.getService(provider, IComponentManagementService.class, RequiredServiceInfo.SCOPE_GLOBAL)
			.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, IComponentIdentifier>(ret)
		{
			public void customResultAvailable(IComponentManagementService cms)
			{
				if(name.indexOf("@")==-1)
				{
					ret.setResult(new ComponentIdentifier(name, parent, parent.getAddresses()));
//					ret.setResult(cms.createComponentIdentifier(name, parent, parent.getAddresses()));
				}
				else
				{
					ret.setResult(new ComponentIdentifier(name));
//					ret.setResult(cms.createComponentIdentifier(name, false));
				}
			}
		});
			
		return ret;
	}
	
	/**
	 *  Get external access for component identifier.
	 */
	protected IFuture<IExternalAccess> getExternalAccess(final IServiceProvider provider, final IComponentIdentifier cid)
	{
		final Future<IExternalAccess> ret = new Future<IExternalAccess>();
		
		IFuture<IComponentManagementService> fut = SServiceProvider.getService(provider, IComponentManagementService.class, RequiredServiceInfo.SCOPE_GLOBAL);
		fut.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, IExternalAccess>(ret)
		{
			public void customResultAvailable(IComponentManagementService cms)
			{
				cms.getExternalAccess(cid).addResultListener(new DelegationResultListener<IExternalAccess>(ret));
			}
		});
		
		return ret;
	}
	
	/**
	 *  Get external access for component name.
	 */
	protected IFuture<IExternalAccess> getExternalAccess(final IServiceProvider provider, final String name, IComponentIdentifier parent)
	{
		final Future<IExternalAccess> ret = new Future<IExternalAccess>();
		
		createComponentIdentifier(provider, name, parent)
			.addResultListener(new ExceptionDelegationResultListener<IComponentIdentifier, IExternalAccess>(ret)
		{
			public void customResultAvailable(IComponentIdentifier result)
			{
				final IComponentIdentifier cid = (IComponentIdentifier)result;
				getExternalAccess(provider, cid).addResultListener(new DelegationResultListener<IExternalAccess>(ret));
			}
		});
		return ret;
	}
	
	/**
	 *  Create component and get external access for component.
	 */
	protected IFuture<IExternalAccess> createComponent(final IServiceProvider provider, final RequiredServiceInfo info, 
		final RequiredServiceBinding binding)
	{
		final Future<IExternalAccess> ret = new Future<IExternalAccess>();
//		final IComponentIdentifier parent = pa!=null? pa: (IComponentIdentifier)provider.getId();
		
		if(binding.isCreate() && binding.getCreationInfo()!=null)
		{
//			System.out.println("Create comp: "+provider+", "+binding.getComponentType());
			getParentAccess(provider, info, binding).addResultListener(new DelegationResultListener<IExternalAccess>(ret)
			{
				public void customResultAvailable(final IExternalAccess exta)
				{
					SServiceProvider.getService(provider, IComponentManagementService.class, RequiredServiceInfo.SCOPE_GLOBAL)
						.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, IExternalAccess>(ret)
					{
						public void customResultAvailable(IComponentManagementService cms)
						{
							exta.createChild(binding.getCreationInfo())
								.addResultListener(new ExceptionDelegationResultListener<IComponentIdentifier, IExternalAccess>(ret)
							{
								public void customResultAvailable(IComponentIdentifier cid)
								{
									getExternalAccess(provider, cid).addResultListener(new DelegationResultListener<IExternalAccess>(ret));
								}
								
								public void exceptionOccurred(Exception exception)
								{
									if(exception instanceof ComponentCreationException && ComponentCreationException.REASON_COMPONENT_EXISTS.equals(
										((ComponentCreationException)exception).getReason()))
									{
//										super.exceptionOccurred(exception);
										getExternalAccess(provider, (IComponentIdentifier)((ComponentCreationException)exception).getInfo())
										.addResultListener(new DelegationResultListener(ret)
										{
											public void exceptionOccurred(Exception exception) 
											{
												exception.printStackTrace();
											};
										});
									}
									else
									{
										super.exceptionOccurred(exception);
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
			ret.setException(new ServiceNotFoundException("name="+info.getName()+", interface="+info.getType().getTypeName()+", no component creation possible"));
		}
		
		return ret;
	}
	
	/**
	 *  Get the arguments.
	 *  @return The arguments as a map of name-value pairs.
	 */
	public Map<String, Object> getArguments(ComponentInstanceInfo component, String[] imports, IValueFetcher fetcher, ClassLoader cl)
	{
		Map<String, Object> ret = null;		
		UnparsedExpression[] arguments = component.getArguments();
		UnparsedExpression argumentsexp = component.getArgumentsExpression();
		
		if(arguments.length>0)
		{
			ret = new HashMap<String, Object>();

			for(int i=0; i<arguments.length; i++)
			{
				// todo: language
				if(arguments[i].getValue()!=null && arguments[i].getValue().length()>0)
				{
					Object val = SJavaParser.evaluateExpression(arguments[i].getValue(), imports, fetcher, cl);
					ret.put(arguments[i].getName(), val);
				}
			}
		}
		else if(argumentsexp!=null && argumentsexp.getValue()!=null && argumentsexp.getValue().length()>0)
		{
			// todo: language
			ret = (Map<String, Object>)SJavaParser.evaluateExpression(argumentsexp.getValue(), imports, fetcher, cl);
		}
		
		return ret;
	}
	
	/**
	 * 
	 */
	public IFuture<IExternalAccess> getParentAccess(final IServiceProvider provider, final RequiredServiceInfo info, final RequiredServiceBinding binding)
	{
		final Future<IExternalAccess> ret = new Future<IExternalAccess>();
		
		if(RequiredServiceInfo.SCOPE_PARENT.equals(binding.getScope()))
		{
			getExternalAccess(provider, provider.getId().getParent())
				.addResultListener(new DelegationResultListener<IExternalAccess>(ret));
			
//			SServiceProvider.getService(provider, IComponentManagementService.class, RequiredServiceInfo.SCOPE_GLOBAL)
//				.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, IExternalAccess>(ret)
//			{
//				public void customResultAvailable(IComponentManagementService cms)
//				{
//					cms.getParent(provider.getId())
//						.addResultListener(new ExceptionDelegationResultListener<IComponentIdentifier, IExternalAccess>(ret)
//					{
//						public void customResultAvailable(IComponentIdentifier cid)
//						{
////							final IComponentIdentifier cid = (IComponentIdentifier)result;
//							getExternalAccess(provider, cid)
//								.addResultListener(new DelegationResultListener<IExternalAccess>(ret));
//						}
//					});
//				}
//			});
		}
		else //if(RequiredServiceInfo.SCOPE_LOCAL.equals(binding.getScope()))
		{
			getExternalAccess(provider, (IComponentIdentifier)provider.getId())
				.addResultListener(new DelegationResultListener<IExternalAccess>(ret));
		}
//		else
//		{
//			ret.setException(new RuntimeException("Only parent or local scopes allowed."));
//		}
		
		return ret;
	}
	
//	/**
//	 *  Create a proxy.
//	 */
//	public Object createProxy(IInternalAccess ia, IExternalAccess ea, IComponentAdapter adapter, 
//		IService service, RequiredServiceInfo info, RequiredServiceBinding binding)
//	{
////		return service;
////		if(!service.getServiceIdentifier().getProviderId().equals(ea.getServiceProvider().getId()) || !Proxy.isProxyClass(service.getClass()))
//		Object proxy = service;
//		proxy = BasicServiceInvocationHandler.createRequiredServiceProxy(ia, ea, adapter, service, this, info, binding);
//		return proxy;
//	}
	
	/**
	 *  Create a proxy.
	 */
	public <T> IFuture<T> createProxy(final IService service, final RequiredServiceInfo info, final RequiredServiceBinding binding)
	{
		final Future<T> ret = new Future<T>();
//		final Class type = info.getType(info, cl)
//		ret.addResultListener(new IResultListener()
//		{
//			public void exceptionOccurred(Exception exception)
//			{
//			}
//			public void resultAvailable(Object result)
//			{
//				System.out.println("createProxy for "+service+": "+result);
//			}
//		});
//		return service;
//		if(!service.getServiceIdentifier().getProviderId().equals(ea.getServiceProvider().getId()) || !Proxy.isProxyClass(service.getClass()))
		
		SServiceProvider.getService(provider, IComponentManagementService.class, RequiredServiceInfo.SCOPE_GLOBAL)
			.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, T>(ret)
		{
			public void customResultAvailable(final IComponentManagementService cms)
			{
//				System.out.println("createProxy 1:"+service);
				cms.getComponentAdapter((IComponentIdentifier)provider.getId())
					.addResultListener(new ExceptionDelegationResultListener<IComponentAdapter, T>(ret)
				{
					public void customResultAvailable(final IComponentAdapter adapter)
					{
						IFuture<T>	fut	= access.scheduleStep(new IComponentStep<T>()
						{
							public IFuture<T> execute(IInternalAccess ia)
							{
//								System.out.println("createProxy 2:"+service);
								return new Future<T>((T)BasicServiceInvocationHandler.createRequiredServiceProxy(ia, ia.getExternalAccess(), 
									(IComponentAdapter)adapter, service, DefaultServiceFetcher.this, info, binding, realtime));
							}
						});
						fut.addResultListener(new DelegationResultListener<T>(ret));
					}
				});
			}
		});
		
		return ret;
	}
	
	/**
	 *  Simple listener that can store the result in a member variable.
	 */
	public class StoreDelegationResultListener<T> extends DelegationResultListener<T>
	{
		//-------- attributes --------
		
		/** The provider. */
		protected IServiceProvider provider;
		
		/** The required service info. */
		protected RequiredServiceInfo info;
		
		/** The required service binding. */
		protected RequiredServiceBinding binding;
		
		//-------- constructors --------
		
		/**
		 *  Create a new listener.
		 */
		public StoreDelegationResultListener(Future<T> ret, IServiceProvider provider, RequiredServiceInfo info, RequiredServiceBinding binding)
		{
			super(ret);
			this.provider = provider;
			this.info = info;
			this.binding = binding;
		}
		
		//-------- methods --------
		
		/**
		 *  Called when result is available.
		 */
		public void customResultAvailable(T result)
		{
			final T res = result;
			
			IFuture<T> fut = createProxy((IService)res, info, binding);
			fut.addResultListener(new DelegationResultListener<T>(future)
			{
				public void customResultAvailable(T result)
				{
					if(!binding.isDynamic())
						DefaultServiceFetcher.this.result = result;
					super.customResultAvailable(result);
				}
			});
			
//			SServiceProvider.getService(provider, IComponentManagementService.class, RequiredServiceInfo.SCOPE_GLOBAL)
//				.addResultListener(new DelegationResultListener(future)
//			{
//				public void customResultAvailable(Object result)
//				{
//					final IComponentManagementService cms = (IComponentManagementService)result;
//					cms.getExternalAccess((IComponentIdentifier)provider.getId()).addResultListener(new DelegationResultListener(future)
//					{
//						public void customResultAvailable(Object result)
//						{
//							final IExternalAccess ea = (IExternalAccess)result;
//							IComponentAdapter adapter = cms.getComponentAdapter((IComponentIdentifier)provider.getId());
//							createProxy((IService)res, info, binding).addResultListener(new DelegationResultListener(future)
//							{
//								public void customResultAvailable(Object result)
//								{
//									if(!binding.isDynamic())
//										DefaultServiceFetcher.this.result = result;
//									super.customResultAvailable(result);
//								}
//							});
//						}
//					});
//				}
//			});
		}	
	}
	
	/**
	 *  Simple listener that can store the result in a member variable.
	 */
	public class StoreIntermediateDelegationResultListener<T> extends IntermediateDelegationResultListener<T>
	{
		//-------- attributes --------
		
		/** The provider. */
		protected IServiceProvider provider;
		
		/** The required service info. */
		protected RequiredServiceInfo info;
		
		/** The required service binding. */
		protected RequiredServiceBinding binding;
		
		/** The checker. */
		protected FutureFinishChecker checker;
		
		//-------- constructors --------
		
		/**
		 *  Create a new listener.
		 */
		public StoreIntermediateDelegationResultListener(IntermediateFuture<T> ret, IServiceProvider provider, 
			final RequiredServiceInfo info, final RequiredServiceBinding binding)
		{
			super(ret);
			this.provider = provider;
			this.info = info;
			this.binding = binding;
			this.checker = new FutureFinishChecker(new DefaultResultListener<T>()
			{
				public void resultAvailable(T result)
				{
					// If already had exception do nothing.
					// Cannot cause race conditions as the checker is called only if all tasks have been done.
					if(future.isDone())
						return;
					
					if(!binding.isDynamic())
						DefaultServiceFetcher.this.result = future.getIntermediateResults();
					
					if(future.getIntermediateResults().size()==0)
					{
						StoreIntermediateDelegationResultListener.this.exceptionOccurred(new ServiceNotFoundException("no results"));
					}
					else
					{
						StoreIntermediateDelegationResultListener.super.finished();
					}
				}
			});
		}
		
		//-------- methods --------
		
		/**
		 *  Called when an intermediate result is available.
		 *  @param result The result.
		 */
		public void customIntermediateResultAvailable(T result)
		{
			final Future<T> ret = new Future<T>();
			checker.addTask(ret);
			
//			System.out.println("result: "+result);
			final Object res = result;
			IFuture<T> fut = createProxy((IService)res, info, binding);
			fut.addResultListener(new IResultListener<T>()
			{
				public void resultAvailable(T result)
				{
//					System.out.println("added: "+result);
					StoreIntermediateDelegationResultListener.super.customIntermediateResultAvailable(result);
					ret.setResult(null);
				}
				
				public void exceptionOccurred(Exception exception)
				{
//					System.out.println("ex: "+exception);
//					exception.printStackTrace();
					// Avoid having multiple exception occurred notifications.
					future.setExceptionIfUndone(exception);
//					if(!future.isDone())
//						StoreIntermediateDelegationResultListener.super.exceptionOccurred(exception);
					ret.setResult(null);
				}
			});

//			SServiceProvider.getService(provider, IComponentManagementService.class, RequiredServiceInfo.SCOPE_GLOBAL)
//				.addResultListener(new DelegationResultListener(future)
//			{
//				public void customResultAvailable(Object result)
//				{
//					final IComponentManagementService cms = (IComponentManagementService)result;
//					cms.getExternalAccess((IComponentIdentifier)provider.getId()).addResultListener(new DelegationResultListener(future)
//					{
//						public void customResultAvailable(Object result)
//						{
//							final IExternalAccess ea = (IExternalAccess)result;
//							IComponentAdapter adapter = cms.getComponentAdapter((IComponentIdentifier)provider.getId());
//							
//							StoreIntermediateDelegationResultListener.super.customIntermediateResultAvailable(createProxy(ea, adapter, (IService)res, info, binding));
//						}
//					});
//				}
//			});
		}
		
		/**
	     *  Declare that the future is finished.
	     */
		public void finished()
		{			
			checker.finished();
		}
		
		/**
		 *  Called when result is available.
		 */
		public void customResultAvailable(Collection<T> result)
		{
			// todo: can this happen??? shouldn't it be always Collection<T>
			
			if(result instanceof Collection)
			{
				for(Iterator<T> it=((Collection<T>)result).iterator(); it.hasNext(); )
				{
					intermediateResultAvailable(it.next());
				}
			}
			else
			{
				intermediateResultAvailable((T)result);
			}
			finished();
			
//			final Object res = result;
//			SServiceProvider.getService(provider, IComponentManagementService.class, RequiredServiceInfo.SCOPE_GLOBAL)
//				.addResultListener(new DelegationResultListener(future)
//			{
//				public void customResultAvailable(Object result)
//				{
//					final IComponentManagementService cms = (IComponentManagementService)result;
//					cms.getExternalAccess((IComponentIdentifier)provider.getId()).addResultListener(new DelegationResultListener(future)
//					{
//						public void customResultAvailable(Object result)
//						{
//							final IExternalAccess ea = (IExternalAccess)result;
//							IComponentAdapter adapter = cms.getComponentAdapter((IComponentIdentifier)provider.getId());
//							
//							if(res instanceof Collection)
//							{
//								List tmp = new ArrayList();
//								CollectionResultListener collis = new CollectionResultListener(((Collection)res).size(), true, new DefaultResultListener()
//								{
//									public void resultAvailable(Object result)
//									{
//										if(binding.isDynamic())
//											DefaultServiceFetcher.this.result = result;
//										StoreIntermediateDelegationResultListener.super.resultAvailable(result);
//									}
//								});
//								for(Iterator it=((Collection)res).iterator(); it.hasNext(); )
//								{
//									createProxy((IService)it.next(), info, binding).addResultListener(collis);
////									tmp.add(createProxy(ea, adapter, (IService)it.next(), info, binding));
//								}
//							}
//							else
//							{
//								createProxy((IService)res, info, binding).addResultListener(new IResultListener()
//								{
//									public void resultAvailable(Object result)
//									{
//										if(binding.isDynamic())
//											DefaultServiceFetcher.this.result = result;
//										StoreIntermediateDelegationResultListener.super.resultAvailable(result);
//									}
//									
//									public void exceptionOccurred(Exception exception)
//									{
//										StoreIntermediateDelegationResultListener.super.exceptionOccurred(exception);
//									}
//								});
//							}
//						}
//					});
//				}
//			});
		}	
	}
}


