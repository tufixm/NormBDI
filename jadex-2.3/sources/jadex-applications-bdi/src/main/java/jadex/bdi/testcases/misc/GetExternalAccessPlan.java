package jadex.bdi.testcases.misc;

import jadex.base.test.TestReport;
import jadex.bdi.runtime.IBDIExternalAccess;
import jadex.bdi.runtime.IBDIInternalAccess;
import jadex.bdi.runtime.Plan;
import jadex.bridge.ComponentIdentifier;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.transformation.annotations.Classname;

import java.util.HashMap;
import java.util.Map;

/**
 *  This plan performs an illegal action. 
 */
public class GetExternalAccessPlan extends Plan
{
	boolean	gotexta	= false;
	Future	done;
	
	/**
	 *  The plan body.
	 */
	public void body()
	{
		// Sub component will not be initialized before wait future is done.
		Future	wait	= new Future();

		// Create component.
		IComponentManagementService ces = (IComponentManagementService)SServiceProvider
			.getServiceUpwards(getServiceContainer(), IComponentManagementService.class).get(this);
		IComponentIdentifier cid = new ComponentIdentifier("ExternalAccessWorker@"+getComponentIdentifier().getName().replace('@', '.'));
		Map	args	= new HashMap();
		args.put("future", wait);
		IFuture init = ces.createComponent(cid.getLocalName(), "jadex/bdi/testcases/misc/ExternalAccessWorker.agent.xml",
			new CreationInfo(null, args, getComponentIdentifier(), false), null);
		final boolean[]	gotexta	= new boolean[3];	// 0: got exception, 1: got access, 2: got belief value.	
		
		// Get and use external access.
		IResultListener	lis	= new DefaultResultListener()
		{
			public void resultAvailable(Object result)
			{
				IBDIExternalAccess exta = (IBDIExternalAccess)result;
				gotexta[0]	= true;
//				System.out.println("Got external access: "+exta);
				
				exta.scheduleStep(new IComponentStep<Void>()
				{
					@Classname("test")
					public IFuture<Void> execute(IInternalAccess ia)
					{
						IBDIInternalAccess bia = (IBDIInternalAccess)ia;
						Object fact = bia.getBeliefbase().getBelief("test").getFact();
						gotexta[1]	= "testfact".equals(fact);
						return IFuture.DONE;
					}
				}).addResultListener(new DelegationResultListener(done));
			}
			
			public void exceptionOccurred(Exception exception)
			{
				// Expected on first call.
			}
		};

		// External access currently accessible in init, as usually component id is not known externally.
//		// External access should not be made available before component has resumed.
//		TestReport	tr	= new TestReport("#1", "No external access before init.");
//		done	= new Future();
//		ces.getExternalAccess(cid).addResultListener(lis);
//		done.get(this);
//		if(gotexta[0])
//			tr.setFailed("Got external access");
//		else
//			tr.setSucceeded(true);
//		getBeliefbase().getBeliefSet("testcap.reports").addFact(tr);
		
		// External access should be made available after component is inited.
		TestReport	tr	= new TestReport("#2", "External access after init.");
		wait.setResult(null);
		init.get(this);
		done	= new Future();
		ces.getExternalAccess(cid).addResultListener(lis);
		done.get(this);
		if(gotexta[0] && gotexta[1])
			tr.setSucceeded(true);
		else
			tr.setFailed("Didn't get external access or belief value.");
		getBeliefbase().getBeliefSet("testcap.reports").addFact(tr);
	}
}
