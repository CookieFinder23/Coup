import java.util.Scanner;
public class Main {
    private static Scanner keyboard;
    private static Card[] cards;
    private static Player[] players;
    private static int currentTurn;
    public static void main(String[] args) {

        // GAME RULES
        keyboard = new Scanner(System.in);
        int numberOfOpponents = askNumber("How many opponents (1-5)?", 1, 5);

        // SETUP PLAYERS
        final int minLength = 1;
        final int maxLength = 10;
        players = new Player[1 + numberOfOpponents];
        players[0] = new User(namePlayer("Name yourself: ", minLength, maxLength), 0);
        for (int i = 1; i < players.length; i++)
            players[i] = new Bot(namePlayer("Name opponent #" + i + ": ", minLength, maxLength), i);

        for (int i = 0; i < players.length; i++)
            System.out.println((i + 1)+ ": " + players[i].getName());
        currentTurn = askNumber("Which player should start?", 1, players.length + 1) - 1;


        // SETUP DECK
        cards = new Card[15];
        for (int i = 0; i < 15; i += 5) {
            cards[i] = new Card(Cards.DUKE);
            cards[i+1] = new Card(Cards.ASSASSIN);
            cards[i+2] = new Card(Cards.AMBASSADOR);
            cards[i+3] = new Card(Cards.CAPTAIN);
            cards[i+4] = new Card(Cards.CONTESSA);
        }

        // DRAW STARTING CARDS
        for (Player player : players) {
            drawCard(player);
            drawCard(player);
        }

        // TURNS (if the game goes from more than one alive player to no alive players in one turn, this would break)
        int lastTurn = -1;
        while(lastTurn != currentTurn) {
            players[currentTurn].takeTurn();
            lastTurn = currentTurn;
            do {
                currentTurn++;
                currentTurn = currentTurn % players.length;
            } while (isPlayerAlive(players[currentTurn]));
        }

        System.out.println(players[currentTurn].getName() + " wins!");

    }

    public static void drawCard(Player player) {
        Card[] cardsInDeck = getCardsInZone(Zones.getZone(GlobalZones.DECK));
        cardsInDeck[(int) (Math.random() * cardsInDeck.length)].setZone(Zones.getZone(player));
    }

    public static String namePlayer(String message, int minLength, int maxLength) {
        System.out.print(message);
        String output = keyboard.next();
        if (output.length() < minLength || output.length() > maxLength) {
            System.out.println("Name must be " + minLength + "-" + maxLength + " characters.");
            return namePlayer(message, minLength, maxLength);
        }

        for (Player player : players) {
            if (player != null) {
                if (player.getName().equals(output)) {
                    System.out.println("Name is already in use.");
                    return namePlayer(message, minLength, maxLength);
                }
            }
        }

        return output;
    }

    public static boolean isPlayerAlive(Player player) {
        return getCardsInZone(Zones.getZone(player)).length > 0;
    }

    public static int askNumber(String message, int min, int max)
    {
        System.out.println(message);
        int output;
        try {
            output = keyboard.nextInt();
            if (output < min || output > max) {
                System.out.println("Input must be between " + min + "-" + max);
                return askNumber(message, min, max);
            }
            return output;
        } catch (Exception e) {
            keyboard.nextLine();
            System.out.println("Input must be an integer.");
            return askNumber(message, min, max);
        }
    }

    public static void printGamestate() {
        for (Card card : cards)
            System.out.println(card);
        for (Player player : players) {
            System.out.println(player);
        }
    }

    public static Card[] getCardsInZone(int zone) {
        int amountOfCards = 0;
        for(Card value : cards) {
            if(value.getZone() == zone)
                amountOfCards++;
        }

        Card[] cardsInZone = new Card[amountOfCards];
        int index = 0;
        for(Card value : cards) {
            if (value.getZone() == zone) {
                cardsInZone[index] = value;
                index++;
            }
        }

        return cardsInZone;
    }

    public static boolean offerBlock(Player player, Actions action, Player target) {
        for (int i = 1; i < players.length - 1; i++)
        {
            Player blocker = players[(i + player.getPositionInTurnOrder()) % players.length];
            Cards chosenCard = blocker.wantsToBlock(player, action, target);
            if (chosenCard != null) {
                if (player.wantsToChallenge(blocker, chosenCard, target)) {
                    return resolveChallenge(player, blocker, chosenCard);
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean offerChallenge(Player player, Cards card, Player target) {
        for (int i = 1; i < players.length - 1; i++)
        {
            Player challenger = players[(i + player.getPositionInTurnOrder()) % players.length];
            if (challenger.wantsToChallenge(player, card, target)) {
                return resolveChallenge(challenger, player, card);
            }
        }
        return false;
    }

    public static boolean resolveChallenge(Player challenger, Player challenged, Cards card) {
        System.out.println(challenger.getName() + " challenged " + challenged.getName() + "'s claim of " + card + ".");
        Card chosenCard = challenger.resolveChallenge(card);
        if(chosenCard == null) {
            System.out.println(challenged.getName() + " lost the challenge.");
            challenged.discard();
            return true;
        } else {
            System.out.println(challenged.getName() + " reveals a " + card + " and shuffles it back into the deck, winning the challenge.");
            challenger.discard();
            chosenCard.setZone(Zones.getZone(GlobalZones.DECK));
            return false;
        }
    }
}