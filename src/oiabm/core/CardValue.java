package oiabm.core;

/**
 * gui.GuiCard value enumeration
 */
public enum CardValue {
    ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING;

    public static CardValue getRandom() {
        double rand = Math.random() * values().length;
        return values()[(int) rand];
    }
}
