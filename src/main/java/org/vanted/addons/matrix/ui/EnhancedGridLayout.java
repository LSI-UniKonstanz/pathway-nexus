package org.vanted.addons.matrix.ui;
import java.awt.*;
import java.io.Serializable;
import java.util.HashSet;

public class EnhancedGridLayout implements LayoutManager, Serializable {

    private final HashSet<Integer> hiddenRows;
    private final HashSet<Integer> hiddenCols;
    int hgap;
    int vgap;
    int rows;
    int cols;
    private boolean transposed;
    // use the same constructors as the super class

    public EnhancedGridLayout(int rows, int cols) {
        transposed = false;
        this.rows = rows;
        this.cols = cols;
        hgap = 0;
        vgap = 0;
        hiddenRows = new HashSet<>();
        hiddenCols = new HashSet<>();
    }

    public void setTranspose(boolean t) {
        this.transposed = t;
    }

    public int getRows() {
        return this.rows;
    }

    public void setRows(int rows) {
        if (rows == 0 && this.cols == 0) {
            throw new IllegalArgumentException("rows and cols cannot both be zero");
        } else {
            this.rows = rows;
        }
    }

    public int getColumns() {
        return this.cols;
    }

    public void setColumns(int cols) {
        if (cols == 0 && this.rows == 0) {
            throw new IllegalArgumentException("rows and cols cannot both be zero");
        } else {
            this.cols = cols;
        }
    }

    public void addLayoutComponent(String str, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        synchronized(parent.getTreeLock()) {
            findHiddenRowsAndCols(parent);
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int nrows = this.rows - hiddenRows.size();
            int ncols = this.cols - hiddenCols.size();
            if (nrows > 0) {
                ncols = (ncomponents + nrows - 1) / nrows - hiddenCols.size();
            } else {
                nrows = (ncomponents + ncols - 1) / ncols - hiddenRows.size();
            }

            int w = 0;
            int h = 0;

            for(int i = 0; i < ncomponents; ++i) {
                Component comp = parent.getComponent(i);
                Dimension d = comp.getPreferredSize();
                if (w < d.width) {
                    w = d.width;
                }

                if (h < d.height) {
                    h = d.height;
                }
            }

            return new Dimension(insets.left + insets.right + ncols * w + (ncols - 1) * this.hgap,
                                 insets.top + insets.bottom + nrows * h + (nrows - 1) * this.vgap);
        }
    }

    public Dimension minimumLayoutSize(Container parent) {
        synchronized(parent.getTreeLock()) {
            findHiddenRowsAndCols(parent);
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int nrows = this.rows - hiddenRows.size();
            int ncols = this.cols - hiddenCols.size();
            if (nrows > 0) {
                ncols = (ncomponents + nrows - 1) / nrows;
            } else {
                nrows = (ncomponents + ncols - 1) / ncols;
            }

            int w = 0;
            int h = 0;

            for(int i = 0; i < ncomponents; ++i) {
                Component comp = parent.getComponent(i);
                Dimension d = comp.getMinimumSize();
                if (w < d.width) {
                    w = d.width;
                }

                if (h < d.height) {
                    h = d.height;
                }
            }
            return new Dimension(insets.left + insets.right + ncols * w + (ncols - 1) * this.hgap,
                                 insets.top + insets.bottom + nrows * h + (nrows - 1) * this.vgap);
        }
    }

    /**
     * Find all rows and columns that are hidden. First go through the columns and find the first one that is not
     * completely hidden. The hidden cells in this column are the hidden rows. Then take the first row that is not
     * hidden and find the hidden cells in this row. These are the hidden columns.
     * @param parent
     */
    private void findHiddenRowsAndCols(Container parent) {
        synchronized(parent.getTreeLock()) {
            hiddenRows.clear();
            hiddenCols.clear();
            int ncomponents = parent.getComponentCount();
            int nrows = this.rows;
            int ncols = this.cols;
            if (ncomponents != 0) {
                int i;
                boolean colHidden;
                int c = 0;
                // find the hidden rows
                do {
                    int hiddenRowCount = 0;
                    for (int r = 0; r < nrows; r++) {
                        i = r * ncols + c;
                        if (i < ncomponents && !parent.getComponent(i).isVisible()) {
                            hiddenRows.add(r);
                            hiddenRowCount++;
                        }
                    }
                    colHidden = hiddenRowCount == nrows;
                    if (colHidden)  // Column only contains hidden elements
                        hiddenRows.clear();
                    c++;
                } while (colHidden && c < ncols);
                // find the first non-hidden row
                int nonHiddenRow = 0;
                while (hiddenRows.contains(nonHiddenRow) && nonHiddenRow < nrows)
                    nonHiddenRow++;
                // All rows are hidden
                if (nonHiddenRow >= nrows)
                    return;

                for (int r = 0; r < ncols; r++) {
                    i = nonHiddenRow * nrows + r;
                    Component cmp = parent.getComponent(i);
                    if (i < ncomponents && !parent.getComponent(i).isVisible()) {
                        hiddenCols.add(r);
                    }
                }
            }
        }
    }

    public void layoutContainer(Container parent) {
        if (transposed)
            transposedLayout(parent);
        else
            normalLayout(parent);

    }

