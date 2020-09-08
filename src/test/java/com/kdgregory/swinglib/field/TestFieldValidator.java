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

import java.awt.Color;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.PlainDocument;

import junit.framework.TestCase;


public class TestFieldValidator
extends TestCase
{
    // these objects are only updated on the event thread, but are member
    // variables so that they persist between Runnables
    private JTextField theField;
    private FieldValidator validator;

    // these variables are used to exchange state between event and test threads
    private volatile boolean isValid;
    private volatile Color currentColor;


//----------------------------------------------------------------------------
//  Test cases
//----------------------------------------------------------------------------

    public void testReadOnlyValidator() throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                theField = new JTextField("blah");
                validator = new FieldValidator(theField, "bla*h");
                isValid = validator.isValid();
            }
        });
        assertTrue(isValid);

        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                theField.setText("argle");
                isValid = validator.isValid();
            }
        });
        assertFalse(isValid);

        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                theField.setText("blaaaaaaaah");
                isValid = validator.isValid();
            }
        });
        assertTrue(isValid);
    }


    public void testHighlighting() throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                theField = new JTextField("blah");
                validator = new FieldValidator(theField, "bla*h", Color.red, Color.blue);
                isValid = validator.isValid();
                currentColor = theField.getBackground();
            }
        });
        assertTrue(isValid);
        assertEquals(Color.blue, currentColor);

        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                theField.setText("argle");
                isValid = validator.isValid();
                currentColor = theField.getBackground();
            }
        });
        assertFalse(isValid);
        assertEquals(Color.red, currentColor);

        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                theField.setText("blaaaaaaaah");
                isValid = validator.isValid();
                currentColor = theField.getBackground();
            }
        });
        assertTrue(isValid);
        assertEquals(Color.blue, currentColor);
    }


    public void testReset() throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                theField = new JTextField("blah");
                validator = new FieldValidator(theField, "bla*h");
                isValid = validator.isValid();
            }
        });
        assertTrue(isValid);

        // update the document without updating the watcher
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                theField.setDocument(new PlainDocument());
                theField.setText("argle");
                isValid = validator.isValid();
            }
        });
        assertTrue(isValid);

        // now reset the watcher, which should change the status
        SwingUtilities.invokeAndWait(new Runnable()
        {
            @Override
            public void run()
            {
                validator.reset();
                isValid = validator.isValid();
            }
        });
        assertFalse(isValid);
    }
}
