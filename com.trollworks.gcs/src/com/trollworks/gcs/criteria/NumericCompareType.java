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

package com.trollworks.gcs.criteria;

import java.text.MessageFormat;

/** The allowed numeric comparison types. */
public enum NumericCompareType {
    /** The comparison for "is". */
    IS {
        @Override
        public String toString() {
            return "exactly";
        }

        @Override
        public String getDescription() {
            return "is";
        }

        @Override
        String getDescriptionFormat() {
            return "{0}exactly {1}";
        }
    },
    /** The comparison for "is at least". */
    AT_LEAST {
        @Override
        public String toString() {
            return "at least";
        }

        @Override
        public String getDescription() {
            return "is at least";
        }

        @Override
        String getDescriptionFormat() {
            return "{0}at least {1}";
        }
    },
    /** The comparison for "is at most". */
    AT_MOST {
        @Override
        public String toString() {
            return "at most";
        }

        @Override
        public String getDescription() {
            return "is at most";
        }

        @Override
        String getDescriptionFormat() {
            return "{0}at most {1}";
        }
    };

    /** @return A description of this object. */
    public abstract String getDescription();

    abstract String getDescriptionFormat();

    /**
     * @param prefix    A prefix to place before the description.
     * @param qualifier The qualifier to use.
     * @return A formatted description of this object.
     */
    public String format(String prefix, String qualifier) {
        return MessageFormat.format(getDescriptionFormat(), prefix, qualifier);
    }
}
