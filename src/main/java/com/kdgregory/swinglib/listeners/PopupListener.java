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

package com.kdgregory.swinglib.listeners;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;


/**
 *  A mouse listener that will attach a popup menu to a given component.
 */
public class PopupListener
extends MouseAdapter
{
    /**
     *  Creates a new instance and attaches it to the component.
     */
    public static void attach(JComponent comp, JPopupMenu menu)
    {
        comp.addMouseListener(new PopupListener(comp, menu));
    }


//----------------------------------------------------------------------------
//  Instance Data and Constructors
//----------------------------------------------------------------------------

    private JPopupMenu menu;

    public PopupListener(JComponent comp, JPopupMenu menu)
    {
        this.menu = menu;
    }


//----------------------------------------------------------------------------
//  MouseListener
//----------------------------------------------------------------------------

    @Override
    public void mousePressed(MouseEvent evt)
    {
        commonHandler(evt);
    }

    @Override
    public void mouseReleased(MouseEvent evt)
    {
        commonHandler(evt);
    }

//----------------------------------------------------------------------------
//  Internals
//----------------------------------------------------------------------------

    private void commonHandler(MouseEvent evt)
    {
        if (evt.isPopupTrigger())
        {
            menu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
}
