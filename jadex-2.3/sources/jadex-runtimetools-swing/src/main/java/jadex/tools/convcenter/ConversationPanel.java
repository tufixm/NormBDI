package jadex.tools.convcenter;

import jadex.base.gui.CMSUpdateHandler;
import jadex.base.gui.componenttree.ComponentIconCache;
import jadex.bridge.IComponentStep;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.IMessageAdapter;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.search.SServiceProvider;
import jadex.bridge.service.types.message.IMessageService;
import jadex.bridge.service.types.message.MessageType;
import jadex.bridge.service.types.message.MessageType.ParameterSpecification;
import jadex.commons.Properties;
import jadex.commons.Property;
import jadex.commons.SUtil;
import jadex.commons.future.DelegationResultListener;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.gui.SGUI;
import jadex.commons.gui.future.SwingDefaultResultListener;
import jadex.commons.transformation.annotations.Classname;
import jadex.xml.bean.JavaReader;
import jadex.xml.bean.JavaWriter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * A panel for sending and receiving messages.
 */
public class ConversationPanel extends JSplitPane
{
	//-------- static part --------
	
	/**
	 * The image icons.
	 */
	protected static UIDefaults icons = new UIDefaults(new Object[]
	{
		// Tab icons.
		"new_message", SGUI.makeIcon(ConversationPanel.class, "/jadex/tools/common/images/new_new_message.png"),
		"sent_message", SGUI.makeIcon(ConversationPanel.class, "/jadex/tools/common/images/new_sent_message.png"),
		"received_message", SGUI.makeIcon(ConversationPanel.class, "/jadex/tools/common/images/new_received_message.png")
	});
	
	/** Id for message type. */
	protected static final String	MESSAGE_TYPE = "FipaConversationPanel-message-type";
	
	//-------- attributes --------
	
	/** The agent to dispatch events to. */
	protected IExternalAccess	agent;
	
	/** The default message type. */
	protected MessageType	defaulttype;
	
	/** The tabbed panel. */
	protected JTabbedPane	tabs;

	/** The send message panel. */
	protected FipaMessagePanel	sendpanel;

	/** The list of sent messages. */
	protected JList	sentmsgs;

	/** The list of received messages. */
	protected JList	receivedmsgs;

	
	//-------- constructors --------
	
	/**
	 *  Create the gui.
	 */
	public ConversationPanel(final IExternalAccess agent, final CMSUpdateHandler cmshandler,
		final ComponentIconCache iconcache, Component comptree, MessageType defaulttype)
	{
		super(JSplitPane.HORIZONTAL_SPLIT, true);
		setOneTouchExpandable(true);

		this.agent	= agent;
		this.defaulttype	= defaulttype;
		
		// Right side starts with initial send panel only.
		Map	msg	= new HashMap();
		msg.put(MESSAGE_TYPE, defaulttype);
		msg.put(defaulttype.getSenderIdentifier(), agent.getComponentIdentifier());
		sendpanel = new FipaMessagePanel(msg, agent, cmshandler, iconcache, comptree);

		JButton send = new JButton("Send");
		send.setToolTipText("Send the specified message");
		send.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
		send.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				// Hack! For handling conversations.
				// If no replies are sent sentmessages are 
				Map	msg	= sendpanel.getMessage();
				MessageType mt = (MessageType)msg.get(MESSAGE_TYPE);
				String ri = mt.getReceiverIdentifier();
				ParameterSpecification ris = mt.getParameter(ri);
				
				// Check if receiver is specified
				if(ris.isSet())
				{
					Object	value = msg.get(ri);
					if(value==null || value instanceof Object[] && ((Object[])value).length==0)	// Hack!!! Even for set may use single cid???
					{
						noReceiverSpecified();
					}
					else
					{
						sendMessage(msg);
					}
				}
				else
				{
					if(msg.get(ri)==null)
					{
						noReceiverSpecified();
					}
					else
					{
						sendMessage(msg);
					}
				}
			}
		});
		
		JButton reset = new JButton("Reset");
		reset.setToolTipText("Reset all specified message values");
		reset.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
		reset.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				resetMessage();
			}
		});

		JPanel	sendcont	= new JPanel(new BorderLayout());
		sendcont.add(BorderLayout.CENTER, sendpanel);
		JPanel	south	= new JPanel(new FlowLayout(FlowLayout.RIGHT));
		south.add(send);
		south.add(reset);