    private void normalLayout(Container parent) {
        synchronized(parent.getTreeLock()) {
            findHiddenRowsAndCols(parent);
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int nrows = this.rows;
            int ncols = this.cols;
            int visibleRows = nrows - hiddenRows.size();
            int visibleCols = ncols - hiddenCols.size();
            boolean ltr = parent.getComponentOrientation().isLeftToRight();
            if (ncomponents != 0) {
                if (nrows > 0) {
                    ncols = (ncomponents + nrows - 1) / nrows;
                } else {
                    nrows = (ncomponents + ncols - 1) / ncols;
                }

                int totalGapsWidth = (visibleCols - 1) * this.hgap;
                int widthWOInsets = parent.getWidth() - (insets.left + insets.right);
                int widthOnComponent = (widthWOInsets - totalGapsWidth) / visibleCols;
                int extraWidthAvailable = (widthWOInsets - (widthOnComponent * visibleCols + totalGapsWidth)) / 2;
                int totalGapsHeight = (visibleRows - 1) * this.vgap;
                int heightWOInsets = parent.getHeight() - (insets.top + insets.bottom);
                int heightOnComponent = (heightWOInsets - totalGapsHeight) / visibleRows;
                int extraHeightAvailable = (heightWOInsets - (heightOnComponent * visibleRows + totalGapsHeight)) / 2;
                int x;
                int r;
                int y;
                int i;
                if (ltr) {
                    x = insets.left + extraWidthAvailable;
                    for(int c = 0; c < ncols; c++) {
                        y = insets.top + extraHeightAvailable;
                        for(r = 0; r < nrows; r++) {
                            i = r * ncols + c;
                            if (i < ncomponents) {
                                parent.getComponent(i).setBounds(x, y, widthOnComponent, heightOnComponent);
                            }
                            if (!hiddenRows.contains(r))
                                y += heightOnComponent + this.vgap;
                        }
                        if (!hiddenCols.contains(c))
                            x += widthOnComponent + this.hgap;
                    }
                } else {
                    x = parent.getWidth() - insets.right - widthOnComponent - extraWidthAvailable;
                    for(int c = 0; c < ncols; c++) {
                        y = insets.top + extraHeightAvailable;
                        for(r = 0; r < nrows; r++) {
                            i = r * ncols + c;
                            if (i < ncomponents) {
                                if (parent.getComponent(i).isVisible())
                                    parent.getComponent(i).setBounds(x, y, widthOnComponent, heightOnComponent);
                                else
                                    parent.getComponent(i).setBounds(x, y, 0, 0);
                            }
                            if (!hiddenRows.contains(c))
                                y += heightOnComponent + this.vgap;
                        }
                        if (!hiddenCols.contains(r))
                            x -= widthOnComponent + this.hgap;
                    }
                }

            }
        }
    }

    private void transposedLayout(Container parent) {
        synchronized(parent.getTreeLock()) {
            findHiddenRowsAndCols(parent);
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int nrows = this.rows;
            int ncols = this.cols;
            int visibleRows = nrows - hiddenRows.size();
            int visibleCols = ncols - hiddenCols.size();
            boolean ltr = parent.getComponentOrientation().isLeftToRight();
            if (ncomponents != 0) {
                if (nrows > 0) {
                    ncols = (ncomponents + nrows - 1) / nrows;
                } else {
                    nrows = (ncomponents + ncols - 1) / ncols;
                }

                int totalGapsWidth = (visibleCols - 1) * this.hgap;
                int widthWOInsets = parent.getWidth() - (insets.left + insets.right);
                int widthOnComponent = (widthWOInsets - totalGapsWidth) / visibleRows;
                int extraWidthAvailable = (widthWOInsets - (widthOnComponent * visibleRows + totalGapsWidth)) / 2;
                int totalGapsHeight = (visibleRows - 1) * this.vgap;
                int heightWOInsets = parent.getHeight() - (insets.top + insets.bottom);
                int heightOnComponent = (heightWOInsets - totalGapsHeight) / visibleCols;
                int extraHeightAvailable = (heightWOInsets - (heightOnComponent * visibleCols + totalGapsHeight)) / 2;
                int x;
                int r;
                int y = insets.top + extraHeightAvailable;
                int i;
                if (ltr) {
                    for(int c = 0; c < ncols; c++) {
                        x = insets.left + extraWidthAvailable;
                        for(r = 0; r < nrows; r++) {
                            i = r * ncols + c;
                            if (i < ncomponents) {
                                parent.getComponent(i).setBounds(x, y, widthOnComponent, heightOnComponent);
                            }
                            if (!hiddenRows.contains(r))
                                x += widthOnComponent + this.hgap;
                        }
                        if (!hiddenCols.contains(c))
                            y += heightOnComponent + this.vgap;
                    }
                } else {
                    for(int c = 0; c < ncols; c++) {
                        x = parent.getWidth() - insets.right - widthOnComponent - extraWidthAvailable;
                        for(r = 0; r < nrows; r++) {
                            i = r * ncols + c;
                            if (i < ncomponents) {
                                if (parent.getComponent(i).isVisible())
                                    parent.getComponent(i).setBounds(x, y, widthOnComponent, heightOnComponent);
                                else
                                    parent.getComponent(i).setBounds(x, y, 0, 0);
                            }
                            if (!hiddenRows.contains(c))
                                x -= widthOnComponent + this.hgap;
                        }
                        if (!hiddenCols.contains(r))
                            y += heightOnComponent + this.vgap;
                    }
                }

            }
        }
    }
}
