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

import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.datafile.DataFile;
import com.trollworks.gcs.datafile.LoadState;
import com.trollworks.gcs.feature.AttributeBonus;
import com.trollworks.gcs.feature.ContainedWeightReduction;
import com.trollworks.gcs.feature.CostReduction;
import com.trollworks.gcs.feature.DRBonus;
import com.trollworks.gcs.feature.Feature;
import com.trollworks.gcs.feature.ReactionBonus;
import com.trollworks.gcs.feature.SkillBonus;
import com.trollworks.gcs.feature.SpellBonus;
import com.trollworks.gcs.feature.WeaponBonus;
import com.trollworks.gcs.prereq.PrereqList;
import com.trollworks.gcs.skill.SkillDefault;
import com.trollworks.gcs.skill.Technique;
import com.trollworks.gcs.utility.FilteredList;
import com.trollworks.gcs.utility.SaveType;
import com.trollworks.gcs.utility.VersionException;
import com.trollworks.gcs.utility.json.JsonWriter;
import com.trollworks.gcs.utility.xml.XMLNodeType;
import com.trollworks.gcs.utility.xml.XMLReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/** A common row super-class for the model. */
public abstract class ListRow extends Row {
    private static final String             ATTRIBUTE_OPEN    = "open";
    private static final String             TAG_NOTES         = "notes";
    private static final String             TAG_CATEGORIES    = "categories";
    private static final String             TAG_CATEGORY      = "category";
    private static final String             KEY_ID            = "id";
    private static final String             KEY_BASED_ON_ID   = "based_on_id";
    private static final String             KEY_BASED_ON_HASH = "based_on_hash";
    private static final String             KEY_FEATURES      = "features";
    private static final String             KEY_DEFAULTS      = "defaults";
    private static final String             KEY_CHILDREN      = "children";
    private static final String             KEY_PREREQS       = "prereqs";
    /** The data file the row is associated with. */
    protected            DataFile           mDataFile;
    private              UUID               mID;
    private              UUID               mBasedOnID;
    private              String             mBasedOnHash;
    private              List<Feature>      mFeatures;
    private              PrereqList         mPrereqList;
    private              List<SkillDefault> mDefaults;
    private              String             mNotes;
    private              TreeSet<String>    mCategories;

    public static void saveList(JsonWriter w, String key, List<?> list, SaveType saveType) throws IOException {
        FilteredList<ListRow> rows = new FilteredList<>(list, ListRow.class, true);
        if (!rows.isEmpty()) {
            w.key(key);
            w.startArray();
            for (ListRow row : rows) {
                row.save(w, saveType);
            }
            w.endArray();
        }
    }

    /**
     * Extracts any "nameable" portions of the buffer and puts their keys into the provided set.
     *
     * @param set    The set to add the nameable keys to.
     * @param buffer The text to check for nameable portions.
     */
    public static void extractNameables(Set<String> set, String buffer) {
        int first = buffer.indexOf('@');
        int last  = buffer.indexOf('@', first + 1);

        while (first != -1 && last != -1) {
            set.add(buffer.substring(first + 1, last));
            first = buffer.indexOf('@', last + 1);
            last = buffer.indexOf('@', first + 1);
        }
    }

    /**
     * Names any "nameable" portions of the data and returns the resulting string.
     *
     * @param map  The map of nameable keys to names.
     * @param data The data to change.
     * @return The revised string.
     */
    public static String nameNameables(Map<String, String> map, String data) {
        int           first  = data.indexOf('@');
        int           last   = data.indexOf('@', first + 1);
        StringBuilder buffer = new StringBuilder();

        while (first != -1 && last != -1) {
            String key         = data.substring(first + 1, last);
            String replacement = map.get(key);

            if (first != 0) {
                buffer.append(data, 0, first);
            }
            if (replacement != null) {
                buffer.append(replacement);
            } else {
                buffer.append('@');
                buffer.append(key);
                buffer.append('@');
            }
            data = last + 1 == data.length() ? "" : data.substring(last + 1);
            first = data.indexOf('@');
            last = data.indexOf('@', first + 1);
        }
        buffer.append(data);
        return buffer.toString();
    }

    public static Set<String> createCategoriesList(String categories) {
        return new TreeSet<>(createList(categories));
    }

    // This is the decompose method that works with the compose method (getCategoriesAsString())
    private static Collection<String> createList(String categories) {
        return Arrays.asList(categories.split(","));
    }

    /**
     * Creates a new data row.
     *
     * @param dataFile    The data file to associate it with.
     * @param isContainer Whether or not this row allows children.
     */
    public ListRow(DataFile dataFile, boolean isContainer) {
        setCanHaveChildren(isContainer);
        setOpen(isContainer);
        mDataFile = dataFile;
        mID = UUID.randomUUID();
        mFeatures = new ArrayList<>();
        mPrereqList = new PrereqList(null, true);
        mDefaults = new ArrayList<>();
        mNotes = "";
        mCategories = new TreeSet<>();
    }

