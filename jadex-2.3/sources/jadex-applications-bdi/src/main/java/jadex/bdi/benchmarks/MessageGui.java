package jadex.bdi.benchmarks;

import jadex.bdi.runtime.AgentEvent;
import jadex.bdi.runtime.IBDIExternalAccess;
import jadex.bdi.runtime.IBDIInternalAccess;
import jadex.bdi.runtime.IBeliefListener;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.TerminationAdapter;
import jadex.commons.future.IFuture;
import jadex.commons.gui.SGUI;
import jadex.commons.transformation.annotations.Classname;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *  Gui for displaying messages.
 */
public class MessageGui extends JFrame
{
	/**
	 *  Create a new message gui.
	 */
	public MessageGui(IBDIExternalAccess agent)
	{
		final JLabel sent = new JLabel("Sent: [0]");
		final JLabel rec = new JLabel("Received: [0]");
		
		agent.scheduleStep(new IComponentStep<Void>()
		{
			@Classname("addListener")
			public IFuture<Void> execute(IInternalAccess ia)
			{
				IBDIInternalAccess bia = (IBDIInternalAccess)ia;
				bia.getBeliefbase().getBelief("sent").addBeliefListener(new IBeliefListener()
				{
					public void beliefChanged(AgentEvent ae)
					{
						sent.setText("Sent: ["+ae.getValue()+"]");
					}
				});
				bia.getBeliefbase().getBelief("received").addBeliefListener(new IBeliefListener()
				{
					public void beliefChanged(AgentEvent ae)
					{
						rec.setText("Received: ["+ae.getValue()+"]");
					}
				});
				bia.addComponentListener(new TerminationAdapter()
				{
					public void componentTerminated()
					{
						SwingUtilities.invokeLater(new Runnable()
						{
							public void run()
							{						
								MessageGui.this.dispose();	
							}
						});
					}
				});
				return IFuture.DONE;
			}
		});
		
		JPanel infos = new JPanel(new GridLayout(2,1));
		infos.add(sent);
		infos.add(rec);
		getContentPane().add(infos);
		pack();
		setLocation(SGUI.calculateMiddlePosition(this));
		setVisible(true);
	}
}
