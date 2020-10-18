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

import com.trollworks.gcs.feature.HitLocation;

/** Tracks the current armor levels. */
public class Armor {
    /** The prefix used in front of all IDs for damage resistance. */
    public static final  String DR_PREFIX                   = GURPSCharacter.CHARACTER_PREFIX + "dr.";
    /** The skull hit location's DR. */
    public static final  String ID_SKULL_DR                 = DR_PREFIX + HitLocation.SKULL.name();
    /** The eyes hit location's DR. */
    public static final  String ID_EYES_DR                  = DR_PREFIX + HitLocation.EYES.name();
    /** The face hit location's DR. */
    public static final  String ID_FACE_DR                  = DR_PREFIX + HitLocation.FACE.name();
    /** The neck hit location's DR. */
    public static final  String ID_NECK_DR                  = DR_PREFIX + HitLocation.NECK.name();
    /** The torso hit location's DR. */
    public static final  String ID_TORSO_DR                 = DR_PREFIX + HitLocation.TORSO.name();
    /** The vitals hit location's DR. */
    public static final  String ID_VITALS_DR                = DR_PREFIX + HitLocation.VITALS.name();
    private static final String ID_FULL_BODY_DR             = DR_PREFIX + HitLocation.FULL_BODY.name();
    private static final String ID_FULL_BODY_EXCEPT_EYES_DR = DR_PREFIX + HitLocation.FULL_BODY_EXCEPT_EYES.name();
    /** The groin hit location's DR. */
    public static final  String ID_GROIN_DR                 = DR_PREFIX + HitLocation.GROIN.name();
    /** The arm hit location's DR. */
    public static final  String ID_ARM_DR                   = DR_PREFIX + HitLocation.ARMS.name();
    /** The hand hit location's DR. */
    public static final  String ID_HAND_DR                  = DR_PREFIX + HitLocation.HANDS.name();
    /** The leg hit location's DR. */
    public static final  String ID_LEG_DR                   = DR_PREFIX + HitLocation.LEGS.name();
    /** The foot hit location's DR. */
    public static final  String ID_FOOT_DR                  = DR_PREFIX + HitLocation.FEET.name();
    /** The tail hit location's DR. */
    public static final  String ID_TAIL_DR                  = DR_PREFIX + HitLocation.TAIL.name();
    /** The wing hit location's DR. */
    public static final  String ID_WING_DR                  = DR_PREFIX + HitLocation.WINGS.name();
    /** The fin hit location's DR. */
    public static final  String ID_FIN_DR                   = DR_PREFIX + HitLocation.FINS.name();
    /** The brain hit location's DR. */
    public static final  String ID_BRAIN_DR                 = DR_PREFIX + HitLocation.BRAIN.name();

    private GURPSCharacter mCharacter;
    private int            mBrainDR;
    private int            mSkullDR;
    private int            mEyesDR;
    private int            mFaceDR;
    private int            mNeckDR;
    private int            mTorsoDR;
    private int            mVitalsDR;
    private int            mGroinDR;
    private int            mArmDR;
    private int            mWingDR;
    private int            mHandDR;
    private int            mFinDR;
    private int            mLegDR;
    private int            mFootDR;
    private int            mTailDR;

    Armor(GURPSCharacter character) {
        mCharacter = character;
        mSkullDR = 2;
    }
}
