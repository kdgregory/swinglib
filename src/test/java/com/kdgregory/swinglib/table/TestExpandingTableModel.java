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
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import junit.framework.TestCase;


public class TestExpandingTableModel extends TestCase
{
//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    private void assertSize(TableModel model, int rows, int cols)
    {
        assertEquals(rows, model.getRowCount());
        assertEquals(cols, model.getColumnCount());
    }


    /**
     *  Asserts that the table contains values corresponding to the passed
     *  array, and that all other cells contain <code>null</code>.
     */
    private void assertContent(TableModel model, Object[][] data)
    {
        for (int row = 0 ; row < data.length ; row++)
        {
            for (int col = 0 ; col < data[row].length ; col++)
            {
                assertEquals("incorrect data at " + row + ", " + col,
                             data[row][col], model.getValueAt(row, col));
            }
        }

        for (int row = 0 ; row < model.getRowCount() ; row++)
        {
            int col = (row < data.length) ? data[row].length : 0;
            for ( ; col < model.getColumnCount() ; col++)
            {
                assertNull(model.getValueAt(row, col));
            }
        }
    }


    /**
     *  Asserts that the table advertises N column headers (no more, no less),
     *  and that the values are as specified (filling with empty strings for
     *  non-specified values).
     */
    private void assertHeaders(TableModel model, int count, String... values)
    {
        for (int ii = 0 ; ii < count ; ii++)
        {
            if (ii < values.length)
            {
                assertEquals("header " + ii, values[ii], model.getColumnName(ii));
            }
            else
            {
                assertEquals("header " + ii, "", model.getColumnName(ii));
            }
        }

        try
        {
            model.getColumnName(count);
            fail("retrieved header for column " + count);
        }
        catch (IndexOutOfBoundsException ee)
        {
            // success
        }
    }


    /**
     *  Asserts that the table advertises N column classes (no more, no less),
     *  and that the values are as specified (filling with Object.class for
     *  non-specified values).
     */
    private void assertClasses(TableModel model, int count, Class<?>... values)
    {
        for (int ii = 0 ; ii < count ; ii++)
        {
            if (ii < values.length)
            {
                assertEquals("header " + ii, values[ii], model.getColumnClass(ii));
            }
            else
            {
                assertEquals("header " + ii, Object.class, model.getColumnClass(ii));
            }
        }

        try
        {
            model.getColumnClass(count);
            fail("retrieved class for column " + count);
        }
        catch (IndexOutOfBoundsException ee)
        {
            // success
        }
    }


    /**
     *  A listener that tracks notifications, and provides assertions on the
     *  events that took place.
     */
    private static class TestListener
    implements TableModelListener
    {
        public List<TableModelEvent> events = new ArrayList<TableModelEvent>();

        @Override
        public void tableChanged(TableModelEvent e)
        {
            events.add(e);
        }

        /**
         *  Clears outstanding events; useful when a test does more than one change.
         */
        public void clear()
        {
            events.clear();
        }

        /**
         *  Asserts that a particular event was fired N times.
         */
        public void assertEventCount(int eventType, int expected)
        {
            int actual = 0;
            for (TableModelEvent event : events)
            {
                if (event.getType() == eventType)
                    actual++;
            }
            assertEquals("event type " + eventType, expected, actual);
        }

        /**
         *  Asserts that a particular event was fired for a given row and column.
         */
        public void assertEvent(int eventType, int row, int col)
        {
            int actual = 0;
            for (TableModelEvent event : events)
            {
                if ((event.getType() == eventType)
                        && (event.getFirstRow() == row)
                        && (event.getColumn() == col))
                    actual++;
            }
            assertEquals("event type " + eventType + "@[" + row + "," + col + "]", 1, actual);
        }


        /**
         *  Asserts that a particular event was fired for a given row.
         */
        public void assertEvent(int eventType, int row)
        {
            int actual = 0;
            for (TableModelEvent event : events)
            {
                if ((event.getType() == eventType)
                        && (event.getFirstRow() == row))
                    actual++;
            }
            assertEquals("event type " + eventType + "@[" + row +  "]", 1, actual);
        }
    }


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    public void testEmptyConstructor() throws Exception
    {
        ExpandingTableModel model = new ExpandingTableModel();
        assertSize(model, 1, 0);
        assertHeaders(model, 0);
        assertClasses(model, 0);
    }


