package jadex.micro.testcases.terminate;

import jadex.base.Starter;
import jadex.base.test.TestReport;
import jadex.base.test.Testcase;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IResourceIdentifier;
import jadex.bridge.LocalResourceIdentifier;
import jadex.bridge.ResourceIdentifier;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.bridge.service.types.remote.RemoteException;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.FutureTerminatedException;
import jadex.commons.future.IFuture;
import jadex.commons.future.IIntermediateFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.future.ITerminableFuture;
import jadex.commons.future.IntermediateExceptionDelegationResultListener;
import jadex.commons.future.IntermediateFuture;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.Result;
import jadex.micro.annotation.Results;

import java.util.Collection;

/**
 *  The invoker agent tests if futures can be terminated
 *  in local and remote cases.
 */
@Agent
@Results(@Result(name="testresults", clazz=Testcase.class))
@Description("The invoker agent tests if futures can be terminated " +
	"in local and remote cases.")
public class InvokerAgent
{
	//-------- attributes --------
	
	@Agent
	protected MicroAgent agent;

	//-------- methods --------
	
	/**
	 *  The agent body.
	 */
	@AgentBody
	public void body()
	{
		final Testcase tc = new Testcase();
		tc.setTestCount(4);
		
		final Future<Void> ret = new Future<Void>();
		ret.addResultListener(agent.createResultListener(new IResultListener<Void>()
		{
			public void resultAvailable(Void result)
			{
//				System.out.println("tests finished: "+tc);
				agent.setResultValue("testresults", tc);
				agent.killAgent();				
			}
			public void exceptionOccurred(Exception exception)
			{
				tc.addReport(new TestReport("#0", "Unexpected exception", false, exception.toString()));
//				System.out.println("tests finished: "+tc);
				agent.setResultValue("testresults", tc);
				agent.killAgent();
			}
		}));
		
		testLocal(1, 100).addResultListener(new ExceptionDelegationResultListener<Collection<TestReport>, Void>(ret)
		{
			public void customResultAvailable(Collection<TestReport> result)
			{
				for(TestReport rep: result)
				{
					tc.addReport(rep);
				}
				
				testRemote(3, 1000).addResultListener(new ExceptionDelegationResultListener<Collection<TestReport>, Void>(ret)
				{
					public void customResultAvailable(Collection<TestReport> result)
					{
						for(TestReport rep: result)
						{
							tc.addReport(rep);
						}
						
						ret.setResult(null);
					}
				});
			}
		});
	}
	
	/**
	 *  Test if local intermediate results are correctly delivered
	 *  (not as bunch when finished has been called). 
	 */
	protected IFuture<Collection<TestReport>> testLocal(int testno, long delay)
	{
		return performTest(agent.getComponentIdentifier().getRoot(), testno, delay);
	}
	
	/**
	 *  Test if remote intermediate results are correctly delivered
	 *  (not as bunch when finished has been called). 
	 */
	protected IFuture<Collection<TestReport>> testRemote(final int testno, final long delay)
	{
		final Future<Collection<TestReport>> ret = new Future<Collection<TestReport>>();
		
		// Start platform
		String url	= "new String[]{\"../jadex-applications-micro/target/classes\"}";	// Todo: support RID for all loaded models.
//		String url	= process.getModel().getResourceIdentifier().getLocalIdentifier().getUrl().toString();
		Starter.createPlatform(new String[]{"-libpath", url, "-platformname", agent.getComponentIdentifier().getPlatformPrefix()+"_*",
			"-saveonexit", "false", "-welcome", "false", "-autoshutdown", "false", "-awareness", "false",
//			"-logging_level", "java.util.logging.Level.INFO",
			"-gui", "false",
//			"-usepass", "false",
			"-simulation", "false", "-printpass", "false"
		}).addResultListener(agent.createResultListener(
			new ExceptionDelegationResultListener<IExternalAccess, Collection<TestReport>>(ret)
		{
			public void customResultAvailable(final IExternalAccess platform)
			{
				performTest(platform.getComponentIdentifier(), testno, delay)
					.addResultListener(new DelegationResultListener<Collection<TestReport>>(ret)
				{
					public void customResultAvailable(final Collection<TestReport> result)
					{
						platform.killComponent();
//							.addResultListener(new ExceptionDelegationResultListener<Map<String, Object>, TestReport>(ret)
//						{
//							public void customResultAvailable(Map<String, Object> v)
//							{
//								ret.setResult(result);
//							}
//						});
						ret.setResult(result);
					}
				});
			}
		}));
		
		return ret;
	}
	
