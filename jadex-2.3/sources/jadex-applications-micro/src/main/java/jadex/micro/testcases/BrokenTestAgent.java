package jadex.micro.testcases;

import jadex.base.test.TestReport;
import jadex.base.test.Testcase;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.TimeoutResultListener;
import jadex.bridge.service.types.cms.CreationInfo;
import jadex.bridge.service.types.cms.IComponentManagementService;
import jadex.commons.Tuple2;
import jadex.commons.concurrent.TimeoutException;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.micro.MicroAgent;
import jadex.micro.annotation.Binding;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;
import jadex.micro.annotation.Result;
import jadex.micro.annotation.Results;

import java.util.Collection;

/**
 *  Testing broken components.
 *  
 *  Starts the following components and verifies that they terminate with exception:
 *  - BodyExceptionAgent
 *  - ProtectedBodyAgent
 *  - PojoBodyExceptionAgent
 */
@Description("Testing broken components.")
@Results(@Result(name="testresults", clazz=Testcase.class))
@RequiredServices(@RequiredService(name="cms", type=IComponentManagementService.class, binding=@Binding(scope=Binding.SCOPE_PLATFORM)))
public class BrokenTestAgent extends MicroAgent
{
	/**
	 *  Perform the tests
	 */
	public IFuture<Void> executeBody()
	{
		final Future<Void> ret = new Future<Void>();
		
		final TestReport	tr1	= new TestReport("#1", "Body exception subcomponent.");
		
		testBrokenComponent(BodyExceptionAgent.class.getName()+".class")
			.addResultListener(createResultListener(new IResultListener<Void>()
		{
			public void resultAvailable(Void result)
			{
				tr1.setSucceeded(true);
				next();
			}
			
			public void exceptionOccurred(Exception exception)
			{
				tr1.setFailed(exception.getMessage());
				next();
			}
			
			protected void next()
			{
				final TestReport	tr2	= new TestReport("#2", "Protected body agent.");
				testBrokenComponent(ProtectedBodyAgent.class.getName()+".class")
					.addResultListener(createResultListener(new IResultListener<Void>()
				{
					public void resultAvailable(Void result)
					{
						tr2.setSucceeded(true);
						next();
					}
					
					public void exceptionOccurred(Exception exception)
					{
						tr2.setFailed(exception.getMessage());
						next();
					}
					
					protected void next()
					{
						final TestReport	tr3	= new TestReport("#3", "PojoBodyExceptionAgent");
						testBrokenComponent(PojoBodyExceptionAgent.class.getName()+".class")
							.addResultListener(createResultListener(new IResultListener<Void>()
						{
							public void resultAvailable(Void result)
							{
								tr3.setSucceeded(true);
								next();
							}
							
							public void exceptionOccurred(Exception exception)
							{
								tr3.setFailed(exception.getMessage());
								next();
							}
							
							protected void next()
							{
								setResultValue("testresults", new Testcase(3, new TestReport[]{tr1, tr2, tr3}));
//								killAgent();
								ret.setResult(null);
							}
						}));
					}
				}));
			}
		}));
		
		return ret;
	}

	/**
	 *  Create subcomponent and check if init produces exception.
	 */
	protected IFuture<Void> testBrokenComponent(final String model)
	{
		final Future<Void>	ret	= new Future<Void>();
		IFuture<IComponentManagementService> fut = getRequiredService("cms");
		fut.addResultListener(new ExceptionDelegationResultListener<IComponentManagementService, Void>(ret)
		{
			public void customResultAvailable(final IComponentManagementService cms)
			{
				IResultListener<Collection<Tuple2<String, Object>>> lis = new TimeoutResultListener<Collection<Tuple2<String, Object>>>(3000, 
					getExternalAccess(), new IResultListener<Collection<Tuple2<String, Object>>>()
				{
					public void resultAvailable(Collection<Tuple2<String, Object>> result)
					{
//						System.out.println("res: "+result);
						ret.setException(new RuntimeException("Terminated gracefully."));
					}
					public void exceptionOccurred(Exception exception)
					{
//						System.out.println("ex: "+exception);
						
						// Could already have exception if init has failed.
						if(exception instanceof TimeoutException)
						{
							ret.setExceptionIfUndone(exception);
						}
						else
						{
							ret.setResultIfUndone(null);
						}
					}
				});
				
				cms.createComponent(null, model, new CreationInfo(getComponentIdentifier()), lis)
					.addResultListener(createResultListener(new ExceptionDelegationResultListener<IComponentIdentifier, Void>(ret)
				{
					public void customResultAvailable(IComponentIdentifier result)
					{
					}
				}));
			}
		});
		return ret;
	}
}
