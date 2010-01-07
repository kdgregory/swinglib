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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;


/**
 *  Tracks input field changes. This is normally used by dialogs, to automatically
 *  enable/disable an "OK" button based on whether the user has made changes.
 *  <p>
 *  To use, create a single instance for the dialog, and attach all fields of
 *  interest. When the dialog is displayed, call {@link #reset}. You can either
 *  manually interrogate the listener for changes, or have it automatically
 *  enable/disable one or more buttons.
 *  <p>
 *  An alternate usage is to create a watcher on a single field (normally a
 *  checkbox), and use it to enable/disable a group of related fields. For
 *  example, add a checkbox to enter a billing address, then enable/disable
 *  the address fields depending on whether the box is checked.
 *  <p>
 *  At present, this watcher knows how to track the following field types:
 *  <dl>
 *  <dt> text fields (any subclass of <code>JTextComponent</code>
 *  <dd> will look for changes to the underlying document;
 *  <dt> stateful buttons (any subclass of <code>JToggleButton</code>)
 *  <dd> will look for changes in the button's state
 *  <dt> <code>JList</code>
 *  <dd> will look for changes in the list's selection; does not consider
 *       changes to the underlying list model
 *  </dl>
 *  In all cases, the watcher will track the initial value of the component,
 *  and recognize when its current state returns to that initial value. The
 *  initial value will be updated whenever {@link #reset} is called.
 */
public class FieldWatcher
{
    private List<AbstractWatcher<?>> _watchers
            = new ArrayList<AbstractWatcher<?>>();
    private Map<JComponent,FieldValidator> _validators
            = new IdentityHashMap<JComponent,FieldValidator>();
    private List<JComponent> _controlledComponents
            = new ArrayList<JComponent>();
    private List<Action> _controlledActions
            = new ArrayList<Action>();
    private Map<JComponent,AbstractWatcher<?>> _changed
            = new IdentityHashMap<JComponent,AbstractWatcher<?>>();


    /**
     *  Creates an instance with zero or more controlled components. These will
     *  be disabled when {@link #reset} is called, enabled when a watched field
     *  changes.
     */
    public FieldWatcher(JComponent... controlled)
    {
        _controlledComponents.addAll(Arrays.asList(controlled));
    }

    /**
     *  Creates an instance with zero or more controlled actions. These will
     *  be disabled when {@link #reset} is called, enabled when a watched field
     *  changes.
     */
    public FieldWatcher(Action... controlled)
    {
        _controlledActions.addAll(Arrays.asList(controlled));
    }


    /**
     *  Adds a controlled component after construction. This is useful if you
     *  want to mix components and actions.
     */
    public FieldWatcher addControlled(JComponent controlled)
    {
        _controlledComponents.add(controlled);
        return this;
    }


    /**
     *  Adds a controlled action after construction. This is useful if you
     *  want to mix components and actions.
     */
    public FieldWatcher addControlled(Action controlled)
    {
        _controlledActions.add(controlled);
        return this;
    }


    /**
     *  Adds a field to the watch list. Will attach a component-appropriate
     *  listener to watch for changes (note that there is no way to remove
     *  this listener).
     *
     *  @return The watcher, allowing multiple components to be added using
     *          chained calls.
     */
    public FieldWatcher addWatchedField(JComponent theField)
    {
        if (theField instanceof JTextComponent)
        {
            _watchers.add(new TextWatcher((JTextComponent)theField));
        }
        else if (theField instanceof JToggleButton)
        {
            _watchers.add(new ToggleWatcher((JToggleButton)theField));
        }
        else if (theField instanceof JList)
        {
            _watchers.add(new ListWatcher((JList)theField));
        }
        else
        {
            throw new IllegalArgumentException(
                    "does not support " + theField.getClass().getName());
        }
        return this;
    }


    /**
     *  Adds a validated field to the watch list. Changes to the field will
     *  only be recorded if the validator claims that the field is valid.
     */
    public FieldWatcher addValidatedField(JTextComponent theField, FieldValidator validator)
    {
        _validators.put(theField, validator);
        return addWatchedField(theField);
    }


