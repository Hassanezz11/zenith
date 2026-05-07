package atlantafx.sampler.zenith;

import java.util.ArrayList;
import java.util.List;

final class Conversation {

    private final Joueur participant;
    private final List<Message> messages;

    Conversation(Joueur participant, List<Message> messages) {
        this.participant = participant;
        this.messages = new ArrayList<>(messages);
    }

    Joueur getParticipant() {
        return participant;
    }

    List<Message> getMessages() {
        return messages;
    }

    Message getLastMessage() {
        if (messages.isEmpty()) {
            return null;
        }

        return messages.get(messages.size() - 1);
    }
}
