/*******************************************************************************
 * Rhythos Editor is a game editor and project management tool for making RPGs on top of the Rhythos Game system.
 * 
 * Copyright (C) 2013  David Maletz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mrpg.editor;

import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class Matrix extends JTable {
	private static final long serialVersionUID = 8625651000718900158L;

	public Matrix(int rows, int cols){
		super(new Model(rows, cols));
		setDefaultRenderer(Double.class, new DoubleRenderer());
	}
	public double[] getMatrix(){return ((Model)getModel()).matrix;}
	public void setMatrix(double[] m){
		removeEditor(); Model model = (Model)getModel();
		System.arraycopy(m, 0, model.matrix, 0, model.matrix.length); model.fireTableDataChanged();
	}
	
	private static class Model extends AbstractTableModel {
		private static final long serialVersionUID = 1052790183520079382L;
		private double[] matrix; private int cols;
		public Model(int rows, int cols){this.cols = cols; matrix = new double[rows*cols];}
		public int getColumnCount(){return cols;}
		public int getRowCount(){return matrix.length/cols;}
		public Class<?> getColumnClass(int col){return Double.class;}
		public boolean isCellEditable(int row, int col){return true;}
		public Object getValueAt(int row, int col){
			return matrix[row*cols+col];
		}
		public void setValueAt(Object val, int row, int col){
			matrix[row*cols+col] = (Double)val; fireTableCellUpdated(row, col);
		}
	}
	
	private static class DoubleRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = -8583617765741239107L;
		private static final DecimalFormat format = new DecimalFormat("#.###");
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
	    	super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	    	setText(format.format((Double)value)); return this;
	    }
	}
}
