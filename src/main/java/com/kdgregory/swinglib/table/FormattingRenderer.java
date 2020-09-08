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

package com.kdgregory.swinglib.table;

import java.text.Format;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;


/**
 *  A renderer that allows the caller to specify formatter and alignment.
 */
public class FormattingRenderer
extends DefaultTableCellRenderer
{
    private static final long serialVersionUID = 1L;


    /**
     *  Used to specify the alignment of text within the cell, in a way
     *  that is not tied to Swing constants.
     */
    public static enum Alignment
    {
        LEFT(SwingConstants.LEFT),
        RIGHT(SwingConstants.RIGHT),
        CENTER(SwingConstants.CENTER);

        //--------------------------------------------------------------------

        private int swingAlign;

        private Alignment(int value)
        {
            swingAlign = value;
        }

        public int getSwingAlignment()
        {
            return swingAlign;
        }
    }


//----------------------------------------------------------------------------
//  Instance Variables and Constructor
//----------------------------------------------------------------------------

    private Format formatter;


    /**
     *  Creates an instance that is right-aligned (I consider this the most
     *  common alignment for special-purpose renderers).
     *
     *  @param formatter    This object is responsible for actual formatting.
     *                      Note that most formatters are not thread-safe;
     *                      they may, however, be shared between renderers
     *                      (since all rendering happens on EDT).
     *  @param alignment    Used to set the text alignment of this renderer.
     */
    public FormattingRenderer(Format formatter)
    {
        this(formatter, Alignment.RIGHT);
    }


    /**
     *  Creates an instance that allows you to specify alignment.
     *
     *  @param formatter    This object is responsible for actual formatting.
     *                      Note that most formatters are not thread-safe;
     *                      they may, however, be shared between renderers
     *                      (since all rendering happens on EDT).
     *  @param alignment    Used to set the text alignment of this renderer.
     */
    public FormattingRenderer(Format formatter, Alignment alignment)
    {
        super();
        setHorizontalAlignment(alignment.getSwingAlignment());
        this.formatter = formatter;
    }


//----------------------------------------------------------------------------
//  DefaultTableCellRenderer
//----------------------------------------------------------------------------

    @Override
    protected void setValue(Object value)
    {
        String text = (value == null)
                    ? ""
                    : formatter.format(value);
        setText(text);
    }
}
