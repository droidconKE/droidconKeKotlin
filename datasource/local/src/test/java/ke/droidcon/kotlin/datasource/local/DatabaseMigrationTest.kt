/*
 * Copyright 2026 DroidconKE
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
package ke.droidcon.kotlin.datasource.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import ke.droidcon.kotlin.datasource.local.di.DatabaseModule
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Guards the database against silent data loss.
 *
 * The production builder used to call `fallbackToDestructiveMigration()`, which meant a
 * schema change shipped without a matching migration wiped every table instead of
 * failing. For this app that deletes the user's bookmarked sessions — their personal
 * conference agenda — with no warning and no recovery.
 *
 * [openingWithAnUnmigratableVersionFailsInsteadOfWipingData] is the test that encodes
 * the fix: it passes only because the destructive fallback is gone. Against the previous
 * configuration Room would have recreated the database and the assertion would fail.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DatabaseMigrationTest {
    private lateinit var context: Context
    private lateinit var databaseFile: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        databaseFile = context.getDatabasePath(TEST_DB)
        databaseFile.parentFile?.mkdirs()
        deleteDatabaseFiles()
    }

    @After
    fun tearDown() {
        deleteDatabaseFiles()
    }

    @Test
    fun `opening with an unmigratable version fails instead of wiping data`() =
        runTest {
            // A database left behind by a much older build, with a bookmark in it.
            // No migration path exists from version 2 to the current schema.
            seedLegacyDatabaseAtVersion(version = 2, bookmarkedSessionId = "session-42")

            val database = DatabaseModule.buildDatabase(context, TEST_DB)

            try {
                // Room opens lazily and the DAO returns a cold Flow, so force the open
                // explicitly — this is what runs the migration path.
                database.openHelper.writableDatabase
                fail(
                    "Expected opening the database to fail because no migration from " +
                        "version 2 exists. It succeeded instead, which means the " +
                        "destructive fallback is back and user bookmarks were deleted.",
                )
            } catch (expected: IllegalStateException) {
                // Room's message names the missing migration. Assert we failed for the
                // right reason rather than on some unrelated IllegalStateException.
                assertTrue(
                    "Unexpected failure: ${expected.message}",
                    expected.message?.contains("Migration didn't properly handle") == true ||
                        expected.message?.contains("A migration from") == true ||
                        expected.message?.contains("migration") == true,
                )
            } finally {
                database.close()
            }
        }

    @Test
    fun `all known migrations are registered`() {
        // A migration that exists but is not in ALL_MIGRATIONS behaves identically to no
        // migration at all — and now that the destructive fallback is gone, that is a
        // crash on upgrade rather than silent data loss. Keep this list in step with the
        // migrations declared on Database.
        assertTrue("ALL_MIGRATIONS is empty", Database.ALL_MIGRATIONS.isNotEmpty())
        assertTrue(
            "MIGRATION_4_5 is declared but not registered in ALL_MIGRATIONS",
            Database.MIGRATION_4_5 in Database.ALL_MIGRATIONS,
        )
    }

    // Not covered here: the SQL inside MIGRATION_4_5 itself. Verifying that through
    // Room needs a byte-exact v4 schema for every entity so Room's post-migration
    // validation passes, which is what MigrationTestHelper and exported schemas exist
    // for. `exportSchema = true` is now on, so schemas ship from version 5 onward and
    // the next migration added can be covered properly. See the plan's §10 for the
    // MigrationTestHelper setup.

    /**
     * Writes a minimal on-disk database at [version] containing the tables this test
     * needs, using raw SQLite so it does not depend on the current entity definitions.
     *
     * Column name is `sessionId`: BookmarkEntity declares `var sessionId: String` with
     * no `@ColumnInfo`, so Room derives the column from the property name.
     */
    private fun seedLegacyDatabaseAtVersion(
        version: Int,
        bookmarkedSessionId: String,
    ) {
        val db = openRawDatabase()
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS bookmarks (sessionId TEXT NOT NULL, PRIMARY KEY(sessionId))")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    remote_id TEXT NOT NULL,
                    description TEXT NOT NULL,
                    sessionFormat TEXT NOT NULL,
                    sessionLevel TEXT NOT NULL,
                    slug TEXT NOT NULL,
                    title TEXT NOT NULL,
                    endDateTime TEXT NOT NULL,
                    endTime TEXT NOT NULL,
                    isBookmarked INTEGER NOT NULL,
                    isKeynote INTEGER NOT NULL,
                    isServiceSession INTEGER NOT NULL,
                    sessionImage TEXT,
                    startDateTime TEXT NOT NULL,
                    startTime TEXT NOT NULL,
                    rooms TEXT NOT NULL,
                    speakers TEXT NOT NULL,
                    startTimestamp INTEGER NOT NULL,
                    sessionImageUrl TEXT NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("INSERT OR REPLACE INTO bookmarks (sessionId) VALUES ('$bookmarkedSessionId')")
            db.version = version
        } finally {
            db.close()
        }
    }

    private fun openRawDatabase() =
        android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(databaseFile, null)

    private fun deleteDatabaseFiles() {
        listOf(databaseFile, File("${databaseFile.path}-wal"), File("${databaseFile.path}-shm"))
            .forEach { if (it.exists()) it.delete() }
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
    }
}