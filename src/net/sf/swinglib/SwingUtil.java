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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;


/**
 *  A collection of static methods that don't really fit anywhere else.
 */
public class SwingUtil
{
    /**
     *  Centers the passed window (dialog or frame) and makes it visible.
     *  <p>
     *  Uses the default Toolkit to get the current display size. Deals with
     *  multiple monitors by assuming that a single monitor has a width:height
     *  ration of less than 2:1; if the toolkit reports a total screen size
     *  larger than this, will divide the width by 2. This works well for 1,
     *  2, or 3 monitors (it appears in the left monitor of 2, center of 3).
     *  If you're running on a bigger configuration, you're on your own.
     *  <p>
     *  If the window is larger than the screen size, it's positioned at the
     *  top-left corner. Hopefully the user will be able to shrink it.
     *  <p>
     *  Remember to call <code>pack()</code> before calling this method!
     */
    public static void centerAndShow(Window window)
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width;
        int y = screenSize.height;
        if (x > y * 2)
            x /= 2;

        Dimension windowSize = window.getSize();
        x -= windowSize.width;
        y -= windowSize.height;

        if (x < 0)
            x = 0;
        if (y < 0)
            y = 0;

        window.setLocation(x/2, y/2);
        window.setVisible(true);
    }
}
