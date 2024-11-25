package it.fast4x.rimusic.utils



import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import it.fast4x.rimusic.enums.Languages
import me.bush.translator.Language
import org.jetbrains.compose.resources.stringResource
import rimusic.composeapp.generated.resources.Res
import rimusic.composeapp.generated.resources.arabic
import rimusic.composeapp.generated.resources.bashkir
import rimusic.composeapp.generated.resources.catalan
import rimusic.composeapp.generated.resources.chinese_simplified
import rimusic.composeapp.generated.resources.chinese_traditional
import rimusic.composeapp.generated.resources.czech
import rimusic.composeapp.generated.resources.english
import rimusic.composeapp.generated.resources.esperanto
import rimusic.composeapp.generated.resources.french
import rimusic.composeapp.generated.resources.german
import rimusic.composeapp.generated.resources.greek
import rimusic.composeapp.generated.resources.hungarian
import rimusic.composeapp.generated.resources.indonesian
import rimusic.composeapp.generated.resources.italian
import rimusic.composeapp.generated.resources.korean
import rimusic.composeapp.generated.resources.lang_afrikaans
import rimusic.composeapp.generated.resources.lang_bengali
import rimusic.composeapp.generated.resources.lang_danish
import rimusic.composeapp.generated.resources.lang_dutch
import rimusic.composeapp.generated.resources.lang_estonian
import rimusic.composeapp.generated.resources.lang_filipino
import rimusic.composeapp.generated.resources.lang_finnish
import rimusic.composeapp.generated.resources.lang_galician
import rimusic.composeapp.generated.resources.lang_hebrew
import rimusic.composeapp.generated.resources.lang_hindi
import rimusic.composeapp.generated.resources.lang_interlingua
import rimusic.composeapp.generated.resources.lang_irish
import rimusic.composeapp.generated.resources.lang_japanese
import rimusic.composeapp.generated.resources.lang_malayalam
import rimusic.composeapp.generated.resources.lang_norwegian
import rimusic.composeapp.generated.resources.lang_serbian_cyrillic
import rimusic.composeapp.generated.resources.lang_serbian_latin
import rimusic.composeapp.generated.resources.lang_sinhala
import rimusic.composeapp.generated.resources.lang_swedish
import rimusic.composeapp.generated.resources.lang_telugu
import rimusic.composeapp.generated.resources.lang_ukrainian
import rimusic.composeapp.generated.resources.lang_vietnamese
import rimusic.composeapp.generated.resources.odia
import rimusic.composeapp.generated.resources.persian
import rimusic.composeapp.generated.resources.polish
import rimusic.composeapp.generated.resources.portuguese
import rimusic.composeapp.generated.resources.portuguese_brazilian
import rimusic.composeapp.generated.resources.romanian
import rimusic.composeapp.generated.resources.russian
import rimusic.composeapp.generated.resources.spanish
import rimusic.composeapp.generated.resources.system_language
import rimusic.composeapp.generated.resources.turkish


@Composable
fun languageDestination (
    language: Languages? = null,
): Language {
    //val languageApp  by rememberPreference(languageAppKey, Languages.English)
    //val otherLanguageApp  by rememberPreference(otherLanguageAppKey, Languages.English)
    val otherLanguageApp  = remember{Languages.English}
    //TimbeRes.d("LanguageDestination: language $language otherLanguageApp $otherLanguageApp")

    return when (language ?: otherLanguageApp) {
        Languages.Afrikaans -> Language.AFRIKAANS
        Languages.Arabic -> Language.ARABIC
        Languages.Bashkir -> Language.BASQUE
        Languages.Bengali -> Language.BENGALI
        Languages.Catalan -> Language.CATALAN
        Languages.ChineseSimplified -> Language.CHINESE_SIMPLIFIED
        Languages.ChineseTraditional -> Language.CHINESE_TRADITIONAL
        Languages.Czech -> Language.CZECH
        Languages.Danish -> Language.DANISH
        Languages.Dutch -> Language.DUTCH
        Languages.English -> Language.ENGLISH
        Languages.Esperanto -> Language.ESPERANTO
        Languages.Estonian -> Language.ESTONIAN
        Languages.Filipino -> Language.FILIPINO
        Languages.Finnish -> Language.FINNISH
        Languages.French -> Language.FRENCH
        Languages.Galician -> Language.GALICIAN
        Languages.German -> Language.GERMAN
        Languages.Greek -> Language.GREEK
        Languages.Hebrew -> Language.HEBREW_HE
        Languages.Hindi -> Language.HINDI
        Languages.Hungarian -> Language.HUNGARIAN
        Languages.Indonesian -> Language.INDONESIAN
        Languages.Interlingua -> Language.LATIN
        Languages.Irish -> Language.IRISH
        Languages.Japanese -> Language.JAPANESE
        Languages.Korean -> Language.KOREAN
        Languages.Italian -> Language.ITALIAN
        Languages.Malayalam -> Language.MALAYALAM
        Languages.Norwegian -> Language.NORWEGIAN
        Languages.Odia -> Language.ODIA
        Languages.Persian -> Language.PERSIAN
        Languages.Polish -> Language.POLISH
        Languages.PortugueseBrazilian -> Language.PORTUGUESE
        Languages.Portuguese -> Language.PORTUGUESE
        Languages.Romanian -> Language.ROMANIAN
        Languages.Russian -> Language.RUSSIAN
        Languages.SerbianCyrillic, Languages.SerbianLatin -> Language.SERBIAN
        Languages.Sinhala -> Language.SINHALA
        Languages.Spanish -> Language.SPANISH
        Languages.Swedish -> Language.SWEDISH
        Languages.Telugu -> Language.TELUGU
        Languages.Turkish -> Language.TURKISH
        Languages.Ukrainian -> Language.UKRAINIAN
        Languages.Vietnamese -> Language.VIETNAMESE
        else -> Language.ENGLISH
    }
}

