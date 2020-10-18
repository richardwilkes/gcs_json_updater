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

package com.trollworks.gcs.equipment;

import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.datafile.DataFile;
import com.trollworks.gcs.datafile.LoadState;
import com.trollworks.gcs.feature.ContainedWeightReduction;
import com.trollworks.gcs.feature.Feature;
import com.trollworks.gcs.modifier.EquipmentModifier;
import com.trollworks.gcs.modifier.EquipmentModifierCostType;
import com.trollworks.gcs.modifier.EquipmentModifierWeightType;
import com.trollworks.gcs.modifier.Fraction;
import com.trollworks.gcs.modifier.Modifier;
import com.trollworks.gcs.modifier.ModifierCostValueType;
import com.trollworks.gcs.modifier.ModifierWeightValueType;
import com.trollworks.gcs.ui.widget.outline.ListRow;
import com.trollworks.gcs.ui.widget.outline.Row;
import com.trollworks.gcs.utility.FilteredList;
import com.trollworks.gcs.utility.Fixed6;
import com.trollworks.gcs.utility.SaveType;
import com.trollworks.gcs.utility.json.JsonWriter;
import com.trollworks.gcs.utility.units.WeightUnits;
import com.trollworks.gcs.utility.units.WeightValue;
import com.trollworks.gcs.utility.xml.XMLReader;
import com.trollworks.gcs.weapon.MeleeWeaponStats;
import com.trollworks.gcs.weapon.RangedWeaponStats;
import com.trollworks.gcs.weapon.WeaponStats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A piece of equipment. */
public class Equipment extends ListRow {
    private static final int                     CURRENT_VERSION              = 7;
    private static final int                     EQUIPMENT_SPLIT_VERSION      = 6;
    private static final String                  DEFAULT_LEGALITY_CLASS       = "4";
    /** The XML tag used for items. */
    public static final  String                  TAG_EQUIPMENT                = "equipment";
    /** The XML tag used for containers. */
    public static final  String                  TAG_EQUIPMENT_CONTAINER      = "equipment_container";
    private static final String                  KEY_WEAPONS                  = "weapons";
    private static final String                  KEY_MODIFIERS                = "modifiers";
    private static final String                  KEY_IGNORE_WEIGHT_FOR_SKILLS = "ignore_weight_for_skills";
    private static final String                  ATTRIBUTE_EQUIPPED           = "equipped";
    private static final String                  TAG_QUANTITY                 = "quantity";
    private static final String                  TAG_USES                     = "uses";
    private static final String                  TAG_MAX_USES                 = "max_uses";
    private static final String                  TAG_DESCRIPTION              = "description";
    private static final String                  TAG_TECH_LEVEL               = "tech_level";
    private static final String                  TAG_LEGALITY_CLASS           = "legality_class";
    private static final String                  TAG_VALUE                    = "value";
    private static final String                  TAG_WEIGHT                   = "weight";
    private static final String                  TAG_REFERENCE                = "reference";
    /** The prefix used in front of all IDs for the equipment. */
    public static final  String                  PREFIX                       = GURPSCharacter.CHARACTER_PREFIX + "equipment.";
    /** The field ID for equipped/carried/not carried changes. */
    public static final  String                  ID_EQUIPPED                  = PREFIX + "Equipped";
    /** The field ID for quantity changes. */
    public static final  String                  ID_QUANTITY                  = PREFIX + "Quantity";
    /** The field ID for uses changes. */
    public static final  String                  ID_USES                      = PREFIX + "Uses";
    /** The field ID for max uses changes. */
    public static final  String                  ID_MAX_USES                  = PREFIX + "MaxUses";
    /** The field ID for description changes. */
    public static final  String                  ID_DESCRIPTION               = PREFIX + "Description";
    /** The field ID for tech level changes. */
    public static final  String                  ID_TECH_LEVEL                = PREFIX + "TechLevel";
    /** The field ID for legality changes. */
    public static final  String                  ID_LEGALITY_CLASS            = PREFIX + "LegalityClass";
    /** The field ID for value changes. */
    public static final  String                  ID_VALUE                     = PREFIX + "Value";
    /** The field ID for weight changes. */
    public static final  String                  ID_WEIGHT                    = PREFIX + "Weight";
    /** The field ID for extended value changes */
    public static final  String                  ID_EXTENDED_VALUE            = PREFIX + "ExtendedValue";
    /** The field ID for extended weight changes */
    public static final  String                  ID_EXTENDED_WEIGHT           = PREFIX + "ExtendedWeight";
    /** The field ID for page reference changes. */
    public static final  String                  ID_REFERENCE                 = PREFIX + "Reference";
    /** The field ID for when the row hierarchy changes. */
    public static final  String                  ID_LIST_CHANGED              = PREFIX + "ListChanged";
    /** The field ID for when the equipment becomes or stops being a weapon. */
    public static final  String                  ID_WEAPON_STATUS_CHANGED     = PREFIX + "WeaponStatus";
    /** The field ID for when the equipment gets Modifiers. */
    public static final  String                  ID_MODIFIER_STATUS_CHANGED   = PREFIX + "Modifier";
    private static final Fixed6                  MIN_CF                       = new Fixed6("-0.8", Fixed6.ZERO, false);
    private              boolean                 mEquipped;
    private              int                     mQuantity;
    private              int                     mUses;
    private              int                     mMaxUses;
    private              String                  mDescription;
    private              String                  mTechLevel;
    private              String                  mLegalityClass;
    private              Fixed6                  mValue;
    private              WeightValue             mWeight;
    private              boolean                 mWeightIgnoredForSkills;
    private              Fixed6                  mExtendedValue;
    private              WeightValue             mExtendedWeight;
    private              WeightValue             mExtendedWeightForSkills;
    private              String                  mReference;
    private              List<WeaponStats>       mWeapons;
    private              List<EquipmentModifier> mModifiers;

