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

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.kdgregory.swinglib.layout.CompactGridLayout;


/**
 *  An example of {@link net.sf.swinglib.layout.CompactGridLayout} that
 *  builds a two-column table with a mixture of labels and entry fields.
 */
public class CompactGridExample
{
    public static void main(String[] argv)
    throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
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


    private static JPanel buildContent()
    {
        JPanel panel = new JPanel(new CompactGridLayout(2, 12, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        panel.add(new JLabel("this is a long label with a short field"));
        panel.add(new JTextField(2));

        panel.add(new JLabel("the reverse"));
        panel.add(new JTextField(20));

        panel.add(new JLabel("another field"));
        panel.add(new JTextField("and text"));

        return panel;
    }
}