	/**
	 *  Perform the test. Consists of the following steps:
	 *  - start an agent that offers the service
	 *  - invoke the service
	 *  - wait with intermediate listener for results 
	 */
	protected IIntermediateFuture<TestReport> performTest(final IComponentIdentifier root, final int testno, final long delay)
	{
		final IntermediateFuture<TestReport> ret = new IntermediateFuture<TestReport>();

		// Start service agent
		agent.getServiceContainer().getService(IComponentManagementService.class, root)
			.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, Collection<TestReport>>(ret)
		{
			public void customResultAvailable(final IComponentManagementService cms)
			{
				IResourceIdentifier	rid	= new ResourceIdentifier(
					new LocalResourceIdentifier(root, agent.getModel().getResourceIdentifier().getLocalIdentifier().getUrl()), null);
				
				cms.createComponent(null, "jadex/micro/testcases/terminate/TerminableProviderAgent.class", new CreationInfo(rid), null)
					.addResultListener(agent.createResultListener(new ExceptionDelegationResultListener<IComponentIdentifier, Collection<TestReport>>(ret)
				{	
					public void customResultAvailable(final IComponentIdentifier cid)
					{
						ret.addResultListener(new IResultListener<Collection<TestReport>>()
						{
							public void resultAvailable(Collection<TestReport> result)
							{
								cms.destroyComponent(cid);
							}
							public void exceptionOccurred(Exception exception)
							{
								cms.destroyComponent(cid);
							}
						});
						
//						System.out.println("cid is: "+cid);
						agent.getServiceContainer().getService(ITerminableService.class, cid)
							.addResultListener(new ExceptionDelegationResultListener<ITerminableService, Collection<TestReport>>(ret)
						{
							public void customResultAvailable(final ITerminableService service)
							{
								testTerminate(testno, service, delay).addResultListener(
									new ExceptionDelegationResultListener<TestReport, Collection<TestReport>>(ret)
								{
									public void customResultAvailable(TestReport result)
									{
										ret.addIntermediateResult(result);
										testTerminateAction(testno+1, service, delay).addResultListener(
											new ExceptionDelegationResultListener<TestReport, Collection<TestReport>>(ret)
										{
											public void customResultAvailable(TestReport result)
											{
												ret.addIntermediateResult(result);
												ret.setFinished();
											}
										});
									}
								});
							}
						});
					}
				}));
			}	
		});
		
		return ret;
	}
	
	/**
	 *  Test terminating a future.
	 */
	protected IFuture<TestReport>	testTerminate(int testno, ITerminableService service, long delay)
	{
//		System.out.println(agent.getComponentIdentifier()+": testTerminate1");
		
		final Future<Void> tmp = new Future<Void>();
		ITerminableFuture<String> fut = service.getResult(delay);
		fut.addResultListener(new IResultListener<String>()
		{
			public void resultAvailable(String result)
			{
//				System.out.println(agent.getComponentIdentifier()+": testTerminate2");
				tmp.setException(new RuntimeException("Termination did not occur: "+result));
			}
			public void exceptionOccurred(Exception exception)
			{
//				System.out.println(agent.getComponentIdentifier()+": testTerminate3");
				if(exception instanceof FutureTerminatedException)
				{
					tmp.setResult(null);
				}
				else
				{
					tmp.setException(new RuntimeException("Wrong exception occurred: "+exception));
				}
			}
		});
		fut.terminate();

		final Future<TestReport>	ret	= new Future<TestReport>();
		final TestReport tr = new TestReport("#"+testno, "Tests if terminating future works");
		tmp.addResultListener(new ExceptionDelegationResultListener<Void, TestReport>(ret)
		{
			public void customResultAvailable(Void result)
			{
//				System.out.println(agent.getComponentIdentifier()+": testTerminate4");
				tr.setSucceeded(true);
				ret.setResult(tr);
			}
			public void exceptionOccurred(Exception exception)
			{
//				System.out.println(agent.getComponentIdentifier()+": testTerminate5");
				tr.setFailed(exception.getMessage());
				ret.setResult(tr);
			}
		});
		return ret;
	}

	
	/**
	 *  Test if terminate action is called.
	 */
	protected IFuture<TestReport>	testTerminateAction(int testno, ITerminableService service, long delay)
	{
//		System.out.println(agent.getComponentIdentifier()+": testTerminateAction1");
		
		final Future<Void> tmp = new Future<Void>();
		
		final ITerminableFuture<String> fut = service.getResult(delay);
		service.terminateCalled().addResultListener(new IntermediateExceptionDelegationResultListener<Void, Void>(tmp)
		{
			public void intermediateResultAvailable(Void result)
			{
				fut.terminate();
			}
			public void customResultAvailable(Collection<Void> result)
			{
//				System.out.println(agent.getComponentIdentifier()+": testTerminateAction2");
				tmp.setResult(null);
			}
			public void finished()
			{
//				System.out.println(agent.getComponentIdentifier()+": testTerminateAction3");
				tmp.setResult(null);
			}
		});

		final Future<TestReport>	ret	= new Future<TestReport>();
		final TestReport tr = new TestReport("#"+testno, "Tests if terminating action of future is called");
		tmp.addResultListener(new ExceptionDelegationResultListener<Void, TestReport>(ret)
		{
			public void customResultAvailable(Void result)
			{
//				System.out.println(agent.getComponentIdentifier()+": testTerminateAction4");
				tr.setSucceeded(true);
				ret.setResult(tr);
			}
			public void exceptionOccurred(Exception exception)
			{
//				System.out.println(agent.getComponentIdentifier()+": testTerminateAction5");
				tr.setFailed(exception.getMessage());
				ret.setResult(tr);
			}
		});
		return ret;
	}
}
