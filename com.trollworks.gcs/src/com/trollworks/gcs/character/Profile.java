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

import com.trollworks.gcs.feature.BonusAttributeType;
import com.trollworks.gcs.notes.Note;
import com.trollworks.gcs.utility.Fixed6;
import com.trollworks.gcs.utility.json.JsonWriter;
import com.trollworks.gcs.utility.text.Text;
import com.trollworks.gcs.utility.units.LengthValue;
import com.trollworks.gcs.utility.units.WeightValue;
import com.trollworks.gcs.utility.xml.XMLNodeType;
import com.trollworks.gcs.utility.xml.XMLReader;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/** Holds the character profile. */
public class Profile {
    /** The root XML tag. */
    public static final  String         TAG_ROOT                  = "profile";
    private static final String         KEY_SM                    = "SM";
    /** The prefix used in front of all IDs for profile. */
    public static final  String         PROFILE_PREFIX            = GURPSCharacter.CHARACTER_PREFIX + "pi.";
    /** The field ID for portrait changes. */
    public static final  String         ID_PORTRAIT               = PROFILE_PREFIX + "Portrait";
    /** The field ID for name changes. */
    public static final  String         ID_NAME                   = PROFILE_PREFIX + "Name";
    /** The field ID for title changes. */
    public static final  String         ID_TITLE                  = PROFILE_PREFIX + "Title";
    /** The field ID for age changes. */
    public static final  String         ID_AGE                    = PROFILE_PREFIX + "Age";
    /** The field ID for birthday changes. */
    public static final  String         ID_BIRTHDAY               = PROFILE_PREFIX + "Birthday";
    /** The field ID for eye color changes. */
    public static final  String         ID_EYE_COLOR              = PROFILE_PREFIX + "EyeColor";
    /** The field ID for hair color changes. */
    public static final  String         ID_HAIR                   = PROFILE_PREFIX + "Hair";
    /** The field ID for skin color changes. */
    public static final  String         ID_SKIN_COLOR             = PROFILE_PREFIX + "SkinColor";
    /** The field ID for handedness changes. */
    public static final  String         ID_HANDEDNESS             = PROFILE_PREFIX + "Handedness";
    /** The field ID for height changes. */
    public static final  String         ID_HEIGHT                 = PROFILE_PREFIX + "Height";
    /** The field ID for weight changes. */
    public static final  String         ID_WEIGHT                 = PROFILE_PREFIX + "Weight";
    /** The field ID for gender changes. */
    public static final  String         ID_GENDER                 = PROFILE_PREFIX + "Gender";
    /** The field ID for religion changes. */
    public static final  String         ID_RELIGION               = PROFILE_PREFIX + "Religion";
    /** The field ID for player name changes. */
    public static final  String         ID_PLAYER_NAME            = PROFILE_PREFIX + "PlayerName";
    /** The field ID for tech level changes. */
    public static final  String         ID_TECH_LEVEL             = PROFILE_PREFIX + "TechLevel";
    /** The field ID for size modifier changes. */
    public static final  String         ID_SIZE_MODIFIER          = PROFILE_PREFIX + BonusAttributeType.SM.name();
    /** The field ID for body type changes. */
    public static final  String         ID_BODY_TYPE              = PROFILE_PREFIX + "BodyType";
    /** The height, in 1/72nds of an inch, of the portrait. */
    public static final  int            PORTRAIT_HEIGHT           = 96;
    /** The width, in 1/72nds of an inch, of the portrait. */
    public static final  int            PORTRAIT_WIDTH            = 3 * PORTRAIT_HEIGHT / 4;
    private static final String         TAG_PLAYER_NAME           = "player_name";
    private static final String         TAG_NAME                  = "name";
    private static final String         TAG_TITLE                 = "title";
    private static final String         TAG_AGE                   = "age";
    private static final String         TAG_BIRTHDAY              = "birthday";
    private static final String         TAG_EYES                  = "eyes";
    private static final String         TAG_HAIR                  = "hair";
    private static final String         TAG_SKIN                  = "skin";
    private static final String         TAG_HANDEDNESS            = "handedness";
    private static final String         TAG_HEIGHT                = "height";
    private static final String         TAG_WEIGHT                = "weight";
    private static final String         TAG_GENDER                = "gender";
    private static final String         TAG_TECH_LEVEL            = "tech_level";
    private static final String         TAG_RELIGION              = "religion";
    private static final String         TAG_PORTRAIT              = "portrait";
    private static final String         TAG_OLD_NOTES             = "notes";
    private static final String         TAG_BODY_TYPE             = "body_type";
    public static final  Set<String>    VALID_HIT_LOCATION_TABLES = new HashSet<>(Arrays.asList("humanoid", "quadruped", "winged_quadruped", "hexapod", "winged_hexapod", "centaur", "avian", "vermiform", "winged_vermiform", "snakemen", "octopod", "squid", "cancroid", "scorpion", "ichthyoid", "arachnoid"));
    private              GURPSCharacter mCharacter;
    private              boolean        mCustomPortrait;
    private              byte[]         mPortrait;
    private              String         mName;
    private              String         mTitle;
    private              int            mAge;
    private              String         mBirthday;
    private              String         mEyeColor;
    private              String         mHair;
    private              String         mSkinColor;
    private              String         mHandedness;
    private              LengthValue    mHeight;
    private              WeightValue    mWeight;
    private              int            mSizeModifier;
    private              int            mSizeModifierBonus;
    private              String         mGender;
    private              String         mReligion;
    private              String         mPlayerName;
    private              String         mTechLevel;
    private              String         mHitLocationTable;

