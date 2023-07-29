package org.breezyweather.common.basic.wrappers

import org.breezyweather.common.basic.models.weather.Alert
import org.breezyweather.common.basic.models.weather.Base
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.History
import org.breezyweather.common.basic.models.weather.Minutely

/**
 * Wrapper to help with secondary data.
 */
data class SecondaryWeatherWrapper(
    val airQuality: AirQualityWrapper? = null,
    val allergen: AllergenWrapper? = null,
    val minutelyForecast: List<Minutely>? = null,
    val alertList: List<Alert>? = null
)