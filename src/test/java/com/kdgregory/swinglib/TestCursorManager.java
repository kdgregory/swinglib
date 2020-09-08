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

package com.kdgregory.swinglib;

import java.awt.Cursor;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import junit.framework.TestCase;


/**
 *  This testcase creates a frame with multiple components, and uses instance
 *  variables to pass cursor values from the event thread to the test thread.
 *  It will pack and show the test frame; I'm not sure whether the test is
 *  valid without doing so.
 */
public class TestCursorManager
extends TestCase
{
    // the object under test
    private CursorManager _manager = new CursorManager();

    // these variables are filled by setUp()
    private JFrame theFrame;
    private JLabel comp1;          // this will use default cursor
    private JTextField comp2;      // this will use text cursor

    // these are also filled by setUp(), as a convenience for assertions
    private Cursor defaultCursor;
    private Cursor crosshairCursor;
    private Cursor textCursor;
    private Cursor waitCursor;

    // these variables are set during test execution
    private Cursor comp1Cursor;
    private Cursor comp2Cursor;
    private Exception exception;


    @Override
    protected void setUp() throws Exception
    {
        // these are separate operations because I have a habit of modularization

        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                defaultCursor = Cursor.getDefaultCursor();
                crosshairCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
                textCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
                waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            }
        });

        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                comp1 = new JLabel("Nothing to see here");
                comp2 = new JTextField(20);

                JPanel content = new JPanel();
                content.add(comp1);
                content.add(comp2);

                theFrame = new JFrame(this.getClass().getName());
                theFrame.setContentPane(content);
                theFrame.pack();
                theFrame.setVisible(true);
            }
        });
    }


    @Override
    protected void tearDown() throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                theFrame.dispose();
            }
        });
    }


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    public void testSinglePushAndPopWithDefault() throws Exception
    {
        // first ensure the default state
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                comp1Cursor = comp1.getCursor();
                comp2Cursor = comp2.getCursor();
            }
        });
        assertEquals(defaultCursor, comp1Cursor);
        assertEquals(textCursor, comp2Cursor);

        // now change the label, and ensure that we didn't touch the textfield
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                _manager.pushCursor(comp1, waitCursor);
                comp1Cursor = comp1.getCursor();
                comp2Cursor = comp2.getCursor();
            }
        });
        assertEquals(waitCursor, comp1Cursor);
        assertEquals(textCursor, comp2Cursor);

        // now pop it off and ensure that we return to default
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                _manager.popCursor(comp1);
                comp1Cursor = comp1.getCursor();
                comp2Cursor = comp2.getCursor();
            }
        });
        assertEquals(defaultCursor, comp1Cursor);
        assertEquals(textCursor, comp2Cursor);
    }


    // this is a "make sure we didn't do anything dumb" test
    public void testSinglePushAndPopWithComponentDefault() throws Exception
    {
        // first ensure the default state
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                comp2Cursor = comp2.getCursor();
            }
        });
        assertEquals(textCursor, comp2Cursor);

        // now change it
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                _manager.pushCursor(comp2, waitCursor);
                comp2Cursor = comp2.getCursor();
            }
        });
        assertEquals(waitCursor, comp2Cursor);

        // and back
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                _manager.popCursor(comp2);
                comp2Cursor = comp2.getCursor();
            }
        });
        assertEquals(textCursor, comp2Cursor);
    }


    // another "make sure we didn't do anything dumb" test
    public void testMultiplePopsIgnored() throws Exception
    {
        // first ensure the default state
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                comp2Cursor = comp2.getCursor();
            }
        });
        assertEquals(textCursor, comp2Cursor);

        // now change it
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                _manager.pushCursor(comp2, waitCursor);
                comp2Cursor = comp2.getCursor();
            }
        });
        assertEquals(waitCursor, comp2Cursor);

        // and back
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                _manager.popCursor(comp2);
                comp2Cursor = comp2.getCursor();
            }
        });
        assertEquals(textCursor, comp2Cursor);

        // and try another pop
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    _manager.popCursor(comp2);
                }
                catch (Exception ex)
                {
                    exception = ex;
                }
                comp2Cursor = comp2.getCursor();
            }
        });
        assertEquals(textCursor, comp2Cursor);
        assertNull(exception);
    }


    // this is here for coverage -- pushBusyCursor() delegates to pushCursor()
    public void testSinglePushAndPopBusyCursor() throws Exception
    {
        // first ensure the default state
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                comp2Cursor = comp2.getCursor();
            }
        });
        assertEquals(textCursor, comp2Cursor);

        // now change it
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                _manager.pushBusyCursor(comp2);
                comp2Cursor = comp2.getCursor();
            }
        });
        assertEquals(waitCursor, comp2Cursor);

        // and back
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                _manager.popCursor(comp2);
                comp2Cursor = comp2.getCursor();
            }
        });
        assertEquals(textCursor, comp2Cursor);
    }


    public void testMultiplePushAndPop() throws Exception
    {
        // first ensure the default state
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                comp2Cursor = comp2.getCursor();
            }
        });
        assertEquals(textCursor, comp2Cursor);

        // now change it
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                _manager.pushBusyCursor(comp2);
                comp2Cursor = comp2.getCursor();
            }
        });
        assertEquals(waitCursor, comp2Cursor);

        // and again
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                _manager.pushCursor(comp2, crosshairCursor);
                comp2Cursor = comp2.getCursor();
            }
        });
        assertEquals(crosshairCursor, comp2Cursor);

        // back to the first push
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                _manager.popCursor(comp2);
                comp2Cursor = comp2.getCursor();
            }
        });
        assertEquals(waitCursor, comp2Cursor);

        // and back to the original
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                _manager.popCursor(comp2);
                comp2Cursor = comp2.getCursor();
            }
        });
        assertEquals(textCursor, comp2Cursor);
    }
}
