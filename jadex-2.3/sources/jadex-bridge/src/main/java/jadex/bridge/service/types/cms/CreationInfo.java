package jadex.bridge.service.types.cms;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IResourceIdentifier;
import jadex.bridge.service.RequiredServiceBinding;

import java.util.Map;

/**
 *  A parameter object to capture
 *  extra information for component creation.
 *  All of the information is optional, i.e.
 *  may be null.
 */
public class CreationInfo
{
	//-------- attributes --------
	
	/** The configuration. */
	protected String	config;
	
	/** The arguments (map with name/value pairs). */
	protected Map<String, Object>	args;
	
	/** The parent component. */
	protected IComponentIdentifier	parent;
	
	/** The resource identifier. */
	protected IResourceIdentifier	rid;
	
	/** The suspend flag (default: false). */
	protected Boolean suspend;

	/** The master flag (default: false). */
	protected Boolean master;
	
	/** The daemon flag (default: false). */
	protected Boolean daemon;
	
	/** The auto shutdown flag (default: false). */
	protected Boolean autoshutdown;
	
//	/** The platform classloader flag (default: false). */
//	protected Boolean platformloader;
	
	/** The imports. */
	protected String[] imports;
	
	/** The bindings. */
	protected RequiredServiceBinding[] bindings;
	
	/** The local component type name. */
	protected String localtype;
	
	//-------- constructors --------
	
	/**
	 *  Create a new creation info. 
	 */
	public CreationInfo()
	{
		// Bean constructor.
	}
	
	/**
	 *  Create a new creation info. 
	 */
	public CreationInfo(CreationInfo info)
	{
		if(info!=null)
		{
			this.config	= info.getConfiguration();
			this.args	= info.getArguments();
			this.parent	= info.getParent();
			this.suspend	= info.getSuspend(); 
			this.master = info.getMaster();
			this.daemon = info.getDaemon();
			this.autoshutdown = info.getAutoShutdown();
			this.imports	= info.getImports();
			this.bindings = info.getRequiredServiceBindings();
			this.rid = info.getResourceIdentifier();
		}
	}

	/**
	 *  Create a new creation info.
	 *  @param parent	The parent of the component to be created.
	 */
	public CreationInfo(IComponentIdentifier parent)
	{
		this(null, parent);
	}
	
	/**
	 *  Create a new creation info.
	 *  @param args	The arguments.
	 */
	public CreationInfo(Map<String, Object> args)
	{
		this(null, args);
	}
	
	/**
	 *  Create a new creation info.
	 *  @param args	The arguments.
	 */
	public CreationInfo(IResourceIdentifier rid)
	{
		this(null, null, null, null, null, null, null, null, null, rid);
	}
	
	/**
	 *  Create a new creation info.
	 *  @param parent	The parent of the component to be created.
	 */
	public CreationInfo(IComponentIdentifier parent, IResourceIdentifier rid)
	{
		this(null, null, parent, null, null, null, null, null, null, rid);
	}
	
	/**
	 *  Create a new creation info.
	 *  @param config	The configuration.
	 *  @param args	The arguments.
	 */
	public CreationInfo(String config, Map<String, Object> args)
	{
		this(config, args, (IComponentIdentifier)null);
	}
	
	/**
	 *  Create a new creation info.
	 *  @param parent	The parent of the component to be created.
	 */
	public CreationInfo(String config, Map<String, Object> args, IResourceIdentifier rid)
	{
		this(config, args, null, null, null, null, null, null, null, rid);
	}
	

	/**
	 *  Create a new creation info.
	 *  @param args	The arguments.
	 *  @param parent	The parent of the component to be created.
	 */
	public CreationInfo(Map<String, Object> args, IComponentIdentifier parent)
	{
		this(null, args, parent);
	}
	
	/**
	 *  Create a new creation info.
	 *  @param config	The configuration.
	 *  @param args	The arguments.
	 *  @param parent	The parent of the component to be created.
	 */
	public CreationInfo(String config, Map<String, Object> args, IComponentIdentifier parent)
	{
		this(config, args, parent, null, (String[])null);
	}
	
	/**
	 *  Create a new creation info.
	 *  @param config	The configuration.
	 *  @param args	The arguments.
	 *  @param parent	The parent of the component to be created.
	 *  @param suspend	The suspend flag.
	 *  @param master	The master flag.
	 *  @param imports	The imports.
	 */
	public CreationInfo(String config, Map<String, Object> args, IComponentIdentifier parent, Boolean suspend)
	{
		this(config, args, parent, suspend, (String[])null);
	}
	
	/**
	 *  Create a new creation info.
	 *  @param config	The configuration.
	 *  @param args	The arguments.
	 *  @param parent	The parent of the component to be created.
	 *  @param suspend	The suspend flag.
	 *  @param master	The master flag.
	 *  @param imports	The imports.
	 */
	public CreationInfo(String config, Map<String, Object> args, IComponentIdentifier parent, Boolean suspend, String[] imports)
	{
		this(config, args, parent, suspend, null, null, null, imports, null, null);
	}
	
	/**
	 *  Create a new creation info.
	 *  @param config	The configuration.
	 *  @param args	The arguments.
	 *  @param parent	The parent of the component to be created.
	 *  @param suspend	The suspend flag.
	 *  @param master	The master flag.
	 */
	public CreationInfo(String config, Map<String, Object> args, IComponentIdentifier parent, Boolean suspend, Boolean master)
	{
		this(config, args, parent, suspend, master, null);
	}
	
