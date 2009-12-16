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

package net.sf.swinglib.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.EnumSet;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import net.sf.swinglib.SwingUtil;


/**
 *  Creates and manages a standardized progress dialog. Any of the public
 *  methods on this class (including the constructor) may be executed from
 *  any thread; the actual operations are queued for the event thread.
 *  <p>
 *  The dialog is displayed by calling {@link #show}, hidden by calling
 *  {@link #hide}. It is not actually created until the first call to
 *  <code>show()</code>; the constructor merely records information. Nor
 *  is it disposed by calling {@link #hide}; you must explicitly call
 *  {@link #dispose}.
 */
public class ProgressDialogController
{
    /**
     *  Options to control the dialog's appearance. Rather than providing a
     *  plethora of constructors (like typical Swing dialogs), you can pass
     *  zero or more of these options to the base constructor.
     */
    public enum Options
    {
        /**
         *  If present, the dialog is modal.
         */
        MODAL,

        /**
         *  If present, the dialog will be displayed centered on its parent.
         *  If the dialog was constructed without a parent, it will be
         *  centered on the screen.
         */
        CENTER,

        /**
         *  If present, the progress bar displays completion percentage
         */
        SHOW_PERCENT_COMPLETE
    }


//----------------------------------------------------------------------------
//  Instance Data and Constructors
//----------------------------------------------------------------------------

    private JFrame _owner;
    private String _title;
    private String _text;
    private Action _action;
    private EnumSet<Options> _options = EnumSet.noneOf(Options.class);

    private JDialog _theDialog;
    private JProgressBar _progressBar;

    private volatile Integer _min;
    private volatile Integer _max;
    private volatile Integer _cur;



    /**
     *  Base constructor, allowing customization of the dialog's appearance
     *  and behavior. Note that the actual dialog is constructed on the first
     *  call to {@link #show}.
     *
     *  @param owner    The dialog owner; may be <code>null</code>, in which
     *                  case Swing will generate a hidden owner frame.
     *  @param title    Text to display in the dialog window's title area; may
     *                  be <code>null</code>, in which case the title is left
     *                  empty.
     *  @param text     Text to display in the body of the dialog; may be
     *                  <code>null</code>, in which case the dialog just
     *                  shows a progress bar (and optional cancel button).
     *  @param action   If not <code>null</code>, the dialog will display a
     *                  single button that invokes this action (normally used
     *                  to cancel the operation).
     *  @param options  Zero or more options controlling the dialog's appearance
     *                  and behavior.
     */
    public ProgressDialogController(JFrame owner, String title, String text,
                                    Action action, Options... options)
    {
        _owner = owner;
        _title = (title != null) ? title : "";
        _text = text;
        _action = action;

        for (Options option : options)
            _options.add(option);
    }


    /**
     *  Convenience constructor for a non-modal dialog that does not have an
     *  action button.
     *
     *  @param owner    The dialog owner; may be <code>null</code>, in which
     *                  case Swing will generate a hidden owner frame.
     *  @param title    Text to display in the dialog window's title area; may
     *                  be <code>null</code>, in which case the title is left
     *                  empty.
     *  @param text     Text to display in the body of the dialog; may be
     *                  <code>null</code>, in which case the dialog just
     *                  shows a progress bar (and optional cancel button).
     */
    public ProgressDialogController(JFrame owner, String title, String text)
    {
        this(owner, title, text, null);
    }


//----------------------------------------------------------------------------
//  Public Methods
//----------------------------------------------------------------------------

    /**
     *  Displays the dialog, constructing it if necessary. The dialog initially
     *  displays in indeterminate mode, unless {@link #setProgress} was called
     *  prior to this method.
     *
     *  @returns The controller itself, as a convenience for construct-and-show
     *           usage (but still assigning to a variable).
     */
    public ProgressDialogController show()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                if (_theDialog == null)
                    constructDialog();
                internalSetProgress();
                SwingUtil.centerAndShow(_theDialog, _owner);
            }
        });
        return this;
    }


    /**
     *  Hides the dialog, but does not dispose it; also resets progress data.
     *  This method is useful if the dialog is to be reused.
     */
    public void hide()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                _theDialog.setVisible(false);
                reset();
            }
        });
    }


    /**
     *  Disposes the dialog, allowing the JVM to reclaim all resources. Any
     *  future calls to {@link #show} will have to reconstruct it.
     */
    public void dispose()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                _theDialog.dispose();
                _theDialog = null;
                reset();
            }
        });
    }


    /**
     *  Sets the dialog's progress indicator to the given values. If the
     *  dialog was previously in indeterminate mode, this will switch it
     *  to determinate mode.
     *
     *  @param  min     The minimum progress value.
     *  @param  current The current progress value; this sets the position
     *                  of the dialog's indicator.
     *  @param  max     The maximum progress value.
     *
     *  @returns The controller itself, as a convenience for construct-and-show
     *           or set-and-show usage.
     */
    public void setProgress(int min, int current, int max)
    {
        _min = Integer.valueOf(min);
        _max = Integer.valueOf(max);
        _cur = Integer.valueOf(current);

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                internalSetProgress();
            }
        });
    }


    /**
     *  Switches the dialog to indeterminate mode.
     *
     *  @returns The controller itself, as a convenience for construct-and-show
     *           or set-and-show usage.
     */
    public void clearProgress()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                reset();
                internalSetProgress();
            }
        });
    }


//----------------------------------------------------------------------------
//  Internals -- all invoked on the event dispatch thread
//----------------------------------------------------------------------------

    private void constructDialog()
    {
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        if (_text != null)
        {
            JLabel text = new JLabel(_text);
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
            panel.add(text);
            contentPane.add(panel, BorderLayout.NORTH);
        }

        _progressBar = new JProgressBar();
        _progressBar.setMinimumSize(new Dimension(150, 30));
        contentPane.add(_progressBar, BorderLayout.CENTER);

        if (_action != null)
        {
            JButton button = new JButton(_action);
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
            panel.add(button);
            contentPane.add(panel, BorderLayout.SOUTH);
        }

        _theDialog = new JDialog(_owner, _title, _options.contains(Options.MODAL));
        _theDialog.setContentPane(contentPane);
        _theDialog.pack();
    }


    private void reset()
    {
        _min = null;
        _max = null;
        _cur = null;
    }


    private void internalSetProgress()
    {
        if ((_min == null) || (_max == null))
        {
            _progressBar.setIndeterminate(true);
            _progressBar.setStringPainted(false);
        }
        else
        {
            _progressBar.setIndeterminate(false);
            _progressBar.setStringPainted(_options.contains(Options.SHOW_PERCENT_COMPLETE));
            _progressBar.setMinimum(_min.intValue());
            _progressBar.setMaximum(_max.intValue());
            if (_cur != null)
                _progressBar.setValue(_cur.intValue());
        }
    }
}
