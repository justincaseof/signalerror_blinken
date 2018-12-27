package vu.de.signalerror.blinken.ui.table;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import vu.de.signalerror.blinken.BlinkenViewer;

public class BlinkenViewerRenderer implements TableCellRenderer
{

	@Override
	public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
	{
		if (value instanceof BlinkenViewer)
			return new JLabel(value.getClass().getSimpleName());
		else if (value instanceof Boolean)
		{
			JCheckBox result = new JCheckBox();
			result.setSelected((Boolean) value);
			return result;
		}
		else
			return null;

	}

}
