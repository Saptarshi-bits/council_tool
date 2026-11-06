package io.council;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Deque;

public class LibraryBook08 {

    public enum Status { AVAILABLE, BORROWED, RESERVED, LOST, MAINTENANCE }

    private final String isbn;
    private final String title;
    private final String author;
    private Status status;
    private String currentBorrowerId;
    private LocalDate dueDate;
    private int totalBorrows;
    private final Deque<String> reservationQueue;
    private static final int LOAN_DAYS = 14;
    private static final double DAILY_FINE = 0.25;

    public LibraryBook08(String isbn, String title, String author) {
        if (isbn == null || isbn.isBlank()) throw new IllegalArgumentException("ISBN cannot be blank");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title cannot be blank");
        if (author == null || author.isBlank()) throw new IllegalArgumentException("Author cannot be blank");
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.status = Status.AVAILABLE;
        this.totalBorrows = 0;
        this.reservationQueue = new ArrayDeque<>();
    }

    public boolean checkOut(String borrowerId) {
        if (borrowerId == null || borrowerId.isBlank()) throw new IllegalArgumentException("Borrower ID required");
        if (status != Status.AVAILABLE) return false;
        if (!reservationQueue.isEmpty() && !reservationQueue.peek().equals(borrowerId)) return false;
        if (!reservationQueue.isEmpty()) reservationQueue.poll();
        this.currentBorrowerId = borrowerId;
        this.dueDate = LocalDate.now().plusDays(LOAN_DAYS);
        this.status = Status.BORROWED;
        this.totalBorrows++;
        return true;
    }

    public boolean returnBook() {
        if (status != Status.BORROWED) return false;
        this.currentBorrowerId = null;
        this.dueDate = null;
        this.status = reservationQueue.isEmpty() ? Status.AVAILABLE : Status.RESERVED;
        return true;
    }

    public boolean reserve(String patronId) {
        if (patronId == null || patronId.isBlank()) throw new IllegalArgumentException("Patron ID required");
        if (status == Status.LOST || status == Status.MAINTENANCE) return false;
        if (patronId.equals(currentBorrowerId)) return false;
        if (reservationQueue.contains(patronId)) return false;
        reservationQueue.add(patronId);
        if (status == Status.AVAILABLE) status = Status.RESERVED;
        return true;
    }

    public boolean cancelReservation(String patronId) {
        boolean removed = reservationQueue.remove(patronId);
        if (removed && reservationQueue.isEmpty() && status == Status.RESERVED) {
            status = Status.AVAILABLE;
        }
        return removed;
    }

    public double calculateFine(LocalDate returnDate) {
        if (dueDate == null || returnDate == null) return 0.0;
        long overdueDays = returnDate.toEpochDay() - dueDate.toEpochDay();
        return overdueDays > 0 ? overdueDays * DAILY_FINE : 0.0;
    }

    public boolean isOverdue() {
        return status == Status.BORROWED && dueDate != null && LocalDate.now().isAfter(dueDate);
    }

    public void markLost() {
        this.status = Status.LOST;
        this.currentBorrowerId = null;
        this.dueDate = null;
    }

    public void sendToMaintenance() {
        if (status == Status.BORROWED) throw new IllegalStateException("Cannot send borrowed book to maintenance");
        this.status = Status.MAINTENANCE;
    }

    public void returnFromMaintenance() {
        if (status != Status.MAINTENANCE) throw new IllegalStateException("Book is not in maintenance");
        this.status = reservationQueue.isEmpty() ? Status.AVAILABLE : Status.RESERVED;
    }

    public int getQueueSize() { return reservationQueue.size(); }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public Status getStatus() { return status; }
    public String getCurrentBorrowerId() { return currentBorrowerId; }
    public LocalDate getDueDate() { return dueDate; }
    public int getTotalBorrows() { return totalBorrows; }

    @Override
    public String toString() {
        return String.format("LibraryBook08[%s, '%s' by %s, status=%s]", isbn, title, author, status);
    }
}
