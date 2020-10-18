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

import com.trollworks.gcs.equipment.Equipment;
import com.trollworks.gcs.modifier.EquipmentModifier;
import com.trollworks.gcs.ui.widget.outline.ListRow;

/** The type of feature. */
public enum FeatureType {
    ATTRIBUTE_BONUS {
        @Override
        public String toString() {
            return "Gives an attribute bonus of";
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof AttributeBonus;
        }

        @Override
        public Feature createFeature() {
            return new AttributeBonus();
        }
    }, DR_BONUS {
        @Override
        public String toString() {
            return "Gives a DR bonus of";
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof DRBonus;
        }

        @Override
        public Feature createFeature() {
            return new DRBonus();
        }
    }, REACTION_BONUS {
        @Override
        public String toString() {
            return "Gives a reaction modifier of";
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof ReactionBonus;
        }

        @Override
        public Feature createFeature() {
            return new ReactionBonus();
        }
    }, SKILL_LEVEL_BONUS {
        @Override
        public String toString() {
            return "Gives a skill level bonus of";
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof SkillBonus;
        }

        @Override
        public Feature createFeature() {
            return new SkillBonus();
        }
    }, SKILL_POINT_BONUS {
        @Override
        public String toString() {
            return "Gives a skill point bonus of";
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof SkillPointBonus;
        }

        @Override
        public Feature createFeature() {
            return new SkillPointBonus();
        }
    }, SPELL_LEVEL_BONUS {
        @Override
        public String toString() {
            return "Gives a spell level bonus of";
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof SpellBonus;
        }

        @Override
        public Feature createFeature() {
            return new SpellBonus();
        }
    }, SPELL_POINT_BONUS {
        @Override
        public String toString() {
            return "Gives a spell point bonus of";
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof SpellPointBonus;
        }

        @Override
        public Feature createFeature() {
            return new SpellPointBonus();
        }
    }, WEAPON_DAMAGE_BONUS {
        @Override
        public String toString() {
            return "Gives a weapon damage bonus of";
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof WeaponBonus;
        }

        @Override
        public Feature createFeature() {
            return new WeaponBonus();
        }
    }, REDUCE_ATTRIBUTE_COST {
        @Override
        public String toString() {
            return "Reduces the attribute cost of";
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof CostReduction;
        }

        @Override
        public Feature createFeature() {
            return new CostReduction();
        }
    }, REDUCE_CONTAINED_WEIGHT {
        @Override
        public String toString() {
            return "Reduces the contained weight by";
        }

        @Override
        public boolean validRow(ListRow row) {
            return row instanceof Equipment || row instanceof EquipmentModifier;
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof ContainedWeightReduction;
        }

        @Override
        public Feature createFeature() {
            return new ContainedWeightReduction();
        }
    };

    /**
     * @param row The row to check.
     * @return {@code true} if the row can be used with this feature type.
     */
    public boolean validRow(ListRow row) {
        return true;
    }

    /**
     * @param feature The feature to check.
     * @return {@code true} if the feature matches this feature type.
     */
    public abstract boolean matches(Feature feature);

    /** @return A new feature of this type. */
    public abstract Feature createFeature();
}
