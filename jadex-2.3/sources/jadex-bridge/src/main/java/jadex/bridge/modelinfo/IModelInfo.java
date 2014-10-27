package jadex.bridge.modelinfo;

import jadex.bridge.IErrorReport;
import jadex.bridge.IResourceIdentifier;
import jadex.bridge.service.ProvidedServiceInfo;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Reference;

import java.util.Map;


/**
 *  This model interface to be used (invoked) by tools and adapters.
 *  Can represent an application or an component (also capability).
 *  Applications can be loaded by the application factory.
 *  @link{IApplicationFactory}
 *  Component types can be loaded by the kernel's component factory
 *  @link{IComponentFactory}. 
 */
@Reference(remote=false)
public interface IModelInfo
{
	/**
	 *  Get the name.
	 *  @return The name.
	 */
	public String getName();
	
	/**
	 *  Get the package name.
	 *  @return The package name.
	 */
	public String getPackage();
	
	/**
	 *  Get the full model name (package.name)
	 *  @return The full name.
	 */
	public String getFullName();
	
	/**
	 *  Get the model description.
	 *  @return The model description.
	 */
	public String getDescription();
	
	/**
	 *  Get the imports.
	 *  @return The imports.
	 */
	public String[] getImports();
	
	/**
	 *  Get the imports including the package.
	 *  @return The imports.
	 */
	public String[] getAllImports();
	
	/**
	 *  Get the report.
	 *  @return The report.
	 */
	public IErrorReport getReport();
	
	/**
	 *  Get the configurations.
	 *  @return The configuration.
	 */
	public String[] getConfigurationNames();
	
	/**
	 *  Get the configurations.
	 *  @return The configuration.
	 */
	public ConfigurationInfo[] getConfigurations();
	
	/**
	 *  Get the configurations.
	 *  @return The configuration.
	 */
	public ConfigurationInfo getConfiguration(String name);
	
	/**
	 *  Get the arguments.
	 *  @return The arguments.
	 */
	public IArgument[] getArguments();
	
	/**
	 *  Get the argument.
	 *  @return The argument.
	 */
	public IArgument getArgument(String name);
	
	/**
	 *  Get the results.
	 *  @return The results.
	 */
	public IArgument[] getResults();
	
	/**
	 *  Get the results.
	 *  @param name The name.
	 *  @return The results.
	 */
	public IArgument getResult(String name);
	
	/**
	 *  Is the model startable.
	 *  @return True, if startable.
	 */
	public boolean isStartable();
	
	/**
	 *  Get the component type (i.e. kernel).
	 *  @return The component type.
	 */
	public String getType();
	
	/**
	 *  Get the filename.
	 *  @return The filename.
	 */
	public String getFilename();

	/**
	 *  Get the properties.
	 *  Arbitrary properties that can e.g. be used to
	 *  define model-specific settings to configure tools. 
	 *  @return The properties.
	 */
	public Map<String, Object>	getProperties();

	/**
	 *  Get a parsed property.
	 *  Unlike raw properties, which may be parsed or unparsed,
	 *  this method always returns parsed property values.
	 *  @param	name	The property name.  
	 *  @return The property value.
	 */
	public Object	getProperty(String name, ClassLoader cl);

//	/**
//	 *  Return the class loader corresponding to the model.
//	 *  @return The class loader corresponding to the model.
//	 */
//	public ClassLoader getClassLoader();
	
	/**
	 *  Return the resource identifier.
	 *  @return The resource identifier.
	 */
	public IResourceIdentifier getResourceIdentifier();
	
	/**
	 *  Get the required services.
	 *  @return The required services.
	 */
	public RequiredServiceInfo[] getRequiredServices();

	/**
	 *  Get the required service.
	 *  @return The required service.
	 */
	public RequiredServiceInfo getRequiredService(String name);
	
	/**
	 *  Get the provided services.
	 *  @return The provided services.
	 */
	public ProvidedServiceInfo[] getProvidedServices();
	
	/**
	 *  Get the suspend flag.
	 *  @param configname The configname.
	 *  @return The suspend flag value.
	 */
	public Boolean getSuspend(String configname);
	
	/**
	 *  Get the master flag.
	 *  @param configname The configname.
	 *  @return The master flag value.
	 */
	public Boolean getMaster(String configname);
	
	/**
	 *  Get the daemon flag.
	 *  @param configname The configname.
	 *  @return The daemon flag value.
	 */
	public Boolean getDaemon(String configname);
	
	/**
	 *  Get the autoshutdown flag.
	 *  @param configname The configname.
	 *  @return The autoshutdown flag value.
	 */
	public Boolean getAutoShutdown(String configname);

	/**
	 *  Get the subcomponent names. 
	 */
	public SubcomponentTypeInfo[] getSubcomponentTypes();
	
	/**
	 *  Get the extension types.
	 *  @return An array of extension declarations as specified in the extensions section of the component model.
	 *    The concrete object type depends on the type of the extension (e.g. EnvSupport vs. AGR).
	 */
	public Object[] getExtensionTypes();
}
