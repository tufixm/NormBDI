package jadex.platform.service.message.transport.niotcpmtp;

import jadex.bridge.service.IServiceProvider;
import jadex.bridge.service.types.message.IMessageService;
import jadex.commons.SUtil;
import jadex.commons.Tuple2;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;


/**
 * The selector thread waits for NIO events and issues the appropriate actions
 * for asynchronous sending and receiving as data becomes available.
 */
public class SelectorThread implements Runnable
{
	// -------- attributes --------

	/** Flag indicating the thread should be running (set to false for shutdown). */
	protected boolean	running;
	
	/** The NIO selector. */
	protected Selector	selector;

	/** The message service for delivering received messages. */
	protected IMessageService msgservice;
	
	/** The logger. */
	protected Logger	logger;
	
	/** The service provider. */
	protected IServiceProvider	provider;
	
	/** The tasks enqueued from external threads. */
	protected List<Runnable>	tasks;

	/** The pool of output connections (address -> Future|NIOTCPOutputConnection|DeadConnection). */
	protected Map<InetSocketAddress, Object>	connections;
	
	/** The write tasks of data waiting to be written to a connection. */
	protected Map<SocketChannel, List<Tuple2<List<ByteBuffer>, Future<Void>>>>	writetasks;
	
	/** The cleanup timer. */
	protected Timer	timer;

	// -------- constructors --------

	/**
	 * Create a NIO selector thread.
	 */
	public SelectorThread(Selector selector, IMessageService msgsservice, Logger logger, IServiceProvider provider)
	{
		this.running = true;
		this.selector	= selector;
		this.msgservice	= msgsservice;
		this.logger	= logger;
		this.provider	= provider;
		this.tasks	= new ArrayList<Runnable>();
		this.connections	= new LinkedHashMap<InetSocketAddress, Object>();
		this.writetasks	= new LinkedHashMap<SocketChannel, List<Tuple2<List<ByteBuffer>, Future<Void>>>>();
	}

	// -------- Runnable interface --------

	/**
	 * Main cycle.
	 */
	public void run()
	{
		while(running)
		{
			try
			{
				// Process pending tasks.
				Runnable[]	atasks;
				synchronized(tasks)
				{
					atasks	= tasks.isEmpty() ? null : tasks.toArray(new Runnable[tasks.size()]);
					tasks.clear();
				}
				for(int i=0; atasks!=null && i<atasks.length; i++)
				{
					atasks[i].run();
				}
				
				// Wait for an event one of the registered channels
//				System.out.println("NIO selector idle");
				this.selector.select();

				// Iterate over the set of keys for which events are available
				Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
				while(selectedKeys.hasNext())
				{
					SelectionKey key = selectedKeys.next();
					selectedKeys.remove();

					if(key.isValid())
					{
						if(key.isAcceptable())
						{
							this.handleAccept(key);
						}
						else if(key.isReadable())
						{
							this.handleRead(key);
						}
						else if(key.isConnectable())
						{
							this.handleConnect(key);
						}
						else if(key.isWritable())
						{
							this.handleWrite(key);
						}
					}
					else
					{
						
//						System.out.println("writetasks3: "+writetasks.get(key.channel()));

						key.cancel();
					}
				}
			}
			catch(Exception e)
			{
				// Key may be cancelled just after isValid() has been tested.
//				e.printStackTrace();
			}
		}
		
		for(SelectionKey key: selector.keys())
		{
			Object	con	= key.attachment();
			if(con instanceof Closeable)
			{
				try
				{
					((Closeable)con).close();
//					System.out.println("closed: "+con);
				}
				catch(IOException e)
				{
				}
			}
		}
		
//		System.out.println("nio selector end");
	}
	
	//-------- methods to be called from external --------
	
	/**
	 *  Set the running flag to false to gracefully terminate the thread.
	 */
	public void	setRunning(boolean running)
	{
		this.running	= running;
		selector.wakeup();
	}
	
	public static int cnt	= 0;
	
