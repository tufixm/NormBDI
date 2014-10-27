package jadex.bridge.service.search;

import jadex.commons.IRemoteFilter;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;

import java.lang.reflect.Proxy;

/**
 *  Test if a class is a proxy.
 */
public class ProxyFilter implements IRemoteFilter
{
	//-------- attributes --------
	
	/** Static proxy filter instance. */
	public static IRemoteFilter PROXYFILTER = new ProxyFilter();

	//-------- methods --------
	
	/**
	 *  Test if service is a remote proxy.
	 */
	public IFuture<Boolean> filter(Object obj)
	{
		try
		{
		boolean ret = Proxy.isProxyClass(obj.getClass()) && Proxy.getInvocationHandler(obj).getClass()
			.getName().indexOf("RemoteMethodInvocationHandler")!=-1;
//		System.out.println("obj: "+obj==null? "null": obj.getClass()+" "+!ret);
		return new Future<Boolean>(!ret);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
//		try
//		{
//		System.out.println("obj: "+obj==null? "null": obj.getClass());
//		if(obj!=null)
//			System.out.println("filter: "+Proxy.isProxyClass(obj.getClass())+" "+Proxy.getInvocationHandler(obj));
//		return new Future<Boolean>(!Proxy.isProxyClass(obj.getClass()) || 
//			// todo: fix this Hack	
//			!(Proxy.getInvocationHandler(obj).getClass().getName().indexOf("RemoteMethodInvocationHandler")!=-1));
////			!(Proxy.getInvocationHandler(obj) instanceof RemoteMethodInvocationHandler));
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//			throw new RuntimeException(e);
//		}
	}
	
	/**
	 *  Get the hashcode.
	 */
	public int hashCode()
	{
		return getClass().hashCode();
	}

	/**
	 *  Test if an object is equal to this.
	 */
	public boolean equals(Object obj)
	{
		return obj!=null && obj.getClass().equals(this.getClass());
	}
}
