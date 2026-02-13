public class Zones {
    public static int getZone(GlobalZones zone) {
        if (zone == GlobalZones.DECK)
            return -1;
        else
            return -2;
    }

    public static int getZone(Player player) {
        return player.getPositionInTurnOrder();
    }

    /*
    THE STUPID ZONE SYSTEM:
    This class acts as a DIY enum.
    Each card has an int field called zone
    By convention, a card's zone can only be changed / viewed / compared using the getZone() method
     */
}