    Profile(GURPSCharacter character) {
        mCharacter = character;
        mCustomPortrait = false;
        mPortrait = null;
        mTitle = "";
        mAge = 0;
        mBirthday = "";
        mEyeColor = "";
        mHair = "";
        mSkinColor = "";
        mHandedness = "";
        Settings settings = mCharacter.getSettings();
        mHeight = new LengthValue(Fixed6.ZERO, settings.defaultLengthUnits());
        mWeight = new WeightValue(Fixed6.ZERO, settings.defaultWeightUnits());
        mGender = "";
        mName = "";
        mTechLevel = "";
        mReligion = "";
        mPlayerName = "";
        mPortrait = null;
        mHitLocationTable = "humanoid";
    }

    void load(XMLReader reader) throws IOException {
        String marker = reader.getMarker();
        do {
            if (reader.next() == XMLNodeType.START_TAG) {
                String tag = reader.getName();
                if (!loadTag(reader, tag)) {
                    reader.skipTag(tag);
                }
            }
        } while (reader.withinMarker(marker));
    }

    boolean loadTag(XMLReader reader, String tag) throws IOException {
        if (TAG_PLAYER_NAME.equals(tag)) {
            mPlayerName = reader.readText();
        } else if (TAG_NAME.equals(tag)) {
            mName = reader.readText();
        } else if (TAG_OLD_NOTES.equals(tag)) {
            Note note = new Note(mCharacter, false);
            note.setDescription(Text.standardizeLineEndings(reader.readText()));
            mCharacter.getNotesRoot().addRow(note, false);
        } else if (TAG_TITLE.equals(tag)) {
            mTitle = reader.readText();
        } else if (TAG_AGE.equals(tag)) {
            mAge = reader.readInteger(0);
        } else if (TAG_BIRTHDAY.equals(tag)) {
            mBirthday = reader.readText();
        } else if (TAG_EYES.equals(tag)) {
            mEyeColor = reader.readText();
        } else if (TAG_HAIR.equals(tag)) {
            mHair = reader.readText();
        } else if (TAG_SKIN.equals(tag)) {
            mSkinColor = reader.readText();
        } else if (TAG_HANDEDNESS.equals(tag)) {
            mHandedness = reader.readText();
        } else if (TAG_HEIGHT.equals(tag)) {
            mHeight = LengthValue.extract(reader.readText(), false);
        } else if (TAG_WEIGHT.equals(tag)) {
            mWeight = WeightValue.extract(reader.readText(), false);
        } else if (BonusAttributeType.SM.getXMLTag().equals(tag) || "size_modifier".equals(tag)) {
            mSizeModifier = reader.readInteger(0);
        } else if (TAG_GENDER.equals(tag)) {
            mGender = reader.readText();
        } else if (TAG_BODY_TYPE.equals(tag)) {
            mHitLocationTable = reader.readText();
            if (!VALID_HIT_LOCATION_TABLES.contains(mHitLocationTable)) {
                mHitLocationTable = "humanoid";
            }
        } else if (TAG_TECH_LEVEL.equals(tag)) {
            mTechLevel = reader.readText();
        } else if (TAG_RELIGION.equals(tag)) {
            mReligion = reader.readText();
        } else if (TAG_PORTRAIT.equals(tag)) {
            mPortrait = Base64.getMimeDecoder().decode(reader.readText());
            mCustomPortrait = true;
        } else {
            return false;
        }
        return true;
    }

