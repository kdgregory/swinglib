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

package net.sf.swinglib.listeners;

import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import junit.framework.TestCase;


public class TestFieldWatcher
extends TestCase
{
    // values for the fields
    private final static String TEXT_INITIAL = "foo";
    private final static String TEXT_UPDATED = "bar";
    private final static boolean BUTTON_INITIAL = false;
    private final static boolean BUTTON_UPDATED = true;
    private final static int LIST_INITIAL_IDX = 0;
    private final static int LIST_UPDATED_IDX = 1;


//----------------------------------------------------------------------------
//  Test data / Setup / Teardown
//----------------------------------------------------------------------------

    // the fields that we will test
    private JFrame _theFrame;
    private JTextField _fText;
    private JCheckBox _fButton;
    private JList _fList;

    // this button is used to validate the response
    private JButton _theButton;

    // this variable is used to exchange state between event and test threads
    // it must be manually updated with the state of the button
    private volatile boolean _buttonState;

    // this list will be filled by calling FieldWatcher.getChangedComponents()
    private Collection<JComponent> _changes;

    // the watcher is created by setUp(), must be attached manually
    private FieldWatcher _watcher;


    @Override
    protected void setUp()
    throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _fText = new JTextField();
                _fButton = new JCheckBox();
                _fList = new JList(new String[] {"foo", "bar", "baz"});
                _theButton = new JButton("don't press");

                // initial values for these fields
                _fText.setText(TEXT_INITIAL);
                _fButton.setSelected(BUTTON_INITIAL);
                _fList.setSelectedIndex(LIST_INITIAL_IDX);

                // we could test without creating an actual containment
                // hierarchy, but feel it's best to keep the code close
                // to real-world
                JPanel content = new JPanel();
                content.add(_fText);
                content.add(_fButton);
                content.add(_fList);
                content.add(_theButton);

                _theFrame = new JFrame(this.getClass().getName());
                _theFrame.setContentPane(content);
                _theFrame.pack();
                // there's no need to actually display the frame, however

                _watcher = new FieldWatcher(_theButton);
                _theButton.setEnabled(false);
            }
        });
    }


    @Override
    protected void tearDown()
    throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _theFrame.dispose();
            }
        });
    }


//----------------------------------------------------------------------------
//  Support Code
//----------------------------------------------------------------------------

    private void recordState()
    {
        _buttonState = _theButton.isEnabled();
        _changes = _watcher.getChangedComponents();
    }


    private void assertState(JComponent... changes)
    {
        if (changes.length > 0)
            assertTrue(_buttonState);

        assertEquals(changes.length, _changes.size());
        for (JComponent comp : changes)
            assertTrue(_changes.contains(comp));
    }


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    public void testTextUpdates() throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _watcher.addWatchedField(_fText);
                recordState();
            }
        });
        assertState();

        // first check that we track the change
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _fText.setText(TEXT_UPDATED);
                recordState();
            }
        });
        assertState(_fText);

        // then that we track the return to initial value
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _fText.setText(TEXT_INITIAL);
                recordState();
            }
        });
        assertState();
    }


    public void testButtonUpdates() throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _watcher.addWatchedField(_fButton);
                recordState();
            }
        });
        assertState();

        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _fButton.setSelected(BUTTON_UPDATED);
                recordState();
            }
        });
        assertState(_fButton);

        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _fButton.setSelected(BUTTON_INITIAL);
                recordState();
            }
        });
        assertState();
    }


    public void testListUpdates() throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _watcher.addWatchedField(_fList);
                _buttonState = _theButton.isEnabled();
                recordState();
            }
        });
        assertState();

        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _fList.setSelectedIndex(LIST_UPDATED_IDX);
                recordState();
            }
        });
        assertState(_fList);

        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _fList.setSelectedIndex(LIST_INITIAL_IDX);
                recordState();
            }
        });
        assertState();
    }


    public void testReset() throws Exception
    {
        // this first step is setup, but I'll assert anyway
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _watcher.addWatchedField(_fText)
                        .addWatchedField(_fButton)
                        .addWatchedField(_fList);
                recordState();
            }
        });
        assertState();

        // update from initial values, everything should be flagged
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _fText.setText(TEXT_UPDATED);
                _fButton.setSelected(BUTTON_UPDATED);
                _fList.setSelectedIndex(LIST_UPDATED_IDX);
                recordState();
            }
        });
        assertState(_fText, _fButton, _fList);

        // reset means nothing will be flagged
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _watcher.reset();
                recordState();
            }
        });
        assertState();

        // changing back to (original) initial values should now set the flag
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _fText.setText(TEXT_INITIAL);
                _fButton.setSelected(BUTTON_INITIAL);
                _fList.setSelectedIndex(LIST_INITIAL_IDX);
                recordState();
            }
        });
        assertState(_fText, _fButton, _fList);

        // and changing back to the reset values should turn it off
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _fText.setText(TEXT_UPDATED);
                _fButton.setSelected(BUTTON_UPDATED);
                _fList.setSelectedIndex(LIST_UPDATED_IDX);
                recordState();
            }
        });
        assertState();
    }
}
