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

package net.sf.swinglib;

import java.awt.Cursor;
import java.util.LinkedList;
import java.util.WeakHashMap;

import javax.swing.JComponent;


/**
 *  This class manages a a stack of cursors for one or more components. It's
 *  normally used to manage busy cursors: at the start of an operation you
 *  call {@link #pushCursor} (or more likely, {@link #pushBusyCursor}, at the
 *  end you {@link #popCursor}.
 *  <p>
 *  Usage note: if you want to change the cursor on a frame or dialog, use the
 *  root pane (frames and dialogs aren't themselves <code>JComponent</code>s).
 *
 */
public class CursorManager
{
    private WeakHashMap<JComponent,LinkedList<Cursor>> _cursors
            = new WeakHashMap<JComponent,LinkedList<Cursor>>();


//----------------------------------------------------------------------------
//  Public Methods
//----------------------------------------------------------------------------

    /**
     *  Pushes the current cursor for the specified component onto the stack,
     *  and sets the specified cursor.
     */
    public void pushCursor(JComponent comp, Cursor newCursor)
    {
        LinkedList<Cursor> stack = getCursorStackFor(comp);
        stack.add(comp.getCursor());
        comp.setCursor(newCursor);
    }


    /**
     *  Pushes the current cursor for the specified component onto the stack,
     *  and sets the standard "busy" cursor.
     */
    public void pushBusyCursor(JComponent comp)
    {
        pushCursor(comp, Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }


    /**
     *  Pops the last saved cursor for the specified component, replacing
     *  whatever it's currently displaying. If there aren't any saved cursors,
     *  does nothing.
     */
    public void popCursor(JComponent comp)
    {
        LinkedList<Cursor> stack = getCursorStackFor(comp);
        if (stack.size() == 0)
            return;

        comp.setCursor(stack.removeLast());
    }


//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private LinkedList<Cursor> getCursorStackFor(JComponent comp)
    {
        LinkedList<Cursor> stack = _cursors.get(comp);
        if (stack == null)
        {
            stack = new LinkedList<Cursor>();
            _cursors.put(comp, stack);
        }
        return stack;
    }
}
