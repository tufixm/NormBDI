package jadex.bridge.service.component.interceptors;

import jadex.bridge.service.IInternalService;
import jadex.bridge.service.IService;
import jadex.bridge.service.ServiceInvalidException;
import jadex.bridge.service.component.BasicServiceInvocationHandler;
import jadex.bridge.service.component.ServiceInfo;
import jadex.bridge.service.component.ServiceInvocationContext;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

/**
 *  The validation interceptor tests whether a service call is valid.
 *  
 *  Calls isValid() on IService to check if service call is allowed.
 */
public class ValidationInterceptor extends AbstractApplicableInterceptor
{
	//-------- constants --------
	
	/** The static map of subinterceptors (method -> interceptor). */
	protected static Set ALWAYSOK;
	
	static
	{
		try
		{
			ALWAYSOK = new HashSet();
			ALWAYSOK.add(Object.class.getMethod("toString", new Class[0]));
			ALWAYSOK.add(Object.class.getMethod("equals", new Class[]{Object.class}));
			ALWAYSOK.add(Object.class.getMethod("hashCode", new Class[0]));
			ALWAYSOK.add(IService.class.getMethod("getServiceIdentifier", new Class[0]));
			ALWAYSOK.add(IInternalService.class.getMethod("startService", new Class[0]));
			ALWAYSOK.add(IInternalService.class.getMethod("shutdownService", new Class[0]));
			ALWAYSOK.add(IService.class.getMethod("isValid", new Class[0]));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *  Execute the interceptor.
	 *  @param context The invocation context.
	 */
	public IFuture<Void> execute(final ServiceInvocationContext sic)
	{
		final Future<Void> ret = new Future<Void>();
		
		boolean scheduleable = sic.getMethod().getReturnType().equals(IFuture.class) 
		|| sic.getMethod().getReturnType().equals(void.class);

		if(!scheduleable || ALWAYSOK.contains(sic.getMethod()))
		{
			sic.invoke().addResultListener(new DelegationResultListener<Void>(ret));
		}
		else
		{
			// Call isValid() on proxy to execute full interceptor chain.
//			IService ser = (IService)sic.getProxy();
//			IFuture<Boolean>	valid	= ser.isValid();
			
			// Call isValid() directly for speed.
			BasicServiceInvocationHandler	handler	= (BasicServiceInvocationHandler)Proxy.getInvocationHandler(sic.getProxy());
			Object	service	= handler.getService();
			IFuture<Boolean>	valid;
			if(service instanceof IService)
			{
				valid	= ((IService)service).isValid();
			}
			else
			{
				valid	= ((ServiceInfo)service).getManagementService().isValid();
			}
			
			valid.addResultListener(new ExceptionDelegationResultListener<Boolean, Void>(ret)
			{
				public void customResultAvailable(Boolean result)
				{
					if(result.booleanValue())
					{
						sic.invoke().addResultListener(new DelegationResultListener<Void>(ret));
					}
					else
					{
						ret.setException(new ServiceInvalidException(sic.getMethod().getName()));
					}
				}
			});
		}
		
		return ret;
	}
}
