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

package com.kdgregory.swinglib.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;


/**
 *  A layout manager designed for building input forms. It provides the
 *  following features:
 *  <UL>
 *  <LI> Divides the container into a grid of X rows and Y columns. Number of
 *       columns is set at construction time, number of rows is determined from
 *       number of components to be laid out.
 *  <LI> Components are assigned to grid locations based on the order that they
 *       are added to the container, going across and then down (ie, row 0
 *       column 0, followed by row 0 column 1) . You do not need to specify an
 *       explicit constraints object for a component.
 *  <LI> The preferred width of a column is found by taking the maximum preferred
 *       width of all components in the column. The preferred height of a column
 *       is found by taking the maximum preferred height of all components in
 *       the column.
 *  <LI> The minimum width/height are calculated the same way, using the minimum
 *       dimensions of the components.
 *  <LI> Each component is laid out at its preferred height and width, if there
 *       is room in the cell; components are not stretched to fit the height and
 *       width of their cell. This layout manager is used for forms, and stretching
 *       all components to same size gives a very unnatural appearance.
 *  <LI> If the layout manager does not have room for all rows and columns, it
 *       will reduce each to its minimum height or width. It will not attempt
 *       to reduce components below their minimum width, which means that
 *       components may be laid out outside of the bounds of the container.
 *  <LI> By default, components will be aligned according to their preferred
 *       horizontal and vertical alignment. The layout manager will provide a
 *       way to override for all components, since forms typically use a left/
 *       center alignment.
 *  </UL>
 */

