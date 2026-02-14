public class Card {
    private final Cards name;
    private int zone;

    public Card(Cards name) {
        this.name = name;
        this.zone = Zones.getZone(GlobalZones.DECK);
    }

    public Cards getName() {
        return name;
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    public int getZone() {
        return zone;
    }

    public String toString() {
        return name.toString();
    }




}
