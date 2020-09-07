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

package net.sf.swinglib.field;

import java.awt.Color;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.PlainDocument;

import net.sf.swinglib.field.FieldValidator;

import junit.framework.TestCase;


public class TestFieldValidator
extends TestCase
{
    // these objects are only updated on the event thread, but are member
    // variables so that they persist between Runnables
    private JTextField _theField;
    private FieldValidator _validator;

    // these variables are used to exchange state between event and test threads
    private volatile boolean _isValid;
    private volatile Color _currentColor;


//----------------------------------------------------------------------------
//  Test cases
//----------------------------------------------------------------------------

    public void testReadOnlyValidator() throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _theField = new JTextField("blah");
                _validator = new FieldValidator(_theField, "bla*h");
                _isValid = _validator.isValid();
            }
        });
        assertTrue(_isValid);

        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _theField.setText("argle");
                _isValid = _validator.isValid();
            }
        });
        assertFalse(_isValid);

        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _theField.setText("blaaaaaaaah");
                _isValid = _validator.isValid();
            }
        });
        assertTrue(_isValid);
    }


    public void testHighlighting() throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _theField = new JTextField("blah");
                _validator = new FieldValidator(_theField, "bla*h", Color.red, Color.blue);
                _isValid = _validator.isValid();
                _currentColor = _theField.getBackground();
            }
        });
        assertTrue(_isValid);
        assertEquals(Color.blue, _currentColor);

        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _theField.setText("argle");
                _isValid = _validator.isValid();
                _currentColor = _theField.getBackground();
            }
        });
        assertFalse(_isValid);
        assertEquals(Color.red, _currentColor);

        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _theField.setText("blaaaaaaaah");
                _isValid = _validator.isValid();
                _currentColor = _theField.getBackground();
            }
        });
        assertTrue(_isValid);
        assertEquals(Color.blue, _currentColor);
    }


    public void testReset() throws Exception
    {
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _theField = new JTextField("blah");
                _validator = new FieldValidator(_theField, "bla*h");
                _isValid = _validator.isValid();
            }
        });
        assertTrue(_isValid);

        // update the document without updating the watcher
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _theField.setDocument(new PlainDocument());
                _theField.setText("argle");
                _isValid = _validator.isValid();
            }
        });
        assertTrue(_isValid);

        // now reset the watcher, which should change the status
        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                _validator.reset();
                _isValid = _validator.isValid();
            }
        });
        assertFalse(_isValid);
    }
}
