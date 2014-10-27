package jadex.bridge.service.types.message;

/**
 *  Encode and decode an object from a string representation.
 */
public interface ICodec
{
	/** Constant for accessing the codec id. */
	public static final String CODEC_ID = "CODEC_ID";
	
	/**
	 *  Get the codec id.
	 *  @return The codec id.
	 */
	public byte getCodecId();
	
	/**
	 *  Encode data with the codec.
	 *  @param val The value.
	 *  @return The encoded object.
	 */
//	public byte[] encode(Object val, ClassLoader classloader);
	public Object encode(Object val, ClassLoader classloader);
	
	/**
	 *  Decode data with the codec.
	 *  @param bytes The value bytes as byte array or input stream.
	 *  @return The decoded object or byte array (for intermediate codecs).
	 */
//	public Object decode(byte[] bytes, ClassLoader classloader);
	public Object decode(Object bytes, ClassLoader classloader);
	
}