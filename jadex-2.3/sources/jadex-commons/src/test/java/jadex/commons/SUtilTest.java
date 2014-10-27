package jadex.commons;

import junit.framework.TestCase;

/**
 *  Some SUtil methods
 */
public class SUtilTest extends TestCase
{
	public void testLongConversion()
	{
		long[]	totest	= new long[]
		{
			0, 1, -1, 255, 256,
			Long.MAX_VALUE, Long.MIN_VALUE,
			Byte.MIN_VALUE, Byte.MIN_VALUE-1,
			Byte.MAX_VALUE, Byte.MAX_VALUE+1,
			Short.MIN_VALUE, Short.MIN_VALUE-1,
			Short.MAX_VALUE, Short.MAX_VALUE+1,
			Integer.MIN_VALUE, Integer.MIN_VALUE-1L,
			Integer.MAX_VALUE, Integer.MAX_VALUE+1L
		};
		
		for(int i=0; i<totest.length; i++)
		{
			byte[]	ba	= SUtil.longToBytes(totest[i]);
			long	val	= SUtil.bytesToLong(ba);
			assertEquals("Array "+i+": "+SUtil.arrayToString(ba), totest[i], val);
		}
	}
}
