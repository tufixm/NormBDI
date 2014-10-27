package jadex.bdi.runtime;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentListener;
import jadex.bridge.IExternalAccess;
import jadex.bridge.modelinfo.IModelInfo;
import jadex.bridge.service.IServiceContainer;
import jadex.bridge.service.types.cms.IComponentDescription;
import jadex.commons.future.IFuture;

import java.util.logging.Logger;


/**
 *  A capability is a self-contained agent module
 *  as specified in  an agent definition file (ADF).
 */
public interface ICapability	extends IElement
{
	/**
	 *  Get the scope.
	 *  Method with IExternalAccess return value included
	 *  for compatibility with IInternalAccess. 
	 *  @return The scope.
	 */
	public IExternalAccess getExternalAccess();

	/**
	 *  Get the scope.
	 *  @return The scope.
	 */
	public IBDIExternalAccess getBDIExternalAccess();

	/**
	 *  Get the parent (if any).
	 *  @return The parent.
	 */
	public IExternalAccess getParentAccess();

	/**
	 *  Get the belief base.
	 *  @return The belief base.
	 */
	public IBeliefbase getBeliefbase();

	/**
	 *  Get the goal base.
	 *  @return The goal base.
	 */
	public IGoalbase getGoalbase();

	/**
	 *  Get the plan base.
	 *  @return The plan base.
	 */
	public IPlanbase getPlanbase();

	/**
	 *  Get the event base.
	 *  @return The event base.
	 */
	public IEventbase getEventbase();

	/**
	 * Get the expression base.
	 * @return The expression base.
	 */
	public IExpressionbase getExpressionbase();

//	/**
//	 * Get the property base.
//	 * @return The property base.
//	 */
//	public IPropertybase getPropertybase();

	/**
	 *  Register a subcapability.
	 *  @param subcap	The subcapability.
	 * /
	public void	registerSubcapability(IMCapabilityReference subcap);

	/**
	 *  Deregister a subcapability.
	 *  @param subcap	The subcapability.
	 * /
	public void	deregisterSubcapability(IMCapabilityReference subcap);*/

	/**
	 *  Get the logger.
	 *  @return The logger.
	 */
	public Logger getLogger();

	/**
	 * Get the agent model.
	 * @return The agent model.
	 */
	public IModelInfo getAgentModel();

	/**
	 * Get the capability model.
	 * @return The capability model.
	 */
	public IModelInfo getModel();

	/**
	 * Get the agent name.
	 * @return The agent name.
	 */
	public String getAgentName();

	/**
	 * Get the configuration name.
	 * @return The configuration name.
	 */
	public String getConfigurationName();

	/**
	 * Get the agent identifier.
	 * @return The agent identifier.
	 */
	public IComponentIdentifier	getComponentIdentifier();

	/**
	 * Get the component description.
	 * @return The component description.
	 */
	public IComponentDescription	getComponentDescription();

	/**
	 *  Get the platform specific agent object.
	 *  Allows to do platform specific things.
	 *  @return The agent object.
	 */
	public Object	getPlatformComponent();

	/**
	 *  Get the container
	 *  @return The container.
	 */
	public IServiceContainer getServiceContainer();
	
	/**
	 *  Get the current time.
	 *  The time unit depends on the currently running clock implementation.
	 *  For the default system clock, the time value adheres to the time
	 *  representation as used by {@link System#currentTimeMillis()}, i.e.,
	 *  the value of milliseconds passed since 0:00 'o clock, January 1st, 1970, UTC.
	 *  For custom simulation clocks, arbitrary representations can be used.
	 *  @return The current time.
	 */
	public long getTime();

	/**
	 *  Get the classloader.
	 *  @return The classloader.
	 */
	public ClassLoader getClassLoader();
	
	/**
	 *  Kill the agent.
	 */
	public IFuture killAgent();
	
//	/**
//	 *  Get the application context.
//	 *  @return The application context (or null).
//	 */
//	public IApplicationContext getApplicationContext();

	/**
	 *  Add an agent listener
	 *  @param listener The listener.
	 */
	public IFuture addComponentListener(IComponentListener listener);
	
	/**
	 *  Add an agent listener
	 *  @param listener The listener.
	 */
	public IFuture removeComponentListener(IComponentListener listener);
	
	/**
	 *  Get subcapability names.
	 *  @return The future with array of subcapability names.
	 */
	public String[]	getSubcapabilityNames();
	
	/**
	 *  Get a subcapability.
	 *  @param name The capability name.
	 *  @return The capability.
	 */
	public ICapability	getSubcapability(String name);
	
//	/**
//	 *  Get a required service.
//	 *  @return The service.
//	 */
//	public IFuture getRequiredService(final String name);
//	
//	/**
//	 *  Get a required services.
//	 *  @return The services.
//	 */
//	public IFuture getRequiredServices(final String name);
}