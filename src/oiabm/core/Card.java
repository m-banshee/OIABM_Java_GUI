package oiabm.core;

/**
 * Core card class. Handles non-graphical card functions.
 */
public class Card {
    private CardSuit suit;
    private CardValue value;

    /**
     * Main constructor. Cards cannot be instantiated without a suit or value, as that makes no sense and causes null issues.
     *
     * @param suit  The card's suit.
     * @param value The card's value.
     */
    public Card(CardSuit suit, CardValue value) {
        this.suit = suit;
        this.value = value;
    }

    /**
     * Copy constructor.
     *
     * @param card The card to copy.
     */
    public Card(Card card) {
        this.suit = card.suit;
        this.value = card.value;
    }

    public CardSuit getSuit() {
        return this.suit;
    }

    public CardValue getValue() {
        return this.value;
    }
}
