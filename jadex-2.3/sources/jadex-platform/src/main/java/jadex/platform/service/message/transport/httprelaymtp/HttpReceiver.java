package jadex.platform.service.message.transport.httprelaymtp;

import jadex.bridge.IComponentStep;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.awareness.AwarenessInfo;
import jadex.bridge.service.types.awareness.IAwarenessManagementService;
import jadex.bridge.service.types.message.ICodec;
import jadex.bridge.service.types.message.IMessageService;
import jadex.commons.SUtil;
import jadex.commons.future.CounterResultListener;
import jadex.commons.future.ExceptionDelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.micro.annotation.Binding;
import jadex.platform.service.message.MapSendTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *  The receiver connects to the relay server
 *  and accepts messages.
 */
public class HttpReceiver
{
	//-------- attributes --------
	
	/** The transport. */
	protected HttpRelayTransport	transport;

	/** The shutdown flag. */
	protected boolean	shutdown;
	
	/** The external access. */
	protected IExternalAccess access;
	
	/** The current connected server address (if any). */
	protected String address;
	
	/** The logger (set on first access). */
	protected Logger	logger;
	
	//-------- constructors --------
	
	/**
	 *  Create a new receiver.
	 */
	public HttpReceiver(HttpRelayTransport transport, IExternalAccess access)
	{
		this.transport	= transport;
		this.access	= access;
	}
	
	//-------- methods --------
	
	/**
	 *  (Re-)Start the receiver.
	 */
	public void start()
	{
		if(!shutdown)
		{
			final long	lasttry	= System.currentTimeMillis();
			fetchServerAddresses().addResultListener(new IResultListener<String>()
			{
				public void resultAvailable(String curadrs)
				{
					if(!shutdown)
					{
						selectServer(curadrs).addResultListener(new IResultListener<String>()
						{
							public void resultAvailable(String adr)
							{
								if(!shutdown)
								{
									handleConnection(adr).addResultListener(new IResultListener<Void>()
									{
										public void resultAvailable(Void result)
										{
											// Connection should always end with error. 
											assert true;
										}
										
										public void exceptionOccurred(Exception exception)
										{
											restart(exception);
										}
									});
								}
							}
							
							public void exceptionOccurred(Exception exception)
							{
								restart(exception);
							}
						});
					}
				}
				
				public void exceptionOccurred(Exception exception)
				{
					restart(exception);
				}
				
				protected void restart(Exception e)
				{
					String copy = address;
					if(copy!=null)
					{
						HttpReceiver.this.transport.connected(copy, true);
						address	= null;
					}
					
					if(!shutdown)
					{
						log(Level.WARNING, "Relay transport exception causing reconnect: "+e);
						
						// When last connection attempt was less than 30 seconds ago, wait some time.
						if(System.currentTimeMillis()-lasttry<HttpRelayTransport.ALIVETIME)
						{
							long	sleep	= lasttry+HttpRelayTransport.ALIVETIME-System.currentTimeMillis();
							log(Level.INFO, "Relay transport waiting "+sleep+" milliseconds before reconnect.");
							Timer	timer	= new Timer(true);
							timer.schedule(new TimerTask()
							{
								public void run()
								{
									if(!shutdown)
									{
										start();
									}
								}
							}, sleep);
						}
						else
						{
							start();							
						}
					}
				}
			});
		}
	}
	
	/**
	 *  Stop the receiver.
	 */
	public void	stop()
	{
		shutdown	= true;
		access	= null;
		address	= null;
	}
	
	//-------- helper methods --------
	
	/**
	 *  Read a complete message from the stream.
	 */
	protected static byte[]	readMessage(InputStream in) throws IOException
	{
		byte[] rawmsg	= null;
		
		// Read message header (size)
		int msg_size;
		byte[] asize = new byte[4];
		for(int i=0; i<asize.length; i++)
		{
			int	b	= in.read();
			if(b==-1) 
				throw new IOException("Stream closed");
			asize[i] = (byte)b;
		}
		
		msg_size = SUtil.bytesToInt(asize);
//		System.out.println("reclen: "+msg_size);
		if(msg_size>0)
		{
			rawmsg = new byte[msg_size];
			int count = 0;
			while(count<msg_size) 
			{
				int bytes_read = in.read(rawmsg, count, msg_size-count);
				if(bytes_read==-1) 
					throw new IOException("Stream closed");
				count += bytes_read;
			}
		}
		
		return rawmsg;
	}
	
