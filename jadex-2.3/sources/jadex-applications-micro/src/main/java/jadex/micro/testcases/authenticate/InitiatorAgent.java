package jadex.micro.testcases.authenticate;

import jadex.base.test.TestReport;
import jadex.base.test.Testcase;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.types.security.ISecurityService;
import jadex.commons.Tuple2;
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
		
		agent.getServiceContainer().searchService(ISecurityService.class, RequiredServiceInfo.SCOPE_PLATFORM)
			.addResultListener(new ExceptionDelegationResultListener<ISecurityService, TestReport>(ret)
		{
			public void customResultAvailable(ISecurityService sec)
			{
				sec.addVirtual("testuser", agent.getComponentIdentifier().getPlatformPrefix())
					.addResultListener(new ExceptionDelegationResultListener<Void, TestReport>(ret)
				{
					public void customResultAvailable(Void result)
					{
						performTest(agent.getComponentIdentifier().getRoot(), testno)
							.addResultListener(agent.createResultListener(new DelegationResultListener<TestReport>(ret)
						{
							public void customResultAvailable(final TestReport result)
							{
								ret.setResult(result);
							}
						}));
					}
				});
			}
		});
		
		return ret;
	}
	
	/**
	 *  Test remote.
	 */
	protected IFuture<TestReport> testRemote(final int testno)
	{
		final Future<TestReport> ret = new Future<TestReport>();
		
		createPlatform(new String[]{"-virtualnames",
			"jadex.commons.SUtil.createHashMap(new String[]{\"testuser\"}, new Object[]{jadex.commons.SUtil.createHashSet(new String[]{\"testcases\"})})"})
			.addResultListener(agent.createResultListener(
			new ExceptionDelegationResultListener<IExternalAccess, TestReport>(ret)
		{
			public void customResultAvailable(final IExternalAccess platform)
			{
				performTest(platform.getComponentIdentifier(), testno)
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
	protected IFuture<TestReport> performTest(final IComponentIdentifier root, final int testno)
	{
		final Future<TestReport> ret = new Future<TestReport>();

		final Future<TestReport> res = new Future<TestReport>();
		
		ret.addResultListener(new DelegationResultListener<TestReport>(res)
		{
			public void exceptionOccurred(Exception exception)
			{
				TestReport tr = new TestReport("#"+testno, "Tests if authentication works.");
				tr.setReason(exception.getMessage());
				super.resultAvailable(tr);
			}
		});
		
		final Future<Collection<Tuple2<String, Object>>> resfut = new Future<Collection<Tuple2<String, Object>>>();
		IResultListener<Collection<Tuple2<String, Object>>> reslis = new DelegationResultListener<Collection<Tuple2<String,Object>>>(resfut);
		
//		System.out.println("root: "+root+" "+SUtil.arrayToString(root.getAddresses()));
		createComponent("jadex/micro/testcases/authenticate/ProviderAgent.class", root, reslis)
			.addResultListener(new ExceptionDelegationResultListener<IComponentIdentifier, TestReport>(ret)
		{
			public void customResultAvailable(final IComponentIdentifier cid) 
			{
				callService(cid, testno).addResultListener(new DelegationResultListener<TestReport>(ret));
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
	protected IFuture<TestReport> callService(final IComponentIdentifier cid, int testno)
	{
		final Future<TestReport> ret = new Future<TestReport>();
		
		final TestReport tr = new TestReport("#"+testno, "Test if authentication works.");
		
		IFuture<ITestService> fut = agent.getServiceContainer().getService(ITestService.class, cid);
		fut.addResultListener(new ExceptionDelegationResultListener<ITestService, TestReport>(ret)
		{
			public void customResultAvailable(final ITestService ts)
			{
				ts.method("test1").addResultListener(new IResultListener<Void>()
				{
					public void resultAvailable(Void result)
					{
						tr.setSucceeded(true);
						ret.setResult(tr);
					}
					
					public void exceptionOccurred(Exception exception)
					{
						tr.setFailed("Exception occurred: "+exception);
						ret.setResult(tr);
					}
				});
			}
		});
		return ret;
	}
	
//	/**
//	 *  Call the service methods.
//	 */
//	protected IFuture<TestReport> callService(final IComponentIdentifier cid, int testno)
//	{
//		final Future<TestReport> ret = new Future<TestReport>();
//		
//		final TestReport tr = new TestReport("#"+testno, "Test if authentication works.");
//		
//		IFuture<ITestService> fut = agent.getServiceContainer().getService(ITestService.class, cid);
//		
//		fut.addResultListener(new ExceptionDelegationResultListener<ITestService, TestReport>(ret)
//		{
//			public void customResultAvailable(final ITestService ts)
//			{
//				IFuture<ISecurityService> fut = agent.getServiceContainer().searchService(ISecurityService.class, RequiredServiceInfo.SCOPE_PLATFORM);
//				fut.addResultListener(new ExceptionDelegationResultListener<ISecurityService, TestReport>(ret)
//				{
//					public void customResultAvailable(ISecurityService ss)
//					{
//						String classname = ITestService.class.getName();
//						String methodname = "method";
//						Object[] args = new Object[]{"test1"};
//						Object[] t = new Object[]{agent.getComponentIdentifier().getPlatformPrefix(), classname, methodname, args};
//						final byte[] content = BinarySerializer.objectToByteArray(t, null);
//						
//						ss.signCall(content).addResultListener(new ExceptionDelegationResultListener<byte[], TestReport>(ret)
//						{
//							public void customResultAvailable(byte[] signed)
//							{
//								System.out.println("Signed: "+SUtil.arrayToString(signed));
//								
//								// create a service call meta object and set the timeout
//								ServiceCall call = ServiceCall.getInvocation();
//								call.setProperty(Authenticated.AUTHENTICATED, signed);
//								ts.method("test1").addResultListener(new IResultListener<Void>()
//								{
//									public void resultAvailable(Void result)
//									{
//										tr.setSucceeded(true);
//										ret.setResult(tr);
//									}
//									
//									public void exceptionOccurred(Exception exception)
//									{
//										tr.setFailed("Exception occurred: "+exception);
//										ret.setResult(tr);
//									}
//								});
//							}
//						});
//					}
//				});
//			}
//		});
//		return ret;
//	}
}
