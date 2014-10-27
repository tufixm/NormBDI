package jadex.bpmn.runtime;

import jadex.bridge.IMessageAdapter;
import jadex.commons.IValueFetcher;

import java.util.Map;

/**
 *  Value fetcher for process threads.
 */
public class ProcessThreadValueFetcher implements IValueFetcher
{
	//-------- attributes --------
	
	/** The process thread. */
	protected ProcessThread thread;

	/** The activity selection flag. */
	protected boolean flag;
	
	/** The fall back value fetcher (if any). */
	protected IValueFetcher fetcher;
	
	//-------- constructors --------
	
	/**
	 *  Create a value fetcher for a given process.
	 *  @param thread	The process thread.
	 *  @param flag	Flag to indicate that values should be fetched from the next activity (otherwise previous activity is used).
	 *  @param fetcher	The fall back fetcher, if any. 
	 */
	public ProcessThreadValueFetcher(ProcessThread thread, boolean flag, IValueFetcher fetcher)
	{
		this.thread = thread;
		this.flag	= flag;
		this.fetcher	 = fetcher;
	}
	
	//-------- methods --------

	/**
	 *  Fetch a named value from an object.
	 *  @param name The name.
	 *  @param object The object.
	 *  @return The fetched value.
	 */
	public Object fetchValue(String name, Object object)
	{
		Object ret;
		if(object instanceof Map && ((Map)object).containsKey(name))
			ret = ((Map)object).get(name);
		else if(object instanceof IMessageAdapter && ((IMessageAdapter)object).getParameterMap().containsKey(name))
			ret = ((IMessageAdapter)object).getValue(name);
		else if("$thread".equals(name))
			ret = thread;
		else if(fetcher!=null)
			ret = fetcher.fetchValue(name, object);
		else
			throw new UnsupportedOperationException();
	
		return ret;
	}
	
	/**
	 *  Fetch a named value.
	 *  @param name The name.
	 *  @return The fetched value.
	 */
	public Object fetchValue(String name)
	{
		boolean	found	= false;
		Object	value	= null;
		
		// Check for parameter value.
		for(ProcessThread t=thread; t!=null && !found; t=t.getThreadContext().getInitiator() )
		{
			if(t.hasParameterValue(name))
			{
				value	= t.getParameterValue(name);
				found	= true;
			}
		}
				
		if(!found)
		{
			if("$thread".equals(name))
			{
				value = thread;
				found = true;
			}
		}
		
		// Ask contained fetcher.
		if(!found && fetcher!=null)
		{
			value	= fetcher.fetchValue(name);
		}
		else if(!found)
		{
			throw new RuntimeException("Parameter not found: "+name);
		}
		
		return value;
	}
}
