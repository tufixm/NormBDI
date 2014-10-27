package jadex.commons.transformation.binaryserializer;

import jadex.commons.transformation.traverser.ITraverseProcessor;
import jadex.commons.transformation.traverser.Traverser;

import java.awt.Rectangle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;

/**
 *  Codec for encoding and decoding Rectangle objects.
 */
public class RectangleCodec extends AbstractCodec
{
	/**
	 *  Tests if the decoder can decode the class.
	 *  @param clazz The class.
	 *  @return True, if the decoder can decode this class.
	 */
	public boolean isApplicable(Class clazz)
	{
		return Rectangle.class.equals(clazz);
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
		ByteBuffer buf = context.getByteBuffer(16);
		buf.order(ByteOrder.BIG_ENDIAN);
		int x = buf.getInt();
		int y = buf.getInt();
		int w = buf.getInt();
		int h = buf.getInt();
		return new Rectangle(x, y, w, h);
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
		Rectangle r = (Rectangle)object;
		ByteBuffer buf = ec.getByteBuffer(16);
		buf.order(ByteOrder.BIG_ENDIAN);
		buf.putInt(r.x);
		buf.putInt(r.y);
		buf.putInt(r.width);
		buf.putInt(r.height);
		
		return object;
	}
}
