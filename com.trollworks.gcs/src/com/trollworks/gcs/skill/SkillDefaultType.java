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

package com.trollworks.gcs.skill;

import com.trollworks.gcs.character.GURPSCharacter;

import java.util.Set;

/** The types of possible skill defaults. */
public enum SkillDefaultType {
    /** The type for ST-based defaults. */
    ST {
        @Override
        public String toString() {
            return "ST";
        }

        @Override
        public int getSkillLevelFast(GURPSCharacter character, SkillDefault skillDefault, boolean requirePoints, Set<String> excludes) {
            return finalLevel(skillDefault, Math.min(SkillAttribute.ST.getBaseSkillLevel(character), 20));
        }
    },
    /** The type for DX-based defaults. */
    DX {
        @Override
        public String toString() {
            return "DX";
        }

        @Override
        public int getSkillLevelFast(GURPSCharacter character, SkillDefault skillDefault, boolean requirePoints, Set<String> excludes) {
            return finalLevel(skillDefault, Math.min(SkillAttribute.DX.getBaseSkillLevel(character), 20));
        }
    },
    /** The type for IQ-based defaults. */
    IQ {
        @Override
        public String toString() {
            return "IQ";
        }

        @Override
        public int getSkillLevelFast(GURPSCharacter character, SkillDefault skillDefault, boolean requirePoints, Set<String> excludes) {
            return finalLevel(skillDefault, Math.min(SkillAttribute.IQ.getBaseSkillLevel(character), 20));
        }
    },
    /** The type for HT-based defaults. */
    HT {
        @Override
        public String toString() {
            return "HT";
        }

        @Override
        public int getSkillLevelFast(GURPSCharacter character, SkillDefault skillDefault, boolean requirePoints, Set<String> excludes) {
            return finalLevel(skillDefault, Math.min(SkillAttribute.HT.getBaseSkillLevel(character), 20));
        }
    },
    /** The type for Will-based defaults. */
    Will {
        @Override
        public String toString() {
            return "Will";
        }

        @Override
        public int getSkillLevelFast(GURPSCharacter character, SkillDefault skillDefault, boolean requirePoints, Set<String> excludes) {
            return finalLevel(skillDefault, Math.min(SkillAttribute.Will.getBaseSkillLevel(character), 20));
        }
    },
    /** The type for Perception-based defaults. */
    Per {
        @Override
        public String toString() {
            return "Perception";
        }

        @Override
        public int getSkillLevelFast(GURPSCharacter character, SkillDefault skillDefault, boolean requirePoints, Set<String> excludes) {
            return finalLevel(skillDefault, Math.min(SkillAttribute.Per.getBaseSkillLevel(character), 20));
        }
    },
    /** The type for Skill-based defaults. */
    Skill {
        @Override
        public String toString() {
            return "Skill named";
        }

        @Override
        public int getSkillLevelFast(GURPSCharacter character, SkillDefault skillDefault, boolean requirePoints, Set<String> excludes) {
            int best = Integer.MIN_VALUE;
            for (Skill skill : character.getSkillNamed(skillDefault.getName(), skillDefault.getSpecialization(), requirePoints, excludes)) {
                int level = skill.getLevel();
                if (level > best) {
                    best = level;
                }
            }
            return finalLevel(skillDefault, best);
        }

        @Override
        public int getSkillLevel(GURPSCharacter character, SkillDefault skillDefault, boolean requirePoints, Set<String> excludes) {
            int best = Integer.MIN_VALUE;
            for (Skill skill : character.getSkillNamed(skillDefault.getName(), skillDefault.getSpecialization(), requirePoints, excludes)) {
                if (skill.getLevel() > best) {
                    int level = skill.getLevel(excludes);
                    if (level > best) {
                        best = level;
                    }
                }
            }
            return finalLevel(skillDefault, best);
        }

        @Override
        public boolean isSkillBased() {
            return true;
        }
    },
    /** The type for Parry-based defaults. */
    Parry {
        @Override
        public String toString() {
            return "Parrying skill named";
        }

        @Override
        public int getSkillLevelFast(GURPSCharacter character, SkillDefault skillDefault, boolean requirePoints, Set<String> excludes) {
            int best = Integer.MIN_VALUE;
            for (Skill skill : character.getSkillNamed(skillDefault.getName(), skillDefault.getSpecialization(), requirePoints, excludes)) {
                int level = skill.getLevel();
                if (level > best) {
                    best = level;
                }
            }
            return finalLevel(skillDefault, best == Integer.MIN_VALUE ? best : best / 2 + 3 + character.getParryBonus());
        }

        @Override
        public int getSkillLevel(GURPSCharacter character, SkillDefault skillDefault, boolean requirePoints, Set<String> excludes) {
            int best = Integer.MIN_VALUE;
            for (Skill skill : character.getSkillNamed(skillDefault.getName(), skillDefault.getSpecialization(), requirePoints, excludes)) {
                if (skill.getLevel() > best) {
                    int level = skill.getLevel(excludes);
                    if (level > best) {
                        best = level;
                    }
                }
            }
            return finalLevel(skillDefault, best == Integer.MIN_VALUE ? best : best / 2 + 3 + character.getParryBonus());
        }

        @Override
        public boolean isSkillBased() {
            return true;
        }
    },
    /** The type for Block-based defaults. */
    Block {
        @Override
        public String toString() {
            return "Blocking skill named";
        }

        @Override
        public int getSkillLevelFast(GURPSCharacter character, SkillDefault skillDefault, boolean requirePoints, Set<String> excludes) {
            int best = Integer.MIN_VALUE;
            for (Skill skill : character.getSkillNamed(skillDefault.getName(), skillDefault.getSpecialization(), requirePoints, excludes)) {
                int level = skill.getLevel();
                if (level > best) {
                    best = level;
                }
            }
            return finalLevel(skillDefault, best == Integer.MIN_VALUE ? best : best / 2 + 3 + character.getBlockBonus());
        }

        @Override
        public int getSkillLevel(GURPSCharacter character, SkillDefault skillDefault, boolean requirePoints, Set<String> excludes) {
            int best = Integer.MIN_VALUE;
            for (Skill skill : character.getSkillNamed(skillDefault.getName(), skillDefault.getSpecialization(), requirePoints, excludes)) {
                if (skill.getLevel() > best) {
                    int level = skill.getLevel(excludes);
                    if (level > best) {
                        best = level;
                    }
                }
            }
            return finalLevel(skillDefault, best == Integer.MIN_VALUE ? best : best / 2 + 3 + character.getBlockBonus());
        }

        @Override
        public boolean isSkillBased() {
            return true;
        }
    },
    /** The type for 10-based defaults. */
    Base10 {
        @Override
        public String toString() {
            return "10";
        }

        @Override
        public int getSkillLevelFast(GURPSCharacter character, SkillDefault skillDefault, boolean requirePoints, Set<String> excludes) {
            return finalLevel(skillDefault, SkillAttribute.Base10.getBaseSkillLevel(character));
        }
    };

