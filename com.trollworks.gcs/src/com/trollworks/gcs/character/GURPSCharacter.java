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

package com.trollworks.gcs.character;

import com.trollworks.gcs.advantage.Advantage;
import com.trollworks.gcs.advantage.AdvantageContainerType;
import com.trollworks.gcs.advantage.AdvantageList;
import com.trollworks.gcs.datafile.DataFile;
import com.trollworks.gcs.datafile.LoadState;
import com.trollworks.gcs.equipment.Equipment;
import com.trollworks.gcs.equipment.EquipmentList;
import com.trollworks.gcs.feature.AttributeBonusLimitation;
import com.trollworks.gcs.feature.Bonus;
import com.trollworks.gcs.feature.BonusAttributeType;
import com.trollworks.gcs.feature.CostReduction;
import com.trollworks.gcs.feature.Feature;
import com.trollworks.gcs.feature.SkillBonus;
import com.trollworks.gcs.feature.SkillPointBonus;
import com.trollworks.gcs.feature.SkillSelectionType;
import com.trollworks.gcs.feature.SpellBonus;
import com.trollworks.gcs.feature.SpellPointBonus;
import com.trollworks.gcs.feature.WeaponBonus;
import com.trollworks.gcs.feature.WeaponSelectionType;
import com.trollworks.gcs.notes.Note;
import com.trollworks.gcs.notes.NoteList;
import com.trollworks.gcs.skill.Skill;
import com.trollworks.gcs.skill.SkillList;
import com.trollworks.gcs.skill.Technique;
import com.trollworks.gcs.spell.RitualMagicSpell;
import com.trollworks.gcs.spell.Spell;
import com.trollworks.gcs.spell.SpellList;
import com.trollworks.gcs.ui.widget.outline.ListRow;
import com.trollworks.gcs.ui.widget.outline.OutlineModel;
import com.trollworks.gcs.ui.widget.outline.Row;
import com.trollworks.gcs.ui.widget.outline.RowIterator;
import com.trollworks.gcs.utility.Dice;
import com.trollworks.gcs.utility.FilteredIterator;
import com.trollworks.gcs.utility.Fixed6;
import com.trollworks.gcs.utility.SaveType;
import com.trollworks.gcs.utility.json.JsonWriter;
import com.trollworks.gcs.utility.text.Numbers;
import com.trollworks.gcs.utility.units.WeightUnits;
import com.trollworks.gcs.utility.units.WeightValue;
import com.trollworks.gcs.utility.xml.XMLNodeType;
import com.trollworks.gcs.utility.xml.XMLReader;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** A GURPS character. */
public class GURPSCharacter extends DataFile {
    private static final int                                 CURRENT_VERSION                      = 5;
    /**
     * The version where equipment was separated out into different lists based on carried/not
     * carried status.
     */
    public static final  int                                 SEPARATED_EQUIPMENT_VERSION          = 4;
    /**
     * The version where HP and FP damage tracking was introduced, rather than a free-form text
     * field.
     */
    public static final  int                                 HP_FP_DAMAGE_TRACKING                = 5;
    private static final String                              TAG_ROOT                             = "character";
    private static final String                              TAG_CREATED_DATE                     = "created_date";
    private static final String                              TAG_MODIFIED_DATE                    = "modified_date";
    private static final String                              TAG_HP_DAMAGE                        = "hp_damage";
    private static final String                              TAG_FP_DAMAGE                        = "fp_damage";
    private static final String                              TAG_UNSPENT_POINTS                   = "unspent_points";
    private static final String                              TAG_TOTAL_POINTS                     = "total_points";
    private static final String                              TAG_INCLUDE_PUNCH                    = "include_punch";
    private static final String                              TAG_INCLUDE_KICK                     = "include_kick";
    private static final String                              TAG_INCLUDE_BOOTS                    = "include_kick_with_boots";
    private static final String                              KEY_HP_ADJ                           = "HP_adj";
    private static final String                              KEY_FP_ADJ                           = "FP_adj";
    private static final String                              KEY_ST                               = "ST";
    private static final String                              KEY_DX                               = "DX";
    private static final String                              KEY_IQ                               = "IQ";
    private static final String                              KEY_HT                               = "HT";
    private static final String                              KEY_WILL_ADJ                         = "will_adj";
    private static final String                              KEY_PER_ADJ                          = "per_adj";
    private static final String                              KEY_SPEED_ADJ                        = "speed_adj";
    private static final String                              KEY_MOVE_ADJ                         = "move_adj";
    public static final  String                              KEY_ADVANTAGES                       = "advantages";
    public static final  String                              KEY_SKILLS                           = "skills";
    public static final  String                              KEY_SPELLS                           = "spells";
    public static final  String                              KEY_EQUIPMENT                        = "equipment";
    public static final  String                              KEY_OTHER_EQUIPMENT                  = "other_equipment";
    public static final  String                              KEY_NOTES                            = "notes";
    /** The prefix for all character IDs. */
    public static final  String                              CHARACTER_PREFIX                     = "gcs.";
    /** The prefix used in front of all IDs for basic attributes. */
    public static final  String                              ATTRIBUTES_PREFIX                    = CHARACTER_PREFIX + "ba.";
    private              long                                mModifiedOn;
    private              long                                mCreatedOn;
    private              HashMap<String, ArrayList<Feature>> mFeatureMap;
    private              int                                 mStrength;
    private              int                                 mStrengthBonus;
    private              int                                 mLiftingStrengthBonus;
    private              int                                 mStrikingStrengthBonus;
    private              int                                 mStrengthCostReduction;
    private              int                                 mDexterity;
    private              int                                 mDexterityBonus;
    private              int                                 mDexterityCostReduction;
    private              int                                 mIntelligence;
    private              int                                 mIntelligenceBonus;
    private              int                                 mIntelligenceCostReduction;
    private              int                                 mHealth;
    private              int                                 mHealthBonus;
    private              int                                 mHealthCostReduction;
    private              int                                 mWillAdj;
    private              int                                 mWillBonus;
    private              int                                 mFrightCheckBonus;
    private              int                                 mPerAdj;
    private              int                                 mPerceptionBonus;
    private              int                                 mVisionBonus;
    private              int                                 mHearingBonus;
    private              int                                 mTasteAndSmellBonus;
    private              int                                 mTouchBonus;
    private              int                                 mHitPointsDamage;
    private              int                                 mHitPointsAdj;
    private              int                                 mHitPointBonus;
    private              int                                 mFatiguePoints;
    private              int                                 mFatiguePointsDamage;
    private              int                                 mFatiguePointBonus;
    private              double                              mSpeedAdj;
    private              double                              mSpeedBonus;
    private              int                                 mMoveAdj;
    private              int                                 mMoveBonus;
    private              int                                 mDodgeBonus;
    private              int                                 mParryBonus;
    private              int                                 mBlockBonus;
    private              int                                 mTotalPoints;
    private              Settings                            mSettings;
    private              Profile                             mProfile;
    private              Armor                               mArmor;
    private              OutlineModel                        mAdvantages;
    private              OutlineModel                        mSkills;
    private              OutlineModel                        mSpells;
    private              OutlineModel                        mEquipment;
    private              OutlineModel                        mOtherEquipment;
    private              OutlineModel                        mNotes;
    private              WeightValue                         mCachedWeightCarried;
    private              WeightValue                         mCachedWeightCarriedForSkills;
    private              Fixed6                              mCachedWealthCarried;
    private              Fixed6                              mCachedWealthNotCarried;
    private              int                                 mCachedAttributePoints;
    private              int                                 mCachedAdvantagePoints;
    private              int                                 mCachedDisadvantagePoints;
    private              int                                 mCachedQuirkPoints;
    private              int                                 mCachedSkillPoints;
    private              int                                 mCachedSpellPoints;
    private              int                                 mCachedRacePoints;
    private              boolean                             mSkillsUpdated;
    private              boolean                             mSpellsUpdated;
    private              boolean                             mDidModify;
    private              boolean                             mNeedAttributePointCalculation;
    private              boolean                             mNeedAdvantagesPointCalculation;
    private              boolean                             mNeedSkillPointCalculation;
    private              boolean                             mNeedSpellPointCalculation;
    private              boolean                             mNeedEquipmentCalculation;

