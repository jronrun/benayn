package com.benayn.ustyle.thirdparty;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.Locale;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

/**
 * @see https://github.com/toonetown/guava-ext
 */
public enum Language implements EnumLookup.Keyed<Locale> {
    ARABIC           (Locale.forLanguageTag("ar")),
    BULGARIAN        (Locale.forLanguageTag("bg")),
    CATALAN          (Locale.forLanguageTag("ca")),
    CHINESE_SIMP     (Locale.forLanguageTag("zh-Hans")),
    CHINESE_TRAD     (Locale.forLanguageTag("zh-Hant")),
    CROATIAN         (Locale.forLanguageTag("hr")),
    CZECH_REPUBLIC   (Locale.forLanguageTag("cs")),
    DANISH           (Locale.forLanguageTag("da")),
    DUTCH            (Locale.forLanguageTag("nl")),
    ENGLISH          (Locale.forLanguageTag("en")),
    ESTONIAN         (Locale.forLanguageTag("et")),
    FILIPINO         (Locale.forLanguageTag("tl")),
    FINNISH          (Locale.forLanguageTag("fi")),
    FRENCH           (Locale.forLanguageTag("fr")),
    GERMAN           (Locale.forLanguageTag("de")),
    GREEK            (Locale.forLanguageTag("el")),
    HATIAN_CREOLE    (Locale.forLanguageTag("ht")),
    HEBREW           (Locale.forLanguageTag("he")),
    HINDI            (Locale.forLanguageTag("hi")),
    HUNGARIAN        (Locale.forLanguageTag("hu")),
    INDONESIAN       (Locale.forLanguageTag("id")),
    ITALIAN          (Locale.forLanguageTag("it")),
    JAPANESE         (Locale.forLanguageTag("ja")),
    KOREAN           (Locale.forLanguageTag("ko")),
    LATVIAN          (Locale.forLanguageTag("lv")),
    LITHUANIAN       (Locale.forLanguageTag("lt")),
    MALAYSIAN        (Locale.forLanguageTag("ms")),
    MALTESE          (Locale.forLanguageTag("mt")),
    NORWEGIAN        (Locale.forLanguageTag("no")),
    PERSIAN          (Locale.forLanguageTag("fa")),
    POLISH           (Locale.forLanguageTag("pl")),
    PORTUGUESE       (Locale.forLanguageTag("pt")),
    ROMANIAN         (Locale.forLanguageTag("ro")),
    RUSSIAN          (Locale.forLanguageTag("ru")),
    SERBIAN          (Locale.forLanguageTag("sr")),
    SLOVAK           (Locale.forLanguageTag("sk")),
    SLOVENIAN        (Locale.forLanguageTag("sl")),
    SPANISH          (Locale.forLanguageTag("es")),
    SWEDISH          (Locale.forLanguageTag("sv")),
    THAI             (Locale.forLanguageTag("th")),
    TURKISH          (Locale.forLanguageTag("tr")),
    UKRANIAN         (Locale.forLanguageTag("uk")),
    URDU             (Locale.forLanguageTag("ur")),
    VIETNAMESE       (Locale.forLanguageTag("vi")),
    UNKNOWN          (Locale.ROOT);

    private final Locale value;
    
    private Language(Locale value) {
    	this.value = value;
    }

    /* Mapping of countries to scripts for when they are missing */
    private static final ImmutableMap<String, String> COUNTRY_SCRIPTS = ImmutableMap.of(
            "CN", "Hans",
            "TW", "Hant"
    );
    /** Returns the tag mapping to zh-CN and zh-TW if chinaCountry is true */
    private static final ImmutableMap<Language, String> COUNTRY_MAP = ImmutableMap.of(
            CHINESE_SIMP, "zh-CN",
            CHINESE_TRAD, "zh-TW"
    );

    /**
     * Returns the language tag (with an optional map of languages).  UNKNOWN returns an empty string
     */
    public String getLanguageTag(final ImmutableMap<Language, String> tagMap) {
        if (this == UNKNOWN) { return ""; }
        if (tagMap != null && tagMap.containsKey(this)) { return tagMap.get(this); }
        return getValue().toLanguageTag();
    }
    public String getLanguageTag(final boolean chinaCountry) {
        return getLanguageTag(chinaCountry ? COUNTRY_MAP : null);
    }
    public String getLanguageTag() { return getLanguageTag(false); }


    private static final EnumLookup<Language, Locale> $ALL = EnumLookup.of(Language.class);

    private static Language tryFind(final Locale l) {
        final Locale.Builder b = new Locale.Builder().setLanguage(l.getLanguage()).setScript(l.getScript());
        if (Strings.isNullOrEmpty(l.getScript())) {
            b.setScript(COUNTRY_SCRIPTS.get(l.getCountry().toUpperCase()));
        }
        final Language f = $ALL.find(b.build());
        return f == UNKNOWN ? null : f;
    }

    /** Finds a single language by locale - only the language (and script, if specified) is used to look up */
    public static Language find(final Locale l) {
        if (Locale.ROOT.equals(l)) {
            /* We only return unknown if we are exactly an empty locale */
            return UNKNOWN;
        }
        
        Language language = tryFind(l);
        if (null == language && !Strings.isNullOrEmpty(l.getScript())) {
            /* Let's try and fetch it with just the language and country itself */
            language = tryFind(new Locale(l.getLanguage(), l.getCountry()));
        }
       
        return language;
    }
    public static Language find(final Locale l, final Language defaultVal) {
    	return firstNonNull(find(l), defaultVal);
    }

    /* Find by string */
    public static Language find(final String s) {
        return find(Locale.forLanguageTag(s));
    }
    public static Language find(final String s, final Language defaultVal) {
        return find(Locale.forLanguageTag(s), defaultVal);
    }

    public static Set<Language> all() { return $ALL.keySet(); }
	
	@Override public Locale getValue() {
		return value;
	}
	
}
