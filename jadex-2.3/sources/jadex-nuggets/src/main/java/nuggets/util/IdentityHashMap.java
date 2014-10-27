/*
 * HT.java
 *
 * Created by aw on Nov 23, 2005.  
 */
package nuggets.util;

import java.io.Serializable;
import java.util.Arrays;


/**
 * HT
 * 
 * @author aw
 * @since Nov 23, 2005
 */
public class IdentityHashMap implements Serializable, Cloneable
{

	private static final Object	NULL	= null;

	/** stores key value pairs */
	private Object[]			objects;

	private Object[]			values;

	/** stores the load */
	private double				load;

	/** masks the adresses */
	private int					mask;

	/** maximum of entries before rehash */
	private int					limit;

	/** the number of entries so far */
	private int					size;

	/**
	 * Constructor for HT. Creates a hashtable with capacity of eight and load
	 * equal 0.5.
	 */
	public IdentityHashMap()
	{
		this(8, 0.5);
	}

	/**
	 * Constructor for HT. Creates a hashtable of given initial capacity and the
	 * specified load factor. If the number of elements is larger than capacity
	 * multiplied with load factor the table will be resized by factor of two.
	 * 
	 * @param capacity
	 *            The intitial capacity of the table. Must be larger than 0.
	 * @param load
	 *            The load factor. (0.0&lt;load&le;1.0)
	 */
	public IdentityHashMap(int capacity, double load)
	{
		// check values
		if(capacity <= 0) throw new IllegalArgumentException("Initial capacity too low: 0>="
				+ capacity);
		if(load <= 0.0) throw new IllegalArgumentException("Load factor out of range: 0>=" + load);
		if(load > 1.0) throw new IllegalArgumentException("Load factor out of range: 1.0<" + load);

		init(capacity);

		this.load = load;
	}

	/**
	 * Stores a value under the key.
	 * 
	 * @param key
	 * @param value
	 * @return the Object previously stored at this position or <code>null</code>
	 */
	public Object put(Object key, Object value)
	{
		final int a = hash(key);
		int i = a;
		Object tKey;

		do
		{
			tKey = objects[i];
			if(tKey == null)
			{
				// insert new entry
				objects[i] = key;
				values[i] = value;
				if(++size > limit) rehash();
				return NULL;
			}
			if(tKey == key)
			{
				// replace the entry
				objects[i] = key;
				Object tValue = values[i];
				values[i] = value;
				return tValue;
			}
			if(--i < 0) i = mask;
		}
		while(i != a);

		// table is overload (e.g. load==0.99)
		rehash();
		reinsert(key, value);
		return NULL;
	}

	/**
	 * Returns the value stored under the key.
	 * 
	 * @param key
	 * @return the Object stored under the key or <code>null</code>
	 */
	public Object get(Object key)
	{
		final int a = hash(key);
		int i = a;
		Object tKey;

		for(i = a; i >= 0; i--)
		{
			tKey = objects[i];
			if(tKey == null) return NULL;
			if(tKey == key) return values[i];
		}
		for(i = mask; i > a; i--)
		{
			tKey = objects[i];
			if(tKey == null) return NULL;
			if(tKey == key) return values[i];
		}

		return NULL;
	}


	/** 
	 * @param key
	 * @return true or false
	 */
	public boolean containsKey(Object key)
	{
		final int a = hash(key);
		int i = a;
		Object tKey;

		for(i = a; i >= 0; i--)
		{
			tKey = objects[i];
			if(tKey == null) return false;
			if(tKey == key) return true;
		}
		for(i = mask; i > a; i--)
		{
			tKey = objects[i];
			if(tKey == null) return false;
			if(tKey == key) return true;
		}

		return false;
	}

	/**
	 * Removes an entry with the specified key.
	 * 
	 * @param key
	 * @return the Object previously stored under the key or <code>null</code>
	 */
	public Object remove(Object key)
	{
		final int a = hash(key);
		int i = a;
		Object tKey;

		do
		{
			tKey = objects[i];
			if(tKey == null) return NULL;
			if(tKey == key)
			{
				// delete the entry
				objects[i] = null;
				Object tValue = values[i];
				values[i] = NULL;
				size--;

				rehashFrom(i - 1);
				return tValue;
			}
			if(--i < 0) i = mask;
		}
		while(i != a);

		return NULL;
	}

	/** 
	 * sets the size to 0 and deletes all entries
	 */
	public void clear()
	{
		size = 0;
		Arrays.fill(objects, null);
		Arrays.fill(values, NULL);
	}

	/**
	 * @return the number of elements in this hashtable
	 */
	public int getSize()
	{
		return size;
	}

	/**
	 * @return the capacity of this table
	 */
	public int getCapacity()
	{
		return objects.length >> 1;
	}

	/**
	 * Creates a shallow copy of this hashtable. All the structure of the
	 * hashtable itself is copied, but the keys and values are not cloned.
	 * 
	 * @return a clone of the hashtable.
	 */
	public Object clone()
	{
		try
		{
			IdentityHashMap t = (IdentityHashMap)super.clone();
			t.objects = (Object[])objects.clone();
			return t;
		}
		catch(CloneNotSupportedException e)
		{
			throw new InternalError(e + " in a clonable hashtable");
		}
	}

	/**
	 * It will resize this table by factor of two
	 */
	protected void rehash()
	{
		Object[] tObjects = objects;
		Object[] tIds = values;

		// update the values
		init(objects.length << 1);

		// reinsert old keys
		for(int i = tObjects.length; i > 0;)
		{
			Object tKey = tObjects[--i];
			if(tKey != null) reinsert(tKey, tIds[i]);
		}
	}

	/** Resets the table to specified length
	 * @param capacity
	 */
	protected void init(int capacity)
	{
		this.objects = new Object[capacity];
		this.values = new Object[capacity];
		this.mask = capacity - 1;
		this.limit = (int)(Math.ceil(load * objects.length));
	}

	/**
	 * @param a
	 */
	protected void rehashFrom(final int a)
	{
		int i = a;
		Object tKey;
		Object tValue;

		// restore lists
		for(i = a; i >= 0; i--)
		{
			tKey = objects[i];
			if(tKey == null) return;
			// remove the binding
			objects[i] = null;
			tValue = values[i];
			values[i] = NULL;

			// reinsert following entries
			reinsert(tKey, tValue);
		}

		for(i = mask; i > a; i--)
		{
			tKey = objects[i];
			if(tKey == null) return;
			// remove the binding
			objects[i] = null;
			tValue = values[i];
			values[i] = NULL;

			// reinsert following entries
			reinsert(tKey, tValue);
		}
	}

	/**
	 * This is a short-cut version of put. It assumes that the key is not in the
	 * table and it returns no value. There must be one free slot in the table.
	 * 
	 * @param key
	 * @param value
	 */
	protected final void reinsert(Object key, Object value)
	{
		final int a = hash(key);

		for(int i = a; i >= 0; i--)
			if(objects[i] == null)
			{
				objects[i] = key;
				values[i] = value;
				return;
			}

		for(int i = mask; i > a; i--)
			if(objects[i] == null)
			{
				objects[i] = key;
				values[i] = value;
				return;
			}
	}

	/**
	 * @param key
	 * @return the hasvalue of a masked by the size of this table
	 */
	protected int hash(Object key)
	{
		return System.identityHashCode(key) % objects.length;
	}


}