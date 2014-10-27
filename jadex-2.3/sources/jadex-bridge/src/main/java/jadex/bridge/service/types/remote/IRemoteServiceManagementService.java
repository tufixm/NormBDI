package jadex.bridge.service.types.remote;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.search.IResultSelector;
import jadex.bridge.service.search.ISearchManager;
import jadex.bridge.service.search.IVisitDecider;
import jadex.commons.future.IFuture;


/**
 *  Interface for remote management service.
 */
public interface IRemoteServiceManagementService
{	
	/**
	 *  Get service proxies from a remote component.
	 *  (called from arbitrary components)
	 *  @param cid Component id that is used to start the search.
	 *  @param manager The search manager.
	 *  @param decider The visit decider.
	 *  @param selector The result selector.
	 *  @return Collection or single result (i.e. service proxies). 
	 */
	public IFuture<Object> getServiceProxies(IComponentIdentifier cid, 
		ISearchManager manager, IVisitDecider decider, IResultSelector selector);
	
	/**
	 *  Get a service proxy from a remote component.
	 *  (called from arbitrary components)
	 *  @param cid	The remote provider id.
	 *  @param service	The service type.
	 *  @param scope	The search scope. 
	 *  @return The service proxy.
	 */
	public <T> IFuture<T> getServiceProxy(IComponentIdentifier cid, Class<T> service, String scope);
	
	/**
	 *  Get all service proxies from a remote component.
	 *  (called from arbitrary components)
	 *  @param cid	The remote provider id.
	 *  @param service	The service type.
	 *  @param scope	The search scope. 
	 *  @return The service proxy.
	 */
	public <T> IFuture<T> getServiceProxies(IComponentIdentifier cid, Class<T> service, String scope);

	/**
	 *  Get all declared service proxies from a remote component.
	 *  (called from arbitrary components)
	 *  @param cid The remote provider id.
	 *  @param service The service type.
	 *  @return The service proxy.
	 */
	public IFuture<Object> getDeclaredServiceProxies(IComponentIdentifier cid);
	
	/**
	 *  Get an external access proxy from a remote component.
	 *  (called from arbitrary components)
	 *  @param cid Component target id.
	 *  @return External access of remote component. 
	 */
	public IFuture<IExternalAccess> getExternalAccessProxy(IComponentIdentifier cid);
}
