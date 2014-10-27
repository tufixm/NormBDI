package jadex.bdiv3;

import jadex.bdiv3.model.BDIModel;
import jadex.bdiv3.runtime.impl.BDIAgentInterpreter;
import jadex.bridge.ComponentIdentifier;
import jadex.bridge.IComponentInstance;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.IResourceIdentifier;
import jadex.bridge.modelinfo.IModelInfo;
import jadex.bridge.service.BasicService;
import jadex.bridge.service.IServiceProvider;
import jadex.bridge.service.RequiredServiceBinding;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.IComponentDescription;
import jadex.bridge.service.types.factory.IComponentAdapter;
import jadex.bridge.service.types.factory.IComponentAdapterFactory;
import jadex.bridge.service.types.factory.IComponentFactory;
import jadex.bridge.service.types.library.ILibraryService;
import jadex.bridge.service.types.library.ILibraryServiceListener;
import jadex.commons.LazyResource;
import jadex.commons.SReflect;
import jadex.commons.SUtil;
import jadex.commons.Tuple2;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IIntermediateResultListener;
/* $if !android $ */
import jadex.kernelbase.IBootstrapFactory;
/* $endif $ */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 *  Factory for creating micro agents.
 */
public class BDIAgentFactory extends BasicService implements IComponentFactory, IBootstrapFactory
{
	//-------- constants --------
	
	/** The bdi agent file type. */
	public static final String	FILETYPE_BDIAGENT	= "BDIV3 Agent";
	
	/** The image icon. */
	protected static final LazyResource ICON = new LazyResource(BDIAgentFactory.class, "/jadex/bdiv3/images/bdi_agent.png");

	//-------- attributes --------
	
	/** The application model loader. */
	protected BDIModelLoader loader;
	
	/** The platform. */
	protected IServiceProvider provider;
	
	/** The properties. */
	protected Map properties;
	
	/** The library service. */
	protected ILibraryService libservice;
	
	/** The library service listener */
	protected ILibraryServiceListener libservicelistener;
	
	//-------- constructors --------
	
	/**
	 *  Create a new agent factory.
	 */
	public BDIAgentFactory(IServiceProvider provider)//, Map properties)
	{
		super(provider.getId(), IComponentFactory.class, null);

		this.provider = provider;
//		this.properties = properties;
		this.loader = new BDIModelLoader();
		
		this.libservicelistener = new ILibraryServiceListener()
		{
			public IFuture<Void> resourceIdentifierAdded(IResourceIdentifier parid, IResourceIdentifier rid, boolean removable)
			{
				loader.clearModelCache();
				return IFuture.DONE;
			}
			
			public IFuture<Void> resourceIdentifierRemoved(IResourceIdentifier parid, IResourceIdentifier rid)
			{
				loader.clearModelCache();
				return IFuture.DONE;
			}
		};
	}
	
	/**
	 *  Create a new agent factory for startup.
	 *  @param platform	The platform.
	 */
	// This constructor is used by the Starter class and the ADFChecker plugin. 
	public BDIAgentFactory(String providerid)
	{
		super(new ComponentIdentifier(providerid), IComponentFactory.class, null);
		this.loader = new BDIModelLoader();
	}
	
	/**
	 *  Start the service.
	 */
	public IFuture<Void> startService(IInternalAccess component, IResourceIdentifier rid)
	{
		this.provider = component.getServiceContainer();
		this.providerid = provider.getId();
		createServiceIdentifier("Bootstrap Factory", IComponentFactory.class, rid, IComponentFactory.class);
		return startService();
	}
	
	/**
	 *  Start the service.
	 */
	public IFuture<Void> startService()
	{
		final Future<Void> ret = new Future<Void>();
		SServiceProvider.getService(provider, ILibraryService.class, RequiredServiceInfo.SCOPE_PLATFORM)
			.addResultListener(new DelegationResultListener(ret)
		{
			public void customResultAvailable(Object result)
			{
				libservice = (ILibraryService)result;
				libservice.addLibraryServiceListener(libservicelistener);
				BDIAgentFactory.super.startService().addResultListener(new DelegationResultListener(ret));
			}
		});
		return ret;
	}
	
	/**
	 *  Shutdown the service.
	 *  @param listener The listener.
	 */
	public synchronized IFuture<Void>	shutdownService()
	{
		final Future<Void>	ret	= new Future<Void>();
		libservice.removeLibraryServiceListener(libservicelistener)
			.addResultListener(new DelegationResultListener<Void>(ret)
		{
			public void customResultAvailable(Void result)
			{
				BDIAgentFactory.super.shutdownService()
					.addResultListener(new DelegationResultListener<Void>(ret));
			}
		});
			
		return ret;
	}
	
