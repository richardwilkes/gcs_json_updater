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

package com.trollworks.gcs;

import com.trollworks.gcs.advantage.AdvantageList;
import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.datafile.DataFile;
import com.trollworks.gcs.equipment.EquipmentList;
import com.trollworks.gcs.modifier.AdvantageModifierList;
import com.trollworks.gcs.modifier.EquipmentModifierList;
import com.trollworks.gcs.notes.NoteList;
import com.trollworks.gcs.skill.SkillList;
import com.trollworks.gcs.spell.SpellList;
import com.trollworks.gcs.template.Template;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class GCSJsonUpdater {
    private static final byte[] XML_MARKER = {'<', '?', 'x', 'm', 'l', ' '};

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", Boolean.TRUE.toString());
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // Ignore
        }
        EventQueue.invokeLater(() -> {
            JFileChooser dialog = new JFileChooser();
            dialog.setDialogTitle("Choose a file or directory to convert");
            dialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            dialog.setMultiSelectionEnabled(true);
            int counter = 0;
            if (dialog.showDialog(null, "Select") == JFileChooser.APPROVE_OPTION) {
                File[] selection = dialog.getSelectedFiles();
                if (selection.length == 0) {
                    counter = traverse(dialog.getCurrentDirectory().toPath(), counter);
                } else {
                    for (File file : selection) {
                        counter = traverse(file.toPath(), counter);
                    }
                }
                JOptionPane.showMessageDialog(null, String.format("Converted %d files.", Integer.valueOf(counter)), "Conversion Complete", JOptionPane.INFORMATION_MESSAGE);
            }
            System.exit(0);
        });
    }

    private static int traverse(Path path, int counter) {
        try {
            String filename = path.getFileName().toString();
            if (!filename.startsWith(".")) {
                if (Files.isDirectory(path)) {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                        for (Path child : stream) {
                            counter = traverse(child, counter);
                        }
                    }
                } else {
                    String ext = "";
                    int    dot = filename.lastIndexOf('.');
                    if (dot != -1 && dot + 1 < filename.length()) {
                        ext = filename.substring(dot + 1);
                    }
                    switch (ext) {
                    case "gcs":
                        if (isXMLFile(path)) {
                            GURPSCharacter gcs = new GURPSCharacter(path);
                            gcs.save(path);
                            counter++;
                        }
                        break;
                    case "gct":
                        if (isXMLFile(path)) {
                            Template tmpl = new Template(path);
                            tmpl.save(path);
                            counter++;
                        }
                        break;
                    case "adq":
                        counter = loadSave(new AdvantageList(), path, counter);
                        break;
                    case "adm":
                        counter = loadSave(new AdvantageModifierList(), path, counter);
                        break;
                    case "eqp":
                        counter = loadSave(new EquipmentList(), path, counter);
                        break;
                    case "eqm":
                        counter = loadSave(new EquipmentModifierList(), path, counter);
                        break;
                    case "skl":
                        counter = loadSave(new SkillList(), path, counter);
                        break;
                    case "spl":
                        counter = loadSave(new SpellList(), path, counter);
                        break;
                    case "not":
                        counter = loadSave(new NoteList(), path, counter);
                        break;
                    }
                }
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(null, String.format("Unable to process %s\n%s", path.toString(), ioe.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return counter;
    }

    private static int loadSave(DataFile data, Path path, int counter) throws IOException {
        if (isXMLFile(path)) {
            data.load(path);
            data.save(path);
            return counter + 1;
        }
        return counter;
    }

    private static boolean isXMLFile(Path path) {
        try (InputStream in = Files.newInputStream(path)) {
            byte[] buffer = new byte[XML_MARKER.length];
            if (in.read(buffer) != XML_MARKER.length || !Arrays.equals(buffer, XML_MARKER)) {
                return false;
            }
        } catch (IOException ioe) {
            return false;
        }
        return true;
    }
}
