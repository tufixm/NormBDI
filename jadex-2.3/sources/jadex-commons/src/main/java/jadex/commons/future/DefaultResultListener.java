package jadex.commons.future;


import jadex.commons.DebugException;

import java.util.logging.Logger;

/**
 *  The default listener for just printing out result information.
 *  Is used as fallback when no other listener is available.
 */
public abstract class DefaultResultListener<E> implements IResultListener<E>
{
	//-------- attributes --------
	
	/** The logger. */
	private Logger logger;	// Private to prevent accidental use.
	
//	/** The static instance. */
//	private static IResultListener instance;
	
	/** The exception (for debugging). */
	private Exception exception;
	
	//-------- constructors --------
	
	/**
	 *  Create a new listener.
	 *  @param logger The logger.
	 */
	public DefaultResultListener()
	{
		this.logger = Logger.getLogger("default-result-listener");
		if(Future.DEBUG)
			exception = new DebugException();
	}
	
	/**
	 *  Create a new listener.
	 *  @param logger The logger.
	 */
	public DefaultResultListener(Logger logger)
	{
		this.logger = logger;
		if(logger==null)
		{
			this.logger = Logger.getLogger("default-result-listener");
		}
	}
	
//	/**
//	 *  Get the listener instance.
//	 *  @return The listener.
//	 */
//	public static IResultListener getInstance()
//	{
//		// Hack! Implement that logger can be passed
//		if(instance==null)
//		{
//			instance = new DefaultResultListener()
//			{
//				public void resultAvailable(Object result)
//				{
//				}
//			};
//		}
//		return instance;
//	}
	
	//-------- methods --------
	
	/**
	 *  Called when the result is available.
	 *  @param result The result.
	 * /
	public void resultAvailable(Object result)
	{
		//logger.info(""+result);
	}*/
	
	/**
	 *  Called when an exception occurred.
	 *  @param exception The exception.
	 */
	public void exceptionOccurred(Exception exception)
	{
		if(Future.DEBUG)
		{
			this.exception.printStackTrace();
			exception.printStackTrace();
		}
		logger.severe("Exception occurred: "+this+", "+exception);
	}
}
