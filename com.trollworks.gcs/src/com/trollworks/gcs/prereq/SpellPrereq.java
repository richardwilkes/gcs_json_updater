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
import com.trollworks.gcs.criteria.StringCompareType;
import com.trollworks.gcs.criteria.StringCriteria;
import com.trollworks.gcs.ui.widget.outline.ListRow;
import com.trollworks.gcs.utility.json.JsonWriter;
import com.trollworks.gcs.utility.xml.XMLNodeType;
import com.trollworks.gcs.utility.xml.XMLReader;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** A Spell prerequisite. */
public class SpellPrereq extends HasPrereq {
    /** The XML tag for this class. */
    public static final  String          TAG_ROOT          = "spell_prereq";
    /** The tag/type for name comparison. */
    public static final  String          TAG_NAME          = "name";
    /** The tag/type for any. */
    public static final  String          TAG_ANY           = "any";
    /** The tag/type for college name comparison. */
    public static final  String          TAG_COLLEGE       = "college";
    /** The tag/type for college count comparison. */
    public static final  String          TAG_COLLEGE_COUNT = "college_count";
    private static final String          TAG_QUANTITY      = "quantity";
    private static final String          KEY_SUB_TYPE      = "sub_type";
    private static final String          KEY_QUALIFIER     = "qualifier";
    private              String          mType;
    private              StringCriteria  mStringCriteria;
    private              IntegerCriteria mQuantityCriteria;

    /**
     * Creates a new prerequisite.
     *
     * @param parent The owning prerequisite list, if any.
     */
    public SpellPrereq(PrereqList parent) {
        super(parent);
        mType = TAG_NAME;
        mStringCriteria = new StringCriteria(StringCompareType.IS, "");
        mQuantityCriteria = new IntegerCriteria(NumericCompareType.AT_LEAST, 1);
    }

    /**
     * Loads a prerequisite.
     *
     * @param parent The owning prerequisite list, if any.
     * @param reader The XML reader to load from.
     */
    public SpellPrereq(PrereqList parent, XMLReader reader) throws IOException {
        this(parent);

        String marker = reader.getMarker();
        loadHasAttribute(reader);

        do {
            if (reader.next() == XMLNodeType.START_TAG) {
                String name = reader.getName();

                if (TAG_NAME.equals(name)) {
                    setType(TAG_NAME);
                    mStringCriteria.load(reader);
                } else if (TAG_ANY.equals(name)) {
                    setType(TAG_ANY);
                    mQuantityCriteria.load(reader);
                } else if (TAG_COLLEGE.equals(name)) {
                    setType(TAG_COLLEGE);
                    mStringCriteria.load(reader);
                } else if (TAG_COLLEGE_COUNT.equals(name)) {
                    setType(TAG_COLLEGE_COUNT);
                    mQuantityCriteria.load(reader);
                } else if (TAG_QUANTITY.equals(name)) {
                    mQuantityCriteria.load(reader);
                } else {
                    reader.skipTag(name);
                }
            }
        } while (reader.withinMarker(marker));
    }

    /**
     * Creates a copy of the specified prerequisite.
     *
     * @param parent The owning prerequisite list, if any.
     * @param prereq The prerequisite to clone.
     */
    protected SpellPrereq(PrereqList parent, SpellPrereq prereq) {
        super(parent, prereq);
        mType = prereq.mType;
        mStringCriteria = new StringCriteria(prereq.mStringCriteria);
        mQuantityCriteria = new IntegerCriteria(prereq.mQuantityCriteria);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof SpellPrereq && super.equals(obj)) {
            SpellPrereq sp = (SpellPrereq) obj;
            return mType.equals(sp.mType) && mStringCriteria.equals(sp.mStringCriteria) && mQuantityCriteria.equals(sp.mQuantityCriteria);
        }
        return false;
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
        return new SpellPrereq(parent, this);
    }

    @Override
    public void saveSelf(JsonWriter w) throws IOException {
        super.saveSelf(w);
        w.keyValue(KEY_SUB_TYPE, mType);
        if (TAG_NAME.equals(mType) || TAG_COLLEGE.equals(mType)) {
            mStringCriteria.save(w, KEY_QUALIFIER);
        }
        mQuantityCriteria.save(w, TAG_QUANTITY);
    }

    /** @return The type of comparison to make. */
    public String getType() {
        return mType;
    }

    /**
     * @param type The type of comparison to make. Must be one of {@link #TAG_NAME}, {@link
     *             #TAG_ANY}, {@link #TAG_COLLEGE}, or {@link #TAG_COLLEGE_COUNT}.
     */
    public void setType(String type) {
        if (TAG_NAME.equals(type)) {
            mType = TAG_NAME;
        } else if (TAG_ANY.equals(type)) {
            mType = TAG_ANY;
        } else if (TAG_COLLEGE.equals(type)) {
            mType = TAG_COLLEGE;
        } else if (TAG_COLLEGE_COUNT.equals(type)) {
            mType = TAG_COLLEGE_COUNT;
        } else {
            mType = TAG_NAME;
        }
    }

    /** @return The string comparison object. */
    public StringCriteria getStringCriteria() {
        return mStringCriteria;
    }

    /** @return The quantity comparison object. */
    public IntegerCriteria getQuantityCriteria() {
        return mQuantityCriteria;
    }

    @Override
    public void fillWithNameableKeys(Set<String> set) {
        if (!Objects.equals(mType, TAG_COLLEGE_COUNT)) {
            ListRow.extractNameables(set, mStringCriteria.getQualifier());
        }
    }

    @Override
    public void applyNameableKeys(Map<String, String> map) {
        if (!Objects.equals(mType, TAG_COLLEGE_COUNT)) {
            mStringCriteria.setQualifier(ListRow.nameNameables(map, mStringCriteria.getQualifier()));
        }
    }
}
