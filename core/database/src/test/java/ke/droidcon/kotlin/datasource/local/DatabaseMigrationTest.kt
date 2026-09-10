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
 * Guards the database against silent data loss: a schema change without a matching
 * migration must fail rather than wipe the user's bookmarked sessions.
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
            // No migration path exists from version 2 to the current schema.
            seedLegacyDatabaseAtVersion(version = 2, bookmarkedSessionId = "session-42")

            val database = DatabaseModule.buildDatabase(context, TEST_DB)

            try {
                // Room opens lazily, so force it: this is what runs the migration path.
                database.openHelper.writableDatabase
                fail(
                    "Expected opening the database to fail because no migration from " +
                        "version 2 exists. It succeeded instead, which means the " +
                        "destructive fallback is back and user bookmarks were deleted.",
                )
            } catch (expected: IllegalStateException) {
                // Assert we failed for the right reason, not an unrelated ISE.
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
        assertTrue("ALL_MIGRATIONS is empty", Database.ALL_MIGRATIONS.isNotEmpty())
        assertTrue(
            "MIGRATION_4_5 is declared but not registered in ALL_MIGRATIONS",
            Database.MIGRATION_4_5 in Database.ALL_MIGRATIONS,
        )
    }

    // Not covered: the SQL inside MIGRATION_4_5. That needs a byte-exact v4 schema for
    // Room's post-migration validation, which is what MigrationTestHelper and exported
    // schemas are for. Schemas now ship from v5, so the next migration can be covered.

    /**
     * Writes a minimal database at [version] using raw SQLite, so it does not depend on
     * the current entity definitions.
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
        android.database.sqlite.SQLiteDatabase
            .openOrCreateDatabase(databaseFile, null)

    private fun deleteDatabaseFiles() {
        listOf(databaseFile, File("${databaseFile.path}-wal"), File("${databaseFile.path}-shm"))
            .forEach { if (it.exists()) it.delete() }
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
    }
}