    public void testPresizeConstructor() throws Exception
    {
        ExpandingTableModel model = new ExpandingTableModel(10, 5);
        assertSize(model, 11, 5);
        assertHeaders(model, 5);
        assertClasses(model, 5);
        assertContent(model, new Object[0][0]);
    }


    public void testPresizeConstructorWithZeroRows() throws Exception
    {
        ExpandingTableModel model = new ExpandingTableModel(0, 5);
        assertSize(model, 1, 5);
        assertHeaders(model, 5);
        assertClasses(model, 5);
        assertContent(model, new Object[0][0]);
    }


    public void testInitializingConstructor() throws Exception
    {
        Object[][] data = new Object[][] {
                new Object[] {"foo", "bar"},
                new Object[] {"argle", "bargle"},
                new Object[] {"asdf", "qwerty"}
        };

        ExpandingTableModel model = new ExpandingTableModel(data);
        assertSize(model, 4, 2);
        assertHeaders(model, 2);
        assertClasses(model, 2);
        assertContent(model, data);
    }


    public void testInitializingConstructorWithVariableWidth() throws Exception
    {
        Object[][] data = new Object[][] {
                new Object[] {"foo"},
                new Object[] {},
                new Object[] {"argle", "bargle", "wargle"},
                new Object[] {"asdf", "qwerty"}
        };

        ExpandingTableModel model = new ExpandingTableModel(data);
        assertSize(model, 5, 3);
        assertHeaders(model, 3);
        assertClasses(model, 3);
        assertContent(model, data);
    }


    public void testInitializingConstructorWithHeaders() throws Exception
    {
        Object[][] data = new Object[][] {
                new Object[] {"foo"},
                new Object[] {"argle", "bargle", "wargle"}
        };
        Object[] headers = new Object[] { "foo", Integer.valueOf(1) };

        ExpandingTableModel model = new ExpandingTableModel(data, headers);
        assertSize(model, 3, 3);
        assertHeaders(model, 3, "foo", "1", "");
        assertClasses(model, 3);
        assertContent(model, data);
    }


    public void testInitializingConstructorWithClasses() throws Exception
    {
        Object[][] data = new Object[][] {
                new Object[] {"foo"},
                new Object[] {"argle", "bargle", Integer.valueOf(1)}
        };
        Object[] headers = new Object[0];
        Class<?>[] classes = new Class<?>[] { String.class, Object.class, Integer.class };

        ExpandingTableModel model = new ExpandingTableModel(data, headers, classes);
        assertSize(model, 3, 3);
        assertHeaders(model, 3);
        assertClasses(model, 3, String.class, Object.class, Integer.class);
        assertContent(model, data);

        assertEquals(String.class, model.getColumnClass(0));
        assertEquals(Object.class, model.getColumnClass(1));
        assertEquals(Integer.class, model.getColumnClass(2));
    }


    public void testInitializingConstructorWithInvalidClasses() throws Exception
    {
        Object[][] data = new Object[][] {
                new Object[] {"foo"},
                new Object[] {"argle", "bargle", "fargle"}
        };
        Object[] headers = new Object[0];
        Class<?>[] classes = new Class<?>[] { String.class, Object.class, Integer.class };

        try
        {
            new ExpandingTableModel(data, headers, classes);
            fail("created model with invalid value per class spec");
        }
        catch (IllegalArgumentException ee)
        {
            // success
        }
    }


    public void testIsCellEditable() throws Exception
    {
        ExpandingTableModel model = new ExpandingTableModel(2, 2);
        assertTrue(model.isCellEditable(0, 0));
        assertTrue(model.isCellEditable(1, 1));
        assertTrue(model.isCellEditable(2, 1));

        assertFalse(model.isCellEditable(3, 1));
        assertFalse(model.isCellEditable(0, 2));
    }


    public void testSetAndGet() throws Exception
    {
        ExpandingTableModel model = new ExpandingTableModel(2, 2);
        assertSize(model, 3, 2);

        TestListener lsnr = new TestListener();
        model.addTableModelListener(lsnr);

        model.setValueAt("foo", 1, 1);
        assertSize(model, 3, 2);
        assertEquals("foo", model.getValueAt(1, 1));

        lsnr.assertEventCount(TableModelEvent.UPDATE, 1);
        lsnr.assertEvent(TableModelEvent.UPDATE, 1, 1);
    }


