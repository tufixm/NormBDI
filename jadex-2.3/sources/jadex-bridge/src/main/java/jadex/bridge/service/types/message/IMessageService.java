package jadex.bridge.service.types.message;

import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IInputConnection;
import jadex.bridge.IOutputConnection;
import jadex.bridge.IResourceIdentifier;
import jadex.bridge.service.IService;
import jadex.bridge.service.annotation.Excluded;
import jadex.commons.IFilter;
import jadex.commons.future.IFuture;

import java.util.Map;

/**
 *  The interface for the message service. It is responsible for
 *  managing the transports and sending/delivering messages.
 */
public interface IMessageService extends IService
{
	/**
	 *  Send a message.
	 *  @param message The message as key value pairs.
	 *  @param msgtype The message type.
	 *  @param sender The sender component identifier.
	 *  @param rid The resource identifier used by the sending component (i.e. corresponding to classes of objects in the message map).
	 *  @param realrec The real receiver if different from the message receiver (e.g. message to rms encapsulating service call to other component).
	 *  @param codecids The codecs to use for encoding (if different from default).
	 *  @param nonfunc The non functional properties that need to be preserved.
	 *  @return Future that indicates an exception when messages could not be delivered to components. 
	 */
	public IFuture<Void> sendMessage(Map<String, Object> message, MessageType msgtype, 
		IComponentIdentifier sender, IResourceIdentifier rid, IComponentIdentifier realrec, 
		byte[] codecids);//, Map<String, Object> nonfunc);
	
//	/**
//	 *  Deliver a message to some components.
//	 */
//	public void deliverMessage(Map<String, Object> msg, String type, IComponentIdentifier[] receivers);

	/**
	 *  Deliver a raw message to some components.
	 *  @param msg	Byte array containing a message in the form {prolog}{data}.
	 */
	public void deliverMessage(Object msg);
	
	/**
	 *  Create a virtual output connection.
	 */
	public IFuture<IOutputConnection> createOutputConnection(IComponentIdentifier sender, IComponentIdentifier receiver, Map<String, Object> nonfunc);

	/**
	 *  Create a virtual input connection.
	 */
	public IFuture<IInputConnection> createInputConnection(IComponentIdentifier sender, IComponentIdentifier receiver, Map<String, Object> nonfunc);
			
	/**
	 *  Add a message listener.
	 *  @param listener The change listener.
	 *  @param filter An optional filter to only receive notifications for matching messages. 
	 */
	public IFuture<Void> addMessageListener(IMessageListener listener, IFilter filter);
	
	/**
	 *  Remove a message listener.
	 *  @param listener The change listener.
	 */
	public IFuture<Void> removeMessageListener(IMessageListener listener);
	
	/**
	 *  Add content codec type.
	 *  @param codec The codec type.
	 */
	public IFuture<Void> addContentCodec(IContentCodec codec);
	
	/**
	 *  Remove content codec type.
	 *  @param codec The codec type.
	 */
	public IFuture<Void> removeContentCodec(IContentCodec codec);
	
	/**
	 *  Add message codec type.
	 *  @param codec The codec type.
	 */
	public IFuture<Void> addMessageCodec(Class codec);
	
	/**
	 *  Remove message codec type.
	 *  @param codec The codec type.
	 */
	public IFuture<Void> removeMessageCodec(Class codec);
	
	/**
	 *  Announce that addresses of transports might have changed.
	 */
	public IFuture<Void>	refreshAddresses();
	
	/**
	 *  Update component identifier with current addresses.
	 *  @param cid The component identifier.
	 *  @return The component identifier.
	 */
	public IFuture<IComponentIdentifier> updateComponentIdentifier(IComponentIdentifier cid);
	
	/**
	 *  Get addresses of all transports.
	 *  @return The addresses of all transports.
	 */
	public IFuture<String[]> getAddresses();
	
	/**
	 *  Get addresses of all transports.
	 *  @return The address schemes of all transports.
	 */
	public String[] getAddressSchemes();

	// todo: remove these method or futurize the first
	/**
	 *  Get the message type.
	 *  @param type The type name.
	 *  @return The message type.
	 */
	@Excluded
	public MessageType getMessageType(String type);
	
	/**
	 *  Get the codecs with message codecs.
	 *  @return The codec factory.
	 */
	public IFuture<Map<Byte, ICodec>> getAllCodecs();
	
	/**
	 *  Get the default codecs.
	 *  @return The default codecs.
	 */
	public IFuture<ICodec[]> getDefaultCodecs();
}
