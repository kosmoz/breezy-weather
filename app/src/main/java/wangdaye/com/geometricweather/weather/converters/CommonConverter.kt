package wangdaye.com.geometricweather.weather.converters

import android.content.Context
import us.dustinj.timezonemap.TimeZoneMap
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.weather.Daily
import wangdaye.com.geometricweather.common.basic.models.weather.HalfDay
import wangdaye.com.geometricweather.common.basic.models.weather.Hourly
import wangdaye.com.geometricweather.common.basic.models.weather.Precipitation
import wangdaye.com.geometricweather.common.basic.models.weather.PrecipitationProbability
import wangdaye.com.geometricweather.common.basic.models.weather.UV
import wangdaye.com.geometricweather.common.basic.models.weather.Wind
import wangdaye.com.geometricweather.common.utils.DisplayUtils
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Helps complete a daily list with information from hourly list.
 * Mainly used by providers which don’t provide half days but only full days.
 * Currently helps completing:
 * - Weather code (at 12:00 for day, at 00:00 for night)
 * - Weather text/phase (at 12:00 for day, at 00:00 for night)
 * - Precipitation (if Precipitation or Precipitation.total is null)
 * - PrecipitationProbability (if PrecipitationProbability or PrecipitationProbability.total is null)
 * - Wind (if Wind or Wind.speed is null)
 * You can expand it to other fields if you need it.
 *
 * @param initialDailyList a list of Daily where for each Daily, date is initialized at 00:00 on the TimeZone passed as 3rd parameter
 * @param hourlyListByDay a Map constructed as {"yyyyMMdd" => {"day": List<Hourly>, "night": List<Hourly>}}
 * @param timeZone the timezone of the location
 * @return a new List<Daily>, the initial dailyList passed as 1st parameter can be freed after
 */
