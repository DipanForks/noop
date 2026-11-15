package com.noop.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.noop.analytics.BiscuitOutfit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.util.ReflectionHelpers
import org.robolectric.util.ReflectionHelpers.ClassParameter
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CompanionPicturesTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()

    private val assetArchive: File by lazy {
        val zip = File.createTempFile("biscuit_assets", ".zip")
        ZipOutputStream(zip.outputStream()).use { out ->
            File("src/main/assets/biscuit").listFiles()?.sortedBy { it.name }?.forEach { asset ->
                out.putNextEntry(ZipEntry("assets/biscuit/${asset.name}"))
                asset.inputStream().use { it.copyTo(out) }
                out.closeEntry()
            }
        }
        zip.deleteOnExit()
        zip
    }

    @Before fun mountAssets() {
        // Robolectric's AssetManager only sees zip archives (like APKs), never plain directories.
        val cookie = ReflectionHelpers.callInstanceMethod<Int>(
            context.assets,
            "addAssetPath",
            ClassParameter.from(String::class.java, assetArchive.path),
        )
        assertTrue("asset archive did not mount", cookie != 0)
    }

    private fun picture(name: String): File = File(context.filesDir, "noop_widget_companion/companion-$name.png")

    @Test fun outfitForFaceMapsKnownFaces() {
        assertEquals(setOf(BiscuitOutfit.COACH), CompanionPictures.outfitForFace("face-coach"))
        assertEquals(setOf(BiscuitOutfit.GLASSES), CompanionPictures.outfitForFace("face-glasses"))
        assertEquals(setOf(BiscuitOutfit.NIGHTCAP), CompanionPictures.outfitForFace("face-nightcap"))
        assertEquals(setOf(BiscuitOutfit.CROWN), CompanionPictures.outfitForFace("face-crown"))
        assertTrue(CompanionPictures.outfitForFace("").isEmpty())
        assertTrue(CompanionPictures.outfitForFace("face-bogus").isEmpty())
    }

    @Test fun theFirstDrawWritesEveryPictureAndLaterDrawsSkip() {
        assertTrue(CompanionPictures.drawPictures(context, null))
        val names = listOf("asleep", "radiant", "proud", "calm", "worried", "happy", "flame", "chest")
        val files = names.associateWith { picture(it) }
        for ((name, file) in files) {
            assertTrue("missing $name", file.isFile)
            assertTrue("empty $name", file.length() > 0)
        }
        val before = files.mapValues { it.value.lastModified() to it.value.length() }
        // The signature is unchanged, so nothing is rewritten.
        assertFalse(CompanionPictures.drawPictures(context, null))
        for (name in names) {
            val file = files.getValue(name)
            assertEquals("rewrote $name", before.getValue(name), file.lastModified() to file.length())
        }
    }

    @Test fun aNewFaceForcesRedraw() {
        assertTrue(CompanionPictures.drawPictures(context, null))
        com.noop.ui.GoalsStore.setEquippedFace(context, "face-crown")
        assertTrue(CompanionPictures.drawPictures(context, null))
    }

    @Test fun theChestDrawsItsBodyInsideATransparentFrame() {
        assertTrue(CompanionPictures.drawPictures(context, null))
        val decoded = BitmapFactory.decodeFile(picture("chest").path)
        assertTrue("chest PNG did not decode", decoded is Bitmap)
        val bmp = decoded!!
        assertTrue(bmp.width == 240 && bmp.height == 240)
        assertTrue(bmp.getPixel(120, 120) and 0xFF000000.toInt() != 0)
        assertTrue(bmp.getPixel(0, 0) and 0xFF000000.toInt() == 0)
    }
}
