package de.atennert.lcarswm.drawing

import de.atennert.lcarswm.lifecycle.closeClosables
import de.atennert.lcarswm.system.DrawApiDummy
import kotlinx.cinterop.*
import xlib.*
import kotlin.test.*

@ExperimentalForeignApi
class ColorFactoryTest {

    @AfterTest
    fun teardown() {
        drawApi.clear()
        closeClosables()
    }

    @Test
    fun `create color GCs`() {
        val colorFactory = ColorFactory(drawApi, screen)
        val color = Color(0xFF, 0x99, 0)

        val gc = colorFactory.createColorGC(1.convert(), color)

        assertNotNull(gc)
        assertEquals(1.toULong(), drawApi.pixel)
    }

    @Test
    fun `create two color GCs`() {
        val colorFactory = ColorFactory(drawApi, screen)
        val color1 = Color(0xFF, 0x99, 0)
        val color2 = Color(0xFF, 0, 0x99)

        val gc1 = colorFactory.createColorGC(1.convert(), color1)
        val gc2 = colorFactory.createColorGC(1.convert(), color2)

        assertNotNull(gc1)
        assertNotNull(gc2)
        assertEquals(2.toULong(), drawApi.pixel)
    }

    @Test
    fun `allocate a color only once`() {
        val colorFactory = ColorFactory(drawApi, screen)
        val color = Color(0xFF, 0x99, 0)

        val gc1 = colorFactory.createColorGC(1.convert(), color)
        val gc2 = colorFactory.createColorGC(1.convert(), color)

        assertNotNull(gc1)
        assertEquals(gc1, gc2)
        assertEquals(1.toULong(), drawApi.pixel)
    }

    @Test
    fun `create different GCs for different drawables`() {
        val colorFactory = ColorFactory(drawApi, screen)
        val color = Color(0xFF, 0x99, 0)

        val gc1 = colorFactory.createColorGC(1.convert(), color)
        val gc2 = colorFactory.createColorGC(2.convert(), color)

        assertNotNull(gc1)
        assertNotNull(gc2)
        assertNotSame(gc1, gc2)
        assertEquals(1.toULong(), drawApi.pixel)
    }

    @Test
    fun `create XFT color by color`() {
        val colorFactory = ColorFactory(drawApi, screen)
        val color = Color(0xFF, 0x99, 0)

        val xftColor = colorFactory.createXftColor(color)

        assertEquals(color.red, xftColor.color.red.toInt())
        assertEquals(color.green, xftColor.color.green.toInt())
        assertEquals(color.blue, xftColor.color.blue.toInt())
        assertEquals(drawApi.pixel, xftColor.pixel)
    }

    @Test
    fun `get same XFT color twice`() {
        val colorFactory = ColorFactory(drawApi, screen)
        val color = Color(0xFF, 0x99, 0)

        val xftColor1 = colorFactory.createXftColor(color)
        val xftColor2 = colorFactory.createXftColor(color)

        assertEquals(xftColor1.rawPtr, xftColor2.rawPtr)
    }

    private val drawApi = object : DrawApiDummy() {
        var pixel = 0.toULong()

        fun clear() {
            pixel = 0.convert()
        }

        override fun createColormap(window: Window, visual: CValuesRef<Visual>, alloc: Int): Colormap {
            return 42.convert()
        }

        override fun allocColor(colorMap: Colormap, color: CPointer<XColor>): Int {
            assertEquals(42.toULong(), colorMap)
            pixel += 1.convert()
            color.pointed.pixel = pixel
            return 1
        }

        override fun createGC(drawable: Drawable, mask: ULong, gcValues: CValuesRef<XGCValues>?): GC {
            return nativeHeap.alloc<GCVar>().ptr.reinterpret()
        }

        override fun freeColormap(colorMap: Colormap): Int = 1

        override fun freeGC(graphicsContext: GC): Int {
            nativeHeap.free(graphicsContext)
            return 1
        }

        override fun freeColors(colorMap: Colormap, pixels: CValuesRef<ULongVar>, pixelCount: Int): Int = 1
    }

    @ThreadLocal
    companion object {
        private lateinit var visual: Visual
        private lateinit var screen: Screen

        @BeforeClass
        fun setupAll() {
            visual = nativeHeap.alloc()
            screen = nativeHeap.alloc()
            screen.root = 1.convert()
            screen.root_visual = visual.ptr
        }

        @AfterClass
        fun teardownAll() {
            nativeHeap.free(screen)
            nativeHeap.free(visual)
        }
    }
}