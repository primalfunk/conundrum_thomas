package com.conundrum.thomas.v2.tools.provenance

import java.io.File
import java.security.MessageDigest
import java.sql.Connection
import java.sql.DriverManager

private const val STATEMENT_MARKER = "-- @statement"

fun main(args: Array<String>) {
    require(args.size == 1) { "Expected repository root" }
    val root = File(args.single()).canonicalFile
    val migrations = File(root, "provenance/migrations")
    val seeds = File(root, "provenance/seeds")
    val output = File(root, "provenance/generated/thomas-provenance.sqlite")
    buildDatabase(migrations, seeds, output)
    println("Generated ${output.relativeTo(root).invariantSeparatorsPath}")
}

internal fun buildDatabase(migrations: File, seeds: File, output: File) {
    require(migrations.isDirectory) { "Missing migration directory: $migrations" }
    require(seeds.isDirectory) { "Missing seed directory: $seeds" }
    output.parentFile.mkdirs()
    if (output.exists()) require(output.delete()) { "Could not replace generated database: $output" }

    Class.forName("org.sqlite.JDBC")
    DriverManager.getConnection("jdbc:sqlite:${output.absolutePath}").use { connection ->
        connection.autoCommit = false
        connection.createStatement().use { statement -> statement.execute("PRAGMA foreign_keys = ON") }

        governedSqlFiles(migrations).forEach { file ->
            executeScript(connection, file)
            recordInput(connection, "schema_migration", file)
        }
        governedSqlFiles(seeds).forEach { file ->
            executeScript(connection, file)
            recordInput(connection, "seed_application", file)
        }

        validateCorpus(connection)
        connection.commit()
    }
}

private fun governedSqlFiles(directory: File): List<File> =
    directory.listFiles()
        .orEmpty()
        .filter { it.isFile && it.extension == "sql" }
        .sortedBy { it.name }

private fun executeScript(connection: Connection, file: File) {
    val statements = file.readText(Charsets.UTF_8)
        .split(Regex("(?m)^${Regex.escape(STATEMENT_MARKER)}\\s*$"))
        .map { it.trim() }
        .filter { it.isNotEmpty() && it.lineSequence().any { line -> !line.trim().startsWith("--") } }

    require(statements.isNotEmpty()) { "No governed statements in ${file.name}" }
    connection.createStatement().use { statement -> statements.forEach(statement::execute) }
}

private fun recordInput(connection: Connection, table: String, file: File) {
    require(table == "schema_migration" || table == "seed_application")
    connection.prepareStatement("INSERT INTO $table(file_name, sha256) VALUES (?, ?)").use { statement ->
        statement.setString(1, file.name)
        statement.setString(2, sha256(file.readBytes()))
        statement.executeUpdate()
    }
}

private fun validateCorpus(connection: Connection) {
    connection.createStatement().use { statement ->
        statement.executeQuery("PRAGMA foreign_key_check").use { result ->
            check(!result.next()) { "Foreign-key violation in ${result.getString(1)}" }
        }
    }

    require(count(connection, "source_document") in 12..25) { "Initial corpus must remain selective" }
    require(count(connection, "source_version") >= count(connection, "source_document"))
    require(count(connection, "rights_record") == count(connection, "source_version")) {
        "Every source version requires a rights record"
    }
    require(count(connection, "source_version_without_class") == 0L) { "Every source needs a source class" }
    require(count(connection, "source_version_without_freshness") == 0L) { "Every version needs freshness metadata" }
    require(count(connection, "clinical_version_without_pending_review") == 0L) {
        "Every clinical source version must enter the human review queue"
    }
}

private fun count(connection: Connection, tableOrView: String): Long =
    connection.createStatement().use { statement ->
        statement.executeQuery("SELECT COUNT(*) FROM $tableOrView").use { result ->
            result.next()
            result.getLong(1)
        }
    }

internal fun sha256(bytes: ByteArray): String =
    MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }
