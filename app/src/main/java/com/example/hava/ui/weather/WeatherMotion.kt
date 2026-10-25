package com.example.hava.ui.weather

import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import com.example.hava.R

private data class MotionAsset(@field:RawRes val loop: Int, @field:DrawableRes val still: Int)

/** Respect both the screen lifecycle and the system animation preference. */
@Composable
internal fun weatherMotionEnabled(): Boolean {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val state by lifecycle.currentStateFlow.collectAsState()
    val systemMotion = remember(context, state) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }
    return systemMotion && state.isAtLeast(Lifecycle.State.STARTED)
}

/** Local GIFs are decoded by Coil's GIF module; paused/reduced motion uses the matching PNG. */
@Composable
fun WeatherMotion(
    kind: WeatherKind,
    modifier: Modifier = Modifier,
    isDay: Boolean = true,
    phase: SolarPhase? = null,
    contentDescription: String? = null
) {
    val asset = when {
        phase == SolarPhase.DAWN -> MotionAsset(R.raw.motion_sunrise, R.drawable.motion_horizon_still)
        phase == SolarPhase.SUNSET -> MotionAsset(R.raw.motion_sunset, R.drawable.motion_horizon_still)
        kind == WeatherKind.SUNNY && !isDay -> MotionAsset(R.raw.motion_night, R.drawable.motion_night_still)
        else -> when (kind) {
            WeatherKind.SUNNY -> MotionAsset(R.raw.motion_sun, R.drawable.motion_sun_still)
            WeatherKind.CLOUDY -> MotionAsset(R.raw.motion_cloud, R.drawable.motion_cloud_still)
            WeatherKind.FOGGY -> MotionAsset(R.raw.motion_fog, R.drawable.motion_fog_still)
            WeatherKind.RAINY -> MotionAsset(R.raw.motion_rain, R.drawable.motion_rain_still)
            WeatherKind.SNOWY -> MotionAsset(R.raw.motion_snow, R.drawable.motion_snow_still)
            WeatherKind.STORMY -> MotionAsset(R.raw.motion_storm, R.drawable.motion_storm_still)
        }
    }
    if (weatherMotionEnabled()) {
        AsyncImage(model = asset.loop, contentDescription = contentDescription,
            placeholder = painterResource(asset.still), error = painterResource(asset.still),
            modifier = modifier.defaultMinSize(80.dp, 80.dp))
    } else {
        Image(painterResource(asset.still), contentDescription, modifier.defaultMinSize(80.dp, 80.dp))
    }
}
