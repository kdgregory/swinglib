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

package com.kdgregory.swinglib.example;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.kdgregory.swinglib.components.ProgressMonitor;


/**
 *  Uses {@link net.sf.swinglib.components.ProgressMonitor} to display
 *  a dialog and update it from the main thread. Note that this program
 *  does not create a panel.
 */
public class ProgressMonitorExample
{
    public static void main(String[] argv)
    throws Exception
    {
        // this Action will be attached to the dialog to allow user to cancel
        // (it happens to exit the program)
        Action cancelAction = new AbstractAction("Cancel")
        {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        };

        // note that we create the dialog without an owning frame; normally you
        // would associate it with some other frame in your application
        ProgressMonitor monitor
                = new ProgressMonitor(
                        null,
                        "Counter",
                        "Watch me count. You can cancel if you want.",
                        cancelAction,
                        ProgressMonitor.Options.CENTER,
                        ProgressMonitor.Options.SHOW_STATUS,
                        ProgressMonitor.Options.SHOW_PERCENT_COMPLETE)
                  .show();

        // because we didn't set an initial progress value, the dialog will
        // appear in indeterminate ("Cylon") mode; let it do that for a while
        monitor.setStatus("(nothing to count yet)");
        Thread.sleep(2000L);

        // now count to 10 ... remember, we're on the main thread here
        for (int ii = 1 ; ii <= 20 ; ii++)
        {
            monitor.setProgress(0, ii, 20);
            monitor.setStatus("current count: " + ii);
            Thread.sleep(500L);
        }

        // we're done, but since we started the event-dispatch thread we have to
        // explicitly call exit()
        System.exit(0);
    }
}
