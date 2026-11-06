package io.council;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class HotelReservation09 {

    public enum RoomType { STANDARD, DELUXE, SUITE, PENTHOUSE }
    public enum Status { PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED }

    private final String reservationId;
    private final String guestName;
    private final RoomType roomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Status status;
    private int numberOfGuests;
    private boolean breakfastIncluded;
    private double totalPrice;
    private double depositPaid;

    private static final double[] BASE_RATES = { 80.0, 130.0, 220.0, 400.0 };
    private static final double BREAKFAST_RATE = 15.0;
    private static final double CANCELLATION_FEE_PERCENT = 20.0;

    public HotelReservation09(String reservationId, String guestName, RoomType roomType,
                               LocalDate checkIn, LocalDate checkOut, int numberOfGuests) {
        if (reservationId == null || reservationId.isBlank())
            throw new IllegalArgumentException("Reservation ID required");
        if (guestName == null || guestName.isBlank())
            throw new IllegalArgumentException("Guest name required");
        if (checkIn == null || checkOut == null)
            throw new IllegalArgumentException("Dates cannot be null");
        if (!checkOut.isAfter(checkIn))
            throw new IllegalArgumentException("Check-out must be after check-in");
        if (numberOfGuests < 1)
            throw new IllegalArgumentException("At least one guest required");

        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomType = roomType;
        this.checkInDate = checkIn;
        this.checkOutDate = checkOut;
        this.numberOfGuests = numberOfGuests;
        this.status = Status.PENDING;
        this.breakfastIncluded = false;
        this.depositPaid = 0.0;
        this.totalPrice = calculatePrice();
    }

    private double calculatePrice() {
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        double rate = BASE_RATES[roomType.ordinal()];
        double price = nights * rate;
        if (breakfastIncluded) price += nights * numberOfGuests * BREAKFAST_RATE;
        return price;
    }

    public void confirm() {
        if (status != Status.PENDING) throw new IllegalStateException("Only pending reservations can be confirmed");
        status = Status.CONFIRMED;
    }

    public void checkIn() {
        if (status != Status.CONFIRMED) throw new IllegalStateException("Reservation must be confirmed before check-in");
        if (LocalDate.now().isBefore(checkInDate))
            throw new IllegalStateException("Cannot check in before check-in date");
        status = Status.CHECKED_IN;
    }

    public void checkOut() {
        if (status != Status.CHECKED_IN) throw new IllegalStateException("Guest must be checked in to check out");
        status = Status.CHECKED_OUT;
    }

    public double cancel() {
        if (status == Status.CHECKED_IN || status == Status.CHECKED_OUT)
            throw new IllegalStateException("Cannot cancel after check-in");
        if (status == Status.CANCELLED) return 0.0;
        status = Status.CANCELLED;
        double fee = totalPrice * CANCELLATION_FEE_PERCENT / 100.0;
        double refund = depositPaid - fee;
        return Math.max(0.0, refund);
    }

    public void addBreakfast() {
        if (status == Status.CHECKED_OUT || status == Status.CANCELLED)
            throw new IllegalStateException("Cannot modify completed or cancelled reservation");
        this.breakfastIncluded = true;
        this.totalPrice = calculatePrice();
    }

    public void removeBreakfast() {
        if (status == Status.CHECKED_OUT || status == Status.CANCELLED)
            throw new IllegalStateException("Cannot modify completed or cancelled reservation");
        this.breakfastIncluded = false;
        this.totalPrice = calculatePrice();
    }

    public void payDeposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive");
        if (status == Status.CANCELLED || status == Status.CHECKED_OUT)
            throw new IllegalStateException("Cannot pay deposit on cancelled or completed reservation");
        this.depositPaid += amount;
    }

    public double getRemainingBalance() {
        return Math.max(0.0, totalPrice - depositPaid);
    }

    public long getNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public boolean extendStay(LocalDate newCheckOut) {
        if (status != Status.CHECKED_IN) return false;
        if (!newCheckOut.isAfter(checkOutDate)) return false;
        this.checkOutDate = newCheckOut;
        this.totalPrice = calculatePrice();
        return true;
    }

    public String getReservationId() { return reservationId; }
    public String getGuestName() { return guestName; }
    public RoomType getRoomType() { return roomType; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public Status getStatus() { return status; }
    public int getNumberOfGuests() { return numberOfGuests; }
    public boolean isBreakfastIncluded() { return breakfastIncluded; }
    public double getTotalPrice() { return totalPrice; }
    public double getDepositPaid() { return depositPaid; }

    @Override
    public String toString() {
        return String.format("HotelReservation09[%s, %s, %s, %s to %s, status=%s, total=%.2f]",
                reservationId, guestName, roomType, checkInDate, checkOutDate, status, totalPrice);
    }
}
