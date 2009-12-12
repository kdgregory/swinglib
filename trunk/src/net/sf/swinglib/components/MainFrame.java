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

package net.sf.swinglib.components;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;


/**
 *  A <code>JFrame</code> that adds some commonly-used features:
 *  <ul>
 *  <li> By default, clicking the close box will exit the application (an
 *       alternate constructor allows an explicit listener).
 *  <li> Iconifying the frame will initiate garbage collection. This will
 *       compact the active pages in the heap, which should reduce paging
 *       when the application is de-iconified.
 *  </ul>
 */
public class MainFrame
extends JFrame
{
    private static final long serialVersionUID = 1L;


    /**
     *  The basic frame.
     */
    public MainFrame(String title)
    {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowIconified(WindowEvent e)
            {
                System.gc();
            }
        });
    }


    /**
     *  A frame that has an explicit window listener, and does nothing itself
     *  the user clicks the "close" button.
     */
    public MainFrame(String title, WindowListener lsnr)
    {
        this(title);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(lsnr);
    }
}
