package jadex.micro.tutorial;

import jadex.base.gui.componentviewer.AbstractComponentViewerPanel;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.commons.gui.future.SwingDefaultResultListener;
import jadex.commons.transformation.annotations.Classname;
import jadex.micro.IPojoMicroAgent;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

/**
 *  The gui for the chat bot allows
 *  changing the keyword and reply message. 
 */
public class BotGuiF4 extends AbstractComponentViewerPanel
{
	/**
	 *  Get the component.
	 */
	public JComponent getComponent()
	{
		JPanel	panel	= new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "ChatBot Settings"));
		
		final JTextField	tfkeyword	= new JTextField();
		final JTextField	tfreply	= new JTextField();
		
		GridBagConstraints	gbc	= new GridBagConstraints();
		gbc.fill	= GridBagConstraints.HORIZONTAL;
		gbc.gridy	= 0;
		gbc.weightx	= 0;
		panel.add(new JLabel("keyword"), gbc);
		gbc.weightx	= 1;
		panel.add(tfkeyword, gbc);
		gbc.gridy	= 1;
		gbc.weightx	= 0;
		panel.add(new JLabel("reply"), gbc);
		gbc.weightx	= 1;
		panel.add(tfreply, gbc);
		
		// Fetch initial values for text fields.
		getActiveComponent().scheduleStep(new IComponentStep<String[]>()
		{
			@Classname("fetch_values")
			public IFuture<String[]> execute(IInternalAccess ia)
			{
				ChatBotF4Agent	chatbot	= (ChatBotF4Agent)((IPojoMicroAgent)ia).getPojoAgent();
				return new Future<String[]>(new String[]{chatbot.getKeyword(), chatbot.getReply()});
			}
		}).addResultListener(new SwingDefaultResultListener<String[]>()
		{
			public void customResultAvailable(String[] values)
			{
				tfkeyword.setText(values[0]);
				tfreply.setText(values[1]);
			}
		});
		
		// Set new keyword.
		tfkeyword.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				final String	keyword	= tfkeyword.getText();
				getActiveComponent().scheduleStep(new IComponentStep<Void>()
				{
					@Classname("set_keyword")
					public IFuture<Void> execute(IInternalAccess ia)
					{
						ChatBotF4Agent	chatbot	= (ChatBotF4Agent)((IPojoMicroAgent)ia).getPojoAgent();
						chatbot.setKeyword(keyword);
						return IFuture.DONE;
					}
				});
			}
		});
		
		// Set new reply message.
		tfreply.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				final String	reply	= tfreply.getText();
				getActiveComponent().scheduleStep(new IComponentStep<Void>()
				{
					@Classname("set_reply")
					public IFuture<Void> execute(IInternalAccess ia)
					{
						ChatBotF4Agent	chatbot	= (ChatBotF4Agent)((IPojoMicroAgent)ia).getPojoAgent();
						chatbot.setReply(reply);
						return IFuture.DONE;
					}
				});
			}
		});
		
		return panel;
	}
}
