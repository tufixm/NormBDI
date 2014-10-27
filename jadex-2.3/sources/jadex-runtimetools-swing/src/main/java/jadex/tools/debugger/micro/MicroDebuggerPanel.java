package jadex.tools.debugger.micro;

import jadex.base.gui.plugin.IControlCenter;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IExternalAccess;
import jadex.commons.IBreakpointPanel;
import jadex.commons.gui.SGUI;
import jadex.tools.debugger.IDebuggerPanel;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIDefaults;

/**
 *  A generic debugger panel that can display
 *  arbitrary java objects.
 */
public class MicroDebuggerPanel	implements IDebuggerPanel
{
	//-------- constants --------

	/**
	 * The image icons.
	 */
	protected static final UIDefaults	icons	= new UIDefaults(new Object[]{
		"contents", SGUI.makeIcon(MicroDebuggerPanel.class, "/jadex/tools/common/images/bug_small.png")
	});

	//-------- IDebuggerPanel methods --------
	
	/** The gui component. */
	protected MicroAgentViewPanel micropanel;

	//-------- IDebuggerPanel methods --------

	/**
	 *  Called to initialize the panel.
	 *  Called on the swing thread.
	 *  @param jcc	The jcc.
	 *  @param bpp	The breakpoint panel.
	 * 	@param id	The component identifier.
	 * 	@param access	The external access of the component.
	 */
	public void init(IControlCenter jcc, IBreakpointPanel bpp, IComponentIdentifier name, IExternalAccess access)
	{
		this.micropanel	= new MicroAgentViewPanel(access, bpp);
	}

	/**
	 *  The title of the panel (name of the tab).
	 *  @return	The tab title.
	 */
	public String getTitle()
	{
		return "Micro Agent Inspector";
	}

	/**
	 *  The icon of the panel.
	 *  @return The icon (or null, if none).
	 */
	public Icon getIcon()
	{
		return icons.getIcon("contents");
	}

	/**
	 *  The component to be shown in the gui.
	 *  @return	The component to be displayed.
	 */
	public JComponent getComponent()
	{
		return micropanel;
	}
	
	/**
	 *  The tooltip text of the panel, if any.
	 *  @return The tooltip text, or null.
	 */
	public String getTooltipText()
	{
		return "Show the micro agent state and history.";
	}
	
	/**
	 *  Dispose the component.
	 */
	public void dispose()
	{
		if(micropanel!=null)
		{
			micropanel.dispose();
		}
	}

}
