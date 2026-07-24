package app.kreate.utils

import java.util.Locale


actual fun getSystemCountryCode(): String = Locale.getDefault().country

actual fun getSystemLanguageCode(): String = Locale.getDefault().language