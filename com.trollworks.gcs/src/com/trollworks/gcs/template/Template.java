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

package com.trollworks.gcs.template;

import com.trollworks.gcs.advantage.Advantage;
import com.trollworks.gcs.advantage.AdvantageList;
import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.datafile.DataFile;
import com.trollworks.gcs.datafile.LoadState;
import com.trollworks.gcs.equipment.Equipment;
import com.trollworks.gcs.equipment.EquipmentList;
import com.trollworks.gcs.notes.Note;
import com.trollworks.gcs.notes.NoteList;
import com.trollworks.gcs.skill.Skill;
import com.trollworks.gcs.skill.SkillList;
import com.trollworks.gcs.skill.Technique;
import com.trollworks.gcs.spell.RitualMagicSpell;
import com.trollworks.gcs.spell.Spell;
import com.trollworks.gcs.spell.SpellList;
import com.trollworks.gcs.ui.widget.outline.ListRow;
import com.trollworks.gcs.ui.widget.outline.OutlineModel;
import com.trollworks.gcs.ui.widget.outline.RowIterator;
import com.trollworks.gcs.utility.FilteredIterator;
import com.trollworks.gcs.utility.SaveType;
import com.trollworks.gcs.utility.json.JsonWriter;
import com.trollworks.gcs.utility.text.Text;
import com.trollworks.gcs.utility.xml.XMLNodeType;
import com.trollworks.gcs.utility.xml.XMLReader;

import java.io.IOException;
import java.nio.file.Path;

/** A template. */
public class Template extends DataFile {
    private static final int          CURRENT_VERSION = 2;
    private static final String       TAG_ROOT        = "template";
    private static final String       TAG_OLD_NOTES   = "notes";
    private              OutlineModel mAdvantages;
    private              OutlineModel mSkills;
    private              OutlineModel mSpells;
    private              OutlineModel mEquipment;
    private              OutlineModel mOtherEquipment;
    private              OutlineModel mNotes;
    private              boolean      mNeedAdvantagesPointCalculation;
    private              boolean      mNeedSkillPointCalculation;
    private              boolean      mNeedSpellPointCalculation;
    private              int          mCachedAdvantagePoints;
    private              int          mCachedDisadvantagePoints;
    private              int          mCachedQuirkPoints;
    private              int          mCachedSkillPoints;
    private              int          mCachedSpellPoints;

    /** Creates a new character with only default values set. */
    public Template() {
        mAdvantages = new OutlineModel();
        mSkills = new OutlineModel();
        mSpells = new OutlineModel();
        mEquipment = new OutlineModel();
        mOtherEquipment = new OutlineModel();
        mNotes = new OutlineModel();
    }

    /**
     * Creates a new character from the specified file.
     *
     * @param path The path to load the data from.
     * @throws IOException if the data cannot be read or the file doesn't contain a valid character
     *                     sheet.
     */
    public Template(Path path) throws IOException {
        this();
        load(path);
    }

    @Override
    public String getJSONTypeName() {
        return TAG_ROOT;
    }

    @Override
    public int getXMLTagVersion() {
        return CURRENT_VERSION;
    }

    @Override
    public String getXMLTagName() {
        return TAG_ROOT;
    }

    @Override
    protected final void loadSelf(XMLReader reader, LoadState state) throws IOException {
        String marker = reader.getMarker();
        do {
            if (reader.next() == XMLNodeType.START_TAG) {
                String name = reader.getName();
                if (AdvantageList.TAG_ROOT.equals(name)) {
                    loadAdvantageList(reader, state);
                } else if (SkillList.TAG_ROOT.equals(name)) {
                    loadSkillList(reader, state);
                } else if (SpellList.TAG_ROOT.equals(name)) {
                    loadSpellList(reader, state);
                } else if (EquipmentList.TAG_CARRIED_ROOT.equals(name)) {
                    loadEquipmentList(reader, state, mEquipment);
                } else if (EquipmentList.TAG_OTHER_ROOT.equals(name)) {
                    loadEquipmentList(reader, state, mOtherEquipment);
                } else if (NoteList.TAG_ROOT.equals(name)) {
                    loadNotesList(reader, state);
                } else if (TAG_OLD_NOTES.equals(name)) {
                    Note note = new Note(this, false);
                    note.setDescription(Text.standardizeLineEndings(reader.readText()));
                    mNotes.addRow(note, false);
                } else {
                    reader.skipTag(name);
                }
            }
        } while (reader.withinMarker(marker));
        calculateAdvantagePoints();
        calculateSkillPoints();
        calculateSpellPoints();
    }