//		HelpBroker hb = SHelp.setupHelp(FipaConversationPanel.this,  "tools.conversationcenter");
//		if(hb != null)
//		{
//			JButton help = new JButton("Help");
//			help.setToolTipText("Open the Javahelp for the Conversation Center");
//			help.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
//			help.addActionListener(new CSH.DisplayHelpFromSource(hb));
//			south.add(help);
//		}
		sendcont.add(BorderLayout.SOUTH, south);
		final JScrollPane sendtab	= new JScrollPane(sendcont);
		sendtab.setBorder(null);

		
		// Left side contains lists of sent/received messages.
		JPanel	lists	= new JPanel(new GridBagLayout());
		GridBagConstraints	gbcons	= new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, 1, 1, 1,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0);

		sentmsgs = new JList(new DefaultListModel());
		sentmsgs.setCellRenderer(new MessageListCellRenderer());
		sentmsgs.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(e.getClickCount()==2 && sentmsgs.locationToIndex(e.getPoint())!=-1)
				{
					final Map msg	= (Map)sentmsgs.getModel()
						.getElementAt(sentmsgs.locationToIndex(e.getPoint()));
					final JPanel	msgtab	= new JPanel(new BorderLayout());
					final FipaMessagePanel	msgpanel = new FipaMessagePanel(msg, agent, cmshandler, iconcache, null);
					msgpanel.setEditable(false);
					final JScrollPane	scroll	= new JScrollPane(msgtab);
					scroll.setBorder(null);

					JButton edit = new JButton("Edit");
					edit.setToolTipText("Edit this sent message");				
					edit.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
					edit.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent ae)
						{
							sendpanel.setMessage(new HashMap(msg));
							tabs.setSelectedComponent(sendtab);
						}
					});

					JButton send = new JButton("Resend");
					send.setMargin(new Insets(2,2,2,2));
					send.setToolTipText("Send this message again");
					send.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);	
					send.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent ae)
						{
							sendMessage(msg);
						}
					});

					JButton reset = new JButton("Close");
					reset.setToolTipText("Close displayed message");
					reset.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
					reset.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent ae)
						{
							tabs.remove(scroll);
						}
					});

					msgtab.add(BorderLayout.CENTER, msgpanel);
					JPanel	south	= new JPanel(new FlowLayout(FlowLayout.RIGHT));
					south.add(edit);
					south.add(send);
					south.add(reset);
					msgtab.add(BorderLayout.SOUTH, south);
					tabs.addTab(getMessageTitle(msg), icons.getIcon("sent_message"), scroll);
					tabs.setSelectedComponent(scroll);
					
					SGUI.adjustComponentSizes(ConversationPanel.this);
				}
			}
		});
		JPanel	cpane	= new JPanel(new BorderLayout());
		cpane.add(BorderLayout.CENTER, new JScrollPane(sentmsgs));
		cpane.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), " Sent Messages "));
		lists.add(cpane, gbcons);

		gbcons.gridy++;
		receivedmsgs = new JList(new DefaultListModel());
		receivedmsgs.setCellRenderer(new MessageListCellRenderer());
		receivedmsgs.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(e.getClickCount()==2)
				{
					int	idx	= receivedmsgs.locationToIndex(e.getPoint());
					if(idx!=-1)
					{
						final Map	msg	= (Map)receivedmsgs.getModel().getElementAt(idx);
						final JPanel msgtab	= new JPanel(new BorderLayout());
						final FipaMessagePanel	msgpanel = new FipaMessagePanel(msg, agent, cmshandler, iconcache, null);
						msgpanel.setEditable(false);
						final JScrollPane	scroll	= new JScrollPane(msgtab);
						scroll.setBorder(null);
	
						JButton reply = new JButton("Reply");
						reply.setToolTipText("Set up a reply message");
						reply.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
						
						reply.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent ae)
							{
								MessageType	mt	= (MessageType)msg.get(MESSAGE_TYPE);
								Map	replymsg	= mt.createReply(msg);
								replymsg.put(MESSAGE_TYPE, mt);
								replymsg.put(mt.getSenderIdentifier(), agent.getComponentIdentifier());
								sendpanel.setMessage(replymsg);
								tabs.setSelectedComponent(sendtab);
							}
						});
	
						JButton reset = new JButton("Close");
						reset.setToolTipText("Close this message view");
						reset.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
						reset.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent ae)
							{
								tabs.remove(scroll);
							}
						});
	
						msgtab.add(BorderLayout.CENTER, msgpanel);
						JPanel	south	= new JPanel(new FlowLayout(FlowLayout.RIGHT));
						south.add(reply);
						south.add(reset);
						msgtab.add(BorderLayout.SOUTH, south);
						tabs.addTab(getMessageTitle(msg), icons.getIcon("received_message"), scroll);
						tabs.setSelectedComponent(scroll);
						SGUI.adjustComponentSizes(ConversationPanel.this);
					}
				}
			}
		});
		cpane	= new JPanel(new BorderLayout());
		cpane.add(BorderLayout.CENTER, new JScrollPane(receivedmsgs));
		cpane.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), " Received Messages "));
		lists.add(cpane, gbcons);

		gbcons.gridy++;
		gbcons.weighty	= 0;
		cpane	= new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton	clear	= new JButton("Clear");
		clear.setToolTipText("Clear the lists of sent and received messages");
		clear.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
		
		clear.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				((DefaultListModel)sentmsgs.getModel()).removeAllElements();
				((DefaultListModel)receivedmsgs.getModel()).removeAllElements();
				while(tabs.getComponentCount()>1)
					tabs.remove(1);
			}
		});
		cpane.add(clear);
		lists.add(cpane, gbcons);
		
		tabs	= new JTabbedPane();
		tabs.addTab("Send", icons.getIcon("new_message"), sendtab);
		
		// Initialize split panel
		add(lists);
		add(tabs);
		
		SGUI.adjustComponentSizes(ConversationPanel.this);
		
		SwingUtilities.invokeLater(new Runnable()
		{
			public void	run()
			{
				// Hack!!! Doesn't work when called before panel is shown.
				setDividerLocation(0.35);
			}
		});
	}
	
	//-------- methods --------

	/**
	 *  Show error when no receiver is specified.
	 */
	protected void	noReceiverSpecified()
	{
		String text = SUtil.wrapText("Cannot not send message, no receiver specified.");
		JOptionPane.showMessageDialog(SGUI.getWindowParent(ConversationPanel.this), text,
			"Message Error", JOptionPane.INFORMATION_MESSAGE);
	}

	
	/**
	 *  Get the message as a title.
	 */
	protected String getMessageTitle(Map msg)
	{
		MessageType	mt	= (MessageType)msg.get(MESSAGE_TYPE);
		String	ret	= mt.getSimplifiedRepresentation(msg);
		
		if(ret.length()>25)
		{
			ret	= ret.substring(0, 21);
			ret	+= "...)";
		}
		return ret;
	}
	
	/**
	 *  Add a received message.
	 */
	public void	addMessage(final IMessageAdapter msg)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void	run()
			{
				Map	msgmap	= new HashMap(msg.getParameterMap());
				msgmap.put(MESSAGE_TYPE, msg.getMessageType());
				((DefaultListModel)receivedmsgs.getModel()).addElement(msgmap);
			}
		});
	}

	/**
	 *  Reset the message panel to an initial state.
	 */
	public void	reset()
	{
		// Hack!!! Constructor chages thread context from swing to agent and back.
		// Make sure to call reset() afterwards.
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				resetMessage();
				setSentMessages(new Map[0]);
				setReceivedMessages(new Map[0]);
				while(tabs.getTabCount()>1)
					tabs.removeTabAt(tabs.getTabCount()-1);
			}
		});
	}
	
	/**
	 *  Reset the message to send.
	 */
	public void	resetMessage()
	{
		SwingUtilities.invokeLater(new Runnable() 
		{
			public void run() 
			{
				Map	msg	= new HashMap();
				msg.put(MESSAGE_TYPE, defaulttype);
				msg.put(defaulttype.getSenderIdentifier(), agent.getComponentIdentifier());
				sendpanel.setMessage(msg);
			}
		});				
	}
	
	//-------- helper methods --------

	/**
	 *  Get the message panel.
	 */
	public FipaMessagePanel getMessagePanel()
	{
		return sendpanel;
	}

	/**
	 *  Get the list of sent messages.
	 */
	public Map[] getSentMessages()
	{
		DefaultListModel model	= (DefaultListModel)sentmsgs.getModel();
		Map[]	ret	= new Map[model.getSize()];
		model.copyInto(ret);
		return ret;
	}

	/**
	 *  Set the list of sent messages.
	 */
	public void	setSentMessages(final Map[] msgs)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void	run()
			{
				DefaultListModel	model	= (DefaultListModel)sentmsgs.getModel();
				model.removeAllElements();
				for(int i=0; i<msgs.length; i++)
				{
					model.addElement(msgs[i]);
				}
			}
		});
	}

	/**
	 *  Set the list of received messages.
	 */
	public void	setReceivedMessages(final Map[] msgs)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void	run()
			{
				DefaultListModel	model	= (DefaultListModel)receivedmsgs.getModel();
				model.removeAllElements();
				for(int i=0; i<msgs.length; i++)
				{
					model.addElement(msgs[i]);
				}
			}
		});
	}

	/**
	 *  Initialize the plugin from the properties.
	 */
	public void setProperties(Properties props)
	{
		try
		{
			// Load last state of message panel.
			String	msg	= props.getStringProperty(ConversationPlugin.LAST_MESSAGE);
			
			if(msg!=null)
			{
				final Map	message	= decodeMessage(msg);
				// Update sender.
				MessageType	mt	= (MessageType)message.get(MESSAGE_TYPE);
				message.put(mt.getSenderIdentifier(), agent.getComponentIdentifier());
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						getMessagePanel().setMessage(message);
					}
				});
			}
			else
			{
				resetMessage();
			}
			
			// Load list of sent messages.
			final List sentmsgs	= new ArrayList();
			Property[]	sents	= props.getProperties(ConversationPlugin.SENT_MESSAGE);
			for(int i=0; i<sents.length; i++)
			{
				final boolean last = i == sents.length-1;
				Map	message	= decodeMessage(sents[i].getValue());
				// Update sender.
				MessageType	mt	= (MessageType)message.get(MESSAGE_TYPE);
				message.put(mt.getSenderIdentifier(), agent.getComponentIdentifier());
				sentmsgs.add(0, message);	// Re-revert order
				
				if(last)
					setSentMessages((Map[])sentmsgs.toArray(new Map[sentmsgs.size()]));
			}
		}
		catch(Exception e)
		{
			final String text = SUtil.wrapText("Could not decode stored message: "+e.getMessage());
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					JOptionPane.showMessageDialog(SGUI.getWindowParent(ConversationPanel.this), text, "Message problem", JOptionPane.INFORMATION_MESSAGE);
				}
			});
		}
	}

	/**
	 *  Fill in message values from string.
	 */
	public Map decodeMessage(String msg)
	{
		Map map = (Map)JavaReader.objectFromXML(msg, null);	// Todo: classloader!?
		return map;
	}

	/** 
	 *  Get properties to be saved.
	 */
	public Properties	getProperties()
	{
		if(!(SwingUtilities.isEventDispatchThread()))// ||  Starter.isShutdown()))
			throw new RuntimeException("Can only save properties from swing thread");
			
		Properties	props	= new Properties();
		// Save message displayed in message panel.
		Map	message	= getMessagePanel().getMessage();
		String msg = (String)encodeMessage(message);
		props.addProperty(new Property(ConversationPlugin.LAST_MESSAGE, msg));
		
		// Save list of sent messages (limit to 5 messages).
		Map[]	msgs = getSentMessages();
		Set	saved	= new HashSet();	// Used to avoid duplicates;
		for(int i=msgs.length-1; i>=0 && saved.size()<5; i--)	// Backward loop to save newest messages.
		{
			msg	= (String)encodeMessage(msgs[i]);
			if(!saved.contains(msg))
			{
				props.addProperty(new Property(ConversationPlugin.SENT_MESSAGE, msg));
				saved.add(msg);
			}
		}
		
		return props;
	}

	/**
	 *  Convert message to a string.
	 *  @param message The message.
	 */
	public String encodeMessage(Map message)
	{
		String	msg	= JavaWriter.objectToXML(message, null);	// Todo: classloader!?
		return msg;
	}
	
	/**
	 *  Send a message.
	 */
	protected void sendMessage(final Map msg)
	{
		// Remove message type for sending.
		final Map	sendmsg	= new HashMap(msg);
		final MessageType	mt	= (MessageType)sendmsg.remove(MESSAGE_TYPE);
		
		agent.scheduleStep(new IComponentStep<Void>()
		{
			@Classname("sendM")
			public IFuture<Void> execute(final IInternalAccess ia)
			{
				final Future	ret	= new Future();
						
				SServiceProvider.getService(ia.getServiceContainer(), IMessageService.class, RequiredServiceInfo.SCOPE_PLATFORM)
					.addResultListener(ia.createResultListener(new DelegationResultListener(ret)
				{
					public void customResultAvailable(Object result)
					{
						IMessageService	ms	= (IMessageService)result;
						ms.sendMessage(sendmsg, mt, ia.getComponentIdentifier(), ia.getModel().getResourceIdentifier(), null, null)
							.addResultListener(ia.createResultListener(new DelegationResultListener(ret)));
					}
				}));
				return ret;
			}
		}).addResultListener(new SwingDefaultResultListener(this)
		{
			public void customResultAvailable(Object result)
			{
				// Re-add message type for storing sent msg. 
				sendmsg.put(MESSAGE_TYPE, mt);
				((DefaultListModel)sentmsgs.getModel()).addElement(sendmsg);
			}
		});
	}

	/**
	 *  Display messages with performative and content instead of type.
	 *  This is because the type of messages is usually "fipamsg".
	 */
	class MessageListCellRenderer extends DefaultListCellRenderer
	{
		public Component getListCellRendererComponent(JList list, Object value, 
			int index, boolean sel, boolean hasfocus)
		{		
			assert value instanceof Map;
			value	= getMessageTitle((Map)value);
			return super.getListCellRendererComponent(list, value, index, sel, hasfocus);
		}
	}
}