	/**
	 *  Post a received awareness info to awareness service (if any).
	 */
	protected void	postAwarenessInfo(final byte[] data, final int type, final Map<Byte, ICodec> codecs)
	{
		SServiceProvider.getService(access.getServiceProvider(), IAwarenessManagementService.class, Binding.SCOPE_PLATFORM)
			.addResultListener(new IResultListener<IAwarenessManagementService>()
		{
			public void resultAvailable(IAwarenessManagementService awa)
			{
				try
				{
					AwarenessInfo	info	= (AwarenessInfo)MapSendTask.decodeMessage(data, codecs, getClass().getClassLoader());
					awa.addAwarenessInfo(info);
				}
				catch(Exception e)
				{
					// Todo: logger?
//					System.out.println("Error receiving awareness info: "+e);
				}
			}
			
			public void exceptionOccurred(Exception exception)
			{
				// No awa service -> ignore awa infos.
			}
		});		
	}

	/**
	 *  Get the current addresses (if any).
	 */
	public synchronized String[]	getAddresses()
	{
		return address!=null ? new String[]{address} : SUtil.EMPTY_STRING_ARRAY;
	}

	/**
	 *  Do some log output.
	 */
	protected void	log(final Level level, final String msg)
	{
		if(logger!=null)
		{
			logger.log(level, msg);
		}
		else
		{
			access.scheduleStep(new IComponentStep<Void>()
			{
				public IFuture<Void> execute(IInternalAccess ia)
				{
					logger	= ia.getLogger();
					logger.log(level, msg);
					return IFuture.DONE;
				}
			});
		}
	}
	
	//-------- connection handling --------
	
	/**
	 *  Fetch an up-to-date server list.
	 *  @return A comma separated list of potential servers.
	 */
	protected IFuture<String>	fetchServerAddresses()
	{
		log(Level.INFO, "Relay transport fetching server addresses from: "+transport.getDefaultAddresses());
		
		final Future<String>	ret	= new Future<String>();
		StringTokenizer	stok	= new StringTokenizer(transport.getDefaultAddresses(), ", ");
		final CounterResultListener<Void>	crl	= new CounterResultListener<Void>(stok.countTokens(), true,
			new ExceptionDelegationResultListener<Void, String>(ret)
		{
			public void customResultAvailable(Void result)
			{
				// If all threads done, but no result -> set exception.
				ret.setExceptionIfUndone(new RuntimeException("Cannot retrieve server list."));
			}
		});
		
		while(stok.hasMoreTokens() && !ret.isDone())
		{
			final String	adr	= stok.nextToken().trim();
			transport.getThreadPool().execute(new Runnable()
			{
				public void run()
				{
					if(!ret.isDone())
					{
						try
						{
							String	curadrs	= transport.getConnectionManager().getServers(adr);
							log(Level.INFO, "Relay transport got server addresses from: "+adr+", "+curadrs);
							ret.setResultIfUndone(curadrs);
						}
						catch(Exception e)
						{
							crl.exceptionOccurred(e);
						}
					}
				}
			});
			
			if(!ret.isDone())
			{
				try
				{
					Thread.sleep(20);	// Short delay to give servers a chance to reply.
				}
				catch(InterruptedException e)
				{
				}
			}
		}
		
		return ret;
	}
	
	/**
	 *  Select a server to connect to.
	 *  @return The address of an available server.
	 */
	protected IFuture<String>	selectServer(String curadrs)
	{
		log(Level.INFO, "Relay transport selecting server from: "+curadrs);
		final Future<String>	ret	= new Future<String>();
		StringTokenizer	stok	= new StringTokenizer(curadrs, ", ");
		List<String>	adrs	= new LinkedList<String>();
		Random	rnd	= new Random();
		while(stok.hasMoreTokens())
		{
			// Insert addresses randomly to distribute load across servers.
			adrs.add(rnd.nextInt(adrs.size()+1), stok.nextToken().trim());
		}
		
		final CounterResultListener<Void>	crl	= new CounterResultListener<Void>(adrs.size(), true,
			new ExceptionDelegationResultListener<Void, String>(ret)
		{
			public void customResultAvailable(Void result)
			{
				// If all threads done, but no result -> set exception.
				ret.setExceptionIfUndone(new RuntimeException("No server available."));
			}
		});
		
		for(int i=0; !ret.isDone() && i<adrs.size(); i++)
		{
			final String	adr	= adrs.get(i);
			transport.getThreadPool().execute(new Runnable()
			{
				public void run()
				{
					if(!ret.isDone())
					{
						try
						{
							transport.getConnectionManager().ping(adr);
							ret.setResultIfUndone(adr);
						}
						catch(Exception e)
						{
							crl.exceptionOccurred(e);
						}
					}
				}
			});
			
			if(!ret.isDone())
			{
				try
				{
					Thread.sleep(20);	// Short delay to give servers a chance to reply.
				}
				catch(InterruptedException e)
				{
				}
			}
		}
		
		return ret;
	}
	
