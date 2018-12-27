/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package vu.de.signalerror.blinken.ui.table;

/*
 * TableRenderDemo.java requires no other files.
 */

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import vu.de.signalerror.blinken.BlinkenApplication;
import vu.de.signalerror.blinken.BlinkenViewer;

/**
 * TableRenderDemo is just like TableDemo, except that it explicitly initializes
 * column sizes and it uses a combo box as an editor for the Sport column.
 */
public class BlinkenViewerTable extends JPanel
{
	private static final long serialVersionUID = 1L;

	private List<BlinkenViewer> blinkenViewer;

	private BlinkenViewerTableModel tableModel;

	public BlinkenViewerTable(BlinkenApplication blinkenApp)
	{
		super(new GridLayout(1, 0));

		this.blinkenViewer = blinkenApp.getBlinkenViewers();
		this.tableModel = new BlinkenViewerTableModel(blinkenViewer);

		JTable table = new JTable(tableModel);
		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		table.setFillsViewportHeight(true);
		table.setDefaultRenderer(BlinkenViewer.class, new BlinkenViewerRenderer());
		table.setDefaultRenderer(Boolean.class, new DefaultTableCellRenderer());

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);

		// Fiddle with the Sport column's cell editors/renderers.
		setEnableBlinkenViewerColumn(table, table.getColumnModel().getColumn(1));

		// Add the scroll pane to this panel.
		add(scrollPane);
	}

	public void setEnableBlinkenViewerColumn( JTable table, TableColumn isactiveColumn )
	{
		// Set up the editor for the sport cells.
		JCheckBox checkBox = new JCheckBox();
		isactiveColumn.setCellEditor(new DefaultCellEditor(checkBox));

		// Set up tool tips for the sport cells.
		BlinkenViewerRenderer renderer = new BlinkenViewerRenderer();
		isactiveColumn.setCellRenderer(renderer);
	}

	public void tableDataChanged( )
	{
		// tableModel.fireTableDataChanged();
		tableModel.tableChanged();
	}

	// /**
	// * Create the GUI and show it. For thread safety, this method should be
	// * invoked from the event-dispatching thread.
	// */
	// private static void createAndShowGUI( )
	// {
	// // Create and set up the window.
	// JFrame frame = new JFrame("TableRenderDemo");
	// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	//
	// // Create and set up the content pane.
	// TableRenderDemo newContentPane = new TableRenderDemo();
	// newContentPane.setOpaque(true); // content panes must be opaque
	// frame.setContentPane(newContentPane);
	//
	// // Display the window.
	// frame.pack();
	// frame.setVisible(true);
	// }
	//
	// public static void main( String[] args )
	// {
	// // Schedule a job for the event-dispatching thread:
	// // creating and showing this application's GUI.
	// javax.swing.SwingUtilities.invokeLater(new Runnable()
	// {
	// public void run( )
	// {
	// createAndShowGUI();
	// }
	// });
	// }
	/*
	 * // * This method picks good column sizes. If all column heads are wider
	 * than // * the column's cells' contents, then you can just use // *
	 * column.sizeWidthToFit(). //
	 */
	// private void initColumnSizes( JTable table )
	// {
	// BlinkenViewerTableModel model = (BlinkenViewerTableModel)
	// table.getModel();
	// TableColumn column = null;
	// Component comp = null;
	// int headerWidth = 0;
	// int cellWidth = 0;
	// TableCellRenderer headerRenderer =
	// table.getTableHeader().getDefaultRenderer();
	//		
	// Object[] examplpeValues = new Object[]
	// {
	// new BlinkenViewer()
	// {
	// @Override
	// public void setBlinkenConfig( BlinkenConfig blinkenConfig ) {}
	// @Override
	// public void setActive( boolean active ) {}
	// @Override
	// public boolean isActive( ) {return false;}
	// },
	// Boolean.TRUE
	// };
	// for (int i = 0; i < 2; i++)
	// {
	// column = table.getColumnModel().getColumn(i);
	//
	// comp = headerRenderer.getTableCellRendererComponent(null,
	// column.getHeaderValue(), false, false, 0, 0);
	// headerWidth = comp.getPreferredSize().width;
	//
	// comp =
	// table.getDefaultRenderer(model.getColumnClass(i)).getTableCellRendererComponent(table,
	// examplpeValues[i], false, false, 0, i);
	// cellWidth = comp.getPreferredSize().width;
	//
	// column.setPreferredWidth( Math.max(headerWidth, cellWidth) );
	// }
	// }

}
