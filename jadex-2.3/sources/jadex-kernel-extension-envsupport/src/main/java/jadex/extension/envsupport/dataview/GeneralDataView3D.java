package jadex.extension.envsupport.dataview;

import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.space3d.Space3D;

import java.util.Map;

/**
 * A general 2D view that presents all Objects in a Space2D to the observer.
 *
 */
public class GeneralDataView3D implements IDataView
{
	/** The space the view is connected to */
	protected Space3D space;
	
	/** The current objects */
	protected Object[] objects;
	
	/** Flag whether the view needs updating */
	protected boolean dirty;
	
	/** Internal lock */
	protected Object monitor;
	
	/**
	 * Creates a general 2D view that presents all Objects in a Space2D to the observer.
	 */
	public GeneralDataView3D()
	{
		dirty = true;
	}
	
	/**
	 *  Init the space.
	 */
	public void init(IEnvironmentSpace space, Map props)
	{
		this.space = (Space3D)space;
		this.monitor	= this.space.getMonitor();
	}
	
	/**
	 * Returns the type of the view.
	 * @return type of the view
	 */
	public String getType()
	{
		return IDataView.SIMPLE_VIEW_2D;
	}
	
	/**
	 * Returns a list of objects in this view
	 * @return list of objects
	 */
	public Object[] getObjects()
	{
		synchronized (monitor)
		{
			if (dirty)
			{
				Space3D spc = (Space3D) space;
				objects = spc.getSpaceObjects();
				dirty = false;
			}
			return objects;
		}
	}
	
	/**
	 *  Updates the view.
	 *  
	 *  @param space the space of the view
	 */
	public void update(IEnvironmentSpace space)
	{
		synchronized(monitor)
		{
			dirty = true;
		}
	}
}
