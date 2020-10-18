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

package com.trollworks.gcs.advantage;

import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.datafile.DataFile;
import com.trollworks.gcs.datafile.LoadState;
import com.trollworks.gcs.modifier.AdvantageModifier;
import com.trollworks.gcs.modifier.Affects;
import com.trollworks.gcs.ui.widget.outline.ListRow;
import com.trollworks.gcs.ui.widget.outline.Row;
import com.trollworks.gcs.ui.widget.outline.Switchable;
import com.trollworks.gcs.utility.FilteredIterator;
import com.trollworks.gcs.utility.Fixed6;
import com.trollworks.gcs.utility.SaveType;
import com.trollworks.gcs.utility.json.JsonWriter;
import com.trollworks.gcs.utility.text.Enums;
import com.trollworks.gcs.utility.text.Text;
import com.trollworks.gcs.utility.xml.XMLReader;
import com.trollworks.gcs.weapon.MeleeWeaponStats;
import com.trollworks.gcs.weapon.RangedWeaponStats;
import com.trollworks.gcs.weapon.WeaponStats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** A GURPS Advantage. */
public class Advantage extends ListRow implements Switchable {
    private static final int                        CURRENT_VERSION            = 4;
    /** The XML tag used for items. */
    public static final  String                     TAG_ADVANTAGE              = "advantage";
    /** The XML tag used for containers. */
    public static final  String                     TAG_ADVANTAGE_CONTAINER    = "advantage_container";
    private static final String                     TAG_REFERENCE              = "reference";
    private static final String                     TAG_BASE_POINTS            = "base_points";
    private static final String                     TAG_POINTS_PER_LEVEL       = "points_per_level";
    private static final String                     TAG_LEVELS                 = "levels";
    private static final String                     TAG_TYPE                   = "type";
    private static final String                     TAG_NAME                   = "name";
    private static final String                     TAG_CR                     = "cr";
    private static final String                     TAG_USER_DESC              = "userdesc";
    private static final String                     TYPE_MENTAL                = "Mental";
    private static final String                     TYPE_PHYSICAL              = "Physical";
    private static final String                     TYPE_SOCIAL                = "Social";
    private static final String                     TYPE_EXOTIC                = "Exotic";
    private static final String                     TYPE_SUPERNATURAL          = "Supernatural";
    private static final String                     ATTR_DISABLED              = "disabled";
    private static final String                     ATTR_ROUND_COST_DOWN       = "round_down";
    private static final String                     ATTR_ALLOW_HALF_LEVELS     = "allow_half_levels";
    private static final String                     ATTR_HALF_LEVEL            = "half_level";
    private static final String                     KEY_CONTAINER_TYPE         = "container_type";
    private static final String                     KEY_WEAPONS                = "weapons";
    private static final String                     KEY_MODIFIERS              = "modifiers";
    private static final String                     KEY_CR_ADJ                 = "cr_adj";
    private static final String                     KEY_MENTAL                 = "mental";
    private static final String                     KEY_PHYSICAL               = "physical";
    private static final String                     KEY_SOCIAL                 = "social";
    private static final String                     KEY_EXOTIC                 = "exotic";
    private static final String                     KEY_SUPERNATURAL           = "supernatural";
    /** The prefix used in front of all IDs for the advantages. */
    public static final  String                     PREFIX                     = GURPSCharacter.CHARACTER_PREFIX + "advantage.";
    /** The field ID for type changes. */
    public static final  String                     ID_TYPE                    = PREFIX + "Type";
    /** The field ID for container type changes. */
    public static final  String                     ID_CONTAINER_TYPE          = PREFIX + "ContainerType";
    /** The field ID for name changes. */
    public static final  String                     ID_NAME                    = PREFIX + "Name";
    /** The field ID for CR changes. */
    public static final  String                     ID_CR                      = PREFIX + "CR";
    /** The field ID for level changes. */
    public static final  String                     ID_LEVELS                  = PREFIX + "Levels";
    /** The field ID for half level. */
    public static final  String                     ID_HALF_LEVEL              = PREFIX + "HalfLevel";
    /** The field ID for round cost down changes. */
    public static final  String                     ID_ROUND_COST_DOWN         = PREFIX + "RoundCostDown";
    /** The field ID for disabled changes. */
    public static final  String                     ID_DISABLED                = PREFIX + "Disabled";
    /** The field ID for allowing half levels. */
    public static final  String                     ID_ALLOW_HALF_LEVELS       = PREFIX + "AllowHalfLevels";
    /** The field ID for point changes. */
    public static final  String                     ID_POINTS                  = PREFIX + "Points";
    /** The field ID for page reference changes. */
    public static final  String                     ID_REFERENCE               = PREFIX + "Reference";
    /** The field ID for when the row hierarchy changes. */
    public static final  String                     ID_LIST_CHANGED            = PREFIX + "ListChanged";
    /** The field ID for when the advantage becomes or stops being a weapon. */
    public static final  String                     ID_WEAPON_STATUS_CHANGED   = PREFIX + "WeaponStatus";
    /** The field ID for when the advantage gets Modifiers. */
    public static final  String                     ID_MODIFIER_STATUS_CHANGED = PREFIX + "Modifier";
    /** The field ID for user description changes. */
    public static final  String                     ID_USER_DESC               = PREFIX + "UserDesc";
    /** The type mask for mental advantages. */
    public static final  int                        TYPE_MASK_MENTAL           = 1 << 0;
    /** The type mask for physical advantages. */
    public static final  int                        TYPE_MASK_PHYSICAL         = 1 << 1;
    /** The type mask for social advantages. */
    public static final  int                        TYPE_MASK_SOCIAL           = 1 << 2;
    /** The type mask for exotic advantages. */
    public static final  int                        TYPE_MASK_EXOTIC           = 1 << 3;
    /** The type mask for supernatural advantages. */
    public static final  int                        TYPE_MASK_SUPERNATURAL     = 1 << 4;
    private              int                        mType;
    private              String                     mName;
    private              SelfControlRoll            mCR;
    private              SelfControlRollAdjustments mCRAdj;
    private              int                        mLevels;
    private              boolean                    mAllowHalfLevels;
    private              boolean                    mHalfLevel;
    private              int                        mPoints;
    private              int                        mPointsPerLevel;
    private              String                     mReference;
    private              AdvantageContainerType     mContainerType;
    private              List<WeaponStats>          mWeapons;
    private              List<AdvantageModifier>    mModifiers;
    private              boolean                    mRoundCostDown;
    private              boolean                    mDisabled;
    private              String                     mUserDesc;