	//-------- IAgentFactory interface --------
	
	/**
	 *  Load a  model.
	 *  @param model The model (e.g. file name).
	 *  @param The imports (if any).
	 *  @return The loaded model.
	 */
	public IFuture<IModelInfo> loadModel(final String model, final String[] imports, final IResourceIdentifier rid)
	{
		final Future<IModelInfo> ret = new Future<IModelInfo>();
//		System.out.println("filename: "+model);
		
		if(libservice!=null)
		{
			libservice.getClassLoader(rid)
				.addResultListener(new ExceptionDelegationResultListener<ClassLoader, IModelInfo>(ret)
			{
				public void customResultAvailable(ClassLoader cl)
				{
					try
					{
						IModelInfo mi = loader.loadComponentModel(model, imports, cl, new Object[]{rid, getProviderId().getRoot()}).getModelInfo();
						ret.setResult(mi);
					}
					catch(Exception e)
					{
						ret.setException(e);
					}
				}
			});		
		}
		else
		{
			try
			{
				ClassLoader cl = getClass().getClassLoader();
				IModelInfo mi = loader.loadComponentModel(model, imports, cl, new Object[]{rid, getProviderId().getRoot()}).getModelInfo();
				ret.setResult(mi);
			}
			catch(Exception e)
			{
				ret.setException(e);
			}			
		}

		
		return ret;
	}
		
	/**
	 *  Test if a model can be loaded by the factory.
	 *  @param model The model (e.g. file name).
	 *  @param The imports (if any).
	 *  @return True, if model can be loaded.
	 */
	public IFuture<Boolean> isLoadable(String model, String[] imports, IResourceIdentifier rid)
	{
//		boolean ret = model.toLowerCase().endsWith("bdi.class");
		boolean ret = model.endsWith(BDIModelLoader.FILE_EXTENSION_BDIV3);
		
//		if(ret)
//			System.out.println("isLoadable: "+model+" "+ret);
		
//		if(model.toLowerCase().endsWith("Agent.class"))
//		{
//			ILibraryService libservice = (ILibraryService)platform.getService(ILibraryService.class);
//			String clname = model.substring(0, model.indexOf(".class"));
//			Class cma = SReflect.findClass0(clname, null, libservice.getClassLoader());
//			ret = cma!=null && cma.isAssignableFrom(IMicroAgent.class);
//			System.out.println(clname+" "+cma+" "+ret);
//		}
		return new Future<Boolean>(ret? Boolean.TRUE: Boolean.FALSE);
	}
	
	/**
	 *  Test if a model is startable (e.g. an component).
	 *  @param model The model (e.g. file name).
	 *  @param The imports (if any).
	 *  @return True, if startable (and loadable).
	 */
	public IFuture<Boolean> isStartable(String model, String[] imports, IResourceIdentifier rid)
	{
		return isLoadable(model, imports, rid);
	}

	/**
	 *  Get the names of ADF file types supported by this factory.
	 */
	public String[] getComponentTypes()
	{
		return new String[]{FILETYPE_BDIAGENT};
	}

	/**
	 *  Get a default icon for a file type.
	 */
	public IFuture<byte[]> getComponentTypeIcon(String type)
	{
		Future<byte[]>	ret	= new Future<byte[]>();
		if(type.equals(FILETYPE_BDIAGENT))
		{
			try
			{
				ret.setResult(ICON.getData());
			}
			catch(IOException e)
			{
				ret.setException(e);
			}
		}
		else
		{
			ret.setResult(null);
		}
		
//		System.out.println("getIcon: "+type+" "+type.equals(FILETYPE_BDIAGENT));
		return ret;
	}	

	/**
	 *  Get the component type of a model.
	 *  @param model The model (e.g. file name).
	 *  @param The imports (if any).
	 */
	public IFuture<String> getComponentType(String model, String[] imports, IResourceIdentifier rid)
	{
//		System.out.println("model: "+model+" "+model.toLowerCase().endsWith("bdi.class"));
		return new Future<String>(model.endsWith(BDIModelLoader.FILE_EXTENSION_BDIV3) ? FILETYPE_BDIAGENT: null);
	}
	
