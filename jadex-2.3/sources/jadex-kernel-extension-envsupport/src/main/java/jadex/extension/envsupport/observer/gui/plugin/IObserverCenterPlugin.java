package jadex.extension.envsupport.observer.gui.plugin;

import jadex.extension.envsupport.observer.gui.ObserverCenter;

import java.awt.Component;

/**
 *  Interface for observer plugins.
 */
public interface IObserverCenterPlugin
{
	/** Starts the plugin
	 *  
	 *  @param the observer center
	 */
	public void start(ObserverCenter main);
	
	/** Stops the plugin
	 *  
	 */
	public void shutdown();
	
	/** Returns the name of the plugin
	 *  
	 *  @return name of the plugin
	 */
	public String getName();
	
	/** Returns the path to the icon for the plugin in the toolbar.
	 * 
	 *  @return path to the icon
	 */
	public String getIconPath();
	
	/** Returns the viewable component of the plugin
	 *  
	 *  @return viewable component of the plugin
	 */
	public Component getView();
	
	/** Refreshes the display
	 */
	public void refresh();
}
