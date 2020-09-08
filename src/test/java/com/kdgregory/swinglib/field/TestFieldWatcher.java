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

package com.kdgregory.swinglib.field;

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
    private JFrame theFrame;
    private JTextField fText;
    private JCheckBox fButton;
    private JList<String> fList;

    // this button is used to validate the response
    private JButton theButton;

    // this variable is used to exchange state between event and test threads
    // it must be manually updated with the state of the button
    private volatile boolean buttonState;

    // this list will be filled by calling FieldWatcher.getChangedComponents()
    private Collection<JComponent> changes;

    // the watcher is created by setUp(), must be attached manually
    private FieldWatcher watcher;


    @Override
    protected void setUp()
    throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                fText = new JTextField();
                fButton = new JCheckBox();
                fList = new JList<String>(new String[] {"foo", "bar", "baz"});
                theButton = new JButton("don't press");

                // initial values for these fields
                fText.setText(TEXT_INITIAL);
                fButton.setSelected(BUTTON_INITIAL);
                fList.setSelectedIndex(LIST_INITIAL_IDX);

                // we could test without creating an actual containment
                // hierarchy, but feel it's best to keep the code close
                // to real-world
                JPanel content = new JPanel();
                content.add(fText);
                content.add(fButton);
                content.add(fList);
                content.add(theButton);

                theFrame = new JFrame(this.getClass().getName());
                theFrame.setContentPane(content);
                theFrame.pack();
                // there's no need to actually display the frame, however

                watcher = new FieldWatcher(theButton);
                theButton.setEnabled(false);
            }
        });
    }


    @Override
    protected void tearDown()
    throws Exception
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
//  Support Code
//----------------------------------------------------------------------------

    private void recordState()
    {
        buttonState = theButton.isEnabled();
        changes = watcher.getChangedComponents();
    }


    private void assertState(JComponent... expectedChanges)
    {
        if (expectedChanges.length > 0)
            assertTrue(buttonState);

        assertEquals(expectedChanges.length, changes.size());
        for (JComponent comp : expectedChanges)
            assertTrue(changes.contains(comp));
    }


//----------------------------------------------------------------------------
//  Test Cases
//----------------------------------------------------------------------------

    public void testTextUpdates() throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                watcher.addWatchedField(fText);
                recordState();
            }
        });
        assertState();

        // first check that we track the change
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                fText.setText(TEXT_UPDATED);
                recordState();
            }
        });
        assertState(fText);

        // then that we track the return to initial value
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                fText.setText(TEXT_INITIAL);
                recordState();
            }
        });
        assertState();
    }


    public void testButtonUpdates() throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                watcher.addWatchedField(fButton);
                recordState();
            }
        });
        assertState();

        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                fButton.setSelected(BUTTON_UPDATED);
                recordState();
            }
        });
        assertState(fButton);

        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                fButton.setSelected(BUTTON_INITIAL);
                recordState();
            }
        });
        assertState();
    }


    public void testListUpdates() throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                watcher.addWatchedField(fList);
                buttonState = theButton.isEnabled();
                recordState();
            }
        });
        assertState();

        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                fList.setSelectedIndex(LIST_UPDATED_IDX);
                recordState();
            }
        });
        assertState(fList);

        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                fList.setSelectedIndex(LIST_INITIAL_IDX);
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
            @Override
            public void run()
            {
                watcher.addWatchedField(fText)
                        .addWatchedField(fButton)
                        .addWatchedField(fList);
                recordState();
            }
        });
        assertState();

        // update from initial values, everything should be flagged
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                fText.setText(TEXT_UPDATED);
                fButton.setSelected(BUTTON_UPDATED);
                fList.setSelectedIndex(LIST_UPDATED_IDX);
                recordState();
            }
        });
        assertState(fText, fButton, fList);

        // reset means nothing will be flagged
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                watcher.reset();
                recordState();
            }
        });
        assertState();

        // changing back to (original) initial values should now set the flag
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                fText.setText(TEXT_INITIAL);
                fButton.setSelected(BUTTON_INITIAL);
                fList.setSelectedIndex(LIST_INITIAL_IDX);
                recordState();
            }
        });
        assertState(fText, fButton, fList);

        // and changing back to the reset values should turn it off
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                fText.setText(TEXT_UPDATED);
                fButton.setSelected(BUTTON_UPDATED);
                fList.setSelectedIndex(LIST_UPDATED_IDX);
                recordState();
            }
        });
        assertState();
    }


    public void testValidatedField() throws Exception
    {
        // setup: the field won't appear, even if it has valid initial state
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                fText.setText("ab");
                FieldValidator validator = new FieldValidator(fText, "a*b+");
                watcher.addValidatedField(fText, validator);
                recordState();
            }
        });
        assertState();

        // verify that it appears when we set valid content
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                fText.setText("b");
                recordState();
            }
        });
        assertState(fText);

        // ... disappears when we set invalid text
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                fText.setText("a");
                recordState();
            }
        });
        assertState();

        // ... and appears again when valid
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                fText.setText("ab");
                recordState();
            }
        });
        assertState();
    }
}
