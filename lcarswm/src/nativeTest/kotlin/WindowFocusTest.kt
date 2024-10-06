import de.atennert.lcarswm.system.FunctionCall
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.pointed
import xlib.Above
import xlib.CWStackMode
import xlib.Window
import xlib.XWindowChanges
import kotlin.test.assertEquals

@ExperimentalForeignApi
class WindowFocusTest {
    private fun checkRestacking(restackCall: FunctionCall, window: Window) {
        assertEquals("configureWindow", restackCall.name, "The window $window needs to be configured")
        assertEquals(window, restackCall.parameters[0], "The _window ${window}_ needs to be configured")
        assertEquals(CWStackMode.convert<UInt>(), restackCall.parameters[1], "The $window window needs to be restacked")
        assertEquals(Above, (restackCall.parameters[2] as CPointer<XWindowChanges>).pointed.stack_mode, "The stack mode should be 'above'")
    }
}