package jadex.micro.tutorial;

import jadex.bridge.IComponentStep;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.commons.future.IFuture;
import jadex.commons.future.IIntermediateFuture;
import jadex.commons.future.IIntermediateResultListener;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *  Basic chat user interface.
 */
public class ChatGuiD5 extends JFrame
{
	//-------- attributes --------
	
	/** The textfield with received messages. */
	protected JTextArea received;
	
	//-------- constructors --------
	
	/**
	 *  Create the user interface
	 */
	public ChatGuiD5(final IExternalAccess agent)
	{
		super(agent.getComponentIdentifier().getName());
		this.setLayout(new BorderLayout());
		
		received = new JTextArea(10, 20);
		final JTextField message = new JTextField();
		JButton send = new JButton("send");
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(message, BorderLayout.CENTER);
		panel.add(send, BorderLayout.EAST);
		
		getContentPane().add(received, BorderLayout.CENTER);
		getContentPane().add(panel, BorderLayout.SOUTH);
		
		send.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				final String text = message.getText(); 
				agent.scheduleStep(new IComponentStep<Void>()
				{
					public IFuture<Void> execute(IInternalAccess ia)
					{
						IIntermediateFuture<IChatService>	fut	= ia.getServiceContainer().getRequiredServices("chatservices");
						fut.addResultListener(new IIntermediateResultListener<IChatService>()
						{
							public void resultAvailable(Collection<IChatService> result)
							{
								for(Iterator<IChatService> it=result.iterator(); it.hasNext(); )
								{
									IChatService cs = it.next();
									try
									{
										cs.message(agent.getComponentIdentifier().getName(), text);
									}
									catch(Exception e)
									{
										System.out.println("Could not send message to: "+cs);
									}
								}
							}
							
							public void intermediateResultAvailable(IChatService cs)
							{
								System.out.println("found: "+cs);
								cs.message(agent.getComponentIdentifier().getName(), text);
							}
							
							public void finished()
							{
							}
							
							public void exceptionOccurred(Exception exception)
							{
							}
							
						});
						return IFuture.DONE;
					}
				});
			}
		});
		
		addWindowListener(new WindowAdapter()
		{
			public void windowClosed(WindowEvent e)
			{
				agent.killComponent();
			}
		});

		pack();
		setVisible(true);
	}
	
	/**
	 *  Method to add a new text message.
	 *  @param text The text.
	 */
	public void addMessage(final String text)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				received.append(text+"\n");
			}
		})
	;}
}
