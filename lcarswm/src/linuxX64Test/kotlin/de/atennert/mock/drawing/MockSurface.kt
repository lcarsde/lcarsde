package de.atennert.mock.drawing

import de.atennert.lcarswm.drawing.Surface

object MockSurface : Surface {
    override fun setSize(width: Int, height: Int) {}

    override fun createSession() = MockSession

    override fun dispose() {}
}