    /**
     * Creates a new advantage.
     *
     * @param dataFile    The data file to associate it with.
     * @param isContainer Whether or not this row allows children.
     */
    public Advantage(DataFile dataFile, boolean isContainer) {
        super(dataFile, isContainer);
        mType = TYPE_MASK_PHYSICAL;
        mName = "Advantage";
        mCR = SelfControlRoll.NONE_REQUIRED;
        mCRAdj = SelfControlRollAdjustments.NONE;
        mLevels = -1;
        mReference = "";
        mContainerType = AdvantageContainerType.GROUP;
        mWeapons = new ArrayList<>();
        mModifiers = new ArrayList<>();
        mUserDesc = "";
    }

    /**
     * Creates a clone of an existing advantage and associates it with the specified data file.
     *
     * @param dataFile  The data file to associate it with.
     * @param advantage The advantage to clone.
     * @param deep      Whether or not to clone the children, grandchildren, etc.
     */
    public Advantage(DataFile dataFile, Advantage advantage, boolean deep) {
        super(dataFile, advantage);
        mType = advantage.mType;
        mName = advantage.mName;
        mCR = advantage.mCR;
        mCRAdj = advantage.mCRAdj;
        mLevels = advantage.mLevels;
        mHalfLevel = advantage.mHalfLevel;
        mAllowHalfLevels = advantage.mAllowHalfLevels;
        mPoints = advantage.mPoints;
        mPointsPerLevel = advantage.mPointsPerLevel;
        mRoundCostDown = advantage.mRoundCostDown;
        mDisabled = advantage.mDisabled;
        mReference = advantage.mReference;
        mContainerType = advantage.mContainerType;
        mUserDesc = dataFile instanceof GURPSCharacter ? advantage.mUserDesc : "";
        mWeapons = new ArrayList<>(advantage.mWeapons.size());
        for (WeaponStats weapon : advantage.mWeapons) {
            if (weapon instanceof MeleeWeaponStats) {
                mWeapons.add(new MeleeWeaponStats(this, (MeleeWeaponStats) weapon));
            } else if (weapon instanceof RangedWeaponStats) {
                mWeapons.add(new RangedWeaponStats(this, (RangedWeaponStats) weapon));
            }
        }
        mModifiers = new ArrayList<>(advantage.mModifiers.size());
        for (AdvantageModifier modifier : advantage.mModifiers) {
            mModifiers.add(new AdvantageModifier(mDataFile, modifier, false));
        }
        if (deep) {
            int count = advantage.getChildCount();

            for (int i = 0; i < count; i++) {
                addChild(new Advantage(dataFile, (Advantage) advantage.getChild(i), true));
            }
        }
    }

    /**
     * Loads an advantage and associates it with the specified data file.
     *
     * @param dataFile The data file to associate it with.
     * @param reader   The XML reader to load from.
     * @param state    The {@link LoadState} to use.
     */
    public Advantage(DataFile dataFile, XMLReader reader, LoadState state) throws IOException {
        this(dataFile, TAG_ADVANTAGE_CONTAINER.equals(reader.getName()));
        load(reader, state);
    }

    @Override
    public boolean isEquivalentTo(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Advantage && super.isEquivalentTo(obj)) {
            Advantage row = (Advantage) obj;
            if (mType == row.mType && mLevels == row.mLevels && mHalfLevel == row.mHalfLevel && mPoints == row.mPoints && mPointsPerLevel == row.mPointsPerLevel && mDisabled == row.mDisabled && mRoundCostDown == row.mRoundCostDown && mAllowHalfLevels == row.mAllowHalfLevels && mContainerType == row.mContainerType && mCR == row.mCR && mCRAdj == row.mCRAdj && mName.equals(row.mName) && mReference.equals(row.mReference)) {
                if (mWeapons.equals(row.mWeapons)) {
                    return mModifiers.equals(row.mModifiers);
                }
            }
        }
        return false;
    }

    @Override
    public String getListChangedID() {
        return ID_LIST_CHANGED;
    }

    @Override
    public String getRowType() {
        return "Advantage";
    }

    @Override
    public String getJSONTypeName() {
        return canHaveChildren() ? TAG_ADVANTAGE_CONTAINER : TAG_ADVANTAGE;
    }

    @Override
    public String getXMLTagName() {
        return canHaveChildren() ? TAG_ADVANTAGE_CONTAINER : TAG_ADVANTAGE;
    }

    @Override
    public int getXMLTagVersion() {
        return CURRENT_VERSION;
    }

    @Override
    protected void prepareForLoad(LoadState state) {
        super.prepareForLoad(state);
        mType = TYPE_MASK_PHYSICAL;
        mName = "Advantage";
        mCR = SelfControlRoll.NONE_REQUIRED;
        mCRAdj = SelfControlRollAdjustments.NONE;
        mLevels = -1;
        mHalfLevel = false;
        mAllowHalfLevels = false;
        mReference = "";
        mContainerType = AdvantageContainerType.GROUP;
        mPoints = 0;
        mPointsPerLevel = 0;
        mRoundCostDown = false;
        mDisabled = false;
        mWeapons = new ArrayList<>();
        mModifiers = new ArrayList<>();
        mUserDesc = "";
    }

    @Override
    protected void loadAttributes(XMLReader reader, LoadState state) {
        super.loadAttributes(reader, state);
        mRoundCostDown = reader.isAttributeSet(ATTR_ROUND_COST_DOWN);
        mDisabled = reader.isAttributeSet(ATTR_DISABLED);
        mAllowHalfLevels = reader.isAttributeSet(ATTR_ALLOW_HALF_LEVELS);
        if (canHaveChildren()) {
            mContainerType = Enums.extract(reader.getAttribute(TAG_TYPE), AdvantageContainerType.values(), AdvantageContainerType.GROUP);
        }
    }

    @Override
    protected void loadSubElement(XMLReader reader, LoadState state) throws IOException {
        String name = reader.getName();
        if (TAG_NAME.equals(name)) {
            mName = reader.readText().replace("\n", " ");
        } else if (TAG_CR.equals(name)) {
            mCRAdj = Enums.extract(reader.getAttribute(SelfControlRoll.ATTR_ADJUSTMENT), SelfControlRollAdjustments.values(), SelfControlRollAdjustments.NONE);
            mCR = SelfControlRoll.get(reader.readText());
        } else if (TAG_REFERENCE.equals(name)) {
            mReference = reader.readText().replace("\n", " ");
        } else if (!state.mForUndo && (TAG_ADVANTAGE.equals(name) || TAG_ADVANTAGE_CONTAINER.equals(name))) {
            addChild(new Advantage(mDataFile, reader, state));
        } else if (AdvantageModifier.TAG_MODIFIER.equals(name)) {
            mModifiers.add(new AdvantageModifier(getDataFile(), reader, state));
        } else if (TAG_USER_DESC.equals(name)) {
            if (getDataFile() instanceof GURPSCharacter) {
                mUserDesc = Text.standardizeLineEndings(reader.readText());
            }
        } else if (!canHaveChildren()) {
            if (TAG_TYPE.equals(name)) {
                mType = getTypeFromText(reader.readText());
            } else if (TAG_LEVELS.equals(name)) {
                // Read the attribute first as next operation clears attribute map
                mHalfLevel = mAllowHalfLevels && reader.isAttributeSet(ATTR_HALF_LEVEL);
                mLevels = reader.readInteger(-1);
            } else if (TAG_BASE_POINTS.equals(name)) {
                mPoints = reader.readInteger(0);
            } else if (TAG_POINTS_PER_LEVEL.equals(name)) {
                mPointsPerLevel = reader.readInteger(0);
            } else if (MeleeWeaponStats.TAG_ROOT.equals(name)) {
                mWeapons.add(new MeleeWeaponStats(this, reader));
            } else if (RangedWeaponStats.TAG_ROOT.equals(name)) {
                mWeapons.add(new RangedWeaponStats(this, reader));
            } else {
                super.loadSubElement(reader, state);
            }
        } else {
            super.loadSubElement(reader, state);
        }
    }

    @Override
    protected void saveSelf(JsonWriter w, SaveType saveType) throws IOException {
        w.keyValueNot(ATTR_ROUND_COST_DOWN, mRoundCostDown, false);
        w.keyValueNot(ATTR_ALLOW_HALF_LEVELS, mAllowHalfLevels, false);
        w.keyValueNot(ATTR_DISABLED, mDisabled, false);
        if (canHaveChildren() && mContainerType != AdvantageContainerType.GROUP) {
            w.keyValue(KEY_CONTAINER_TYPE, Enums.toId(mContainerType));
        }
        w.keyValue(TAG_NAME, mName);
        if (!canHaveChildren()) {
            w.keyValueNot(KEY_MENTAL, (mType & TYPE_MASK_MENTAL) != 0, false);
            w.keyValueNot(KEY_PHYSICAL, (mType & TYPE_MASK_PHYSICAL) != 0, false);
            w.keyValueNot(KEY_SOCIAL, (mType & TYPE_MASK_SOCIAL) != 0, false);
            w.keyValueNot(KEY_EXOTIC, (mType & TYPE_MASK_EXOTIC) != 0, false);
            w.keyValueNot(KEY_SUPERNATURAL, (mType & TYPE_MASK_SUPERNATURAL) != 0, false);
            if (mLevels != -1) {
                Fixed6 levels = new Fixed6(mLevels);
                if (mAllowHalfLevels && mHalfLevel) {
                    levels = levels.add(new Fixed6(0.5));
                }
                w.keyValue(TAG_LEVELS, levels.toString());
            }
            w.keyValueNot(TAG_BASE_POINTS, mPoints, 0);
            w.keyValueNot(TAG_POINTS_PER_LEVEL, mPointsPerLevel, 0);
            WeaponStats.saveList(w, KEY_WEAPONS, mWeapons);
        }
        if (mCR != SelfControlRoll.NONE_REQUIRED) {
            w.keyValue(TAG_CR, mCR.getCR());
            if (mCRAdj != SelfControlRollAdjustments.NONE) {
                w.keyValue(KEY_CR_ADJ, Enums.toId(mCRAdj));
            }
        }
        saveList(w, KEY_MODIFIERS, mModifiers, saveType);
        if (getDataFile() instanceof GURPSCharacter) {
            w.keyValueNot(TAG_USER_DESC, mUserDesc, "");
        }
        w.keyValueNot(TAG_REFERENCE, mReference, "");
    }

    /** @return The container type. */
    public AdvantageContainerType getContainerType() {
        return mContainerType;
    }

    /** @return The type. */
    public int getType() {
        return mType;
    }

    @Override
    public String getLocalizedName() {
        return "Advantage";
    }

    /** @return The name. */
    public String getName() {
        return mName;
    }

    public String getUserDesc() {
        return mUserDesc;
    }

    /** @return The CR. */
    public SelfControlRoll getCR() {
        return mCR;
    }

    /** @return The CR adjustment. */
    public SelfControlRollAdjustments getCRAdj() {
        return mCRAdj;
    }

    /** @return Whether this advantage is leveled or not. */
    public boolean isLeveled() {
        return mLevels >= 0;
    }

    /** @return The levels. */
    public int getLevels() {
        return mLevels;
    }

    /** @return The total points, taking levels into account. */
    public int getAdjustedPoints() {
        if (isDisabled()) {
            return 0;
        }
        if (canHaveChildren()) {
            int points = 0;
            if (mContainerType == AdvantageContainerType.ALTERNATIVE_ABILITIES) {
                List<Integer> values = new ArrayList<>();
                for (Advantage child : new FilteredIterator<>(getChildren(), Advantage.class)) {
                    int pts = child.getAdjustedPoints();
                    values.add(Integer.valueOf(pts));
                    if (pts > points) {
                        points = pts;
                    }
                }
                int     max   = points;
                boolean found = false;
                for (Integer one : values) {
                    int value = one.intValue();
                    if (!found && max == value) {
                        found = true;
                    } else {
                        points += applyRounding(calculateModifierPoints(value, 20), mRoundCostDown);
                    }
                }
            } else {
                for (Advantage child : new FilteredIterator<>(getChildren(), Advantage.class)) {
                    points += child.getAdjustedPoints();
                }
            }
            return points;
        }
        return getAdjustedPoints(mPoints, mLevels, mAllowHalfLevels && mHalfLevel, mPointsPerLevel, mCR, getAllModifiers(), mRoundCostDown);
    }

    private static int applyRounding(double value, boolean roundCostDown) {
        return (int) (roundCostDown ? Math.floor(value) : Math.ceil(value));
    }

    public boolean isDisabled() {
        return !isEnabled();
    }

    @Override
    public boolean isEnabled() {
        if (mDisabled) {
            return false;
        }
        Row parent = getParent();
        if (parent instanceof Switchable) {
            return ((Switchable) parent).isEnabled();
        }
        return true;
    }

    /**
     * @param basePoints     The base point cost.
     * @param levels         The number of levels.
     * @param halfLevel      Whether a half level is present.
     * @param pointsPerLevel The point cost per level.
     * @param cr             The {@link SelfControlRoll} to apply.
     * @param modifiers      The {@link AdvantageModifier}s to apply.
     * @param roundCostDown  Whether the point cost should be rounded down rather than up, as is
     *                       normal for most GURPS rules.
     * @return The total points, taking levels and modifiers into account.
     */
    public int getAdjustedPoints(int basePoints, int levels, boolean halfLevel, int pointsPerLevel, SelfControlRoll cr, Collection<AdvantageModifier> modifiers, boolean roundCostDown) {
        int    baseEnh    = 0;
        int    levelEnh   = 0;
        int    baseLim    = 0;
        int    levelLim   = 0;
        double multiplier = cr.getMultiplier();

        for (AdvantageModifier one : modifiers) {
            if (one.isEnabled()) {
                int modifier = one.getCostModifier();
                switch (one.getCostType()) {
                case PERCENTAGE:
                default:
                    switch (one.getAffects()) {
                    case TOTAL:
                    default:
                        if (modifier < 0) { // Limitation
                            baseLim += modifier;
                            levelLim += modifier;
                        } else { // Enhancement
                            baseEnh += modifier;
                            levelEnh += modifier;
                        }
                        break;
                    case BASE_ONLY:
                        if (modifier < 0) { // Limitation
                            baseLim += modifier;
                        } else { // Enhancement
                            baseEnh += modifier;
                        }
                        break;
                    case LEVELS_ONLY:
                        if (modifier < 0) { // Limitation
                            levelLim += modifier;
                        } else { // Enhancement
                            levelEnh += modifier;
                        }
                        break;
                    }
                    break;
                case POINTS:
                    if (one.getAffects() == Affects.LEVELS_ONLY) {
                        pointsPerLevel += modifier;
                    } else {
                        basePoints += modifier;
                    }
                    break;
                case MULTIPLIER:
                    multiplier *= one.getCostMultiplier();
                    break;
                }
            }
        }

        double modifiedBasePoints = basePoints;
        double leveledPoints      = pointsPerLevel * (levels + (halfLevel ? 0.5 : 0));
        if (baseEnh != 0 || baseLim != 0 || levelEnh != 0 || levelLim != 0) {
            int baseMod  = Math.max(baseEnh + baseLim, -80);
            int levelMod = Math.max(levelEnh + levelLim, -80);
            modifiedBasePoints = baseMod == levelMod ? modifyPoints(modifiedBasePoints + leveledPoints, baseMod) : modifyPoints(modifiedBasePoints, baseMod) + modifyPoints(leveledPoints, levelMod);
        } else {
            modifiedBasePoints += leveledPoints;
        }

        return applyRounding(modifiedBasePoints * multiplier, roundCostDown);
    }

    private static double modifyPoints(double points, int modifier) {
        return points + calculateModifierPoints(points, modifier);
    }

    private static double calculateModifierPoints(double points, int modifier) {
        return points * modifier / 100.0;
    }

    /** @return The points. */
    public int getPoints() {
        return mPoints;
    }

    /** @return The points per level. */
    public int getPointsPerLevel() {
        return mPointsPerLevel;
    }

    public String getReference() {
        return mReference;
    }

    public String getReferenceHighlight() {
        return getName();
    }

    @Override
    public boolean contains(String text, boolean lowerCaseOnly) {
        if (getName().toLowerCase().contains(text)) {
            return true;
        }
        return super.contains(text, lowerCaseOnly);
    }

    private static int getTypeFromText(String text) {
        int type = 0;
        if (text.contains(TYPE_MENTAL)) {
            type |= TYPE_MASK_MENTAL;
        }
        if (text.contains(TYPE_PHYSICAL)) {
            type |= TYPE_MASK_PHYSICAL;
        }
        if (text.contains(TYPE_SOCIAL)) {
            type |= TYPE_MASK_SOCIAL;
        }
        if (text.contains(TYPE_EXOTIC)) {
            type |= TYPE_MASK_EXOTIC;
        }
        if (text.contains(TYPE_SUPERNATURAL)) {
            type |= TYPE_MASK_SUPERNATURAL;
        }
        return type;
    }

    /** @return The type as a text string. */
    public String getTypeAsText() {
        if (!canHaveChildren()) {
            String        separator = ", ";
            StringBuilder buffer    = new StringBuilder();
            int           type      = getType();
            if ((type & TYPE_MASK_MENTAL) != 0) {
                buffer.append(TYPE_MENTAL);
            }
            if ((type & TYPE_MASK_PHYSICAL) != 0) {
                if (!buffer.isEmpty()) {
                    buffer.append("/");
                }
                buffer.append(TYPE_PHYSICAL);
            }
            if ((type & TYPE_MASK_SOCIAL) != 0) {
                if (!buffer.isEmpty()) {
                    buffer.append(separator);
                }
                buffer.append(TYPE_SOCIAL);
            }
            if ((type & TYPE_MASK_EXOTIC) != 0) {
                if (!buffer.isEmpty()) {
                    buffer.append(separator);
                }
                buffer.append(TYPE_EXOTIC);
            }
            if ((type & TYPE_MASK_SUPERNATURAL) != 0) {
                if (!buffer.isEmpty()) {
                    buffer.append(separator);
                }
                buffer.append(TYPE_SUPERNATURAL);
            }
            return buffer.toString();
        }
        return "";
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        if (!canHaveChildren()) {
            boolean halfLevel = mAllowHalfLevels && mHalfLevel;
            if (mLevels > 0 || halfLevel) {
                builder.append(' ');
                if (mLevels > 0) {
                    builder.append(mLevels);
                }
                if (halfLevel) {
                    builder.append('½');
                }
            }
        }
        return builder.toString();
    }

    /** @return The weapon list. */
    public List<WeaponStats> getWeapons() {
        return Collections.unmodifiableList(mWeapons);
    }

    /** @return The modifiers. */
    public List<AdvantageModifier> getModifiers() {
        return Collections.unmodifiableList(mModifiers);
    }

    /** @return The modifiers including those inherited from parent row. */
    public List<AdvantageModifier> getAllModifiers() {
        List<AdvantageModifier> allModifiers = new ArrayList<>(mModifiers);
        if (getParent() != null) {
            allModifiers.addAll(((Advantage) getParent()).getAllModifiers());
        }
        return Collections.unmodifiableList(allModifiers);
    }
}