    void save(JsonWriter w) throws IOException {
        w.startMap();
        w.keyValueNot(TAG_PLAYER_NAME, mPlayerName, "");
        w.keyValueNot(TAG_NAME, mName, "");
        w.keyValueNot(TAG_TITLE, mTitle, "");
        w.keyValueNot(TAG_AGE, mAge, 0);
        w.keyValueNot(TAG_BIRTHDAY, mBirthday, "");
        w.keyValueNot(TAG_EYES, mEyeColor, "");
        w.keyValueNot(TAG_HAIR, mHair, "");
        w.keyValueNot(TAG_SKIN, mSkinColor, "");
        w.keyValueNot(TAG_HANDEDNESS, mHandedness, "");
        if (!mHeight.getNormalizedValue().equals(Fixed6.ZERO)) {
            w.keyValue(TAG_HEIGHT, mHeight.toString(false));
        }
        if (!mWeight.getNormalizedValue().equals(Fixed6.ZERO)) {
            w.keyValue(TAG_WEIGHT, mWeight.toString(false));
        }
        w.keyValueNot(KEY_SM, mSizeModifier, 0);
        w.keyValueNot(TAG_GENDER, mGender, "");
        w.keyValue(TAG_BODY_TYPE, mHitLocationTable);
        w.keyValueNot(TAG_TECH_LEVEL, mTechLevel, "");
        w.keyValueNot(TAG_RELIGION, mReligion, "");
        if (mCustomPortrait && mPortrait != null) {
            w.keyValue(TAG_PORTRAIT, Base64.getEncoder().encodeToString(mPortrait));
        }
        w.endMap();
    }

    void update() {
        setSizeModifierBonus(mCharacter.getIntegerBonusFor(GURPSCharacter.ATTRIBUTES_PREFIX + BonusAttributeType.SM.name()));
    }

    /** @return The name. */
    public String getName() {
        return mName;
    }

    /**
     * Sets the name.
     *
     * @param name The new name.
     */
    public void setName(String name) {
        if (!mName.equals(name)) {
            mName = name;
        }
    }

    /** @return The gender. */
    public String getGender() {
        return mGender;
    }

    /**
     * Sets the gender.
     *
     * @param gender The new gender.
     */
    public void setGender(String gender) {
        if (!mGender.equals(gender)) {
            mGender = gender;
        }
    }

    /** @return The religion. */
    public String getReligion() {
        return mReligion;
    }

    /**
     * Sets the religion.
     *
     * @param religion The new religion.
     */
    public void setReligion(String religion) {
        if (!mReligion.equals(religion)) {
            mReligion = religion;
        }
    }

    /** @return The player's name. */
    public String getPlayerName() {
        return mPlayerName;
    }

    /**
     * Sets the player's name.
     *
     * @param player The new player's name.
     */
    public void setPlayerName(String player) {
        if (!mPlayerName.equals(player)) {
            mPlayerName = player;
        }
    }

    /** @return The tech level. */
    public String getTechLevel() {
        return mTechLevel;
    }

    /**
     * Sets the tech level.
     *
     * @param techLevel The new tech level.
     */
    public void setTechLevel(String techLevel) {
        if (!mTechLevel.equals(techLevel)) {
            mTechLevel = techLevel;
        }
    }

