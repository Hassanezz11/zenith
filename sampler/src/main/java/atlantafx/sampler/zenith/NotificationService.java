package atlantafx.sampler.zenith;

import atlantafx.sampler.zenith.dao.FriendRequestDAO;
import atlantafx.sampler.zenith.dao.MessageDAO;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

class NotificationService {

    private static final NotificationService INSTANCE = new NotificationService();

    private final IntegerProperty unreadMessages  = new SimpleIntegerProperty(0);
    private final IntegerProperty pendingRequests = new SimpleIntegerProperty(0);

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> task;
    private int watchedUserId = -1;

    private NotificationService() {}

    static NotificationService getInstance() { return INSTANCE; }

    IntegerProperty unreadMessagesProperty()  { return unreadMessages; }
    IntegerProperty pendingRequestsProperty() { return pendingRequests; }

    int getTotalBadge() { return unreadMessages.get() + pendingRequests.get(); }

    void start(int userId) {
        if (userId <= 0) return;
        if (watchedUserId == userId) return;   // already running for this user
        stop();
        watchedUserId = userId;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "zenith-notif-poller");
            t.setDaemon(true);
            return t;
        });
        task = scheduler.scheduleAtFixedRate(() -> poll(userId), 0, 3, TimeUnit.SECONDS);
    }

    void stop() {
        watchedUserId = -1;
        if (task != null)      { task.cancel(true); task = null; }
        if (scheduler != null) { scheduler.shutdownNow(); scheduler = null; }
    }

    void refresh() {
        if (watchedUserId > 0) poll(watchedUserId);
    }

    private void poll(int userId) {
        try {
            int msgs = MessageDAO.getUnreadCount(userId);
            int reqs = FriendRequestDAO.getPendingCount(userId);
            Platform.runLater(() -> {
                unreadMessages.set(msgs);
                pendingRequests.set(reqs);
            });
        } catch (SQLException ignored) {}
    }
}