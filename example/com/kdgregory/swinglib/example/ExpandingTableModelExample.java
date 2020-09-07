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

package com.kdgregory.swinglib.example;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import com.kdgregory.swinglib.table.ExpandingTableModel;


/**
 *  An example of {@link net.sf.swinglib.table.ExpandingTableModel} that
 *  builds a simple two-column editable table with some initial data.
 */
public class ExpandingTableModelExample
{
    public static void main(String[] argv)
    throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                JFrame frame = new JFrame("CompactGridExample");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setContentPane(buildContent());
                frame.pack();
                frame.setVisible(true);
            }
        });
    }


    private static JComponent buildContent()
    {
        TableModel model = new ExpandingTableModel(
                                    new Object[][] {
                                        new Object[] { 123, "abc" },
                                        new Object[] { 456, "def" },
                                        new Object[] { 789, "ghi" }
                                        },
                                    new String[] { "IntValue", "StrValue" },
                                    new Class<?>[] { Integer.class, String.class });
        JTable table = new JTable(model);
        return new JScrollPane(table);
    }
}