    /**
     * Creates a new character from the specified file.
     *
     * @param path The path to load the data from.
     * @throws IOException if the data cannot be read or the file doesn't contain a valid character
     *                     sheet.
     */
    public GURPSCharacter(Path path) throws IOException {
        load(path);
    }

    private void characterInitialize() {
        mSettings = new Settings(this);
        mFeatureMap = new HashMap<>();
        mAdvantages = new OutlineModel();
        mSkills = new OutlineModel();
        mSpells = new OutlineModel();
        mEquipment = new OutlineModel();
        mOtherEquipment = new OutlineModel();
        mNotes = new OutlineModel();
        mTotalPoints = 0;
        mStrength = 10;
        mDexterity = 10;
        mIntelligence = 10;
        mHealth = 10;
        mHitPointsDamage = 0;
        mFatiguePointsDamage = 0;
        mProfile = new Profile(this);
        mArmor = new Armor(this);
        mCachedWeightCarried = new WeightValue(Fixed6.ZERO, mSettings.defaultWeightUnits());
        mCachedWeightCarriedForSkills = new WeightValue(Fixed6.ZERO, mSettings.defaultWeightUnits());
        mModifiedOn = System.currentTimeMillis();
        mCreatedOn = mModifiedOn;
    }

    @Override
    public String getJSONTypeName() {
        return TAG_ROOT;
    }

    @Override
    public int getXMLTagVersion() {
        return CURRENT_VERSION;
    }

    @Override
    public String getXMLTagName() {
        return TAG_ROOT;
    }

    @Override
    protected final void loadSelf(XMLReader reader, LoadState state) throws IOException {
        String marker        = reader.getMarker();
        int    unspentPoints = 0;
        int    currentHP     = Integer.MIN_VALUE;
        int    currentFP     = Integer.MIN_VALUE;
        characterInitialize();
        long modifiedOn = mModifiedOn;
        do {
            if (reader.next() == XMLNodeType.START_TAG) {
                String name = reader.getName();

                if (state.mDataFileVersion == 0) {
                    if (mProfile.loadTag(reader, name)) {
                        continue;
                    }
                }

                if (state.mDataFileVersion < HP_FP_DAMAGE_TRACKING) {
                    if ("current_hp".equals(name)) {
                        currentHP = reader.readInteger(Integer.MIN_VALUE);
                        continue;
                    } else if ("current_fp".equals(name)) {
                        currentFP = reader.readInteger(Integer.MIN_VALUE);
                        continue;
                    }
                }

                if (Settings.TAG_ROOT.equals(name)) {
                    mSettings.load(reader);
                } else if (Profile.TAG_ROOT.equals(name)) {
                    mProfile.load(reader);
                } else if (TAG_CREATED_DATE.equals(name)) {
                    mCreatedOn = Numbers.extractDateTime(reader.readText());
                } else if (TAG_MODIFIED_DATE.equals(name)) {
                    modifiedOn = Numbers.extractDateTime(reader.readText());
                } else if (BonusAttributeType.HP.getXMLTag().equals(name)) {
                    mHitPointsAdj = reader.readInteger(0);
                } else if (TAG_HP_DAMAGE.equals(name)) {
                    mHitPointsDamage = reader.readInteger(0);
                } else if (BonusAttributeType.FP.getXMLTag().equals(name)) {
                    mFatiguePoints = reader.readInteger(0);
                } else if (TAG_FP_DAMAGE.equals(name)) {
                    mFatiguePointsDamage = reader.readInteger(0);
                } else if (TAG_UNSPENT_POINTS.equals(name)) {
                    unspentPoints = reader.readInteger(0);
                } else if (TAG_TOTAL_POINTS.equals(name)) {
                    mTotalPoints = reader.readInteger(0);
                } else if (BonusAttributeType.ST.getXMLTag().equals(name)) {
                    mStrength = reader.readInteger(0);
                } else if (BonusAttributeType.DX.getXMLTag().equals(name)) {
                    mDexterity = reader.readInteger(0);
                } else if (BonusAttributeType.IQ.getXMLTag().equals(name)) {
                    mIntelligence = reader.readInteger(0);
                } else if (BonusAttributeType.HT.getXMLTag().equals(name)) {
                    mHealth = reader.readInteger(0);
                } else if (BonusAttributeType.WILL.getXMLTag().equals(name)) {
                    mWillAdj = reader.readInteger(0);
                } else if (BonusAttributeType.PERCEPTION.getXMLTag().equals(name)) {
                    mPerAdj = reader.readInteger(0);
                } else if (BonusAttributeType.SPEED.getXMLTag().equals(name)) {
                    mSpeedAdj = reader.readDouble(0.0);
                } else if (BonusAttributeType.MOVE.getXMLTag().equals(name)) {
                    mMoveAdj = reader.readInteger(0);
                } else if (AdvantageList.TAG_ROOT.equals(name)) {
                    loadAdvantageList(reader, state);
                } else if (SkillList.TAG_ROOT.equals(name)) {
                    loadSkillList(reader, state);
                } else if (SpellList.TAG_ROOT.equals(name)) {
                    loadSpellList(reader, state);
                } else if (EquipmentList.TAG_CARRIED_ROOT.equals(name)) {
                    loadEquipmentList(reader, state, mEquipment);
                } else if (EquipmentList.TAG_OTHER_ROOT.equals(name)) {
                    loadEquipmentList(reader, state, mOtherEquipment);
                } else if (NoteList.TAG_ROOT.equals(name)) {
                    loadNoteList(reader, state);
                } else {
                    reader.skipTag(name);
                }
            }
        } while (reader.withinMarker(marker));

        // Loop through the skills and update their levels. It is necessary to do this here and not
        // as they are loaded, since references to defaults won't work until the entire list is
        // available.
        for (Skill skill : getSkillsIterator()) {
            skill.updateLevel(false);
        }

        calculateAll();
        if (unspentPoints != 0) {
            setUnspentPoints(unspentPoints);
        }

        if (state.mDataFileVersion < HP_FP_DAMAGE_TRACKING) {
            if (currentHP != Integer.MIN_VALUE) {
                mHitPointsDamage = -Math.min(currentHP - getHitPointsAdj(), 0);
            }
            if (currentFP != Integer.MIN_VALUE) {
                mFatiguePointsDamage = -Math.min(currentFP - getFatiguePoints(), 0);
            }
        }
        mModifiedOn = modifiedOn;
    }

    private void loadAdvantageList(XMLReader reader, LoadState state) throws IOException {
        String marker = reader.getMarker();
        do {
            if (reader.next() == XMLNodeType.START_TAG) {
                String name = reader.getName();
                if (Advantage.TAG_ADVANTAGE.equals(name) || Advantage.TAG_ADVANTAGE_CONTAINER.equals(name)) {
                    mAdvantages.addRow(new Advantage(this, reader, state), true);
                } else {
                    reader.skipTag(name);
                }
            }
        } while (reader.withinMarker(marker));
    }

    private void loadSkillList(XMLReader reader, LoadState state) throws IOException {
        String marker = reader.getMarker();
        do {
            if (reader.next() == XMLNodeType.START_TAG) {
                String name = reader.getName();
                if (Skill.TAG_SKILL.equals(name) || Skill.TAG_SKILL_CONTAINER.equals(name)) {
                    mSkills.addRow(new Skill(this, reader, state), true);
                } else if (Technique.TAG_TECHNIQUE.equals(name)) {
                    mSkills.addRow(new Technique(this, reader, state), true);
                } else {
                    reader.skipTag(name);
                }
            }
        } while (reader.withinMarker(marker));
    }

