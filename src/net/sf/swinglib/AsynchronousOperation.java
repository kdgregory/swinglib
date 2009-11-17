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

import javax.swing.SwingUtilities;


/**
 *  A base class for implementing operations that need to run on their own
 *  thread and report back to the event thread. Unlike Sun's <code>SwingWorker
 *  </code> class, this object does not spawn its own thread. Instead, it must
 *  be passed to a program-created thread, or better, a threadpool.
 *  <p>
 *  To use, subclass and pass an instance to your operation thread.
 *  <p>
 *  You must implement at least the {@link #performOperation} method, which is
 *  executed on the operation thread. This method may return a single object,
 *  or throw any exception type. Depending on how it completes (return/throw),
 *  one of {@link #onSuccess}, {@link #onFailure} will then be executed on the
 *  event thread.
**/

public abstract class AsynchronousOperation<T>
implements Runnable
{
    public final void run()
    {
        try
        {
            final T result = performOperation();
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    onSuccess(result);
                }
            });
        }
        catch (final Exception e)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    onFailure(e);
                }
            });
        }
    }


    /**
     *  The concrete class implements this method, which is executed on the
     *  non-event thread. It is permitted to return a value that is then passed
     *  to the code running on the event thread. It is also permitted to throw
     *  any exception type.
     */
    protected abstract T performOperation()
    throws Exception;


    /**
     *  This method is invoked on the event thread after a successful call to
     *  <code>performOperation()</code>. Application code typically overrides
     *  to do something with that result.
     */
    protected void onSuccess(T result)
    {
        // default implementation does nothing
    }


    /**
     *  This method is invoked on the event thread if <code>performOperation()
     *  </code> threw an exception. Application code typically overrides
     *  to do something with that exception.
     */
    protected void onFailure(Exception e)
    {
        throw new RuntimeException(e);
    }
}
