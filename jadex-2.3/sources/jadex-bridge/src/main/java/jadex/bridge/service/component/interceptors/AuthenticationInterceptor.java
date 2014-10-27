package jadex.bridge.service.component.interceptors;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.bridge.ServiceCall;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Authenticated;
import jadex.bridge.service.component.ServiceInvocationContext;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.security.ISecurityService;
import jadex.commons.SUtil;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.transformation.binaryserializer.BinarySerializer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

/**
 *  Interceptor that can be used to realize authenticated end-to-end communication.
 *  - generates a signed version of the message that is attached to it (in non-functional properties)
 *  - verifies that a call is authenticated by checking the singed version with public key of the sender
 */
public class AuthenticationInterceptor extends AbstractLRUApplicableInterceptor
{
	//-------- attributes --------
	
	/** The external access. */
	protected IExternalAccess ea;
	
	/** The mode (send or receive). */
	protected boolean send;
	
	//-------- constructors --------
	
	/**
	 *  Create a new AuthenticationInterceptor.
	 */
	public AuthenticationInterceptor(IExternalAccess ea, boolean send)
	{
		this.ea = ea;
		this.send = send;
	}

	//-------- constructors --------
	
	/**
	 *  Test if the interceptor is applicable.
	 *  @return True, if applicable.
	 */
	public boolean customIsApplicable(ServiceInvocationContext context)
	{
		boolean ret = false;
		Annotation[] anns = context.getMethod().getDeclaringClass().getAnnotations();
		for(int i=0; !ret && i<anns.length; i++)
		{
			ret = isAuthenticated(anns[i]);
		}
		if(!ret)
		{
			Annotation[] methodannos = context.getMethod().getAnnotations();
			for(int i=0; !ret && i<methodannos.length; i++)
			{
				ret = isAuthenticated(methodannos[i]);
			}
		}
		
		return ret;
	}
	
	/**
	 *  Check if an annotation belongs to the supported
	 *  types of pre/postconditions.
	 */
	protected boolean isAuthenticated(Annotation anno)
	{
		return anno instanceof Authenticated;
	}
	
	/**
	 *  Execute the interceptor.
	 *  @param context The invocation context.
	 */
	public IFuture<Void> execute(final ServiceInvocationContext context)
	{
		final Future<Void> ret = new Future<Void>();
		
		if(send)
		{
			createAuthentication(context).addResultListener(new DelegationResultListener<Void>(ret)
			{
				public void customResultAvailable(Void result)
				{
					context.invoke().addResultListener(new DelegationResultListener<Void>(ret));
				}
			});
		}
		else
		{
			checkAuthentication(context).addResultListener(new DelegationResultListener<Void>(ret)
			{
				public void customResultAvailable(Void result)
				{
					context.invoke().addResultListener(new DelegationResultListener<Void>(ret));
				}
			});
		}
		
		return ret;
	}
	
	/**
	 *  Check the authentication.
	 */
	protected IFuture<Void> createAuthentication(final ServiceInvocationContext context)
	{
		final Future<Void> ret = new Future<Void>();
		
		String classname = context.getMethod().getDeclaringClass().getName();
		String methodname = context.getMethod().getName();
		Object[] args = context.getArgumentArray();
		Object[] t = new Object[]{context.getCaller().getPlatformPrefix(), classname, methodname, args};
		final byte[] content = BinarySerializer.objectToByteArray(t, null);
		
		SServiceProvider.getService(ea.getServiceProvider(), ISecurityService.class, RequiredServiceInfo.SCOPE_PLATFORM)
			.addResultListener(new ExceptionDelegationResultListener<ISecurityService, Void>(ret)
		{
			public void customResultAvailable(ISecurityService sser)
			{
				sser.signCall(content).addResultListener(new ExceptionDelegationResultListener<byte[], Void>(ret)
				{
					public void customResultAvailable(byte[] signed)
					{
//						System.out.println("Signed: "+SUtil.arrayToString(signed));
						
						// get service call meta object and set the timeout
						context.getServiceCall().setProperty(Authenticated.AUTHENTICATED, signed);		
						ret.setResult(null);
					}
				});
			}
		});
	
		return ret;
	}
	
