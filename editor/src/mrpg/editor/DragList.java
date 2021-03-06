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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.TransferHandler;

public class DragList extends DragSource implements DragSourceListener, DragGestureListener, KeyListener {
	private static final long serialVersionUID = -7697869179730226540L;
	private JList list; private Class<?> type; private static Object clipboard; private Listener listener;
	public DragList(JList l, Class<?> t){this(l,t,null);}
	public DragList(JList l, Class<?> t, Listener ln){
		super(); list = l; type = t; listener = ln; list.setDragEnabled(true); list.setDropMode(DropMode.INSERT);
		list.setTransferHandler(new MyTransferHandler()); list.addKeyListener(this);
		createDefaultDragGestureRecognizer(list, DnDConstants.ACTION_COPY_OR_MOVE, this);
	}
	public static final DataFlavor flavor = new DataFlavor(ObjectTransferable.class, "");
	private static class ObjectTransferable implements Transferable {
		public final JList list; public final int index;
		public ObjectTransferable(JList l){list = l; index = list.getSelectedIndex();}
		public Object getObject(){return list.getModel().getElementAt(index);}
		public void remove(){((DefaultListModel)list.getModel()).removeElementAt(index);}
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {return this;}
		public DataFlavor[] getTransferDataFlavors(){return new DataFlavor[]{flavor};}
		public boolean isDataFlavorSupported(DataFlavor f){return f == flavor;}
		
	}
	public void dragGestureRecognized(DragGestureEvent dge) {
		ObjectTransferable transferable = new ObjectTransferable(list);
		if(dge.getDragAction() == DnDConstants.ACTION_COPY)
			startDrag(dge, DragSource.DefaultCopyDrop, transferable, this);
		else startDrag(dge, DragSource.DefaultMoveDrop, transferable, this);
	}
	public void dragEnter(DragSourceDragEvent dsde){
		try{
			DragSourceContext c = dsde.getDragSourceContext();
			if(((ObjectTransferable)c.getTransferable().getTransferData(flavor)).list == list){
				c.setCursor((dsde.getDropAction() == DnDConstants.ACTION_COPY)?DragSource.DefaultCopyDrop:DragSource.DefaultMoveDrop);
			} else c.setCursor(DragSource.DefaultMoveNoDrop);
		}catch(Exception e){}
	}
	public void dragExit(DragSourceEvent dse){
		dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
	}
	public void dragOver(DragSourceDragEvent dsde){}
	public void dragDropEnd(DragSourceDropEvent dsde){}
	public void dropActionChanged(DragSourceDragEvent dsde){
		if(dsde.getDragSourceContext().getCursor() != DragSource.DefaultMoveNoDrop)
			dsde.getDragSourceContext().setCursor(
				(dsde.getDropAction() == DnDConstants.ACTION_COPY)?DragSource.DefaultMoveDrop:DragSource.DefaultCopyDrop);
	}
	
	public void keyPressed(KeyEvent e){
		Object o = list.getSelectedValue();
		if(e.isControlDown()){
			switch(e.getKeyCode()){
			case 'c': case 'C': if(o != null) clipboard = o; break;
			case 'v': case 'V':
				if(clipboard != null && clipboard.getClass() == type){
					Object paste = clipboard; if(listener == null || (paste=listener.paste(clipboard))!=null){
						((DefaultListModel)list.getModel()).addElement(paste); if(listener != null) listener.updateDrag();
					}
				} break;
			case 'x': case 'X':
				if(o != null){clipboard = o; ((DefaultListModel)list.getModel()).removeElementAt(list.getSelectedIndex()); if(listener != null) listener.updateDrag();}
				break;
			}
		}
	}
	public void keyReleased(KeyEvent e){}
	public void keyTyped(KeyEvent e){}
	
	private class MyTransferHandler extends TransferHandler {
		private static final long serialVersionUID = 4697250886587273644L;
		public boolean canImport(TransferHandler.TransferSupport support){
			try{
				return ((JList.DropLocation)support.getDropLocation()).getIndex() != -1 &&
					((ObjectTransferable)support.getTransferable().getTransferData(flavor)).getObject().getClass() == type;
			}catch(Exception e){return false;}
		}
		public boolean importData(TransferHandler.TransferSupport support){
			if(!canImport(support)) return false;
			try{
				ObjectTransferable transferable = (ObjectTransferable)support.getTransferable().getTransferData(flavor);
				int dropIndex = ((JList.DropLocation)support.getDropLocation()).getIndex();
				DefaultListModel m = (DefaultListModel)list.getModel();
				if(list == transferable.list){
					if(transferable.index == dropIndex) return false;
					Object o; if(support.getDropAction() == DnDConstants.ACTION_COPY) o = transferable.getObject();
					else {o = m.remove(transferable.index); if(transferable.index < dropIndex) dropIndex--;}
					m.add(dropIndex, o); list.setSelectedIndex(dropIndex);
				} else {
					Object o = transferable.getObject();
					if(support.getDropAction() != DnDConstants.ACTION_COPY) transferable.remove();
					m.add(dropIndex, o); list.setSelectedIndex(dropIndex);
				} if(listener != null) listener.updateDrag(); return true;
			}catch(Exception e){return false;}
		  }
		}
	public static interface Listener {
		public void updateDrag();
		//Called when user wants to paste clipboard. Returns the object to paste (or clipboard if you can paste an exact copy of clipboard), or null if this action is not possible.
		public Object paste(Object clipboard);
	}
}
