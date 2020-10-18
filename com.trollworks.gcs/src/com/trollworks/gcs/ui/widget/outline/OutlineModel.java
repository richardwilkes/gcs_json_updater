/*
 * Copyright ©1998-2020 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.gcs.ui.widget.outline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OutlineModel {
    private ArrayList<Row> mRows = new ArrayList<>();

    /**
     * Adds the specified row.
     *
     * @param row             The row to add.
     * @param includeChildren Whether children of open rows are added as well.
     */
    public void addRow(Row row, boolean includeChildren) {
        addRow(mRows.size(), row, includeChildren);
    }

    /**
     * Adds the specified row.
     *
     * @param index           The index to add the row at.
     * @param row             The row to add.
     * @param includeChildren Whether children of open rows are added as well.
     */
    public void addRow(int index, Row row, boolean includeChildren) {
        List<Row> list = new ArrayList<>();
        if (includeChildren) {
            collectRowsAndSetOwner(list, row, false);
        } else {
            list.add(row);
            row.setOwner(this);
        }
        mRows.addAll(index, list);
    }

    private void addChildren(Row row) {
        List<Row> list = collectRowsAndSetOwner(new ArrayList<>(), row, true);
        mRows.addAll(getIndexOfRow(row) + 1, list);
    }

    /**
     * Adds the specified row to the passed in list, along with its children if the row is open.
     * Each row added to the list also has its owner set to this outline model.
     *
     * @param list         The list to add it to.
     * @param row          The row to add.
     * @param childrenOnly {@code false} to include the passed in row as well as its children.
     * @return The passed in list.
     */
    public List<Row> collectRowsAndSetOwner(List<Row> list, Row row, boolean childrenOnly) {
        if (!childrenOnly) {
            list.add(row);
            row.setOwner(this);
        }
        if (row.isOpen() && row.hasChildren()) {
            for (Row row2 : row.getChildren()) {
                collectRowsAndSetOwner(list, row2, false);
            }
        }
        return list;
    }

    /**
     * Removes the specified rows.
     *
     * @param rows The rows to remove.
     */
    public void removeRows(Row[] rows) {
        Set<Row> set    = new HashSet<>();
        int      i;
        int      length = rows.length;
        for (i = 0; i < length; i++) {
            int index = getIndexOfRow(rows[i]);
            if (index > -1) {
                collectRowAndDescendantsAtIndex(set, index);
            }
        }
        int[] indexes = new int[set.size()];
        i = 0;
        for (Row row : set) {
            indexes[i++] = getIndexOfRow(row);
        }
        removeRowsInternal(indexes);
    }

    /**
     * Adds the specified row index to the provided set as well as any descendant rows.
     *
     * @param set   The set to add the rows to.
     * @param index The index to start at.
     */
    public void collectRowAndDescendantsAtIndex(Set<Row> set, int index) {
        Row row = getRowAtIndex(index);
        int max = mRows.size();
        set.add(row);
        while (++index < max) {
            Row next = getRowAtIndex(index);
            if (!next.isDescendantOf(row)) {
                break;
            }
            set.add(next);
        }
    }

    private void removeRowsInternal(int[] indexes) {
        int   length = indexes.length;
        Row[] rows   = new Row[length];
        int   i;
        Arrays.sort(indexes);
        for (i = 0; i < length; i++) {
            rows[i] = getRowAtIndex(indexes[i]);
        }
        for (i = length - 1; i >= 0; i--) {
            mRows.remove(indexes[i]);
            rows[i].setOwner(null);
        }
    }

    /** @return The rows contained by the model. */
    public List<Row> getRows() {
        return mRows;
    }

    /** @return The total number of rows present in the outline. */
    public int getRowCount() {
        return mRows.size();
    }

    /**
     * @param index The index of the row.
     * @return The row at the specified index.
     */
    public Row getRowAtIndex(int index) {
        return mRows.get(index);
    }

    /**
     * @param row The row.
     * @return The row index of the specified row.
     */
    public int getIndexOfRow(Row row) {
        return mRows.indexOf(row);
    }

    /** @return The top-level rows (i.e. those with a {@code null} parent). */
    public List<Row> getTopLevelRows() {
        List<Row> list = new ArrayList<>();
        for (Row row : mRows) {
            if (row.getParent() == null) {
                list.add(row);
            }
        }
        return list;
    }

    /**
     * Called when a row's open state changes.
     *
     * @param row  The row being changed.
     * @param open The new open state.
     */
    public void rowOpenStateChanged(Row row, boolean open) {
        if (row.hasChildren() && mRows.contains(row)) {
            if (open) {
                addChildren(row);
            } else {
                removeRows(row.getChildren().toArray(new Row[0]));
            }
        }
    }
}
