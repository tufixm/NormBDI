package jadex.commons.collection;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *  A least recently used map.
 */
public class LRU<K, V> extends LinkedHashMap<K, V>
{
	// note for mobile version: change LinkedHashMap to HashMap
	
	//-------- variables --------

	/** The maximum number of entries. */
	protected int max;
	
	/** The entry cleaner (for performing cleanup code on removed entries). */
	protected ILRUEntryCleaner cleaner;

	//-------- constructors --------

	/**
	 *  Create a new LRU.
	 *  @param max The maximum number of entries.
	 */
	public LRU(int max)
	{
		this(max, null);
	}
	
	/**
	 *  Create a new LRU.
	 *  @param max The maximum number of entries.
	 */
	public LRU(int max, ILRUEntryCleaner cleaner)
	{
		this.max = max;
		this.cleaner = cleaner;
	}

	//-------- methods --------

	/**
	 *  Get the maximum number of the
	 *  @return The maximum number of entries.
	 */
	public int getMaxEntries()
	{
		return max;
	}

	/**
	 *  Set the maximum number of entries.
	 *  @param max The maximum number of entries.
	 */
	public void setMaxEntries(int max)
	{
		this.max = max;
	}
	
	/**
	 *  Set the cleaner object.
	 *  @param cleaner	The cleaner object.
	 */
	public void setCleaner(ILRUEntryCleaner cleaner)
	{
		this.cleaner = cleaner;
	}

	/**
	 *  Remove the eldest entry.
	 *  @param eldest The eldest entry.
	 *  @return True if need to be removed.
	 */
	public boolean removeEldestEntry(Map.Entry<K, V> eldest)
	{
//		if(size() > max)
//			System.out.println("removing: "+eldest);
		if(cleaner!=null && size() > max)
			cleaner.cleanupEldestEntry(eldest);
		return size() > max;
	}
}