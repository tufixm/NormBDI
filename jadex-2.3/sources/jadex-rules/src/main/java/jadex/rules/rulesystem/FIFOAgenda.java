package jadex.rules.rulesystem;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *  FIFO agenda.
 */
public class FIFOAgenda extends AbstractAgenda
{
	//-------- attributes --------
	
	/** The activations. */
	protected Set activations;
	
	/** The next activation (selected by strategy or manually form outside for debugging). */
	protected Activation next;
	
	//-------- constructors --------
	
	/**
	 *  Create a new agenda.
	 *  @param state The state.
	 */
	public FIFOAgenda()
	{
		this.activations = new LinkedHashSet();
	}
	
	/**
	 *  Add a new activation.
	 *  @param act The activation.
	 */
	public void addActivation(Activation act)
	{
		if(activations.isEmpty())
			next = act;
		this.activations.add(act);
		state++;
		notifyListeners();
	}
	
	/**
	 *  Remove an activation
	 *  @param act The activation.
	 */
	public void removeActivation(Activation act)
	{
		this.activations.remove(act);
		if(act.equals(next))
			next = null;
		state++;
		notifyListeners();
	}
	
	/**
	 *  Get the current activations.
	 *  @return The activations.
	 */
	public Collection getActivations()
	{
		return activations;
	}
	
	/**
	 *  Test if the agenda is empty.
	 *  @return True if is empty.
	 */
	public boolean isEmpty()
	{
		return activations.isEmpty();
	}
	
	/**
	 *  Get the next activation.
	 *  @return The next activation.
	 */ 
	public Activation getNextActivation()
	{
		Activation ret	= next;
		if(ret==null && activations.size()>0)
		{
			ret	= (Activation)activations.iterator().next();
		}
		return ret;
	}

	/**
	 *  Set the next activation.
	 */ 
	public void setNextActivation(Activation next)
	{
		if(activations.contains(next))
		{
			this.next	= next;
		}
		else
		{
			throw new RuntimeException("Activation not in agenda: "+next+", "+this);
		}
		notifyListeners();
	}
}
