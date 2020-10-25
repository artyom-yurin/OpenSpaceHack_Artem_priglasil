package models;

public enum ConversationState {
    Init {
        @Override
        public String toString() {
            return "init";
        }
    },
    Clarification {
        @Override
        public String toString() {
            return "clarification";
        }
    },
    Answered {
        @Override
        public String toString() {
            return "answered";
        }
    };

    public static ConversationState fromString(String str_state) throws IllegalArgumentException {
        switch (str_state) {
            case ("init"):
                return Init;
            case ("clarification"):
                return Clarification;
            case ("answered"):
                return Answered;
            default:
                throw new IllegalArgumentException("Unknown state");
        }
    }

    public abstract String toString();
}