	/**
	 *  Main thread while connected.
	 *  @return When the connection is closed.
	 */
	protected IFuture<Void> handleConnection(final String adr)
	{
		final Future<Void>	ret	= new Future<Void>();
		SServiceProvider.getService(access.getServiceProvider(), IMessageService.class, RequiredServiceInfo.SCOPE_PLATFORM)
			.addResultListener(new ExceptionDelegationResultListener<IMessageService, Void>(ret)
		{
			public void customResultAvailable(final IMessageService ms)
			{
				ms.getAllCodecs().addResultListener(new ExceptionDelegationResultListener<Map<Byte,ICodec>, Void>(ret)
				{
					public void customResultAvailable(final Map<Byte,ICodec> codecs)
					{
						transport.getThreadPool().execute(new Runnable()
						{
							public void run()
							{
								HttpURLConnection	con	= null;
								Timer	timeout	= null;
								try
								{
									con	= transport.getConnectionManager().openReceiverConnection(adr,
										HttpReceiver.this.access.getComponentIdentifier());
									
									InputStream	in	= con.getInputStream();
									address	= RelayConnectionManager.relayAddress(adr);
									transport.connected(address, false);
//									System.out.println("connected to: "+address);
									
									// Start a read timeout timer.
									final long[]	time	= new long[]{System.currentTimeMillis()};
									timeout	= new Timer(true);
									timeout.schedule(new TimeoutTask(time, con), (long)(SRelay.PING_DELAY*1.5));
									
									while(true)
									{
										// Read message type.
										int	b	= in.read();
										
										// Update timer
										long	newtime	= System.currentTimeMillis();
										if(newtime/1000>time[0]/1000)
										{
											time[0]	= newtime;
											timeout.schedule(new TimeoutTask(time, con), (long)(SRelay.PING_DELAY*1.5));
										}
										if(b==-1)
										{
											throw new IOException("Stream closed");
										}
										else if(b==SRelay.MSGTYPE_PING)
										{
			//								System.out.println("Received ping");
										}
										else if(b==SRelay.MSGTYPE_AWAINFO)
										{
											final byte[] rawmsg = readMessage(in);
											postAwarenessInfo(rawmsg, b, codecs);
										}
										else if(b==SRelay.MSGTYPE_DEFAULT)
										{
											final byte[] rawmsg = readMessage(in);
											if(rawmsg!=null)
											{
												try
												{
													ms.deliverMessage(rawmsg);
												}
												catch(Exception e)
												{
													log(Level.WARNING, "Relay transport exception when delivering message: "+e+", "+rawmsg);
												}
											}
										}
									}		
								}
								catch(Exception e)
								{
									ret.setException(e);
								}
								
								if(con!=null)
								{
									transport.getConnectionManager().remove(con);
								}
								
								if(timeout!=null)
								{
									timeout.cancel();
								}
							}
						});						
					}
				});
			}
		});
		return ret;
	}
	
	/**
	 *  The timeout task
	 */
	public static class TimeoutTask	extends TimerTask
	{
		/** The time of the last message (connection alive if different from start time). */
		protected long[]	time;
		
		/** The time at creation. */
		protected long	starttime;
		
		/** The url connection. */
		protected HttpURLConnection	con;
		
		public TimeoutTask(long[] time, HttpURLConnection con)
		{
			this.time	= time;
			this.starttime	= time[0];
			this.con	= con;
		}
		
		public void run()
		{
			if(time[0]!=starttime)
			{
//				System.out.println("relay alive");
			}
			else
			{
//				System.out.println("relay timeout");
				RelayConnectionManager.closeConnection(con);
			}
		}
	}
}
