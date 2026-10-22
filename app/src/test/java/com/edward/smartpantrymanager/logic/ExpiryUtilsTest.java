package com.edward.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

/** Tests the date-only expiry rules independently of the current device date. */
public class ExpiryUtilsTest {

    private Calendar day(int year, int month, int date) {
        Calendar calendar = new GregorianCalendar(year, month - 1, date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    @Test
    public void validDatesRejectImpossibleAndWrongFormatValues() {
        assertTrue(ExpiryUtils.isValidDate("2026-02-28"));
        assertFalse(ExpiryUtils.isValidDate("2026-02-30"));
        assertFalse(ExpiryUtils.isValidDate("2026-2-8"));
        assertFalse(ExpiryUtils.isValidDate("not-a-date"));
    }

    @Test
    public void expiryStatusUsesCalendarDays() {
        Calendar today = day(2026, 10, 22);

        assertEquals(ExpiryUtils.Status.EXPIRED,
                ExpiryUtils.getStatus("2026-10-21", today));
        assertEquals(ExpiryUtils.Status.TODAY,
                ExpiryUtils.getStatus("2026-10-22", today));
        assertEquals(ExpiryUtils.Status.TOMORROW,
                ExpiryUtils.getStatus("2026-10-23", today));
        assertEquals(ExpiryUtils.Status.SOON,
                ExpiryUtils.getStatus("2026-10-25", today));
        assertEquals(ExpiryUtils.Status.FRESH,
                ExpiryUtils.getStatus("2026-10-26", today));
    }
}
