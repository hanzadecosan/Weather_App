package com.example.hava

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherMotionAssetTest {
    @Test
    @SdkSuppress(minSdkVersion = 28)
    fun bundledWeatherLoopsDecodeAsAnimatedImages() {
        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        val loops = listOf(R.raw.motion_sun, R.raw.motion_cloud, R.raw.motion_rain,
            R.raw.motion_snow, R.raw.motion_storm, R.raw.motion_fog, R.raw.motion_night,
            R.raw.motion_sunrise, R.raw.motion_sunset)
        loops.forEach { resource ->
            val drawable = ImageDecoder.decodeDrawable(ImageDecoder.createSource(resources, resource))
            assertTrue("Resource $resource must be animated", drawable is AnimatedImageDrawable)
            assertTrue(drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0)
        }
    }
}