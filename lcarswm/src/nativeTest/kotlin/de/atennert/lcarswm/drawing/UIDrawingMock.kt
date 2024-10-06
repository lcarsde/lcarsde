package de.atennert.lcarswm.drawing

import de.atennert.lcarswm.system.FunctionCall

class UIDrawingMock : UIDrawing {
    val functionCalls = mutableListOf<FunctionCall>()

    override fun drawWindowManagerFrame() {
        functionCalls.add(FunctionCall("drawWindowManagerFrame"))
    }
}