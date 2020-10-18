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

import com.trollworks.gcs.criteria.NumericCompareType;
import com.trollworks.gcs.criteria.WeightCriteria;
import com.trollworks.gcs.utility.Fixed6;
import com.trollworks.gcs.utility.json.JsonWriter;
import com.trollworks.gcs.utility.text.Enums;
import com.trollworks.gcs.utility.units.WeightUnits;
import com.trollworks.gcs.utility.units.WeightValue;
import com.trollworks.gcs.utility.xml.XMLReader;

import java.io.IOException;

/** An equipment contained weight prerequisite. */
public class ContainedWeightPrereq extends HasPrereq {
    /** The XML tag for this class. */
    public static final  String         TAG_ROOT          = "contained_weight_prereq";
    private static final String         ATTRIBUTE_COMPARE = "compare";
    private static final String         KEY_QUALIFIER     = "qualifier";
    private              WeightCriteria mWeightCompare;

    /**
     * Creates a new prerequisite.
     *
     * @param parent The owning prerequisite list, if any.
     */
    public ContainedWeightPrereq(PrereqList parent, WeightUnits defUnits) {
        super(parent);
        mWeightCompare = new WeightCriteria(NumericCompareType.AT_MOST, new WeightValue(new Fixed6(5), defUnits));
    }

    /**
     * Loads a prerequisite.
     *
     * @param parent The owning prerequisite list, if any.
     * @param reader The XML reader to load from.
     */
    public ContainedWeightPrereq(PrereqList parent, WeightUnits defUnits, XMLReader reader) throws IOException {
        this(parent, defUnits);
        loadHasAttribute(reader);
        mWeightCompare.setType(Enums.extract(reader.getAttribute(ATTRIBUTE_COMPARE), NumericCompareType.values(), NumericCompareType.AT_LEAST));
        mWeightCompare.setQualifier(WeightValue.extract(reader.readText(), false));
    }

    /**
     * Creates a copy of the specified prerequisite.
     *
     * @param parent The owning prerequisite list, if any.
     * @param prereq The prerequisite to clone.
     */
    protected ContainedWeightPrereq(PrereqList parent, ContainedWeightPrereq prereq) {
        super(parent, prereq);
        mWeightCompare = new WeightCriteria(prereq.mWeightCompare);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ContainedWeightPrereq && super.equals(obj)) {
            return mWeightCompare.equals(((ContainedWeightPrereq) obj).mWeightCompare);
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
        return new ContainedWeightPrereq(parent, this);
    }

    @Override
    public void saveSelf(JsonWriter w) throws IOException {
        super.saveSelf(w);
        mWeightCompare.save(w, KEY_QUALIFIER);
    }

    /** @return The weight comparison object. */
    public WeightCriteria getWeightCompare() {
        return mWeightCompare;
    }
}