	/**
	 *  Get a connection to one of the given addresses.
	 *  Tries all addresses in parallel and returns the first
	 *  available connection.
	 *  
	 *  @param addresses	The address to connect to.
	 *  @return A future containing a connection to the first responsive address.
	 */
	public IFuture<NIOTCPOutputConnection>	getConnection(final InetSocketAddress address)
	{
		Future<NIOTCPOutputConnection>	ret;
		
		synchronized(connections)
		{
			// Try to find existing connection.
			Object	val	= connections.get(address); 
			if(val instanceof NIOTCPOutputConnection)
			{
				ret	= new Future<NIOTCPOutputConnection>((NIOTCPOutputConnection)val);
			}
			
			// Not found: get futures for connecting to addresses
			else
			{
				// Reset connection if connection should be retried.
				if(val instanceof NIOTCPDeadConnection && ((NIOTCPDeadConnection)val).shouldRetry())
				{
					val	= null;
				}
				
				if(val==null)
				{
					final Future<NIOTCPOutputConnection>	fut	= new Future<NIOTCPOutputConnection>();
					ret	= fut;
					connections.put(address, fut);
					Runnable	task	= new Runnable()
					{
						public void run()
						{
							boolean	connected	= false;
							try
							{
								synchronized(connections)
								{
									cnt++;
								}
								
								SocketChannel	sc	= SocketChannel.open();
//								sc.socket().setSoTimeout(10);
								sc.configureBlocking(false);
								sc.connect(address);
								connected	= true;
								sc.register(selector, SelectionKey.OP_CONNECT, new Tuple2<InetSocketAddress, Future<NIOTCPOutputConnection>>(address, fut));
//								Thread.sleep(1000);
//								sc.register(selector, SelectionKey.OP_CONNECT, new Tuple2<InetSocketAddress, Future<NIOTCPOutputConnection>>(address, fut));
								logger.info("Attempting connection to: "+address);
//									System.out.println(new Date()+": Attempting connection to: "+address+", "+ret.hashCode());
							}
							catch(Exception e)
							{
								if(connected)
								{
									e.printStackTrace();
								}
								
								synchronized(connections)
								{
									cnt--;
								}
								
								logger.info("Failed connection to: "+address+": "+cnt);
								fut.setException(e);
								Cleaner	cleaner	= new Cleaner(address, NIOTCPTransport.DEADSPAN);
								synchronized(connections)
								{
									if(connections.get(address)==fut)
									{
										connections.put(address, new NIOTCPDeadConnection(cleaner));
									}
								}
							}
						}			
					};
					synchronized(tasks)
					{
						tasks.add(task);
					}
					selector.wakeup();
				}
				else if(val instanceof Future)
				{
					ret	= (Future<NIOTCPOutputConnection>)val;
				}
				else
				{
					ret	= new Future<NIOTCPOutputConnection>(new RuntimeException("Dead connection: "+address));
				}
			}
		}
		
		return ret;
	}
	
	/**
	 *  Send a message using the given connection.
	 *  @param sc	The connection.
	 * 	@param msg	The message.
	 *  @param codecids	The codec ids.
	 *  @return	A future indicating success.
	 */
	public IFuture<Void> sendMessage(final NIOTCPOutputConnection con, final byte[] prolog, final byte[] data)
	{
		final Future<Void>	ret	= new Future<Void>();
		Runnable	run	= new Runnable()
		{
			public void run()
			{
				try
				{
					SelectionKey	key	= con.getSocketChannel().keyFor(selector);
					if(key!=null && key.isValid())
					{
						// Convert message into buffers.
						List<ByteBuffer>	buffers	= new ArrayList<ByteBuffer>();
						
						buffers.add(ByteBuffer.wrap(SUtil.intToBytes(prolog.length+data.length)));
						buffers.add(ByteBuffer.wrap(prolog));
						buffers.add(ByteBuffer.wrap(data));
						
						// Add buffers as new write task.
						Tuple2<List<ByteBuffer>, Future<Void>>	task	= new Tuple2<List<ByteBuffer>, Future<Void>>(buffers, ret);
						List<Tuple2<List<ByteBuffer>, Future<Void>>>	queue	= (List<Tuple2<List<ByteBuffer>, Future<Void>>>)writetasks.get(con.getSocketChannel());
						if(queue==null)
						{
							queue	= new LinkedList<Tuple2<List<ByteBuffer>, Future<Void>>>();
							writetasks.put(con.getSocketChannel(), queue);
	//						System.out.println("writetasks0: "+writetasks.size());
						}
						queue.add(task);
						
						// Inform NIO that we want to write data.
						key.interestOps(SelectionKey.OP_WRITE);
						key.attach(con);
					}
					else
					{						
//						System.err.println("writetasks6: "+writetasks.get(con.getSocketChannel()));
						ret.setException(new RuntimeException("key is null or invalid!? "+key));
					}
				}
				catch(RuntimeException e)
				{
//					System.err.println("writetasks4: "+writetasks.get(con.getSocketChannel())+", "+e);
//					e.printStackTrace();
					
					// Message encoding failed.
					ret.setException(e);
				}
			}			
		};
		
		synchronized(tasks)
		{
			tasks.add(run);
		}
		selector.wakeup();

		
		return ret;
	}
	