    public void testSetPhantomCell() throws Exception
    {
        ExpandingTableModel model = new ExpandingTableModel(2, 2);
        assertSize(model, 3, 2);

        TestListener lsnr = new TestListener();
        model.addTableModelListener(lsnr);

        model.setValueAt("foo", 2, 1);
        assertSize(model, 4, 2);
        assertEquals("foo", model.getValueAt(2, 1));

        lsnr.assertEventCount(TableModelEvent.INSERT, 1);
        lsnr.assertEvent(TableModelEvent.INSERT, 3);
        lsnr.assertEventCount(TableModelEvent.UPDATE, 1);
        lsnr.assertEvent(TableModelEvent.UPDATE, 2, 1);
    }


    public void testSetValueInvalidClass() throws Exception
    {
        ExpandingTableModel model = new ExpandingTableModel(2, 2);
        model.setColumnClass(0, Integer.class);

        try
        {
            model.setValueAt("foo", 0, 0);
            fail("created model with invalid value per class spec");
        }
        catch (IllegalArgumentException ee)
        {
            // success
        }
    }


    public void testSetWidth() throws Exception
    {
        Object[][] data = new Object[][]
                          {
                              new Object[] { "A", "B", "C", "D", "E" },
                              new Object[] { "F", "G", "H", "I" }
                          };
        Object[] names = new Object[] { "foo", "bar", "baz", "bop", "boop" };

        ExpandingTableModel model = new ExpandingTableModel(data, names);
        assertSize(model, 3, 5);

        TestListener lsnr = new TestListener();
        model.addTableModelListener(lsnr);

        model.setWidth(4);
        assertSize(model, 3, 4);
        lsnr.assertEventCount(TableModelEvent.UPDATE, 1);
        lsnr.assertEvent(TableModelEvent.UPDATE, TableModelEvent.HEADER_ROW);

        lsnr.clear();

        model.setWidth(5);
        assertSize(model, 3, 5);
        assertNull(model.getValueAt(0, 4));
        assertEquals("boop", model.getColumnName(4));
        lsnr.assertEventCount(TableModelEvent.UPDATE, 1);
        lsnr.assertEvent(TableModelEvent.UPDATE, TableModelEvent.HEADER_ROW);
    }


    public void testSetColumnName() throws Exception
    {
        ExpandingTableModel model = new ExpandingTableModel(1, 4);
        assertSize(model, 2, 4);
        assertEquals("", model.getColumnName(2));

        TestListener lsnr = new TestListener();
        model.addTableModelListener(lsnr);

        model.setColumnName(2, "foo");
        assertEquals("foo", model.getColumnName(2));
        lsnr.assertEventCount(TableModelEvent.UPDATE, 1);
        lsnr.assertEvent(TableModelEvent.UPDATE, TableModelEvent.HEADER_ROW);

        lsnr.clear();
        model.setColumnName(7, "bar");
        lsnr.assertEventCount(TableModelEvent.UPDATE, 0);

        model.setWidth(8);
        assertEquals("bar", model.getColumnName(7));
    }


    public void testSetColumnClass() throws Exception
    {
        ExpandingTableModel model = new ExpandingTableModel(1, 4);
        assertSize(model, 2, 4);
        assertEquals(Object.class, model.getColumnClass(2));

        TestListener lsnr = new TestListener();
        model.addTableModelListener(lsnr);

        model.setColumnClass(2, String.class);
        assertEquals(String.class, model.getColumnClass(2));
        lsnr.assertEventCount(TableModelEvent.UPDATE, 1);
        lsnr.assertEvent(TableModelEvent.UPDATE, TableModelEvent.HEADER_ROW);

        lsnr.clear();
        model.setColumnClass(7, Number.class);
        lsnr.assertEventCount(TableModelEvent.UPDATE, 0);

        model.setWidth(8);
        assertEquals(Number.class, model.getColumnClass(7));
    }


    public void testSetColumnClassWithInvalidData() throws Exception
    {
        Object[][] data = new Object[][] { new Object[] { "foo", "bar" } };
        ExpandingTableModel model = new ExpandingTableModel(data);
        assertEquals(Object.class, model.getColumnClass(0));

        try
        {
            model.setColumnClass(0, Integer.class);
            fail("able to set class with invalid data in place");
        }
        catch (IllegalArgumentException e)
        {
            // success
        }
    }
}
