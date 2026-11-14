package cn.gcte.xts.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse time expressions from command arguments.
 *
 * <p>Supports two forms:
 * <ul>
 *   <li>relative: "5s", "1m", "1m5s", "1h18m5s", "1d"</li>
 *   <li>absolute: "at &lt;date&gt; &lt;time&gt;" where date/time parts are optional</li>
 * </ul>
 */
public final class TimeParser {

    public static final Pattern RELATIVE = Pattern.compile(
            "^(\\d+d)?(\\d+h)?(\\d+m)?(\\d+s)?$",
            Pattern.CASE_INSENSITIVE
    );

    /** date without time, e.g. 2026-01-01 */
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** time without seconds, e.g. 10:13 */
    public static final DateTimeFormatter TIME_SHORT = DateTimeFormatter.ofPattern("HH:mm");
    /** time with seconds, e.g. 10:13:15 */
    public static final DateTimeFormatter TIME_LONG = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private TimeParser() {}

    /** Thrown when a time expression cannot be parsed. */
    @Contract("_ -> new")
    public static @NotNull CommandSyntaxException badSyntax(String raw) {
        return new CommandSyntaxException(
                null,
                Component.literal("Invalid time: " + raw
                        + ". Expected e.g. '5s', '1m5s', 'in 1h', 'at 2026-01-01 10:13:15'")
        );
    }

    /**
     * Parse a relative duration expression.
     *
     * @param raw raw token, e.g. "5s", "1m", "1h18m5s"
     * @return duration from now
     * @throws CommandSyntaxException if the format is invalid
     */
    @Contract("null -> fail")
    public static @NotNull Duration parseRelative(String raw) throws CommandSyntaxException {
        if (raw == null || raw.isEmpty()) {
            throw badSyntax("");
        }
        Matcher m = RELATIVE.matcher(raw);
        if (!m.matches() || raw.contains("--")) {
            throw badSyntax(raw);
        }
        Duration d = Duration.ZERO;
        try {
            if (m.group(1) != null) d = d.plusDays(parseDigits(m.group(1)));
            if (m.group(2) != null) d = d.plusHours(parseDigits(m.group(2)));
            if (m.group(3) != null) d = d.plusMinutes(parseDigits(m.group(3)));
            if (m.group(4) != null) d = d.plusSeconds(parseDigits(m.group(4)));
        } catch (NumberFormatException e) {
            throw badSyntax(raw);
        }
        if (d.isZero() || d.isNegative()) {
            throw badSyntax(raw);
        }
        return d;
    }

    private static long parseDigits(@NotNull String s) {
        // strip trailing unit letter
        String num = s.substring(0, s.length() - 1);
        return Long.parseLong(num);
    }

    /**
     * Parse an absolute target time from date + time tokens.
     * dateToken / timeToken may be null or empty to mean "not specified".
     *
     * <p>Resolution rules:
     * <ul>
     *   <li>date only: time = 00:00:00 of that day</li>
     *   <li>time only: date = today; if that instant is already past, use tomorrow</li>
     *   <li>date + time: exact instant</li>
     * </ul>
     *
     * @return duration from now; always &gt;= 0 (clamped to zero if in the past)
     */
    public static Duration parseAbsolute(String dateToken, String timeToken)
            throws CommandSyntaxException {

        LocalDate date = null;
        LocalTime time = null;

        if (dateToken != null && !dateToken.isEmpty()) {
            date = parseDate(dateToken);
        }
        if (timeToken != null && !timeToken.isEmpty()) {
            time = parseTime(timeToken);
        }

        LocalDateTime now = LocalDateTime.now(ZONE);
        LocalDateTime target;

        if (date != null && time != null) {
            target = LocalDateTime.of(date, time);
        } else if (date != null) {
            target = LocalDateTime.of(date, LocalTime.MIDNIGHT);
        } else if (time != null) {
            LocalDateTime today = LocalDateTime.of(now.toLocalDate(), time);
            target = today.isBefore(now) ? today.plusDays(1) : today;
        } else {
            throw new CommandSyntaxException(
                    null,
                    Component.literal("Usage: /restart at <yyyy-MM-dd> [HH:mm[:ss]]")
            );
        }

        Duration d = Duration.between(now, target);
        return d.isNegative() ? Duration.ZERO : d;
    }

    private static @NotNull LocalDate parseDate(String s) throws CommandSyntaxException {
        try {
            return LocalDate.parse(s, DATE);
        } catch (DateTimeParseException e) {
            throw new CommandSyntaxException(
                    null,
                    Component.literal("Invalid date: " + s + ". Expected yyyy-MM-dd")
            );
        }
    }

    private static @NotNull LocalTime parseTime(@NotNull String s) throws CommandSyntaxException {
        try {
            if (s.length() == 5) { // HH:mm
                return LocalTime.parse(s, TIME_SHORT);
            }
            if (s.length() == 8) { // HH:mm:ss
                return LocalTime.parse(s, TIME_LONG);
            }
        } catch (DateTimeParseException ignored) {
            // fall through to error
        }
        throw new CommandSyntaxException(
                null,
                Component.literal("Invalid time: " + s + ". Expected HH:mm or HH:mm:ss")
        );
    }

    /** Convert duration to ticks. */
    public static long toTicks(@NotNull Duration d, long mspt) {
        return Math.max(0L, d.toMillis() / mspt);
    }

    /** Human-readable delay, e.g. "1h 18m 5s". */
    public static @NotNull String formatDelay(@NotNull Duration d) {
        long total = d.getSeconds();
        if (total <= 0) return "now";
        long days = total / 86400;
        long hours = (total % 86400) / 3600;
        long mins = (total % 3600) / 60;
        long secs = total % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (mins > 0) sb.append(mins).append("m ");
        if (secs > 0 || sb.isEmpty()) sb.append(secs).append("s");
        return sb.toString().trim();
    }

    /** Current instant in the server's timezone, used for marker / logging. */
    public static Instant now() {
        return Instant.now();
    }

    /** Formats an Instant for chat/log output, e.g. "2026-01-01 00:13:15". */
    public static @NotNull String formatInstant(Instant instant) {
        if (instant == null) return "n/a";
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }
}