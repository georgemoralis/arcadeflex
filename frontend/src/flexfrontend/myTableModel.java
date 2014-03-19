/*
This file is part of Arcadeflex.

Arcadeflex is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Arcadeflex is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Arcadeflex.  If not, see <http://www.gnu.org/licenses/>.
 */
package flexfrontend;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author tsol
 */
public class myTableModel extends DefaultTableModel {

    String columns[] = {"Name","Description"};
    boolean editable;

    public myTableModel(boolean editable) {
        super();
        for (int i = 0; i < columns.length; i++) {
            addColumn(columns[i]);
        }
        this.editable = editable;
    }

    public void setEditable(boolean t) {
        editable = t;
    }

    public void update(String entryname, String name) {
        for (int i = 0; i < getRowCount(); i++) {
            String wholename="^"+getValueAt(i, 0)+"_"+getValueAt(i, 1);
            if (wholename.equals(entryname)) {
                this.setValueAt(name, i, 3);
            }
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int mColIndex) {
        return editable;
    }
}
