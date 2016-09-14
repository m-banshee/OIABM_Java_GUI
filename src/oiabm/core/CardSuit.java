package oiabm.core;

/**
 * gui.GuiCard suit enumeration.
 */
public enum CardSuit {
    SPADES, CLUBS, DIAMONDS, HEARTS;

    public static CardSuit getRandom() {
        return values()[(int) (Math.random() * values().length)];
    }
}