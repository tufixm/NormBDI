package jadex.micro.testcases.timeout;

import jadex.base.test.TestReport;
import jadex.base.test.Testcase;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.ServiceCall;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.component.interceptors.CallAccess;
import jadex.commons.Tuple2;
import jadex.commons.concurrent.TimeoutException;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;
import jadex.micro.testcases.TestAgent;

import java.util.Collection;

/**
 * 
 */
@Agent
@RequiredServices(
{
	@RequiredService(name="ts", type=ITestService.class, binding=@Binding(scope=RequiredServiceInfo.SCOPE_GLOBAL))
})
public class InitiatorAgent extends TestAgent
{
	/**
	 *  Perform the tests.
	 */
	protected IFuture<Void> performTests(final Testcase tc)
	{
		final Future<Void> ret = new Future<Void>();
		
		testLocal(1).addResultListener(agent.createResultListener(new ExceptionDelegationResultListener<TestReport, Void>(ret)
		{
			public void customResultAvailable(TestReport result)
			{
				tc.addReport(result);
				testRemote(2).addResultListener(agent.createResultListener(new ExceptionDelegationResultListener<TestReport, Void>(ret)
				{
					public void customResultAvailable(TestReport result)
					{
						tc.addReport(result);
						ret.setResult(null);
					}
				}));
			}
		}));
		
		return ret;
	}
	
	/**
	 *  Test local.
	 */
	protected IFuture<TestReport> testLocal(final int testno)
	{
		final Future<TestReport> ret = new Future<TestReport>();
		
		performTest(agent.getComponentIdentifier().getRoot(), testno, true)
			.addResultListener(agent.createResultListener(new DelegationResultListener<TestReport>(ret)
		{
			public void customResultAvailable(final TestReport result)
			{
				ret.setResult(result);
			}
		}));
		
		return ret;
	}
	
	/**
	 *  Test remote.
	 */
	protected IFuture<TestReport> testRemote(final int testno)
	{
		final Future<TestReport> ret = new Future<TestReport>();
		
		createPlatform(null).addResultListener(agent.createResultListener(
			new ExceptionDelegationResultListener<IExternalAccess, TestReport>(ret)
		{
			public void customResultAvailable(final IExternalAccess platform)
			{
				performTest(platform.getComponentIdentifier(), testno, false)
					.addResultListener(agent.createResultListener(new DelegationResultListener<TestReport>(ret)
				{
					public void customResultAvailable(final TestReport result)
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
				}));
			}
		}));
		
		return ret;
	}
	
	/**
	 *  Perform the test. Consists of the following steps:
	 *  Create provider agent
	 *  Call methods on it
	 */
	protected IFuture<TestReport> performTest(final IComponentIdentifier root, final int testno, final boolean hassectrans)
	{
		final Future<TestReport> ret = new Future<TestReport>();

		final Future<TestReport> res = new Future<TestReport>();
		
		ret.addResultListener(new DelegationResultListener<TestReport>(res)
		{
			public void exceptionOccurred(Exception exception)
			{
				TestReport tr = new TestReport("#"+testno, "Tests if timeout works.");
				tr.setReason(exception.getMessage());
				super.resultAvailable(tr);
			}
		});
		
		final Future<Collection<Tuple2<String, Object>>> resfut = new Future<Collection<Tuple2<String, Object>>>();
		IResultListener<Collection<Tuple2<String, Object>>> reslis = new DelegationResultListener<Collection<Tuple2<String,Object>>>(resfut);
		
//		System.out.println("root: "+root+" "+SUtil.arrayToString(root.getAddresses()));
		createComponent("jadex/micro/testcases/timeout/ProviderAgent.class", root, reslis)
			.addResultListener(new ExceptionDelegationResultListener<IComponentIdentifier, TestReport>(ret)
		{
			public void customResultAvailable(final IComponentIdentifier cid) 
			{
				callService(cid, testno, 5000).addResultListener(new DelegationResultListener<TestReport>(ret));
			}
			
			public void exceptionOccurred(Exception exception)
			{
				exception.printStackTrace();
				super.exceptionOccurred(exception);
			}
		});
		
		return res;
	}
	
	/**
	 *  Call the service methods.
	 */
	protected IFuture<TestReport> callService(IComponentIdentifier cid, int testno, final long to)
	{
		final Future<TestReport> ret = new Future<TestReport>();
		
		final TestReport tr = new TestReport("#"+testno, "Test if timeout works "+(to==-1? "without ": "with "+to)+" timeout.");
		
		IFuture<ITestService> fut = agent.getServiceContainer().getService(ITestService.class, cid);
		
//		fut.addResultListener(new IResultListener()
//		{
//			public void resultAvailable(Object result)
//			{
//				System.out.println("res: "+result+" "+SUtil.arrayToString(result.getClass().getInterfaces()));
//				try
//				{
//					ITestService ts = (ITestService)result;
//				}
//				catch(Exception e)
//				{
//					e.printStackTrace();
//				}
//			}
//			public void exceptionOccurred(Exception exception)
//			{
//				exception.printStackTrace();
//			}
//		});
		
		fut.addResultListener(new ExceptionDelegationResultListener<ITestService, TestReport>(ret)
		{
			public void customResultAvailable(final ITestService ts)
			{
				// create a service call meta object and set the timeout
				final long start = System.currentTimeMillis();
				if(to!=-1)
				{
//					ServiceCall.setInvocationProperties(to, true);
					ServiceCall call = ServiceCall.getInvocation();
					call.setTimeout(to);
					call.setRealtime(Boolean.TRUE);
					call.setProperty("extra", "somval");
				}				
				
				ts.method("test1").addResultListener(new IResultListener<Void>()
				{
					public void resultAvailable(Void result)
					{
						tr.setFailed("No timeout occurred");
						ret.setResult(tr);
					}
					
					public void exceptionOccurred(Exception exception)
					{
						ServiceCall	next	= CallAccess.getNextInvocation();
						if(next!=null)
						{
							tr.setFailed("User invocation data still available: "+next);
						}
						else if(exception instanceof TimeoutException)
						{
							long diff = System.currentTimeMillis() - (start+to);
							if(diff>=0 && diff<2000) // 2 secs max overdue delay?
							{
								tr.setSucceeded(true);
							}
							else
							{
								tr.setFailed("Timeout difference too high: "+diff);
							}
						}
						else
						{
							tr.setFailed("No timeout occurred");
						}
						ret.setResult(tr);
					}
				});
			}
		});
		return ret;
	}
}
