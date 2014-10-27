package jadex.base.gui.jtable;

import jadex.bridge.IComponentIdentifier;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * A renderer for AgentIdentifiers. This class is used to display the receiver
 * entry in the table. The receiver is displayed with its addresses.
 */
public class ComponentIdentifierRenderer extends DefaultTableCellRenderer
{
	/**
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
	 * Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
		IComponentIdentifier cid = (IComponentIdentifier)value;
		if(cid!=null)
		{
			setText(cid.getName());
			String[] addresses = cid.getAddresses();
			String tooltip = "<b>" + cid.getName() + "</b>";
			if(addresses!=null)
			{
				for(int i = 0; i < addresses.length; i++)
				{
					tooltip += "<br>" + addresses[i];
				}
			}
			setToolTipText("<html>" + tooltip + "</html>");
		}
		return this;
	}
}