	/**
	 *  Check the authentication.
	 */
	protected IFuture<Void> checkAuthentication(final ServiceInvocationContext context)
	{
		final Future<Void> ret = new Future<Void>();
		
		final ServiceCall call = ServiceCall.getCurrentInvocation();
		final IComponentIdentifier caller = call.getCaller();
		final String callername = caller.getPlatformPrefix();
		final byte[] signed = (byte[])call.getProperty(Authenticated.AUTHENTICATED);
		
		if(signed==null)
		{
			ret.setException(new SecurityException("No authentication info provided: "+context.getMethod().getName()));
		}
		else
		{
			try
			{
				// Find allowed caller names by inspecting the implementation annotation
				// todo: make this more configurable somehow

				// try to find implementation method annotation
				Class<?> targetcl = context.getTargetObject().getClass();
				Method targetm = targetcl.getDeclaredMethod(context.getMethod().getName(), context.getMethod().getParameterTypes());
				Authenticated au = targetm.getAnnotation(Authenticated.class);
				
				// if not available use interface method anno
				if(au==null)
				{
					au = context.getMethod().getAnnotation(Authenticated.class);
					if(au.names().length==0 && au.virtuals().length==0)
						au = null;
				}
				
				// if not available use implementation service anno
				if(au==null)
				{
					au = targetcl.getAnnotation(Authenticated.class);
				}
				
				// if not available use interface service anno
				if(au==null)
				{
					au = context.getMethod().getClass().getAnnotation(Authenticated.class);
				}
				
				Set<String> allowed = SUtil.arrayToSet(au.names());
				if(!allowed.contains(callername))
				{
					// if not contained in direct names check virtual name mappings
					final String[] virtuals = au.virtuals();
					SServiceProvider.getService(ea.getServiceProvider(), ISecurityService.class, RequiredServiceInfo.SCOPE_PLATFORM)
						.addResultListener(new ExceptionDelegationResultListener<ISecurityService, Void>(ret)
					{
						public void customResultAvailable(ISecurityService sser)
						{
							sser.checkVirtual(virtuals, callername).addResultListener(new DelegationResultListener<Void>(ret)
							{
								public void customResultAvailable(Void result)
								{
									// In principle allowed caller, now has to be authenticated
									// todo: timepoint
									internalCheck(context, callername, signed).addResultListener(new DelegationResultListener<Void>(ret));
								}
							});
						}
					});
				}
				else
				{
					// In principle allowed caller, now has to be authenticated
					// todo: timepoint
					internalCheck(context, callername, signed).addResultListener(new DelegationResultListener<Void>(ret));
				}
			}
			catch(Exception e)
			{
				ret.setException(new SecurityException(e));
			}
		}
		
		return ret;
	}
	
	/**
	 *  Internal check method that calls verify on 
	 */
	protected IFuture<Void> internalCheck(ServiceInvocationContext context, final String callername, final byte[] signed)
	{
		final Future<Void> ret = new Future<Void>();
		
		String classname = context.getMethod().getDeclaringClass().getName();
		String methodname = context.getMethod().getName();
		Object[] args = context.getArgumentArray();
		Object[] t = new Object[]{callername, classname, methodname, args};
		final byte[] content = BinarySerializer.objectToByteArray(t, null);
		
		SServiceProvider.getService(ea.getServiceProvider(), ISecurityService.class, RequiredServiceInfo.SCOPE_PLATFORM)
			.addResultListener(new ExceptionDelegationResultListener<ISecurityService, Void>(ret)
		{
			public void customResultAvailable(ISecurityService sser)
			{
				sser.verifyCall(content, signed, callername).addResultListener(new DelegationResultListener<Void>(ret));
			}
		});
		
		return ret;
	}
}
