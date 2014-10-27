package jadex.commons.transformation;

import jadex.commons.SReflect;
import jadex.commons.Tuple;
import jadex.commons.Tuple2;
import jadex.commons.collection.MultiCollection;
import jadex.commons.transformation.annotations.Classname;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.security.auth.x500.X500Principal;

import junit.framework.TestCase;

import org.bouncycastle.x509.X509V1CertificateGenerator;

/**
 *  Testcases for writer and reader.
 */
public abstract class Test extends TestCase
{
	/**
	 * 
	 */
	public void performTests()
	{
		performTests(1);
	}
	
	/**
	 * 
	 */
	public void performTests(int cnt)
	{
//		try
//		{
//			InputStream is = SUtil.getResource("jadex/xml/test.xml", null);
//			Object object = JavaReader.getInstance().read(is, null, null);
//			System.out.println("read: "+object);
//			String xml = JavaWriter.objectToXML(object, null);
//			System.out.println("wrote: "+xml);
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
		
		try
		{
//			int cnt = 1000;
			long start = System.currentTimeMillis();
			for(int i=0; i<cnt; i++)
//			while(true)
			{
				testException();

//				testCertificate(); 
//
//				testTimestamp();
//
//				testEnum();
//
//				testByte();
//				testDouble();
////				testBigData();
//				testSpecialCharacter();
//					
//				testByteArray();
//				testBByteArray();
//				testIntArray();
//				testIntegerArray();
//				testDoubleArray();
//				testBDoubleArray();
//				testFloatArray();
//				testBFloatArray();
//				testLongArray();
//				testBLongArray();
//				testCharArray();
//				testCharacterArray();
//				testShortArray();
//				testBShortArray();
//				testBooleanArray();
//				testBBooleanArray();
//				testVectorModel();
//				testMultiCollection();
//				testEmptySet();
//				testEmptyList();
//				testEmptyMap();
//				testArray();
//				testList();
//				testSet();
//				testMap();
//				testEmptyArray();
//				testArrayOrder();
//				testMultiArray();
//				testMultiArray2();
//				testMultiArrayAttribute();
//				testByteArrayAttribute();
//				
//				testClass();
//				testDate();
//				testUUID();
//				testInnerClass();
//				testURL();
//				testLoggingLevel();
//				testLogRecord();
//				testInetAddress();
//				testTuple();
//				testTuple2();
//				testTimestamp();
//			
//				testBean();
//				testBeanWithPublicFields();
//				testBeanWithIncludedFields();
//				testAnonymousInnerClass();
//				testAnonymousInnerClassWithSimpleTypes();
//				testSelfReferenceBean();
//			
//				testColor();
//				testImage();
//				testRectangle();
			}
			long dur = System.currentTimeMillis()-start;
			
			System.out.println("Needed: "+dur+" for cnt="+cnt);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//-------- test methods --------
	
	/**
	 *  Method for writing and reading an object.
	 */
	protected void doWriteAndRead(Object wo) throws Exception
	{
		doWriteAndRead(wo, null);
	}
	
	/**
	 *  Method for writing and reading an object.
	 */
	protected void doWriteAndRead(Object wo, Comparator comp) throws Exception
	{
		//(new RuntimeException()).printStackTrace();
		Object written = doWrite(wo);
		
//		System.out.println("written is:"+new String((byte[])written));
		
		Object ro = doRead(written);
		
//		String xml = JavaWriter.objectToXML(wo, null);
		
//		System.out.println("xml is:"+xml);
		
//		Object ro = JavaReader.objectFromXML(xml, null);
		
//		System.out.println("Write: "+wo);
//		FileOutputStream fos = new FileOutputStream("test.xml");
//		writer.write(wo, fos, null, null);
//		fos.close();
//		
//		FileInputStream fis = new FileInputStream("test.xml");
//		Object ro = reader.read(fis, null, null);
//		fis.close();
//		System.out.println("Read: "+ro+" / class="+ro.getClass());
		
//		System.out.println("equals: "+wo.equals(ro));
		compare(wo, ro, written, comp);
		
//		assertEquals("Written and read objects should be equal:", wo, ro);
	}
	
	/**
	 * 
	 */
	public abstract Object doWrite(Object wo);
	
	/**
	 * 
	 */
	public abstract Object doRead(Object ro);
	
	/**
	 * 
	 */
	public void compare(Object wo, Object ro, Object written, Comparator comp)
	{
		if(comp!=null)
		{
			if(comp.compare(wo, ro)!=0)
			{
				throw new RuntimeException("Not equal: "+wo+", "+ro+"\n"
					+wo.getClass()+" \n"+ro.getClass()+" \n"+written);
			}
		}
		else
		{
			if(!wo.equals(ro) && !(wo.getClass().isArray() && Arrays.deepEquals((Object[])wo, (Object[])ro)))
			{
				throw new RuntimeException("Not equal: "+wo+", "+ro+"\n"
					+wo.getClass()+" \n"+ro.getClass()+" \n"+written);
			}
		}
	}
	
	/**
	 * 
	 */
	public void testUUID() throws Exception
	{
//		System.out.println("test enum: "+(TestEnum.A instanceof Enum));
		doWriteAndRead(UUID.randomUUID());
	}
	
	/**
	 * 
	 */
	public void testEnum() throws Exception
	{
//		System.out.println("test enum: "+(TestEnum.A instanceof Enum));
		doWriteAndRead(TestEnum.A);
	}
	
	/**
	 * 
	 */
	public void testByte() throws Exception
	{
		byte data = 55;
		
		doWriteAndRead(data);
	}
	
	/**
	 * 
	 */
	public void testDouble() throws Exception
	{
		double data = 1E6*1.00001;
		
		doWriteAndRead(data);
	}
	
	/**
	 * 
	 */
	public void testByteArray() throws Exception
	{
		byte[] data = new String("hello world").getBytes();
		
		doWriteAndRead(data, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				return Arrays.equals((byte[])o1, (byte[])o2)? 0: -1;
			}
		});
	}
	