    /**
     * @param name The name of a {@link SkillDefaultType}, as returned from {@link #name()} or
     *             {@link #toString()}.
     * @return The matching {@link SkillDefaultType}, or {@link #Skill} if a match cannot be found.
     */
    public static final SkillDefaultType getByName(String name) {
        for (SkillDefaultType type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.toString().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return Skill;
    }

    /** @return Whether the {@link SkillDefaultType} is based on another skill or not. */
    @SuppressWarnings("static-method")
    public boolean isSkillBased() {
        return false;
    }

    /**
     * @param character     The character to work with.
     * @param skillDefault  The default being calculated.
     * @param requirePoints Only look at {@link Skill}s that have points. {@link Technique}s,
     *                      however, still won't need points even if this is {@code true}.
     * @param excludes      Exclude these {@link Skill}s from consideration.
     * @return The base skill level for this {@link SkillDefaultType}.
     */
    public abstract int getSkillLevelFast(GURPSCharacter character, SkillDefault skillDefault, boolean requirePoints, Set<String> excludes);

    /**
     * @param character     The character to work with.
     * @param skillDefault  The default being calculated.
     * @param requirePoints Only look at {@link Skill}s that have points. {@link Technique}s,
     *                      however, still won't need points even if this is {@code true}.
     * @param excludes      Exclude these {@link Skill}s from consideration.
     * @return The base skill level for this {@link SkillDefaultType}.
     */
    public int getSkillLevel(GURPSCharacter character, SkillDefault skillDefault, boolean requirePoints, Set<String> excludes) {
        return getSkillLevelFast(character, skillDefault, requirePoints, excludes);
    }

    /**
     * @param skillDefault The {@link SkillDefault}.
     * @param level        The level without the default modifier.
     * @return The final level.
     */
    @SuppressWarnings("static-method")
    protected int finalLevel(SkillDefault skillDefault, int level) {
        if (level != Integer.MIN_VALUE) {
            level += skillDefault.getModifier();
        }
        return level;
    }
}
