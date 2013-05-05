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
package mrpg.editor.property;

import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class StringProperty implements Property {
	private final JPanel panel; private final JTextField field = new JTextField(20);
	private Method set_method, get_method; private Field update_field;
	public StringProperty(){panel = new JPanel(); panel.add(field);}
	public StringProperty(String name){this(); panel.setBorder(BorderFactory.createTitledBorder(name));}
	public void setUpdateMethod(Method set, Method get){set_method = set; get_method = get; update_field = null;}
	public void setUpdateField(Field f){set_method = null; get_method = null; update_field = f;}
	public void setAction(String command, ActionListener listener){field.setActionCommand(command); field.addActionListener(listener);}
	public void focus(){field.requestFocus(); field.selectAll();}
	public JPanel getPanel(){return panel;}
	public void setValue(Object obj){
		try{
		if(get_method != null) field.setText(get_method.invoke(obj).toString());
		else if(update_field != null) field.setText(update_field.get(obj).toString());
		} catch(Exception e){}
	}
	public void updateValue(Object obj){
		try{
		if(set_method != null) set_method.invoke(obj, field.getText());
		else if(update_field != null) update_field.set(obj, field.getText());
		} catch(Exception e){}
	}
}
