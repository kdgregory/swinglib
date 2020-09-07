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

import java.awt.Color;
import java.awt.Component;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;


/**
 *  Static methods and constants that help build UI objects with a
 *  consistent appearance.
 */
public class UIHelper
{
//----------------------------------------------------------------------------
//  Constants
//----------------------------------------------------------------------------

    /**
     *  An "inter-component" horizontal space, also used as the standard
     *  inset from a frame to its content. This is the nominal "Em" space.
     */
    public final static int STANDARD_SPACE = 12;


    /**
     *  The highlight color for invalid fields.
     */
    public final static Color HIGHLIGHT_COLOR = new Color(255, 240, 240);


//----------------------------------------------------------------------------
//  Factories for standard spacing objects
//----------------------------------------------------------------------------

    /**
     *  The border to be used around a dialog's content.
     */
    public static Border dialogBorder()
    {
        return BorderFactory.createEmptyBorder(
                STANDARD_SPACE, STANDARD_SPACE,
                STANDARD_SPACE, STANDARD_SPACE);
    }


    /**
     *  The border to be used around a group of components in a dialog.
     *  Assumes that there will be a label above the group, and that there
     *  won't be decoration between groups.
     */
    public static Border dialogGroupBorder()
    {
        return BorderFactory.createEmptyBorder(
                STANDARD_SPACE / 2,
                STANDARD_SPACE,
                STANDARD_SPACE * 3 / 2,
                0);
    }


    /**
     *  A horizontal strut between buttons on the same line.
     */
    public static Component interButtonSpace()
    {
        return Box.createHorizontalStrut(STANDARD_SPACE);
    }


//----------------------------------------------------------------------------
//  Factories for consistent GUI objects
//----------------------------------------------------------------------------

    /**
     *  Builds a standard modal input dialog, with content and buttons to
     *  accept or cancel that content.
     */
    public static JDialog newModalDialog(
            JFrame owner, String title,
            JPanel content, JButton... buttons)
    {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setAlignmentX(0.0f);
        buttonPanel.add(Box.createHorizontalGlue());
        for (int ii = 0 ; ii < buttons.length ; ii++)
        {
            if (ii > 0)
                buttonPanel.add(interButtonSpace());
            buttonPanel.add(buttons[ii]);
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(dialogBorder());
        panel.add(content);
        panel.add(Box.createVerticalStrut(18));
        panel.add(buttonPanel);

        JDialog theDialog = new JDialog(owner, title, true);
        theDialog.setContentPane(panel);
        theDialog.pack();
        return theDialog;
    }


    /**
     *  Builds a standard modal input dialog, with content and buttons to
     *  accept or cancel that content.
     */
    public static JDialog newModalDialog(
            JFrame owner, String title,
            JPanel content, Action... actions)
    {
        JButton[] buttons = new JButton[actions.length];
        for (int ii = 0 ; ii < actions.length ; ii++)
            buttons[ii] = new JButton(actions[ii]);
        return newModalDialog(owner, title, content, buttons);
    }
}
