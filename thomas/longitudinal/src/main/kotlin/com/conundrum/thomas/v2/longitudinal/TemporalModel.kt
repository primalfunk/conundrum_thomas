package com.conundrum.thomas.v2.longitudinal

import java.io.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

@JvmInline value class ReportTime(val value: Instant) : Serializable

@JvmInline value class RecordTime(val value: Instant) : Serializable

enum class ApproximationPrecision { DAYS, WEEKS, MONTHS, SEASON, YEAR }

enum class RelativeTemporalRelation { BEFORE, AFTER, DURING, AROUND, DURATION }

sealed interface CalendarBoundary : Serializable {
    val earliest: LocalDate
    val latest: LocalDate

    data class Date(val value: LocalDate) : CalendarBoundary {
        override val earliest: LocalDate = value
        override val latest: LocalDate = value
    }

    data class Month(val value: YearMonth) : CalendarBoundary {
        override val earliest: LocalDate = value.atDay(1)
        override val latest: LocalDate = value.atEndOfMonth()
    }

    data class CalendarYear(val value: Year) : CalendarBoundary {
        override val earliest: LocalDate = value.atDay(1)
        override val latest: LocalDate = value.atMonth(12).atEndOfMonth()
    }
}

/** Event-time expression. It never supplies a current time or invents precision. */
sealed interface EventTime : Serializable {
    data class ExactInstant(val value: Instant) : EventTime
    data class CalendarDate(val value: LocalDate) : EventTime
    data class ApproximateDate(val center: LocalDate, val precision: ApproximationPrecision) : EventTime
    data class ApproximateYear(val year: Year) : EventTime

    data class Range(
        val start: CalendarBoundary,
        val end: CalendarBoundary,
        val startApproximate: Boolean = false,
        val endApproximate: Boolean = false,
    ) : EventTime {
        init { require(!start.earliest.isAfter(end.latest)) { "Temporal range start must not follow its end" } }
    }

    data class RelativePeriod(
        val description: String,
        val relation: RelativeTemporalRelation,
        val anchorEntityId: LifeEntityId? = null,
    ) : EventTime {
        init { require(description.isNotBlank()) }
    }

    data class OngoingInterval(
        val knownStart: CalendarBoundary? = null,
        val description: String,
    ) : EventTime {
        init { require(description.isNotBlank()) }
    }

    data class BeforeOrAfter(
        val referenceEntityId: LifeEntityId,
        val relation: RelativeTemporalRelation,
        val description: String,
    ) : EventTime {
        init {
            require(relation == RelativeTemporalRelation.BEFORE || relation == RelativeTemporalRelation.AFTER)
            require(description.isNotBlank())
        }
    }

    data class UncertainChronology(val description: String, val candidateLabels: List<String>) : EventTime {
        init {
            require(description.isNotBlank())
            require(candidateLabels.size >= 2)
            require(candidateLabels.none(String::isBlank))
        }
    }

    data class Unknown(val statedReason: String? = null) : EventTime {
        init { require(statedReason == null || statedReason.isNotBlank()) }
    }
}

data class TemporalCoordinates(
    val eventTime: EventTime,
    val reportTime: ReportTime,
    val recordTime: RecordTime,
) : Serializable {
    init {
        require(!recordTime.value.isBefore(reportTime.value)) {
            "Record time cannot precede the source report time"
        }
    }
}