	/**
	 * Create a component instance.
	 * @param adapter The component adapter.
	 * @param model The component model.
	 * @param config The name of the configuration (or null for default configuration) 
	 * @param arguments The arguments for the agent as name/value pairs.
	 * @param parent The parent component (if any).
	 * @return An instance of a component.
	 */
	public IFuture<Tuple2<IComponentInstance, IComponentAdapter>> createComponentInstance(final IComponentDescription desc, final IComponentAdapterFactory factory, final IModelInfo model, 
		final String config, final Map<String, Object> arguments, final IExternalAccess parent, final RequiredServiceBinding[] binding, final boolean copy, final boolean realtime,
		final IIntermediateResultListener<Tuple2<String, Object>> listener, final Future<Void> inited)
	{
		final Future<Tuple2<IComponentInstance, IComponentAdapter>> res = new Future<Tuple2<IComponentInstance, IComponentAdapter>>();
		
		if(libservice!=null)
		{
			// todo: is model info ok also in remote case?
	//		ClassLoader cl = libservice.getClassLoader(model.getResourceIdentifier());
			libservice.getClassLoader(model.getResourceIdentifier())
				.addResultListener(new ExceptionDelegationResultListener<ClassLoader, Tuple2<IComponentInstance, IComponentAdapter>>(res)
			{
				public void customResultAvailable(ClassLoader cl)
				{
					try
					{
						BDIModel mm = loader.loadComponentModel(model.getFilename(), null, cl, new Object[]{model.getResourceIdentifier(), getProviderId().getRoot()});
						BDIAgentInterpreter mai = new BDIAgentInterpreter(desc, factory, mm, getMicroAgentClass(model.getFullName()+BDIModelLoader.FILE_EXTENSION_BDIV3_FIRST, 
							null, cl), arguments, config, parent, binding, copy, realtime, listener, inited);
						res.setResult(new Tuple2<IComponentInstance, IComponentAdapter>(mai, mai.getComponentAdapter()));
					}
					catch(Exception e)
					{
						res.setException(e);
					}
				}
			});
		}
		
		// For platform bootstrapping
		else
		{
			try
			{
				ClassLoader	cl	= getClass().getClassLoader();
				BDIModel mm = loader.loadComponentModel(model.getFilename(), null, cl, new Object[]{model.getResourceIdentifier(), getProviderId().getRoot()});
				BDIAgentInterpreter mai = new BDIAgentInterpreter(desc, factory, mm, getMicroAgentClass(model.getFullName()+BDIModelLoader.FILE_EXTENSION_BDIV3_FIRST, 
					null, cl), arguments, config, parent, binding, copy, realtime, listener, inited);
				res.setResult(new Tuple2<IComponentInstance, IComponentAdapter>(mai, mai.getComponentAdapter()));
			}
			catch(Exception e)
			{
				res.setException(e);
			}
		}

		return res;
//		return new Future<Tuple2<IComponentInstance, IComponentAdapter>>(new Tuple2<IComponentInstance, IComponentAdapter>(mai, mai.getAgentAdapter()));
	}
	
	/**
	 *  Get the element type.
	 *  @return The element type (e.g. an agent, application or process).
	 * /
	public String getElementType()
	{
		return IComponentFactory.ELEMENT_TYPE_AGENT;
	}*/
	
	/**
	 *  Get the properties.
	 *  Arbitrary properties that can e.g. be used to
	 *  define kernel-specific settings to configure tools.
	 *  @param type	The component type. 
	 *  @return The properties or null, if the component type is not supported by this factory.
	 */
	public Map	getProperties(String type)
	{
		return FILETYPE_BDIAGENT.equals(type)? properties: null;
	}
	
	/**
	 *  Start the service.
	 * /
	public synchronized IFuture	startService()
	{
		return new Future(null);
	}*/
	
	/**
	 *  Shutdown the service.
	 *  @param listener The listener.
	 * /
	public synchronized IFuture	shutdownService()
	{
		return new Future(null);
	}*/
	
	/**
	 *  Get the mirco agent class.
	 */
	// todo: make use of cache
	protected Class getMicroAgentClass(String clname, String[] imports, ClassLoader classloader)
	{
		Class ret = SReflect.findClass0(clname, imports, classloader);
//		System.out.println("getMAC:"+clname+" "+SUtil.arrayToString(imports)+" "+ret);
		int idx;
		while(ret==null && (idx=clname.indexOf('.'))!=-1)
		{
			clname	= clname.substring(idx+1);
			ret = SReflect.findClass0(clname, imports, classloader);
//			System.out.println(clname+" "+cma+" "+ret);
		}
		if(ret==null)// || !cma.isAssignableFrom(IMicroAgent.class))
			throw new RuntimeException("No bdi agent file: "+clname+" "+classloader);
		return ret;
	}
	
	/**
	 *  Add excluded methods.
	 */
	public static void addExcludedMethods(Map props, String[] excludes)
	{
		Object ex = props.get("remote_excluded");
		if(ex!=null)
		{
			List newex = new ArrayList();
			for(Iterator it=SReflect.getIterator(ex); it.hasNext(); )
			{
				newex.add(it.next());
			}
			for(int i=0; i<excludes.length; i++)
			{
				newex.add(excludes[i]);
			}
		}
		else
		{
			props.put("remote_excluded", excludes);
		}
	}
}
