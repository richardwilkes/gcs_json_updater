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

/** Valid encumbrance levels. */
public enum Encumbrance {
    NONE(1) {
        @Override
        public String toString() {
            return "None";
        }
    }, LIGHT(2) {
        @Override
        public String toString() {
            return "Light";
        }
    }, MEDIUM(3) {
        @Override
        public String toString() {
            return "Medium";
        }
    }, HEAVY(6) {
        @Override
        public String toString() {
            return "Heavy";
        }
    }, EXTRA_HEAVY(10) {
        @Override
        public String toString() {
            return "X-Heavy";
        }
    };

    private int mMultiplier;

    Encumbrance(int multiplier) {
        mMultiplier = multiplier;
    }

    /** @return The weight multiplier associated with this level of encumbrance. */
    public int getWeightMultiplier() {
        return mMultiplier;
    }

    /** @return The penalty associated with this level of encumbrance. */
    public int getEncumbrancePenalty() {
        return -ordinal();
    }
}
