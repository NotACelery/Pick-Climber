package dev.maicra.pickclimber.rules;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class RuleBookNamePolicy {
    private static final Set<String> WINDOWS_RESERVED = Set.of(
            "con", "prn", "aux", "nul",
            "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9",
            "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9"
    );

    private RuleBookNamePolicy() {
    }

    public static Optional<String> portableFileStem(String bookName) {
        if (bookName == null) {
            return Optional.empty();
        }
        String name = bookName.trim();
        if (name.isEmpty() || name.length() > ClimbingRuleBookDefinition.MAX_BOOK_NAME_LENGTH) {
            return Optional.empty();
        }
        if (!name.equals(bookName) || name.equals(".") || name.equals("..")) {
            return Optional.empty();
        }
        if (name.endsWith(".") || containsForbiddenCharacter(name) || isReservedDeviceName(name)) {
            return Optional.empty();
        }
        return Optional.of(name);
    }

    public static String normalizeImportedFileStem(String fileName) {
        if (fileName == null) {
            return "";
        }
        String name = stripJsonExtension(fileName.trim());
        StringBuilder normalized = new StringBuilder(name.length());
        for (int index = 0; index < name.length(); index++) {
            char character = name.charAt(index);
            normalized.append(isForbiddenCharacter(character) ? '_' : character);
        }

        String result = normalized.toString()
                .replaceAll("\\.{2,}", ".")
                .replaceAll("^[._ ]+|[. ]+$", "");
        if (result.length() > ClimbingRuleBookDefinition.MAX_BOOK_NAME_LENGTH) {
            result = result.substring(0, ClimbingRuleBookDefinition.MAX_BOOK_NAME_LENGTH).stripTrailing();
        }
        return portableFileStem(result).orElse("");
    }

    private static String stripJsonExtension(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".rules.json")) {
            return name.substring(0, name.length() - ".rules.json".length()).trim();
        }
        if (lower.endsWith(".json")) {
            return name.substring(0, name.length() - ".json".length()).trim();
        }
        return name;
    }

    private static boolean containsForbiddenCharacter(String name) {
        for (int index = 0; index < name.length(); index++) {
            if (isForbiddenCharacter(name.charAt(index))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isForbiddenCharacter(char character) {
        return character < 32
                || character == '\\'
                || character == '/'
                || character == ':'
                || character == '*'
                || character == '?'
                || character == '"'
                || character == '<'
                || character == '>'
                || character == '|';
    }

    private static boolean isReservedDeviceName(String name) {
        String lowerName = name.toLowerCase(Locale.ROOT);
        int firstDot = lowerName.indexOf('.');
        String deviceName = firstDot >= 0 ? lowerName.substring(0, firstDot) : lowerName;
        return WINDOWS_RESERVED.contains(deviceName);
    }
}