fun completeDailyListWithHourlyList(
    initialDailyList: List<Daily>,
    hourlyListByDay: Map<String?, Map<String, List<Hourly>>>,
    timeZone: TimeZone?
): List<Daily> {
    val dailyList = ArrayList<Daily>()

    for (initialDaily in initialDailyList) {
        val dailyDate = initialDaily.date
        var dailyDay = initialDaily.day()
        var dailyNight = initialDaily.night()
        val dailySun = initialDaily.sun()
        val dailyMoon = initialDaily.moon()
        val dailyMoonPhase = initialDaily.moonPhase
        val dailyAirQuality = initialDaily.airQuality
        val dailyPollen = initialDaily.pollen
        val dailyUv = initialDaily.uV
        val dailyHoursOfSun = initialDaily.hoursOfSun

        val dailyDateFormatted = DisplayUtils.getFormattedDate(dailyDate, timeZone, "yyyyMMdd")
        if (hourlyListByDay.containsKey(dailyDateFormatted)) {
            for ((dayOrNight, halfDayHourlyList) in hourlyListByDay[dailyDateFormatted]!!) {
                var halfDayWeatherText = if (dayOrNight == "day") dailyDay?.weatherText else dailyNight?.weatherText
                var halfDayWeatherPhase = if (dayOrNight == "day") dailyDay?.weatherPhase else dailyNight?.weatherPhase
                var halfDayWeatherCode = if (dayOrNight == "day") dailyDay?.weatherCode else dailyNight?.weatherCode
                val halfDayTemperature = if (dayOrNight == "day") dailyDay?.temperature else dailyNight?.temperature
                var halfDayPrecipitation = if (dayOrNight == "day") dailyDay?.precipitation else dailyNight?.precipitation
                var halfDayPrecipitationProbability = if (dayOrNight == "day") dailyDay?.precipitationProbability else dailyNight?.precipitationProbability
                val halfDayPrecipitationDuration = if (dayOrNight == "day") dailyDay?.precipitationDuration else dailyNight?.precipitationDuration
                var halfDayWind = if (dayOrNight == "day") dailyDay?.wind else dailyNight?.wind
                val halfDayCloudCover = if (dayOrNight == "day") dailyDay?.cloudCover else dailyNight?.cloudCover

                // Weather code + Weather text
                if (halfDayWeatherCode == null || halfDayWeatherText == null) {
                    // Update at 12:00 on daytime and 00:00 on nighttime
                    halfDayHourlyList
                        .firstOrNull { it.date.time == dailyDate.time + (if (dayOrNight == "day") 12 else 24) * 3600 * 1000 }
                        ?.let {
                            if (halfDayWeatherCode == null) {
                                halfDayWeatherCode = it.weatherCode
                            }
                            if (halfDayWeatherPhase == null) {
                                halfDayWeatherPhase = it.weatherText
                            }
                            if (halfDayWeatherText == null) {
                                halfDayWeatherText = it.weatherText
                            }
                        }
                }

                // Precipitation
                if (halfDayPrecipitation?.total == null) {
                    val halfDayHourlyListPrecipitationTotal = halfDayHourlyList
                        .filter { it.precipitation?.total != null }
                    val precipitationTotal = if (halfDayHourlyListPrecipitationTotal.isNotEmpty()) {
                        halfDayHourlyListPrecipitationTotal
                            .map { it.precipitation!!.total!!.toDouble() }
                            .sumOf { it }
                    } else null

                    val halfDayHourlyListPrecipitationThunderstorm = halfDayHourlyList
                        .filter { it.precipitation?.thunderstorm != null }
                    val precipitationThunderstorm = if (halfDayHourlyListPrecipitationThunderstorm.isNotEmpty()) {
                        halfDayHourlyListPrecipitationThunderstorm
                            .map { it.precipitation!!.thunderstorm!!.toDouble()}
                            .sumOf { it }
                    } else null

                    val halfDayHourlyListPrecipitationRain = halfDayHourlyList
                        .filter { it.precipitation?.rain != null }
                    val precipitationRain = if (halfDayHourlyListPrecipitationRain.isNotEmpty()) {
                        halfDayHourlyListPrecipitationRain
                            .map { it.precipitation!!.rain!!.toDouble()}
                            .sumOf { it }
                    } else null

                    val halfDayHourlyListPrecipitationSnow = halfDayHourlyList
                        .filter { it.precipitation?.snow != null }
                    val precipitationSnow = if (halfDayHourlyListPrecipitationSnow.isNotEmpty()) {
                        halfDayHourlyListPrecipitationSnow
                            .map { it.precipitation!!.snow!!.toDouble()}
                            .sumOf { it }
                    } else null

                    val halfDayHourlyListPrecipitationIce = halfDayHourlyList
                        .filter { it.precipitation?.ice != null }
                    val precipitationIce = if (halfDayHourlyListPrecipitationIce.isNotEmpty()) {
                        halfDayHourlyListPrecipitationIce
                            .map { it.precipitation!!.ice!!.toDouble()}
                            .sumOf { it }
                    } else null

                    halfDayPrecipitation = Precipitation(
                        total = precipitationTotal?.toFloat(),
                        thunderstorm = precipitationThunderstorm?.toFloat(),
                        rain = precipitationRain?.toFloat(),
                        snow = precipitationSnow?.toFloat(),
                        ice = precipitationIce?.toFloat(),
                    )
                }

                // Precipitation probability
                if (halfDayPrecipitationProbability?.total == null) {
                    val halfDayHourlyListPrecipitationProbabilityTotal = halfDayHourlyList
                        .filter { it.precipitationProbability?.total != null }
                    val precipitationProbabilityTotal = if (halfDayHourlyListPrecipitationProbabilityTotal.isNotEmpty()) {
                        halfDayHourlyListPrecipitationProbabilityTotal
                            .map { it.precipitationProbability!!.total!!.toDouble()}
                            .maxOf { it }
                    } else null

                    val halfDayHourlyListPrecipitationProbabilityThunderstorm = halfDayHourlyList
                        .filter { it.precipitationProbability?.thunderstorm != null }
                    val precipitationProbabilityThunderstorm = if (halfDayHourlyListPrecipitationProbabilityThunderstorm.isNotEmpty()) {
                        halfDayHourlyListPrecipitationProbabilityThunderstorm
                            .map { it.precipitationProbability!!.thunderstorm!!.toDouble()}
                            .maxOf { it }
                    } else null

                    val halfDayHourlyListPrecipitationProbabilityRain = halfDayHourlyList
                        .filter { it.precipitationProbability?.rain != null }
                    val precipitationProbabilityRain = if (halfDayHourlyListPrecipitationProbabilityRain.isNotEmpty()) {
                        halfDayHourlyListPrecipitationProbabilityRain
                            .map { it.precipitationProbability!!.rain!!.toDouble()}
                            .maxOf { it }
                    } else null

                    val halfDayHourlyListPrecipitationProbabilitySnow = halfDayHourlyList
                        .filter { it.precipitationProbability?.snow != null }
                    val precipitationProbabilitySnow = if (halfDayHourlyListPrecipitationProbabilitySnow.isNotEmpty()) {
                        halfDayHourlyListPrecipitationProbabilitySnow
                            .map { it.precipitationProbability!!.snow!!.toDouble()}
                            .maxOf { it }
                    } else null

                    val halfDayHourlyListPrecipitationProbabilityIce = halfDayHourlyList
                        .filter { it.precipitationProbability?.ice != null }
                    val precipitationProbabilityIce = if (halfDayHourlyListPrecipitationProbabilityIce.isNotEmpty()) {
                        halfDayHourlyListPrecipitationProbabilityIce
                            .map { it.precipitationProbability!!.ice!!.toDouble()}
                            .maxOf { it }
                    } else null

                    halfDayPrecipitationProbability = PrecipitationProbability(
                        total = precipitationProbabilityTotal?.toFloat(),
                        thunderstorm = precipitationProbabilityThunderstorm?.toFloat(),
                        rain = precipitationProbabilityRain?.toFloat(),
                        snow = precipitationProbabilitySnow?.toFloat(),
                        ice = precipitationProbabilityIce?.toFloat(),
                    )
                }

                // Wind
                if (halfDayWind?.speed == null) {
                    halfDayHourlyList
                        .filter { it.wind?.speed != null }
                        .maxByOrNull { it.wind!!.speed!! }
                        ?.let {
                            halfDayWind = it.wind
                        }
                }

                if (dayOrNight == "day") {
                    dailyDay = HalfDay(
                        weatherText = halfDayWeatherText,
                        weatherPhase = halfDayWeatherPhase,
                        weatherCode = halfDayWeatherCode,
                        temperature = halfDayTemperature,
                        precipitation = halfDayPrecipitation,
                        precipitationProbability = halfDayPrecipitationProbability,
                        precipitationDuration = halfDayPrecipitationDuration,
                        wind = halfDayWind,
                        cloudCover = halfDayCloudCover
                    )
                } else {
                    dailyNight = HalfDay(
                        weatherText = halfDayWeatherText,
                        weatherPhase = halfDayWeatherPhase,
                        weatherCode = halfDayWeatherCode,
                        temperature = halfDayTemperature,
                        precipitation = halfDayPrecipitation,
                        precipitationProbability = halfDayPrecipitationProbability,
                        precipitationDuration = halfDayPrecipitationDuration,
                        wind = halfDayWind,
                        cloudCover = halfDayCloudCover
                    )
                }
            }
        }

        dailyList.add(
            Daily(
                date = dailyDate,
                day = dailyDay,
                night = dailyNight,
                sun = dailySun,
                moon = dailyMoon,
                moonPhase = dailyMoonPhase,
                airQuality = dailyAirQuality,
                pollen = dailyPollen,
                uV = dailyUv,
                hoursOfSun = dailyHoursOfSun
            )
        )
    }
    return dailyList
}