@Composable
fun languageDestinationName (
    language: Languages? = null,
): String {
    //val languageApp  by rememberPreference(languageAppKey, Languages.English)
    //val otherLanguageApp  by rememberPreference(otherLanguageAppKey, Languages.English)
    val otherLanguageApp  = remember{Languages.English}
    //TimbeRes.d("LanguageDestination: language $language otherLanguageApp $otherLanguageApp")

    return when (language ?: otherLanguageApp) {
        Languages.System -> stringResource(Res.string.system_language)
        Languages.Afrikaans -> stringResource(Res.string.lang_afrikaans)
        Languages.Arabic -> stringResource(Res.string.arabic)
        Languages.Bashkir -> stringResource(Res.string.bashkir)
        Languages.Bengali -> stringResource(Res.string.lang_bengali)
        Languages.Catalan -> stringResource(Res.string.catalan)
        Languages.ChineseSimplified -> stringResource(Res.string.chinese_simplified)
        Languages.ChineseTraditional -> stringResource(Res.string.chinese_traditional)
        Languages.Czech -> stringResource(Res.string.czech)
        Languages.Danish -> stringResource(Res.string.lang_danish)
        Languages.Dutch -> stringResource(Res.string.lang_dutch)
        Languages.English -> stringResource(Res.string.english)
        Languages.Esperanto -> stringResource(Res.string.esperanto)
        Languages.Estonian -> stringResource(Res.string.lang_estonian)
        Languages.Filipino -> stringResource(Res.string.lang_filipino)
        Languages.Finnish -> stringResource(Res.string.lang_finnish)
        Languages.French -> stringResource(Res.string.french)
        Languages.Galician -> stringResource(Res.string.lang_galician)
        Languages.German -> stringResource(Res.string.german)
        Languages.Greek -> stringResource(Res.string.greek)
        Languages.Hebrew -> stringResource(Res.string.lang_hebrew)
        Languages.Hindi -> stringResource(Res.string.lang_hindi)
        Languages.Hungarian -> stringResource(Res.string.hungarian)
        Languages.Indonesian -> stringResource(Res.string.indonesian)
        Languages.Interlingua -> stringResource(Res.string.lang_interlingua)
        Languages.Irish -> stringResource(Res.string.lang_irish)
        Languages.Japanese -> stringResource(Res.string.lang_japanese)
        Languages.Korean -> stringResource(Res.string.korean)
        Languages.Italian -> stringResource(Res.string.italian)
        Languages.Malayalam -> stringResource(Res.string.lang_malayalam)
        Languages.Norwegian -> stringResource(Res.string.lang_norwegian)
        Languages.Odia -> stringResource(Res.string.odia)
        Languages.Persian -> stringResource(Res.string.persian)
        Languages.Polish -> stringResource(Res.string.polish)
        Languages.PortugueseBrazilian -> stringResource(Res.string.portuguese_brazilian)
        Languages.Portuguese -> stringResource(Res.string.portuguese)
        Languages.Romanian -> stringResource(Res.string.romanian)
        //Languages.RomanianEmo -> stringResource(Res.string.romanian_emoticons_rom_n)
        Languages.Russian -> stringResource(Res.string.russian)
        Languages.SerbianCyrillic -> stringResource(Res.string.lang_serbian_cyrillic)
        Languages.SerbianLatin -> stringResource(Res.string.lang_serbian_latin)
        Languages.Sinhala -> stringResource(Res.string.lang_sinhala)
        Languages.Spanish -> stringResource(Res.string.spanish)
        Languages.Swedish -> stringResource(Res.string.lang_swedish)
        Languages.Telugu -> stringResource(Res.string.lang_telugu)
        Languages.Turkish -> stringResource(Res.string.turkish)
        Languages.Ukrainian -> stringResource(Res.string.lang_ukrainian)
        Languages.Vietnamese -> stringResource(Res.string.lang_vietnamese)
    }
}