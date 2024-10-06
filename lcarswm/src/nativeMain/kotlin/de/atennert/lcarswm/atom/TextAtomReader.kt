package de.atennert.lcarswm.atom

import de.atennert.lcarswm.system.api.SystemApi
import kotlinx.cinterop.*
import xlib.Window
import xlib.XTextProperty

@ExperimentalForeignApi
class TextAtomReader(private val system: SystemApi, private val atomLibrary: AtomLibrary) {
    fun readTextProperty(windowId: Window, atom: Atoms): String {
        val textProperty = nativeHeap.alloc<XTextProperty>()
        val result = system.getTextProperty(windowId, textProperty.ptr, atomLibrary[atom])

        if (result == 0 || textProperty.nitems.toInt() == 0 || !hasCorrectEncoding(textProperty)) {
            return NO_NAME
        }

        val name = when (textProperty.encoding) {
            atomLibrary[Atoms.COMPOUND_TEXT] -> {
                val localeString = getByteArrayListFromCompound(textProperty).toKString()
                val readBytes = ULongArray(1)
                val utfBytes = readBytes.usePinned { readBytesPinned ->
                    system.localeToUtf8(localeString, (-1).convert(), readBytesPinned.addressOf(0))
                        ?: system.localeToUtf8(localeString, readBytesPinned.get()[0].convert(), null)
                        ?: return NO_NAME
                }
                ByteArray(readBytes[0].convert()) { utfBytes[it] }.toKString()
            }
            atomLibrary[Atoms.UTF_STRING] -> {
                getByteArray(textProperty).toKString()
            }
            atomLibrary[Atoms.STRING] -> {
                val latinString = getByteArray(textProperty).takeWhile { c ->
                    // filter forbidden control characters
                    c.toInt() == 9 ||
                            c.toInt() == 10 ||
                            c.toInt() in 32..126 ||
                            c.toInt() > 160
                }.toByteArray()
                    .toKString()
                    .trim()
                val readBytes = ULongArray(1)
                val utfBytes = readBytes.usePinned { readBytesPinned ->
                    system.convertLatinToUtf8(latinString, (-1).convert(), readBytesPinned.addressOf(0))
                        ?: system.convertLatinToUtf8(latinString, readBytesPinned.get()[0].convert(), null)
                        ?: return NO_NAME
                }
                ByteArray(readBytes[0].convert()) { utfBytes[it] }.toKString()
            }
            else -> ""
        }
        system.xFree(textProperty.value)
        nativeHeap.free(textProperty)

        return name.trim()
                .ifEmpty { NO_NAME }
                .uppercase()
    }

    private fun hasCorrectEncoding(textProperty: XTextProperty): Boolean {
        return textProperty.encoding == atomLibrary[Atoms.STRING] ||
                textProperty.encoding == atomLibrary[Atoms.UTF_STRING] ||
                textProperty.encoding == atomLibrary[Atoms.COMPOUND_TEXT]
    }

    private fun getByteArrayListFromCompound(textProperty: XTextProperty): ByteArray {
        val resultList = nativeHeap.allocPointerTo<CPointerVar<ByteVar>>()
        val listCount = IntArray(1)
        listCount.usePinned { listCountPinned ->
            system.xmbTextPropertyToTextList(textProperty.ptr, resultList.ptr, listCountPinned.addressOf(0))
        }

        val byteList = mutableListOf<Byte>()
        var index = 0
        var value = resultList.value?.get(0)?.get(0)
        while (value != null && value.toInt() != 0) {
            byteList.add(value)
            index++
            value = resultList.value?.get(0)?.get(index)
        }
        byteList.add(0)
        nativeHeap.free(resultList)
        return byteList.toByteArray()
    }

    private fun getByteArray(textProperty: XTextProperty): ByteArray {
        return UByteArray(textProperty.nitems.convert()) { textProperty.value?.get(it)!! }
            .fold(mutableListOf<Byte>()) { list, ub ->
                list.add(ub.convert())
                if (ub.convert<Int>() == 0) {
                    return@fold list
                }
                list
            }
            .toByteArray()
    }

    companion object {
        const val NO_NAME = "-"
    }
}