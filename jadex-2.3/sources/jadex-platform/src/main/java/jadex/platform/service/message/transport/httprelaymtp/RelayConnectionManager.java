package jadex.platform.service.message.transport.httprelaymtp;

import jadex.bridge.IComponentIdentifier;
import jadex.commons.HttpConnectionManager;
import jadex.commons.SUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Scanner;

/**
 *  The connection manager performs http requests and further
 *  allows asynchronously terminating open connections
 *  to avoid hanging on e.g. platform shutdown.
 */
public class RelayConnectionManager	extends HttpConnectionManager
{
	//-------- constants --------
	
	/** A buffer for reading response data (ignored but needs to be read for connection to be reusable). */
	protected static final byte[]	RESPONSE_BUF	= new byte[8192];
	
	//-------- methods --------
	
	/**
	 *  Ping a relay server.
	 *  @throws IOException on connection failures
	 */
	public void	ping(String address)	throws IOException
	{
		address	= httpAddress(address);
		
		// Hack!!! when pinging before awareness message, strip extension
		if(address.endsWith("/awareness"))
		{
			address	= address.substring(0, address.lastIndexOf("/awareness")+1);
		}
		else if(address.endsWith("/awareness/"))
		{
			address	= address.substring(0, address.lastIndexOf("/awareness/")+1);
		}
		
		HttpURLConnection	con	= null;
		try
		{
			con	= openConnection(address + "ping");
			con.connect();
			int	code	= con.getResponseCode();
			if(code!=HttpURLConnection.HTTP_OK)
				throw new IOException("HTTP code "+code+": "+con.getResponseMessage());
			while(con.getInputStream().read(RESPONSE_BUF)!=-1)
			{
			}
			con.getInputStream().close();
		}
		finally
		{
			if(con!=null)
			{
				remove(con);
			}
		}
	}
	
	/**
	 *  Open a receiving connection.
	 *  The connection should be removed when it is closed to avoid memory leaks. 
	 *  @return The connection.
	 *  @throws IOException on connection failures
	 */
	public HttpURLConnection	openReceiverConnection(String address, IComponentIdentifier receiver)	throws IOException
	{
		address	= httpAddress(address);
		HttpURLConnection	con	= null;
		String	xmlid	= receiver.getRoot().getName();
		con	= openConnection(address+"?id="+URLEncoder.encode(xmlid, "UTF-8"));
		con.setUseCaches(false);
//		con.setRequestProperty("User-Agent", "Jadex4Android2.1");
//		con.setRequestProperty("Cache-Control", "no-cache, no-transform");
//		con.setRequestProperty("Pragma", "no-cache");
		
		//						// Hack!!! Do not validate server (todo: enable/disable by platform argument).
		//						if(con instanceof HttpsURLConnection)
		//						{
		//							HttpsURLConnection httpscon = (HttpsURLConnection) con;  
		//					        httpscon.setHostnameVerifier(new HostnameVerifier()  
		//					        {        
		//					            public boolean verify(String hostname, SSLSession session)  
		//					            {  
		//					                return true;  
		//					            }  
		//					        });												
		//						}

		return con;
	}
	
	/**
	 *  Get known servers from a server.
	 *  @param address	The remote server address.
	 *  @return The comma separated server list.
	 *  @throws IOException on connection failures
	 */
	public String	getServers(String address)	throws IOException
	{
		return getPeerServers(address, null, false);
	}

	/**
	 *  Get known servers from a peer server.
	 *  @param peeraddress	The remote server address.
	 *  @param ownaddress	The local server address supplied for mutual connection (may be null if not connecting to peer).
	 *  @param initial	True, when peer connects initially (only sent when ownaddress!=null).
	 *  @return The comma separated server list.
	 *  @throws IOException on connection failures
	 */
	public String	getPeerServers(String peeraddress, String ownaddress, boolean initial)	throws IOException
	{
		String	ret;
		HttpURLConnection	con	= null;
		peeraddress	= httpAddress(peeraddress);
		ownaddress	= ownaddress!=null ? httpAddress(ownaddress) : null;
		try
		{
			con	= openConnection(peeraddress+"servers"
				+(ownaddress!=null ? "?peerurl="+URLEncoder.encode(ownaddress, "UTF-8")+"&initial="+initial : ""));
			if(con.getContentType()!=null && con.getContentType().startsWith("text/plain"))
			{
				ret	= new Scanner(con.getInputStream()).useDelimiter("\\A").next();
			}
			else
			{
				throw new IOException("Unexpected content type: "+con.getContentType());
			}
		}
		finally
		{
			if(con!=null)
			{
				remove(con);
			}
		}
		return ret;
	}
	
	/**
	 *  Post a message.
	 *  @throws IOException on connection failures
	 */
	public void postMessage(String address, IComponentIdentifier targetid, byte[][] data)	throws IOException
	{
		address	= httpAddress(address);
		byte[]	iddata	= targetid.getName().getBytes("UTF-8");
		
		HttpURLConnection	con	= null;
		OutputStream	out;
		int	code;
		try
		{
			con	= openConnection(address);
			int	datalength	= 0;
			for(int i=0; i<data.length; i++)
			{
				datalength	+= data[i].length;
			}
	
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setRequestProperty("Content-Type", "application/octet-stream");
			con.setRequestProperty("Content-Length", ""+(4+iddata.length+4+datalength));
	
	//		synchronized(POOL_LOCK)
			{
				con.connect();
		
				out	= con.getOutputStream();
	
				out.write(SUtil.intToBytes(iddata.length));
				out.write(iddata);
				out.write(SUtil.intToBytes(datalength));
				for(int i=0; i<data.length; i++)
				{
					out.write(data[i]);
				}
				out.flush();
	//								out.close();
		
				code	= con.getResponseCode();
			}
			
			if(code!=HttpURLConnection.HTTP_OK)
			{
				throw new IOException("HTTP code "+code+": "+con.getResponseMessage()+" target="+targetid);
			}
			while(con.getInputStream().read(RESPONSE_BUF)!=-1)
			{
			}
	//		con.getInputStream().close();
		}
		finally
		{
			if(con!=null)
			{
				remove(con);
			}			
		}
	}
	
	//-------- helper methods --------
	
	/**
	 *  Convert a potential 'relay-' address to normal http(s) address.
	 *  Also makes sure that addresses end with '/'.
	 */
	public static String	httpAddress(String address)
	{
		if(address.startsWith("relay-"))
		{
			address	= address.substring(6);
		}
		if(!address.endsWith("/") && !address.endsWith("/awareness"))	// For compatibility with old servers: don't add slash (Todo: remove)
		{
			address	+= "/";
		}
		return address;
	}
	
	/**
	 *  Convert a potential non 'relay-' address to relay address.
	 *  Also makes sure that addresses end with '/'.
	 */
	public static String	relayAddress(String address)
	{
		if(!address.startsWith("relay-"))
		{
			address	= "relay-"+address;
		}
		if(!address.endsWith("/") && !address.endsWith("/awareness"))	// For compatibility with old servers: don't add slash (Todo: remove)
		{
			address	+= "/";
		}
		return address;
	}
}
