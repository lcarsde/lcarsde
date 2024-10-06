package de.atennert.lcarswm.atom

import de.atennert.lcarswm.conversion.toULongArray
import de.atennert.lcarswm.system.wrapXFree
import de.atennert.lcarswm.system.wrapXGetWindowProperty
import kotlinx.cinterop.*
import xlib.Display
import xlib.FALSE
import xlib.Success
import xlib.Window

@ExperimentalForeignApi
class NumberAtomReader(private val display: CPointer<Display>?, private val atomLibrary: AtomLibrary) {

    fun readULongArrayPropertyOrNull(windowId: Window, atom: Atoms, type: Atoms): ULongArray? {
        return getPropertyAsUByteArrayOrNull(windowId, atom, type, 32)
            ?.toULongArray()
    }

    private fun getPropertyAsUByteArrayOrNull(windowId: Window, atom: Atoms, type: Atoms, size: Int): UByteArray? {
        val returnType = nativeHeap.alloc(0.toULong())
        val returnFormat = nativeHeap.alloc<Int>(0)
        val returnItemCount = nativeHeap.alloc(0.toULong())
        val returnBytesAfter = nativeHeap.alloc(0.toULong())
        val returnProperty = nativeHeap.allocPointerTo<UByteVar>()

        val response = wrapXGetWindowProperty(
            display,
            windowId,
            atomLibrary[atom],
            0,
            Long.MAX_VALUE,
            FALSE,
            atomLibrary[type],
            returnType.ptr,
            returnFormat.ptr,
            returnItemCount.ptr,
            returnBytesAfter.ptr,
            returnProperty.ptr
        )

        val result = if (response == Success
            && returnFormat.value == size
            && returnItemCount.value > 0.toULong()
        ) {
            UByteArray(returnItemCount.value.toInt() * (size / 8)) {
                returnProperty.value!![0]
            }
        } else {
            null
        }

        nativeHeap.free(returnType)
        nativeHeap.free(returnFormat)
        nativeHeap.free(returnItemCount)
        nativeHeap.free(returnBytesAfter)
        wrapXFree(returnProperty.ptr)

        return result
    }
}