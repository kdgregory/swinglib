// Copyright Keith D Gregory
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.kdgregory.swinglib.table;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;


/**
 *  A collection of static utility methods for working with tables.
 */
public class TableUtil
{
    /**
     *  Sets the preferred widths of the columns of a table to the specified
     *  percentages of the current width. The caller may provide more or
     *  fewer values than actual columns in the table: excess values are
     *  ignored, excess columns will divide up width not assigned by the
     *  provided values (which may make them tiny). If the provided widths
     *  total more than 100%, the table will be expanded.
     */
    public static void setRelativeColumnWidths(JTable table, int... widths)
    {
        int tableWidth = table.getWidth();
        int usedWidth = 0;
        TableColumnModel model = table.getColumnModel();
        for (int idx = 0 ; idx < Math.min(widths.length, model.getColumnCount()) ;
             idx++)
        {
            TableColumn col = model.getColumn(idx);
            int colWidth = tableWidth * widths[idx] / 100;
            col.setPreferredWidth(colWidth);
            usedWidth += colWidth;
        }

        int leftover = model.getColumnCount() - widths.length;
        if (leftover <= 0)
            return;

        int colWidth = (tableWidth - usedWidth) / leftover;
        for (int idx = widths.length ; idx < model.getColumnCount() ; idx++)
        {
            TableColumn col = model.getColumn(idx);
            col.setPreferredWidth(colWidth);
        }
    }
}
