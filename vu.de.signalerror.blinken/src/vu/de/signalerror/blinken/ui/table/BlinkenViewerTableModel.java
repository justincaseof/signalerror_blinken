package vu.de.signalerror.blinken.ui.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import vu.de.signalerror.blinken.BlinkenViewer;

public class BlinkenViewerTableModel extends AbstractTableModel implements TableModel
{
	private static Logger logger = Logger.getLogger(BlinkenViewerTableModel.class);

	private static final long serialVersionUID = 1L;

	private List<BlinkenViewer> blinkenViewer;
	private List<TableModelListener> tableModelListeners;

	public BlinkenViewerTableModel(List<BlinkenViewer> blinkenViewer)
	{
		this.blinkenViewer = blinkenViewer;
		this.tableModelListeners = new ArrayList<TableModelListener>();
	}

	public void addTableModelListener( TableModelListener l )
	{
		tableModelListeners.add(l);
	}

	public void removeTableModelListener( TableModelListener l )
	{
		tableModelListeners.remove(l);
	}

	public Class<?> getColumnClass( int columnIndex )
	{
		switch (columnIndex)
		{
		case 0:
			return BlinkenViewer.class;
		case 1:
			return JCheckBox.class;
		default:
			throw new IndexOutOfBoundsException("Only 2 Columns!");
		}
	}

	public String getColumnName( int columnIndex )
	{
		switch (columnIndex)
		{
		case 0:
			return "BlinkenViewer";
		case 1:
			return "active";
		default:
			throw new IndexOutOfBoundsException("Only 2 Columns!");
		}
	}

	public int getRowCount( )
	{
		return blinkenViewer.size();
	}

	public int getColumnCount( )
	{
		return 2;
	}

	public Object getValueAt( int rowIndex, int columnIndex )
	{
		switch (columnIndex)
		{
		case 0:
			return blinkenViewer.get(rowIndex);
		case 1:
			return blinkenViewer.get(rowIndex).isActive();
		default:
			throw new IndexOutOfBoundsException("Only 2 Columns!");
		}
	}

	public boolean isCellEditable( int rowIndex, int columnIndex )
	{
		if (columnIndex == 1)
			return true;
		return false;
	}

	public void setValueAt( Object aValue, int rowIndex, int columnIndex )
	{
		if (columnIndex == 1)
		{
			if (aValue instanceof Boolean)
			{
				blinkenViewer.get(rowIndex).setActive((Boolean) aValue);
				logger.debug("[" + blinkenViewer.get(rowIndex).getClass().getSimpleName() + "] now " + ((Boolean) aValue ? "on" : "off"));
			}
		}
	}

	public void tableChanged( )
	{
		fireTableChanged();
	}

	private void fireTableChanged( )
	{
		for (TableModelListener l : tableModelListeners)
			l.tableChanged(new TableModelEvent(this));
	}

}
