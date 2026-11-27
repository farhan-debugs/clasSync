package com.classsync.app.data.remote

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.*
import com.classsync.app.data.local.entity.AttendanceEntity
import com.classsync.app.data.local.entity.StudentEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages all Google Sheets API operations:
 *  - Import student roster from a sheet
 *  - Write/update attendance records
 *  - Batch sync unsynced records
 */
@Singleton
class SheetsApiService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val APP_NAME      = "ClassSync"
        private const val ROSTER_RANGE  = "Roster!A2:C"      // Name | Roll | Email
        private val SCOPES = listOf(
            SheetsScopes.SPREADSHEETS,
            "https://www.googleapis.com/auth/drive.file"
        )
    }

    /**
     * Build an authenticated [Sheets] client using the signed-in Google account.
     * [accountEmail] must match one of the accounts on device.
     */
    private fun buildService(accountEmail: String): Sheets {
        val credential = GoogleAccountCredential
            .usingOAuth2(context, SCOPES)
            .also { it.selectedAccountName = accountEmail }
        return Sheets.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName(APP_NAME).build()
    }

    // ── 1. Read student roster ──────────────────────────────────────────────

    /**
     * Fetches student names + roll numbers from the "Roster" tab.
     * Expected columns: A=Name, B=RollNumber, C=Email (optional).
     */
    suspend fun fetchRoster(
        spreadsheetId: String,
        accountEmail: String,
        classId: String = "default"
    ): Result<List<StudentEntity>> = withContext(Dispatchers.IO) {
        try {
            val service  = buildService(accountEmail)
            val response = service.spreadsheets().values()
                .get(spreadsheetId, ROSTER_RANGE)
                .execute()

            val rows     = response.getValues() ?: emptyList()
            val students = rows.mapIndexedNotNull { idx, row ->
                val name  = row.getOrNull(0)?.toString()?.trim() ?: return@mapIndexedNotNull null
                val roll  = row.getOrNull(1)?.toString()?.trim() ?: "${idx + 1}"
                val email = row.getOrNull(2)?.toString()?.trim()
                StudentEntity(
                    rollNumber = roll,
                    name       = name,
                    email      = email,
                    classId    = classId
                )
            }
            Result.success(students)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── 2. Write attendance records ─────────────────────────────────────────

    /**
     * Writes a batch of attendance records to the spreadsheet.
     * Layout: each date+subject = one column; each student = one row.
     * Appends new data to the "Attendance" tab.
     */
    suspend fun syncAttendance(
        spreadsheetId: String,
        accountEmail: String,
        records: List<AttendanceEntity>,
        studentNames: Map<Long, String>  // studentId → name
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (records.isEmpty()) return@withContext Result.success(Unit)
            val service = buildService(accountEmail)

            // Group by date + subject to form columns
            val grouped = records.groupBy { "${it.date}|${it.subject}" }

            val dataList = grouped.map { (key, sessionRecords) ->
                val (date, subject) = key.split("|")
                // Header row: "Date – Subject"
                val header = "$date – $subject"
                val rows   = mutableListOf<List<Any>>(listOf(header))
                sessionRecords.forEach { rec ->
                    val name = studentNames[rec.studentId] ?: "Roll ${rec.studentId}"
                    rows.add(listOf(name, rec.status))
                }
                ValueRange()
                    .setRange("Attendance!A1")
                    .setValues(rows)
            }

            val body = BatchUpdateValuesRequest()
                .setValueInputOption("RAW")
                .setData(dataList)

            service.spreadsheets().values()
                .batchUpdate(spreadsheetId, body)
                .execute()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── 3. Append a single session (simpler fast path) ─────────────────────

    /**
     * Appends a single-session attendance column.
     * Range format: "Attendance!A1" (Sheets auto-finds the next empty column).
     */
    suspend fun appendSessionColumn(
        spreadsheetId: String,
        accountEmail: String,
        subject: String,
        date: String,
        rows: List<Pair<String, String>>   // (studentName, status)
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val service = buildService(accountEmail)
            val values  = mutableListOf<List<Any>>()
            values.add(listOf("$date – $subject"))
            rows.forEach { (name, status) -> values.add(listOf(name, status)) }

            service.spreadsheets().values()
                .append(spreadsheetId, "Attendance", ValueRange().setValues(values))
                .setValueInputOption("RAW")
                .setInsertDataOption("INSERT_ROWS")
                .execute()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── 4. Validate / get sheet metadata ───────────────────────────────────

    /**
     * Returns the spreadsheet title to confirm access + validate the ID.
     */
    suspend fun validateSpreadsheet(
        spreadsheetId: String,
        accountEmail: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service      = buildService(accountEmail)
            val spreadsheet  = service.spreadsheets().get(spreadsheetId).execute()
            Result.success(spreadsheet.properties.title ?: "Untitled Sheet")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
