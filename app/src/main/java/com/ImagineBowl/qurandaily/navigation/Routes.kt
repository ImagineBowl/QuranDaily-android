package com.imaginebowl.qurandaily.navigation

object Routes {
    const val LISTEN_HOME = "listen_home"
    const val SURAH_LIST = "surah_list"

    fun surahRead(surahNumber: Int, ayahNumber: Int, autoPlay: Boolean): String =
        "surah_read/$surahNumber/$ayahNumber/$autoPlay"

    const val SURAH_READ_PATTERN = "surah_read/{surahNumber}/{ayahNumber}/{autoPlay}"

    fun parseAutoPlay(value: String): Boolean = value == "true"
}
