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

/** The attribute affected by a {@link AttributeBonus}. */
public enum BonusAttributeType {
    /** The ST attribute. */
    ST {
        @Override
        public String toString() {
            return "ST";
        }
    },
    /** The DX attribute. */
    DX {
        @Override
        public String toString() {
            return "DX";
        }
    },
    /** The IQ attribute. */
    IQ {
        @Override
        public String toString() {
            return "IQ";
        }
    },
    /** The HT attribute. */
    HT {
        @Override
        public String toString() {
            return "HT";
        }
    },
    /** The Will attribute. */
    WILL {
        @Override
        public String toString() {
            return "will";
        }
    },
    /** The Fright Check attribute. */
    FRIGHT_CHECK {
        @Override
        public String toString() {
            return "fright checks";
        }
    },
    /** The Perception attribute. */
    PERCEPTION {
        @Override
        public String toString() {
            return "perception";
        }
    },
    /** The Vision attribute. */
    VISION {
        @Override
        public String toString() {
            return "vision";
        }
    },
    /** The Hearing attribute. */
    HEARING {
        @Override
        public String toString() {
            return "hearing";
        }
    },
    /** The TasteSmell attribute. */
    TASTE_SMELL {
        @Override
        public String toString() {
            return "taste & smell";
        }
    },
    /** The Touch attribute. */
    TOUCH {
        @Override
        public String toString() {
            return "touch";
        }
    },
    /** The Dodge attribute. */
    DODGE {
        @Override
        public String toString() {
            return "dodge";
        }
    },
    /** The Parry attribute. */
    PARRY {
        @Override
        public String toString() {
            return "parry";
        }
    },
    /** The Block attribute. */
    BLOCK {
        @Override
        public String toString() {
            return "block";
        }
    },
    /** The Speed attribute. */
    SPEED {
        @Override
        public String toString() {
            return "basic speed";
        }

        @Override
        public boolean isIntegerOnly() {
            return false;
        }
    },
    /** The Move attribute. */
    MOVE {
        @Override
        public String toString() {
            return "basic move";
        }
    },
    /** The FP attribute. */
    FP {
        @Override
        public String toString() {
            return "FP";
        }
    },
    /** The HP attribute. */
    HP {
        @Override
        public String toString() {
            return "HP";
        }
    },
    /** The size modifier attribute. */
    SM {
        @Override
        public String toString() {
            return "size modifier";
        }
    };

    private String mTag;

    BonusAttributeType() {
        mTag = name();
        if (mTag.length() > 2) {
            mTag = mTag.toLowerCase();
        }
    }

    /** @return The presentation name. */
    public String getPresentationName() {
        String name = name();
        if (name.length() > 2) {
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
        }
        return name;
    }

    /** @return {@code true} if only integer values are permitted. */
    @SuppressWarnings("static-method")
    public boolean isIntegerOnly() {
        return true;
    }

    /** @return The XML tag to use for this {@link BonusAttributeType}. */
    public String getXMLTag() {
        return mTag;
    }
}
