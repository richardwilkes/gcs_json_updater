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

package com.trollworks.gcs.modifier;

/** Describes how a {@link AdvantageModifier} affects the point cost. */
public enum Affects {
    /** Affects the total cost. */
    TOTAL {
        @Override
        public String toString() {
            return "to cost";
        }

        @Override
        public String getShortTitle() {
            return "";
        }
    },
    /** Affects only the base cost, not the leveled cost. */
    BASE_ONLY {
        @Override
        public String toString() {
            return "to base cost only";
        }

        @Override
        public String getShortTitle() {
            return "(base only)";
        }
    },
    /** Affects only the leveled cost, not the base cost. */
    LEVELS_ONLY {
        @Override
        public String toString() {
            return "to leveled cost only";
        }

        @Override
        public String getShortTitle() {
            return "(levels only)";
        }
    };

    /** @return The short version of the title. */
    public abstract String getShortTitle();
}