fun getTimeZoneForPosition(map: TimeZoneMap, lat: Double, lon: Double): TimeZone {
    return try {
        TimeZone.getTimeZone(map.getOverlappingTimeZone(lat, lon)!!.zoneId)
    } catch (ignored: Exception) {
        TimeZone.getDefault()
    }
}

fun getWindLevel(context: Context, speed: Float?): String? {
    return if (speed == null) {
        null
    } else when (speed) {
        in 0f..Wind.WIND_SPEED_0 -> context.getString(R.string.wind_0)
        in Wind.WIND_SPEED_0..Wind.WIND_SPEED_1 -> context.getString(R.string.wind_1)
        in Wind.WIND_SPEED_1..Wind.WIND_SPEED_2 -> context.getString(R.string.wind_2)
        in Wind.WIND_SPEED_2..Wind.WIND_SPEED_3 -> context.getString(R.string.wind_3)
        in Wind.WIND_SPEED_3..Wind.WIND_SPEED_4 -> context.getString(R.string.wind_4)
        in Wind.WIND_SPEED_4..Wind.WIND_SPEED_5 -> context.getString(R.string.wind_5)
        in Wind.WIND_SPEED_5..Wind.WIND_SPEED_6 -> context.getString(R.string.wind_6)
        in Wind.WIND_SPEED_6..Wind.WIND_SPEED_7 -> context.getString(R.string.wind_7)
        in Wind.WIND_SPEED_7..Wind.WIND_SPEED_8 -> context.getString(R.string.wind_8)
        in Wind.WIND_SPEED_8..Wind.WIND_SPEED_9 -> context.getString(R.string.wind_9)
        in Wind.WIND_SPEED_9..Wind.WIND_SPEED_10 -> context.getString(R.string.wind_10)
        in Wind.WIND_SPEED_10..Wind.WIND_SPEED_11 -> context.getString(R.string.wind_11)
        in Wind.WIND_SPEED_11..Float.MAX_VALUE -> context.getString(R.string.wind_12)
        else -> null
    }
}

