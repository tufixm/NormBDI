package jadex.micro.testcases;

import jadex.base.test.TestReport;
import jadex.base.test.Testcase;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.annotation.Value;
import jadex.bridge.service.component.IServiceInvocationInterceptor;
import jadex.bridge.service.component.ServiceInvocationContext;
import jadex.commons.future.DefaultResultListener;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;
import jadex.micro.annotation.Result;
import jadex.micro.annotation.Results;

import java.util.ArrayList;
import java.util.List;

/**
 *  Testing if required services can be equipped with interceptors.
 */
@Description("Testing if required services can be equipped with interceptors.")
@Results(@Result(name="testresults", clazz=Testcase.class))
@ProvidedServices(@ProvidedService(name="aservice", type=IAService.class, implementation=
	@Implementation(expression="$component", interceptors=@Value("$component.provinter"))))
@RequiredServices(@RequiredService(name="aservice", type=IAService.class, 
	binding=@Binding(scope="local", interceptors=@Value("$component.reqinter"))))
@Service(IAService.class)
public class InterceptorAgent extends MicroAgent implements IAService
{	
	public SimpleInterceptor provinter = new SimpleInterceptor();
	public SimpleInterceptor reqinter = new SimpleInterceptor();
	
	/**
	 *  Just finish the test by setting the result and killing the agent.
	 */
	public IFuture<Void> executeBody()
	{
		final Future<Void> ret = new Future<Void>();
		
		final List testresults = new ArrayList();
		performProvidedServiceTest(testresults).addResultListener(createResultListener(new DelegationResultListener(ret)
		{
			public void customResultAvailable(Object result)
			{
				performRequiredServiceTest(testresults).addResultListener(createResultListener(new DelegationResultListener(ret)
				{
					public void customResultAvailable(Object result)
					{
//						System.out.println("testresults: "+testresults);
						TestReport[] tr = (TestReport[])testresults.toArray(new TestReport[testresults.size()]);
						setResultValue("testresults", new Testcase(tr.length, tr));
//						killAgent();
						ret.setResult(null);
					}
				}));
			}
		}));
		
		return ret;
	}
	
	/**
	 *  Perform test for provided service.
	 */
	public IFuture performProvidedServiceTest(final List testresults)
	{
		final Future ret = new Future();
		IAService ser = (IAService)getServiceContainer().getProvidedService("aservice");
		ser.test().addResultListener(new DelegationResultListener(ret)
		{
			public void customResultAvailable(Object result)
			{
				TestReport tr = new TestReport("#1", "Provided service test.");
				if(provinter.getCnt()==1)
				{
					tr.setSucceeded(true);
				}
				else
				{
					tr.setReason("Wrong interceptor count: "+provinter.getCnt());
				}
				testresults.add(tr);
				ret.setResult(null);
			}
		});
		return ret;
	}
	
	/**
	 *  Perform test for required service.
	 */
	public IFuture performRequiredServiceTest(final List testresults)
	{
		final Future ret = new Future();
		getRequiredService("aservice").addResultListener(new DefaultResultListener()
		{
			public void resultAvailable(Object result)
			{
				IAService ser = (IAService)result;
				ser.test().addResultListener(new DefaultResultListener()
				{
					public void resultAvailable(Object result)
					{
						TestReport tr = new TestReport("#2", "Required service test.");
						if(reqinter.getCnt()==1)
						{
							tr.setSucceeded(true);
						}
						else
						{
							tr.setReason("Wrong interceptor count: "+reqinter.getCnt());
						}
						testresults.add(tr);
						ret.setResult(null);
					}
				});
			}
		});
		return ret;
	}
	
	/**
	 *  Init service method.
	 */
	public IFuture<Void> test()
	{
//		System.out.println("called service");
		return IFuture.DONE;
	}
}

/**
 *  Simple interceptor that remembers how often it was called.
 */
class SimpleInterceptor implements IServiceInvocationInterceptor
{
	protected int interceptcnt;

	/**
	 *  Test if the interceptor is applicable.
	 *  @return True, if applicable.
	 */
	public boolean isApplicable(ServiceInvocationContext context)
	{
		try
		{
			return context.getMethod().getName().equals("test");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 *  Execute the interceptor.
	 *  @param context The invocation context.
	 */
	public IFuture<Void> execute(ServiceInvocationContext context)
	{
		interceptcnt++;
//		System.out.println("exe: "+interceptcnt);
		return context.invoke();
	}
	
	/**
	 *  Get the interceptor cnt.
	 */
	public int getCnt()
	{
		return interceptcnt;
	}
}