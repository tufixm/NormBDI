package jadex.bdi.testcases.semiautomatic;

import jadex.bdi.runtime.Plan;

/**
 *  Plan that adds a reasoning interceptor to the service and invokes it.
 */
public class InterceptorPlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
//		IServiceInvocationInterceptor ic = new IServiceInvocationInterceptor()
//		{
//			public boolean isApplicable(ServiceInvocationContext context)
//			{
//				return context.getMethod().getName().equals("printHello");
//			}
//			
//			public IFuture execute(final ServiceInvocationContext sic)
//			{
//				final Future ret = new Future();
//				final IGoal g = createGoal("reasoncall");
//				g.addGoalListener(new IGoalListener()
//				{
//					public void goalFinished(AgentEvent ae)
//					{
//						if(((Boolean)g.getParameter("execute").getValue()).booleanValue())
//						{
//							sic.invoke().addResultListener(new DelegationResultListener(ret));
//						}
//						else
//						{
//							ret.setException(new ServiceInvalidException(sic.getMethod().getName()));
//						}
//					}
//					
//					public void goalAdded(AgentEvent ae)
//					{
//					}
//				});
//				dispatchTopLevelGoal(g);
//				return ret;
//			}
//		};
		
//		IPrintHelloService ps = (IPrintHelloService)getServiceContainer().getProvidedService("printservice");
//		IServiceInvocationInterceptor[] ics = getServiceContainer().getInterceptors(ps);
//		getServiceContainer().addInterceptor(ic, ps, ics.length-1);

		for(int i=0; i<3; i++)
			callPrintService();
	}
	
	/**
	 *  Invoke the print service.
	 */
	protected void callPrintService()
	{
		try
		{
			IPrintHelloService ps = (IPrintHelloService)getServiceContainer().getProvidedService("printservice");
			ps.printHello().get(this);
		}
		catch(Exception e)
		{
			System.out.println("Could not call service: "+e);
		}
	}
}