    /**
     * Creates a clone of an existing data row and associates it with the specified data file.
     *
     * @param dataFile   The data file to associate it with.
     * @param rowToClone The data row to clone.
     */
    public ListRow(DataFile dataFile, ListRow rowToClone) {
        this(dataFile, rowToClone.canHaveChildren());
        setOpen(rowToClone.isOpen());
        mNotes = rowToClone.mNotes;
        for (Feature feature : rowToClone.mFeatures) {
            mFeatures.add(feature.cloneFeature());
        }
        mPrereqList = new PrereqList(null, rowToClone.getPrereqs());
        mDefaults = new ArrayList<>();
        for (SkillDefault skillDefault : rowToClone.mDefaults) {
            mDefaults.add(new SkillDefault(skillDefault));
        }
        mCategories = new TreeSet<>(rowToClone.mCategories);
        try {
            MessageDigest         digest = MessageDigest.getInstance("SHA3-256");
            StringBuilder         buffer = new StringBuilder();
            ByteArrayOutputStream baos   = new ByteArrayOutputStream();
            try (JsonWriter w = new JsonWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8), "")) {
                rowToClone.save(w, SaveType.HASH);
            }
            mBasedOnHash = Base64.getEncoder().withoutPadding().encodeToString(digest.digest(baos.toByteArray()));
            mBasedOnID = rowToClone.mID;
        } catch (Exception exception) {
            mBasedOnID = null;
            mBasedOnHash = null;
        }
    }

    /**
     * @param obj The other object to compare against.
     * @return Whether or not this {@link ListRow} is equivalent.
     */
    public boolean isEquivalentTo(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ListRow) {
            ListRow row = (ListRow) obj;
            if (mNotes.equals(row.mNotes) && mCategories.equals(row.mCategories)) {
                if (mDefaults.equals(row.mDefaults)) {
                    if (mPrereqList.equals(row.mPrereqList)) {
                        if (mFeatures.equals(row.mFeatures)) {
                            int childCount = getChildCount();
                            if (childCount == row.getChildCount()) {
                                for (int i = 0; i < childCount; i++) {
                                    if (!((ListRow) getChild(i)).isEquivalentTo(row.getChild(i))) {
                                        return false;
                                    }
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /** @return The localized name for this row object. */
    public abstract String getLocalizedName();

    /** @return The ID for the "list changed" notification. */
    public abstract String getListChangedID();

    /** @return The type name to use for this data. */
    public abstract String getJSONTypeName();

    /** @return The XML root container tag name for this particular row. */
    public abstract String getXMLTagName();

    /** @return The most recent version of the XML tag this object knows how to load. */
    public abstract int getXMLTagVersion();

    /** @return The type of row. */
    public abstract String getRowType();

    /**
     * Loads this row's contents.
     *
     * @param reader The XML reader to load from.
     * @param state  The {@link LoadState} to use.
     */
    public final void load(XMLReader reader, LoadState state) throws IOException {
        String marker = reader.getMarker();
        state.mDataItemVersion = reader.getAttributeAsInteger(LoadState.ATTRIBUTE_VERSION, 0);
        if (state.mDataItemVersion > getXMLTagVersion()) {
            throw VersionException.createTooNew();
        }
        boolean isContainer = reader.getName().endsWith("_container");
        setCanHaveChildren(isContainer);
        setOpen(isContainer);
        prepareForLoad(state);
        loadAttributes(reader, state);
        do {
            if (reader.next() == XMLNodeType.START_TAG) {
                String name = reader.getName();
                if (AttributeBonus.TAG_ROOT.equals(name)) {
                    mFeatures.add(new AttributeBonus(reader));
                } else if (DRBonus.TAG_ROOT.equals(name)) {
                    mFeatures.add(new DRBonus(reader));
                } else if (ReactionBonus.TAG_ROOT.equals(name)) {
                    mFeatures.add(new ReactionBonus(reader));
                } else if (SkillBonus.TAG_ROOT.equals(name)) {
                    mFeatures.add(new SkillBonus(reader));
                } else if (SpellBonus.TAG_ROOT.equals(name)) {
                    mFeatures.add(new SpellBonus(reader));
                } else if (WeaponBonus.TAG_ROOT.equals(name)) {
                    mFeatures.add(new WeaponBonus(reader));
                } else if (CostReduction.TAG_ROOT.equals(name)) {
                    mFeatures.add(new CostReduction(reader));
                } else if (ContainedWeightReduction.TAG_ROOT.equals(name)) {
                    mFeatures.add(new ContainedWeightReduction(reader));
                } else if (PrereqList.TAG_ROOT.equals(name)) {
                    mPrereqList = new PrereqList(null, mDataFile.defaultWeightUnits(), reader);
                } else if (!(this instanceof Technique) && SkillDefault.TAG_ROOT.equals(name)) {
                    mDefaults.add(new SkillDefault(reader));
                } else if (TAG_NOTES.equals(name)) {
                    mNotes = reader.readText();
                } else if (TAG_CATEGORIES.equals(name)) {
                    String subMarker = reader.getMarker();
                    do {
                        if (reader.next() == XMLNodeType.START_TAG) {
                            name = reader.getName();
                            if (TAG_CATEGORY.equals(name)) {
                                mCategories.add(reader.readText());
                            } else {
                                reader.skipTag(name);
                            }
                        }
                    } while (reader.withinMarker(subMarker));
                } else {
                    loadSubElement(reader, state);
                }
            }
        } while (reader.withinMarker(marker));
        finishedLoading(state);
    }

    /**
     * Called to prepare the row for loading.
     *
     * @param state The {@link LoadState} to use.
     */
    protected void prepareForLoad(LoadState state) {
        mNotes = "";
        mFeatures.clear();
        mDefaults.clear();
        mPrereqList = new PrereqList(null, true);
        mCategories.clear();
    }

    /**
     * Loads this row's custom attributes from the specified element.
     *
     * @param reader The XML reader to load from.
     * @param state  The {@link LoadState} to use.
     */
    protected void loadAttributes(XMLReader reader, LoadState state) {
        if (canHaveChildren()) {
            setOpen(reader.isAttributeSet(ATTRIBUTE_OPEN));
        }
    }

    /**
     * Loads this row's custom data from the specified element.
     *
     * @param reader The XML reader to load from.
     * @param state  The {@link LoadState} to use.
     */
    @SuppressWarnings("static-method")
    protected void loadSubElement(XMLReader reader, LoadState state) throws IOException {
        reader.skipTag(reader.getName());
    }

    /**
     * Called when loading of this row is complete. Does nothing by default.
     *
     * @param state The {@link LoadState} to use.
     */
    protected void finishedLoading(LoadState state) {
        // Nothing to do.
    }

    /**
     * Saves the row.
     *
     * @param w        The {@link JsonWriter} to use.
     * @param saveType The type of save being performed.
     */
    public void save(JsonWriter w, SaveType saveType) throws IOException {
        w.startMap();
        w.keyValue(DataFile.KEY_TYPE, getJSONTypeName());
        w.keyValue(LoadState.ATTRIBUTE_VERSION, 1);
        w.keyValue(KEY_ID, mID.toString());
        if (mBasedOnID != null) {
            w.keyValue(KEY_BASED_ON_ID, mBasedOnID.toString());
            w.keyValue(KEY_BASED_ON_HASH, mBasedOnHash);
        }
        saveSelf(w, saveType);
        if (!mPrereqList.isEmpty()) {
            w.key(KEY_PREREQS);
            mPrereqList.save(w);
        }
        if (!(this instanceof Technique) && !mDefaults.isEmpty()) {
            w.key(KEY_DEFAULTS);
            w.startArray();
            for (SkillDefault skillDefault : mDefaults) {
                skillDefault.save(w, false);
            }
            w.endArray();
        }
        if (!mFeatures.isEmpty()) {
            w.key(KEY_FEATURES);
            w.startArray();
            for (Feature feature : mFeatures) {
                feature.save(w);
            }
            w.endArray();
        }
        w.keyValueNot(TAG_NOTES, mNotes, "");
        if (!mCategories.isEmpty()) {
            w.key(TAG_CATEGORIES);
            w.startArray();
            for (String category : mCategories) {
                w.value(category);
            }
            w.endArray();
        }
        if (canHaveChildren()) {
            if (saveType != SaveType.HASH) {
                w.keyValue(ATTRIBUTE_OPEN, isOpen());
            }
            if (saveType != SaveType.UNDO) {
                saveList(w, KEY_CHILDREN, getChildren(), saveType);
            }
        }
        w.endMap();
    }

    /**
     * Saves the row.
     *
     * @param w        The {@link JsonWriter} to use.
     * @param saveType The type of save being performed.
     */
    protected abstract void saveSelf(JsonWriter w, SaveType saveType) throws IOException;

    /** Called to update any information that relies on children. */
    public void update() {
        // Do nothing by default.
    }

    /** @return The owning data file. */
    public DataFile getDataFile() {
        return mDataFile;
    }

    /** @return The owning character. */
    public GURPSCharacter getCharacter() {
        return mDataFile instanceof GURPSCharacter ? (GURPSCharacter) mDataFile : null;
    }

    /** @return The features provided by this data row. */
    public List<Feature> getFeatures() {
        return Collections.unmodifiableList(mFeatures);
    }

    /** @return The categories this data row belongs to. */
    public Set<String> getCategories() {
        return Collections.unmodifiableSet(mCategories);
    }

    /** @return The prerequisites needed by this data row. */
    public PrereqList getPrereqs() {
        return mPrereqList;
    }

    /** @return The defaults for this row. */
    public List<SkillDefault> getDefaults() {
        return Collections.unmodifiableList(mDefaults);
    }

    /**
     * @param text          The text to search for.
     * @param lowerCaseOnly The passed in text is all lowercase.
     * @return {@code true} if this row contains the text.
     */
    @SuppressWarnings("static-method")
    public boolean contains(String text, boolean lowerCaseOnly) {
        return false;
    }

    /** @return The notes. */
    public String getNotes() {
        return mNotes;
    }
}