    private void loadAdvantageList(XMLReader reader, LoadState state) throws IOException {
        String marker = reader.getMarker();
        do {
            if (reader.next() == XMLNodeType.START_TAG) {
                String name = reader.getName();
                if (Advantage.TAG_ADVANTAGE.equals(name) || Advantage.TAG_ADVANTAGE_CONTAINER.equals(name)) {
                    mAdvantages.addRow(new Advantage(this, reader, state), true);
                } else {
                    reader.skipTag(name);
                }
            }
        } while (reader.withinMarker(marker));
    }

    private void loadSkillList(XMLReader reader, LoadState state) throws IOException {
        String marker = reader.getMarker();
        do {
            if (reader.next() == XMLNodeType.START_TAG) {
                String name = reader.getName();
                if (Skill.TAG_SKILL.equals(name) || Skill.TAG_SKILL_CONTAINER.equals(name)) {
                    mSkills.addRow(new Skill(this, reader, state), true);
                } else if (Technique.TAG_TECHNIQUE.equals(name)) {
                    mSkills.addRow(new Technique(this, reader, state), true);
                } else {
                    reader.skipTag(name);
                }
            }
        } while (reader.withinMarker(marker));
    }

    private void loadSpellList(XMLReader reader, LoadState state) throws IOException {
        String marker = reader.getMarker();
        do {
            if (reader.next() == XMLNodeType.START_TAG) {
                String name = reader.getName();
                if (Spell.TAG_SPELL.equals(name) || Spell.TAG_SPELL_CONTAINER.equals(name)) {
                    mSpells.addRow(new Spell(this, reader, state), true);
                } else if (RitualMagicSpell.TAG_RITUAL_MAGIC_SPELL.equals(name)) {
                    mSpells.addRow(new RitualMagicSpell(this, reader, state), true);
                } else {
                    reader.skipTag(name);
                }
            }
        } while (reader.withinMarker(marker));
    }

    private void loadEquipmentList(XMLReader reader, LoadState state, OutlineModel outline) throws IOException {
        String marker = reader.getMarker();
        do {
            if (reader.next() == XMLNodeType.START_TAG) {
                String name = reader.getName();
                if (Equipment.TAG_EQUIPMENT.equals(name) || Equipment.TAG_EQUIPMENT_CONTAINER.equals(name)) {
                    outline.addRow(new Equipment(this, reader, state), true);
                } else {
                    reader.skipTag(name);
                }
            }
        } while (reader.withinMarker(marker));
    }

    private void loadNotesList(XMLReader reader, LoadState state) throws IOException {
        String marker = reader.getMarker();
        do {
            if (reader.next() == XMLNodeType.START_TAG) {
                String name = reader.getName();
                if (Note.TAG_NOTE.equals(name) || Note.TAG_NOTE_CONTAINER.equals(name)) {
                    mNotes.addRow(new Note(this, reader, state), true);
                } else {
                    reader.skipTag(name);
                }
            }
        } while (reader.withinMarker(marker));
    }

    @Override
    protected void saveSelf(JsonWriter w, SaveType saveType) throws IOException {
        ListRow.saveList(w, GURPSCharacter.KEY_ADVANTAGES, mAdvantages.getTopLevelRows(), saveType);
        ListRow.saveList(w, GURPSCharacter.KEY_SKILLS, mSkills.getTopLevelRows(), saveType);
        ListRow.saveList(w, GURPSCharacter.KEY_SPELLS, mSpells.getTopLevelRows(), saveType);
        ListRow.saveList(w, GURPSCharacter.KEY_EQUIPMENT, mEquipment.getTopLevelRows(), saveType);
        ListRow.saveList(w, GURPSCharacter.KEY_OTHER_EQUIPMENT, mOtherEquipment.getTopLevelRows(), saveType);
        ListRow.saveList(w, GURPSCharacter.KEY_NOTES, mNotes.getTopLevelRows(), saveType);
    }

