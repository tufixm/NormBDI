package jadex.commons;

import jadex.commons.collection.SCollection;
import jadex.commons.concurrent.IThreadPool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * This class provides several useful static util methods.
 */
public class SUtil
{
	/** Line separator. */
	public static final String LF = System.getProperty("line.separator");
	
	/** Units for representing byte values. */
	public static final String[]	BYTE_UNITS	= new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB"};	// captures up to Long.MAX_VALUE (= 7.99 EB)
	
	/** The byte formatter for one predecimal digit. */
	public static final DecimalFormat	BYTEFORMATTER1	= new DecimalFormat("0.00");

	/** The byte formatter for two predecimal digits. */
	public static final DecimalFormat	BYTEFORMATTER2	= new DecimalFormat("00.0");

	/** The byte formatter for three predecimal digits. */
	public static final DecimalFormat	BYTEFORMATTER3	= new DecimalFormat("000");

	/** Constant that indicates a conversion of all known characters. */
	public static final int			CONVERT_ALL				= 1;

	/** Constant that indicates a conversion of all known characters except &. */
	public static final int			CONVERT_ALL_EXCEPT_AMP	= 2;

	/** Constant that indicates a conversion of no characters. */
	public static final int			CONVERT_NONE			= 3;

	/** A Null value. */
	public static final String		NULL					= "NULL";

	/** Simple date format. */
	public static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	public static final SimpleDateFormat SDF2 = new SimpleDateFormat("dd.MM.yyyy");

	
	/**
	 * Mapping from single characters to encoded version for displaying on
	 * xml-style interfaces.
	 */
	protected static Map<String, String>			htmlwraps;

	/** Holds the single characters. */
	protected static String			seps;
	

	/** An empty enumeration. */
	public static final Enumeration	EMPTY_ENUMERATION	= new Enumeration()
	{
		public boolean hasMoreElements()
		{
			return false;
		}

		public Object nextElement()
		{
			return null;
		}
	};

	/** An empty string array. */
	public static final String[] EMPTY_STRING_ARRAY	 = new String[0];

	/** An empty class array. */
	public static final Class[]	EMPTY_CLASS_ARRAY		= new Class[0];
	
	protected static final IResultCommand<ResourceInfo, URLConnection>[]	RESOURCEINFO_MAPPERS;