public class CompactGridLayout
implements LayoutManager2
{
//----------------------------------------------------------------------------
//  Instance data and constructors
//----------------------------------------------------------------------------

    private int     cols;                  // # columns, set by ctor
    private int     rows;                  // # rows, set by recalculate()

    private int     vGap;                  // gap between rows
    private int     hGap;                  // gap between cols

    private float   xAlignment;            // horizontal alignment within cell
    private float   yAlignment;            // vertical alignment within cell

    private int[]   minRowHeights;         // minimum height of each row
    private int[]   prfRowHeights;         // preferred height of each row
    private int[]   minColWidths;          // minimum width of each column
    private int[]   prfColWidths;          // preferred width of each column

    private int     preferredHeight;       // these four include container insets
    private int     preferredWidth;
    private int     minimumHeight;
    private int     minimumWidth;


    /** Basic constructor, which allows user to specify the number of columns
     *  and nothing else. Vertical and horizontal gaps both default to 0, and
     *  component alignment defaults to left-horizontal and center-vertical.
     *
     *  @param  cols    Number of columns in the layout.
     */
    public CompactGridLayout( int cols )
    {
        this(cols, 0, 0, 0.0f, 0.5f);
    }


    /** Constructor that allows specification of horizontal and vertical gaps
     *  between components. Alignment defaults to left-horizontal and center-
     *  vertical.
     *
     *  @param  cols    Number of columns in the layout.
     *  @param  hGap    Horizontal gap between components, in pixels.
     *  @param  vGap    Vertical gap between components, in pixels.
     */
    public CompactGridLayout( int cols, int hGap, int vGap )
    {
        this(cols, hGap, vGap, 0.0f, 0.5f);
    }


    /** Constructor that allows specification of horizontal and vertical gaps
     *  between components, as well as component alignment within cells.
     *
     *  @param  cols    Number of columns in the layout.
     *  @param  hGap    Horizontal gap between components, in pixels.
     *  @param  vGap    Vertical gap between components, in pixels.
     *  @param  hAlign  Horizontal alignment of components within cell.
     *  @param  vAlign  Vertical alignment of components within cell.
     */
    public CompactGridLayout( int cols, int hGap, int vGap, float hAlign, float vAlign )
    {
        this.cols = cols;
        this.hGap = hGap;
        this.vGap = vGap;
        this.xAlignment = hAlign;
        this.yAlignment = vAlign;
    }


//----------------------------------------------------------------------------
//  Public methods
//----------------------------------------------------------------------------

    /**
     *  Adds a component to the layout, associating it with the specified
     *  constraints object.
     *
     *  @param  comp    The component to add.
     *  @param  cons    A constraints object to go with this component. Since
     *                  this layout manager does not use constraints, this
     *                  parameter is ignored, and may be <CODE>null</CODE>
     */
    @Override
    public void addLayoutComponent( Component comp, Object cons )
    {
        // in this revision, we don't keep track of constraints, so don't
        // need to do anything in this method
    }


    /**
     *  Adds a component to the layout, associating it with the specified
     *  constraints object.
     *
     *  @deprecated     use <CODE>addLayoutComponent(Component,Object)</CODE>
     *
     *  @param  comp    The component to add.
     *  @param  cons    A constraints object to go with this component. Since
     *                  this layout manager does not use constraints, this
     *                  parameter is ignored, and may be <CODE>null</CODE>
     */
    @Override
    @Deprecated
    public void addLayoutComponent( String name, Component comp )
    {
        // in this revision, we don't keep track of constraints, so don't
        // need to do anything in this method
    }


    /**
     *  Removes a component from the layout.
     */
    @Override
    public void removeLayoutComponent(Component comp)
    {
        // we don't need to do anything since we just get the list of
        // components from the container
    }


    /**
     *  Invalidates the current cached information for this layout, and
     *  recalculates the row heights and column widths.
     */
    @Override
    public void invalidateLayout(Container target)
    {
        synchronized (target.getTreeLock())
        {
            minRowHeights = null;
            prfRowHeights = null;
            minColWidths  = null;
            prfColWidths  = null;
        }
    }


    /**
     *  Returns the preferred size of this layout, based on its components.
     */
    @Override
    public Dimension preferredLayoutSize(Container target)
    {
        synchronized (target.getTreeLock())
        {
            recalculate(target);
            return new Dimension(preferredWidth, preferredHeight);
        }
    }


    /**
     *  Returns the minimum size of this layout, based on its components.
     */
    @Override
    public Dimension minimumLayoutSize(Container target)
    {
        synchronized (target.getTreeLock())
        {
            recalculate(target);
            return new Dimension(minimumWidth, minimumHeight);
        }
    }


    /**
     *  Returns the maximum size of this layout, which is the same as its
     *  preferred size.
     */
    @Override
    public Dimension maximumLayoutSize(Container target)
    {
        return preferredLayoutSize(target);
    }


    /**
     *  Lays out the container.
     */
    @Override
    public void layoutContainer(Container target)
    {
        synchronized (target.getTreeLock())
        {
            recalculate(target);

            int row = 0;
            int col = 0;
            int x = target.getInsets().left;
            int y = target.getInsets().top;

            int rowHeight = calculateRowHeight(row);

            for (int ii = 0 ; ii < target.getComponentCount() ; ii++)
            {
                Component comp = target.getComponent(ii);
                int compWidth = comp.getPreferredSize().width;
                int compHeight = comp.getPreferredSize().height;

                int colWidth = calculateColWidth(col);

                comp.setBounds(x, y, compWidth, compHeight);

                x += colWidth + hGap;

                if (++col == cols)
                {
                    col = 0;
                    row++;
                    x = target.getInsets().left;
                    y += rowHeight + vGap;
                    if (row < rows)
                        rowHeight = calculateRowHeight(row);
                }
            }
        }
    }


    /**
     *  Returns a default horizontal alignment value for this container.
     */
    @Override
    public float getLayoutAlignmentX(Container target)
    {
        return 0.0f;
    }


    /**
     *  Returns a default vertical alignment value for this container.
     */
    @Override
    public float getLayoutAlignmentY(Container target)
    {
        return 0.0f;
    }


//----------------------------------------------------------------------------
//  Internal methods
//----------------------------------------------------------------------------

    /**
     *  Recalculates the row heights and column widths for this layout, based
     *  on the current components. This information is cached, and this method
     *  doesn't actually do the calculation unless the cache is invalid.
     */
    private void recalculate( Container target )
    {
        if (minRowHeights != null)
            return;

        Component[] comps = target.getComponents();

        int _rows = (comps.length / cols)
                  + (((comps.length % cols) == 0) ? 0 : 1);

        minRowHeights   = new int[_rows];
        prfRowHeights   = new int[_rows];
        minColWidths    = new int[cols];
        prfColWidths    = new int[cols];

        int row = 0;
        int col = 0;
        for (int ii = 0 ; ii < comps.length ; ii++)
        {
            minRowHeights[row] = Math.max(minRowHeights[row],
                                           comps[ii].getMinimumSize().height);
            minColWidths[col]  = Math.max(minColWidths[col],
                                           comps[ii].getMinimumSize().width);
            prfRowHeights[row] = Math.max(prfRowHeights[row],
                                           comps[ii].getPreferredSize().height);
            prfColWidths[col]  = Math.max(prfColWidths[col],
                                           comps[ii].getPreferredSize().width);

            if (++col == cols)
            {
                col = 0;
                row++;
            }
        }

        minimumHeight = preferredHeight
                       = target.getInsets().top + target.getInsets().bottom;
        minimumWidth  = preferredWidth
                       = target.getInsets().left + target.getInsets().right;

        for (row = 0 ; row < _rows ; row++)
        {
            preferredHeight += prfRowHeights[row];
            minimumHeight += minRowHeights[row];
        }

        for (col = 0 ; col < cols ; col++)
        {
            preferredWidth += prfColWidths[col];
            minimumWidth += minColWidths[col];
        }

        preferredHeight += (_rows - 1) * vGap;
        preferredWidth  += (cols - 1) * hGap;
        minimumHeight   += (_rows - 1) * vGap;
        minimumWidth    += (cols - 1) * hGap;
    }


    /** Calculates the height of a row, scaling if necessary for an undersized
     *  layout. This method is only called from <CODE>layoutContainer()</CODE>,
     *  but I want to minimize code clutter in there.
     */
    private int calculateRowHeight( int row )
    {
        return prfRowHeights[row];
    }


    /** Calculates the width of a column, scaling if necessary for an undersized
     *  layout. As with <CODE>calculateRowHeight()</CODE>, this method is only
     *  called from <CODE>layoutContainer()</CODE>.
     */
    private int calculateColWidth( int col )
    {
        return prfColWidths[col];
    }
}
