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

import java.awt.Toolkit;
import javax.swing.SwingUtilities;

import junit.framework.*;

import com.kdgregory.swinglib.AsynchronousOperation;


public class TestAsynchronousOperation extends TestCase
{
//------------------------------------------------------------------------------
//  Boilerplate
//------------------------------------------------------------------------------

    public TestAsynchronousOperation(String testName)
    {
        super(testName);
    }

//------------------------------------------------------------------------------
//  Common test code
//------------------------------------------------------------------------------

    @Override
    public void setUp()
    {
        // this should ensure that the event thread is running
        Toolkit.getDefaultToolkit();
    }


//------------------------------------------------------------------------------
//  Support code
//------------------------------------------------------------------------------

    /**
     *  A mock implementation of <CODE>AsynchronousOperation</CODE>. This still
     *  needs to be subclassed to implement <CODE>performOperation()</CODE>.
     */
    private static abstract class BaseOperation
    extends AsynchronousOperation<Object>
    {
        public boolean      callbackOnEventThread;
        public boolean      onCompleteCalled;
        public Object       callbackResult;
        public Throwable    callbackException;

        @Override
        protected void onComplete()
        {
            onCompleteCalled = true;
        }

        @Override
        protected void onSuccess(Object result)
        {
            callbackOnEventThread = SwingUtilities.isEventDispatchThread();
            callbackResult = result;
        }

        @Override
        protected void onFailure(Throwable e)
        {
            callbackOnEventThread = SwingUtilities.isEventDispatchThread();
            callbackException = e;
        }
    }


    /**
     *  Instances of this object are put on the event thread to synchronize
     *  tests.
     */
    private static class NullRunnable
    implements Runnable
    {
        @Override
        public void run()
        {
            // nothing happening here
        }
    }


//------------------------------------------------------------------------------
//  Test methods go here
//------------------------------------------------------------------------------

    public void testSuccess()
    throws Exception
    {
        final Integer result = new Integer(123);
        BaseOperation testOperation = new BaseOperation()
        {
            @Override
            protected Object performOperation()
            throws Exception
            {
                return result;
            }
        };

        testOperation.run();
        SwingUtilities.invokeAndWait(new NullRunnable());

        assertTrue("callback on event thread", testOperation.callbackOnEventThread);
        assertTrue("onComplete() called", testOperation.onCompleteCalled);
        assertEquals("result", result, testOperation.callbackResult);
        assertNull("exception", testOperation.callbackException);
    }


    public void testFailure()
    throws Exception
    {
        final Exception result = new IndexOutOfBoundsException("test");
        BaseOperation testOperation = new BaseOperation()
        {
            @Override
            protected Object performOperation()
            throws Exception
            {
                throw result;
            }
        };

        testOperation.run();
        SwingUtilities.invokeAndWait(new NullRunnable());

        assertTrue("callback on event thread", testOperation.callbackOnEventThread);
        assertTrue("onComplete() called", testOperation.onCompleteCalled);
        assertEquals("exception", result, testOperation.callbackException);
        assertNull("result", testOperation.callbackResult);
    }
}
