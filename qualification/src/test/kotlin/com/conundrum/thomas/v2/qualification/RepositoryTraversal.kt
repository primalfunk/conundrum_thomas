package com.conundrum.thomas.v2.qualification

import java.io.File

/**
 * Traverses canonical repository source only. The ignored `out/` directory
 * contains retained historical qualification snapshots and is not part of the
 * source tree being asserted by boundary tests.
 */
internal fun File.walkCanonicalTopDown(): Sequence<File> = walkTopDown().onEnter { directory ->
    directory.name !in setOf(".git", ".gradle", ".idea", "build", "out")
}
