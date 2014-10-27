package jadex.bridge.service.search;

/**
 *  Exception to denote that a requested service was not found.
 */
public class ServiceNotFoundException extends RuntimeException
{
	/**
	 *  Create a new service not found exception.
	 */
	public ServiceNotFoundException(String message)
	{
		super(message);
	}
}
