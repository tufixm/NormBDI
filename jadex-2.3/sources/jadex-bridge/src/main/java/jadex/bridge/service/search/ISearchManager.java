package jadex.bridge.service.search;

import jadex.bridge.service.IService;
import jadex.bridge.service.IServiceProvider;
import jadex.commons.future.IIntermediateFuture;

import java.util.Collection;
import java.util.Map;

/**
 *  Manager for doing searches across service providers.
 */
public interface ISearchManager
{
	/**
	 *  Search for services, starting at the given service provider.
	 *  @param provider	The service provider to start the search at.
	 *  @param decider	The visit decider to select nodes and terminate the search.
	 *  @param selector	The result selector to select matching services and produce the final result. 
	 *  @param services	The local services of the provider (class->list of services).
	 */
	public IIntermediateFuture<IService> searchServices(IServiceProvider provider, IVisitDecider decider, 
		IResultSelector selector, Map<Class<?>, Collection<IService>> services);

	/**
	 *  Get the cache key.
	 *  Needs to identify this element with respect to its important features so that
	 *  two equal elements should return the same key.
	 *  @return	The cache key or null, if results should not be cached.
	 */
	public Object getCacheKey();
	
	/**
	 *  Test if a search must be performed (no cached values allowed).
	 *  @return True, if search must be performed.
	 */
	public boolean isForcedSearch();
	
}
