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

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;


/**
 *  A <code>TableModel</code> that can be resized on the fly, tracks whether
 *  a cell has been changed, and allows the table to add new rows via edits.
 *  <p>
 *  The "expanding" behavior is supported by reporting an extra row via
 *  {@link #getRowCount}. Calls to {@link #getValueAt} for this "phantom" row
 *  return <code>null</code>, while calls to {@link #setValueAt} make the row
 *  permanent and create a new phantom row.
 */
public class ExpandingTableModel
extends AbstractTableModel
{
    private static final long serialVersionUID = 1L;

    private int colCount = 0;
    private ArrayList<Object> headers = new ArrayList<Object>();
    private ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
    private ArrayList<ArrayList<Object>> data = new ArrayList<ArrayList<Object>>();


    /**
     *  Creates an instance that has no rows and no columns. Application
     *  must at least call {@link #setWidth}.
     */
    public ExpandingTableModel()
    {
        // nothing here
    }


    /**
     *  Creates an instance that is pre-sized to the specified number of
     *  rows and columns, with all cells containing <code>null</code>.
     */
    public ExpandingTableModel(int rows, int cols)
    {
        colCount = cols;
        for (int ii = 0 ; ii < rows ; ii++)
        {
            addRow();
        }
    }


    /**
     *  Creates an instance that is initialized with the passed data. The
     *  passed rows may be different length: the model will size itself
     *  to the largest and fill missing values with <code>null</code>.
     */
    public ExpandingTableModel(Object[][] data)
    {
        for (int ii = 0 ; ii < data.length ; ii++)
        {
            addRow(data[ii]);
        }
    }


    /**
     *  Creates an instance that is initialized with the passed data and column
     *  headers. The number of headers and number of columns need not match:
     *  excess headers will be retained for later use, insufficient headers are
     *  returned as "".
     *  <p>
     *  Note that headers are defined as arbitrary objects. If the object is
     *  mutable, the header value will reflect the string value of the header.
     */
    public ExpandingTableModel(Object[][] data, Object[] columnNames)
    {
        this(data);
        for (Object obj : columnNames)
        {
            headers.add(obj);
        }
    }


    /**
     *  Creates an instance that is initialized with the passed data, column
     *  headers, and column classes. The number of headers/classes and number
     *  of columns need not match: excess headers/classes will be retained
     *  for later use, insufficient headers/classes are returned as "" and
     *  <code>Object</code> respectively.
     *
     *  @throws IllegalArgumentException if a data element does not correspond
     *          to the class specification.
     */
    public ExpandingTableModel(Object[][] data, Object[] columnNames, Class<?>[] colClasses)
    {
        this(data, columnNames);
        for (Class<?> klass : colClasses)
        {
            classes.add(klass);
        }

        for (int row = 0 ; row < data.length ; row++)
        {
            for (int col = 0 ; col < data[row].length ; col++)
            {
                Object item = data[row][col];
                checkClass(item, getColumnClass(col), row, col);
            }
        }
    }

//----------------------------------------------------------------------------
//  TableModel
//----------------------------------------------------------------------------

    /**
     *  Returns the current number of columns in the model. This may be
     *  changed via {@link #setWidth}.
     */
    @Override
    public int getColumnCount()
    {
        return colCount;
    }


    /**
     *  Returns the number of rows in the model, including the "phantom" row.
     */
    @Override
    public int getRowCount()
    {
        return data.size() + 1;
    }


    /**
     *  Returns the name of the particular column, empty string if a header
     *  has not been defined for the column.
     *  <p>
     *  Note that headers may be specified as any type of object, but are
     *  returned as strings. This is no doubt an artifact of the original
     *  implementation using <code>Vector</code>, but allows the creation
     *  of dynamic headers using mutable objects.
     */
    @Override
    public String getColumnName(int col)
    {
        if (col >= colCount)
            throw new IndexOutOfBoundsException("attempted: " + col + ", model size: " + colCount);

        return (col < headers.size())
               ? String.valueOf(headers.get(col))
               : "";
    }


    /**
     *  Returns the class of the particular column, <code>Object</code> if
     *  no class has been specified for the column.
     */
    @Override
    public Class<?> getColumnClass(int col)
    {
        if (col >= colCount)
            throw new IndexOutOfBoundsException("attempted: " + col + ", model size: " + colCount);

        return (col < classes.size())
               ? classes.get(col)
               : Object.class;
    }


    /**
     *  Returns the value at a particular cell in the model. Cells in the
     *  "phantom" row always return <code>null</code>
     *
     *  @throws IndexOutOfBoundsException on attempts to get an invalid cell.
     */
    @Override
    public Object getValueAt(int row, int col)
    {
        return (row == data.size())
               ? null
               : data.get(row).get(col);
    }


    /**
     *  Stores the passed value in the specified cell. If this cell is in the
     *  "phantom" row, will make that row permanent (and increase the size of
     *  the table).
     *
     *  @throws IndexOutOfBoundsException on attempts to set an invalid cell.
     */
    @Override
    public void setValueAt(Object value, int row, int col)
    {
        checkClass(value, getColumnClass(col), row, col);
        if (row == data.size())
        {
            addRow();
            fireTableRowsInserted(row + 1, row + 1);
        }
        data.get(row).set(col, value);
        fireTableCellUpdated(row, col);
    }


    @Override
    public boolean isCellEditable(int row, int col)
    {
        return (row <= data.size()) && (col < colCount);
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Sets the width (column count) of the model. Does nothing if called
     *  with the current width. Will remove elements from the end of each
     *  row if the specified width is less than the current width, insert
     *  <code>null</code>s on each row if greater.
     *  <p>
     *  Header names are not removed when the width is reduced, and will
     *  re-appear if the table is subsequently widened.
     */
    public void setWidth(int width)
    {
        if (width == colCount)
            return;

        colCount = width;
        for (int ii = 0 ; ii < data.size() ; ii++)
        {
            resizeRow(ii);
        }

        fireTableStructureChanged();
    }


    /**
     *  Updates the name for a column with the passed value. Names may be
     *  set for columns that are not yet shown; they will become active
     *  when the table is widened.
     */
    public void setColumnName(int col, Object name)
    {
        while (headers.size() <= col)
        {
            headers.add("");
        }
        headers.set(col, name);

        if (col < colCount)
            fireTableStructureChanged();
    }


    /**
     *  Updates the name of a column with the passed value. Classes may be
     *  set for columns that are not yet shown; they will become active
     *  when the table is widened.
     */
    public void setColumnClass(int col, Class<?> klass)
    {
        if (col < colCount)
        {
            for (int row = 0 ; row < data.size() ; row++)
            {
                checkClass(getValueAt(row, col), klass, row, col);
            }
        }

        while (classes.size() <= col)
        {
            classes.add(Object.class);
        }
        classes.set(col, klass);

        if (col < colCount)
            fireTableStructureChanged();
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    /**
     *  Adds an empty row with the current number of columns.
     */
    private ArrayList<Object> addRow()
    {
        ArrayList<Object> row = new ArrayList<Object>(colCount);
        for (int ii = 0 ; ii < colCount ; ii++)
        {
            row.add(null);
        }
        data.add(row);
        return row;
    }


    /**
     *  Adds an row with the current number of columns, filled with
     *  the specified data. Will resize the model if the passed data
     *  is too wide.
     */
    private ArrayList<Object> addRow(Object[] rowData)
    {
        setWidth(Math.max(rowData.length, colCount));
        ArrayList<Object> row = addRow();
        for (int ii = 0 ; ii < rowData.length ; ii++)
        {
            row.set(ii, rowData[ii]);
        }
        return row;
    }


    /**
     *  Resizes the specified row, either adding nulls to the end or removing
     *  elements from the end, until it matches current column count.
     */
    private ArrayList<Object> resizeRow(int ii)
    {
        ArrayList<Object> row = data.get(ii);
        while (row.size() > colCount)
        {
            row.remove(row.size() - 1);
        }
        while (row.size() < colCount)
        {
            row.add(null);
        }
        return row;
    }


    /**
     *  Verifies that the value is an instance of the specified class, and
     *  throws if not (row and col are provided just for the exception).
     */
    private void checkClass(Object value, Class<?> klass, int row, int col)
    {
        if ((value != null) && !klass.isInstance(value))
        {
            throw new IllegalArgumentException(
                    "value at " + row + "," + col + " is "
                    + value.getClass().getName()
                    + ", expected " + klass.getName());
        }
    }
}
