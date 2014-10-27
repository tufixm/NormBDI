package jadex.commons.transformation.binaryserializer;

import jadex.commons.Base64;
import jadex.commons.SReflect;
import jadex.commons.gui.SGUI;
import jadex.commons.transformation.traverser.ITraverseProcessor;
import jadex.commons.transformation.traverser.Traverser;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.Map;


/**
 *  Codec for encoding and decoding certificate objects.
 */
public class CertificateCodec extends AbstractCodec
{
	/**
	 *  Tests if the decoder can decode the class.
	 *  @param clazz The class.
	 *  @return True, if the decoder can decode this class.
	 */
	public boolean isApplicable(Class clazz)
	{
		return SReflect.isSupertype(Certificate.class, clazz);
	}
	
	/**
	 *  Creates the object during decoding.
	 *  
	 *  @param clazz The class of the object.
	 *  @param context The decoding context.
	 *  @return The created object.
	 */
	public Object createObject(Class clazz, DecodingContext context)
	{
		try
		{
			String type = context.readString();
//			String type = "X.509";
			// This is correct because this byte array is a technical object specific to the image and
			// is not part of the object graph proper.
			byte[] data = (byte[])BinarySerializer.decodeObject(context);
			CertificateFactory cf = CertificateFactory.getInstance(type);
			return cf.generateCertificate(new ByteArrayInputStream(data));
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
	 *  Test if the processor is applicable.
	 *  @param object The object.
	 *  @param targetcl	If not null, the traverser should make sure that the result object is compatible with the class loader,
	 *    e.g. by cloning the object using the class loaded from the target class loader.
	 *  @return True, if is applicable. 
	 */
	public boolean isApplicable(Object object, Class<?> clazz, boolean clone, ClassLoader targetcl)
	{
		return isApplicable(clazz);
	}
	
	/**
	 *  Encode the object.
	 */
	public Object encode(Object object, Class<?> clazz, List<ITraverseProcessor> processors, 
			Traverser traverser, Map<Object, Object> traversed, boolean clone, EncodingContext ec)
	{
		try
		{
			ec.writeString(((Certificate)object).getType());
			byte[] encimg = ((Certificate)object).getEncoded();
			traverser.traverse(encimg, encimg.getClass(), traversed, processors, clone, null, ec);
			return object;
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
}