	static
	{
		htmlwraps = new Hashtable<String, String>();
		htmlwraps.put("\\u0022", "&quot;");
		htmlwraps.put("\u0026", "&amp;"); // Hmm???
		htmlwraps.put("\u0027", "&apos;");
		htmlwraps.put("\u003C", "&lt;");
		htmlwraps.put("\u003E", "&gt;");
		htmlwraps.put("\u00E4", "&auml;");
		htmlwraps.put("\u00C4", "&Auml;");
		htmlwraps.put("\u00FC", "&uuml;");
		htmlwraps.put("\u00DC", "&Uuml;");
		htmlwraps.put("\u00F6", "&ouml;");
		htmlwraps.put("\u00D6", "&Ouml;");

		htmlwraps.put("\u00B4", "&acute;");
		htmlwraps.put("\u00E1", "&aacute;");
		htmlwraps.put("\u00C1", "&Aacute;");
		htmlwraps.put("\u00E0", "&agrave;");
		htmlwraps.put("\u00C0", "&Agrave;");
		htmlwraps.put("\u00E5", "&aring;");
		htmlwraps.put("\u00C5", "&Aring;l");
		htmlwraps.put("\u00E2", "&acirc;");
		htmlwraps.put("\u00C2", "&Acirc;");

		htmlwraps.put("\u00E9", "&eacute;");
		htmlwraps.put("\u00C9", "&Eacute;");
		htmlwraps.put("\u00E8", "&egrave;");
		htmlwraps.put("\u00C8", "&Egrave;");
		htmlwraps.put("\u00EA", "&ecirc;");
		htmlwraps.put("\u00CA", "&Ecirc;");

		htmlwraps.put("\u00ED", "&iacute;");
		htmlwraps.put("\u00CD", "&Iacute;");
		htmlwraps.put("\u00EC", "&igrave;");
		htmlwraps.put("\u00CC", "&Igrave;");
		htmlwraps.put("\u00EE", "&icirc;");
		htmlwraps.put("\u00CE", "&Icirc;");

		htmlwraps.put("\u00F3", "&oacute;");
		htmlwraps.put("\u00D3", "&Oacute;");
		htmlwraps.put("\u00F2", "&ograve;");
		htmlwraps.put("\u00D2", "&Ograve;");
		htmlwraps.put("\u00F4", "&ocirc;");
		htmlwraps.put("\u00D4", "&Ocirc;");
		htmlwraps.put("\u00F5", "&otilde;");
		htmlwraps.put("\u00D5", "&Otilde;");

		htmlwraps.put("\u00FA", "&uacute;");
		htmlwraps.put("\u00DA", "&Uacute;");
		htmlwraps.put("\u00F9", "&ugrave;");
		htmlwraps.put("\u00D9", "&Ugrave;");
		htmlwraps.put("\u00FB", "&ucirc;");
		htmlwraps.put("\u00DB", "&Ucirc;");

		htmlwraps.put("\u00E7", "&ccedil;");
		htmlwraps.put("\u00C7", "&Ccedil;");

		seps = "";
		Iterator<String> it = htmlwraps.keySet().iterator();
		while(it.hasNext())
			seps += it.next();
		
		List<IResultCommand<ResourceInfo, URLConnection>>	mappers	= new ArrayList();
		String	custommappers	= System.getProperty("jadex.resourcemappers");
		if(custommappers!=null)
		{
			StringTokenizer	stok	= new StringTokenizer(custommappers, ",");
			while(stok.hasMoreTokens())
			{
				String	mapper	= stok.nextToken().trim();
				try
				{
					Class	clazz	= SReflect.classForName(mapper, SUtil.class.getClassLoader());
					mappers.add((IResultCommand<ResourceInfo, URLConnection>)clazz.newInstance());
				}
				catch(Exception e)
				{
					System.err.println("Error loading custom resource mapper: "+mapper);
					throw new RuntimeException(e);
				}
			}
		}
		
		// ResourceInfo mapper for Jar URL connection
		mappers.add(new IResultCommand<ResourceInfo, URLConnection>()
		{
			public ResourceInfo execute(URLConnection con)
			{
				ResourceInfo	ret	= null;
				if(con instanceof JarURLConnection)
				{
					try
					{
						long	modified	= 0;
						String	filename	= con.getURL().getFile();
						JarURLConnection juc = (JarURLConnection)con;
						// System.out.println("Jar file:     "+juc.getJarFile());
						// System.out.println("Jar file url: "+juc.getJarFileURL());
						// System.out.println("Jar entry:    "+juc.getJarEntry());
						// System.out.println("Entry name:   "+juc.getEntryName());
	
						// Add jar protocol to file (hack???).
						if(!filename.startsWith("jar:"))
							filename = "jar:" + filename;
	
						// Set modified date to time of entry (if
						// specified).
						if(juc.getJarEntry().getTime() != -1)
							modified = juc.getJarEntry().getTime();
	
						try
						{
							ret = new ResourceInfo(filename,
									con.getInputStream(), modified);
						}
						catch(NullPointerException e)
						{
							// Workaround for Java bug #5093378 !?
							// Maybe this is only a race condition???
							String jarfilename = juc.getJarFile().getName();
							ret = new ResourceInfo(filename, new JarFile(
									jarfilename).getInputStream(juc
									.getJarEntry()), modified);
							// System.err.println("loaded with workaround: "+url);
						}
	
						// todo: what about jar directories?!
					}
					catch(IOException e)
					{
					}
				}
				return ret;
			}
		});
		// Eclipse OSGI resource bundle.
		mappers.add(new IResultCommand<ResourceInfo, URLConnection>()
		{
			public ResourceInfo execute(URLConnection con)
			{
				ResourceInfo ret = null;
				long modified = con.getLastModified();
				if(con.getClass().getName().equals("org.eclipse.osgi.framework.internal.core.BundleURLConnection"))
				{
					try
					{
						Method	m	= con.getClass().getMethod("getLocalURL", new Class<?>[0]);
						ret = new ResourceInfo(m.invoke(con, new Object[0]).toString(), con.getInputStream(), modified);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				return ret;
			}
		});
		// Fallback resource info.
		mappers.add(new IResultCommand<ResourceInfo, URLConnection>()
		{
			public ResourceInfo execute(URLConnection con)
			{
				ResourceInfo	ret	= null;
				try
				{
					long modified = con.getLastModified();
					String	filename	= URLDecoder.decode(con.getURL().getFile(), "UTF-8");
					ret	= new ResourceInfo(filename, con.getInputStream(), modified);
				}
				catch(IOException e)
				{
				}
				return ret;
			}
		});
		RESOURCEINFO_MAPPERS	= mappers.toArray(new IResultCommand[mappers.size()]);
	}

	/**
	 * Get a string array of properties that are separated by commas.
	 * 
	 * @param key The key.
	 * @param props The properties.
	 * @return The strings.
	 */
	public static String[] getStringArray(String key, Properties props)
	{
		String[] ret = new String[0];
		String os = props.getProperty(key);
		if(os != null)
		{
			StringTokenizer stok = new StringTokenizer(os, ",");
			ret = new String[stok.countTokens()];
			for(int i = 0; stok.hasMoreTokens(); i++)
			{
				ret[i] = stok.nextToken().trim();
			}
		}
		return ret;
	}

	/**
	 * Joins two arrays of the same type. Creates a new array containing the
	 * values of the first array followed by the values of the second.
	 * 
	 * @param a1 The first array.
	 * @param a2 The second array.
	 * @return The joined array.
	 */
	public static Object joinArrays(Object a1, Object a2)
	{
		int l1 = Array.getLength(a1);
		int l2 = Array.getLength(a2);
		Object res = Array.newInstance(a1.getClass().getComponentType(), l1
				+ l2);
		System.arraycopy(a1, 0, res, 0, l1);
		System.arraycopy(a2, 0, res, l1, l2);
		return res;
	}

	/**
	 * Joins any arrays of (possibly) different type. todo: Does not support
	 * basic types yet. Problem basic type array and object arrays cannot be
	 * mapped (except they are mapped).
	 * 
	 * @param as The array of arrays to join..
	 * @return The joined array.
	 */
	public static <T> T[] joinArbitraryArrays(Object[] as)
	{
		int lsum = 0;
		for(int i = 0; i < as.length; i++)
			lsum += Array.getLength(as[i]);
		T[] ret = (T[])new Object[lsum];
		// Object ret = Array.newInstance(Object.class, lsum);

		int start = 0;
		for(int i = 0; i < as.length; i++)
		{
			int length = Array.getLength(as[i]);
			System.arraycopy(as[i], 0, ret, start, length);
			start += length;
		}

		return ret;
	}

	/**
	 * Cut two arrays.
	 * 
	 * @param a1 The first array.
	 * @param a2 The second array.
	 * @return The cutted array.
	 */
	public static Object cutArrays(Object a1, Object a2)
	{
		List ar1 = arrayToList(a1);
		List ar2 = arrayToList(a2);
		List<Object> ret = new ArrayList<Object>();
		Object tmp;

		for(int i = 0; i < ar1.size(); i++)
		{
			tmp = ar1.get(i);
			if(ar2.contains(tmp))
			{
				ret.add(tmp);
			}
		}
		return ret.toArray((Object[])Array.newInstance(a1.getClass()
				.getComponentType(), ret.size()));
	}

	/**
	 * First array minus second array.
	 * 
	 * @param a1 The first array.
	 * @param a2 The second array.
	 * @return The substracted array.
	 */
	public static Object substractArrays(Object a1, Object a2)
	{
		List ar1 = arrayToList(a1);
		List ar2 = arrayToList(a2);
		Object tmp;

		for(int i = 0; i < ar2.size(); i++)
		{
			tmp = ar2.get(i);
			if(ar1.contains(tmp))
			{
				ar1.remove(tmp);
			}
		}
		return ar1.toArray((Object[])Array.newInstance(a1.getClass()
				.getComponentType(), ar1.size()));
	}

	/**
	 * Transform an array to a vector.
	 * 
	 * @param a The array.
	 * @return The vector for the array.
	 */
	public static <T> List<T> arrayToList(Object a)
	{
		ArrayList ret = null;
		if(a!=null)
		{
			int l = Array.getLength(a);
			ret = SCollection.createArrayList();
			for(int i = 0; i < l; i++)
			{
				ret.add(Array.get(a, i));
			}
		}
		return ret;
	}

	/**
	 * Transform an array to a vector.
	 * 
	 * @param a The array.
	 * @return The vector for the array.
	 */
	public static <T> Set<T> arrayToSet(Object a)
	{
		int l = Array.getLength(a);
		Set ret = SCollection.createHashSet();
		for(int i = 0; i < l; i++)
		{
			ret.add(Array.get(a, i));
		}
		return ret;
	}

	/**
	 * Join two sets.
	 * 
	 * @param a The first set.
	 * @param b The second set.
	 * @return A set with elements from a and b. / public static Set
	 *         joinSets(Set a, Set b) { Set ret = new HashSet(); ret.addAll(a);
	 *         ret.addAll(b); return ret; }
	 */

	/**
	 * Transform an iterator to a list.
	 */
	public static List iteratorToList(Iterator it)
	{
		List ret = new ArrayList();
		while(it.hasNext())
			ret.add(it.next());
		return ret;
	}

	/**
	 * Transform an iterator to a list.
	 */
	public static List iteratorToList(Iterator it, List ret)
	{
		if(ret == null)
			ret = new ArrayList();
		while(it.hasNext())
			ret.add(it.next());
		return ret;
	}

	/**
	 * Transform an iterator to an array.
	 */
	public static Object[] iteratorToArray(Iterator it, Class clazz)
	{
		List list = iteratorToList(it);
		return list.toArray((Object[])Array.newInstance(clazz, list.size()));
	}

	/**
	 * Check if an element is contained in an array.
	 * 
	 * @param array The array.
	 * @param value The value.
	 */
	public static boolean arrayContains(Object array, Object value)
	{
		int l = Array.getLength(array);
		boolean ret = false;
		for(int i = 0; !ret && i < l; i++)
		{
			ret = equals(Array.get(array, i), value);
		}
		return ret;
	}

	/**
	 *  Get the array dimension.
	 *  @param array The array.
	 *  @return The number of dimensions.
	 */
	public static int getArrayDimension(Object array) 
	{
		int ret = 0;
		Class arrayClass = array.getClass();
		while(arrayClass.isArray()) 
		{
			ret++;
			arrayClass = arrayClass.getComponentType();
		}
		return ret;
	}
	
//	/**
//	 * Get the dimension of an array.
//	 * 
//	 * @param array
//	 * @return The array dimension.
//	 */
//	public static int[] getArrayLengths(Object array)
//	{
//		List lens = new ArrayList();
//		Class cls = array.getClass();
//
//		while(cls.isArray())
//		{
//			lens.add(new Integer(Array.getLength(array)));
//			cls = cls.getComponentType();
//		}
//
//		int[] ret = new int[lens.size()];
//		for(int i = 0; i < lens.size(); i++)
//			ret[i] = ((Integer)lens.get(i)).intValue();
//
//		return ret;
//	}

	/*
	 * Test if two values are equal or both null.
	 * @param val1 The first value.
	 * @param val2 The second value.
	 * @return True when the values are equal.
	 */
	public static boolean equals(Object val1, Object val2)
	{
		// Should try comparable first, for consistency???
		return val1 == val2 || val1 != null && val1.equals(val2);
	}

	/**
	 * Test if two arrays are content equal or both null.
	 * 
	 * @param array1 The first array.
	 * @param array2 The second array.
	 * @return True when the arrays are content equal.
	 */
	public static boolean arrayEquals(Object array1, Object array2)
	{
		boolean ret = array1 == null && array2 == null;
		if(!ret && array1 != null && array2 != null)
		{
			int l1 = Array.getLength(array1);
			int l2 = Array.getLength(array2);
			if(l1 == l2)
			{
				ret = true;
				for(int i = 0; i < l1 && ret; i++)
				{
					if(!Array.get(array1, i).equals(Array.get(array2, i)))
						ret = false;
				}
			}
		}
		return ret;
	}

	/**
	 * Calculate a hash code for an array.
	 */
	public static int arrayHashCode(Object a)
	{
		int ret = 1;

		for(int i = 0; i < Array.getLength(a); i++)
		{
			Object val = Array.get(a, i);
			ret = 31 * ret + (val != null ? val.hashCode() : 0);
		}

		return ret;
	}

	/**
	 * Get a string representation for an array.
	 * 
	 * @param array The array.
	 * @return formatted string.
	 */
	public static String arrayToString(Object array)
	{
		StringBuffer str = new StringBuffer();

		if(array != null && array.getClass().getComponentType() != null)
		{
			// inside arrays.
			str.append("[");
			for(int i = 0; i < Array.getLength(array); i++)
			{
				str.append(arrayToString(Array.get(array, i)));
				if(i < Array.getLength(array) - 1)
				{
					str.append(", ");
				}
			}
			str.append("]");
		}
		else
		{
			// simple type
			str.append(array);
		}
		return str.toString();
	}

	/**
	 * Get the singular of a word in plural. Does NOT find all correct singular.
	 * 
	 * @param s The plural word.
	 * @return The singular of this word.
	 */
	public static String getSingular(String s)
	{
		String sing = s;
		if(s.endsWith("shes") || s.endsWith("ches") || s.endsWith("xes")
				|| s.endsWith("ses"))
		{
			sing = s.substring(0, s.length() - 2);
		}
		else if(s.endsWith("ies"))
		{
			sing = s.substring(0, s.length() - 3) + "y";
		}
		else if(s.endsWith("s"))
		{
			sing = s.substring(0, s.length() - 1);
		}

		return sing;
	}

	/**
	 * Get the plural of a word in singular. Does NOT find all correct plurals.
	 * 
	 * @param s The word.
	 * @return The plural of this word.
	 */
	public static String getPlural(String s)
	{
		String plu = s;
		if(s.endsWith("y"))
		{
			plu = s.substring(0, s.length() - 1) + "ies";
		}
		else if(s.endsWith("s"))
		{
			plu = s + "es";
		}
		else
		{
			plu = s + "s";
		}
		return plu;
	}

	/**
	 * Compares two strings, ignoring case.
	 * 
	 * @param a The first string.
	 * @param b The second string.
	 * @return a<b => <0
	 */
	public static int compareTo(String a, String b)
	{
		return a.toUpperCase().toLowerCase()
				.compareTo(b.toUpperCase().toLowerCase());
	}

	/**
	 * Test if the date is in the range. Start or end may null and will so not
	 * be checked.
	 * 
	 * @param date The date.
	 * @param start The start.
	 * @param end The end.
	 * @return True, if date is in range.
	 */
	public static boolean isInRange(Date date, Date start, Date end)
	{
		boolean ret = true;
		if(start != null && date.before(start))
		{
			ret = false;
		}
		if(ret && end != null && date.after(end))
		{
			ret = false;
		}
		return ret;
	}

	/**
	 * Remove file extension.
	 * 
	 * @param fn The filename..
	 * @return filename without extension.
	 */
	public static String removeExtension(String fn)
	{
		int index = fn.lastIndexOf(".");
		if(index > -1)
		{
			fn = fn.substring(0, index);
		}
		return fn;
	}

	/**
	 * Wrap a text at a given line length. Doesn't to word wrap, just inserts
	 * linebreaks every nth character. If the string already contains
	 * linebreaks, these are handled properly (extra linebreaks will only be
	 * inserted when needed).
	 * 
	 * @param text The text to wrap.
	 */
	public static String wrapText(String text)
	{
		return wrapText(text, 80);
	}

	/**
	 * Wrap a text at a given line length. Doesn't to word wrap, just inserts
	 * linebreaks every nth character. If the string already contains
	 * linebreaks, these are handled properly (extra linebreaks will only be
	 * inserted when needed).
	 * 
	 * @param text The text to wrap.
	 * @param wrap The column width.
	 */
	public static String wrapText(String text, int wrap)
	{
		StringBuffer buf = new StringBuffer(text);
		int i = 0;

		// Insert line breaks, while more than <wrap> characters to go.
		while(buf.length() > i + wrap)
		{
			// Find next line break.
			// int next = buf.indexOf("\n", i); // works for 1.4 only.
			int next = buf.substring(i).indexOf("\n");

			// Skip line break, when in sight.
			if(next != -1 && next - i <= wrap)
			{
				i = i + next + 1;
			}

			// Otherwise, insert line break.
			else
			{
				buf.insert(i + wrap, '\n');
				i = i + wrap + 1;
			}
		}

		return buf.toString();
	}


	/** Constant for sorting up. */
	public static final int	SORT_UP		= 0;

	/** Constant for sorting down. */
	public static final int	SORT_DOWN	= 1;

	/**
	 * Remove the least element form a collection.
	 */
	protected static int getExtremeElementIndex(Vector source, int direction)
	{
		String ret = (String)source.elementAt(0);
		int retidx = 0;
		int size = source.size();
		for(int i = 0; i < size; i++)
		{
			String tmp = (String)source.elementAt(i);
			int res = tmp.compareTo(ret);
			if((res < 0 && direction == SORT_UP)
					|| (res > 0 && direction == SORT_DOWN))
			{
				ret = tmp;
				retidx = i;
			}
		}
		return retidx;
	}

	/**
	 * Convert an output to html/wml conform presentation.
	 * 
	 * @param input The input string.
	 * @return The converted output string.
	 */
	public static String makeConform(String input)
	{
		return makeConform(input, CONVERT_ALL);
	}

	/**
	 * Convert an output to html/wml conform presentation.
	 * 
	 * @param input The input string.
	 * @param flag CONVERT_ALL, CONVERT_NONE, CONVERT_ALL_EXCEPT_AMP;
	 * @return The converted output string.
	 */
	public static String makeConform(String input, int flag)
	{
		String res = input;
		if(flag != CONVERT_NONE)
		{
			StringTokenizer stok = new StringTokenizer(input, seps, true);
			res = "";
			while(stok.hasMoreTokens())
			{
				String tmp = stok.nextToken();
				String rep = null;
				if(!(tmp.equals("&") && flag == CONVERT_ALL_EXCEPT_AMP))
					rep = htmlwraps.get(tmp);
				if(rep != null)
					res += rep;
				else
					res += tmp;
			}
		}
		return res;
	}

	/**
	 * Strip tags (e.g. html) from a string, leaving only the text content.
	 */
	public static String stripTags(String source)
	{
		int start, end;
		while((start = source.indexOf("<")) != -1
				&& (end = source.indexOf(">")) > start)
		{
			if(end == source.length() - 1)
			{
				source = source.substring(0, start);
			}
			else
			{
				source = source.substring(0, start) + source.substring(end + 1);
			}
		}
		return source;
	}

	/**
	 * Convert an output readable in english. Therefore remove all &auml;s,
	 * &ouml;s, &uuml;s etc.
	 * 
	 * @param input The input string.
	 * @return The converted output string.
	 */
	public static String makeEnglishConform(String input)
	{
		StringTokenizer stok = new StringTokenizer(input, seps, true);
		String res = "";
		while(stok.hasMoreTokens())
		{
			String tmp = stok.nextToken();
			if(htmlwraps.get(tmp) == null)
				res += tmp;
		}
		return res;
	}

	/**
	 * Extract the values out of an sl message.
	 * 
	 * @param message The sl message.
	 * @return The extracted properties. / // obsolete ??? public static
	 *         Properties parseSLToPropertiesFast(String message) { Properties
	 *         props = new Properties(); int index = message.indexOf(':');
	 *         while(index!=-1) { // Hack !!! Assume space separated slots. int
	 *         index2 = message.indexOf(' ', index); String name =
	 *         message.substring(index+1, index2); index = message.indexOf('"',
	 *         index2); index2 = message.indexOf('"', index+1); String value =
	 *         message.substring(index+1, index2); props.setProperty(name,
	 *         value); index = message.indexOf(':', index2); } return props; }
	 */
	/**
	 * Extract the value(s) out of an sl message.
	 * 
	 * @param message The sl message.
	 * @return The extracted value(s) as string, index map or array list.
	 * @see #toSLString(Object) / public static Object fromSLString(String
	 *      message) { Object ret; // Parse map. if(message.startsWith("(Map ")
	 *      && message.endsWith(")")) { message = message.substring(5,
	 *      message.length()-1); ExpressionTokenizer exto = new
	 *      ExpressionTokenizer(message, " \t\r\n", new String[]{"\"\"", "()"});
	 *      Map map = new IndexMap().getAsMap(); // Hack???
	 *      while(exto.hasMoreTokens()) { // Check for ":" as start of slot
	 *      name. String slot = exto.nextToken(); if(!slot.startsWith(":") ||
	 *      !exto.hasMoreTokens()) throw new
	 *      RuntimeException("Invalid SL: "+message); slot = slot.substring(1);
	 *      //if(slot.equals("2")) // System.out.println("Da1!"); map.put(slot,
	 *      fromSLString(exto.nextToken())); } ret = map; } // Parse sequence to
	 *      collection object. else if(message.startsWith("(sequence ") &&
	 *      message.endsWith(")")) { message = message.substring(10,
	 *      message.length()-1); ExpressionTokenizer exto2 = new
	 *      ExpressionTokenizer(message, " \t\r\n", new String[]{"\"\"", "()"});
	 *      List list = new ArrayList(); while(exto2.hasMoreTokens()) {
	 *      list.add(fromSLString(exto2.nextToken())); } ret = list; } // Simple
	 *      slot message. else { // Remove quotes from message.
	 *      if(message.startsWith("\"") && message.endsWith("\"")) message =
	 *      message.substring(1, message.length()-1); // Replace escaped quotes.
	 *      message = SUtil.replace(message, "\\\"", "\""); ret = message; }
	 *      return ret; }
	 */

	/**
	 * Convert an object to an SL string. When the value is of type
	 * java.util.Map the key value pairs are extracted as slots. Keys must be
	 * valid slot names. Values of type java.util.Collection are stored as
	 * sequence.
	 * 
	 * @return A string representation in SL. / public static String
	 *         toSLString(Object o) { StringBuffer sbuf = new StringBuffer();
	 *         toSLString(o, sbuf); return sbuf.toString(); }
	 */

	/**
	 * Convert an object to an SL string. When the value is of type
	 * java.util.Map the key value pairs are extracted as slots. Keys must be
	 * valid slot names. Values of type java.util.Collection are stored as
	 * sequence.
	 * 
	 * @param o The object to convert to SL.
	 * @param sbuf The buffer to convert into. / public static void
	 *        toSLString(Object o, StringBuffer sbuf) { // Get mapo from
	 *        encodable object. /*if(o instanceof IEncodable) { o =
	 *        ((IEncodable)o).getEncodableRepresentation(); }* / // Write
	 *        contents as slot value pairs. if(o instanceof Map) { Map contents
	 *        = (Map)o; sbuf.append("(Map "); for(Iterator
	 *        i=contents.keySet().iterator(); i.hasNext();) { Object key =
	 *        i.next(); Object val = contents.get(key); if(val!=null &&
	 *        !"null".equals(val)) // Hack ??? { // Check if key is valid slot
	 *        identifier. String keyval = key.toString();
	 *        if(keyval.indexOf(' ')!=-1 || keyval.indexOf('\t')!=-1 ||
	 *        keyval.indexOf('\r')!=-1 || keyval.indexOf('\n')!=-1) { throw new
	 *        RuntimeException("Encoding error: Invalid slot name "+keyval); }
	 *        sbuf.append(" :"); sbuf.append(keyval); sbuf.append(" ");
	 *        toSLString(val, sbuf); } } sbuf.append(")"); } // Write collection
	 *        value as sequence. else if(o instanceof Collection) { Collection
	 *        coll = (Collection)o; sbuf.append(" (sequence "); for(Iterator
	 *        j=coll.iterator(); j.hasNext(); ) { sbuf.append(" ");
	 *        toSLString(j.next(), sbuf); } sbuf.append(")"); } // Write normal
	 *        slot value as string. else { sbuf.append("\""); // Escape quotes
	 *        (directly writes to string buffer). SUtil.replace(""+o, sbuf,
	 *        "\"", "\\\""); sbuf.append("\""); } }
	 */

	/**
	 * Parse a source string replacing occurrences and storing the result in the
	 * given string buffer. This is a fast alternative to String.replaceAll(),
	 * because it does not use regular expressions.
	 * 
	 * @param source The source string.
	 * @param dest The destination string buffer.
	 * @param old The string to replace.
	 * @param newstring The string to use as replacement.
	 */
	public static void replace(String source, StringBuffer dest, String old,
			String newstring)
	{
		int last = 0;
		int index;
		while((index = source.indexOf(old, last)) != -1)
		{
			dest.append(source.substring(last, index));
			dest.append(newstring);
			last = index + old.length();
		}
		dest.append(source.substring(last));
	}

	/**
	 * Parse a source string replacing occurrences and returning the result.
	 * This is a fast alternative to String.replaceAll(), because it does not
	 * use regular expressions.
	 * 
	 * @param source The source string.
	 * @param old The string to replace.
	 * @param newstring The string to use as replacement.
	 */
	public static String replace(String source, String old, String newstring)
	{
		StringBuffer sbuf = new StringBuffer();
		replace(source, sbuf, old, newstring);
		return sbuf.toString();
	}

	/**
	 * Get an input stream for whatever provided. 1. It is tried to load the
	 * resource as file. 2. It is tried to load the resource via the
	 * ClassLoader. 3. It is tried to load the resource as URL.
	 * 
	 * @param name The resource description.
	 * @return The input stream for the resource.
	 * @throws IOException when the resource was not found.
	 */
	public static InputStream getResource(String name, ClassLoader classloader)
			throws IOException
	{
		InputStream is = getResource0(name, classloader);
		if(is == null)
			throw new IOException("Could not load file: " + name);

		return is;
	}

	/**
	 * Get an input stream for whatever provided. 1. It is tried to load the
	 * resource as file. 2. It is tried to load the resource via the
	 * ClassLoader. 3. It is tried to load the resource as URL.
	 * 
	 * @param name The resource description.
	 * @return The input stream for the resource or null when the resource was
	 *         not found.
	 */
	public synchronized static InputStream getResource0(String name,
			ClassLoader classloader)
	{
		InputStream is = null;
		File file;

		if(classloader == null)
			classloader = SUtil.class.getClassLoader();

		// File...
		// Hack!!! Might throw exception in applet / webstart.
		try
		{
			file = new File(name);
			if(file.exists())
			{
				try
				{
					is = new FileInputStream(file);
				}
				catch(FileNotFoundException e)
				{
					// File is directory, or maybe locked...
				}
			}
		}
		catch(SecurityException e)
		{
		}

		// Classpath...
		if(is == null)
		{
			// is = getClassLoader().getResourceAsStream(name.startsWith("/") ?
			// name.substring(1) : name);
			is = classloader.getResourceAsStream(name.startsWith("/") ? name
					.substring(1) : name);
		}

		// URL...
		if(is == null)
		{
			try
			{
				is = new URL(name).openStream();
			}
			catch(IOException le)
			{
			}
		}

		return is;
	}

	/**
	 * Get an input stream for whatever provided. 1. It is tried to load the
	 * resource as file. 2. It is tried to load the resource via the
	 * ClassLoader. 3. It is tried to load the resource as URL.
	 * 
	 * @param name The resource description.
	 * @return The info object for the resource or null when the resource was
	 *         not found.
	 */
	public synchronized static ResourceInfo getResourceInfo0(String name, ClassLoader classloader)
	{
		ResourceInfo ret = null;
		File file;

		if(classloader == null)
			classloader = SUtil.class.getClassLoader();

		// File...
		// Hack!!! Might throw exception in applet / webstart.
		try
		{
			file = new File(name);
			if(file.exists())
			{
				if(file.isDirectory())
				{
					try
					{
						ret = new ResourceInfo(file.getCanonicalPath(), null, file.lastModified());
					}
					catch(IOException e)
					{
						// shouldn't happen
						e.printStackTrace();
					}
				}
				else
				{
					try
					{
						ret = new ResourceInfo(file.getCanonicalPath(),
								new FileInputStream(file), file.lastModified());
					}
					catch(FileNotFoundException e)
					{
						// File is directory, or maybe locked...
					}
					catch(IOException e)
					{
						// shouldn't happen
						e.printStackTrace();
					}
				}
			}
		}
		catch(SecurityException e)
		{
			e.printStackTrace();
		}

		// Classpath...
		if(ret == null)
		{
			URL url = classloader.getResource(name.startsWith("/") ? name.substring(1) : name);
			// System.out.println("Classloader: "+classloader+" "+name+" "+url+" "+classloader.getParent());
			// if(classloader instanceof URLClassLoader)
			// System.out.println("URLs: "+SUtil.arrayToString(((URLClassLoader)classloader).getURLs()));

			if(url != null)
			{
				// Local file from classpath.
				// Hack!!! Needed because of last-modified precision (argl).
				if(url.getProtocol().equals("file"))
				{
					// Find out default encoding (might fail in applets).
					String encoding = "ISO-8859-1";
					try
					{
						encoding = System.getProperty("file.encoding");
					}
					catch(SecurityException e)
					{
					}

					try
					{
						file = new File(URLDecoder.decode(url.getFile(), encoding)); // does only work since 1.4.
						// file = new File(URLDecoder.decode(url.getFile())); //
						// problem decode is deprecated.
						if(file.exists())
						{
							if(file.isDirectory())
							{
								try
								{
									ret = new ResourceInfo(file.getCanonicalPath(),
										null, file.lastModified());
								}
								catch(IOException e)
								{
									// shouldn't happen
									e.printStackTrace();
								}
							}
							else
							{
								try
								{
									ret = new ResourceInfo(file.getCanonicalPath(),
										new FileInputStream(file), file.lastModified());
								}
								catch(FileNotFoundException fnfe)
								{
									// File is directory, or maybe locked...
								}
								catch(IOException e)
								{
									// shouldn't happen
									e.printStackTrace();
								}
							}
						}
					}
					catch(UnsupportedEncodingException e)
					{
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						throw new RuntimeException(sw.toString());
					}
				}

				// Remote or jar file...
				else
				{
					try
					{
						url = classloader.getResource(name.startsWith("/")
							? name.substring(1) : name);
						URLConnection con = url.openConnection();
						con.setDefaultUseCaches(false); // See Java Bug ID 4386865
						for(int i=0; ret==null && i<RESOURCEINFO_MAPPERS.length; i++)
						{
							ret	= RESOURCEINFO_MAPPERS[i].execute(con);
						}
					}
					catch(IOException e)
					{
						// System.err.println("Error loading: "+url);
						// e.printStackTrace();
					}
				}
			}
		}

		// URL...
		if(ret == null)
		{
			try
			{
				URL url = new URL(name);
				URLConnection con = url.openConnection();
				ret = new ResourceInfo(name, con.getInputStream(), con.getLastModified());
			}
			catch(IOException le)
			{
			}
		}

		return ret;
	}

	/**
	 * Encode an object into XML.
	 * 
	 * @param ob The object.
	 * @return The xml representation. / // Hack!!! has to be synchronized due
	 *         to bug in JDK 1.4 java.beans.Statement public synchronized static
	 *         String encodeXML(Object ob) { ByteArrayOutputStream bs = new
	 *         ByteArrayOutputStream(); XMLEncoder e = new XMLEncoder(bs);
	 *         e.setExceptionListener(new ExceptionListener() { public void
	 *         exceptionThrown(Exception e) {
	 *         System.out.println("XML encoding ERROR: "); e.printStackTrace();
	 *         } }); e.writeObject(ob); e.close(); return bs.toString(); }
	 */

	/**
	 * Decode an object from XML.
	 * 
	 * @param xml_ob The xml representation.
	 * @return The object. / // Hack!!! has to be synchronized due to bug in JDK
	 *         1.4 java.beans.Statement public synchronized static Object
	 *         decodeXML(final String xml_ob) { ByteArrayInputStream bs = new
	 *         ByteArrayInputStream(xml_ob.getBytes()); XMLDecoder d = new
	 *         XMLDecoder(bs, null, new ExceptionListener() { public void
	 *         exceptionThrown(Exception e) {
	 *         System.out.println("XML decoding ERROR: "+xml_ob);
	 *         e.printStackTrace(); } }); Object ob = d.readObject(); d.close();
	 *         return ob; }
	 */

	/**
	 * Get a string representation for a duration.
	 * 
	 * @param ms The duration in ms.
	 * @return The string representation.
	 */
	public static String getDurationHMS(long ms)
	{
		long h = ms / 3600000;
		ms = ms - h * 3600000;
		long m = ms / 60000;
		ms = ms - m * 60000;
		long s = ms / 1000;
		ms = ms - s * 1000;
		StringBuffer ret = new StringBuffer("");
		if(h > 0)
		{
			ret.append(h);
			ret.append("h ");
		}
		if(m > 0)
		{
			ret.append(m);
			ret.append("min ");
		}
		// if(s>0)
		// {
		ret.append(s);
		ret.append(",");
		ret.append(ms / 100);
		ret.append("s ");
		/*
		 * } if(h==0 && m==0 && s==0) { ret.append(ms); ret.append("ms"); }
		 */
		return ret.toString();
	}

	/**
	 * Find a package name from a path. Searches the most specific classpath and
	 * uses the rest of the pathname as package name.
	 * 
	 * @param path The directory.
	 * @return The package.
	 */
	public static String convertPathToPackage(String path, URL[] urls)
	{
		String ret = null;
		File fpath = new File(path);
		if(!fpath.isDirectory())
			path = fpath.getParent();

		java.util.List toks = SCollection.createArrayList();
		StringTokenizer stok = new StringTokenizer(path, File.separator);
		while(stok.hasMoreTokens())
			toks.add(stok.nextToken());

		int quality = 0;
		for(int i = 0; i<urls.length; i++)
		{
			String cp = urls[i].getFile();
			stok = new StringTokenizer(cp, "/!"); // Exclamation mark to support
													// jar files.
			int cplen = stok.countTokens();
			if(cplen <= toks.size())
			{
				int j = 0;
				for(; stok.hasMoreTokens(); j++)
				{
					if(!stok.nextToken().equals(toks.get(j)))
						break;
				}

				if(j == cplen && cplen > quality)
				{
					ret = "";
					for(int k = j; k < toks.size(); k++)
					{
						if(k > j && k < toks.size())
							ret += ".";
						ret += "" + toks.get(k);
					}
					quality = cplen;
				}
			}
		}
		return ret;
	}
	
//	/**
//	 * Find a package name from a path. Searches the most specific classpath and
//	 * uses the rest of the pathname as package name.
//	 * 
//	 * @param path The directory.
//	 * @return The package.
//	 */
//	public static String convertPathToPackage(String path,
//			ClassLoader classloader)
//	{
//		String ret = null;
//		File fpath = new File(path);
//		if(!fpath.isDirectory())
//			path = fpath.getParent();
//
//		List cps = getClasspathURLs(classloader);
//
//		java.util.List toks = SCollection.createArrayList();
//		StringTokenizer stok = new StringTokenizer(path, File.separator);
//		while(stok.hasMoreTokens())
//			toks.add(stok.nextToken());
//
//		int quality = 0;
//		for(int i = 0; i < cps.size(); i++)
//		{
//			String cp = ((URL)cps.get(i)).getFile();
//			stok = new StringTokenizer(cp, "/!"); // Exclamation mark to support
//													// jar files.
//
//			int cplen = stok.countTokens();
//			if(cplen <= toks.size())
//			{
//				int j = 0;
//				for(; stok.hasMoreTokens(); j++)
//				{
//					if(!stok.nextToken().equals(toks.get(j)))
//						break;
//				}
//
//				if(j == cplen && cplen > quality)
//				{
//					ret = "";
//					for(int k = j; k < toks.size(); k++)
//					{
//						if(k > j && k < toks.size())
//							ret += ".";
//						ret += "" + toks.get(k);
//					}
//					quality = cplen;
//				}
//			}
//		}
//		return ret;
//	}

	// /**
	// * Get the classloader.
	// * Uses the context class loader, if available.
	// */
	// public static ClassLoader getClassLoader()
	// {
	// ClassLoader ret = Thread.currentThread().getContextClassLoader();
	// if(ret==null)
	// {
	// ret = SReflect.class.getClassLoader();
	// }
	// return ret;
	// }

	/**
	 * Get the current classpath as a list of URLs
	 */
	public static List<URL> getClasspathURLs(ClassLoader classloader)
	{
		if(classloader == null)
			classloader = SUtil.class.getClassLoader();

		Set<URL> cps = new LinkedHashSet<URL>(); 
				
		StringTokenizer stok = new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator"));
		while(stok.hasMoreTokens())
		{
			try
			{
				String entry = stok.nextToken();
				File file = new File(entry);
				cps.add(file.getCanonicalFile().toURI().toURL());
				
				// Code below does not work for paths with spaces in it.
				// Todo: is above code correct in all cases? (relative/absolute, local/remote, jar/directory)
//				if(file.isDirectory()
//						&& !entry
//								.endsWith(System.getProperty("file.separator")))
//				{
//					// Normalize, that directories end with "/".
//					entry += System.getProperty("file.separator");
//				}
//				cps.add(new URL("file:///" + entry));
			}
			catch(MalformedURLException e)
			{
				// Maybe invalid classpath entries --> just ignore.
				// Hack!!! Print warning?
				// e.printStackTrace();
			}
			catch(IOException e)
			{
			}
		}

		if(classloader instanceof URLClassLoader)
		{
			URL[] urls = ((URLClassLoader)classloader).getURLs();
			for(int i = 0; i < urls.length; i++)
				cps.add(urls[i]);
		}
		
		cps.addAll(collectClasspathURLs(classloader));
		return new ArrayList<URL>(cps);
	}
	
	/**
	 *  Get other contained (but not directly managed) urls from parent classloaders.
	 *  @return The set of urls.
	 */
	public static Set<URL>	collectClasspathURLs(ClassLoader baseloader)
	{
		Set<URL> ret = new LinkedHashSet<URL>();
		SUtil.collectClasspathURLs(baseloader, ret, new HashSet<String>());
		return ret;
	}
	
	/**
	 *  Collect all URLs belonging to a class loader.
	 */
	protected static void	collectClasspathURLs(ClassLoader classloader, Set<URL> set, Set<String> jarnames)
	{
		assert classloader!=null;
		
		if(classloader.getParent()!=null)
		{
			collectClasspathURLs(classloader.getParent(), set, jarnames);
		}
		
		if(classloader instanceof URLClassLoader)
		{
			URL[] urls = ((URLClassLoader)classloader).getURLs();
			for(int i=0; i<urls.length; i++)
			{
				String	name	= SUtil.getFile(urls[i]).getName();
				if(name.endsWith(".jar"))
				{
					String jarname	= getJarName(name);
					jarnames.add(jarname);
				}
			}
			
			for(int i = 0; i < urls.length; i++)
			{
				set.add(urls[i]);
				collectManifestURLs(urls[i], set, jarnames);
			}
		}
	}
	
	/**
	 *  Get the name of a jar file without extension and version info.
	 */
	public static String	getJarName(String filename)
	{
		String	ret	= filename;
		int	slash	= filename.lastIndexOf('/');
		if(slash!=-1)
		{
			ret	= ret.substring(slash+1);
		}
		Scanner	s	= new Scanner(ret);
		s.findWithinHorizon("(.*?)(-[0-9]+\\.|\\.jar)", 0);
		ret	= s.match().group(1);
//		System.out.println("jar: "+filename+" is "+ret);
		s.close();
		return ret;
	}
	
	/**
	 *  Collect all URLs as specified in a manifest.
	 */
	public static void	collectManifestURLs(URL url, Set<URL> set, Set<String> jarnames)
	{
		File	file	= SUtil.urlToFile(url.toString());
		if(file!=null && file.exists() && !file.isDirectory())	// Todo: load manifest also from directories!?
		{
			JarFile jarfile = null;
	        try 
	        {
	            jarfile	= new JarFile(file);
	            Manifest manifest = jarfile.getManifest();
	            if(manifest!=null)
	            {
	                String	classpath	= manifest.getMainAttributes().getValue(new Attributes.Name("Class-Path"));
	                if(classpath!=null)
	                {
	                	StringTokenizer	tok	= new StringTokenizer(classpath, " ");
	            		while(tok.hasMoreElements())
	            		{
	            			String path = tok.nextToken();
	            			File	urlfile;
	            			
	            			// Search in directory of original jar (todo: also search in local dir!?)
	            			urlfile = new File(file.getParentFile(), path);
	            			
	            			// Try as absolute path
	            			if(!urlfile.exists())
	            			{
	            				urlfile	= new File(path);
	            			}
	            			
	            			// Try as url
	            			if(!urlfile.exists())
	            			{
	            				urlfile	= SUtil.urlToFile(path);
	            			}
	
	            			if(urlfile!=null && urlfile.exists())
	            			{
		            			try
			                	{
		            				if(urlfile.getName().endsWith(".jar"))
		            				{
		            					String jarname	= getJarName(urlfile.getName());
		            					jarnames.add(jarname);
		            				}
		            				URL depurl = urlfile.toURI().toURL();
		            				set.add(depurl);
		            				collectManifestURLs(depurl, set, jarnames);
		            			}
		                    	catch (Exception e)
		                    	{
		                    		//component.getLogger().warning("Error collecting manifest URLs for "+urlfile+": "+e);
		                    	}
	                    	}
	            			else if(!path.endsWith(".jar") || !jarnames.contains(getJarName(path)))
	            			{
	            				//component.getLogger().warning("Jar not found: "+file+", "+path);
	            			}
	               		}
	                }
	            }
		    }
		    catch(Exception e)
		    {
				//component.getLogger().warning("Error collecting manifest URLs for "+url+": "+e);
		    }
	        finally
	        {
	        	try
	        	{
	        		if(jarfile!=null)
	        			jarfile.close();
	        	}
	        	catch(Exception e)
	        	{
	        	}
	        }
		}
	}

	/**
	 * Calculate the cartesian product of parameters. Example: names = {"a",
	 * "b"}, values = {{"1", "2"}, {"3", "4"}} result = {{"a"="1", "b"="3"},
	 * {"a"="2", "b"="3"}, {"a"="1", "b"="4"}, {"a=2", b="4"}}
	 * 
	 * @param names The names.
	 * @param values The values (must be some form of collection, i.e. array,
	 *        list, iterator etc.)
	 */
	public static List calculateCartesianProduct(String[] names, Object[] values)
	{
		ArrayList ret = SCollection.createArrayList();
		if(names == null || values == null)
			return ret;
		if(names.length != values.length)
			throw new IllegalArgumentException("Must have same length: "
					+ names.length + " " + values.length);

		HashMap binding = SCollection.createHashMap();
		Iterator[] iters = new Iterator[values.length];

		for(int i = 0; i < values.length; i++)
		{
			// When one collection is empty -> no binding at all.
			// First binding consists of all first elements.
			iters[i] = SReflect.getIterator(values[i]);
			if(!iters[i].hasNext())
			{
				return ret;
			}
			else
			{
				binding.put(names[i], iters[i].next());
			}
		}
		ret.add(binding);

		// Iterate through binding sets for subsequent bindings.
		while(true)
		{
			// Calculate next binding.
			// Copy old binding and change one value.
			binding = (HashMap)binding.clone();
			int i = 0;
			for(; i < values.length && !iters[i].hasNext(); i++)
			{
				// Overflow: Re-init iterator.
				iters[i] = SReflect.getIterator(values[i]);
				binding.put(names[i], iters[i].next());
			}
			if(i < iters.length)
			{
				binding.put(names[i], iters[i].next());
			}
			else
			{
				// Overflow in last iterator: done.
				// Hack: Unnecessarily re-inits all iterators before break ?
				break;
			}
			ret.add(binding);
		}

		return ret;
	}

	/**
	 * Test if a file is a Java source file.
	 * 
	 * @param filename The filename.
	 * @return True, if it is a Java source file.
	 */
	public static boolean isJavaSourceFilename(String filename)
	{
		return filename != null && filename.toLowerCase().endsWith(".java");
	}

	/**
	 * Create a hash map from keys and values.
	 * 
	 * @param keys The keys.
	 * @param values The values.
	 * @return The map.
	 */
	public static <K,T>	Map<K, T> createHashMap(K[] keys, T[] values)
	{
		HashMap<K, T> ret = new HashMap<K, T>();
		for(int i = 0; i < keys.length; i++)
		{
			ret.put(keys[i], values[i]);
		}
		return ret;
	}

	/**
	 * Create a hash set from values.
	 * 
	 * @param values The values.
	 * @return The map.
	 */
	public static <T> Set<T> createHashSet(T[] values)
	{
		Set<T> ret = new HashSet<T>();
		for(int i = 0; i < values.length; i++)
		{
			ret.add(values[i]);
		}
		return ret;
	}

	/**
	 * Create an array list from values.
	 * 
	 * @param values The values.
	 * @return The map.
	 */
	public static <T> List<T> createArrayList(T[] values)
	{
		List<T> ret = new ArrayList<T>();
		for(int i = 0; i < values.length; i++)
		{
			ret.add(values[i]);
		}
		return ret;
	}

	/** The counter for conversation ids. */
	protected static int	convidcnt;

	/**
	 * Create a globally unique conversation id.
	 * 
	 * @return The conversation id.
	 */
	public static String createUniqueId(String name)
	{
		synchronized(SUtil.class)
		{
			// return
			// "id_"+name+"_"+System.currentTimeMillis()+"_"+Math.random()+"_"+(++convidcnt);
			// return "id_"+name+"_"+Math.random()+"_"+(++convidcnt);
			return name + "_" + Math.random() + "_" + (++convidcnt);
		}
	}

	/**
	 * Create a globally unique conversation id.
	 * 
	 * @return The conversation id.
	 */
	public static String createUniqueId(String name, int length)
	{
		UUID uuid = UUID.randomUUID();
		String rand = uuid.toString();
		return name + "_" + rand.substring(0, length);

		// String rand = ""+Math.random();
		// rand = rand.substring(2, 2+Math.min(length-2, rand.length()-2));
		// return name+"_"+rand+(++convidcnt%100);
	}	
	
	/**
	 * 
	 */
	protected static void testIntByteConversion()
	{
		Random	rnd	= new Random(123);	
		for(int i=1; i<10000000; i++)
		{
			int	val	= rnd.nextInt(Integer.MAX_VALUE);
			if(i%2==0)	// Test negative values too.
			{
				val	= -val;
			}
			byte[]	bytes	= intToBytes(val);
			int	val2	= bytesToInt(bytes);
			if(val!=val2)
			{
				throw new RuntimeException("Failed: "+val+", "+val2+", "+arrayToString(bytes));
			}
//			System.out.println("Converted: "+val+", "+arrayToString(bytes));
		}
	}

	/**
	 * Convert an absolute path to a relative path based on the current user
	 * directory.
	 */
	public static String convertPathToRelative(String absolute)
	{
		// Special treatment for files in jar file -> just make jar file name relative and keep inner name 
		if(absolute.startsWith("jar:file:") && absolute.indexOf("!")!=-1)
		{
			String	jarname	= absolute.substring(4, absolute.indexOf("!"));
			String	filename	= absolute.substring(absolute.indexOf("!"));
			jarname	= convertPathToRelative(jarname);
			return "jar:"+jarname+filename;
		}
		// Special treatment for file urls 
		if(absolute.startsWith("file:"))
		{
			String	filename	= absolute.substring(5);
			if(File.separatorChar=='\\')
			{
				filename	= filename.replace("/", "\\");
			}
			
			filename	= convertPathToRelative(filename);
			
			if(File.separatorChar=='\\')
			{
				filename	= filename.replace("\\", "/");
			}
			
			return "file:"+filename;
		}
		
		// Build path as list of files (directories).
		File basedir = new File(System.getProperty("user.dir"));
		List<File> basedirs = new ArrayList<File>();
		while(basedir != null)
		{
			basedirs.add(0, basedir);
			basedir = basedir.getParentFile();
		}

		// Build path as list of files (directories).
		File target = new File(absolute);
		List<File> targets = new ArrayList<File>();
		while(target != null)
		{
			targets.add(0, target);
			target = target.getParentFile();
		}

		// Find common path prefix
		int index = 0;
		while(index < basedirs.size() && index < targets.size()
				&& basedirs.get(index).equals(targets.get(index)))
		{
			index++;
		}
		
		String ret;
		// Relative if common directory on drive exists.
		if(index>1)
		{
			StringBuffer buf = new StringBuffer();
			for(int i = index; i < basedirs.size(); i++)
			{
				buf.append("..");
				buf.append(File.separatorChar);
			}
			for(int i = index; i < targets.size(); i++)
			{
				buf.append(targets.get(i).getName());
				if(i != targets.size() - 1)
					buf.append(File.separatorChar);
			}
			ret = buf.toString();
		}
		else
		{
			ret = absolute;
		}
		
//		System.out.println("CPtR: "+ret+", "+absolute);

		return ret;
	}
	
	/**
	 *  Convert a file/string/url array.
	 *  @param urls The url strings.
	 *  @return The urls.
	 */
	public static URL[] toURLs(Object[] urls)
	{
		if(urls==null)
			return null;
		
		URL[] ret = new URL[urls.length];
		for(int i=0; i<urls.length; i++)
		{
			ret[i] = toURL(urls[i]);
		}
		return ret;
	}
		
	/**
	 *  Convert a file/string/url.
	 */
	public static URL toURL(Object url)
	{
		URL	ret	= null;
		boolean	jar	= false;
		if(url instanceof String)
		{
			String	string	= (String) url;
			if(string.startsWith("file:") || string.startsWith("jar:file:"))
			{
				try
				{
					string	= URLDecoder.decode(string, "UTF-8");
				}
				catch(UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}
			}
			
			jar	= string.startsWith("jar:file:");
			url	= jar ? new File(string.substring(9))
				: string.startsWith("file:") ? new File(string.substring(5)) : null;
			
			
			if(url==null)
			{
				File file	= new File(string);
				if(file.exists())
				{
					url	= file;
				}
				else
				{
					file	= new File(System.getProperty("user.dir"), string);
					if(file.exists())
					{
						url	= file;
					}
					else
					{
						try
						{
							url	= new URL(string);
						}
						catch (MalformedURLException e)
						{
							throw new RuntimeException(e);
						}
					}
				}
			}
		}
		
		if(url instanceof URL)
		{
			ret	= (URL)url;
		}
		else if(url instanceof File)
		{
			try
			{
				String	abs	= ((File)url).getAbsolutePath();
				String	rel	= SUtil.convertPathToRelative(abs);
				ret	= abs.equals(rel) ? new File(abs).getCanonicalFile().toURI().toURL()
					: new File(System.getProperty("user.dir"), rel).getCanonicalFile().toURI().toURL();
				if(jar)
				{
					if(ret.toString().endsWith("!"))
						ret	= new URL("jar:"+ret.toString()+"/");	// Add missing slash in jar url.
					else
						ret	= new URL("jar:"+ret.toString());						
				}
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
		return ret;
	}

	/**
	 * Main method for testing. / public static void main(String[] args) {
	 * String res1 = getRelativePath("c:/a/b/c", "c:/a/d"); String res2 =
	 * getRelativePath("c:/a/b/c", "c:/a/b/c"); //String res2 =
	 * getRelativePath("c:/a/b/c", "d:/a/d"); String res3 =
	 * getRelativePath("c:/a/b/c", "c:/a/b/c/d/e"); //String tst =
	 * "wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww"
	 * ; //System.out.println(tst); //System.out.println(SUtil.wrapText(tst));
	 * /*String[] a = new String[]{"a1", "a2", "a3"}; Integer[] b = new
	 * Integer[]{new Integer(1), new Integer(2), new Integer(3)};
	 * System.out.println(arrayToString(joinArbitraryArrays(new Object[]{a,
	 * b})));
	 */

	/*
	 * try { URL target = new URL("file:///C:/projects/jadex/lib/examples.jar");
	 * DynamicURLClassLoader loader = new DynamicURLClassLoader(new URL[0]); try
	 * { Class clazz = loader.loadClass("jadex.examples.ping.PingPlan");
	 * System.out.println("Loaded class: "+clazz); }
	 * catch(ClassNotFoundException e){System.out.println(e);}
	 * loader.addURL(target); try { Class clazz =
	 * loader.loadClass("jadex.examples.ping.PingPlan");
	 * System.out.println("Loaded class: "+clazz); }
	 * catch(ClassNotFoundException e){System.out.println(e);} } catch(Exception
	 * e) { System.out.println(e); } }
	 */
	
	/**
	 *  Convert bytes to a short.
	 */
	public static short bytesToShort(byte[] buffer)
	{
		assert buffer.length == 2;

		short value = (short)((0xFF & buffer[0]) << 8);
		value |= (0xFF & buffer[1]);

		return value;
	}

	/**
	 *  Convert a short to bytes.
	 */
	public static byte[] shortToBytes(int val)
	{
		byte[] buffer = new byte[2];

		buffer[0] = (byte)((val >>> 8) & 0xFF);
		buffer[1] = (byte)(val & 0xFF);

		return buffer;
	}

	/**
	 *  Convert bytes to an integer.
	 */
	public static int bytesToInt(byte[] buffer)
	{
		assert buffer.length == 4;
//		if(buffer.length != 4)
//		{
//			throw new IllegalArgumentException("buffer length must be 4 bytes!");
//		}

		int value = (0xFF & buffer[0]) << 24;
		value |= (0xFF & buffer[1]) << 16;
		value |= (0xFF & buffer[2]) << 8;
		value |= (0xFF & buffer[3]);

		return value;
	}

	/**
	 *  Convert an integer to bytes.
	 */
	public static byte[] intToBytes(int val)
	{
		byte[] buffer = new byte[4];

		buffer[0] = (byte)((val >>> 24) & 0xFF);
		buffer[1] = (byte)((val >>> 16) & 0xFF);
		buffer[2] = (byte)((val >>> 8) & 0xFF);
		buffer[3] = (byte)(val & 0xFF);

		return buffer;
	}
	
//	/**
//	 *  Convert an ip to a long.
//	 *  @param ip The ip address.
//	 *  @return The long.
//	 */
//	public static long ipToLong(InetAddress ip)
//	{
//		byte[] octets = ip.getAddress();
//		long result = 0;
//		for(byte octet : octets)
//		{
//			result <<= 8;
//			result |= octet & 0xff;
//		}
//		return result;
//	}

	/**
	 *  Convert bytes to an integer.
	 */
	public static long bytesToLong(byte[] buffer)
	{
		assert buffer.length == 8;
//		if(buffer.length != 8)
//		{
//			throw new IllegalArgumentException("Buffer length must be 8 bytes: "+arrayToString(buffer));
//		}

		long value = (0xFFL & buffer[0]) << 56L;
		value |= (0xFFL & buffer[1]) << 48L;
		value |= (0xFFL & buffer[2]) << 40L;
		value |= (0xFFL & buffer[3]) << 32L;
		value |= (0xFFL & buffer[4]) << 24L;
		value |= (0xFFL & buffer[5]) << 16L;
		value |= (0xFFL & buffer[6]) << 8L;
		value |= (0xFFL & buffer[7]);

		return value;
	}

	/**
	 *  Convert an integer to bytes.
	 */
	public static byte[] longToBytes(long val)
	{
		byte[] buffer = new byte[8];

		buffer[0] = (byte)((val >>> 56) & 0xFF);
		buffer[1] = (byte)((val >>> 48) & 0xFF);
		buffer[2] = (byte)((val >>> 40) & 0xFF);
		buffer[3] = (byte)((val >>> 32) & 0xFF);
		buffer[4] = (byte)((val >>> 24) & 0xFF);
		buffer[5] = (byte)((val >>> 16) & 0xFF);
		buffer[6] = (byte)((val >>> 8) & 0xFF);
		buffer[7] = (byte)(val & 0xFF);

		return buffer;
	}
	
	/**
	 *  Get the network ip for an internet address and the prefix length.
	 *  Example: ip: 134.100.33.22 / prefixlen: 24 (c class) -> 134.100.33.0
	 *  @param addr The internet address.
	 *  @param prefixlen The prefix length.
	 *  @return The net address.
	 */
	public static InetAddress getNetworkIp(InetAddress addr, short prefixlen)
	{
		InetAddress ret = null;
		try
		{
			if(addr instanceof Inet4Address)
			{
				int ad = SUtil.bytesToInt(addr.getAddress());
				ad >>>= 32-prefixlen;
				ad <<= 32-prefixlen;
				ret = InetAddress.getByAddress(SUtil.intToBytes(ad));
			}
			else if(addr instanceof Inet6Address)
			{
				// Use only first 64 bit of IPv6 address.
				byte[]	baddr	= new byte[8];
				System.arraycopy(addr.getAddress(), 0, baddr, 0, 8);
				long ad = SUtil.bytesToLong(baddr);
				System.arraycopy(SUtil.longToBytes(ad), 0, baddr, 0, 8);
				ad >>>= 8;
				System.arraycopy(SUtil.longToBytes(ad), 0, baddr, 0, 8);
				ad <<= 8;
				baddr	= new byte[16];
				System.arraycopy(SUtil.longToBytes(ad), 0, baddr, 0, 8);
				ret = InetAddress.getByAddress(baddr);
			}
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		return ret;
	}
	
	/**
	 *  Get bytes as human readable string.
	 */
	public static String	bytesToString(long bytes)
	{
		String ret;
		if(bytes>0)
		{
		    int	unit	= (int)(Math.log10(bytes)/Math.log10(1024));	// 1=bytes, 2=kBytes, ...
		    double	value	= bytes/Math.pow(1024, unit);	// value between 1.0 .. 1023.999...
		    ret	= value>=100 ? BYTEFORMATTER3.format(value)
		    	: value>=10 ? BYTEFORMATTER2.format(value)
		    	: BYTEFORMATTER1.format(value);
		    ret	+= " "+BYTE_UNITS[unit];
		}
		else
		{
			ret = bytes + BYTE_UNITS[0];
		}
	    return ret;
		
//		String	ret;
//		if(bytes>=1024*1024*1024*100L)
//		{
//			ret	= bytes/(1024*1024*1024) + " GB"; 
//		}
//		else if(bytes>=1024*1024*1024*10L)
//		{
//			ret	= (bytes*10/(1024*1024*1024))/10.0 + " GB"; 
//		}
//		else if(bytes>=1024*1024*1024)
//		{
//			ret	= (bytes*100/(1024*1024*1024))/100.0 + " GB"; 
//		}
//		else if(bytes>=1024*1024*100)
//		{
//			ret	= bytes/(1024*1024) + " MB"; 
//		}
//		else if(bytes>=1024*1024*10)
//		{
//			ret	= (bytes*10/(1024*1024))/10.0 + " MB"; 
//		}
//		else if(bytes>=1024*1024)
//		{
//			ret	= (bytes*100/(1024*1024))/100.0 + " MB"; 
//		}
//		else if(bytes>=1024*100)
//		{
//			ret	= bytes/1024 + " KB"; 
//		}
//		else if(bytes>=1024*10)
//		{
//			ret	= (bytes*10/1024)/10.0 + " KB"; 
//		}
//		else if(bytes>=1024)
//		{
//			ret	= (bytes*100/1024)/100.0 + " KB"; 
//		}
//		else
//		{
//			ret	= Long.toString(bytes)+ " B"; 
//		}
//		return ret;
	}


	/**
	 *  Convert an URL to a local file name.
	 *  @param url The url.
	 *  @return The absolute path to the url resource.
	 */
	public static String convertURLToString(URL url)
	{
		File f;
		try
		{
			f	= new File(url.toURI());
		}
		catch(Exception e)
		{
			f	= new File(url.getPath());
		}
		
		// Hack!!! Above code doesnt handle relative url paths. 
		if(!f.exists())
		{
			File newfile = new File(new File("."), url.getPath());
			if(newfile.exists())
			{
				f = newfile;
			}
		}
		
		return f.getAbsolutePath();
	}
	
	/**
	 *  Test if a file name is contained.
	 */
	public static int indexOfFilename(String url, List<String> urlstrings)
	{
		int ret = -1;
		try
		{
			File file = urlToFile(url);
			if(file==null)
				file	= new File(url);
			for(int i=0; file!=null && i<urlstrings.size() && ret==-1; i++)
			{
				String	totest	= (String)urlstrings.get(i);
				File test = urlToFile(totest);
				if(test==null)
					test	= new File(totest);
				if(test!=null && file.getCanonicalPath().equals(test.getCanonicalPath()))
					ret = i;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 *  Convert an URL to a file.
	 *  @return null, if the URL is neither 'file:' nor 'jar:file:' URL and no path point to an existing file.
	 */
	public static File urlToFile(String url)
	{
		File	file	= null;
		if(url.startsWith("file:"))
		{
			try
			{
				url	= URLDecoder.decode(url, "UTF-8");
			}
			catch(UnsupportedEncodingException uee)
			{
			}
			file	= new File(url.substring(5));
		}
		else if(url.startsWith("jar:file:"))
		{
			try
			{
				url	= URLDecoder.decode(url, "UTF-8");
			}
			catch(UnsupportedEncodingException uee)
			{
			}
			file	= new File(url.substring(9));
		}
		else
		{
			file	= new File(url);
			if(!file.exists())
			{
				file	= null;
			}
		}
		return file;
	}	

	/**
	 *  Add a listener to System.out.
	 */
	public static synchronized void	addSystemOutListener(IChangeListener listener)
	{
		if(!(System.out instanceof AccessiblePrintStream)
			|| !(((AccessiblePrintStream)System.out).out instanceof ListenableStream))
		{
			System.setOut(new AccessiblePrintStream(new ListenableStream(System.out, "out")));
		}
		((ListenableStream)((AccessiblePrintStream)System.out).out).addLineListener(listener);
	}
	
	/**
	 *  Remove a listener from System.out.
	 */
	public static synchronized void	removeSystemOutListener(IChangeListener listener)
	{
		if(System.out instanceof AccessiblePrintStream
			&& ((AccessiblePrintStream)System.out).out instanceof ListenableStream)
		{
			((ListenableStream)((AccessiblePrintStream)System.out).out).removeLineListener(listener);
		}
	}
	
	/**
	 *  Add a listener to System.err.
	 */
	public static synchronized void	addSystemErrListener(IChangeListener listener)
	{
		if(!(System.err instanceof AccessiblePrintStream)
			|| !(((AccessiblePrintStream)System.err).out instanceof ListenableStream))
		{
			System.setErr(new AccessiblePrintStream(new ListenableStream(System.err, "err")));
		}
		((ListenableStream)((AccessiblePrintStream)System.err).out).addLineListener(listener);
	}
	
	/**
	 *  Remove a listener from System.err.
	 */
	public static synchronized void	removeSystemErrListener(IChangeListener listener)
	{
		if(System.err instanceof AccessiblePrintStream
			&& ((AccessiblePrintStream)System.err).out instanceof ListenableStream)
		{
			((ListenableStream)((AccessiblePrintStream)System.err).out).removeLineListener(listener);
		}
	}
	
	/**
	 *  Get an output stream that is automatically fed into the new System.in,
	 *  i.e. this method replaces System.in and delivers an output stream to
	 *  which can be written.
	 *  @param tp The thread pool.
	 */
	public static synchronized OutputStream getOutForSystemIn(IThreadPool tp) throws IOException
	{
		OutputStream ret;
		
		if(System.in instanceof CombinedInputStream)
		{
			ret = ((CombinedInputStream)System.in).getOutin();
		}
		else
		{
			PipedOutputStream pos = new PipedOutputStream();
			CombinedInputStream cis = new CombinedInputStream(new ProtectedInputStream(System.in), pos, tp);
			System.setIn(cis);
			ret = pos;
		}
		
		return ret;
	}
	
	/**
	 *  Get a IPV4 address of the local host.
	 *  Ignores loopback address and V6 addresses.
	 *  @return First found IPV4 address.
	 */
	public static InetAddress getInet4Address()
	{
		InetAddress ret = null;
		
		try
		{
			for(NetworkInterface ni: getNetworkInterfaces())
			{
				Enumeration e2 = ni.getInetAddresses();
				while(e2.hasMoreElements() && ret==null)
				{
					InetAddress tmp = (InetAddress)e2.nextElement();
					if(tmp instanceof Inet4Address && !tmp.isLoopbackAddress())
						ret = (InetAddress)tmp;
				}
			}
			
			if(ret==null)
			{
				InetAddress tmp = InetAddress.getLocalHost();
				if(tmp instanceof Inet4Address && !tmp.isLoopbackAddress())
					ret = (InetAddress)tmp;
			}
		}
		catch(Exception e)
		{
//			e.printStackTrace();
		}
		
		return ret;
	}
	
	/**
	 *  Get a IPV4 address of the local host.
	 *  Ignores loopback address and V4 addresses.
	 *  @return First found IPV4 address.
	 */
	public static InetAddress getInet6Address()
	{
		InetAddress ret = null;
		
		try
		{
			for(NetworkInterface ni: getNetworkInterfaces())
			{
				Enumeration e2 = ni.getInetAddresses();
				while(e2.hasMoreElements() && ret==null)
				{
					InetAddress tmp = (InetAddress)e2.nextElement();
					if(tmp instanceof Inet6Address && !tmp.isLoopbackAddress())
						ret = (InetAddress)tmp;
				}
			}
			
			if(ret==null)
			{
				InetAddress tmp = InetAddress.getLocalHost();
				if(tmp instanceof Inet6Address && !tmp.isLoopbackAddress())
					ret = (InetAddress)tmp;
			}
		}
		catch(Exception e)
		{
//			e.printStackTrace();
		}
		
		return ret;
	}
	
	/**
	 *  Get an address of the local host.
	 *  Tries to get a IPV4 address and if not available 
	 *  tries to get a IPV6 address.
	 *  @return First found IPV4 or IPV6 address.
	 */
	public static InetAddress getInetAddress()
	{
		InetAddress ret = getInet4Address();
		if(ret==null)
			ret = getInet6Address();
		return ret;
	}
	
	/**
	 *  Get the network prefix length for IPV4 address
	 *  24=C, 16=B, 8=A classes. 
	 *  Returns -1 in case of V6 address.
	 *  @param iadr The address.
	 *  @return The length of the prefix.
	 */
	public static short getNetworkPrefixLength(InetAddress iadr)
	{
		short ret = -1;
		if(!SReflect.isAndroid() || SReflect.getAndroidVersion() > 8)
		{
			ret	= SNonAndroid.getNetworkPrefixLength(iadr);
		}
		
		return ret;
	}
	
	/**
	 *  Get the source code base using a packagename and a filename.
	 *  Looks at the filename and subtracts the package name.
	 *  @param filename The filename.
	 *  @param pck The package name.
	 *  @return The source base. 
	 */
	public static String getCodeSource(String filename, String pck)
	{
		// Use unix separator for file or jar URLs.
		char	sep	= filename.startsWith("file:") || filename.startsWith("jar:file:") ? '/' : File.separatorChar;
		int occ = pck!=null? countOccurrences(pck, '.')+2: 1;
		String ret = filename;
		for(int i=0; i<occ; i++)
		{
			int idx = ret.lastIndexOf(sep);
			if(idx>0)
			{
				ret = ret.substring(0, idx);
			}
			else
			{
				// Try other variant before fail
				if('/'!=File.separatorChar)
				{
					if('/'==sep)
						sep = File.separatorChar;
					else
						sep = '/';
					idx = ret.lastIndexOf(sep);
					if(idx>0)
						ret = ret.substring(0, idx);
					else
						throw new RuntimeException("Corrupt filename: "+filename);
				}
				else
				{
					throw new RuntimeException("Corrupt filename: "+filename);
				}
			}
		}
		
		if(ret.startsWith("jar:file:") && ret.endsWith("!"))
		{
			// Strip 'jar:' and '!', because java.net.URL doesn't like jar URLs without...
			ret	= ret.substring(4, ret.length()-1);
		}
		
		return ret;
	}
	
	/**
	 *  Count the occurrences of a char in a string.
	 *  @param string The string.
	 *  @param find The char to find.
	 *  @return The number of occurrences.
	 */
	public static int countOccurrences(String string, char find)
	{
	    int count = 0;
	    for(int i=0; i < string.length(); i++)
	    {
	        if(string.charAt(i) == find)
	        {
	             count++;
	        }
	    }
	    return count;
	}

	/** The cached network interfaces. */
	protected static List<NetworkInterface>	NIS;
	
	/** The time of the last caching of network interfaces. */
	protected static long	NISTIME;
	
	/**
	 *  Get the network interfaces.
	 *  The result is cached for a short time period to speed things up.
	 */
	public static List<NetworkInterface> getNetworkInterfaces()	throws SocketException
	{
		if(NIS==null || (System.currentTimeMillis()-NISTIME)>30000)
		{
			NIS = Collections.list(NetworkInterface.getNetworkInterfaces());
			NISTIME	= System.currentTimeMillis();
		}
		return NIS;
	}
	
	/**
	 *  Get the addresses to be used for transports.
	 */
	public static String[]	getNetworkAddresses() throws SocketException
	{
		// Determine useful transport addresses.
		Set<String>	addresses	= new HashSet<String>();	// global network addresses (uses all)
		Set<InetAddress>	sitelocal	= new HashSet<InetAddress>();	// local network addresses e.g. 192.168.x.x (use one v4 and one v6 if no global)
		Set<InetAddress>	linklocal	= new HashSet<InetAddress>();	// link-local fallback addresses e.g. 169.254.x.x (use one v4 and one v6 if no global or local)
		Set<InetAddress>	loopback	= new HashSet<InetAddress>();	// loopback addresses e.g. 127.0.0.1 (use one v4 and one v6 if no global or local or link-local)
		
		boolean	v4	= false;	// true when one v4 address was added.
		boolean	v6	= false;	// true when one v6 address was added.
		
		for(NetworkInterface ni: getNetworkInterfaces())
		{
			for(Enumeration<InetAddress> iadrs = ni.getInetAddresses(); iadrs.hasMoreElements(); )
			{
				InetAddress addr = iadrs.nextElement();
//				System.out.println("addr: "+addr+" "+addr.isAnyLocalAddress()+" "+addr.isLinkLocalAddress()+" "+addr.isLoopbackAddress()+" "+addr.isSiteLocalAddress()+", "+ni.getDisplayName());
				if(addr.isLoopbackAddress())
				{
					loopback.add(addr);
				}
				else if(addr.isLinkLocalAddress())
				{
					linklocal.add(addr);
				}
				else if(addr.isSiteLocalAddress())
				{
					sitelocal.add(addr);
				}
				else
				{
					v4	= v4 || addr instanceof Inet4Address;
					v6	= v6 || addr instanceof Inet6Address;
					addresses.add(addr.getHostAddress());
				}
			}
		}

		boolean	tmpv4	= v4;
		boolean	tmpv6	= v6;
		for(Iterator<InetAddress> it=sitelocal.iterator(); it.hasNext(); )
		{
			InetAddress	addr	= it.next();
			if(!tmpv4 && addr instanceof Inet4Address || !tmpv6 && addr instanceof Inet6Address)
			{
				v4	= v4 || addr instanceof Inet4Address;
				v6	= v6 || addr instanceof Inet6Address;
				addresses.add(addr.getHostAddress());
			}
		}
		
		tmpv4	= v4;
		tmpv6	= v6;
		for(Iterator<InetAddress> it=linklocal.iterator(); it.hasNext(); )
		{
			InetAddress	addr	= it.next();
			if(!tmpv4 && addr instanceof Inet4Address || !tmpv6 && addr instanceof Inet6Address)
			{
				v4	= v4 || addr instanceof Inet4Address;
				v6	= v6 || addr instanceof Inet6Address;
				addresses.add(addr.getHostAddress());
			}
		}
		
		tmpv4	= v4;
		tmpv6	= v6;
		for(Iterator<InetAddress> it=loopback.iterator(); it.hasNext(); )
		{
			InetAddress	addr	= it.next();
			if(!tmpv4 && addr instanceof Inet4Address || !tmpv6 && addr instanceof Inet6Address)
			{
				v4	= v4 || addr instanceof Inet4Address;
				v6	= v6 || addr instanceof Inet6Address;
				addresses.add(addr.getHostAddress());
			}
		}
		
//		InetAddress iaddr = InetAddress.getLocalHost();
//		String lhostname = iaddr.getCanonicalHostName();
//		InetAddress[] laddrs = InetAddress.getAllByName(lhostname);
//
//		addrs.add(getAddress(iaddr.getHostAddress(), this.port));
//		// Get the ip addresses
//		for(int i=0; i<laddrs.length; i++)
//		{
//			String hostname = laddrs[i].getHostName().toLowerCase();
//			String ip_addr = laddrs[i].getHostAddress();
//			addrs.add(getAddress(ip_addr, this.port));
//			if(!ip_addr.equals(hostname))
//			{
//				// We have a fully qualified domain name.
//				addrs.add(getAddress(hostname, this.port));
//			}
//		}
		
//		System.out.println("addresses: "+addresses);
		
		return addresses.toArray(new String[addresses.size()]);		
	}
	
	/**
	 *  Unzip a file into a specific dir.
	 *  @param zip The zip file.
	 *  @param dir The target dir.
	 */
	public static void unzip(ZipFile zip, File dir)
	{
		Enumeration<? extends ZipEntry> files = zip.entries();
		FileOutputStream fos = null;
		InputStream is = null;
		
		for(ZipEntry entry=files.nextElement(); files.hasMoreElements(); entry=files.nextElement())
		{
			try
			{
				is = zip.getInputStream(entry);
				byte[] buffer = new byte[8192];
				int bytesRead = 0;

				File f = new File(dir.getAbsolutePath()+ File.separator + entry.getName());

				if(entry.isDirectory())
				{
					f.mkdirs();
					continue;
				}
				else
				{
					f.getParentFile().mkdirs();
					f.createNewFile();
				}

				fos = new FileOutputStream(f);

				while((bytesRead = is.read(buffer))!= -1)
				{
					fos.write(buffer, 0, bytesRead);
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				if(fos!=null)
				{
					try
					{
						fos.close();
					}
					catch(IOException e)
					{
					}
				}
			}
		}
		if(is!=null)
		{
			try
			{
				is.close();
			}
			catch(IOException e)
			{
			}
		}
		if(zip!=null)
		{
			try
			{
				zip.close();
			}
			catch(IOException e)
			{
			}
		}
	}
	
	/**
	 *  Delete a directory completely (including all subdirs and files).
	 *  @param dir The dir to delete. 
	 *  @return True, if was successfully deleted.
	 */
	static public boolean deleteDirectory(File dir)
	{
		if(dir.exists() && dir.isDirectory())
		{
			File[] files = dir.listFiles();
			if(files!=null)
			{
				for(int i=0; i<files.length; i++)
				{
					if(files[i].isDirectory())
					{
						deleteDirectory(files[i]);
					}
					else
					{
						files[i].delete();
					}
				}
			}
		}
		return dir.delete();
	}
	
	/**
	 *  An subclass of print stream to allow accessing the underlying stream.
	 */
	public static class AccessiblePrintStream	extends PrintStream
	{
		//-------- attributes --------
		
		/** The underlying output stream. */
		protected OutputStream	out;
		
		//-------- constructors --------
		
		/**
		 *  Create an accessible output stream.
		 */
		public AccessiblePrintStream(OutputStream out)
		{
			super(out);
			this.out	= out;
		}
	}

	//-------- abstractions for Android --------
	
	/**
	 *  Get the home directory.
	 */
	public static File	getHomeDirectory()
	{
		return SReflect.isAndroid() ? new File(System.getProperty("user.home")) : SNonAndroid.getHomeDirectory();		
	}
	
	/**
	 *  Get the home directory.
	 */
	public static File	getDefaultDirectory()
	{
		// Todo: default directory on android?
		return SReflect.isAndroid() ? new File(System.getProperty("user.home")) : SNonAndroid.getDefaultDirectory();		
	}
	
	/**
	 *  Get the parent directory.
	 */
	public static File	getParentDirectory(File file)
	{
		// Todo: parent directory on android?
		return SReflect.isAndroid() ? file.getParentFile() : SNonAndroid.getParentDirectory(file);		
	}

	/**
	 *  Get the files of a directory.
	 */
	public static File[]	getFiles(File file, boolean hiding)
	{
		// Todo: hidden files on android?
		return SReflect.isAndroid() ? file.listFiles() : SNonAndroid.getFiles(file, hiding);		
	}
	
	/**
	 *  Get the prefix length of a file.
	 */
	public static int getPrefixLength(File file)
	{
		int	ret	= 0;
		try
		{
			Method m = File.class.getDeclaredMethod("getPrefixLength", new Class[0]);
			m.setAccessible(true);
			ret	= ((Integer)m.invoke(file, new Object[0])).intValue();
		}
		catch(Exception e)
		{
			// Hack!!! assume unix as default
			String	path	= file.getPath();
			if(path.startsWith("~"))	// '~/' or '~user/'
			{
				ret	= path.indexOf('/');
			}
			else if(path.startsWith("/"))
			{
				ret	= 1;
			}
		}
		
		return ret;
	}

	/**
	 *  Check if a file represents a floppy.
	 *  Returns false on android.
	 */
	public static boolean isFloppyDrive(File file)
	{
		return SReflect.isAndroid() ? false : SNonAndroid.isFloppyDrive(file);
	}

	/**
	 *  Get the display name (e.g. of a system drive).
	 *  Returns null on android.
	 */
	public static String getDisplayName(File file)
	{
		return SReflect.isAndroid() ? null : SNonAndroid.getDisplayName(file);
	}

	/**
	 *  Test if a call is running on a gui (e.g. Swing or Android UI) thread.
	 *  Currently returns false on android.
	 */
	public static boolean isGuiThread()
	{
		// Todo: ask android helper for android UI thread.
		return SReflect.isAndroid() ? false : SNonAndroid.isGuiThread();
	}
	
	/**
	 *  Escape a java string.
	 *  @param str The string to escape.
	 *  @return The escaped string.
	 */
	public static String escapeString(String str) 
	{
		if(str == null)
			return null;
		
		StringWriter writer = new StringWriter(str.length() * 2);
		
		boolean essq = true;
		boolean esfs = false;
		
		int sz;
		sz = str.length();
		for(int i = 0; i < sz; i++)
		{
			char ch = str.charAt(i);

			if(ch > 0xfff)
			{
				writer.write("\\u" + hex(ch));
			}
			else if(ch > 0xff)
			{
				writer.write("\\u0" + hex(ch));
			}
			else if(ch > 0x7f)
			{
				writer.write("\\u00" + hex(ch));
			}
			else if(ch < 32)
			{
				switch(ch)
				{
					case '\b':
						writer.write('\\');
						writer.write('b');
						break;
					case '\n':
						writer.write('\\');
						writer.write('n');
						break;
					case '\t':
						writer.write('\\');
						writer.write('t');
						break;
					case '\f':
						writer.write('\\');
						writer.write('f');
						break;
					case '\r':
						writer.write('\\');
						writer.write('r');
						break;
					default:
						if(ch > 0xf)
						{
							writer.write("\\u00" + hex(ch));
						}
						else
						{
							writer.write("\\u000" + hex(ch));
						}
						break;
				}
			}
			else
			{
				switch(ch)
				{
					case '\'':
						if(essq)
						{
							writer.write('\\');
						}
						writer.write('\'');
						break;
					case '"':
						writer.write('\\');
						writer.write('"');
						break;
					case '\\':
						writer.write('\\');
						writer.write('\\');
						break;
					case '/':
						if(esfs)
						{
							writer.write('\\');
						}
						writer.write('/');
						break;
					default:
						writer.write(ch);
						break;
				}
			}
		}
		
		return writer.toString();
	}
	
	/**
	 *  Convert char to hex vavlue.
	 */
	public static String hex(char ch) 
	{
		return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
	}
	
	/**
	 *  Convert a byte array to a string representation.
	 */
	public static String hex(byte[] data)
	{
		return hex(data, null, 1);
	}
	
	/**
	 *  Convert a byte array to a string representation.
	 */
	public static String hex(byte[] data, String delim, int block)
	{
		StringBuffer ret = new StringBuffer();
		for(int i=0; i<data.length; i++) 
		{
		    ret.append(String.format("%02X", data[i]));
		    if(delim!=null && i+1<data.length && (i+1)%block==0)
		    	ret.append(delim);
		}
		return ret.toString();
	}
	
	/**
	 *  Taken from ant.
	 *  Split a command line.
	 * 
	 *  @param line The command line to process.
	 *  @return The command line broken into strings. An empty or null toProcess
	 *    parameter results in a zero sized array.
	 */
	public static String[] splitCommandline(String line)
	{
		if(line == null || line.length() == 0)
		{
			// no command? no string
			return new String[0];
		}
		
		// parse with a simple finite state machine

		final int normal = 0;
		final int inquote = 1;
		final int indoublequote = 2;
		
		int state = normal;
		StringTokenizer tok = new StringTokenizer(line, "\"\' ", true);
		Vector v = new Vector();
		StringBuffer current = new StringBuffer();
		boolean lasttok = false;

		while(tok.hasMoreTokens())
		{
			String nextTok = tok.nextToken();
			switch(state)
			{
				case inquote:
					if("\'".equals(nextTok))
					{
						lasttok = true;
						state = normal;
					}
					else
					{
						current.append(nextTok);
					}
					break;
				case indoublequote:
					if("\"".equals(nextTok))
					{
						lasttok = true;
						state = normal;
					}
					else
					{
						current.append(nextTok);
					}
					break;
				default:
					if("\'".equals(nextTok))
					{
						state = inquote;
					}
					else if("\"".equals(nextTok))
					{
						state = indoublequote;
					}
					else if(" ".equals(nextTok))
					{
						if(lasttok || current.length() != 0)
						{
							v.addElement(current.toString());
							current = new StringBuffer();
						}
					}
					else
					{
						current.append(nextTok);
					}
					lasttok = false;
					break;
			}
		}
		if(lasttok || current.length() != 0)
		{
			v.addElement(current.toString());
		}
		if(state == inquote || state == indoublequote)
		{
			throw new RuntimeException("unbalanced quotes in " + line);
		}
		String[] args = new String[v.size()];
		v.copyInto(args);
		
		return args;
	}

	
	/**
	 *  Get the file from an URL.
	 *	@param url	The file URL.
	 *  @return	The file.
	 */
	public static File	getFile(URL url)
	{
		assert url.getProtocol().equals("file");
		
		File	file;
		try
		{
			file = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
//			String	filename	= URLDecoder.decode(url.toString(), "UTF-8");
//			file = new File(filename.substring(5));	// strip "file:"
		}
		catch(UnsupportedEncodingException e)
		{
			// Shouldn't happen for existing files!?
			throw new RuntimeException(e);			
		}
		return file;
	}

	/**
	 *  Copy a file.
	 *  @param source	The source file.
	 *  @param target	The target file or directory (will be deleted first).
	 */
	public static void	copyFile(File source, File target)	throws IOException
	{
		if(target.isDirectory())
		{
			target	= new File(target, source.getName());
		}
		if(target.exists())
		{
			target.delete();
		}
		InputStream	is	= new FileInputStream(source);
		OutputStream	os	= new FileOutputStream(target);
		byte[]	buf	= new byte[8192];
		int	read;
		while((read=is.read(buf))!=-1)
		{
			os.write(buf, 0, read);
		}
		os.close();
		is.close();
	}
	
	/**
	 *  Moves a file to a target location.
	 *  @param source	The source file.
	 *  @param target	The target file location (will be deleted first, if it exists).
	 */
	public static void	moveFile(File source, File target)	throws IOException
	{
		Class filesclazz = null;
		try
		{
			filesclazz = Class.forName("java.nio.file.Files");
		}
		catch (ClassNotFoundException e)
		{
		}
		
		if (filesclazz != null)
		{
			// Java 7+ mode
			try
			{
				Class pathclazz = Class.forName("java.nio.file.Path");
				Class copyoptionclazz = Class.forName("java.nio.file.CopyOption");
				Class standardcopyoptionclazz = Class.forName("java.nio.file.StandardCopyOption");
				Object atomicflag = standardcopyoptionclazz.getField("ATOMIC_MOVE").get(null);
				Object replaceflag = standardcopyoptionclazz.getField("REPLACE_EXISTING").get(null);
				Object moveoptions = Array.newInstance(copyoptionclazz, 2);
				Array.set(moveoptions, 0, replaceflag);
				Array.set(moveoptions, 1, atomicflag);
				Class copyoptionarrayclazz = moveoptions.getClass();
				Method movemethod = filesclazz.getMethod("move", pathclazz, pathclazz, copyoptionarrayclazz);
				
				Method topathmethod = File.class.getMethod("toPath", (Class<?>[]) null);
				Object srcpath = topathmethod.invoke(source, (Object[]) null);
				Object tgtpath = topathmethod.invoke(target, (Object[]) null);
				
				try
				{
					movemethod.invoke(null, srcpath, tgtpath, moveoptions);
				}
				catch (Exception e)
				{
					// Try non-atomic move.
					moveoptions = Array.newInstance(copyoptionclazz, 1);
					Array.set(moveoptions, 0, replaceflag);
					movemethod.invoke(null, srcpath, tgtpath, moveoptions);
				}
			}
			catch (Exception e)
			{
				// Still try fallback, maybe that works.
				filesclazz = null;
			}
		}
		
		if (filesclazz == null)
		{
			// Compatability fallback.
			if (!source.renameTo(target))
			{
				copyFile(source, target);
				if (!source.delete())
				{
					throw new IOException("Unable to delete source file after move: " + source.getAbsolutePath());
				}
			}
		}
	}
	
	/**
	 *  Get the exception stack trace as string. 
	 *  @param e The exception.
	 *  @return The string.
	 */
	public static String getStackTrace(Exception e)
	{
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	
	/**
	 *  Fast way to compute log2(x).
	 *  @param num The number.
	 *  @return The log2(x).
	 */
	public static int log2(int num) 
	{
	    int ret = 0;
	    if((num & 0xffff0000)!= 0) 
	    { 
	    	num >>>= 16; 
			ret = 16; 
	    }
	    if(num >= 256) 
	    { 
	    	num >>>= 8; 
	    	ret += 8; 
	    }
	    if(num >= 16) 
	    { 
	    	num >>>= 4; 
	    	ret += 4; 
	    }
	    if(num >= 4) 
	    { 
	    	num >>>= 2; 
	    	ret += 2; 
	    }
	    return ret + (num >>> 1);
	}
	
	/**
	 *  Fast way to compute log2(x).
	 *  @param num The number.
	 *  @return The log2(x).
	 */
	public static int log2(long num) 
	{
	    int ret = 0;
	    
	    if((num & 0xffffffff00000000l)!= 0) 
	    { 
	    	num >>>= 64; 
			ret = 64; 
	    }
	    if(num >= 4294967296l) 
	    { 
	    	num >>>= 32; 
			ret = 32; 
	    }
	    if(num >= 65536) 
	    { 
	    	num >>>= 16; 
			ret = 16; 
	    }
	    if(num >= 256) 
	    { 
	    	num >>>= 8; 
	    	ret += 8; 
	    }
	    if(num >= 16) 
	    { 
	    	num >>>= 4; 
	    	ret += 4; 
	    }
	    if(num >= 4) 
	    { 
	    	num >>>= 2; 
	    	ret += 2; 
	    }
	    if(num >= 2)
	    {
	    	ret++;
	    }
	    return ret;
	}

	/**
	 *  Compute a file hash.
	 *  @param filename The filename.
	 *  @return The hash.
	 */
	public static byte[] computeFileHash(String filename)
	{
		return computeFileHash(filename, null);
	}
	
	/**
	 *  Compute a file hash.
	 *  @param filename The filename.
	 *  @param algorithm The hash algorithm.
	 *  @return The hash.
	 */
	public static byte[] computeFileHash(String filename, String algorithm)
	{
		byte[] ret = null;
		
		FileInputStream fis = null;
		try
		{
			MessageDigest md = MessageDigest.getInstance(algorithm==null? "SHA-1": algorithm);
			File file = new File(filename);
			
			if(file.exists() && !file.isDirectory())
			{
				fis = new FileInputStream(file);
				int bufsize = 8192;
				byte[] data = new byte[bufsize];
				int read = 0;
				long len = file.length();
				while(read!=-1 && len>0)
				{
					int toread = (int)Math.min(len, bufsize);
					read = fis.read(data, 0, toread);
				    len -= read;
					md.update(data, 0, read);
				}
				ret = md.digest();
 			}
			else
			{
				String[] files = file.list(new FilenameFilter()
				{
					public boolean accept(File dir, String name)
					{
						return !".jadexbackup".equals(name);
					}
				});
				for(String name: files)
				{
					md.update((byte)0);	// marker between directory names to avoid {a, bc} being the same as {ab, c}. 
					md.update(name.getBytes("UTF-8"));
				}
				
				ret = md.digest();
			}
		}
		catch(RuntimeException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			try
			{
				if(fis!=null)
				{
					fis.close();
				}
			}
			catch(Exception e)
			{
			}
		}
		
		return ret;
	}
	
	/**
	 *  Convert a string to the same string with first
	 *  letter in upper case.
	 *  @param str The string.
	 *  @return The string with first letter in uppercase.
	 */
	public static String firstToUpperCase(String str)
	{
		return str.substring(0, 1).toUpperCase()+str.substring(1);
	}
	
	/**
	 * Main method for testing.
	 */
	public static void main(String[] args)
	{
		System.out.println(log2(8));
		System.out.println(log2(800000000000L));
		
		
//		System.out.println("Here: " + createUniqueId("test", 3));
//		System.out.println(htmlwraps);
//		testIntByteConversion();
	}
	
}
