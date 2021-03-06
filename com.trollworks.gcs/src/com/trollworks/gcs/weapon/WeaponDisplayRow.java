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

package com.trollworks.gcs.weapon;

import com.trollworks.gcs.ui.widget.outline.ListRow;
import com.trollworks.gcs.ui.widget.outline.Row;

/** A non-editable row for displaying weapon information. */
public class WeaponDisplayRow extends Row {
    private WeaponStats mWeapon;

    /**
     * Creates a new weapon display row.
     *
     * @param weapon The weapon to display.
     */
    public WeaponDisplayRow(WeaponStats weapon) {
        mWeapon = weapon;
    }

    /**
     * @param obj The other object to compare against.
     * @return Whether or not this {@link ListRow} is equivalent.
     */
    public boolean isEquivalentTo(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof WeaponDisplayRow) {
            return mWeapon.equals(((WeaponDisplayRow) obj).mWeapon);
        }
        return false;
    }

    /** @return The weapon. */
    public WeaponStats getWeapon() {
        return mWeapon;
    }

    public String getSkillLevelToolTip() {
        return mWeapon.getSkillLevelToolTip();
    }

    public String getDamageToolTip() {
        return mWeapon.getDamage().getDamageToolTip();
    }
}
