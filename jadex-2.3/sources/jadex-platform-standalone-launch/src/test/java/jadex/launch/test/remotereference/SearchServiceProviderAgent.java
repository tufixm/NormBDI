package jadex.launch.test.remotereference;

import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.commons.future.IFuture;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.Imports;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

/**
 *  Agent providing the remote search service.
 */
@Agent
@Imports("jadex.micro.*")
@RequiredServices(@RequiredService(name="local", type=ILocalService.class, binding=@Binding(scope=RequiredServiceInfo.SCOPE_GLOBAL)))
@ProvidedServices(@ProvidedService(type=ISearchService.class, implementation=@Implementation(expression="((IPojoMicroAgent)$component).getPojoAgent()")))
@Service
public class SearchServiceProviderAgent implements ISearchService
{
	//-------- attributes --------
	
	/** The agent. */
	@Agent
	protected MicroAgent	agent;
	
	public IFuture<ILocalService> searchService(String dummy)
	{
//		System.out.println("searcher");
		IFuture<ILocalService>	ret	= agent.getRequiredService("local");
		return ret;
	}
}
