package com.conundrum.thomas.v2.longitudinal.store

import java.nio.file.Path

enum class StoreFaultPoint { AFTER_LEDGER_INSERT, AFTER_PROJECTION_WRITE, BEFORE_COMMIT }
fun interface StoreFaultInjector { fun check(point: StoreFaultPoint) }

class QualificationStoreLocation private constructor(internal val jdbcUrl: String, val description: String) {
    companion object {
        fun file(path: Path): QualificationStoreLocation {
            val absolute = path.toAbsolutePath().normalize()
            val temporaryRoot = Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().normalize()
            val underTemporary = absolute.startsWith(temporaryRoot)
            val underBuild = absolute.iterator().asSequence().any { it.toString().equals("build", ignoreCase = true) }
            require(underTemporary || underBuild) { "Qualification database path must be under build output or the OS temporary directory" }
            require(absolute.fileName.toString().endsWith(".sqlite")) { "Qualification database must use a .sqlite suffix" }
            return QualificationStoreLocation("jdbc:sqlite:$absolute", absolute.toString())
        }

        fun inMemory(name: String): QualificationStoreLocation {
            require(name.matches(Regex("^[a-z0-9-]+$")))
            return QualificationStoreLocation("jdbc:sqlite:file:$name?mode=memory&cache=shared", "in-memory:$name")
        }
    }
}

interface QualificationLongitudinalStore : AutoCloseable {
    val admission: LongitudinalAdmissionController
    val reader: LongitudinalReader
    fun replayIntoEmpty(location: QualificationStoreLocation): QualificationLongitudinalStore
}

object QualificationLongitudinalStoreFactory {
    fun open(
        location: QualificationStoreLocation,
        clock: StoreClock,
        faultInjector: StoreFaultInjector = StoreFaultInjector { },
    ): QualificationLongitudinalStore = SQLiteQualificationLongitudinalStore.open(location, clock, faultInjector)
}

// Shared governed ports, including the sealed interface AcceptedAdmissionReceipt, live in
// :thomas:personal-data-persistence so Android never imports the JDBC qualification adapter.