    /**
     * Creates a new equipment.
     *
     * @param dataFile    The data file to associate it with.
     * @param isContainer Whether or not this row allows children.
     */
    public Equipment(DataFile dataFile, boolean isContainer) {
        super(dataFile, isContainer);
        mEquipped = true;
        mQuantity = 1;
        mDescription = "Equipment";
        mTechLevel = "";
        mLegalityClass = DEFAULT_LEGALITY_CLASS;
        mReference = "";
        mValue = Fixed6.ZERO;
        mExtendedValue = Fixed6.ZERO;
        mWeightIgnoredForSkills = false;
        mWeight = new WeightValue(Fixed6.ZERO, dataFile.defaultWeightUnits());
        mExtendedWeight = new WeightValue(mWeight);
        mExtendedWeightForSkills = new WeightValue(mWeight);
        mWeapons = new ArrayList<>();
        mModifiers = new ArrayList<>();
    }

    /**
     * Creates a clone of an existing equipment and associates it with the specified data file.
     *
     * @param dataFile  The data file to associate it with.
     * @param equipment The equipment to clone.
     * @param deep      Whether or not to clone the children, grandchildren, etc.
     */
    public Equipment(DataFile dataFile, Equipment equipment, boolean deep) {
        super(dataFile, equipment);
        boolean forSheet = dataFile instanceof GURPSCharacter;
        mEquipped = !forSheet || equipment.mEquipped;
        mQuantity = forSheet ? equipment.mQuantity : 1;
        mUses = forSheet ? equipment.mUses : equipment.mMaxUses;
        mMaxUses = equipment.mMaxUses;
        mDescription = equipment.mDescription;
        mTechLevel = equipment.mTechLevel;
        mLegalityClass = equipment.mLegalityClass;
        mValue = equipment.mValue;
        mWeight = new WeightValue(equipment.mWeight);
        mWeightIgnoredForSkills = equipment.mWeightIgnoredForSkills;
        mReference = equipment.mReference;
        mWeapons = new ArrayList<>(equipment.mWeapons.size());
        for (WeaponStats weapon : equipment.mWeapons) {
            if (weapon instanceof MeleeWeaponStats) {
                mWeapons.add(new MeleeWeaponStats(this, (MeleeWeaponStats) weapon));
            } else if (weapon instanceof RangedWeaponStats) {
                mWeapons.add(new RangedWeaponStats(this, (RangedWeaponStats) weapon));
            }
        }
        mModifiers = new ArrayList<>(equipment.mModifiers.size());
        for (EquipmentModifier modifier : equipment.mModifiers) {
            mModifiers.add(new EquipmentModifier(mDataFile, modifier, false));
        }
        mExtendedValue = new Fixed6(mQuantity).mul(getAdjustedValue());
        mExtendedWeight = new WeightValue(getAdjustedWeight(false));
        mExtendedWeight.setValue(mExtendedWeight.getValue().mul(new Fixed6(mQuantity)));
        mExtendedWeightForSkills = new WeightValue(getAdjustedWeight(true));
        mExtendedWeightForSkills.setValue(mExtendedWeightForSkills.getValue().mul(new Fixed6(mQuantity)));
        if (deep) {
            int count = equipment.getChildCount();
            for (int i = 0; i < count; i++) {
                addChild(new Equipment(dataFile, (Equipment) equipment.getChild(i), true));
            }
        }
    }

