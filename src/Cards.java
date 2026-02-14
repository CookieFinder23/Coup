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

    public Actions getAction() {
        return switch(this) {
            case Cards.DUKE -> Actions.TAX;
            case Cards.ASSASSIN -> Actions.ASSASSINATE;
            case Cards.CAPTAIN -> Actions.STEAL;
            case Cards.AMBASSADOR -> Actions.EXCHANGE;
            default -> throw new IllegalStateException("Unexpected value: " + this);
        };
    }
}