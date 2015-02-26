/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.paulmanning.sunshine.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    // Since we want each test to start with a clean slate
    @Before
    public void deleteTheDatabase() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    /*
        Students: Uncomment this test once you've written the code to create the Location
        table.  Note that you will have to have chosen the same column names that I did in
        my solution for this test to compile, so if you haven't yet done that, this is
        a good time to change your column names to match mine.

        Note that this only tests that the Location table has the correct columns, since we
        give you the code for the weather table.  This test does not look at the
     */
    @Test
    public void createDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(WeatherContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + WeatherContract.LocationEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        location database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can uncomment out the "createNorthPoleLocationValues" function.  You can
        also make use of the ValidateCurrentRecord function from within TestUtilities.  Return
        the rowId of the inserted location.
    */
    @Test
    public long locationTable() {
        // First step: Get reference to writable database
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        ContentValues testData = TestUtilities.createNorthPoleLocationValues();

        // Insert ContentValues into database and get a row ID back
        long insertLocation = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, testData);
        assertTrue("Could not add test data to db", insertLocation != -1);

//        String[] keys = new String[4];
//        keys[0] = WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING;
//        keys[1] = WeatherContract.LocationEntry.COLUMN_CITY_NAME;
//        keys[2] = WeatherContract.LocationEntry.COLUMN_COORD_LAT;
//        keys[3] = WeatherContract.LocationEntry.COLUMN_COORD_LONG;
//        Cursor dbCursor = db.query(WeatherContract.LocationEntry.TABLE_NAME,
//                keys,
//                null, null, null, null, null);
//        String[] keys = new String[0];
//        Cursor dbCursor = db.query(WeatherContract.LocationEntry.TABLE_NAME,
//                testData.valueSet().toArray(keys),
//                null, null, null, null, null);

        Cursor allLocationRowsAndColumnsCursor = db.query(WeatherContract.LocationEntry.TABLE_NAME,
                null, null, null, null, null, null);

        // Move the cursor to a valid database row
        assertTrue("No records returned from query", allLocationRowsAndColumnsCursor.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Data not read correctly from Location database", allLocationRowsAndColumnsCursor, testData);

        assertFalse("Inserted one record but got more than one record", allLocationRowsAndColumnsCursor.moveToNext());

        // Finally, close the cursor and database
        allLocationRowsAndColumnsCursor.close();
        db.close();

        // Return the rowId of the inserted location, or "-1" on failure.
        return insertLocation;
    }

//    public void testLocTable() throws Throwable {
//        testLocationTable();
//    }


    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createWeatherValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
    @Test
    public void weatherTable() {
        // First insert the location, and then use the locationRowId to insert
        // the weather. Make sure to cover as many failure cases as you can.

        // We return the rowId of the inserted location in testLocationTable, so
        // you should just call that function rather than rewriting it
        long locationRowId = locationTable();
        assertFalse(locationRowId == -1);

        // First step: Get reference to writable database
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // Create ContentValues of what you want to insert
        // (you can use the createWeatherValues TestUtilities function if you wish)
        ContentValues weatherData = TestUtilities.createWeatherValues(locationRowId);

        // Insert ContentValues into database and get a row ID back
        long insertLocation = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, weatherData);
        assertTrue("Could not add data to weather db", insertLocation != -1);

        // Query the database and receive a Cursor back
        Cursor allLocationRowsAndColumnsCursor = db.query(WeatherContract.WeatherEntry.TABLE_NAME,
                null, null, null, null, null, null);

        // Move the cursor to a valid database row
        assertTrue("No records returned from query", allLocationRowsAndColumnsCursor.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Data not read correctly from weather database", allLocationRowsAndColumnsCursor, weatherData);

        assertFalse("Inserted one record but got more than one record", allLocationRowsAndColumnsCursor.moveToNext());

        // Finally, close the cursor and database
        allLocationRowsAndColumnsCursor.close();
        db.close();
    }

}
