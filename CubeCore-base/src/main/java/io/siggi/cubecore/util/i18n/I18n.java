package io.siggi.cubecore.util.i18n;

import io.siggi.cubecore.util.CubeCoreUtil;
import java.io.File;
import java.io.FileInputStream;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class I18n {
    private I18n() {
    }

    private static final Map<String, String> defaultLocalesByNamespace = new HashMap<>();
    private static String serverDefaultLocale = "en_us";
    private static final Map<String, Map<String, Map<String, String>>> namespaces = new HashMap<>();
    private static final Map<Class<?>, Stringifier<?>> stringifiers = new HashMap<>();
    private static boolean initialized = false;
    private static File languageDirectory;
    private static final Pattern variable = Pattern.compile("%(?:(\\d+)\\$)?(s|d|(?:\\.\\d+)?f)");

    static {
        registerStringifier(Number.class, (locale, valueType, value) -> {
            if (valueType.endsWith("d") || valueType.endsWith("f")) {
                try {
                    return reformatNumber(locale, String.format("%" + valueType, value));
                } catch (Exception e) {
                    return reformatNumber(locale, value.toString());
                }
            } else {
                return reformatNumber(locale, value.toString());
            }
        });
        for (Locale locale : Locale.getAvailableLocales()) {
            String s = locale.toString().replace("-", "_").toLowerCase(Locale.ROOT);
            if (!s.contains("_") || s.length() > 5) continue;
            NumberFormat numberFormat = NumberFormat.getInstance(locale);
            String format = numberFormat.format(12345678.9);
            String thousandsSeparator = format.substring(2, 3);
            if (thousandsSeparator.equals("3")) thousandsSeparator = "";
            String decimalSeparator = format.substring(format.length() - 2, format.length() - 1);
            Map<String, String> map = new HashMap<>();
            map.put("thousandsSeparator", thousandsSeparator);
            map.put("decimalSeparator", decimalSeparator);
            register("numbers", s, map, false);
        }
    }

    private static String reformatNumber(String locale, String number) {
        int dotPosition = number.indexOf(".");
        String thousandsSeparator = i18n(locale, "numbers", "thousandsSeparator");
        if (thousandsSeparator.equals("thousandsSeparator")) thousandsSeparator = ",";
        if (dotPosition == -1) return formatThousands(thousandsSeparator, number);
        String beforeDecimalPoint = number.substring(0, dotPosition);
        String afterDecimalPoint = number.substring(dotPosition + 1);
        String decimalSeparator = i18n(locale, "numbers", "decimalSeparator");
        if (decimalSeparator.equals("decimalSeparator")) decimalSeparator = ".";
        return formatThousands(thousandsSeparator, beforeDecimalPoint) + decimalSeparator + afterDecimalPoint;
    }

    private static String formatThousands(String thousandsSeparator, String number) {
        StringBuilder sb = new StringBuilder();
        for (int i = number.length() % 3; i <= number.length(); i += 3) {
            if (i == 0) continue;
            if (sb.length() != 0) sb.append(thousandsSeparator);
            sb.append(number, Math.max(0, i - 3), i);
        }
        return sb.toString();
    }

    public static void init(File languageDirectory) {
        if (initialized) throw new IllegalStateException("Calling init() after I18n was already initialized.");
        initialized = true;
        I18n.languageDirectory = languageDirectory;
        loadOverrides();
    }

    private static void loadOverrides() {
        File[] namespaceDirectories = languageDirectory.listFiles();
        if (namespaceDirectories == null) return;
        for (File namespaceDirectory : namespaceDirectories) {
            if (!namespaceDirectory.isDirectory()) continue;
            loadOverrides(namespaceDirectory.getName(), namespaceDirectory);
        }
    }

    private static void loadOverrides(String namespace, File directory) {
        for (File file : directory.listFiles()) {
            String name = file.getName();
            int dotPos = name.lastIndexOf(".");
            if (dotPos == -1) continue;
            String locale = name.substring(0, dotPos);
            Map<String, String> mappings;
            try (FileInputStream in = new FileInputStream(file)) {
                mappings = CubeCoreUtil.loadMap(in);
            } catch (Exception e) {
                continue;
            }
            register(namespace, locale, mappings, true);
        }
    }

    public static String getServerDefaultLocale() {
        return serverDefaultLocale;
    }

    public static void setServerDefaultLocale(String locale) {
        serverDefaultLocale = locale.toLowerCase(Locale.ROOT).replace("-", "_");
    }

    public static String getDefaultLocale(String namespace) {
        return defaultLocalesByNamespace.get(namespace);
    }

    public static void setDefaultLocale(String namespace, String locale) {
        defaultLocalesByNamespace.put(namespace.toLowerCase(Locale.ROOT), locale.toLowerCase(Locale.ROOT).replace("-", "_"));
    }

    public static void register(String namespace, String locale, Map<String, String> mappings) {
        register(namespace, locale, mappings, false);
    }

    private static void register(String namespace, String locale, Map<String, String> mappings, boolean overrides) {
        namespace = namespace.toLowerCase(Locale.ROOT);
        locale = locale.toLowerCase(Locale.ROOT).replace("-", "_");
        if (!overrides)
            defaultLocalesByNamespace.putIfAbsent(namespace, locale);
        Map<String, Map<String, String>> namespaceMap = namespaces.computeIfAbsent(namespace, a -> new HashMap<>());
        Map<String, String> languageMap = namespaceMap.computeIfAbsent(locale, a -> new HashMap<>());
        if (overrides) {
            languageMap.putAll(mappings);
        } else {
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                languageMap.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
    }

    public static <T> void registerStringifier(Class<T> clazz, Stringifier<? super T> stringifier) {
        if (clazz == null || stringifier == null) throw new NullPointerException();
        stringifiers.put(clazz, stringifier);
    }

    public static String i18n(String locale, String namespace, String key, Object... values) {
        if (namespace == null || key == null) {
            throw new NullPointerException();
        }
        locale = locale == null ? null : locale.toLowerCase(Locale.ROOT).replace("-", "_");
        namespace = namespace.toLowerCase(Locale.ROOT);
        Map<String, Map<String, String>> namespaceMap = namespaces.get(namespace);
        if (namespaceMap != null) {
            if (locale != null) {
                Map<String, String> localeMap = namespaceMap.get(locale);
                String string = localeMap.get(key);
                if (string != null) {
                    return process(locale, string, values);
                }
            }
            Map<String, String> defaultServerLocaleMap = namespaceMap.get(serverDefaultLocale);
            if (defaultServerLocaleMap != null) {
                String string = defaultServerLocaleMap.get(key);
                if (string != null)
                    return process(serverDefaultLocale, string, values);
            }
            String defaultNamespaceLocale = defaultLocalesByNamespace.getOrDefault(namespace, "en_us");
            if (defaultNamespaceLocale != null) {
                Map<String, String> defaultLocaleMap = namespaceMap.get(defaultNamespaceLocale);
                if (defaultLocaleMap != null) {
                    String string = defaultLocaleMap.get(key);
                    if (string != null) {
                        return process(defaultNamespaceLocale, string, values);
                    }
                }
            }
        }
        if (values == null || values.length == 0) {
            return key;
        }
        StringBuilder sb = new StringBuilder(key);
        int valueCount = 0;
        sb.append("[");
        for (Object value : values) {
            if (valueCount != 0) {
                sb.append(", ");
            }
            valueCount += 1;
            sb.append("\"");
            sb.append(toString(locale, "s", value));
            sb.append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String process(String locale, String string, Object[] values) {
        StringBuilder sb = new StringBuilder();
        int unnumbered = 0;
        int end = 0;
        Matcher matcher = variable.matcher(string);
        while (matcher.find()) {
            int start = matcher.start();
            sb.append(string, end, start);
            end = matcher.end();
            String indexString = matcher.group(1);
            String valueType = matcher.group(2);
            Object value;
            if (indexString != null && !indexString.isEmpty()) {
                int index = Integer.parseInt(indexString) - 1; // -1 since indexes are 1-based and not 0-based
                try {
                    value = values[index];
                } catch (ArrayIndexOutOfBoundsException e) {
                    value = null;
                }
            } else if (unnumbered < values.length) {
                value = values[unnumbered];
                unnumbered += 1;
            } else {
                value = null;
            }
            if (value == null) {
                sb.append(matcher.group(0));
            } else {
                sb.append(toString(locale, valueType, value));
            }
        }
        sb.append(string.substring(end));
        return sb.toString();
    }

    private static String toString(String locale, String valueType, Object value) {
        if (value == null) return "null";
        Class<?> clazz = value.getClass();
        Stringifier stringifier;
        do {
            if (clazz == Object.class) {
                return value.toString();
            }
            stringifier = stringifiers.get(clazz);
            clazz = clazz.getSuperclass();
        } while (stringifier == null);
        return stringifier.stringify(locale, valueType, value);
    }

    @FunctionalInterface
    public interface Stringifier<T> {
        String stringify(String locale, String valueType, T value);
    }
}
