package jadex.bdi.examples.garbagecollector_classic;

import jadex.bdi.runtime.IBDIExternalAccess;
import jadex.bdi.runtime.IBDIInternalAccess;
import jadex.bridge.IComponentStep;
import jadex.bridge.IInternalAccess;
import jadex.bridge.TerminationAdapter;
import jadex.commons.future.IFuture;
import jadex.commons.gui.SGUI;
import jadex.commons.transformation.annotations.Classname;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *  The gui plan.
 */
public class EnvironmentGui	extends JFrame
{
	protected boolean disposed;

	//-------- constructors --------

	/**
	 *  Create a new gui.
	 */
	public EnvironmentGui(final IBDIExternalAccess agent)
	{
		super("Garbage Collector Environment");
		
		agent.scheduleStep(new IComponentStep<Void>()
		{
			@Classname("start")
			public IFuture<Void> execute(IInternalAccess ia)
			{
				IBDIInternalAccess bia = (IBDIInternalAccess)ia;
				final Environment env = (Environment)bia.getBeliefbase().getBelief("env").getFact();
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						if(!disposed)
						{
							MapPanel map = new MapPanel(env);
							getContentPane().add("Center", map);
							
							setSize(400, 400);
							setLocation(SGUI.calculateMiddlePosition(EnvironmentGui.this));
							setVisible(true);
						}
					}
				});
				return IFuture.DONE;
			}
		});
//		agent.getBeliefbase().getBeliefFact("env").addResultListener(new SwingDefaultResultListener(this)
//		{
//			public void customResultAvailable(Object source, Object result)
//			{
//				MapPanel map = new MapPanel((Environment)result);
//				getContentPane().add("Center", map);
//				
//				setSize(400, 400);
//				setLocation(SGUI.calculateMiddlePosition(EnvironmentGui.this));
//				setVisible(true);
//			}
//		});
		
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				// Shutdown environment agent to close application (due to master flag).
				agent.killComponent();
			}
		});
		
		agent.scheduleStep(new IComponentStep<Void>()
		{
			@Classname("dispose")
			public IFuture<Void> execute(IInternalAccess ia)
			{
				ia.addComponentListener(new TerminationAdapter()
				{
					public void componentTerminated()
					{
						SwingUtilities.invokeLater(new Runnable()
						{
							public void run()
							{
								EnvironmentGui.this.dispose();
							}
						});
					}
				});		
				return IFuture.DONE;
			}
		});
	}

	public void dispose()
	{
		disposed	= true;
		super.dispose();
	}
}

