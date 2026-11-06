package io.council;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotificationService16 {

    public enum Channel { EMAIL, SMS, PUSH, WEBHOOK }
    public enum Status { PENDING, SENT, FAILED, SKIPPED }

    public static class Notification {
        private final String id;
        private final String recipientId;
        private final Channel channel;
        private final String subject;
        private final String body;
        private Status status;
        private int attemptCount;
        private String errorMessage;

        public Notification(String id, String recipientId, Channel channel, String subject, String body) {
            this.id = id;
            this.recipientId = recipientId;
            this.channel = channel;
            this.subject = subject;
            this.body = body;
            this.status = Status.PENDING;
            this.attemptCount = 0;
        }

        public String getId() { return id; }
        public String getRecipientId() { return recipientId; }
        public Channel getChannel() { return channel; }
        public String getSubject() { return subject; }
        public String getBody() { return body; }
        public Status getStatus() { return status; }
        public int getAttemptCount() { return attemptCount; }
        public String getErrorMessage() { return errorMessage; }

        @Override
        public String toString() {
            return String.format("Notification[%s, to=%s, ch=%s, status=%s]", id, recipientId, channel, status);
        }
    }

    private final int maxRetries;
    private final Set<String> suppressedRecipients;
    private final Map<String, Set<String>> deduplicationKeys;
    private final Map<Channel, Boolean> channelEnabled;
    private final List<Notification> log;

    public NotificationService16(int maxRetries) {
        if (maxRetries < 0) throw new IllegalArgumentException("Max retries cannot be negative");
        this.maxRetries = maxRetries;
        this.suppressedRecipients = new HashSet<>();
        this.deduplicationKeys = new HashMap<>();
        this.channelEnabled = new HashMap<>();
        this.log = new ArrayList<>();
        for (Channel c : Channel.values()) channelEnabled.put(c, true);
    }

    public boolean send(Notification notification, NotificationSender sender) {
        if (notification == null) throw new IllegalArgumentException("Notification cannot be null");

        if (suppressedRecipients.contains(notification.recipientId)) {
            notification.status = Status.SKIPPED;
            log.add(notification);
            return false;
        }

        if (!channelEnabled.getOrDefault(notification.channel, false)) {
            notification.status = Status.SKIPPED;
            log.add(notification);
            return false;
        }

        String dedupKey = notification.recipientId + ":" + notification.subject;
        Set<String> sentKeys = deduplicationKeys.computeIfAbsent(notification.recipientId, k -> new HashSet<>());
        if (sentKeys.contains(dedupKey)) {
            notification.status = Status.SKIPPED;
            log.add(notification);
            return false;
        }

        boolean success = false;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            notification.attemptCount++;
            try {
                sender.send(notification);
                notification.status = Status.SENT;
                sentKeys.add(dedupKey);
                success = true;
                break;
            } catch (Exception e) {
                notification.errorMessage = e.getMessage();
                if (attempt == maxRetries) {
                    notification.status = Status.FAILED;
                }
            }
        }

        log.add(notification);
        return success;
    }

    public void suppressRecipient(String recipientId) {
        suppressedRecipients.add(recipientId);
    }

    public void unsuppressRecipient(String recipientId) {
        suppressedRecipients.remove(recipientId);
    }

    public boolean isRecipientSuppressed(String recipientId) {
        return suppressedRecipients.contains(recipientId);
    }

    public void enableChannel(Channel channel) { channelEnabled.put(channel, true); }
    public void disableChannel(Channel channel) { channelEnabled.put(channel, false); }
    public boolean isChannelEnabled(Channel channel) { return channelEnabled.getOrDefault(channel, false); }

    public List<Notification> getLog() { return new ArrayList<>(log); }

    public long countByStatus(Status status) {
        return log.stream().filter(n -> n.status == status).count();
    }

    public void clearDeduplicationCache(String recipientId) {
        deduplicationKeys.remove(recipientId);
    }

    @FunctionalInterface
    public interface NotificationSender {
        void send(Notification notification) throws Exception;
    }
}
