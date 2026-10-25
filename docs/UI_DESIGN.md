# Hava — UI and motion

The supplied references inform the warm/cool light, large central temperature,
thin line icons and glass panels. Artwork is original and generated locally.

## Boundaries

The existing Activity → ViewModel → Repository → API flow is unchanged.
WeatherTimeline remains the source of local time, solar phase and forecast selection.
The presentation files have one responsibility each:

| File | Responsibility |
| --- | --- |
| ui/city/CityScreen.kt | Adaptive city grid, search, working city shortcuts |
| ui/city/CityCard.kt | Stable, name-based decorative card palettes; no simulated forecasts |
| ui/weather/WeatherDetailScreen.kt | Lifecycle-aware clock, state handling and section composition |
| ui/weather/WeatherComponents.kt | Hero, metrics, solar light track, hourly strip |
| ui/weather/WeatherAppearance.kt | Central phase/condition palette and glass/ink tokens |
| ui/weather/AnimatedWeatherBackground.kt | Draw-only ambient light, seamless trails and snow/stars |
| ui/weather/WeatherMotion.kt | Local GIF/PNG resource selection and motion policy |

## Motion assets

Nine bundled transparent GIFs cover sun, cloud, rain, snow, storm, fog, night,
sunrise and sunset. Each loop is sampled at 32 steps / 70 ms and runs for 2.24 seconds. Pillow merges identical adjacent frames, so the encoded files contain 28–32 frames. All loop infinitely.
They require no network permission or external image URLs. The complete GIF set
is under 800 KB. Still PNGs provide immediate placeholders and reduced-motion views.

Regenerate with Python and Pillow:

```sh
python tools/generate_weather_motion.py
```

Coil Compose + coil-gif decode these resources. Hourly forecast cells use static PNGs
so a scrolling row does not start twelve independent decoders. Decorative GIFs have
no accessibility label; adjacent text carries the actual weather information.

Motion is enabled only while the lifecycle is STARTED and the system animator
scale is nonzero. Removing the active branch releases the animated painter and
ambient infinite transition; matching PNGs remain visible. Palette changes use
Compose color transitions. No weather or timing logic is placed in animation code.

## Solar appearance

- Day: warm paper for clear skies; cool blue/lavender for cloud, rain and snow.
- Dawn: rose/apricot light with a sunrise loop and the existing invitation.
- Sunset: amber/coral light with a horizon loop and the existing invitation.
- Night: navy/lavender, pale foreground and a moon for clear skies.

The existing two-hour invitation window and twenty-minute twilight are preserved.
All solar times, freshness and retry behavior continue to come from the existing
WeatherTimeline and WeatherUiState. The daylight track is a clamped visual ratio,
not a new source of time or forecast data.

## Validation

Existing time/race/state unit tests remain in place. UI tests cover the invitation,
retry/back actions, empty search, stale-city protection and Activity recreation.
WeatherMotionAssetTest checks that all bundled GIFs decode as animated drawables.
Screenshots use Compose root capture (not an asynchronous compositor snapshot).