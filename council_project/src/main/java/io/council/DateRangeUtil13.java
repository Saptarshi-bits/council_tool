package io.council;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DateRangeUtil13 {

    public static class DateRange {
        public final LocalDate start;
        public final LocalDate end;

        public DateRange(LocalDate start, LocalDate end) {
            if (start == null || end == null) throw new IllegalArgumentException("Dates cannot be null");
            if (end.isBefore(start)) throw new IllegalArgumentException("End must not be before start");
            this.start = start;
            this.end = end;
        }

        public long lengthInDays() {
            return ChronoUnit.DAYS.between(start, end) + 1;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof DateRange)) return false;
            DateRange other = (DateRange) o;
            return start.equals(other.start) && end.equals(other.end);
        }

        @Override
        public int hashCode() { return Objects.hash(start, end); }

        @Override
        public String toString() { return "[" + start + ", " + end + "]"; }
    }

    private DateRangeUtil13() {}

    public static boolean overlaps(DateRange a, DateRange b) {
        return !a.end.isBefore(b.start) && !b.end.isBefore(a.start);
    }

    public static boolean contains(DateRange outer, DateRange inner) {
        return !inner.start.isBefore(outer.start) && !inner.end.isAfter(outer.end);
    }

    public static boolean containsDate(DateRange range, LocalDate date) {
        return !date.isBefore(range.start) && !date.isAfter(range.end);
    }

    public static DateRange intersection(DateRange a, DateRange b) {
        if (!overlaps(a, b)) return null;
        LocalDate start = a.start.isAfter(b.start) ? a.start : b.start;
        LocalDate end = a.end.isBefore(b.end) ? a.end : b.end;
        return new DateRange(start, end);
    }

    public static DateRange union(DateRange a, DateRange b) {
        if (!overlaps(a, b) && !a.end.plusDays(1).equals(b.start) && !b.end.plusDays(1).equals(a.start)) {
            throw new IllegalArgumentException("Ranges are not adjacent or overlapping");
        }
        LocalDate start = a.start.isBefore(b.start) ? a.start : b.start;
        LocalDate end = a.end.isAfter(b.end) ? a.end : b.end;
        return new DateRange(start, end);
    }

    public static List<DateRange> subtract(DateRange base, DateRange toRemove) {
        List<DateRange> result = new ArrayList<>();
        if (!overlaps(base, toRemove)) {
            result.add(base);
            return result;
        }
        if (base.start.isBefore(toRemove.start)) {
            result.add(new DateRange(base.start, toRemove.start.minusDays(1)));
        }
        if (base.end.isAfter(toRemove.end)) {
            result.add(new DateRange(toRemove.end.plusDays(1), base.end));
        }
        return result;
    }

    public static long countBusinessDays(DateRange range) {
        long count = 0;
        LocalDate current = range.start;
        while (!current.isAfter(range.end)) {
            DayOfWeek dow = current.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) count++;
            current = current.plusDays(1);
        }
        return count;
    }

    public static LocalDate addBusinessDays(LocalDate start, int days) {
        if (days < 0) throw new IllegalArgumentException("Days must be non-negative");
        LocalDate current = start;
        int added = 0;
        while (added < days) {
            current = current.plusDays(1);
            DayOfWeek dow = current.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) added++;
        }
        return current;
    }

    public static List<DateRange> splitByMonth(DateRange range) {
        List<DateRange> parts = new ArrayList<>();
        LocalDate current = range.start;
        while (!current.isAfter(range.end)) {
            LocalDate monthEnd = current.withDayOfMonth(current.lengthOfMonth());
            LocalDate partEnd = monthEnd.isBefore(range.end) ? monthEnd : range.end;
            parts.add(new DateRange(current, partEnd));
            current = partEnd.plusDays(1);
        }
        return parts;
    }
}