    /**
     *  Returns the components that have changed since construction or the
     *  last call to {@link #reset}.
     */
    public Collection<JComponent> getChangedComponents()
    {
        return new ArrayList<JComponent>(_changed.keySet());
    }


    /**
     *  Resets this watcher: clears the list of changed fields, and disables
     *  any controlled components.
     */
    public void reset()
    {
        _changed.clear();
        updateControlled();
        for (AbstractWatcher<?> watcher : _watchers)
            watcher.reset();
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    /**
     *  Enables/disables controlled components based on whether there are
     *  any changes. This should be called whenever the {@link #_changed}
     *  set is modified.
     */
    private void updateControlled()
    {
        boolean enable = (_changed.size() > 0);
        for (JComponent comp : _controlledComponents)
            comp.setEnabled(enable);
        for (Action action : _controlledActions)
            action.setEnabled(enable);
    }


//----------------------------------------------------------------------------
//  Listeners
//----------------------------------------------------------------------------

    private abstract class AbstractWatcher<T extends JComponent>
    {
        private T _component;

        protected AbstractWatcher(T component)
        {
            _component = component;
        }

        protected T getComponent()
        {
            return _component;
        }

        protected void markChanged(boolean hasChanged)
        {
            if (hasChanged)
                _changed.put(_component, this);
            else
                _changed.remove(_component);
            updateControlled();
        }

        public abstract void reset();
    }


    private class TextWatcher
    extends AbstractWatcher<JTextComponent>
    implements DocumentListener
    {
        private int _initialLength;
        private String _initialValue;

        public TextWatcher(JTextComponent theField)
        {
            super(theField);
            theField.getDocument().addDocumentListener(this);
            reset();
        }

        @Override
        public void reset()
        {
            Document doc = getComponent().getDocument();
            _initialLength = doc.getLength();
            _initialValue = getDocumentText(doc);
        }

        public void changedUpdate(DocumentEvent evt)
        {
            commonHandler(evt);
        }

        public void insertUpdate(DocumentEvent evt)
        {
            commonHandler(evt);
        }

        public void removeUpdate(DocumentEvent evt)
        {
            commonHandler(evt);
        }

        private void commonHandler(DocumentEvent evt)
        {
            Document doc = getComponent().getDocument();

            // initial length check is quick, if unchanged we need to check
            // actual content
            boolean hasChanged = _initialLength != doc.getLength();
            if (!hasChanged)
                hasChanged = !_initialValue.equals(getDocumentText(doc));

            // if there's a validator, make sure we're now valid
            FieldValidator validator = _validators.get(getComponent());
            if (hasChanged && (validator != null))
                hasChanged = validator.isValid();

            markChanged(hasChanged);
        }

        // a helper method to retrieve document text, with fallback for the
        // will-never-happen checked exception; called by ctor and event
        private String getDocumentText(Document doc)
        {
            try
            {
                return doc.getText(0, doc.getLength());
            }
            catch (BadLocationException e)
            {
                return "";
            }
        }
    }


    private class ToggleWatcher
    extends AbstractWatcher<JToggleButton>
    implements ChangeListener
    {
        private boolean _initialState;

        public ToggleWatcher(JToggleButton theButton)
        {
            super(theButton);
            theButton.addChangeListener(this);
            reset();
        }

        @Override
        public void reset()
        {
            _initialState = getComponent().isSelected();
        }

        public void stateChanged(ChangeEvent evt)
        {
            boolean currentState = getComponent().isSelected();
            markChanged(currentState != _initialState);
        }
    }


    private class ListWatcher
    extends AbstractWatcher<JList>
    implements ListSelectionListener
    {
        private int[] _initialSelections;

        public ListWatcher(JList theList)
        {
            super(theList);
            theList.addListSelectionListener(this);
            reset();
        }

        @Override
        public void reset()
        {
            _initialSelections = getComponent().getSelectedIndices();
        }

        public void valueChanged(ListSelectionEvent evt)
        {
            int[] currentSelections = getComponent().getSelectedIndices();
            boolean hasChanged = !Arrays.equals(currentSelections, _initialSelections);
            markChanged(hasChanged);
        }
    }
}