fun getWindDirection(context: Context, degree: Float?): String? {
    return if (degree == null) {
        null
    } else when(degree) {
        in 0f..22.5f -> context.getString(R.string.wind_direction_short_N)
        in 22.5f..67.5f -> context.getString(R.string.wind_direction_short_NE)
        in 67.5f..112.5f -> context.getString(R.string.wind_direction_short_E)
        in 112.5f..157.5f -> context.getString(R.string.wind_direction_short_SE)
        in 157.5f..202.5f -> context.getString(R.string.wind_direction_short_S)
        in 202.5f..247.5f -> context.getString(R.string.wind_direction_short_SW)
        in 247.5f..292.5f -> context.getString(R.string.wind_direction_short_W)
        in 292.5f..337.5f -> context.getString(R.string.wind_direction_short_NW)
        in 337.5f..360f -> context.getString(R.string.wind_direction_short_N)
        else -> context.getString(R.string.wind_direction_short_variable)
    }
}

fun getMoonPhaseAngle(phase: String?): Int? {
    return if (phase.isNullOrEmpty()) {
        null
    } else when (phase.lowercase(Locale.getDefault())) {
        "waxingcrescent", "waxing crescent" -> 45
        "first", "firstquarter", "first quarter" -> 90
        "waxinggibbous", "waxing gibbous" -> 135
        "full", "fullmoon", "full moon" -> 180
        "waninggibbous", "waning gibbous" -> 225
        "third", "thirdquarter", "third quarter", "last", "lastquarter", "last quarter" -> 270
        "waningcrescent", "waning crescent" -> 315
        else -> 360
    }
}

fun getUVLevel(context: Context, uvIndex: Int?): String? {
    return if (uvIndex == null) {
        null
    } else when (uvIndex) {
        in 0..UV.UV_INDEX_LOW -> context.getString(R.string.uv_level_0_2)
        in UV.UV_INDEX_LOW..UV.UV_INDEX_MIDDLE -> context.getString(R.string.uv_level_3_5)
        in UV.UV_INDEX_MIDDLE..UV.UV_INDEX_HIGH -> context.getString(R.string.uv_level_6_7)
        in UV.UV_INDEX_HIGH..UV.UV_INDEX_EXCESSIVE -> context.getString(R.string.uv_level_8_10)
        in UV.UV_INDEX_EXCESSIVE..Int.MAX_VALUE -> context.getString(R.string.uv_level_11)
        else -> null
    }
}

fun isDaylight(sunrise: Date?, sunset: Date?, current: Date?, timeZone: TimeZone): Boolean {
    if (sunrise == null || sunset == null || current == null || sunrise.after(sunset)) return true

    val calendar = Calendar.getInstance(timeZone)

    calendar.time = sunrise
    val sunriseTime = calendar[Calendar.HOUR_OF_DAY] * 60 + calendar[Calendar.MINUTE]

    calendar.time = sunset
    val sunsetTime = calendar[Calendar.HOUR_OF_DAY] * 60 + calendar[Calendar.MINUTE]

    calendar.time = current
    val currentTime = calendar[Calendar.HOUR_OF_DAY] * 60 + calendar[Calendar.MINUTE]

    return currentTime in sunriseTime until sunsetTime
}

fun getCurrentUV(
    context: Context,
    dayMaxUV: Int,
    currentDate: Date?,
    sunriseDate: Date?,
    sunsetDate: Date?,
    timeZone: TimeZone
): UV? {
    if (currentDate == null || sunriseDate == null || sunsetDate == null || sunriseDate.after(sunsetDate))
        return null

    // You can visualize formula here: https://www.desmos.com/calculator/lna7dco4zi
    val calendar = Calendar.getInstance(timeZone)
    calendar.time = currentDate

    val currentTime = calendar[Calendar.HOUR_OF_DAY] + calendar[Calendar.MINUTE] / 60f // Approximating to the minute is enough
    calendar.time = sunriseDate

    val sunRiseTime = calendar[Calendar.HOUR_OF_DAY] + calendar[Calendar.MINUTE] / 60f // b in desmos graph

    calendar.time = sunsetDate
    val sunSetTime =  calendar[Calendar.HOUR_OF_DAY] + calendar[Calendar.MINUTE] / 60f // c in desmos graph

    val sunlightDuration = sunSetTime - sunRiseTime // d in desmos graph
    val sunRiseOffset = -Math.PI.toFloat() * sunRiseTime / sunlightDuration // o in desmos graph
    val currentUV =
        dayMaxUV * sin(Math.PI / sunlightDuration * currentTime + sunRiseOffset) // dayMaxUV = a in desmos graph

    return UV(currentUV.roundToInt(), getUVLevel(context, currentUV.roundToInt()), null)
}

fun getHoursOfDay(sunrise: Date?, sunset: Date?): Float? {
    return if (sunrise == null || sunset == null || sunrise.after(sunset)) {
        null
    } else {
        ((sunset.time - sunrise.time) // get delta millisecond.
                / 1000 // second.
                / 60 // minutes.
                / 60.0 // hours.
                ).toFloat()
    }
}