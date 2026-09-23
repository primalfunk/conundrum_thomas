package com.conundrum.thomas.v2.platform.speech

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechInputQualificationTest {
    @Test fun `speech result enters the editable draft without sending`() {
        val session = SpeechDraftSession()
        assertTrue(session.begin(""))
        assertEquals("A spoken turn", session.partial("A spoken turn"))
        assertEquals("A spoken turn", session.finish("A spoken turn"))
        assertEquals(SpeechCaptureState.TRANSCRIPT_READY, session.state)
    }

    @Test fun `finished speech remains editable`() {
        val session = SpeechDraftSession()
        session.begin("")
        session.finish("raw recognition")

        session.setDraft("corrected recognition")

        assertEquals("corrected recognition", session.finish("ignored second callback"))
        assertEquals(SpeechCaptureState.TRANSCRIPT_READY, session.state)
    }

    @Test fun `existing typed text is preserved when speech is appended`() {
        val session = SpeechDraftSession()
        session.begin("Typed beginning")

        assertEquals("Typed beginning spoken addition", session.finish("spoken addition"))
    }

    @Test fun `multiple speech sessions append rather than overwrite`() {
        val session = SpeechDraftSession()
        session.begin("")
        session.finish("first segment")
        session.begin("first segment")

        assertEquals("first segment second segment", session.finish("second segment"))
    }

    @Test fun `stop requested preserves partial text while final callback completes it`() {
        val session = SpeechDraftSession()
        session.begin("Existing")
        session.partial("partial words")
        session.stopRequested()

        assertEquals(SpeechCaptureState.FINALIZING, session.state)
        assertEquals("Existing final words", session.finish("final words"))
    }

    @Test fun `cancel restores the pre-capture draft exactly`() {
        val session = SpeechDraftSession()
        session.begin("Keep this text")
        session.partial("discard this recognition")

        assertEquals("Keep this text", session.cancel())
        assertEquals(SpeechCaptureState.IDLE, session.state)
    }

    @Test fun `empty speech never adds a turn`() {
        val session = SpeechDraftSession()
        session.begin("Existing draft")

        assertEquals("Existing draft", session.finish(""))
        assertTrue(session.state == SpeechCaptureState.TRANSCRIPT_READY)
    }

    @Test fun `recognizer failure leaves keyboard draft usable`() {
        val session = SpeechDraftSession()
        session.begin("Existing")
        session.partial("recognized before failure")

        assertEquals("Existing recognized before failure", session.fail(SpeechFailure.PROVIDER_ERROR))
        assertEquals(SpeechCaptureState.ERROR, session.state)
        session.setDraft("typed recovery")
        assertEquals("typed recovery", session.finish("ignored"))
    }

    @Test fun `unavailable recognizer is a non-blocking state`() {
        val session = SpeechDraftSession()
        session.begin("Existing")

        assertEquals("Existing", session.fail(SpeechFailure.RECOGNIZER_UNAVAILABLE))
        assertEquals(SpeechCaptureState.UNAVAILABLE, session.state)
        session.setDraft("keyboard still works")
        assertEquals("keyboard still works", session.finish("ignored"))
    }

    @Test fun `permission failure does not erase the composer`() {
        val session = SpeechDraftSession()
        session.begin("Typed text")

        assertEquals("Typed text", session.fail(SpeechFailure.PERMISSION_DENIED))
        assertEquals(SpeechCaptureState.ERROR, session.state)
    }

    @Test fun `lifecycle reset cleans active capture without retaining recognition`() {
        val session = SpeechDraftSession()
        session.begin("Existing")
        session.partial("in flight")

        session.reset("Existing")

        assertEquals(SpeechCaptureState.IDLE, session.state)
        assertTrue(session.begin("Existing"))
        assertEquals("Existing resumed", session.finish("resumed"))
    }

    @Test fun `duplicate final callbacks cannot duplicate recognized text`() {
        val session = SpeechDraftSession()
        session.begin("")
        session.finish("one turn")

        assertEquals("one turn", session.finish("one turn"))
    }

    @Test fun `interruption retains partial material for keyboard recovery`() {
        val session = SpeechDraftSession()
        session.begin("Typed")
        session.partial("spoken")

        assertEquals("Typed spoken", session.fail(SpeechFailure.INTERRUPTED))
        assertEquals(SpeechCaptureState.ERROR, session.state)
    }

    @Test fun `a failed capture can be retried without stale recognition`() {
        val session = SpeechDraftSession()
        session.begin("Existing")
        session.partial("stale")
        session.fail(SpeechFailure.TIMEOUT)
        session.setDraft("Existing edited")
        session.begin("Existing edited")

        assertEquals("Existing edited fresh", session.finish("fresh"))
    }

    @Test fun `append normalizes only the capture boundary spacing`() {
        assertEquals("one two", SpeechDraftSession.append("one ", " two"))
        assertEquals("two", SpeechDraftSession.append("", " two"))
        assertEquals("one", SpeechDraftSession.append("one ", ""))
    }
}