    /** @return The title. */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Sets the title.
     *
     * @param title The new title.
     */
    public void setTitle(String title) {
        if (!mTitle.equals(title)) {
            mTitle = title;
        }
    }

    /** @return The age. */
    public int getAge() {
        return mAge;
    }

    /**
     * Sets the age.
     *
     * @param age The new age.
     */
    public void setAge(int age) {
        if (mAge != age) {
            Integer value = Integer.valueOf(age);
            mAge = age;
        }
    }

    /** @return The date of birth. */
    public String getBirthday() {
        return mBirthday;
    }

    /**
     * Sets the date of birth.
     *
     * @param birthday The new date of birth.
     */
    public void setBirthday(String birthday) {
        if (!mBirthday.equals(birthday)) {
            mBirthday = birthday;
        }
    }

    /** @return The eye color. */
    public String getEyeColor() {
        return mEyeColor;
    }

    /**
     * Sets the eye color.
     *
     * @param eyeColor The new eye color.
     */
    public void setEyeColor(String eyeColor) {
        if (!mEyeColor.equals(eyeColor)) {
            mEyeColor = eyeColor;
        }
    }

    /** @return The hair. */
    public String getHair() {
        return mHair;
    }

    /**
     * Sets the hair.
     *
     * @param hair The new hair.
     */
    public void setHair(String hair) {
        if (!mHair.equals(hair)) {
            mHair = hair;
        }
    }

    /** @return The skin color. */
    public String getSkinColor() {
        return mSkinColor;
    }

    /**
     * Sets the skin color.
     *
     * @param skinColor The new skin color.
     */
    public void setSkinColor(String skinColor) {
        if (!mSkinColor.equals(skinColor)) {
            mSkinColor = skinColor;
        }
    }

    /** @return The handedness. */
    public String getHandedness() {
        return mHandedness;
    }

    /**
     * Sets the handedness.
     *
     * @param handedness The new handedness.
     */
    public void setHandedness(String handedness) {
        if (!mHandedness.equals(handedness)) {
            mHandedness = handedness;
        }
    }

    /** @return The height. */
    public LengthValue getHeight() {
        return mHeight;
    }

    /**
     * Sets the height.
     *
     * @param height The new height.
     */
    public void setHeight(LengthValue height) {
        if (!mHeight.equals(height)) {
            height = new LengthValue(height);
            mHeight = height;
        }
    }

    /** @return The weight. */
    public WeightValue getWeight() {
        return mWeight;
    }

    /**
     * Sets the weight.
     *
     * @param weight The new weight.
     */
    public void setWeight(WeightValue weight) {
        if (!mWeight.equals(weight)) {
            weight = new WeightValue(weight);
            mWeight = weight;
        }
    }

    /** @return The multiplier compared to average weight for this character. */
    public Fixed6 getWeightMultiplier() {
        if (mCharacter.hasAdvantageNamed("Very Fat")) {
            return new Fixed6(2);
        } else if (mCharacter.hasAdvantageNamed("Fat")) {
            return new Fixed6("1.5", Fixed6.ZERO, false);
        } else if (mCharacter.hasAdvantageNamed("Overweight")) {
            return new Fixed6("1.3", Fixed6.ZERO, false);
        } else if (mCharacter.hasAdvantageNamed("Skinny")) {
            return new Fixed6("0.67", Fixed6.ZERO, false);
        }
        return Fixed6.ONE;
    }

    /** @return The size modifier. */
    public int getSizeModifier() {
        return mSizeModifier + mSizeModifierBonus;
    }

    /** @return The size modifier bonus. */
    public int getSizeModifierBonus() {
        return mSizeModifierBonus;
    }

    /** @param size The new size modifier. */
    public void setSizeModifier(int size) {
        int totalSizeModifier = getSizeModifier();
        if (totalSizeModifier != size) {
            Integer value = Integer.valueOf(size);
            mSizeModifier = size - mSizeModifierBonus;
        }
    }

    /** @param bonus The new size modifier bonus. */
    public void setSizeModifierBonus(int bonus) {
        if (mSizeModifierBonus != bonus) {
            mSizeModifierBonus = bonus;
        }
    }
}