	/**
	 * 
	 */
	public void testBByteArray() throws Exception
	{
		byte[] tmp = new String("hello world").getBytes();
		Byte[] data = new Byte[tmp.length];
		for(int i=0; i<tmp.length; i++)
			data[i] = new Byte(tmp[i]);
		
		doWriteAndRead(data);
	}
	
	/**
	 * 
	 */
	public void testIntArray() throws Exception
	{
		int[] data = new int[]{1,2,3,4,5,6};
		
		doWriteAndRead(data, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				return Arrays.equals((int[])o1, (int[])o2)? 0: -1;
			}
		});
	}
	
	/**
	 * 
	 */
	public void testIntegerArray() throws Exception
	{
		Integer[] data = new Integer[]{new Integer(1), new Integer(2), new Integer(3)};
		
		doWriteAndRead(data); 
	}
	
	/**
	 * 
	 */
	public void testDoubleArray() throws Exception
	{
		double[] data = new double[]{1E6*1.00001,1.0,3.0,4.00001,5.00002,6.99999};
		
		doWriteAndRead(data, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				return Arrays.equals((double[])o1, (double[])o2)? 0: -1;
			}
		});
	}
	
	/**
	 * 
	 */
	public void testBDoubleArray() throws Exception
	{
		Double[] data = new Double[]{new Double(1), new Double(2), new Double(3)};
		
		doWriteAndRead(data); 
	}
	
	/**
	 * 
	 */
	public void testFloatArray() throws Exception
	{
		float[] data = new float[]{1.01f,1.0f,3.0f,4.00001f,5.00002f,6.99999f};
		
		doWriteAndRead(data, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				return Arrays.equals((float[])o1, (float[])o2)? 0: -1;
			}
		});
	}
	
	/**
	 * 
	 */
	public void testBFloatArray() throws Exception
	{
		Float[] data = new Float[]{new Float(1), new Float(2), new Float(3)};
		
		doWriteAndRead(data); 
	}
	
	/**
	 * 
	 */
	public void testLongArray() throws Exception
	{
		long[] data = new long[]{1000000000,1,3,4,5,699999};
		
		doWriteAndRead(data, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				return Arrays.equals((long[])o1, (long[])o2)? 0: -1;
			}
		});
	}
	
	/**
	 * 
	 */
	public void testBLongArray() throws Exception
	{
		Long[] data = new Long[]{new Long(1), new Long(2), new Long(3)};
		
		doWriteAndRead(data); 
	}
	
	/**
	 * 
	 */
	public void testCharArray() throws Exception
	{
		char[] data = new char[]{'a','b','c'};
		
		doWriteAndRead(data, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				return Arrays.equals((char[])o1, (char[])o2)? 0: -1;
			}
		});
	}
	
	/**
	 * 
	 */
	public void testCharacterArray() throws Exception
	{
		Character[] data = new Character[]{new Character('a'), new Character('b'), new Character('c')};
		
		doWriteAndRead(data); 
	}
	
	/**
	 * 
	 */
	public void testShortArray() throws Exception
	{
		short[] data = new short[]{1,2,3,4,5,6};
		
		doWriteAndRead(data, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				return Arrays.equals((short[])o1, (short[])o2)? 0: -1;
			}
		});
	}
	
	/**
	 * 
	 */
	public void testBShortArray() throws Exception
	{
		Short[] data = new Short[]{new Short((short)1), new Short((short)2), new Short((short)3)};
		
		doWriteAndRead(data); 
	}
	
	/**
	 * 
	 */
	public void testBooleanArray() throws Exception
	{
		boolean[] data = new boolean[]{true,false,false,true,true,false};
		
		doWriteAndRead(data, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				return Arrays.equals((boolean[])o1, (boolean[])o2)? 0: -1;
			}
		});
	}
	
	/**
	 * 
	 */
	public void testBBooleanArray() throws Exception
	{
		Boolean[] data = new Boolean[]{Boolean.TRUE, Boolean.FALSE, Boolean.TRUE};
		
		doWriteAndRead(data); 
	}
	
