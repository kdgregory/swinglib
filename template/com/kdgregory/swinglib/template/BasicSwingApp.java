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

package com.kdgregory.swinglib.template;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;


/**
 *  A boilerplate Swing application, consisting of a main frame with menu bar
 *  and content area. To use, copy into a new file (unless you like the name),
 *  and implement {@link #buildContent} and {@link #buildMenuBar}.
 */
public class BasicSwingApp
{
    public static void main(String[] argv)
    throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                new BasicSwingApp().buildFrame().setVisible(true);
            }
        });
    }


//----------------------------------------------------------------------------
//  Main Frame and Content Pane
//----------------------------------------------------------------------------

    private JFrame buildFrame()
    {
        JFrame frame = new JFrame("Boilerplate Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(buildMenuBar());
        frame.setContentPane(buildContent());
        frame.pack();
        return frame;
    }


    private JComponent buildContent()
    {
        JLabel label = new JLabel("put something here");
        label.setPreferredSize(new Dimension(300, 100));
        return label;
    }


//----------------------------------------------------------------------------
//  Main Menu
//----------------------------------------------------------------------------

    private JMenuBar buildMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(buildFileMenu());
        return menuBar;
    }


    private JMenu buildFileMenu()
    {
        JMenu menu = new JMenu("File");

        JMenuItem mQuit = new JMenuItem("Quit");
        mQuit.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        });
        menu.add(mQuit);

        return menu;
    }
}