    private int getTotalPoints() {
        return getAdvantagePoints() + getDisadvantagePoints() + getQuirkPoints() + getSkillPoints() + getSpellPoints();
    }

    /** @return The number of points spent on advantages. */
    public int getAdvantagePoints() {
        return mCachedAdvantagePoints;
    }

    /** @return The number of points spent on disadvantages. */
    public int getDisadvantagePoints() {
        return mCachedDisadvantagePoints;
    }

    /** @return The number of points spent on quirks. */
    public int getQuirkPoints() {
        return mCachedQuirkPoints;
    }

    private void calculateAdvantagePoints() {
        mCachedAdvantagePoints = 0;
        mCachedDisadvantagePoints = 0;
        mCachedQuirkPoints = 0;

        for (Advantage advantage : getAdvantagesIterator()) {
            if (!advantage.canHaveChildren()) {
                int pts = advantage.getAdjustedPoints();

                if (pts > 0) {
                    mCachedAdvantagePoints += pts;
                } else if (pts < -1) {
                    mCachedDisadvantagePoints += pts;
                } else if (pts == -1) {
                    mCachedQuirkPoints--;
                }
            }
        }
    }

    /** @return The number of points spent on skills. */
    public int getSkillPoints() {
        return mCachedSkillPoints;
    }

    private void calculateSkillPoints() {
        mCachedSkillPoints = 0;
        for (Skill skill : getSkillsIterable()) {
            if (!skill.canHaveChildren()) {
                mCachedSkillPoints += skill.getPoints();
            }
        }
    }

    /** @return The number of points spent on spells. */
    public int getSpellPoints() {
        return mCachedSpellPoints;
    }

    private void calculateSpellPoints() {
        mCachedSpellPoints = 0;
        for (Spell spell : getSpellsIterator()) {
            if (!spell.canHaveChildren()) {
                mCachedSpellPoints += spell.getPoints();
            }
        }
    }

    /** @return The outline model for the advantages. */
    public OutlineModel getAdvantagesModel() {
        return mAdvantages;
    }

    /** @return A recursive iterator over the advantages. */
    public RowIterator<Advantage> getAdvantagesIterator() {
        return new RowIterator<>(mAdvantages);
    }

    /** @return The outline model for the skills. */
    public OutlineModel getSkillsModel() {
        return mSkills;
    }

    /** @return A recursive iterable for the template's skills. */
    public Iterable<Skill> getSkillsIterable() {
        return new FilteredIterator<>(new RowIterator<ListRow>(mSkills), Skill.class);
    }

    /** @return The outline model for the spells. */
    public OutlineModel getSpellsModel() {
        return mSpells;
    }

    /** @return A recursive iterator over the spells. */
    public RowIterator<Spell> getSpellsIterator() {
        return new RowIterator<>(mSpells);
    }

    /** @return The outline model for the equipment. */
    public OutlineModel getEquipmentModel() {
        return mEquipment;
    }

    /** @return A recursive iterator over the equipment. */
    public RowIterator<Equipment> getEquipmentIterator() {
        return new RowIterator<>(mEquipment);
    }

    /** @return The outline model for the other equipment. */
    public OutlineModel getOtherEquipmentModel() {
        return mOtherEquipment;
    }

    /** @return A recursive iterator over the other equipment. */
    public RowIterator<Equipment> getOtherEquipmentIterator() {
        return new RowIterator<>(mOtherEquipment);
    }

    /** @return The outline model for the notes. */
    public OutlineModel getNotesModel() {
        return mNotes;
    }

    /** @return A recursive iterator over the notes. */
    public RowIterator<Note> getNoteIterator() {
        return new RowIterator<>(mNotes);
    }
}
