package oiabm.gui;

import java.awt.*;
import java.util.Vector;

/**
 * JPanel paint manager. Util methods for drawing card piles in appropriate places.
 */
public class PaintManager {
    public void paintLeftPiles(Vector<Vector<GuiCard>> L_piles, Vector<Vector<GuiCard>> R_piles) {
                /* Cycle through the four Left piles */
        for (int i = 0; i < 4; i++) {
            Vector<GuiCard> outer = L_piles.elementAt(i);
                    /* Cycle through each left pile */
            for (int j = 0; j < outer.size(); j++) {
                GuiCard c = outer.elementAt(j);

                        /* If it's the top, set it face-up */
                if (j == 0) {
                    c.setFaceUp();
                } else {
                    c.setFaceDown();
                }

                        /* Set paint coordinates for card */
                c.setPosition(new Point(pilePos[i].x + j * SPREAD, pilePos[i].y));
                add(Board.moveCard(c, pilePos[i].x + j * SPREAD, pilePos[i].y));
                c.setWhereAmI(getPosition());
            }
        }
    }

    public void paintRightPiles() {
        for (int i = 0; i < 4; i++) {
            Vector<GuiCard> rp = R_piles.elementAt(i);
                    /* Cycle through each right pile */
            for (int j = rp.size(); j > 0; j--) {
                        /* Set them faceup */
                GuiCard c = rp.elementAt(j - 1);
                c.setFaceUp();

                        /* Set Pain coordinates for card */
                c.setPosition(new Point(pilePos[i].x + ROW_START_X + j * (SPREAD * 2), pilePos[i].y));
                add(Board.moveCard(c, pilePos[i].x + ROW_START_X + j * (SPREAD * 2), pilePos[i].y));
                c.setWhereAmI(getPosition());
            }
        }
    }

    public void paintDeckPile() {
        int spreadVar = 0;

                /* If deck isn't empty, show card at deckPtr */
        if (!L_piles.elementAt(4).isEmpty()) {
            GuiCard c_temp = L_piles.elementAt(4).elementAt(deckPtr);
            c_temp.setFaceUp();
            c_temp.setPosition(new Point(pilePos[4].x, pilePos[4].y));
            add(Board.moveCard(c_temp, pilePos[4].x, pilePos[4].y));
            c_temp.setWhereAmI(getPosition());
        }

                /* Don't actually know why I need this outer loop,
                       but it doesn't work without it. */
        for (int i = 0; i < 5; i++) {
                    /* Cycle through deck pile */
            for (int j = 0; j < L_piles.elementAt(4).size(); j++) {
                GuiCard c = L_piles.elementAt(4).elementAt(j);

                        /* Set spread variable if it's one card before deckPtr */
                if (j == deckPtr - 1) {
                    spreadVar = 80;
                    c.setPosition(new Point(pilePos[i].x + (j + 1) * SPREAD, pilePos[i].y));
                    add(Board.moveCard(c, pilePos[i].x + (j + 1) * SPREAD, pilePos[i].y));
                    c.setWhereAmI(getPosition());

                    c.setFaceDown();
                }
                        /* Spreads deck for single card */
                else if (j == 0 && deckPtr == 0) {
                    spreadVar = 80;
                }
                        /* Paint deck card */
                else if (j != deckPtr) {
                    c.setFaceDown();
                    c.setPosition(new Point(pilePos[i].x + spreadVar + (j + 1) * SPREAD, pilePos[i].y));
                    add(Board.moveCard(c, pilePos[i].x + spreadVar + (j + 1) * SPREAD, pilePos[i].y));
                    c.setWhereAmI(getPosition());
                }
            }
            spreadVar = 0;
        }
    }
}
