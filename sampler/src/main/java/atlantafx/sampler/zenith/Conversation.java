package atlantafx.sampler.zenith;

import java.util.ArrayList;
import java.util.List;

final class Conversation {

    private final Joueur participant;
    private final List<Message> messages;
    private int unreadCount;

    Conversation(Joueur participant, List<Message> messages) {
        this.participant = participant;
        this.messages = new ArrayList<>(messages);
    }

    Joueur getParticipant() { return participant; }

    List<Message> getMessages() { return messages; }

    int getUnreadCount() { return unreadCount; }

    void setUnreadCount(int count) { this.unreadCount = count; }

    Message getLastMessage() {
        return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }
}
