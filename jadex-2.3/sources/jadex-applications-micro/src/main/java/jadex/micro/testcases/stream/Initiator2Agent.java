package jadex.micro.testcases.stream;

import jadex.base.test.TestReport;
import jadex.base.test.Testcase;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInputConnection;
import jadex.bridge.service.types.message.IMessageService;
import jadex.commons.Tuple2;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Agent;
import jadex.micro.testcases.TestAgent;

import java.util.Collection;
import java.util.Map;

/**
 *  Agent that provides a service with a stream.
 */
@Agent
//@RequiredServices(
//{
//	@RequiredService(name="msgservice", type=IMessageService.class, 
//		binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM)),
//	@RequiredService(name="cms", type=IComponentManagementService.class, 
//		binding=@Binding(scope=RequiredServiceInfo.SCOPE_PLATFORM))
//})
//@ComponentTypes(
//	@ComponentType(name="receiver", filename="jadex/micro/testcases/stream/Receiver2Agent.class")
//)
public class Initiator2Agent extends TestAgent
{
	@Agent
	protected MicroAgent agent;
	
	protected IInputConnection icon;
	
	/**
	 * 
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
	 * 
	 */
	protected IFuture<TestReport> testLocal(int testno)
	{
		return performTest(agent.getComponentIdentifier().getRoot(), testno);
	}
	
	/**
	 * 
	 */
	protected IFuture<TestReport> testRemote(final int testno)
	{
		final Future<TestReport> ret = new Future<TestReport>();
		
		createPlatform(null).addResultListener(agent.createResultListener(
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
	 *  - start a receiver agent
	 *  - create connection
	 */
	protected IFuture<TestReport> performTest(final IComponentIdentifier root, final int testno)
	{
		final Future<TestReport> ret = new Future<TestReport>();

		final Future<TestReport> res = new Future<TestReport>();
		
		ret.addResultListener(new DelegationResultListener<TestReport>(res)
		{
			public void exceptionOccurred(Exception exception)
			{
				TestReport tr = new TestReport("#"+testno, "Tests if streams work");
				tr.setReason(exception.getMessage());
				super.resultAvailable(tr);
			}
		});
		
		final Future<Collection<Tuple2<String, Object>>> resfut = new Future<Collection<Tuple2<String, Object>>>();
		IResultListener<Collection<Tuple2<String, Object>>> reslis = new DelegationResultListener<Collection<Tuple2<String,Object>>>(resfut);
		
		createComponent("jadex/micro/testcases/stream/Receiver2Agent.class", root, reslis)
			.addResultListener(new ExceptionDelegationResultListener<IComponentIdentifier, TestReport>(ret)
		{
			public void customResultAvailable(final IComponentIdentifier cid) 
			{
				IFuture<IMessageService> msfut = agent.getServiceContainer().getRequiredService("msgservice");
				msfut.addResultListener(new ExceptionDelegationResultListener<IMessageService, TestReport>(ret)
				{
					public void customResultAvailable(IMessageService ms)
					{
						ms.createInputConnection(agent.getComponentIdentifier(), cid, null)
							.addResultListener(new ExceptionDelegationResultListener<IInputConnection, TestReport>(ret)
						{
							public void customResultAvailable(final IInputConnection icon) 
							{
								receiveBehavior(testno, icon, resfut).addResultListener(new DelegationResultListener<TestReport>(ret)
								{
									public void customResultAvailable(final TestReport tr)
									{
										destroyComponent(cid).addResultListener(new ExceptionDelegationResultListener<Map<String,Object>, TestReport>(ret)
										{
											public void customResultAvailable(Map<String,Object> result) 
											{
												ret.setResult(tr);
											}
										});
									}
								});
							}
						});
					}
				});
			}
		});
		
		return res;
	}
	
	/**
	 * 
	 */
	protected IFuture<TestReport> receiveBehavior(int testno, final IInputConnection con, IFuture<Collection<Tuple2<String, Object>>> resfut)
	{
		final Future<TestReport> ret = new Future<TestReport>();
		
		final TestReport tr = new TestReport(""+testno, "Test if file is transferred correctly.");
		StreamProviderAgent.read(con).addResultListener(new TestReportListener(tr, ret, Receiver2Agent.getNumberOfBytes()));
			
		return ret;
	}
	
}

