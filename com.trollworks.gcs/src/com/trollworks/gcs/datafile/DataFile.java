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

package com.trollworks.gcs.datafile;

import com.trollworks.gcs.utility.SafeFileUpdater;
import com.trollworks.gcs.utility.SaveType;
import com.trollworks.gcs.utility.VersionException;
import com.trollworks.gcs.utility.json.JsonWriter;
import com.trollworks.gcs.utility.units.WeightUnits;
import com.trollworks.gcs.utility.xml.XMLNodeType;
import com.trollworks.gcs.utility.xml.XMLReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/** A common super class for all data file-based model objects. */
public abstract class DataFile {
    /** The 'id' attribute. */
    public static final String ATTRIBUTE_ID = "id";
    /** Identifies the type of a JSON object. */
    public static final String KEY_TYPE     = "type";
    private             Path   mPath;
    private             UUID   mID          = UUID.randomUUID();

    /** @param path The path to load. */
    public void load(Path path) throws IOException {
        setPath(path);
        try (BufferedReader fileReader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            fileReader.mark(20);
            char[] buffer = new char[5];
            int    n      = fileReader.read(buffer);
            if (n < 0) {
                throw new IOException("Premature EOF");
            }
            fileReader.reset();
            if (n == 5 && buffer[0] == '<' && buffer[1] == '?' && buffer[2] == 'x' && buffer[3] == 'm' && buffer[4] == 'l') {
                // Load xml format from version 4.18 and earlier
                try (XMLReader reader = new XMLReader(fileReader)) {
                    XMLNodeType type  = reader.next();
                    boolean     found = false;
                    while (type != XMLNodeType.END_DOCUMENT) {
                        if (type == XMLNodeType.START_TAG) {
                            String name = reader.getName();
                            if (matchesRootTag(name)) {
                                if (found) {
                                    throw new IOException();
                                }
                                found = true;
                                load(reader, new LoadState());
                            } else {
                                reader.skipTag(name);
                            }
                            type = reader.getType();
                        } else {
                            type = reader.next();
                        }
                    }
                }
            } else {
                throw new IOException("only loads xml");
            }
        }
    }

    /**
     * @param reader The {@link XMLReader} to load data from.
     * @param state  The {@link LoadState} to use.
     */
    public void load(XMLReader reader, LoadState state) throws IOException {
        try {
            mID = UUID.fromString(reader.getAttribute(ATTRIBUTE_ID));
        } catch (Exception exception) {
            mID = UUID.randomUUID();
        }
        state.mDataFileVersion = reader.getAttributeAsInteger(LoadState.ATTRIBUTE_VERSION, 0);
        if (state.mDataFileVersion > getXMLTagVersion()) {
            throw VersionException.createTooNew();
        }
        loadSelf(reader, state);
    }

    /**
     * Called to load the data file.
     *
     * @param reader The {@link XMLReader} to load data from.
     * @param state  The {@link LoadState} to use.
     */
    protected abstract void loadSelf(XMLReader reader, LoadState state) throws IOException;

    /**
     * Saves the data out to the specified path. Does not affect the result of {@link #getPath()}.
     *
     * @param path The path to write to.
     * @return {@code true} on success.
     */
    public void save(Path path) throws IOException {
        SafeFileUpdater transaction = new SafeFileUpdater();
        transaction.begin();
        try {
            File transactionFile = transaction.getTransactionFile(path.toFile());
            try (JsonWriter w = new JsonWriter(new BufferedWriter(new FileWriter(transactionFile, StandardCharsets.UTF_8)), "\t")) {
                save(w, SaveType.NORMAL, false);
            }
            transaction.commit();
        } catch (IOException ioe) {
            transaction.abort();
            throw ioe;
        } catch (Throwable throwable) {
            transaction.abort();
            throw new IOException(throwable);
        }
    }

    /**
     * Writes the data to the specified {@link JsonWriter}.
     *
     * @param w              The {@link JsonWriter} to use.
     * @param saveType       The type of save being performed.
     * @param onlyIfNotEmpty Whether to write something even if the file contents are empty.
     */
    public void save(JsonWriter w, SaveType saveType, boolean onlyIfNotEmpty) throws IOException {
        if (!onlyIfNotEmpty || !isEmpty()) {
            w.startMap();
            w.keyValue(KEY_TYPE, getJSONTypeName());
            w.keyValue(LoadState.ATTRIBUTE_VERSION, 1);
            w.keyValue(ATTRIBUTE_ID, mID.toString());
            saveSelf(w, saveType);
            w.endMap();
        }
    }

    /**
     * Called to save the data file.
     *
     * @param w        The {@link JsonWriter} to use.
     * @param saveType The type of save being performed.
     */
    protected abstract void saveSelf(JsonWriter w, SaveType saveType) throws IOException;

    /** @return Whether the file is empty. By default, returns {@code false}. */
    @SuppressWarnings("static-method")
    public boolean isEmpty() {
        return false;
    }

    /** @return The type name to use for this data. */
    public abstract String getJSONTypeName();

    /** @return The most recent version of the XML tag this object knows how to load. */
    public abstract int getXMLTagVersion();

    /** @return The XML root container tag name for this particular file. */
    public abstract String getXMLTagName();

    /**
     * Called to match an XML tag name with the root tag for this data file.
     *
     * @param name The tag name to check.
     * @return Whether it matches the root tag or not.
     */
    public boolean matchesRootTag(String name) {
        return getXMLTagName().equals(name);
    }

    /** @return The path associated with this data file. */
    public Path getPath() {
        return mPath;
    }

    /** @param path The path associated with this data file. */
    public void setPath(Path path) {
        if (path != null) {
            path = path.normalize().toAbsolutePath();
        }
        mPath = path;
    }

    /** @return The ID for this data file. */
    public UUID getID() {
        return mID;
    }

    /** Replaces the existing ID with a new randomly generated one. */
    public void generateNewID() {
        mID = UUID.randomUUID();
    }

    public WeightUnits defaultWeightUnits() {
        return WeightUnits.LB;
    }
}