	/**
	 *  Create a new creation info.
	 *  @param config	The configuration.
	 *  @param args	The arguments.
	 *  @param parent	The parent of the component to be created.
	 *  @param suspend	The suspend flag.
	 *  @param master	The master flag.
	 */
	public CreationInfo(String config, Map<String, Object> args, IComponentIdentifier parent, Boolean suspend, Boolean master, Boolean daemon)
	{
		this(config, args, parent, suspend, master, daemon, null);
	}
	
	/**
	 *  Create a new creation info.
	 *  @param config	The configuration.
	 *  @param args	The arguments.
	 *  @param parent	The parent of the component to be created.
	 *  @param suspend	The suspend flag.
	 *  @param master	The master flag.
	 */
	public CreationInfo(String config, Map<String, Object> args, IComponentIdentifier parent, Boolean suspend, 
		Boolean master, Boolean daemon, Boolean autoshutdown)
	{
		this(config, args, parent, suspend, master, daemon, autoshutdown, null, null, null);
	}
	
	/**
	 *  Create a new creation info.
	 *  @param config	The configuration.
	 *  @param args	The arguments.
	 *  @param parent	The parent of the component to be created.
	 *  @param suspend	The suspend flag.
	 *  @param master	The master flag.
	 *  @param imports	The imports.
	 */
	public CreationInfo(String config, Map<String, Object> args, IComponentIdentifier parent, 
		Boolean suspend, Boolean master, Boolean daemon, Boolean autoshutdown, 
		String[] imports, RequiredServiceBinding[] bindings, IResourceIdentifier rid)
	{
		this.config	= config;
		this.args	= args;
		this.parent	= parent;
		this.suspend	= suspend;
		this.master = master;
		this.daemon = daemon;
		this.autoshutdown = autoshutdown;
		this.imports	= imports;
		this.bindings = bindings;
		this.rid = rid;
	}
	
	//-------- methods --------
	
	/**
	 *  Get the configuration.
	 *  @return the config.
	 */
	public String getConfiguration()
	{
		return config;
	}

	/**
	 *  Set the configuration.
	 *  @param config the config to set.
	 */
	public void setConfiguration(String config)
	{
		this.config = config;
	}

	/**
	 *  Get the arguments.
	 *  @return the args.
	 */
	public Map<String, Object> getArguments()
	{
		return args;
	}

	/**
	 *  Set the arguments.
	 *  @param args the args to set
	 */
	public void setArguments(Map<String, Object> args)
	{
		this.args = args;
	}

	/**
	 *  Get the parent.
	 *  @return the parent
	 */
	public IComponentIdentifier getParent()
	{
		return parent;
	}

	/**
	 *  Set the parent.
	 *  @param parent the parent to set
	 */
	public void setParent(IComponentIdentifier parent)
	{
		this.parent = parent;
	}

	/**
	 *  Get the resource identifier for loading the component model.
	 *  @return the resource identifier.
	 */
	public IResourceIdentifier getResourceIdentifier()
	{
		return rid;
	}

	/**
	 *  Set the resource identifier for loading the component model.
	 *  @param rid the resource identifier to set
	 */
	public void setResourceIdentifier(IResourceIdentifier rid)
	{
		this.rid = rid;
	}

	/**
	 *  Get the suspend flag.
	 *  @return the suspend flag
	 */
	public Boolean getSuspend()
	{
		return suspend;
	}

	/**
	 *  Set the suspend flag.
	 *  @param suspend the suspend to set flag
	 */
	public void setSuspend(Boolean suspend)
	{
		this.suspend = suspend;
	}

	/**
	 *  Get the master.
	 *  @return The master.
	 */
	public Boolean getMaster()
	{
		return master;
	}

	/**
	 *  Set the master.
	 *  @param master The master to set.
	 */
	public void setMaster(Boolean master)
	{
		this.master = master;
	}

	/**
	 *  Get the daemon.
	 *  @return The daemon.
	 */
	public Boolean getDaemon()
	{
		return daemon;
	}

	/**
	 *  Set the daemon.
	 *  @param daemon The daemon to set.
	 */
	public void setDaemon(Boolean daemon)
	{
		this.daemon = daemon;
	}

	/**
	 *  Get the autoshutdown.
	 *  @return The autoshutdown.
	 */
	public Boolean getAutoShutdown()
	{
		return autoshutdown;
	}

	/**
	 *  Set the autoshutdown.
	 *  @param autoshutdown The autoshutdown to set.
	 */
	public void setAutoShutdown(Boolean autoshutdown)
	{
		this.autoshutdown = autoshutdown;
	}
	
//	public Boolean getPlatformloader()
//	{
//		return platformloader;
//	}
//	
//	public void setPlatformloader(Boolean platformloader)
//	{
//		this.platformloader = platformloader;
//	}

	/**
	 *  Get the imports.
	 *  @return the imports.
	 */
	public String[] getImports()
	{
		return imports;
	}

	/**
	 *  Set the imports
	 *  @param imports The imports to set.
	 */
	public void setImports(String[] imports)
	{
		this.imports = imports;
	}

	/**
	 *  Get the bindings.
	 *  @return The bindings.
	 */
	public RequiredServiceBinding[] getRequiredServiceBindings()
	{
		return bindings;
	}

	/**
	 *  Set the bindings.
	 *  @param bindings The bindings to set.
	 */
	public void setRequiredServiceBindings(RequiredServiceBinding[] bindings)
	{
		this.bindings = bindings;
	}

	/**
	 *  Get the localtype.
	 *  @return the localtype.
	 */
	public String getLocalType()
	{
		return localtype;
	}

	/**
	 *  Set the localtype.
	 *  @param localtype The localtype to set.
	 */
	public void setLocalType(String localtype)
	{
		this.localtype = localtype;
	}
}