	//-------- handler methods --------

	/**
	 *  Accept a connection request.
	 */
	protected void handleAccept(SelectionKey key)
	{
		// For an accept to be pending the channel must be a server socket channel.
		ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
		try
		{
			// Accept the connection and make it non-blocking
			SocketChannel sc = ssc.accept();
			sc.configureBlocking(false);
			
			// Write one byte for handshake (always works due to local network buffers).
			sc.write(ByteBuffer.wrap(new byte[1]));
	
			// Register the new SocketChannel with our Selector, indicating
			// we'd like to be notified when there's data waiting to be read
			
			sc.register(this.selector, SelectionKey.OP_READ, new NIOTCPInputConnection(sc));
			
			logger.info("Accepted connection from: "+sc.socket().getRemoteSocketAddress());
		}
		catch(Exception e)
		{
			this.logger.info("Failed connection attempt: "+ssc+", "+e);
//			e.printStackTrace();
			key.cancel();
		}
	}
	
	/**
	 *  Read data from a connection.
	 */
	protected void	handleRead(SelectionKey key)
	{
		if(key.attachment() instanceof NIOTCPInputConnection)
		{
			NIOTCPInputConnection	con	= (NIOTCPInputConnection)key.attachment();
			try
			{
				// Read as much messages as available (if any).
				for(byte[] msg=con.read(); msg!=null; msg=con.read())
				{
	//				System.out.println("Read message from: "+con);
					msgservice.deliverMessage(msg);
				}
			}
			catch(Exception e)
			{ 
//				logger.warning("NIOTCP receiving error while reading data: "+con+", "+e);
	//			e.printStackTrace();
				con.close();
				key.cancel();
			}
		}
		
		// Handle output handshake (read one byte to make sure that channel is working before sending).
		else
		{
			SocketChannel	sc	= (SocketChannel)key.channel();
			try{sc.socket().setSendBufferSize(sc.socket().getSendBufferSize()<<1);}catch(Exception e){}
			Tuple2<InetSocketAddress, Future<NIOTCPOutputConnection>>	tuple	= (Tuple2<InetSocketAddress, Future<NIOTCPOutputConnection>>)key.attachment();
			InetSocketAddress	address	= (InetSocketAddress)tuple.get(0);
			Future<NIOTCPOutputConnection>	ret	= tuple.getSecondEntity();
			try
			{
				if(sc.read(ByteBuffer.wrap(new byte[1]))!=1)
					throw new IOException("Error receiving handshake byte.");
				
				Cleaner	cleaner	= new Cleaner(address, NIOTCPTransport.MAX_KEEPALIVE);
				NIOTCPOutputConnection	con	= new NIOTCPOutputConnection(sc, address, cleaner);
				synchronized(connections)
				{
					connections.put(address, con);
				}
				cleaner.refresh();
				// Keep channel on hold until we are ready to write.
			    key.interestOps(0);
				logger.info("NIOTCP connected to: "+address);
				ret.setResult(con);
			}
			catch(Exception e)
			{
				Cleaner	cleaner	= new Cleaner(address, NIOTCPTransport.DEADSPAN);
				synchronized(connections)
				{
					connections.put(address, new NIOTCPDeadConnection(cleaner));
				}
				cleaner.refresh();
				ret.setException(e);
				logger.info("NIOTCP receiving error while opening connection (address marked as dead for "+NIOTCPTransport.DEADSPAN/1000+" seconds): "+address+", "+e);
//				e.printStackTrace();
				key.cancel();
			}
		}
	}

