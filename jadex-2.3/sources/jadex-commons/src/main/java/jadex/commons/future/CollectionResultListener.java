package jadex.commons.future;


import java.util.ArrayList;
import java.util.Collection;


/**
 *  Collection result listener collects a number of results and return a collection.
 */
public class CollectionResultListener<E>	implements IResultListener<E>
{
	//-------- attributes --------
	
	/** The number of sub listeners to wait for. */
	protected int num;
	
	/** The original result collection. */
	protected Collection<E>	results;

	/** The delegate result listener. */
	protected IResultListener<Collection<E>>delegate;
	
	/** Flag to indicate that the delegate already has been notified. */
	protected boolean	notified;

	/** Flag to indicate that failures should be ignored and only valid results returned. */
	protected boolean	ignorefailures;

	
	//-------- constructors --------
	
	/**
	 *  Create a new collection listener.
	 *  @param num The expected number of results.
	 *  @param ignorefailures When set to true failures will be 
	 *  	tolerated and just not be added to the result collection.
	 *  @param delegate	The delegate result listener.
	 */
	public CollectionResultListener(int num, boolean ignorefailures, IResultListener<Collection<E>> delegate)
	{
		this.num = num;
		this.ignorefailures	= ignorefailures;
		this.delegate	= delegate;
		this.results	= new ArrayList();
//		System.out.println("CollectionResultListener: "+this+", "+num);
		
		if(num==0)
		{
			this.notified	= true;
//			System.out.println("collecting finished: "+this+", "+this.sresults.size());
			delegate.resultAvailable(results);
		}
	}
	
	//-------- methods --------
	
	/**
	 *  Called when some result is available.
	 * @param result The result.
	 */
	public void resultAvailable(E result)
	{
		boolean	notify	= false;
		synchronized(this)
		{
			if(!notified)
			{
				results.add(result);
//				System.out.println("resultAvailable: "+this+", "+this.sresults.size());
				notify	= num==this.results.size();
				notified	= notify;
			}
		}

		if(notify)
		{
//			System.out.println("collecting finished: "+this+", "+this.sresults.size());
			delegate.resultAvailable(results);
		}
	}
	
	/**
	 *  Called when an exception occurred.
	 * @param exception The exception.
	 */
	public void exceptionOccurred(Exception exception)
	{
		boolean	notify	= false;
		synchronized(this)
		{
			if(ignorefailures)
			{
				num--;
				notify	= num==this.results.size();
				notified	= notify;
			}
			else if(!notified)
			{
				notify	= true;
				notified	= true;
			}
		}

		if(notify)
		{
//			System.out.println("exceptionOcurred: "+this+", "+this.sresults.size());
//			
			if(ignorefailures)
			{
				delegate.resultAvailable(results);
			}
			else
			{
				delegate.exceptionOccurred(exception);
			}
		}
	}
}
