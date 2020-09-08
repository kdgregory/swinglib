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
import java.util.regex.Pattern;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;


/**
 *  Applies a regular expression to a text field to check its validity. Provides
 *  an optional listener that will change the field's background color if invalid.
 *  <p>
 *  Note: the validator records the component's document at time of construction.
 *  If you change the document, you must call {@link #reset}.
 */
public class FieldValidator
{
    private JTextComponent component;
    private Document document;
    private Pattern regex;
    private Color normalColor;
    private Color highlightColor;


    /**
     *  Constructs a "listen-only" instance; you must explicitly call
     *  {@link #isValid} to check the field's state.
     *
     *  @param  component   The component to listen to; the new instance is
     *                      attached as a <code>DocumentListener</code>.
     *  @param  regex       A regular expression defining the valid contents
     *                      of the field. This will be applied on any change
     *                      to the underlying document.
     */
    public FieldValidator(JTextComponent component, String regex)
    {
        this.component = component;
        this.document = component.getDocument();
        this.regex = Pattern.compile(regex);
    }


    /**
     *  Constructs a an instance that will change the field's background
     *  color when its contents are invalid, and revert to its parent's
     *  color when the contents are valid.
     *  <p>
     *  Note: the first check is performed by this constructor.
     *
     *  @param  component   The component to listen to; the new instance is
     *                      attached as a <code>DocumentListener</code>.
     *  @param  regex       A regular expression defining the valid contents
     *                      of the field. This will be applied on any change
     *                      to the underlying document.
     *  @param  highlight   When the field is invalid, it will be set to this
     *                      color.
     */
    public FieldValidator(JTextComponent component, String regex, Color highlight)
    {
        this(component, regex, highlight, null);
    }


    /**
     *  Constructs a an instance that will change the field's background
     *  color to either a "normal" or a "highlight" color depending on
     *  the field's contents.
     *  <p>
     *  Note: the first check is performed by this constructor. There is
     *        no need to make a separate <code>setBackground()</code> call
     *        on the component itself.
     *
     *  @param  component   The component to listen to; the new instance is
     *                      attached as a <code>DocumentListener</code>.
     *  @param  regex       A regular expression defining the valid contents
     *                      of the field. This will be applied on any change
     *                      to the underlying document.
     *  @param  highlight   When the field is invalid, it will be set to this
     *                      color.
     *  @param  normal      When the field is valid, it will be set to this color.
     */
    public FieldValidator(JTextComponent component, String regex,
                          Color highlight, Color normal)
    {
        this(component, regex);
        this.highlightColor = highlight;
        this.normalColor = normal;
        this.document.addDocumentListener(new MyDocumentListener());
        validateAndHighlight();
    }


//----------------------------------------------------------------------------
//  Public Methods
//----------------------------------------------------------------------------

    /**
     *  Determines whether the document is currently valid (note: this may be
     *  called within an event handler, as long as the update has been applied
     *  to the document).
     */
    public boolean isValid()
    {
        int len = document.getLength();
        try
        {
            String text = document.getText(0, len);
            return regex.matcher(text).matches();
        }
        catch (BadLocationException e)
        {
            // this should never happen unless the document implementation is
            // bad ... in which case we can call the field invalid forever
            return false;
        }
    }


    /**
     *  Call this method if you change the document on the component.
     */
    public void reset()
    {
        // removing our reference to the p;d document should allow it to be
        // collected; we don't care that it still holds a reference to us
        // via the listener

        document = component.getDocument();
        if (highlightColor != null)
            document.addDocumentListener(new MyDocumentListener());
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    // this is the method that does the listener's work, and is also called
    // from the constructor
    private void validateAndHighlight()
    {
        if (isValid())
            component.setBackground(normalColor);
        else
            component.setBackground(highlightColor);
    }


    // by creating an inner class, we keep our public interface clean
    private class MyDocumentListener
    implements DocumentListener
    {
        @Override
        public void changedUpdate(DocumentEvent e)
        {
            validateAndHighlight();
        }

        @Override
        public void insertUpdate(DocumentEvent e)
        {
            validateAndHighlight();
        }

        @Override
        public void removeUpdate(DocumentEvent e)
        {
            validateAndHighlight();
        }
    }
}
