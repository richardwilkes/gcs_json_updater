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

package com.trollworks.gcs.prereq;

import com.trollworks.gcs.criteria.IntegerCriteria;
import com.trollworks.gcs.criteria.NumericCompareType;
import com.trollworks.gcs.utility.json.JsonWriter;
import com.trollworks.gcs.utility.text.Enums;
import com.trollworks.gcs.utility.xml.XMLReader;

import java.io.IOException;

/** An equipment contained quantity prerequisite. */
public class ContainedQuantityPrereq extends HasPrereq {
    /** The XML tag for this class. */
    public static final  String          TAG_ROOT          = "contained_quantity_prereq";
    private static final String          ATTRIBUTE_COMPARE = "compare";
    private static final String          KEY_QUALIFIER     = "qualifier";
    private              IntegerCriteria mQuantityCompare;

    /**
     * Creates a new prerequisite.
     *
     * @param parent The owning prerequisite list, if any.
     */
    public ContainedQuantityPrereq(PrereqList parent) {
        super(parent);
        mQuantityCompare = new IntegerCriteria(NumericCompareType.AT_MOST, 1);
    }

    /**
     * Loads a prerequisite.
     *
     * @param parent The owning prerequisite list, if any.
     * @param reader The XML reader to load from.
     */
    public ContainedQuantityPrereq(PrereqList parent, XMLReader reader) throws IOException {
        this(parent);
        loadHasAttribute(reader);
        mQuantityCompare.setType(Enums.extract(reader.getAttribute(ATTRIBUTE_COMPARE), NumericCompareType.values(), NumericCompareType.AT_LEAST));
        mQuantityCompare.setQualifier(reader.readInteger(0));
    }

    /**
     * Creates a copy of the specified prerequisite.
     *
     * @param parent The owning prerequisite list, if any.
     * @param prereq The prerequisite to clone.
     */
    protected ContainedQuantityPrereq(PrereqList parent, ContainedQuantityPrereq prereq) {
        super(parent, prereq);
        mQuantityCompare = new IntegerCriteria(prereq.mQuantityCompare);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ContainedQuantityPrereq && super.equals(obj)) {
            return mQuantityCompare.equals(((ContainedQuantityPrereq) obj).mQuantityCompare);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String getJSONTypeName() {
        return TAG_ROOT;
    }

    @Override
    public String getXMLTag() {
        return TAG_ROOT;
    }

    @Override
    public Prereq clone(PrereqList parent) {
        return new ContainedQuantityPrereq(parent, this);
    }

    @Override
    public void saveSelf(JsonWriter w) throws IOException {
        super.saveSelf(w);
        mQuantityCompare.save(w, KEY_QUALIFIER);
    }

    /** @return The quantity comparison object. */
    public IntegerCriteria getQuantityCompare() {
        return mQuantityCompare;
    }
}
