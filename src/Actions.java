public enum Actions {
    INCOME,
    FOREIGN_AID,
    COUP,
    TAX,
    ASSASSINATE,
    EXCHANGE,
    STEAL;

    public String toString() {
        return switch(this) {
            case Actions.INCOME -> "Income";
            case Actions.FOREIGN_AID -> "Foreign Aid";
            case Actions.COUP -> "Coup";
            case Actions.TAX -> "Tax";
            case Actions.ASSASSINATE -> "Assassinate";
            case Actions.EXCHANGE -> "Exchange";
            case Actions.STEAL -> "Steal";
        };
    }

    public Cards getCard() {
        return switch(this) {
            case Actions.TAX -> Cards.DUKE;
            case Actions.ASSASSINATE -> Cards.ASSASSIN;
            case Actions.EXCHANGE -> Cards.AMBASSADOR;
            case Actions.STEAL -> Cards.CAPTAIN;
            default -> null;
        };
    }
}
