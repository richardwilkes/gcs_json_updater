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

package com.trollworks.gcs.feature;

public enum SkillSelectionType {
    THIS_WEAPON {
        @Override
        public String toString() {
            return "to this weapon";
        }
    }, WEAPONS_WITH_NAME {
        @Override
        public String toString() {
            return "to weapons whose name";
        }
    }, SKILLS_WITH_NAME {
        @Override
        public String toString() {
            return "to skills whose name";
        }
    }
}