	/**
	 *  Read data from a connection.
	 */
	protected void	handleConnect(SelectionKey key)
	{
		SocketChannel	sc	= (SocketChannel)key.channel();
		Tuple2<InetSocketAddress, Future<NIOTCPOutputConnection>>	tuple	= (Tuple2<InetSocketAddress, Future<NIOTCPOutputConnection>>)key.attachment();
		InetSocketAddress	address	= (InetSocketAddress)tuple.get(0);
		Future<NIOTCPOutputConnection>	ret	= tuple.getSecondEntity();
		try
		{
			boolean	finished	= sc.finishConnect();
			assert finished;
			// Before connection can be used, make sure that it works by waiting for handshake byte.
			key.interestOps(SelectionKey.OP_READ);
			
			logger.info("NIOTCP connected to: "+address+", waiting for handshake");
		}
		catch(Exception e)
		{
			Cleaner	cleaner	= new Cleaner(address, NIOTCPTransport.DEADSPAN);
			synchronized(connections)
			{
				connections.put(address, new NIOTCPDeadConnection(cleaner));
				cnt--;
			}
			cleaner.refresh();
			ret.setException(e);
//			System.out.println("NIOTCP receiving error while opening connection (address marked as dead for "+NIOTCPDeadConnection.DEADSPAN/1000+" seconds): "+address+", "+e);
			logger.info("NIOTCP receiving error while opening connection (address marked as dead for "+NIOTCPTransport.DEADSPAN/1000+" seconds): "+address+", "+e);
//			e.printStackTrace();
			key.cancel();
						
//			System.out.println("writetasks2: "+writetasks.get(sc));
		}
	}
	
	/**
	 *  Write data to a connection.
	 */
	protected void handleWrite(SelectionKey key)
	{
		SocketChannel	sc	= (SocketChannel)key.channel();
		
//		System.out.println("write: "+sc.socket().isInputShutdown()+", "+sc.socket().isOutputShutdown());
		
		NIOTCPOutputConnection	con	= (NIOTCPOutputConnection)key.attachment();
		List<Tuple2<List<ByteBuffer>, Future<Void>>>	queue	= (List<Tuple2<List<ByteBuffer>, Future<Void>>>)this.writetasks.get(sc);

		try
		{
			boolean	more	= true;
			while(more)
			{
				if(queue.isEmpty())
				{
					more	= false;
					// We wrote away all data, so we're no longer interested in
					// writing on this socket.
					key.interestOps(0);

					writetasks.remove(sc);
//					System.out.println("writetasks5: "+writetasks.size());
				}
				else
				{
					Tuple2<List<ByteBuffer>, Future<Void>>	task	= queue.get(0);
					List<ByteBuffer>	buffers	= task.getFirstEntity();	
					Future<Void>	fut	= task.getSecondEntity();	
					ByteBuffer buf = buffers.get(0);
					sc.write(buf);
					if(buf.remaining()>0)
					{
						// Ouput buffer is full: stop sending for now.
						more	= false;
					}
					else
					{
						// Buffer written: remove task and inform future, when no more buffers for this task.
						buffers.remove(buf);
						if(buffers.isEmpty())
						{
							queue.remove(task);
//							System.out.println("Sent with NIO TCP: "+con.address);
							fut.setResult(null);
						}
					}
					
					con.getCleaner().refresh();
//					System.out.println("Wrote data to: "+sc.socket().getRemoteSocketAddress());
				}
			}
		}
		catch(Exception e)
		{
			con.getCleaner().remove();
			synchronized(connections)
			{
				// Connection lost, try to reconnect before marking as dead connection.
				connections.remove(con.getAddress());
				cnt--;
			}
			
			// Connection failure: notify all open tasks.
			for(Iterator<Tuple2<List<ByteBuffer>, Future<Void>>> it=queue.iterator(); it.hasNext(); )
			{
				Tuple2<List<ByteBuffer>, Future<Void>>	task	= (Tuple2<List<ByteBuffer>, Future<Void>>)it.next();
				Future<Void>	fut	= task.getSecondEntity();
				fut.setException(e);
				it.remove();
			}
			writetasks.remove(sc);
//			System.out.println("writetasks1: "+writetasks.size());
			
			logger.info("NIOTCP sending error while writing to connection: "+sc.socket().getRemoteSocketAddress()+", "+e);
//			e.printStackTrace();
			key.cancel();
		}
	}
	
