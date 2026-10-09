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
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
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
        assertTrue(
            "MIGRATION_5_6 is declared but not registered in ALL_MIGRATIONS",
            Database.MIGRATION_5_6 in Database.ALL_MIGRATIONS,
        )
    }

    @Test
    fun `migrating 5 to 6 collapses duplicated rows and keeps every bookmark`() {
        seedVersion5 {
            // Session "a" was synced twice (ids 1 and 2). Session "1" has a remote id that
            // looks like a generated one, so its bookmark must not be rewritten.
            insertSession(id = 1, remoteId = "a", title = "old")
            insertSession(id = 2, remoteId = "a", title = "new")
            insertSession(id = 3, remoteId = "b", title = "b")
            insertSession(id = 4, remoteId = "1", title = "one")
            listOf("2", "b", "1").forEach { execSQL("INSERT INTO bookmarks (sessionId) VALUES ('$it')") }

            execSQL("INSERT INTO speakers VALUES (1, 'Ann', 't', 'b', 'a', 'old'), (2, 'Ann', 't', 'b', 'a', 'new')")
            execSQL("INSERT INTO organizers VALUES (1, 'Org', 't', 'l', 'ty', 'p', 'b', 'old', 'd', 'c'), (2, 'Org', 't', 'l', 'ty', 'p', 'b', 'new', 'd', 'c')")
            execSQL("INSERT INTO feed VALUES (1, 'Post', 'old', 't', 'u', NULL, 'c'), (2, 'Post', 'new', 't', 'u', NULL, 'c')")
        }

        val database = DatabaseModule.buildDatabase(context, TEST_DB)
        try {
            val db = database.openHelper.writableDatabase
            assertEquals(listOf("1|one", "a|new", "b|b"), db.rows("SELECT remote_id || '|' || title FROM sessions ORDER BY remote_id"))
            assertEquals(listOf("1", "a", "b"), db.rows("SELECT sessionId FROM bookmarks ORDER BY sessionId"))
            assertEquals(listOf("new"), db.rows("SELECT twitter FROM speakers"))
            assertEquals(listOf("new"), db.rows("SELECT twitterHandle FROM organizers"))
            assertEquals(listOf("new"), db.rows("SELECT body FROM feed"))
        } finally {
            database.close()
        }
    }

    // Not covered: the SQL inside MIGRATION_4_5, because no v4 schema was ever exported.

    /** Creates a database exactly as Room left it at version 5, from the exported schema. */
    private fun seedVersion5(seed: android.database.sqlite.SQLiteDatabase.() -> Unit) {
        val schema = JSONObject(File(SCHEMA_DIR, "5.json").readText()).getJSONObject("database")
        val db = openRawDatabase()
        try {
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                val table = entity.getString("tableName")
                db.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                val indices = entity.optJSONArray("indices") ?: continue
                for (j in 0 until indices.length()) {
                    db.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", table))
                }
            }
            val setup = schema.getJSONArray("setupQueries")
            for (i in 0 until setup.length()) db.execSQL(setup.getString(i))
            db.seed()
            db.version = 5
        } finally {
            db.close()
        }
    }

    private fun android.database.sqlite.SQLiteDatabase.insertSession(
        id: Int,
        remoteId: String,
        title: String,
    ) = execSQL(
        "INSERT INTO sessions VALUES ($id, '$remoteId', 'd', 'f', 'l', 's', '$title', 'e', 'e', 0, 0, 0, NULL, 's', 's', 'r', '[]', 0, 0, 'u')",
    )

    private fun androidx.sqlite.db.SupportSQLiteDatabase.rows(sql: String): List<String> = query(sql).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.getString(0)) } }

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
        const val SCHEMA_DIR = "schemas/ke.droidcon.kotlin.datasource.local.Database"
    }
}