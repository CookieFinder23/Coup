public enum Cards {
    DUKE,
    ASSASSIN,
    AMBASSADOR,
    CAPTAIN,
    CONTESSA;

    public String toString() {
        return switch(this) {
            case Cards.DUKE -> "Duke";
            case Cards.ASSASSIN -> "Assassin";
            case Cards.AMBASSADOR -> "Ambassador";
            case Cards.CAPTAIN -> "Captain";
            case Cards.CONTESSA -> "Contessa";
        };
    }
}