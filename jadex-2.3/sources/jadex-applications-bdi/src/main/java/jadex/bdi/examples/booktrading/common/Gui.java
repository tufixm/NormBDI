package jadex.bdi.examples.booktrading.common;

import jadex.bdi.runtime.IBDIExternalAccess;
import jadex.bdi.runtime.IBDIInternalAccess;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.TerminationAdapter;
import jadex.commons.future.IFuture;
import jadex.commons.future.IResultListener;
import jadex.commons.gui.SGUI;
import jadex.commons.transformation.annotations.Classname;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *  The gui allows to add and delete buy or sell orders and shows open and
 *  finished orders.
 */
public class Gui extends JFrame
{
	//-------- constructors --------

	/**
	 *  Shows the gui, and updates it when beliefs change.
	 */
	public Gui(final IBDIExternalAccess agent)//, final boolean buy)
	{
		super((GuiPanel.isBuyer(agent)? "Buyer: ": "Seller: ")+agent.getComponentIdentifier().getName());
		
//			System.out.println("booktrading0: "+agent.getComponentIdentifier());
		GuiPanel gp = new GuiPanel(agent);
		
		add(gp, BorderLayout.CENTER);
		pack();
		setLocation(SGUI.calculateMiddlePosition(this));
		setVisible(true);
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				agent.killComponent();
			}
		});
		
		// Dispose frame on exception.
		IResultListener<Void>	dislis	= new IResultListener<Void>()
		{
			public void exceptionOccurred(Exception exception)
			{
//				System.out.println("booktrading5: "+agent.getComponentIdentifier());
				dispose();
			}
			public void resultAvailable(Void result)
			{
//				System.out.println("booktrading6: "+agent.getComponentIdentifier());
			}
		};
		
//		System.out.println("booktrading1: "+agent.getComponentIdentifier());
		agent.scheduleStep(new IComponentStep<Void>()
		{
			@Classname("dispose")
			public IFuture<Void> execute(IInternalAccess ia)
			{
//				System.out.println("booktrading2: "+agent.getComponentIdentifier());
				IBDIInternalAccess bia = (IBDIInternalAccess)ia;
				bia.addComponentListener(new TerminationAdapter()
				{
					public void componentTerminated()
					{
//						System.out.println("booktrading3: "+agent.getComponentIdentifier());
						SwingUtilities.invokeLater(new Runnable()
						{
							public void run()
							{
//								System.out.println("booktrading4: "+agent.getComponentIdentifier());
								dispose();
							}
						});
					}
				});
				return IFuture.DONE;
			}
		}).addResultListener(dislis);
	}
}