	//-------- helper classes --------

//	/**
//	 *  Class for cleaning output connections after 
//	 *  max keep alive time has been reached.
//	 */
/* if_not[android] */
//	protected class Cleaner	implements	ActionListener
/* else[android]
//	protected class Cleaner implements TimerListener
//	end[android]*/
//	{
//		//-------- attributes --------
//		
//		/** The address of the connection. */
//		protected InetSocketAddress address;
//		
//		/** The timer. */
//		// Hack!!! java.util.Timer does not support cancellation of scheduled tasks, grrr.
//		protected Timer timer;
//		
//		//-------- constructors --------
//		
//		/**
//		 *  Cleaner for a specified output connection.
//		 *  @param address The address.
//		 */
//		public Cleaner(InetSocketAddress address)
//		{
//			this.address = address;
//		}
//		
//		//-------- methods --------
//		
//		
//		/**
//		 *  Called when timepoint was reached.
//		 */
/* if_not[android] */
//	    public void actionPerformed(ActionEvent event)
//	    /* $else    
//	    public void actionPerformed()
/* end[android] */
//		{
//			Object	con;
//			synchronized(connections)
//			{
//				con	= connections.remove(address);
//			}
//			if(con instanceof NIOTCPOutputConnection)
//			{
//				try
//				{
//					((NIOTCPOutputConnection)con).getSocketChannel().close();
//				}
//				catch(Exception e)
//				{
//				}
//				logger.info("Removed connection to : "+address);
//			}
//		}
//		
//		/**
//		 *  Refresh the timeout.
//		 */
//		public void refresh()
//		{
//			if(timer==null)
//			{
//				timer = new Timer(NIOTCPTransport.MAX_KEEPALIVE, this);
//				timer.start();
//			}
//			else
//			{
//				timer.restart();
//			}
//		}
//		
//		/**
//		 *  Remove this cleaner.
//		 */
//		public void remove()
//		{
//			if(timer!=null)
//			{
//				timer.stop();
//			}
//		}
//	}

	
	/**
	 *  Class for cleaning output connections after 
	 *  max keep alive time has been reached.
	 */
	protected class Cleaner
	{
		//-------- attributes --------
		
		/** The address of the connection. */
		protected InetSocketAddress address;
		
		/** The timer task. */
		protected TimerTask timertask;
		
		/** The delay time before the cleaner gets active. */
		protected long	delay;
		
		/** The connection associated to the address. */
		protected Object	con;
		
		//-------- constructors --------
		
		/**
		 *  Cleaner for a specified output connection.
		 *  @param address The address.
		 */
		public Cleaner(InetSocketAddress address, long delay)
		{
			this.address = address;
			this.delay	= delay;
		}
		
		//-------- methods --------
		
		/**
		 *  Refresh the timeout.
		 */
		public void refresh()
		{
			if(con==null)
			{
				synchronized(connections)
				{
					con	= connections.get(address);
				}
			}
			
			if(timer==null)
			{
				timer	= new Timer(true);
			}
			
			if(timertask!=null)
			{
				timertask.cancel();
			}
			timertask	= new TimerTask()
			{
				public void run()
				{
					logger.info("Timeout reached for: "+address+", "+delay);
					synchronized(connections)
					{
						if(con.equals(connections.get(address)))
						{
							connections.remove(address);
						}
						else
						{
							// Connection of cleaner is obsolete.
							con	= null;
						}
					}
					if(con instanceof NIOTCPOutputConnection)
					{
						synchronized(connections)
						{
							cnt--;
						}
						
						try
						{
							((NIOTCPOutputConnection)con).getSocketChannel().close();
						}
						catch(Exception e)
						{
						}
						logger.info("Removed connection to : "+address+", "+cnt);
					}
				}
			};
			timer.schedule(timertask, delay);
		}
		
		/**
		 *  Remove this cleaner.
		 */
		public void remove()
		{
			if(timertask!=null)
			{
				timertask.cancel();
			}
		}
	}
}