//	/**
//	 * 
//	 */
//	public void testBigData() throws Exception
//	{
////		File f = new File("C:\\zips\\cd-ripper\\easy-cd-ripper.exe");
//		File f = new File("C:\\zips\\aida32ee_393.zip");
//		FileInputStream fis = new FileInputStream(f);
//		byte[] data = new byte[(int)f.length()];
//		fis.read(data);
//		String bd = new String(Base64.encode(data));
//		
//		doWriteAndRead(bd);
//	}
	
	/**
	 * 
	 */
	public void testMultiCollection() throws Exception
	{
		MultiCollection col = new MultiCollection();
		col.put("a", "a");
		col.put("a", "b");
		col.put("a", "c");
		col.put("b", "b");

		doWriteAndRead(col);
	}
	
	/**
	 * 
	 */
	public void testImage() throws Exception
	{
		BufferedImage bi = new BufferedImage(70,70,BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		g.setColor(Color.BLUE);
		g.draw3DRect(10, 10, 50, 50, true);

//		JFrame test = new JFrame();
//		test.setLayout(new FlowLayout());
//		test.add(new JLabel(new ImageIcon(bi)));
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		ImageIO.write(bi,"PNG", baos);
//		byte[] buf = baos.toByteArray();
//		Image img = ImageIO.read(new ByteArrayInputStream(buf));
//		test.add(new JLabel(new ImageIcon(img)));
//		test.pack();
//		test.setVisible(true);
//		System.out.println("buf: "+SUtil.arrayToString(buf));

		doWriteAndRead(bi, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				BufferedImage	b1	= (BufferedImage)o1;
				BufferedImage	b2	= (BufferedImage)o2;
				boolean	equal	= b1.getWidth()==b2.getWidth() && b1.getHeight()==b2.getHeight();
				for(int x=0; equal && x<b1.getWidth(); x++)
				{
					for(int y=0; equal && y<b1.getHeight(); y++)
					{
						equal	= b1.getRGB(x, y)==b2.getRGB(x, y);
					}					
				}
				return equal ? 0 : 1;
			}
		});
	}
	
	/**
	 *  Test if awt rectangle works.
	 */
	public void testRectangle() throws Exception
	{
		doWriteAndRead(new Rectangle(new Dimension(20, 30)));
	}
	
	/**
	 *  Test if empty set constant works.
	 */
	public void testEmptySet() throws Exception
	{
		doWriteAndRead(Collections.EMPTY_SET);
	}
	
	/**
	 *  Test if empty list constant works.
	 */
	public void testEmptyList() throws Exception
	{
		doWriteAndRead(Collections.EMPTY_LIST);
	}
	
	/**
	 *  Test if empty map constant works.
	 */
	public void testEmptyMap() throws Exception
	{
		doWriteAndRead(Collections.EMPTY_MAP);
	}
	
	/**
	 *  Test if multi array transfer works.
	 */
	public void testEmptyArray() throws Exception
	{
		int[] data = new int[0];
		A a = new A();
		a.setInts(data);
		
		doWriteAndRead(a);
	}
	
	/**
	 * Test if the order of an array is preserved when (de-)serializing.
	 */
	public void testArrayOrder() throws Exception
	{
		Object[][] data = new Object[1][8];

		data[0][0] = new Long(1);
		data[0][1] = "A";
		data[0][2] = "";
		data[0][3] = "";
		data[0][4] = "B";
		data[0][5] = null;
		data[0][6] = "";
		data[0][7] = new Long(2);
        
		doWriteAndRead(data);
	}

	
	/**
	 *  Test if multi array transfer works.
	 */
	public void testMultiArray() throws Exception
	{
//		String[][] array = new String[3][2]; 
//		array[0][0] = "a";
//		array[1][0] = "b";
//		array[2][0] = "c";
		
		Object[][] data = new Object[1][8];

		data[0][0] = new Long(1);
		data[0][1] = "Hallo";
		data[0][2] = "";
		data[0][3] = "";
		data[0][4] = "Moin";
		data[0][5] = null;
		data[0][6] = "";
		data[0][7] = new Long(2);
		
		doWriteAndRead(data);
	}
	
	/**
	 *  Test if multi array transfer works.
	 */
	public void testMultiArray2() throws Exception
	{
//		String[][] array = new String[3][2]; 
//		array[0][0] = "a";
//		array[1][0] = "b";
//		array[2][0] = "c";
		
		Object[][] data = new Object[2][];

		data[0] = new Object[]{1,2};
		data[1] = new Object[]{3,4,5};
		
		doWriteAndRead(data);
	}
	
	/**
	 *  Test if multi array attribute transfer works.
	 */
	public void testMultiArrayAttribute() throws Exception
	{
//		int[][]	data	= new int[][]
//		{
//			{1, 2, 3},
//			{4, 5, 6}
//		};
		ArrayHolder	ad	= new ArrayHolder();
		ad.setData(new int[]{1,2,3});
		
		doWriteAndRead(ad);
	}
	
	/**
	 *  Test if multi array attribute transfer works.
	 */
	public void testByteArrayAttribute() throws Exception
	{
		byte[]	data	= new byte[256];
		for(int b=0; b<256; b++)
		{
			data[b]	= (byte)b;
		}
		ByteArrayHolder	bah	= new ByteArrayHolder();
		bah.setData(data);
		
		doWriteAndRead(bah);
	}
	
	/**
	 *  Test if vector model transfer works.
	 */
	public void testVectorModel() throws Exception
	{
		VectorModel vm = new VectorModel();
		vm.addToV1("a");
		vm.addToV2("b");
		doWriteAndRead(vm);
	}
	
	/**
	 *  Test if class transfer works.
	 */
	public void testClass() throws Exception
	{
		doWriteAndRead(boolean.class);
		doWriteAndRead(InnerTestClass.class);
	}
	
	public static class InnerTestClass{}
	
	/**
	 *  Test if date transfer works.
	 */
	public void testDate() throws Exception
	{
		doWriteAndRead(new java.util.Date());
	}
	
	/**
	 *  Test if color transfer works.
	 */
	public void testColor() throws Exception
	{
		doWriteAndRead(new java.awt.Color(200, 100, 50));
	}
	
	/**
	 *  Test if array transfer works.
	 */
	public void testArray() throws Exception
	{
		Object[] array = new Object[6]; 
		array[0] = new Integer(0);
//		array[1] = new Integer(1);
		array[2] = new Integer(2);
		array[3] = new Integer(3);
		array[4] = new Integer(4);
		array[5] = new Integer(5);
		
		doWriteAndRead(array);
	}
	
	/**
	 *  Test if bean transfer works.
	 */
	public void testBean() throws Exception
	{
		doWriteAndRead(getABean());
	}
	
	/**
	 *  Test if references work.
	 */
	public void testSelfReferenceBean() throws Exception
	{
		E e = new E();
		e.setSelfReference(e);
		doWriteAndRead(e, new Comparator<E>()
		{
			public int compare(E o1, E o2)
			{
				return o2 != o2.getSelfReference()? -1 : 0;
			}
		});
	}
	
	/**
	 *  Test list transfer works.
	 */
	public void testList() throws Exception
	{
		List list = new ArrayList();
		list.add("str_a");
		list.add(new Integer(2));
		list.add(getABean());
		
		doWriteAndRead(list);
	}
	
	/**
	 *  Test if set transfer works.
	 */
	public void testSet() throws Exception
	{
		Set set = new HashSet();
		set.add("str_a");
		set.add(new Integer(2));
		set.add(getABean());
		
		doWriteAndRead(getABean());
	}
	
	/**
	 *  Test if map transfer works.
	 */
	public void testMap() throws Exception
	{
		Map map = new LinkedHashMap();
		map.put("$", "A");
		map.put(new Integer(2), new Integer(22));
		map.put("obja", getABean());
//		for(Iterator it=map.entrySet().iterator(); it.hasNext(); )
//		{
//			Map.Entry e = (Map.Entry)it.next();
//			System.out.println("key="+e.getKey()+" value="+e.getValue());
//		}
		
		doWriteAndRead(map);
	}
	
	/**
	 *  Test if inner class transfer works.
	 */
	public void testInnerClass() throws Exception
	{
		StaticInnerClass ic = new StaticInnerClass("some string");
		
		doWriteAndRead(ic);
	}
	
	/**
	 *  Test if URL transfer works.
	 */
	public void testURL() throws Exception
	{
//		URL url = new URL("http", "host", 4711, "file");
		URL url = new URL("file:/C:/projects/jadex/jadex-applications-micro/target/classes/");
		
		doWriteAndRead(url);
	}
	
	/**
	 *  Test if java.util.logging.Level transfer works.
	 */
	public void testLoggingLevel() throws Exception
	{
		Level level = Level.SEVERE;
		
		doWriteAndRead(level);
	}
	
	/**
	 *  Test if java.util.logging.LogRecord transfer works.
	 */
	public void testLogRecord() throws Exception
	{
		LogRecord lr = new LogRecord(Level.WARNING, "test message");
		
		doWriteAndRead(lr, new Comparator<LogRecord>()
		{
			public int compare(LogRecord o1, LogRecord o2)
			{
				return o1.getMessage().equals(o2.getMessage()) && o1.getLevel().equals(o2.getLevel())? 0: 1;
			}
		});
	}
	
	/**
	 *  Test if java.util.logging.Level transfer works.
	 */
	public void testInetAddress() throws Exception
	{
		InetAddress adr = InetAddress.getByName("127.0.0.1");
		
		doWriteAndRead(adr);
	}
	
	/**
	 *  Test if writer writes public bean fields (when XML_INCLUDE_FIELDS is set).
	 */
	public void testBeanWithPublicFields() throws Exception
	{
		C c = new C("test\n", 23);
		
		doWriteAndRead(c);
	}
	
	/**
	 *  Test if writer writes public bean fields (when XMLIncludeFields annotation is present).
	 */
	public void testBeanWithIncludedFields() throws Exception
	{
		D d = new D("test\n", 23);
		
		doWriteAndRead(d);
	}
	
	/**
	 *  Test if special characters can be transferred.
	 */
	public void testSpecialCharacter() throws Exception
	{
		String str = "\u00DC\n";
		
		doWriteAndRead(str);
	}
	
	/**
	 *  Test if anonymous inner classes can be transferred.
	 */
	public void testAnonymousInnerClass() throws Exception
	{
		// Do not use final directly as compiler optimizes field away.
		String	tmp	= "hugo";
		final String	name	= tmp;
		Object	obj	= new Object()
		{
			@Classname("test")
			public boolean equals(Object obj)
			{
				String	othername	= null;
				try
				{
					Field	field	= SReflect.getField(obj.getClass(), "val$name");
					field.setAccessible(true);
					othername	= (String)field.get(obj);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				return name.equals(othername);
			}
						
			public String toString()
			{
				return getClass().getName()+"("+name+")";
			}
		};
		
		doWriteAndRead(obj);
	}
	
	/**
	 *  Test if anonymous inner classes can be transferred.
	 */
	public void testAnonymousInnerClassWithSimpleTypes() throws Exception
	{
		// Do not use final directly as compiler optimizes field away.
		String	tmp	= "hugo";
		final String	name	= tmp;
		Boolean tmp2 = true;
		final boolean booli = tmp2;
		Object	obj	= new Object()
		{
			@Classname("test2")
			public boolean equals(Object obj)
			{
				String	othername	= null;
				Boolean otherbooli = null;
				try
				{
					Field	field	= SReflect.getField(obj.getClass(), "val$name");
					field.setAccessible(true);
					othername	= (String)field.get(obj);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				try
				{
					Field	field	= SReflect.getField(obj.getClass(), "val$booli");
					field.setAccessible(true);
					otherbooli	= (Boolean)field.get(obj);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				return name.equals(othername) && otherbooli!=null && otherbooli.booleanValue()==booli;
			}
						
			public String toString()
			{
				return getClass().getName()+"("+name+", "+booli+")";
			}
		};
		
		doWriteAndRead(obj);
	}
	
	/**
	 *  Test reading / writing tuple2.
	 */
	public void	testTimestamp() throws Exception
	{
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		doWriteAndRead(ts);
	}
	
	/**
	 *  Test reading / writing exception.
	 */
	public void	testException() throws Exception
	{
		RuntimeException e = new RuntimeException("Some runtime reason.");
		SecurityException ex = new SecurityException("Some security concern.", e);
		doWriteAndRead(ex, new Comparator<Exception>()
		{
			public int compare(Exception e1, Exception e2)
			{
				int ret = -1;
				if(e1.getClass().equals(e2.getClass()))
				{
					ret = Arrays.equals(e1.getStackTrace(), e2.getStackTrace())? 0: -1;
				}
				return ret;
			}
		});
	}
	
	/**
	 *  Test reading / writing tuple2.
	 */
	public void	testTuple() throws Exception
	{
		Tuple	obj	= new Tuple("hello", "world");
		doWriteAndRead(obj);
		
		obj	= new Tuple(new String[]{"hello", "world", "!"});
		doWriteAndRead(obj);

	}
	
	/**
	 *  Test reading / writing tuple2.
	 */
	public void	testTuple2() throws Exception
	{
		Tuple2<String, String>	obj	= new Tuple2<String, String>("hello", "world");
		doWriteAndRead(obj);
	}
	
	/**
	 *  Test reading / writing tuple2.
	 */
	public void	testCertificate() throws Exception
	{
		KeyStore ks = getKeystore("./testkeystore", "pass", "pass", "test");
		Certificate cert = ks.getCertificate("test");
		doWriteAndRead(cert, new Comparator<Certificate>()
		{
			public int compare(Certificate o1, Certificate o2)
			{
				boolean ret = false;
				try
				{
					ret = Arrays.equals(o1.getEncoded(), o2.getEncoded());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				return ret? 0: -1;
			}
		});
	}
	
	/**
	 *  Get some bean.
	 */
	protected Object getABean()
	{
		B b1 = new B("test b1");
		B b2 = new B("test b2");
		B b3 = new B("test b3");
		B b4 = new B("test b4");
		A a = new A(10, "test a", b1, new B[]{b1, b2, b3, b4});
		return a;
	}
	
	public static class StaticInnerClass
	{
		protected String string;
		
		public StaticInnerClass()
		{
		}
		
		public StaticInnerClass(String string)
		{
			this.string = string;
		}

		public String getString() 
		{
			return string;
		}

		public void setString(String string) 
		{
			this.string = string;
		}

		public int hashCode() 
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((string == null) ? 0 : string.hashCode());
			return result;
		}

		public boolean equals(Object obj) 
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StaticInnerClass other = (StaticInnerClass) obj;
			if (string == null) {
				if (other.string != null)
					return false;
			} else if (!string.equals(other.string))
				return false;
			return true;
		}
	}

	// copied from SSecurity to avoid dependencies :-( 
	
	/**
	 *  Get keystore from a given file.
	 */
	public static KeyStore getKeystore(String storepath, String storepass, String keypass, String alias)
	{
		try
		{
			KeyStore ks = KeyStore.getInstance("JKS");
			FileInputStream fis = null;
			boolean loaded = false;
			try
			{
				File f = new File(storepath);
				if(f.exists())
				{
					fis = new FileInputStream(storepath);
					ks.load(fis, storepass.toCharArray());
					loaded = true;
				}
			}
			catch(Exception e)
			{
			}
			finally
			{
				if(fis!=null)
					fis.close();
				if(!loaded || !ks.containsAlias(alias))
					initKeystore(ks, storepath, storepass, keypass, alias);
			}
			return ks;
		}
		catch(Exception e)
		{
//			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 *  Get keystore from a given file.
	 */
	public static void saveKeystore(KeyStore keystore, String storepath, String storepass)
	{
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(storepath);
			keystore.store(fos, storepass.toCharArray());
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			if(fos!=null)
			{
				try
				{
					fos.close();
				}
				catch(Exception e)
				{
				}
			}
		}
	}
	
	/**
	 *  Init keystore with a self-signed certificate.
	 */
	public static void initKeystore(KeyStore ks, String storepath, String storepass, String keypass, String alias)
	{
		try
    	{
	    	ks.load(null, null); // Must be called. 
	    	
//	    	RSAKeyPairGenerator r = new RSAKeyPairGenerator();
//	    	r.init(new KeyGenerationParameters(new SecureRandom(), 1024));
//	    	AsymmetricCipherKeyPair keys = r.generateKeyPair();
	    	
	    	KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");  
	 	    gen.initialize(1024);  
	 	    
//	 	    System.out.println("Generating key pair, this may take a short while.");
	 	    KeyPair keys = gen.generateKeyPair();
//	 	    System.out.println("Key generation finished.");
	 	    
		    Certificate c = generateCertificate("CN=CKS Self Signed Cert", keys, 1000, "MD5WithRSA");
		    
		    ks.setKeyEntry(alias, keys.getPrivate(), keypass.toCharArray(),  
		    	new java.security.cert.Certificate[]{c});  
		    
		    saveKeystore(ks, storepath, storepass);
    	}
		catch(RuntimeException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/** 
	 * Create a self-signed X.509 Certificate
	 * @param dn the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
	 * @param pair the KeyPair
	 * @param days how many days from now the Certificate is valid for
	 * @param algorithm the signing algorithm, eg "SHA1withRSA"
	 */ 
	public static Certificate generateCertificate(String dn, KeyPair pair, int days, String algorithm) throws GeneralSecurityException, IOException
	{
		X509V1CertificateGenerator gen = new X509V1CertificateGenerator();
		X500Principal dnn = new X500Principal(dn); //"CN=Test CA Certificate"

		Date from = new Date();
		Date to = new Date(from.getTime() + days * 86400000l);
		BigInteger sn = new BigInteger(64, new SecureRandom());
		
		gen.setSerialNumber(sn);
		gen.setIssuerDN(dnn);
		gen.setNotBefore(from);
		gen.setNotAfter(to);
		gen.setSubjectDN(dnn);                       // note: same as issuer
		gen.setPublicKey(pair.getPublic());
		gen.setSignatureAlgorithm(algorithm);

		Certificate cert = gen.generate(pair.getPrivate());
		
		return cert;
	}   
	
}
