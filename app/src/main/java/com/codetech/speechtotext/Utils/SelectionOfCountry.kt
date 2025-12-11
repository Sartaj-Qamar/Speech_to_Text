package com.codetech.speechtotext.Utils

object SelectionOfCountry {
    val countries = listOf(
        Country("USA"),
        Country("Canada"),
        Country("Mexico"),
        Country("France"),
        Country("Germany"),
        Country("Spain"),
        Country("Italy"),
        Country("India"),
        Country("China"),
        Country("Japan"),
        Country("Russia"),
        Country("Brazil"),
        Country("Argentina"),
        Country("Australia"),
        Country("South Africa"),
        Country("Saudi Arabia"),
        Country("United Kingdom"),
        Country("South Korea"),
        Country("Egypt"),
        Country("Turkey")
    )

    fun getLanguageCode(countryName: String): String {
        return when (countryName.trim()) {
            "USA" -> "en-US"
            "Canada" -> "fr-CA"
            "Mexico" -> "es-MX"
            "France" -> "fr-FR"
            "Germany" -> "de-DE"
            "Spain" -> "es-ES"
            "Italy" -> "it-IT"
            "India" -> "hi-IN"
            "China" -> "zh-CN"
            "Japan" -> "ja-JP"
            "Russia" -> "ru-RU"
            "Brazil" -> "pt-BR"
            "Argentina" -> "es-AR"
            "Australia" -> "en-AU"
            "South Africa" -> "en-ZA"
            "Saudi Arabia" -> "ar-SA"
            "United Kingdom" -> "en-GB"
            "South Korea" -> "ko-KR"
            "Egypt" -> "ar-EG"
            "Turkey" -> "tr-TR"
            "Urdu" -> "ur-PK"
            else -> "en-US" // Default to US English if country not found
        }
    }

    val languageCodeToCountryMap = mapOf(
        "en-US" to "United States",
        "hi-IN" to "India",
        "fr-CA" to "Canada",
        "es-MX" to "Mexico",
        "ur-PK" to "Pakistan",
        "fr-FR" to "France",
        "de-DE" to "Germany",
        "es-ES" to "Spain",
        "it-IT" to "Italy",
        "zh-CN" to "China",
        "ja-JP" to "Japan",
        "ru-RU" to "Russia",
        "pt-BR" to "Brazil",
        "es-AR" to "Argentina",
        "en-AU" to "Australia",
        "en-ZA" to "South Africa",
        "ar-SA" to "Saudi Arabia",
        "en-GB" to "United Kingdom",
        "ko-KR" to "South Korea",
        "ar-EG" to "Egypt",
        "tr-TR" to "Turkey"
    )


}



