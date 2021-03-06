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

/** Hit locations. */
public enum HitLocation {
    /** The skull hit location. */
    SKULL {
        @Override
        public String toString() {
            return "to the skull";
        }
    },
    /** The eyes hit location. */
    EYES {
        @Override
        public String toString() {
            return "to the eyes";
        }
    },
    /** The face hit location. */
    FACE {
        @Override
        public String toString() {
            return "to the face";
        }
    },
    /** The neck hit location. */
    NECK {
        @Override
        public String toString() {
            return "to the neck";
        }
    },
    /** The torso hit location. */
    TORSO {
        @Override
        public String toString() {
            return "to the torso";
        }
    },
    /** The vitals hit location. */
    VITALS {
        @Override
        public String toString() {
            return "to the vitals";
        }

        @Override
        public boolean isChoosable() {
            return false;
        }
    },
    /** The groin hit location. */
    GROIN {
        @Override
        public String toString() {
            return "to the groin";
        }
    },
    /** The arm hit location. */
    ARMS {
        @Override
        public String toString() {
            return "to the arms";
        }
    },
    /** The hand hit location. */
    HANDS {
        @Override
        public String toString() {
            return "to the hands";
        }
    },
    /** The leg hit location. */
    LEGS {
        @Override
        public String toString() {
            return "to the legs";
        }
    },
    /** The foot hit location. */
    FEET {
        @Override
        public String toString() {
            return "to the feet";
        }
    },
    /** The tail hit location. */
    TAIL {
        @Override
        public String toString() {
            return "to the tail";
        }
    },
    /** The wing hit location. */
    WINGS {
        @Override
        public String toString() {
            return "to the wings";
        }
    },
    /** The fin hit location. */
    FINS {
        @Override
        public String toString() {
            return "to the fins";
        }
    },
    /** The brain hit location. */
    BRAIN {
        @Override
        public String toString() {
            return "to the brain";
        }
    },
    /** The full body hit location. */
    FULL_BODY {
        @Override
        public String toString() {
            return "to the full body";
        }
    },
    /** The full body except eyes hit location. */
    FULL_BODY_EXCEPT_EYES {
        @Override
        public String toString() {
            return "to the full body except the eyes";
        }
    };

    /** @return Whether this location is choosable as an armor protection spot. */
    @SuppressWarnings("static-method")
    public boolean isChoosable() {
        return true;
    }
}
