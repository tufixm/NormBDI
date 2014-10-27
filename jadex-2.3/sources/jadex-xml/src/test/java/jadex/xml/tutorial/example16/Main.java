package jadex.xml.tutorial.example16;

import jadex.commons.SUtil;
import jadex.xml.AccessInfo;
import jadex.xml.AttributeInfo;
import jadex.xml.MappingInfo;
import jadex.xml.ObjectInfo;
import jadex.xml.SubobjectInfo;
import jadex.xml.TypeInfo;
import jadex.xml.TypeInfoPathManager;
import jadex.xml.XMLInfo;
import jadex.xml.bean.BeanObjectReaderHandler;
import jadex.xml.bean.BeanObjectWriterHandler;
import jadex.xml.reader.Reader;
import jadex.xml.writer.Writer;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 *  Main class to execute tutorial lesson.
 */
public class Main
{
	/**
	 *  Main method for using the xml reader/writer.
	 */
	public static void main(String[] args) throws Exception
	{
		// This example shows the usage of id and idref attributes.
		
		// During the read process id attributes will be used to store
		// the object under the corresponding attribute value. When idref
		// attributes are used the attribute will be used to fetch the 
		// already read object and strore it as attribute value.
		
		// During the write process the writer replaces idref attribute values
		// with the corresponding id value of the object that should be originally
		// written as attribute value.
		
		// Create minimal type infos for types that need to be mapped
		
		Set typeinfos = new HashSet();
		typeinfos.add(new TypeInfo(new XMLInfo("products"), new ObjectInfo(ProductList.class))); 
		typeinfos.add(new TypeInfo(new XMLInfo("product"), new ObjectInfo(Product.class),
			new MappingInfo(null, new AttributeInfo[]{
			new AttributeInfo(new AccessInfo("name"), null, AttributeInfo.ID)
		})));
		typeinfos.add(new TypeInfo(new XMLInfo("part"), new ObjectInfo(Part.class),
			new MappingInfo(null, new AttributeInfo[]{
			new AttributeInfo(new AccessInfo("product"), null, AttributeInfo.IDREF)
		})));
		
		// Create an xml reader with standard bean object reader and the
		// custom typeinfos
		Reader xmlreader = new Reader(false, false, false, null);
		InputStream is = SUtil.getResource("jadex/xml/tutorial/example16/data.xml", null);
		Object object = xmlreader.read(new TypeInfoPathManager(typeinfos), new BeanObjectReaderHandler(), is, null, null);
		is.close();
		
		// For writing some more information is necessary
		// - use a container tag for parts/part
		// - write price as tag, not as attribute
		
		typeinfos = new HashSet();
		typeinfos.add(new TypeInfo(new XMLInfo("products"), new ObjectInfo(ProductList.class),
			new MappingInfo(null, new SubobjectInfo[]{
			new SubobjectInfo(new AccessInfo("product", "products"), null, true)
			}))); 
		typeinfos.add(new TypeInfo(new XMLInfo("product"), new ObjectInfo(Product.class),
			new MappingInfo(null, new AttributeInfo[]{
			new AttributeInfo(new AccessInfo("name"), null, AttributeInfo.ID)},
			new SubobjectInfo[]{
			new SubobjectInfo(new AccessInfo("price", "price")),
			new SubobjectInfo(new XMLInfo("parts/part"), new AccessInfo("part", "parts"), null, true)
			})));
		typeinfos.add(new TypeInfo(new XMLInfo("part"), new ObjectInfo(Part.class),
			new MappingInfo(null, new AttributeInfo[]{
			new AttributeInfo(new AccessInfo("product"), null, AttributeInfo.IDREF)
		})));
		
		// Write the xml to the output file.
		Writer xmlwriter = new Writer(false);
		String xml = Writer.objectToXML(xmlwriter, object, null, new BeanObjectWriterHandler(typeinfos, false, true));
//		OutputStream os = new FileOutputStream("out.xml");
//		xmlwriter.write(object, os, null, null);
//		os.close();
		
		// And print out the result.
		System.out.println("Read object: "+object);
		System.out.println("Wrote xml: "+xml);
	}
}
