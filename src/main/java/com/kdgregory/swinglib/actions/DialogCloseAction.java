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

package com.kdgregory.swinglib.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;


/**
 *  When invoked, this action hides or disposes the associated dialog.
 *  <p>
 *  It's primarily used for implementations where the dialog is managed by
 *  a controller class that wraps that dialog's fields. Such dialogs do not
 *  need to take specific actions in response to the "OK" or "Cancel"
 *  button, because the controller handles that.
 *  <p>
 *  When to dispose versus hide: dispose dialogs that are infrequently used;
 *  this will free system resources. However, when you dispose a dialog you
 *  need to <code>pack()</code> it before displaying it again.
 */
public class DialogCloseAction
extends AbstractAction
{
    private static final long serialVersionUID = 1L;

    private JDialog _dialog;
    private boolean _dispose;


    /**
     *  Constructs an action that will hide the dialog.
     */
    public DialogCloseAction(JDialog dialog, String name)
    {
        this(dialog, name, false);
    }


    /**
     *  Constructs an action that will either hide or dispose the dialog.
     */
    public DialogCloseAction(JDialog dialog, String name, boolean dispose)
    {
        super(name);
        _dialog = dialog;
        _dispose = dispose;
    }


    /**
     *  Sets the dialog associated with this action. This allows you to create
     *  the dialog with {@link com.kdgregory.swinglib.UIHelper#newModalDialog} and
     *  its ilk, which requires you to pass in a list of actions.
     */
    public void setDialog(JDialog dialog)
    {
        _dialog = dialog;
    }


    public void actionPerformed(ActionEvent ignored)
    {
        if (_dispose)
            _dialog.dispose();
        else
            _dialog.setVisible(false);
    }
}