    private void loadSpellList(XMLReader reader, LoadState state) throws IOException {
        String marker = reader.getMarker();
        do {
            if (reader.next() == XMLNodeType.START_TAG) {
                String name = reader.getName();
                if (Spell.TAG_SPELL.equals(name) || Spell.TAG_SPELL_CONTAINER.equals(name)) {
                    mSpells.addRow(new Spell(this, reader, state), true);
                } else if (RitualMagicSpell.TAG_RITUAL_MAGIC_SPELL.equals(name)) {
                    mSpells.addRow(new RitualMagicSpell(this, reader, state), true);
                } else {
                    reader.skipTag(name);
                }
            }
        } while (reader.withinMarker(marker));
    }

    private void loadEquipmentList(XMLReader reader, LoadState state, OutlineModel equipmentList) throws IOException {
        String marker = reader.getMarker();
        do {
            if (reader.next() == XMLNodeType.START_TAG) {
                String name = reader.getName();
                if (Equipment.TAG_EQUIPMENT.equals(name) || Equipment.TAG_EQUIPMENT_CONTAINER.equals(name)) {
                    state.mUncarriedEquipment = new HashSet<>();
                    Equipment equipment = new Equipment(this, reader, state);
                    if (state.mDataFileVersion < SEPARATED_EQUIPMENT_VERSION && equipmentList == mEquipment && !state.mUncarriedEquipment.isEmpty()) {
                        if (addToEquipment(state.mUncarriedEquipment, equipment)) {
                            equipmentList.addRow(equipment, true);
                        }
                    } else {
                        equipmentList.addRow(equipment, true);
                    }
                } else {
                    reader.skipTag(name);
                }
            }
        } while (reader.withinMarker(marker));
    }

    private boolean addToEquipment(HashSet<Equipment> uncarried, Equipment equipment) {
        if (uncarried.contains(equipment)) {
            mOtherEquipment.addRow(equipment, true);
            return false;
        }
        List<Row> children = equipment.getChildren();
        if (children != null) {
            for (Row child : new ArrayList<>(children)) {
                if (!addToEquipment(uncarried, (Equipment) child)) {
                    equipment.removeChild(child);
                }
            }
        }
        return true;
    }

    private void loadNoteList(XMLReader reader, LoadState state) throws IOException {
        String marker = reader.getMarker();
        do {
            if (reader.next() == XMLNodeType.START_TAG) {
                String name = reader.getName();
                if (Note.TAG_NOTE.equals(name) || Note.TAG_NOTE_CONTAINER.equals(name)) {
                    mNotes.addRow(new Note(this, reader, state), true);
                } else {
                    reader.skipTag(name);
                }
            }
        } while (reader.withinMarker(marker));
    }

    private void calculateAll() {
        calculateAttributePoints();
        calculateAdvantagePoints();
        calculateSkillPoints();
        calculateSpellPoints();
        calculateWeightAndWealthCarried(false);
        calculateWealthNotCarried(false);
    }