    /**
     * Loads an equipment and associates it with the specified data file.
     *
     * @param dataFile The data file to associate it with.
     * @param reader   The XML reader to load from.
     * @param state    The {@link LoadState} to use.
     */
    public Equipment(DataFile dataFile, XMLReader reader, LoadState state) throws IOException {
        this(dataFile, TAG_EQUIPMENT_CONTAINER.equals(reader.getName()));
        load(reader, state);
    }

    @Override
    public boolean isEquivalentTo(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Equipment && super.isEquivalentTo(obj)) {
            Equipment row = (Equipment) obj;
            if (mQuantity == row.mQuantity && mUses == row.mUses && mMaxUses == row.mMaxUses && mValue.equals(row.mValue) && mEquipped == row.mEquipped && mWeightIgnoredForSkills == row.mWeightIgnoredForSkills && mWeight.equals(row.mWeight) && mDescription.equals(row.mDescription) && mTechLevel.equals(row.mTechLevel) && mLegalityClass.equals(row.mLegalityClass) && mReference.equals(row.mReference)) {
                if (mWeapons.equals(row.mWeapons)) {
                    return mModifiers.equals(row.mModifiers);
                }
            }
        }
        return false;
    }

    @Override
    public String getLocalizedName() {
        return "Equipment";
    }

    @Override
    public String getListChangedID() {
        return ID_LIST_CHANGED;
    }

    @Override
    public String getJSONTypeName() {
        return canHaveChildren() ? TAG_EQUIPMENT_CONTAINER : TAG_EQUIPMENT;
    }

    @Override
    public String getXMLTagName() {
        return canHaveChildren() ? TAG_EQUIPMENT_CONTAINER : TAG_EQUIPMENT;
    }

    @Override
    public int getXMLTagVersion() {
        return CURRENT_VERSION;
    }

    @Override
    public String getRowType() {
        return "Equipment";
    }

    @Override
    protected void prepareForLoad(LoadState state) {
        super.prepareForLoad(state);
        mEquipped = true;
        mQuantity = 1;
        mUses = 0;
        mMaxUses = 0;
        mDescription = "Equipment";
        mTechLevel = "";
        mLegalityClass = DEFAULT_LEGALITY_CLASS;
        mReference = "";
        mValue = Fixed6.ZERO;
        mWeight.setValue(Fixed6.ZERO);
        mWeightIgnoredForSkills = false;
        mWeapons = new ArrayList<>();
        mModifiers = new ArrayList<>();
    }

    @Override
    protected void loadAttributes(XMLReader reader, LoadState state) {
        super.loadAttributes(reader, state);
        if (mDataFile instanceof GURPSCharacter) {
            mEquipped = state.mDataItemVersion == 0 || state.mDataItemVersion >= EQUIPMENT_SPLIT_VERSION ? reader.isAttributeSet(ATTRIBUTE_EQUIPPED) : "equipped".equals(reader.getAttribute("state"));
            if (state.mDataFileVersion < GURPSCharacter.SEPARATED_EQUIPMENT_VERSION) {
                if (!mEquipped && !"carried".equals(reader.getAttribute("state"))) {
                    state.mUncarriedEquipment.add(this);
                }
            }
        }
    }

    @Override
    protected void loadSubElement(XMLReader reader, LoadState state) throws IOException {
        String name = reader.getName();
        if (TAG_DESCRIPTION.equals(name)) {
            mDescription = reader.readText().replace("\n", " ");
        } else if (TAG_TECH_LEVEL.equals(name)) {
            mTechLevel = reader.readText().replace("\n", " ");
        } else if (TAG_LEGALITY_CLASS.equals(name)) {
            mLegalityClass = reader.readText().replace("\n", " ");
        } else if (TAG_VALUE.equals(name)) {
            mValue = new Fixed6(reader.readText(), Fixed6.ZERO, false);
        } else if (TAG_WEIGHT.equals(name)) {
            mWeight = WeightValue.extract(reader.readText(), false);
        } else if (TAG_REFERENCE.equals(name)) {
            mReference = reader.readText().replace("\n", " ");
        } else if (TAG_USES.equals(name)) {
            mUses = reader.readInteger(0);
        } else if (TAG_MAX_USES.equals(name)) {
            mMaxUses = reader.readInteger(0);
        } else if (!state.mForUndo && (TAG_EQUIPMENT.equals(name) || TAG_EQUIPMENT_CONTAINER.equals(name))) {
            addChild(new Equipment(mDataFile, reader, state));
        } else if (EquipmentModifier.TAG_MODIFIER.equals(name)) {
            mModifiers.add(new EquipmentModifier(getDataFile(), reader, state));
        } else if (MeleeWeaponStats.TAG_ROOT.equals(name)) {
            mWeapons.add(new MeleeWeaponStats(this, reader));
        } else if (RangedWeaponStats.TAG_ROOT.equals(name)) {
            mWeapons.add(new RangedWeaponStats(this, reader));
        } else if (!canHaveChildren()) {
            if (TAG_QUANTITY.equals(name)) {
                mQuantity = reader.readInteger(1);
            } else {
                super.loadSubElement(reader, state);
            }
        } else {
            super.loadSubElement(reader, state);
        }
    }

    @Override
    protected void finishedLoading(LoadState state) {
        if (mMaxUses < 0) {
            mMaxUses = 0;
        }
        if (mUses > mMaxUses) {
            mUses = mMaxUses;
        } else if (mUses < 0) {
            mUses = 0;
        }
        updateExtendedValue(false);
        updateExtendedWeight(false);
        super.finishedLoading(state);
    }

    @Override
    protected void saveSelf(JsonWriter w, SaveType saveType) throws IOException {
        if (mDataFile instanceof GURPSCharacter) {
            w.keyValue(ATTRIBUTE_EQUIPPED, mEquipped);
        }
        if (!canHaveChildren()) {
            w.keyValueNot(TAG_QUANTITY, mQuantity, 0);
        }
        w.keyValueNot(TAG_DESCRIPTION, mDescription, "");
        w.keyValueNot(TAG_TECH_LEVEL, mTechLevel, "");
        w.keyValueNot(TAG_LEGALITY_CLASS, mLegalityClass, DEFAULT_LEGALITY_CLASS);
        if (!mValue.equals(Fixed6.ZERO)) {
            w.keyValue(TAG_VALUE, mValue.toString());
        }
        if (mWeightIgnoredForSkills) {
            w.keyValue(KEY_IGNORE_WEIGHT_FOR_SKILLS, true);
        }
        if (!mWeight.getNormalizedValue().equals(Fixed6.ZERO)) {
            w.keyValue(TAG_WEIGHT, mWeight.toString(false));
        }
        w.keyValueNot(TAG_REFERENCE, mReference, "");
        w.keyValueNot(TAG_USES, mUses, 0);
        w.keyValueNot(TAG_MAX_USES, mMaxUses, 0);
        WeaponStats.saveList(w, KEY_WEAPONS, mWeapons);
        saveList(w, KEY_MODIFIERS, mModifiers, saveType);
    }

    @Override
    public void update() {
        updateExtendedValue(true);
        updateExtendedWeight(true);
    }

    public void updateNoNotify() {
        updateExtendedValue(false);
        updateExtendedWeight(false);
    }

    /** @return The quantity. */
    public int getQuantity() {
        return mQuantity;
    }

    /**
     * @param quantity The quantity to set.
     * @return Whether it was modified.
     */
    public boolean setQuantity(int quantity) {
        if (quantity != mQuantity) {
            mQuantity = quantity;
            updateContainingWeights(true);
            updateContainingValues(true);
            return true;
        }
        return false;
    }

    /** @return The number of times this item can be used. */
    public int getUses() {
        return mUses;
    }

    /** @param uses The number of times this item can be used. */
    public boolean setUses(int uses) {
        if (uses > mMaxUses) {
            uses = mMaxUses;
        } else if (uses < 0) {
            uses = 0;
        }
        if (uses != mUses) {
            mUses = uses;
            return true;
        }
        return false;
    }

    /** @return The maximum number of times this item can be used. */
    public int getMaxUses() {
        return mMaxUses;
    }

    /** @param maxUses The maximum number of times this item can be used. */
    public boolean setMaxUses(int maxUses) {
        if (maxUses < 0) {
            maxUses = 0;
        }
        if (maxUses != mMaxUses) {
            mMaxUses = maxUses;
            if (mMaxUses > mUses) {
                mUses = mMaxUses;
            }
            return true;
        }
        return false;
    }

    /** @return The description. */
    public String getDescription() {
        return mDescription;
    }

    /**
     * @param description The description to set.
     * @return Whether it was modified.
     */
    public boolean setDescription(String description) {
        if (!mDescription.equals(description)) {
            mDescription = description;
            return true;
        }
        return false;
    }

    /** @return The tech level. */
    public String getTechLevel() {
        return mTechLevel;
    }

    /**
     * @param techLevel The tech level to set.
     * @return Whether it was modified.
     */
    public boolean setTechLevel(String techLevel) {
        if (!mTechLevel.equals(techLevel)) {
            mTechLevel = techLevel;
            return true;
        }
        return false;
    }

    /** @return The legality class. */
    public String getLegalityClass() {
        return mLegalityClass;
    }

    /**
     * @param legalityClass The legality class to set.
     * @return Whether it was modified.
     */
    public boolean setLegalityClass(String legalityClass) {
        if (!mLegalityClass.equals(legalityClass)) {
            mLegalityClass = legalityClass;
            return true;
        }
        return false;
    }

    /** @return The value after any cost adjustments. */
    public Fixed6 getAdjustedValue() {
        return getValueAdjustedForModifiers(mValue, getModifiers());
    }

    /**
     * @param value     The base value to adjust.
     * @param modifiers The modifiers to apply.
     * @return The adjusted value.
     */
    public static Fixed6 getValueAdjustedForModifiers(Fixed6 value, List<EquipmentModifier> modifiers) {
        // Apply all EquipmentModifierCostType.TO_ORIGINAL_COST
        Fixed6 cost = processNonCFStep(EquipmentModifierCostType.TO_ORIGINAL_COST, value, modifiers);

        // Apply all EquipmentModifierCostType.TO_BASE_COST
        Fixed6 cf    = Fixed6.ZERO;
        int    count = 0;
        for (EquipmentModifier modifier : modifiers) {
            if (modifier.isEnabled() && modifier.getCostAdjType() == EquipmentModifierCostType.TO_BASE_COST) {
                String                adj = modifier.getCostAdjAmount();
                ModifierCostValueType mvt = EquipmentModifierCostType.TO_BASE_COST.determineType(adj);
                Fixed6                amt = mvt.extractValue(adj, false);
                if (mvt == ModifierCostValueType.MULTIPLIER) {
                    amt = amt.sub(Fixed6.ONE);
                }
                cf = cf.add(amt);
                count++;
            }
        }
        if (!cf.equals(Fixed6.ZERO)) {
            if (cf.lessThan(MIN_CF)) {
                cf = MIN_CF;
            }
            cost = cost.mul(cf.add(Fixed6.ONE));
        }

        // Apply all EquipmentModifierCostType.TO_FINAL_BASE_COST
        cost = processNonCFStep(EquipmentModifierCostType.TO_FINAL_BASE_COST, cost, modifiers);

        // Apply all EquipmentModifierCostType.TO_FINAL_COST
        cost = processNonCFStep(EquipmentModifierCostType.TO_FINAL_COST, cost, modifiers);
        return cost.greaterThanOrEqual(Fixed6.ZERO) ? cost : Fixed6.ZERO;
    }

    private static Fixed6 processNonCFStep(EquipmentModifierCostType costType, Fixed6 value, List<EquipmentModifier> modifiers) {
        Fixed6 percentages = Fixed6.ZERO;
        Fixed6 additions   = Fixed6.ZERO;
        Fixed6 cost        = value;
        for (EquipmentModifier modifier : modifiers) {
            if (modifier.isEnabled() && modifier.getCostAdjType() == costType) {
                String                adj = modifier.getCostAdjAmount();
                ModifierCostValueType mvt = costType.determineType(adj);
                Fixed6                amt = mvt.extractValue(adj, false);
                switch (mvt) {
                case ADDITION -> additions = additions.add(amt);
                case PERCENTAGE -> percentages = percentages.add(amt);
                case MULTIPLIER -> cost = cost.mul(amt);
                }
            }
        }
        cost = cost.add(additions);
        if (!percentages.equals(Fixed6.ZERO)) {
            cost = cost.add(value.mul(percentages.div(new Fixed6(100))));
        }
        return cost;
    }

    /** @return The value. */
    public Fixed6 getValue() {
        return mValue;
    }

    /**
     * @param value The value to set.
     * @return Whether it was modified.
     */
    public boolean setValue(Fixed6 value) {
        if (!mValue.equals(value)) {
            mValue = value;
            updateContainingValues(true);
            return true;
        }
        return false;
    }

    /** @return The extended value. */
    public Fixed6 getExtendedValue() {
        return mExtendedValue;
    }

    /** @return The weight after any adjustments. */
    public WeightValue getAdjustedWeight(boolean forSkills) {
        if (forSkills && mWeightIgnoredForSkills) {
            return new WeightValue(Fixed6.ZERO, mWeight.getUnits());
        }
        return getWeightAdjustedForModifiers(mWeight, getModifiers());
    }

    /**
     * @param weight    The base weight to adjust.
     * @param modifiers The modifiers to apply.
     * @return The adjusted value.
     */
    public WeightValue getWeightAdjustedForModifiers(WeightValue weight, List<EquipmentModifier> modifiers) {
        WeightUnits defUnits = getDataFile().defaultWeightUnits();
        weight = new WeightValue(weight);

        // Apply all EquipmentModifierWeightType.TO_ORIGINAL_COST
        Fixed6      percentages = Fixed6.ZERO;
        WeightValue original    = new WeightValue(weight);
        for (EquipmentModifier modifier : modifiers) {
            if (modifier.isEnabled() && modifier.getWeightAdjType() == EquipmentModifierWeightType.TO_ORIGINAL_WEIGHT) {
                String                  adj = modifier.getWeightAdjAmount();
                ModifierWeightValueType mvt = EquipmentModifierWeightType.TO_ORIGINAL_WEIGHT.determineType(adj);
                Fixed6                  amt = mvt.extractFraction(adj, false).value();
                if (mvt == ModifierWeightValueType.ADDITION) {
                    weight.add(new WeightValue(amt, ModifierWeightValueType.extractUnits(adj, defUnits)));
                } else {
                    percentages = percentages.add(amt);
                }
            }
        }
        if (!percentages.equals(Fixed6.ZERO)) {
            original.setValue(original.getValue().mul(percentages.div(new Fixed6(100))));
            weight.add(original);
        }

        // Apply all EquipmentModifierWeightType.TO_BASE_COST
        weight = processMultiplyAddWeightStep(EquipmentModifierWeightType.TO_BASE_WEIGHT, weight, defUnits, modifiers);

        // Apply all EquipmentModifierWeightType.TO_FINAL_BASE_COST
        weight = processMultiplyAddWeightStep(EquipmentModifierWeightType.TO_FINAL_BASE_WEIGHT, weight, defUnits, modifiers);

        // Apply all EquipmentModifierWeightType.TO_FINAL_COST
        weight = processMultiplyAddWeightStep(EquipmentModifierWeightType.TO_FINAL_WEIGHT, weight, defUnits, modifiers);
        if (weight.getValue().lessThan(Fixed6.ZERO)) {
            weight.setValue(Fixed6.ZERO);
        }
        return weight;
    }

    private WeightValue processMultiplyAddWeightStep(EquipmentModifierWeightType weightType, WeightValue weight, WeightUnits defUnits, List<EquipmentModifier> modifiers) {
        weight = new WeightValue(weight);
        WeightValue sum = new WeightValue(Fixed6.ZERO, weight.getUnits());
        for (EquipmentModifier modifier : modifiers) {
            if (modifier.isEnabled() && modifier.getWeightAdjType() == weightType) {
                String                  adj      = modifier.getWeightAdjAmount();
                ModifierWeightValueType mvt      = weightType.determineType(adj);
                Fraction                fraction = mvt.extractFraction(adj, false);
                switch (mvt) {
                case MULTIPLIER:
                    weight.setValue(weight.getValue().mul(fraction.mNumerator).div(fraction.mDenominator));
                    break;
                case PERCENTAGE_MULTIPLIER:
                    weight.setValue(weight.getValue().mul(fraction.mNumerator).div(fraction.mDenominator.mul(new Fixed6(100))));
                    break;
                case ADDITION:
                    sum.add(new WeightValue(fraction.value(), ModifierWeightValueType.extractUnits(adj, defUnits)));
                    break;
                default:
                    break;
                }
            }
        }
        weight.add(sum);
        return weight;
    }

    /** @return The weight. */
    public WeightValue getWeight() {
        return mWeight;
    }

    /**
     * @param weight The weight to set.
     * @return Whether it was modified.
     */
    public boolean setWeight(WeightValue weight) {
        if (!mWeight.equals(weight)) {
            mWeight = new WeightValue(weight);
            updateContainingWeights(true);
            return true;
        }
        return false;
    }

    private boolean updateExtendedWeight(boolean okToNotify) {
        WeightValue saved          = mExtendedWeight;
        WeightValue savedForSkills = mExtendedWeightForSkills;
        int         count          = getChildCount();
        WeightUnits units          = mWeight.getUnits();
        mExtendedWeight = new WeightValue(getAdjustedWeight(false).getValue().mul(new Fixed6(mQuantity)), units);
        mExtendedWeightForSkills = new WeightValue(getAdjustedWeight(true).getValue().mul(new Fixed6(mQuantity)), units);
        WeightValue contained          = new WeightValue(Fixed6.ZERO, units);
        WeightValue containedForSkills = new WeightValue(Fixed6.ZERO, units);
        for (int i = 0; i < count; i++) {
            Equipment   one    = (Equipment) getChild(i);
            WeightValue weight = one.mExtendedWeight;
            contained.add(weight);
            weight = one.mExtendedWeightForSkills;
            containedForSkills.add(weight);
        }
        Fixed6      percentage = Fixed6.ZERO;
        WeightValue reduction  = new WeightValue(Fixed6.ZERO, units);
        for (Feature feature : getFeatures()) {
            if (feature instanceof ContainedWeightReduction) {
                ContainedWeightReduction cwr = (ContainedWeightReduction) feature;
                if (cwr.isPercentage()) {
                    percentage = percentage.add(new Fixed6(cwr.getPercentageReduction()));
                } else {
                    reduction.add(cwr.getAbsoluteReduction(mDataFile.defaultWeightUnits()));
                }
            }
        }
        for (EquipmentModifier modifier : getModifiers()) {
            if (modifier.isEnabled()) {
                for (Feature feature : modifier.getFeatures()) {
                    if (feature instanceof ContainedWeightReduction) {
                        ContainedWeightReduction cwr = (ContainedWeightReduction) feature;
                        if (cwr.isPercentage()) {
                            percentage = percentage.add(new Fixed6(cwr.getPercentageReduction()));
                        } else {
                            reduction.add(cwr.getAbsoluteReduction(mDataFile.defaultWeightUnits()));
                        }
                    }
                }
            }
        }
        if (percentage.greaterThan(Fixed6.ZERO)) {
            Fixed6 oneHundred = new Fixed6(100);
            if (percentage.greaterThanOrEqual(oneHundred)) {
                contained = new WeightValue(Fixed6.ZERO, units);
                containedForSkills = new WeightValue(Fixed6.ZERO, units);
            } else {
                contained.subtract(new WeightValue(contained.getValue().mul(percentage).div(oneHundred), contained.getUnits()));
                containedForSkills.subtract(new WeightValue(containedForSkills.getValue().mul(percentage).div(oneHundred), containedForSkills.getUnits()));
            }
        }
        contained.subtract(reduction);
        containedForSkills.subtract(reduction);
        if (contained.getNormalizedValue().greaterThan(Fixed6.ZERO)) {
            mExtendedWeight.add(contained);
        }
        if (containedForSkills.getNormalizedValue().greaterThan(Fixed6.ZERO)) {
            mExtendedWeightForSkills.add(containedForSkills);
        }
        if (getParent() instanceof Equipment) {
            ((Equipment) getParent()).updateContainingWeights(okToNotify);
        }
        if (!saved.equals(mExtendedWeight) || !savedForSkills.equals(mExtendedWeightForSkills)) {
            return true;
        }
        return false;
    }

    private void updateContainingWeights(boolean okToNotify) {
        Row parent = this;
        while (parent instanceof Equipment) {
            Equipment parentRow = (Equipment) parent;
            if (parentRow.updateExtendedWeight(okToNotify)) {
                parent = parentRow.getParent();
            } else {
                break;
            }
        }
    }

    private boolean updateExtendedValue(boolean okToNotify) {
        Fixed6 savedValue = mExtendedValue;
        int    count      = getChildCount();
        mExtendedValue = new Fixed6(mQuantity).mul(getAdjustedValue());
        for (int i = 0; i < count; i++) {
            Equipment child = (Equipment) getChild(i);
            mExtendedValue = mExtendedValue.add(child.mExtendedValue);
        }
        if (getParent() instanceof Equipment) {
            ((Equipment) getParent()).updateContainingValues(okToNotify);
        }
        if (!mExtendedValue.equals(savedValue)) {
            return true;
        }
        return false;
    }

    private void updateContainingValues(boolean okToNotify) {
        Row parent = this;
        while (parent instanceof Equipment) {
            Equipment parentRow = (Equipment) parent;
            if (parentRow.updateExtendedValue(okToNotify)) {
                parent = parentRow.getParent();
            } else {
                break;
            }
        }
    }

    /** @return The extended weight. */
    public WeightValue getExtendedWeight(boolean forSkills) {
        return forSkills ? mExtendedWeightForSkills : mExtendedWeight;
    }

    /** @return Whether skills ignore the weight of this equipment for encumbrance calculations. */
    public boolean isWeightIgnoredForSkills() {
        return mWeightIgnoredForSkills;
    }

    /**
     * @param ignore Whether skills ignore the weight of this equipment for encumbrance
     *               calculations.
     * @return Whether it was changed.
     */
    public boolean setWeightIgnoredForSkills(boolean ignore) {
        if (mWeightIgnoredForSkills != ignore) {
            mWeightIgnoredForSkills = ignore;
            updateContainingWeights(true);
            return true;
        }
        return false;
    }

    /** @return Whether this item is equipped. */
    public boolean isEquipped() {
        return mEquipped;
    }

    /**
     * @param equipped The new equipped state.
     * @return Whether it was changed.
     */
    public boolean setEquipped(boolean equipped) {
        if (mEquipped != equipped) {
            mEquipped = equipped;
            return true;
        }
        return false;
    }

    public String getReference() {
        return mReference;
    }

    public String getReferenceHighlight() {
        return getDescription();
    }

    public boolean setReference(String reference) {
        if (!mReference.equals(reference)) {
            mReference = reference;
            return true;
        }
        return false;
    }

    @Override
    public boolean contains(String text, boolean lowerCaseOnly) {
        if (getDescription().toLowerCase().contains(text)) {
            return true;
        }
        return super.contains(text, lowerCaseOnly);
    }

    @Override
    public String toString() {
        return getDescription();
    }

    /** @return The weapon list. */
    public List<WeaponStats> getWeapons() {
        return Collections.unmodifiableList(mWeapons);
    }

    /**
     * @param weapons The weapons to set.
     * @return Whether it was modified.
     */
    public boolean setWeapons(List<WeaponStats> weapons) {
        if (!mWeapons.equals(weapons)) {
            mWeapons = new ArrayList<>(weapons);
            for (WeaponStats weapon : mWeapons) {
                weapon.setOwner(this);
            }
            return true;
        }
        return false;
    }

    /** @return The modifiers. */
    public List<EquipmentModifier> getModifiers() {
        return Collections.unmodifiableList(mModifiers);
    }

    /**
     * @param modifiers The value to set for modifiers.
     * @return {@code true} if modifiers changed
     */
    public boolean setModifiers(List<? extends Modifier> modifiers) {
        List<EquipmentModifier> in = new FilteredList<>(modifiers, EquipmentModifier.class);
        if (!mModifiers.equals(in)) {
            mModifiers = in;
            update();
            return true;
        }
        return false;
    }

    /**
     * @param name The name to match against. Case-insensitive.
     * @return The first modifier that matches the name.
     */
    public EquipmentModifier getActiveModifierFor(String name) {
        for (EquipmentModifier m : getModifiers()) {
            if (m.isEnabled() && m.getName().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }
}
