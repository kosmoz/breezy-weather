package org.breezyweather.common.basic.models.options.provider

import android.content.Context
import androidx.annotation.ColorInt
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.Utils
import org.breezyweather.common.basic.models.options._basic.VoiceEnum

enum class WeatherSource(
    override val id: String,
    @ColorInt val sourceColor: Int,
    val sourceUrl: String,
    val locationProvider: String? = null,
    val airQualityPollenSource: String? = null
): VoiceEnum {

    OPEN_METEO("openmeteo", -0x0077ff, "Open-Meteo.com CC BY 4.0", "Open-Meteo / GeoNames", "Open-Meteo.com, METEO FRANCE, Institut national de l'environnement industriel et des risques (Ineris), Aarhus University, Norwegian Meteorological Institute (MET Norway), Jülich Institut für Energie- und Klimaforschung (IEK), Institute of Environmental Protection – National Research Institute (IEP-NRI), Koninklijk Nederlands Meteorologisch Instituut (KNMI), Nederlandse Organisatie voor toegepast-natuurwetenschappelijk onderzoek (TNO), Swedish Meteorological and Hydrological Institute (SMHI), Finnish Meteorological Institute (FMI), Italian National Agency for New Technologies, Energy and Sustainable Economic Development (ENEA) and Barcelona Supercomputing Center (BSC) (2022): CAMS European air quality forecasts, ENSEMBLE data. Copernicus Atmosphere Monitoring Service (CAMS) Atmosphere Data Store (ADS). (Updated twice daily)."),
    ACCU("accu", -0x10a7dd, "accuweather.com"),
    METNO("metno", -0xdba791, "met.no", "Open-Meteo / GeoNames"),
    OPEN_WEATHER("openweather", -0x1491b5, "openweathermap.org", "Open-Meteo / GeoNames"),
    MF("mf", -0xffa76e, "meteofrance.com", "Open-Meteo / GeoNames"),
    CHINA("china", -0xa14472, "weatherbj, caiyun, CNEMC");

    companion object {

        @JvmStatic
        fun getInstance(
            value: String?
        ): WeatherSource {
            return with (value) {
                when {
                    equals("openmeteo", ignoreCase = true) -> OPEN_METEO
                    equals("accu", ignoreCase = true) -> ACCU
                    equals("metno", ignoreCase = true) -> METNO
                    equals("openweather", ignoreCase = true) -> OPEN_WEATHER
                    equals("mf", ignoreCase = true) -> MF
                    equals("china", ignoreCase = true) -> CHINA
                    else -> ACCU
                }
            }
        }
    }

    override val valueArrayId = R.array.weather_source_values
    override val nameArrayId = R.array.weather_sources
    override val voiceArrayId = R.array.weather_source_voices

    override fun getName(context: Context) = Utils.getName(context, this)
    override fun getVoice(context: Context) = Utils.getVoice(context, this)
}