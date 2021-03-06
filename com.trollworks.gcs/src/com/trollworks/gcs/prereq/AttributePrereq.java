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

import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.criteria.IntegerCriteria;
import com.trollworks.gcs.criteria.NumericCompareType;
import com.trollworks.gcs.feature.BonusAttributeType;
import com.trollworks.gcs.utility.json.JsonWriter;
import com.trollworks.gcs.utility.text.Enums;
import com.trollworks.gcs.utility.xml.XMLReader;

import java.io.IOException;

/** A Attribute prerequisite. */
public class AttributePrereq extends HasPrereq {
    /** The possible {@link BonusAttributeType}s that can be affected. */
    public static final  BonusAttributeType[] TYPES                   = {BonusAttributeType.ST, BonusAttributeType.DX, BonusAttributeType.IQ, BonusAttributeType.HT, BonusAttributeType.WILL, BonusAttributeType.PERCEPTION};
    /** The XML tag for this class. */
    public static final  String               TAG_ROOT                = "attribute_prereq";
    private static final String               ATTRIBUTE_WHICH         = "which";
    private static final String               ATTRIBUTE_COMBINED_WITH = "combined_with";
    private static final String               ATTRIBUTE_COMPARE       = "compare";
    private static final String               KEY_QUALIFIER           = "qualifier";
    private              BonusAttributeType   mWhich;
    private              BonusAttributeType   mCombinedWith;
    private              IntegerCriteria      mValueCompare;

    /**
     * Creates a new prerequisite.
     *
     * @param parent The owning prerequisite list, if any.
     */
    public AttributePrereq(PrereqList parent) {
        super(parent);
        mValueCompare = new IntegerCriteria(NumericCompareType.AT_LEAST, 10);
        setWhich(BonusAttributeType.IQ);
        setCombinedWith(null);
    }

    /**
     * Loads a prerequisite.
     *
     * @param parent The owning prerequisite list, if any.
     * @param reader The XML reader to load from.
     */
    public AttributePrereq(PrereqList parent, XMLReader reader) throws IOException {
        this(parent);
        loadHasAttribute(reader);
        setWhich(Enums.extract(reader.getAttribute(ATTRIBUTE_WHICH), TYPES, BonusAttributeType.ST));
        setCombinedWith(Enums.extract(reader.getAttribute(ATTRIBUTE_COMBINED_WITH), TYPES));
        mValueCompare.setType(Enums.extract(reader.getAttribute(ATTRIBUTE_COMPARE), NumericCompareType.values(), NumericCompareType.AT_LEAST));
        mValueCompare.setQualifier(reader.readInteger(10));
    }

    /**
     * Creates a copy of the specified prerequisite.
     *
     * @param parent The owning prerequisite list, if any.
     * @param prereq The prerequisite to clone.
     */
    protected AttributePrereq(PrereqList parent, AttributePrereq prereq) {
        super(parent, prereq);
        mWhich = prereq.mWhich;
        mCombinedWith = prereq.mCombinedWith;
        mValueCompare = new IntegerCriteria(prereq.mValueCompare);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof AttributePrereq && super.equals(obj)) {
            AttributePrereq ap = (AttributePrereq) obj;
            return mWhich == ap.mWhich && mCombinedWith == ap.mCombinedWith && mValueCompare.equals(ap.mValueCompare);
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
        return new AttributePrereq(parent, this);
    }

    @Override
    public void saveSelf(JsonWriter w) throws IOException {
        super.saveSelf(w);
        w.keyValue(ATTRIBUTE_WHICH, Enums.toId(mWhich));
        if (mCombinedWith != null) {
            w.keyValue(ATTRIBUTE_COMBINED_WITH, Enums.toId(mCombinedWith));
        }
        mValueCompare.save(w, KEY_QUALIFIER);
    }

    /** @return The type of comparison to make. */
    public BonusAttributeType getWhich() {
        return mWhich;
    }

    /** @param which The type of comparison to make. */
    public void setWhich(BonusAttributeType which) {
        mWhich = which;
    }

    /** @return The type of comparison to make. */
    public BonusAttributeType getCombinedWith() {
        return mCombinedWith;
    }

    /** @param which The type of comparison to make. */
    public void setCombinedWith(BonusAttributeType which) {
        mCombinedWith = which;
    }

    private static int getAttributeValue(GURPSCharacter character, BonusAttributeType attribute) {
        if (attribute == null) {
            return 0;
        }
        return switch (attribute) {
            case ST -> character.getStrength();
            case DX -> character.getDexterity();
            case IQ -> character.getIntelligence();
            case HT -> character.getHealth();
            case WILL -> character.getWillAdj();
            case PERCEPTION -> character.getPerAdj();
            default -> 0;
        };
    }

    /** @return The value comparison object. */
    public IntegerCriteria getValueCompare() {
        return mValueCompare;
    }
}
