package com.conundrum.thomas.v2

/** Temporary qualification diagnostics; no content, state mutation, or dispatch interception. */
object SubmissionDispatchProbe {
    fun record(stage: String, details: String = "") {
        android.util.Log.i("R1Dispatch", "t=" + android.os.SystemClock.elapsedRealtimeNanos() +
            " thread=" + Thread.currentThread().name + " stage=" + stage + " " + details)
    }
}
