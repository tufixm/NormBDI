package jadex.commons.gui.future;

import jadex.commons.future.IIntermediateResultListener;
import jadex.commons.gui.SGUI;

import java.awt.Component;
import java.util.Collection;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

/**
 *  Default implementation of intermediate result listener
 *  with methods called on swing thread.
 */
public abstract class SwingIntermediateDefaultResultListener<E> extends SwingDefaultResultListener<Collection<E>>	implements IIntermediateResultListener<E>
{
	//-------- constructors --------
	
	/**
	 *  Create a new listener.
	 */
	public SwingIntermediateDefaultResultListener()
	{
	}
	
	/**
	 *  Create a new listener.
	 *  @param parent The parent component (when errors should be shown as dialog).
	 */
	public SwingIntermediateDefaultResultListener(Component parent)
	{
		super(parent);
	}
	
	/**
	 *  Create a new listener.
	 *  @param logger The logger.
	 */
	public SwingIntermediateDefaultResultListener(Logger logger)
	{
		super(logger);
	}
	//-------- template methods --------

	/**
	 *  Called when an intermediate result is available.
	 *  @param result The result.
	 */
	public abstract void customIntermediateResultAvailable(E result);
	
	/**
     *  Declare that the future is finished.
	 *  This method is only called for intermediate futures,
	 *  i.e. when this method is called it is guaranteed that the
	 *  intermediateResultAvailable method was called for all
	 *  intermediate results before.
     */
    public void customFinished()
    {
    	// Empty default implementation.
    }

    //-------- methods --------
	/**
	 *  Call customIntermediateResultAvailable() on swing thread.
	 */
	public final void intermediateResultAvailable(final E result)
	{
		// Hack!!! When triggered from shutdown hook, swing might be terminated
		// and invokeLater has no effect (grrr).
		if(!SGUI.HAS_GUI || SwingUtilities.isEventDispatchThread())// || Starter.isShutdown())
		{
			customIntermediateResultAvailable(result);			
		}
		else
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					customIntermediateResultAvailable(result);
				}
			});
		}
	}

	/**
	 *  Call customFinished() on swing thread.
	 */
	public final void finished()
	{
		// Hack!!! When triggered from shutdown hook, swing might be terminated
		// and invokeLater has no effect (grrr).
		if(!SGUI.HAS_GUI || SwingUtilities.isEventDispatchThread())// || Starter.isShutdown())
		{
			customFinished();			
		}
		else
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					customFinished();
				}
			});
		}
	}

	/**
	 *  Overwritten to call intermediate and finished methods.
	 */
	public void customResultAvailable(Collection<E> result)
	{
		// Already called on swing thread.
		for(E e: result)
		{
			customIntermediateResultAvailable(e);
		}
		customFinished();
	}
}
