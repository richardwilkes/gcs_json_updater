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
import com.trollworks.gcs.utility.units.WeightUnits;
import com.trollworks.gcs.utility.xml.XMLNodeType;
import com.trollworks.gcs.utility.xml.XMLReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** A prerequisite list. */
public class PrereqList extends Prereq {
    /** The XML tag used for the prereq list. */
    public static final  String          TAG_ROOT      = "prereq_list";
    private static final String          TAG_WHEN_TL   = "when_tl";
    private static final String          ATTRIBUTE_ALL = "all";
    private static final String          KEY_PREREQS = "prereqs";
    private              boolean         mAll;
    private              IntegerCriteria mWhenTLCriteria;
    private              List<Prereq>    mPrereqs;

    /**
     * Creates a new prerequisite list.
     *
     * @param parent The owning prerequisite list, if any.
     * @param all    Whether only one criteria in this list has to be met, or all of them must be
     *               met.
     */
    public PrereqList(PrereqList parent, boolean all) {
        super(parent);
        mAll = all;
        mWhenTLCriteria = new IntegerCriteria(NumericCompareType.AT_LEAST, Integer.MIN_VALUE);
        mPrereqs = new ArrayList<>();
    }

    /**
     * Loads a prerequisite list.
     *
     * @param parent The owning prerequisite list, if any.
     * @param reader The XML reader to load from.
     */
    public PrereqList(PrereqList parent, WeightUnits defWeightUnits, XMLReader reader) throws IOException {
        this(parent, true);
        String marker = reader.getMarker();
        mAll = reader.isAttributeSet(ATTRIBUTE_ALL);
        do {
            if (reader.next() == XMLNodeType.START_TAG) {
                String name = reader.getName();
                if (TAG_WHEN_TL.equals(name)) {
                    mWhenTLCriteria.load(reader);
                } else if (TAG_ROOT.equals(name)) {
                    mPrereqs.add(new PrereqList(this, defWeightUnits, reader));
                } else if (AdvantagePrereq.TAG_ROOT.equals(name)) {
                    mPrereqs.add(new AdvantagePrereq(this, reader));
                } else if (AttributePrereq.TAG_ROOT.equals(name)) {
                    mPrereqs.add(new AttributePrereq(this, reader));
                } else if (ContainedWeightPrereq.TAG_ROOT.equals(name)) {
                    mPrereqs.add(new ContainedWeightPrereq(this, defWeightUnits, reader));
                } else if (ContainedQuantityPrereq.TAG_ROOT.equals(name)) {
                    mPrereqs.add(new ContainedQuantityPrereq(this, reader));
                } else if (SkillPrereq.TAG_ROOT.equals(name)) {
                    mPrereqs.add(new SkillPrereq(this, reader));
                } else if (SpellPrereq.TAG_ROOT.equals(name)) {
                    mPrereqs.add(new SpellPrereq(this, reader));
                } else {
                    reader.skipTag(name);
                }
            }
        } while (reader.withinMarker(marker));
    }

    /**
     * Creates a clone of the specified prerequisite list.
     *
     * @param parent     The new owning prerequisite list, if any.
     * @param prereqList The prerequisite to clone.
     */
    public PrereqList(PrereqList parent, PrereqList prereqList) {
        super(parent);
        mAll = prereqList.mAll;
        mWhenTLCriteria = new IntegerCriteria(prereqList.mWhenTLCriteria);
        mPrereqs = new ArrayList<>(prereqList.mPrereqs.size());
        for (Prereq prereq : prereqList.mPrereqs) {
            mPrereqs.add(prereq.clone(this));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof PrereqList) {
            PrereqList list = (PrereqList) obj;
            return mAll == list.mAll && mWhenTLCriteria.equals(list.mWhenTLCriteria) && mPrereqs.equals(list.mPrereqs);
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
    public void saveSelf(JsonWriter w) throws IOException {
        w.keyValue(ATTRIBUTE_ALL, mAll);
        if (isWhenTLEnabled(mWhenTLCriteria)) {
            mWhenTLCriteria.save(w, TAG_WHEN_TL);
        }
        if (!mPrereqs.isEmpty()) {
            w.key(KEY_PREREQS);
            w.startArray();
            for (Prereq prereq : mPrereqs) {
                prereq.save(w);
            }
            w.endArray();
        }
    }

    public boolean isEmpty() {
        return mPrereqs.isEmpty();
    }

    /** @return The character's TL criteria. */
    public IntegerCriteria getWhenTLCriteria() {
        return mWhenTLCriteria;
    }

    /**
     * @param criteria The {@link IntegerCriteria} to check.
     * @return Whether the character's TL criteria check is enabled.
     */
    public static boolean isWhenTLEnabled(IntegerCriteria criteria) {
        return criteria.getType() != NumericCompareType.AT_LEAST || criteria.getQualifier() != Integer.MIN_VALUE;
    }

    /**
     * @param criteria The {@link IntegerCriteria} to work on.
     * @param enabled  Whether the character's TL criteria check is enabled.
     */
    public static void setWhenTLEnabled(IntegerCriteria criteria, boolean enabled) {
        if (isWhenTLEnabled(criteria) != enabled) {
            criteria.setQualifier(enabled ? 0 : Integer.MIN_VALUE);
        }
    }

    /** @return Whether only one criteria in this list has to be met, or all of them must be met. */
    public boolean requiresAll() {
        return mAll;
    }

    /**
     * @param requiresAll Whether only one criteria in this list has to be met, or all of them must
     *                    be met.
     */
    public void setRequiresAll(boolean requiresAll) {
        mAll = requiresAll;
    }

    /**
     * @param prereq The prerequisite to work on.
     * @return The index of the specified prerequisite. -1 will be returned if the component isn't a
     *         direct child.
     */
    public int getIndexOf(Prereq prereq) {
        return mPrereqs.indexOf(prereq);
    }

    /** @return The number of children in this list. */
    public int getChildCount() {
        return mPrereqs.size();
    }

    /** @return The children of this list. */
    public List<Prereq> getChildren() {
        return Collections.unmodifiableList(mPrereqs);
    }

    /**
     * Adds the specified prerequisite to this list.
     *
     * @param index  The index to add the list at.
     * @param prereq The prerequisite to add.
     */
    public void add(int index, Prereq prereq) {
        mPrereqs.add(index, prereq);
    }

    /**
     * Removes the specified prerequisite from this list.
     *
     * @param prereq The prerequisite to remove.
     */
    public void remove(Prereq prereq) {
        if (mPrereqs.contains(prereq)) {
            mPrereqs.remove(prereq);
            prereq.mParent = null;
        }
    }

    @Override
    public Prereq clone(PrereqList parent) {
        return new PrereqList(parent, this);
    }

    @Override
    public void fillWithNameableKeys(Set<String> set) {
        for (Prereq prereq : mPrereqs) {
            prereq.fillWithNameableKeys(set);
        }
    }

    @Override
    public void applyNameableKeys(Map<String, String> map) {
        for (Prereq prereq : mPrereqs) {
            prereq.applyNameableKeys(map);
        }
    }
}
