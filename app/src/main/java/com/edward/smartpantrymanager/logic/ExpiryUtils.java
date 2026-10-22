package com.edward.smartpantrymanager.logic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Date-only expiry helpers. The app supports API 24, so this deliberately uses
 * Calendar instead of java.time. Keeping it here also makes date behaviour easy
 * to unit test without depending on the device clock.
 */
public final class ExpiryUtils {

    public enum Status {
        NONE,
        INVALID,
        EXPIRED,
        TODAY,
        TOMORROW,
        SOON,
        FRESH
    }

    private ExpiryUtils() {
        // Utility class.
    }

    public static Status getStatus(String expiryDate) {
        return getStatus(expiryDate, Calendar.getInstance());
    }

    /**
     * Returns an expiry status relative to the supplied day. An item is "soon"
     * when it expires two or three calendar days from now; today and tomorrow are
     * intentionally more specific so the UI can give a helpful prompt.
     */
    public static Status getStatus(String expiryDate, Calendar today) {
        if (expiryDate == null || expiryDate.trim().isEmpty()) return Status.NONE;

        Calendar expiry = parseDate(expiryDate);
        if (expiry == null || today == null) return Status.INVALID;

        int daysUntil = daysBetween(startOfDay(today), expiry);
        if (daysUntil < 0) return Status.EXPIRED;
        if (daysUntil == 0) return Status.TODAY;
        if (daysUntil == 1) return Status.TOMORROW;
        if (daysUntil <= 3) return Status.SOON;
        return Status.FRESH;
    }

    /** True for an expired item or an item due within the next three days. */
    public static boolean isExpiringSoon(String expiryDate) {
        Status status = getStatus(expiryDate);
        return status == Status.EXPIRED || status == Status.TODAY
                || status == Status.TOMORROW || status == Status.SOON;
    }

    /**
     * Validates the yyyy-MM-dd value used in SQLite. SimpleDateFormat is set to
     * non-lenient mode so impossible dates such as 2026-02-30 are rejected.
     */
    public static boolean isValidDate(String expiryDate) {
        return parseDate(expiryDate) != null;
    }

    private static Calendar parseDate(String value) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setLenient(false);
            Date parsed = format.parse(value);
            if (parsed == null || !format.format(parsed).equals(value)) return null;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsed);
            return startOfDay(calendar);
        } catch (ParseException e) {
            return null;
        }
    }

    private static Calendar startOfDay(Calendar source) {
        Calendar day = (Calendar) source.clone();
        day.set(Calendar.HOUR_OF_DAY, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        day.set(Calendar.MILLISECOND, 0);
        return day;
    }

    /**
     * Steps by calendar days instead of dividing milliseconds, which remains
     * correct across daylight-saving changes.
     */
    private static int daysBetween(Calendar start, Calendar end) {
        if (sameDay(start, end)) return 0;

        int direction = start.before(end) ? 1 : -1;
        int days = 0;
        Calendar cursor = (Calendar) start.clone();
        while (!sameDay(cursor, end)) {
            cursor.add(Calendar.DAY_OF_MONTH, direction);
            days += direction;
        }
        return days;
    }

    private static boolean sameDay(Calendar first, Calendar second) {
        return first.get(Calendar.ERA) == second.get(Calendar.ERA)
                && first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR);
    }
}