    @Override
    protected void saveSelf(JsonWriter w, SaveType saveType) throws IOException {
        w.key(Settings.TAG_ROOT);
        mSettings.save(w);
        w.keyValue(TAG_CREATED_DATE, DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(mCreatedOn)));
        w.keyValue(TAG_MODIFIED_DATE, DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(mModifiedOn)));
        w.key(Profile.TAG_ROOT);
        mProfile.save(w);
        w.keyValueNot(KEY_HP_ADJ, mHitPointsAdj, 0);
        w.keyValueNot(TAG_HP_DAMAGE, mHitPointsDamage, 0);
        w.keyValueNot(KEY_FP_ADJ, mFatiguePoints, 0);
        w.keyValueNot(TAG_FP_DAMAGE, mFatiguePointsDamage, 0);
        w.keyValue(TAG_TOTAL_POINTS, mTotalPoints);
        w.keyValue(KEY_ST, mStrength);
        w.keyValue(KEY_DX, mDexterity);
        w.keyValue(KEY_IQ, mIntelligence);
        w.keyValue(KEY_HT, mHealth);
        w.keyValueNot(KEY_WILL_ADJ, mWillAdj, 0);
        w.keyValueNot(KEY_PER_ADJ, mPerAdj, 0);
        w.keyValueNot(KEY_SPEED_ADJ, mSpeedAdj, 0);
        w.keyValueNot(KEY_MOVE_ADJ, mMoveAdj, 0);
        ListRow.saveList(w, KEY_ADVANTAGES, mAdvantages.getTopLevelRows(), saveType);
        ListRow.saveList(w, KEY_SKILLS, mSkills.getTopLevelRows(), saveType);
        ListRow.saveList(w, KEY_SPELLS, mSpells.getTopLevelRows(), saveType);
        ListRow.saveList(w, KEY_EQUIPMENT, mEquipment.getTopLevelRows(), saveType);
        ListRow.saveList(w, KEY_OTHER_EQUIPMENT, mOtherEquipment.getTopLevelRows(), saveType);
        ListRow.saveList(w, KEY_NOTES, mNotes.getTopLevelRows(), saveType);
    }

    /** @return The created on date. */
    public long getCreatedOn() {
        return mCreatedOn;
    }

    /** @return The modified date. */
    public long getModifiedOn() {
        return mModifiedOn;
    }

    public void updateSkills() {
        for (Skill skill : getSkillsIterator()) {
            skill.updateLevel(true);
        }
        mSkillsUpdated = true;
    }

    private void updateSpells() {
        for (Spell spell : getSpellsIterator()) {
            spell.updateLevel(true);
        }
        mSpellsUpdated = true;
    }

    /** @return The strength (ST). */
    public int getStrength() {
        return mStrength + mStrengthBonus;
    }

    /** @return The current strength bonus from features. */
    public int getStrengthBonus() {
        return mStrengthBonus;
    }

    /** @return The current lifting strength bonus from features. */
    public int getLiftingStrengthBonus() {
        return mLiftingStrengthBonus;
    }

    /** @return The current striking strength bonus from features. */
    public int getStrikingStrengthBonus() {
        return mStrikingStrengthBonus;
    }

    /** @return The number of points spent on strength. */
    public int getStrengthPoints() {
        int reduction = mStrengthCostReduction;
        if (!mSettings.useKnowYourOwnStrength()) {
            reduction += mProfile.getSizeModifier() * 10;
        }
        return getPointsForAttribute(mStrength - 10, 10, reduction);
    }

    private static int getPointsForAttribute(int delta, int ptsPerLevel, int reduction) {
        int amt = delta * ptsPerLevel;
        if (reduction > 0 && delta > 0) {
            if (reduction > 80) {
                reduction = 80;
            }
            amt = (99 + amt * (100 - reduction)) / 100;
        }
        return amt;
    }

    /** @return The basic thrusting damage. */
    public Dice getThrust() {
        return getThrust(getStrength() + mStrikingStrengthBonus);
    }

    /**
     * @param strength The strength to return basic thrusting damage for.
     * @return The basic thrusting damage.
     */
    public Dice getThrust(int strength) {
        if (mSettings.useThrustEqualsSwingMinus2()) {
            Dice dice = getSwing(strength);
            dice.add(-2);
            return dice;
        }
        if (mSettings.useReducedSwing()) {
            if (strength < 19) {
                return new Dice(1, -(6 - (strength - 1) / 2));
            }
            int dice = 1;
            int adds = (strength - 10) / 2 - 2;
            if ((strength - 10) % 2 == 1) {
                adds++;
            }
            dice += 2 * (adds / 7);
            adds %= 7;
            dice += adds / 4;
            adds %= 4;
            if (adds == 3) {
                dice++;
                adds = -1;
            }

            return new Dice(dice, adds);
        }

        if (mSettings.useKnowYourOwnStrength()) {
            if (strength < 12) {
                return new Dice(1, strength - 12);
            }
            return new Dice((strength - 7) / 4, (strength + 1) % 4 - 1);
        }

        int value = strength;

        if (value < 19) {
            return new Dice(1, -(6 - (value - 1) / 2));
        }

        value -= 11;
        if (strength > 50) {
            value--;
            if (strength > 79) {
                value -= 1 + (strength - 80) / 5;
            }
        }
        return new Dice(value / 8 + 1, value % 8 / 2 - 1);
    }

    /** @return The basic swinging damage. */
    public Dice getSwing() {
        return getSwing(getStrength() + mStrikingStrengthBonus);
    }

    /**
     * @param strength The strength to return basic swinging damage for.
     * @return The basic thrusting damage.
     */
    public Dice getSwing(int strength) {
        if (mSettings.useReducedSwing()) {
            if (strength < 10) {
                return new Dice(1, -(5 - (strength - 1) / 2));
            }

            int dice = 1;
            int adds = (strength - 10) / 2;
            dice += 2 * (adds / 7);
            adds %= 7;
            dice += adds / 4;
            adds %= 4;
            if (adds == 3) {
                dice++;
                adds = -1;
            }

            return new Dice(dice, adds);
        }

        if (mSettings.useKnowYourOwnStrength()) {
            if (strength < 10) {
                return new Dice(1, strength - 10);
            }
            return new Dice((strength - 5) / 4, (strength - 1) % 4 - 1);
        }

        int value = strength;

        if (value < 10) {
            return new Dice(1, -(5 - (value - 1) / 2));
        }

        if (value < 28) {
            value -= 9;
            return new Dice(value / 4 + 1, value % 4 - 1);
        }

        if (strength > 40) {
            value -= (strength - 40) / 5;
        }

        if (strength > 59) {
            value++;
        }
        value += 9;
        return new Dice(value / 8 + 1, value % 8 / 2 - 1);
    }

    /** @return Basic lift. */
    public WeightValue getBasicLift() {
        return getBasicLift(defaultWeightUnits());
    }

    private WeightValue getBasicLift(WeightUnits desiredUnits) {
        Fixed6      ten = new Fixed6(10);
        WeightUnits units;
        Fixed6      divisor;
        Fixed6      multiplier;
        Fixed6      roundAt;
        if (useSimpleMetricConversions() && defaultWeightUnits().isMetric()) {
            units = WeightUnits.KG;
            divisor = ten;
            multiplier = Fixed6.ONE;
            roundAt = new Fixed6(5);
        } else {
            units = WeightUnits.LB;
            divisor = new Fixed6(5);
            multiplier = new Fixed6(2);
            roundAt = ten;
        }
        int strength = getStrength() + mLiftingStrengthBonus;
        if (isTired()) {
            boolean plusOne = strength % 2 != 0;
            strength /= 2;
            if (plusOne) {
                strength++;
            }
        }
        Fixed6 value;
        if (strength < 1) {
            value = Fixed6.ZERO;
        } else {
            if (mSettings.useKnowYourOwnStrength()) {
                int diff = 0;
                if (strength > 19) {
                    diff = strength / 10 - 1;
                    strength -= diff * 10;
                }
                value = new Fixed6(Math.pow(10.0, strength / 10.0)).mul(multiplier);
                value = strength <= 6 ? value.mul(ten).round().div(ten) : value.round();
                value = value.mul(new Fixed6(Math.pow(10, diff)));
            } else {
                //noinspection UnnecessaryExplicitNumericCast
                value = new Fixed6((long) strength * (long) strength).div(divisor);
            }
            if (value.greaterThanOrEqual(roundAt)) {
                value = value.round();
            }
            value = value.mul(ten).trunc().div(ten);
        }
        return new WeightValue(desiredUnits.convert(units, value), desiredUnits);
    }

    private WeightValue getMultipleOfBasicLift(int multiple) {
        WeightValue lift = getBasicLift();
        lift.setValue(lift.getValue().mul(new Fixed6(multiple)));
        return lift;
    }

    /** @return The one-handed lift value. */
    public WeightValue getOneHandedLift() {
        return getMultipleOfBasicLift(2);
    }

    /** @return The two-handed lift value. */
    public WeightValue getTwoHandedLift() {
        return getMultipleOfBasicLift(8);
    }

    /** @return The shove and knock over value. */
    public WeightValue getShoveAndKnockOver() {
        return getMultipleOfBasicLift(12);
    }

    /** @return The running shove and knock over value. */
    public WeightValue getRunningShoveAndKnockOver() {
        return getMultipleOfBasicLift(24);
    }

    /** @return The carry on back value. */
    public WeightValue getCarryOnBack() {
        return getMultipleOfBasicLift(15);
    }

    /** @return The shift slightly value. */
    public WeightValue getShiftSlightly() {
        return getMultipleOfBasicLift(50);
    }

    /**
     * @param encumbrance The encumbrance level.
     * @return The maximum amount the character can carry for the specified encumbrance level.
     */
    public WeightValue getMaximumCarry(Encumbrance encumbrance) {
        WeightUnits desiredUnits = defaultWeightUnits();
        WeightUnits calcUnits    = useSimpleMetricConversions() && desiredUnits.isMetric() ? WeightUnits.KG : WeightUnits.LB;
        WeightValue lift         = getBasicLift(calcUnits);
        lift.setValue(lift.getValue().mul(new Fixed6(encumbrance.getWeightMultiplier())));
        return new WeightValue(desiredUnits.convert(calcUnits, lift.getValue()), desiredUnits);
    }

    /**
     * @return The character's basic speed.
     */
    public double getBasicSpeed() {
        return mSpeedAdj + mSpeedBonus + getRawBasicSpeed();
    }

    private double getRawBasicSpeed() {
        return (getDexterity() + getHealth()) / 4.0;
    }

    /** @return The basic speed bonus. */
    public double getBasicSpeedBonus() {
        return mSpeedBonus;
    }

    /** @return The number of points spent on basic speed. */
    public int getBasicSpeedPoints() {
        return (int) (mSpeedAdj * 20.0);
    }

    /**
     * @return The character's basic move.
     */
    public int getBasicMove() {
        return Math.max(mMoveAdj + mMoveBonus + getRawBasicMove(), 0);
    }

    private int getRawBasicMove() {
        return (int) Math.floor(getBasicSpeed());
    }

    /** @return The basic move bonus. */
    public int getBasicMoveBonus() {
        return mMoveBonus;
    }

    /** @return The number of points spent on basic move. */
    public int getBasicMovePoints() {
        return mMoveAdj * 5;
    }

    /**
     * @param encumbrance The encumbrance level.
     * @return The character's ground move for the specified encumbrance level.
     */
    public int getMove(Encumbrance encumbrance) {
        int     initialMove = getBasicMove();
        boolean reeling     = isReeling();
        boolean tired       = isTired();
        if (reeling || tired) {
            int     divisor = (reeling && tired) ? 4 : 2;
            boolean plusOne = initialMove % divisor != 0;
            initialMove /= divisor;
            if (plusOne) {
                initialMove++;
            }
        }
        int move = initialMove * (10 + 2 * encumbrance.getEncumbrancePenalty()) / 10;
        if (move < 1) {
            return initialMove > 0 ? 1 : 0;
        }
        return move;
    }

    /**
     * @param encumbrance The encumbrance level.
     * @return The character's dodge for the specified encumbrance level.
     */
    public int getDodge(Encumbrance encumbrance) {
        int     dodge   = 3 + mDodgeBonus + (int) Math.floor(getBasicSpeed());
        boolean reeling = isReeling();
        boolean tired   = isTired();
        if (reeling || tired) {
            int     divisor = (reeling && tired) ? 4 : 2;
            boolean plusOne = dodge % divisor != 0;
            dodge /= divisor;
            if (plusOne) {
                dodge++;
            }
        }
        return Math.max(dodge + encumbrance.getEncumbrancePenalty(), 1);
    }

    /** @return The dodge bonus. */
    public int getDodgeBonus() {
        return mDodgeBonus;
    }

    /** @return The parry bonus. */
    public int getParryBonus() {
        return mParryBonus;
    }

    /** @return The block bonus. */
    public int getBlockBonus() {
        return mBlockBonus;
    }

    /** @return The current encumbrance level. */
    public Encumbrance getEncumbranceLevel(boolean forSkills) {
        Fixed6 carried = getWeightCarried(forSkills).getNormalizedValue();
        for (Encumbrance encumbrance : Encumbrance.values()) {
            if (carried.lessThanOrEqual(getMaximumCarry(encumbrance).getNormalizedValue())) {
                return encumbrance;
            }
        }
        return Encumbrance.EXTRA_HEAVY;
    }

    /**
     * @return {@code true} if the carried weight is greater than the maximum allowed for an
     *         extra-heavy load.
     */
    public boolean isCarryingGreaterThanMaxLoad(boolean forSkills) {
        return getWeightCarried(forSkills).getNormalizedValue().greaterThan(getMaximumCarry(Encumbrance.EXTRA_HEAVY).getNormalizedValue());
    }

    /** @return The current weight being carried. */
    public WeightValue getWeightCarried(boolean forSkills) {
        return forSkills ? mCachedWeightCarriedForSkills : mCachedWeightCarried;
    }

    /** @return The current wealth being carried. */
    public Fixed6 getWealthCarried() {
        return mCachedWealthCarried;
    }

    /** @return The current wealth not being carried. */
    public Fixed6 getWealthNotCarried() {
        return mCachedWealthNotCarried;
    }

    /**
     * Convert a metric {@link WeightValue} by GURPS Metric rules into an imperial one. If an
     * imperial {@link WeightValue} is passed as an argument, it will be returned unchanged.
     *
     * @param value The {@link WeightValue} to be converted by GURPS Metric rules.
     * @return The converted imperial {@link WeightValue}.
     */
    public static WeightValue convertFromGurpsMetric(WeightValue value) {
        return switch (value.getUnits()) {
            case G -> new WeightValue(value.getValue().div(new Fixed6(30)), WeightUnits.OZ);
            case KG -> new WeightValue(value.getValue().mul(new Fixed6(2)), WeightUnits.LB);
            case T -> new WeightValue(value.getValue(), WeightUnits.LT);
            default -> value;
        };
    }

    /**
     * Convert an imperial {@link WeightValue} by GURPS Metric rules into a metric one. If a metric
     * {@link WeightValue} is passed as an argument, it will be returned unchanged.
     *
     * @param value The {@link WeightValue} to be converted by GURPS Metric rules.
     * @return The converted metric {@link WeightValue}.
     */
    public static WeightValue convertToGurpsMetric(WeightValue value) {
        return switch (value.getUnits()) {
            case LB -> new WeightValue(value.getValue().div(new Fixed6(2)), WeightUnits.KG);
            case LT, TN -> new WeightValue(value.getValue(), WeightUnits.T);
            case OZ -> new WeightValue(value.getValue().mul(new Fixed6(30)), WeightUnits.G);
            default -> value;
        };
    }

    /**
     * Calculate the total weight and wealth carried.
     *
     * @param notify Whether to send out notifications if the resulting values are different from
     *               the previous values.
     */
    public void calculateWeightAndWealthCarried(boolean notify) {
        WeightValue savedWeight          = new WeightValue(mCachedWeightCarried);
        WeightValue savedWeightForSkills = new WeightValue(mCachedWeightCarriedForSkills);
        Fixed6      savedWealth          = mCachedWealthCarried;
        mCachedWeightCarried = new WeightValue(Fixed6.ZERO, defaultWeightUnits());
        mCachedWeightCarriedForSkills = new WeightValue(Fixed6.ZERO, defaultWeightUnits());
        mCachedWealthCarried = Fixed6.ZERO;
        for (Row one : mEquipment.getTopLevelRows()) {
            Equipment   equipment = (Equipment) one;
            WeightValue weight    = new WeightValue(equipment.getExtendedWeight(false));
            if (useSimpleMetricConversions()) {
                weight = defaultWeightUnits().isMetric() ? convertToGurpsMetric(weight) : convertFromGurpsMetric(weight);
            }
            mCachedWeightCarried.add(weight);
            mCachedWealthCarried = mCachedWealthCarried.add(equipment.getExtendedValue());

            weight = new WeightValue(equipment.getExtendedWeight(true));
            if (useSimpleMetricConversions()) {
                weight = defaultWeightUnits().isMetric() ? convertToGurpsMetric(weight) : convertFromGurpsMetric(weight);
            }
            mCachedWeightCarriedForSkills.add(weight);
        }
    }

    /**
     * Calculate the total wealth not carried.
     *
     * @param notify Whether to send out notifications if the resulting values are different from
     *               the previous values.
     */
    public void calculateWealthNotCarried(boolean notify) {
        Fixed6 savedWealth = mCachedWealthNotCarried;
        mCachedWealthNotCarried = Fixed6.ZERO;
        for (Row one : mOtherEquipment.getTopLevelRows()) {
            mCachedWealthNotCarried = mCachedWealthNotCarried.add(((Equipment) one).getExtendedValue());
        }
    }

    /** @return The dexterity (DX). */
    public int getDexterity() {
        return mDexterity + mDexterityBonus;
    }

    /** @return The dexterity bonus. */
    public int getDexterityBonus() {
        return mDexterityBonus;
    }

    /** @return The number of points spent on dexterity. */
    public int getDexterityPoints() {
        return getPointsForAttribute(mDexterity - 10, 20, mDexterityCostReduction);
    }

    /** @return The intelligence (IQ). */
    public int getIntelligence() {
        return mIntelligence + mIntelligenceBonus;
    }

    /** @return The intelligence bonus. */
    public int getIntelligenceBonus() {
        return mIntelligenceBonus;
    }

    /** @return The number of points spent on intelligence. */
    public int getIntelligencePoints() {
        return getPointsForAttribute(mIntelligence - 10, 20, mIntelligenceCostReduction);
    }

    /** @return The health (HT). */
    public int getHealth() {
        return mHealth + mHealthBonus;
    }

    /** @return The health bonus. */
    public int getHealthBonus() {
        return mHealthBonus;
    }

    /** @return The number of points spent on health. */
    public int getHealthPoints() {
        return getPointsForAttribute(mHealth - 10, 10, mHealthCostReduction);
    }

    /** @return The total number of points this character has. */
    public int getTotalPoints() {
        return mTotalPoints;
    }

    /** @return The total number of points spent. */
    public int getSpentPoints() {
        return getAttributePoints() + getAdvantagePoints() + getDisadvantagePoints() + getQuirkPoints() + getSkillPoints() + getSpellPoints() + getRacePoints();
    }

    /** @return The number of unspent points. */
    public int getUnspentPoints() {
        return mTotalPoints - getSpentPoints();
    }

    /**
     * Sets the unspent character points.
     *
     * @param unspent The new unspent character points.
     */
    public void setUnspentPoints(int unspent) {
        int current = getUnspentPoints();
        if (current != unspent) {
            mTotalPoints = unspent + getSpentPoints();
        }
    }

    /** @return The number of points spent on basic attributes. */
    public int getAttributePoints() {
        return mCachedAttributePoints;
    }

    private void calculateAttributePoints() {
        mCachedAttributePoints = getStrengthPoints() + getDexterityPoints() + getIntelligencePoints() + getHealthPoints() + getWillPoints() + getPerceptionPoints() + getBasicSpeedPoints() + getBasicMovePoints() + getHitPointPoints() + getFatiguePointPoints();
    }

    /** @return The number of points spent on a racial package. */
    public int getRacePoints() {
        return mCachedRacePoints;
    }

    /** @return The number of points spent on advantages. */
    public int getAdvantagePoints() {
        return mCachedAdvantagePoints;
    }

    /** @return The number of points spent on disadvantages. */
    public int getDisadvantagePoints() {
        return mCachedDisadvantagePoints;
    }

    /** @return The number of points spent on quirks. */
    public int getQuirkPoints() {
        return mCachedQuirkPoints;
    }

    private void calculateAdvantagePoints() {
        mCachedAdvantagePoints = 0;
        mCachedDisadvantagePoints = 0;
        mCachedRacePoints = 0;
        mCachedQuirkPoints = 0;
        for (Advantage advantage : new FilteredIterator<>(mAdvantages.getTopLevelRows(), Advantage.class)) {
            calculateSingleAdvantagePoints(advantage);
        }
    }

    private void calculateSingleAdvantagePoints(Advantage advantage) {
        if (advantage.canHaveChildren()) {
            AdvantageContainerType type = advantage.getContainerType();
            if (type == AdvantageContainerType.GROUP) {
                for (Advantage child : new FilteredIterator<>(advantage.getChildren(), Advantage.class)) {
                    calculateSingleAdvantagePoints(child);
                }
                return;
            } else if (type == AdvantageContainerType.RACE) {
                mCachedRacePoints += advantage.getAdjustedPoints();
                return;
            }
        }

        int pts = advantage.getAdjustedPoints();
        if (pts > 0) {
            mCachedAdvantagePoints += pts;
        } else if (pts < -1) {
            mCachedDisadvantagePoints += pts;
        } else if (pts == -1) {
            mCachedQuirkPoints--;
        }
    }

    /** @return The number of points spent on skills. */
    public int getSkillPoints() {
        return mCachedSkillPoints;
    }

    private void calculateSkillPoints() {
        mCachedSkillPoints = 0;
        for (Skill skill : getSkillsIterator()) {
            mCachedSkillPoints += skill.getRawPoints();
        }
    }

    /** @return The number of points spent on spells. */
    public int getSpellPoints() {
        return mCachedSpellPoints;
    }

    private void calculateSpellPoints() {
        mCachedSpellPoints = 0;
        for (Spell spell : getSpellsIterator()) {
            mCachedSpellPoints += spell.getRawPoints();
        }
    }

    public int getCurrentHitPoints() {
        return getHitPointsAdj() - getHitPointsDamage();
    }

    /** @return The hit points (HP). */
    public int getHitPointsAdj() {
        return getStrength() + mHitPointsAdj + mHitPointBonus;
    }

    /** @return The number of points spent on hit points. */
    public int getHitPointPoints() {
        int pts = 2 * mHitPointsAdj;
        if (!mSettings.useKnowYourOwnStrength()) {
            int sizeModifier = mProfile.getSizeModifier();
            if (sizeModifier > 0) {
                int rem;
                if (sizeModifier > 8) {
                    sizeModifier = 8;
                }
                pts *= 10 - sizeModifier;
                rem = pts % 10;
                pts /= 10;
                if (rem > 4) {
                    pts++;
                } else if (rem < -5) {
                    pts--;
                }
            }
        }
        return pts;
    }

    /** @return The hit point bonus. */
    public int getHitPointBonus() {
        return mHitPointBonus;
    }

    /** @return The hit points damage. */
    public int getHitPointsDamage() {
        return mHitPointsDamage;
    }

    /** @return The number of hit points where "reeling" effects start. */
    public int getReelingHitPoints() {
        int hp        = getHitPointsAdj();
        int threshold = hp / 3;
        if (hp % 3 != 0) {
            threshold++;
        }
        return Math.max(--threshold, 0);
    }

    public boolean isReeling() {
        return getCurrentHitPoints() <= getReelingHitPoints();
    }

    public boolean isCollapsedFromHP() {
        return getCurrentHitPoints() <= getUnconsciousChecksHitPoints();
    }

    public boolean isDeathCheck1() {
        return getCurrentHitPoints() <= getDeathCheck1HitPoints();
    }

    public boolean isDeathCheck2() {
        return getCurrentHitPoints() <= getDeathCheck2HitPoints();
    }

    public boolean isDeathCheck3() {
        return getCurrentHitPoints() <= getDeathCheck3HitPoints();
    }

    public boolean isDeathCheck4() {
        return getCurrentHitPoints() <= getDeathCheck4HitPoints();
    }

    public boolean isDead() {
        return getCurrentHitPoints() <= getDeadHitPoints();
    }

    /** @return The number of hit points where unconsciousness checks must start being made. */
    @SuppressWarnings("static-method")
    public int getUnconsciousChecksHitPoints() {
        return 0;
    }

    /** @return The number of hit points where the first death check must be made. */
    public int getDeathCheck1HitPoints() {
        return -1 * getHitPointsAdj();
    }

    /** @return The number of hit points where the second death check must be made. */
    public int getDeathCheck2HitPoints() {
        return -2 * getHitPointsAdj();
    }

    /** @return The number of hit points where the third death check must be made. */
    public int getDeathCheck3HitPoints() {
        return -3 * getHitPointsAdj();
    }

    /** @return The number of hit points where the fourth death check must be made. */
    public int getDeathCheck4HitPoints() {
        return -4 * getHitPointsAdj();
    }

    /** @return The number of hit points where the character is just dead. */
    public int getDeadHitPoints() {
        return -5 * getHitPointsAdj();
    }

    /** @return The will. */
    public int getWillAdj() {
        return mWillAdj + mWillBonus + (mSettings.baseWillAndPerOn10() ? 10 : getIntelligence());
    }

    /** @return The will bonus. */
    public int getWillBonus() {
        return mWillBonus;
    }

    /** @return The number of points spent on will. */
    public int getWillPoints() {
        return mWillAdj * 5;
    }

    /** @return The fright check. */
    public int getFrightCheck() {
        return getWillAdj() + mFrightCheckBonus;
    }

    /** @return The fright check bonus. */
    public int getFrightCheckBonus() {
        return mFrightCheckBonus;
    }

    /** @return The vision. */
    public int getVision() {
        return getPerAdj() + mVisionBonus;
    }

    /** @return The vision bonus. */
    public int getVisionBonus() {
        return mVisionBonus;
    }

    /** @return The hearing. */
    public int getHearing() {
        return getPerAdj() + mHearingBonus;
    }

    /** @return The hearing bonus. */
    public int getHearingBonus() {
        return mHearingBonus;
    }

    /** @return The touch perception. */
    public int getTouch() {
        return getPerAdj() + mTouchBonus;
    }

    /** @return The touch bonus. */
    public int getTouchBonus() {
        return mTouchBonus;
    }

    /** @return The taste and smell perception. */
    public int getTasteAndSmell() {
        return getPerAdj() + mTasteAndSmellBonus;
    }

    /** @return The taste and smell bonus. */
    public int getTasteAndSmellBonus() {
        return mTasteAndSmellBonus;
    }

    /** @return The perception (Per). */
    public int getPerAdj() {
        return mPerAdj + mPerceptionBonus + (mSettings.baseWillAndPerOn10() ? 10 : getIntelligence());
    }

    /** @return The perception bonus. */
    public int getPerceptionBonus() {
        return mPerceptionBonus;
    }

    /** @return The number of points spent on perception. */
    public int getPerceptionPoints() {
        return mPerAdj * 5;
    }

    public int getCurrentFatiguePoints() {
        return getFatiguePoints() - getFatiguePointsDamage();
    }

    /** @return The fatigue points (FP). */
    public int getFatiguePoints() {
        return getHealth() + mFatiguePoints + mFatiguePointBonus;
    }

    /** @return The number of points spent on fatigue points. */
    public int getFatiguePointPoints() {
        return 3 * mFatiguePoints;
    }

    /** @return The fatigue point bonus. */
    public int getFatiguePointBonus() {
        return mFatiguePointBonus;
    }

    /** @return The fatigue points damage. */
    public int getFatiguePointsDamage() {
        return mFatiguePointsDamage;
    }

    /** @return The number of fatigue points where "tired" effects start. */
    public int getTiredFatiguePoints() {
        int fp        = getFatiguePoints();
        int threshold = fp / 3;
        if (fp % 3 != 0) {
            threshold++;
        }
        return Math.max(--threshold, 0);
    }

    public boolean isTired() {
        return getCurrentFatiguePoints() <= getTiredFatiguePoints();
    }

    public boolean isCollapsedFromFP() {
        return getCurrentFatiguePoints() <= getUnconsciousChecksFatiguePoints();
    }

    public boolean isUnconscious() {
        return getCurrentFatiguePoints() <= getUnconsciousFatiguePoints();
    }

    /** @return The number of fatigue points where unconsciousness checks must start being made. */
    @SuppressWarnings("static-method")
    public int getUnconsciousChecksFatiguePoints() {
        return 0;
    }

    /** @return The number of hit points where the character falls over, unconscious. */
    public int getUnconsciousFatiguePoints() {
        return -1 * getFatiguePoints();
    }

    /** @return The {@link Profile} data. */
    public Profile getProfile() {
        return mProfile;
    }

    public Settings getSettings() {
        return mSettings;
    }

    /** @return The {@link Armor} stats. */
    public Armor getArmor() {
        return mArmor;
    }

    /** @return The outline model for the character's advantages. */
    public OutlineModel getAdvantagesModel() {
        return mAdvantages;
    }

    /**
     * @param includeDisabled {@code true} if disabled entries should be included.
     * @return A recursive iterator over the character's advantages.
     */
    public RowIterator<Advantage> getAdvantagesIterator(boolean includeDisabled) {
        if (includeDisabled) {
            return new RowIterator<>(mAdvantages);
        }
        return new RowIterator<>(mAdvantages, (row) -> row.isEnabled());
    }

    /**
     * Searches the character's current advantages list for the specified name.
     *
     * @param name The name to look for.
     * @return The advantage, if present, or {@code null}.
     */
    public Advantage getAdvantageNamed(String name) {
        for (Advantage advantage : getAdvantagesIterator(false)) {
            if (advantage.getName().equals(name)) {
                return advantage;
            }
        }
        return null;
    }

    /**
     * Searches the character's current advantages list for the specified name.
     *
     * @param name The name to look for.
     * @return Whether it is present or not.
     */
    public boolean hasAdvantageNamed(String name) {
        return getAdvantageNamed(name) != null;
    }

    /** @return The outline model for the character's skills. */
    public OutlineModel getSkillsRoot() {
        return mSkills;
    }

    /** @return A recursive iterable for the character's skills. */
    public RowIterator<Skill> getSkillsIterator() {
        return new RowIterator<>(mSkills);
    }

    /**
     * Searches the character's current skill list for the specified name.
     *
     * @param name           The name to look for.
     * @param specialization The specialization to look for. Pass in {@code null} or an empty string
     *                       to ignore.
     * @param requirePoints  Only look at {@link Skill}s that have points. {@link Technique}s,
     *                       however, still won't need points even if this is {@code true}.
     * @param excludes       The set of {@link Skill}s to exclude from consideration.
     * @return The skill if it is present, or {@code null} if its not.
     */
    public List<Skill> getSkillNamed(String name, String specialization, boolean requirePoints, Set<String> excludes) {
        List<Skill> skills              = new ArrayList<>();
        boolean     checkSpecialization = specialization != null && !specialization.isEmpty();
        for (Skill skill : getSkillsIterator()) {
            if (!skill.canHaveChildren()) {
                if (excludes == null || !excludes.contains(skill.toString())) {
                    if (!requirePoints || skill instanceof Technique || skill.getPoints() > 0) {
                        if (skill.getName().equalsIgnoreCase(name)) {
                            if (!checkSpecialization || skill.getSpecialization().equalsIgnoreCase(specialization)) {
                                skills.add(skill);
                            }
                        }
                    }
                }
            }
        }
        return skills;
    }

    /**
     * Searches the character's current {@link Skill} list for the {@link Skill} with the best level
     * that matches the name.
     *
     * @param name           The {@link Skill} name to look for.
     * @param specialization An optional specialization to look for. Pass {@code null} if it is not
     *                       needed.
     * @param requirePoints  Only look at {@link Skill}s that have points. {@link Technique}s,
     *                       however, still won't need points even if this is {@code true}.
     * @param excludes       The set of {@link Skill}s to exclude from consideration.
     * @return The {@link Skill} that matches with the highest level.
     */
    public Skill getBestSkillNamed(String name, String specialization, boolean requirePoints, Set<String> excludes) {
        Skill best  = null;
        int   level = Integer.MIN_VALUE;
        for (Skill skill : getSkillNamed(name, specialization, requirePoints, excludes)) {
            int skillLevel = skill.getLevel(excludes);
            if (best == null || skillLevel > level) {
                best = skill;
                level = skillLevel;
            }
        }
        return best;
    }

    /** @return The outline model for the character's spells. */
    public OutlineModel getSpellsRoot() {
        return mSpells;
    }

    /** @return A recursive iterator over the character's spells. */
    public RowIterator<Spell> getSpellsIterator() {
        return new RowIterator<>(mSpells);
    }

    /** @return The outline model for the character's equipment. */
    public OutlineModel getEquipmentRoot() {
        return mEquipment;
    }

    /** @return A recursive iterator over the character's equipment. */
    public RowIterator<Equipment> getEquipmentIterator() {
        return new RowIterator<>(mEquipment);
    }

    /** @return The outline model for the character's other equipment. */
    public OutlineModel getOtherEquipmentRoot() {
        return mOtherEquipment;
    }

    /** @return A recursive iterator over the character's other equipment. */
    public RowIterator<Equipment> getOtherEquipmentIterator() {
        return new RowIterator<>(mOtherEquipment);
    }

    /** @return The outline model for the character's notes. */
    public OutlineModel getNotesRoot() {
        return mNotes;
    }

    /** @return A recursive iterator over the character's notes. */
    public RowIterator<Note> getNoteIterator() {
        return new RowIterator<>(mNotes);
    }

    /**
     * @param id The cost reduction ID to search for.
     * @return The cost reduction, as a percentage.
     */
    public int getCostReductionFor(String id) {
        int           total = 0;
        List<Feature> list  = mFeatureMap.get(id.toLowerCase());

        if (list != null) {
            for (Feature feature : list) {
                if (feature instanceof CostReduction) {
                    total += ((CostReduction) feature).getPercentage();
                }
            }
        }
        if (total > 80) {
            total = 80;
        }
        return total;
    }

    /**
     * @param id The feature ID to search for.
     * @return The bonus.
     */
    public int getIntegerBonusFor(String id) {
        return getIntegerBonusFor(id, null);
    }

    /**
     * @param id      The feature ID to search for.
     * @param toolTip The toolTip being built.
     * @return The bonus.
     */
    public int getIntegerBonusFor(String id, StringBuilder toolTip) {
        int           total = 0;
        List<Feature> list  = mFeatureMap.get(id.toLowerCase());
        if (list != null) {
            for (Feature feature : list) {
                if (feature instanceof Bonus && !(feature instanceof WeaponBonus)) {
                    Bonus bonus = (Bonus) feature;
                    total += bonus.getAmount().getIntegerAdjustedAmount();
                    bonus.addToToolTip(toolTip);
                }
            }
        }
        return total;
    }

    /**
     * @param id                      The feature ID to search for.
     * @param nameQualifier           The name qualifier.
     * @param specializationQualifier The specialization qualifier.
     * @param categoriesQualifier     The categories qualifier.
     * @return The bonuses.
     */
    public List<WeaponBonus> getWeaponComparedBonusesFor(String id, String nameQualifier, String specializationQualifier, Set<String> categoriesQualifier, StringBuilder toolTip) {
        List<WeaponBonus> bonuses = new ArrayList<>();
        int               rsl     = Integer.MIN_VALUE;

        for (Skill skill : getSkillNamed(nameQualifier, specializationQualifier, true, null)) {
            int srsl = skill.getRelativeLevel();

            if (srsl > rsl) {
                rsl = srsl;
            }
        }

        if (rsl != Integer.MIN_VALUE) {
            List<Feature> list = mFeatureMap.get(id.toLowerCase());
            if (list != null) {
                for (Feature feature : list) {
                    if (feature instanceof WeaponBonus) {
                        WeaponBonus bonus = (WeaponBonus) feature;
                        if (bonus.getNameCriteria().matches(nameQualifier) && bonus.getSpecializationCriteria().matches(specializationQualifier) && bonus.getLevelCriteria().matches(rsl) && bonus.matchesCategories(categoriesQualifier)) {
                            bonuses.add(bonus);
                            bonus.addToToolTip(toolTip);
                        }
                    }
                }
            }
        }
        return bonuses;
    }

    /**
     * @param id                  The feature ID to search for.
     * @param nameQualifier       The name qualifier.
     * @param usageQualifier      The usage qualifier.
     * @param categoriesQualifier The categories qualifier.
     * @return The bonuses.
     */
    public List<WeaponBonus> getNamedWeaponBonusesFor(String id, String nameQualifier, String usageQualifier, Set<String> categoriesQualifier, StringBuilder toolTip) {
        List<WeaponBonus> bonuses = new ArrayList<>();
        List<Feature>     list    = mFeatureMap.get(id.toLowerCase());
        if (list != null) {
            for (Feature feature : list) {
                if (feature instanceof WeaponBonus) {
                    WeaponBonus bonus = (WeaponBonus) feature;
                    if (bonus.getWeaponSelectionType() == WeaponSelectionType.WEAPONS_WITH_NAME && bonus.getNameCriteria().matches(nameQualifier) && bonus.getSpecializationCriteria().matches(usageQualifier) && bonus.matchesCategories(categoriesQualifier)) {
                        bonuses.add(bonus);
                        bonus.addToToolTip(toolTip);
                    }
                }
            }
        }
        return bonuses;
    }

    /**
     * @param id                  The feature ID to search for.
     * @param nameQualifier       The name qualifier.
     * @param usageQualifier      The usage qualifier.
     * @param categoriesQualifier The categories qualifier.
     * @return The bonuses.
     */
    public List<SkillBonus> getNamedWeaponSkillBonusesFor(String id, String nameQualifier, String usageQualifier, Set<String> categoriesQualifier, StringBuilder toolTip) {
        List<SkillBonus> bonuses = new ArrayList<>();
        List<Feature>    list    = mFeatureMap.get(id.toLowerCase());
        if (list != null) {
            for (Feature feature : list) {
                if (feature instanceof SkillBonus) {
                    SkillBonus bonus = (SkillBonus) feature;
                    if (bonus.getSkillSelectionType() == SkillSelectionType.WEAPONS_WITH_NAME && bonus.getNameCriteria().matches(nameQualifier) && bonus.getSpecializationCriteria().matches(usageQualifier) && bonus.matchesCategories(categoriesQualifier)) {
                        bonuses.add(bonus);
                        bonus.addToToolTip(toolTip);
                    }
                }
            }
        }
        return bonuses;
    }

    /**
     * @param id                      The feature ID to search for.
     * @param nameQualifier           The name qualifier.
     * @param specializationQualifier The specialization qualifier.
     * @param categoryQualifier       The categories qualifier
     * @return The bonus.
     */
    public int getSkillComparedIntegerBonusFor(String id, String nameQualifier, String specializationQualifier, Set<String> categoryQualifier) {
        return getSkillComparedIntegerBonusFor(id, nameQualifier, specializationQualifier, categoryQualifier, null);
    }

    /**
     * @param id                      The feature ID to search for.
     * @param nameQualifier           The name qualifier.
     * @param specializationQualifier The specialization qualifier.
     * @param categoryQualifier       The categories qualifier
     * @param toolTip                 The toolTip being built
     * @return The bonus.
     */
    public int getSkillComparedIntegerBonusFor(String id, String nameQualifier, String specializationQualifier, Set<String> categoryQualifier, StringBuilder toolTip) {
        int           total = 0;
        List<Feature> list  = mFeatureMap.get(id.toLowerCase());
        if (list != null) {
            for (Feature feature : list) {
                if (feature instanceof SkillBonus) {
                    SkillBonus bonus = (SkillBonus) feature;
                    if (bonus.getNameCriteria().matches(nameQualifier) && bonus.getSpecializationCriteria().matches(specializationQualifier) && bonus.matchesCategories(categoryQualifier)) {
                        total += bonus.getAmount().getIntegerAdjustedAmount();
                        bonus.addToToolTip(toolTip);
                    }
                }
            }
        }
        return total;
    }

    /**
     * @param id                      The feature ID to search for.
     * @param nameQualifier           The name qualifier.
     * @param specializationQualifier The specialization qualifier.
     * @param categoryQualifier       The categories qualifier
     * @return The bonus.
     */
    public int getSkillPointComparedIntegerBonusFor(String id, String nameQualifier, String specializationQualifier, Set<String> categoryQualifier) {
        return getSkillPointComparedIntegerBonusFor(id, nameQualifier, specializationQualifier, categoryQualifier, null);
    }

    /**
     * @param id                      The feature ID to search for.
     * @param nameQualifier           The name qualifier.
     * @param specializationQualifier The specialization qualifier.
     * @param categoryQualifier       The categories qualifier
     * @param toolTip                 The toolTip being built
     * @return The point bonus.
     */
    public int getSkillPointComparedIntegerBonusFor(String id, String nameQualifier, String specializationQualifier, Set<String> categoryQualifier, StringBuilder toolTip) {
        int           total = 0;
        List<Feature> list  = mFeatureMap.get(id.toLowerCase());
        if (list != null) {
            for (Feature feature : list) {
                if (feature instanceof SkillPointBonus) {
                    SkillPointBonus bonus = (SkillPointBonus) feature;
                    if (bonus.getNameCriteria().matches(nameQualifier) && bonus.getSpecializationCriteria().matches(specializationQualifier) && bonus.matchesCategories(categoryQualifier)) {
                        total += bonus.getAmount().getIntegerAdjustedAmount();
                        bonus.addToToolTip(toolTip);
                    }
                }
            }
        }
        return total;
    }

    /**
     * @param id         The feature ID to search for.
     * @param qualifier  The qualifier.
     * @param categories The categories qualifier
     * @return The bonus.
     */
    public int getSpellComparedIntegerBonusFor(String id, String qualifier, Set<String> categories, StringBuilder toolTip) {
        int           total = 0;
        List<Feature> list  = mFeatureMap.get(id.toLowerCase());
        if (list != null) {
            for (Feature feature : list) {
                if (feature instanceof SpellBonus) {
                    SpellBonus bonus = (SpellBonus) feature;
                    if (bonus.getNameCriteria().matches(qualifier) && bonus.matchesCategories(categories)) {
                        total += bonus.getAmount().getIntegerAdjustedAmount();
                        bonus.addToToolTip(toolTip);
                    }
                }
            }
        }
        return total;
    }

    /**
     * @param id         The feature ID to search for.
     * @param qualifier  The qualifier.
     * @param categories The categories qualifier
     * @return The bonus.
     */
    public int getSpellPointComparedIntegerBonusFor(String id, String qualifier, Set<String> categories) {
        return getSpellPointComparedIntegerBonusFor(id, qualifier, categories, null);
    }

    /**
     * @param id         The feature ID to search for.
     * @param qualifier  The qualifier.
     * @param categories The categories qualifier
     * @param toolTip    The toolTip being built
     * @return The point bonus.
     */
    public int getSpellPointComparedIntegerBonusFor(String id, String qualifier, Set<String> categories, StringBuilder toolTip) {
        int           total = 0;
        List<Feature> list  = mFeatureMap.get(id.toLowerCase());
        if (list != null) {
            for (Feature feature : list) {
                if (feature instanceof SpellPointBonus) {
                    SpellPointBonus bonus = (SpellPointBonus) feature;
                    if (bonus.getNameCriteria().matches(qualifier) && bonus.matchesCategories(categories)) {
                        total += bonus.getAmount().getIntegerAdjustedAmount();
                        bonus.addToToolTip(toolTip);
                    }
                }
            }
        }
        return total;
    }

    /**
     * @param id The feature ID to search for.
     * @return The bonus.
     */
    public double getDoubleBonusFor(String id) {
        double        total = 0;
        List<Feature> list  = mFeatureMap.get(id.toLowerCase());
        if (list != null) {
            for (Feature feature : list) {
                if (feature instanceof Bonus && !(feature instanceof WeaponBonus)) {
                    total += ((Bonus) feature).getAmount().getAdjustedAmount();
                }
            }
        }
        return total;
    }

    @Override
    public WeightUnits defaultWeightUnits() {
        return mSettings.defaultWeightUnits();
    }

    public boolean useSimpleMetricConversions() {
        return mSettings.useSimpleMetricConversions();
    }

    public boolean useMultiplicativeModifiers() {
        return mSettings.useMultiplicativeModifiers();
    }

    public boolean useModifyingDicePlusAdds() {
        return mSettings.useModifyingDicePlusAdds();
    }
}
