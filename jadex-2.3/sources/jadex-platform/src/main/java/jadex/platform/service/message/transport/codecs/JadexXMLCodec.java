package jadex.platform.service.message.transport.codecs;

import jadex.bridge.service.types.message.ICodec;
import jadex.xml.bean.JavaReader;
import jadex.xml.bean.JavaWriter;

import java.io.IOException;
import java.io.InputStream;

/**
 *  The Jadex XML codec. Codec supports parallel
 *  calls of multiple concurrent clients (no method
 *  synchronization necessary).
 *  
 *  Converts object -> byte[] and byte[] -> object.
 */
public class JadexXMLCodec implements ICodec
{
	//-------- constants --------
	
	/** The JadexXML codec id. */
	public static final byte CODEC_ID = 4;

	/** The debug flag. */
	protected boolean DEBUG = false;
	
	//-------- methods --------
	
	/**
	 *  Get the codec id.
	 *  @return The codec id.
	 */
	public byte getCodecId()
	{
		return CODEC_ID;
	}
	
	/**
	 *  Encode an object.
	 *  @param obj The object.
	 *  @throws IOException
	 */
//	public byte[] encode(Object val, ClassLoader classloader)
	public Object encode(Object val, ClassLoader classloader)
	{
		byte[] ret = JavaWriter.objectToByteArray(val, classloader);
		if(DEBUG)
			System.out.println("encode message: "+(new String(ret)));
		return ret;
	}

	/**
	 *  Decode an object.
	 *  @return The decoded object.
	 *  @throws IOException
	 */
//	public Object decode(byte[] bytes, ClassLoader classloader)
	public Object decode(Object bytes, ClassLoader classloader)
	{
		Object ret = bytes instanceof byte[]
			? JavaReader.objectFromByteArray((byte[])bytes, classloader)
			: JavaReader.objectFromInputStream((InputStream)bytes, classloader);
		if(DEBUG)
			System.out.println("decode message: "+(new String((byte[])bytes)));
		return ret;
	}
}