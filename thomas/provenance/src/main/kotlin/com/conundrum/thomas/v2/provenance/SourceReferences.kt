package com.conundrum.thomas.v2.provenance

private val governedIdFormat = Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$")

private fun requireGovernedId(kind: String, value: String): String {
    require(governedIdFormat.matches(value)) { "$kind must be a lowercase hyphenated governed identifier." }
    return value
}

@JvmInline
value class SourceDocumentId private constructor(val value: String) {
    companion object {
        fun parse(value: String) = SourceDocumentId(requireGovernedId("Source document ID", value))
    }
}

@JvmInline
value class SourceVersionId private constructor(val value: String) {
    companion object {
        fun parse(value: String) = SourceVersionId(requireGovernedId("Source version ID", value))
    }
}

@JvmInline
value class SourceSectionId private constructor(val value: String) {
    companion object {
        fun parse(value: String) = SourceSectionId(requireGovernedId("Source section ID", value))
    }
}

@JvmInline
value class SourceLocatorId private constructor(val value: String) {
    companion object {
        fun parse(value: String) = SourceLocatorId(requireGovernedId("Source locator ID", value))
    }
}

@JvmInline
value class SourceConflictId private constructor(val value: String) {
    companion object {
        fun parse(value: String) = SourceConflictId(requireGovernedId("Source conflict ID", value))
    }
}

@JvmInline
value class ReviewRequirementId private constructor(val value: String) {
    companion object {
        fun parse(value: String) = ReviewRequirementId(requireGovernedId("Review requirement ID", value))
    }
}

data class GovernedSourceReference(
    val documentId: SourceDocumentId,
    val versionId: SourceVersionId,
    val sectionId: SourceSectionId,
    val locatorId: SourceLocatorId? = null,
)
