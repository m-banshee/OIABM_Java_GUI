/*!******************************************************************************
  * @file    BlueMoon.java
  * @author  Mark Banchy
  * @date    May 20, 2016
  * @brief   Java class for implementing "Once In A Blue Moon".\n 
  *          Implements logic for painting and moving cards.
  *****************************************************************************/

import gui.GuiCard;
import oiabm.core.CardSuit;
import oiabm.core.CardValue;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;
import java.io.*;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

/* This is GUI component with a embedded
 * data structure. This structure is a mixture
 * of a queue and a stack
 */
class BlueMoon extends JComponent {
    protected int SPREAD = 8;
    public static int ROW_START_X = 150;
    public static int DEST_ROW_WIDTH = 300;
    public static int BASE_X = 5, BASE_Y = 5;
    public static int ROW_HEIGHT = 100;

    public static final Point[] pilePos = new Point[5];
    public Vector<Vector<GuiCard>> leftPiles = new Vector<>();
    public Vector<Vector<GuiCard>> rightPiles = new Vector<>();

    public Vector<GuiCard> tempDeck;

    public Rectangle[][] hitBoxes = new Rectangle[5][2];

    protected int _x = 0;
    protected int _y = 0;

    public int deckPtr = 2;

    Vector<int[]> prevMoves = new Vector<int[]>();

    boolean changeHappened = true, winState = false;
    winAnimation animation = new winAnimation();
    paintManager deckPainter = new paintManager();

    public BlueMoon() {
            /* Initialize array of points to determine where to paint piles. */
        pilePos[0] = new Point(BASE_X + 5, 0 * ROW_HEIGHT + 5);
        pilePos[1] = new Point(BASE_X + 5, 1 * ROW_HEIGHT + 5);
        pilePos[2] = new Point(BASE_X + 5, 2 * ROW_HEIGHT + 5);
        pilePos[3] = new Point(BASE_X + 5, 3 * ROW_HEIGHT + 5);
        pilePos[4] = new Point(BASE_X + 5, 4 * ROW_HEIGHT + 5);

        this.setPosition(0, 0);

        this.setLayout(null);
        tempDeck = new Vector<GuiCard>();
        leftPiles = new Vector<Vector<GuiCard>>();
            
            /* Setup 5 Left Piles */
        leftPiles.add(new Vector<GuiCard>());
        leftPiles.add(new Vector<GuiCard>());
        leftPiles.add(new Vector<GuiCard>());
        leftPiles.add(new Vector<GuiCard>());
        leftPiles.add(new Vector<GuiCard>());
            
            /* Setup 4 Right Piles */
        rightPiles.add(new Vector<GuiCard>());
        rightPiles.add(new Vector<GuiCard>());
        rightPiles.add(new Vector<GuiCard>());
        rightPiles.add(new Vector<GuiCard>());
            
            /* Populate deck */
        for (CardSuit suit : CardSuit.values()) {
            for (CardValue value : CardValue.values()) {
                try {
                    tempDeck.add(new GuiCard(suit, value));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
            
            /* Shuffle deck */
        this.shuffle();
            
            /* Populate left piles */
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                Vector<GuiCard> outer = leftPiles.elementAt(i);
                outer.add(tempDeck.remove((int) (Math.random() * tempDeck.size())));
            }

        }
            
            /* Populate right piles */
        rightPiles.elementAt(0).add(tempDeck.remove(0));
            
            /* Populate draw deck */
        while (!tempDeck.isEmpty()) {
            leftPiles.elementAt(4).add(tempDeck.remove(0));
        }
            
            /* Initialize hitboxes for piles */
        hitBoxes[0][0] = new Rectangle(BASE_X + 5, BASE_Y + 5, BASE_X + 5 * SPREAD + GuiCard.CARD_WIDTH, GuiCard.CARD_HEIGHT);
        hitBoxes[0][1] = new Rectangle(BASE_X + 5 + ROW_START_X, BASE_Y + 5, DEST_ROW_WIDTH, GuiCard.CARD_HEIGHT);

        hitBoxes[1][0] = new Rectangle(BASE_X + 5, BASE_Y + GuiCard.CARD_HEIGHT + 5, BASE_X + 5 * SPREAD + GuiCard.CARD_WIDTH, GuiCard.CARD_HEIGHT);
        hitBoxes[1][1] = new Rectangle(BASE_X + 5 + ROW_START_X, BASE_Y + GuiCard.CARD_HEIGHT + 5, DEST_ROW_WIDTH, GuiCard.CARD_HEIGHT);

        hitBoxes[2][0] = new Rectangle(BASE_X + 5, BASE_Y + 2 * GuiCard.CARD_HEIGHT + 5, BASE_X + 5 * SPREAD + GuiCard.CARD_WIDTH, GuiCard.CARD_HEIGHT);
        hitBoxes[2][1] = new Rectangle(BASE_X + 5 + ROW_START_X, BASE_Y + 2 * GuiCard.CARD_HEIGHT + 5, DEST_ROW_WIDTH, GuiCard.CARD_HEIGHT);

        hitBoxes[3][0] = new Rectangle(BASE_X + 5, BASE_Y + 3 * GuiCard.CARD_HEIGHT + 5, BASE_X + 5 * SPREAD + GuiCard.CARD_WIDTH, GuiCard.CARD_HEIGHT);
        hitBoxes[3][1] = new Rectangle(BASE_X + 5 + ROW_START_X, BASE_Y + 3 * GuiCard.CARD_HEIGHT + 5, DEST_ROW_WIDTH, GuiCard.CARD_HEIGHT);

        hitBoxes[4][0] = new Rectangle(BASE_X + 5, BASE_Y + 4 * GuiCard.CARD_HEIGHT + 5, BASE_X + 5 * SPREAD + GuiCard.CARD_WIDTH + DEST_ROW_WIDTH, GuiCard.CARD_HEIGHT);
        hitBoxes[4][1] = new Rectangle(BASE_X + 5, BASE_Y + 4 * GuiCard.CARD_HEIGHT + 5, BASE_X + 5 * SPREAD + GuiCard.CARD_WIDTH + DEST_ROW_WIDTH, GuiCard.CARD_HEIGHT);
    }

    /* Variables for game logic checks */
    private GuiCard source = null;
    private int destRow = -1;
    private int sourceRow = -1;

    /* Determines if Card c can be placed in dRow (destination row) */
    private boolean canCardBePlaced(GuiCard c, int dRow) {
            /* Check if it's a deck flip */
        if (dRow == 4 && sourceRow == 4) {
            return true;
        }
            /* There's no destination row 4 */
        else if (dRow == 4) {
            return false;
        }
                    
            /* Verify the suit hasn't been played before */
        for (int i = 0; i < dRow; i++) {
                /* Verify previous rows aren't empty */
            if (rightPiles.elementAt(i).isEmpty()) {
                return false;
            }
                /* Verify suit hasn't been played before */
            else if (rightPiles.elementAt(i).firstElement().getSuit() == c.getSuit()) {
                return false;
            }
        }
                    
            /* Handle first row check */
        if (dRow == 0 && (rightPiles.elementAt(0).firstElement().getSuit() == c.getSuit())) {
            return true;
        }
            /* If destination row is empty, the new
               card has to match prev row first card.*/
        else if (rightPiles.elementAt(dRow).isEmpty()) {
                /* Verify card value is the same as the first row first entry value */
            if (c.getValue() == rightPiles.elementAt(0).firstElement().getValue()) {
                return true;
            } else {
                return false;
            }
        }
            /* Verify row is valid */
        else if (dRow <= 3 && dRow > 0) {
                /* Check to make sure the value is in the previous row */
            Vector<GuiCard> prevRow = rightPiles.elementAt(dRow - 1);
            for (int j = 0; j < prevRow.size(); j++) {
                GuiCard tempC = prevRow.elementAt(j);
                    /* Verify value and suit match */
                if (tempC.getValue() == c.getValue() &&
                        rightPiles.elementAt(dRow).firstElement().getSuit() == c.getSuit()) {
                    return true;
                }
            }
        }

        return false;
    }

    /* Takes a card and destination row and determines if it's a valid move */
    private boolean validPlayStackMove(GuiCard source, int destRow) {
            /* Verify card is valid */
        if (source == null) {
            return false;
        }
            
            /* Verify card is eligible to move to destRow */
        return canCardBePlaced(source, destRow);
    }

    public boolean isDragMove(Point p1, Point p2) {
        return getSourceRow(p1) != getSourceRow(p2);
    }

    public void performUndo(int sRow, int dRow, int dPtr) {
        GuiCard temp;
        if (sRow == 4 && dRow != 4) {
            temp = rightPiles.elementAt(dRow).elementAt(rightPiles.elementAt(dRow).size() - 1);
            if (deckPtr == 2) {
                deckPtr = 0;
            } else {
                deckPtr++;
            }
            leftPiles.elementAt(sRow).add(deckPtr, temp);
            rightPiles.elementAt(dRow).removeElementAt(rightPiles.elementAt(dRow).size() - 1);
        } else if (sRow == 4 && dRow == 4) {
            deckPtr = dPtr;
        } else {
            temp = rightPiles.elementAt(dRow).elementAt(rightPiles.elementAt(dRow).size() - 1);
            leftPiles.elementAt(sRow).add(0, temp);
            rightPiles.elementAt(dRow).removeElementAt(rightPiles.elementAt(dRow).size() - 1);
        }
        changeHappened = true;
    }

    public void popPrevMove() {
        if (prevMoves.size() > 0) {

            int[] arr = prevMoves.elementAt(prevMoves.size() - 1);
            //System.out.println("Pop: " + arr[0] + " " + arr[1]+ " " + arr[2]);
            performUndo(arr[0], arr[1], arr[2]);
            prevMoves.removeElementAt(prevMoves.size() - 1);

        }
    }

    public void pushPrevMove(int sRow, int dRow, int dPtr) {
        int[] tempMove = new int[3];
        tempMove[0] = sRow;
        tempMove[1] = dRow;
        tempMove[2] = dPtr;
        prevMoves.add(tempMove);
    }

    public void handleDoubleClick(Point p) {
        updateSourceCard(p);

        if (sourceRow != -1) {
            for (int i = 0; i < 4; i++) {
                    /* Determine if move is valid */
                if (validPlayStackMove(source, i)) {
                    GuiCard temp;
                        
                        /* Check sourceRow and destRow combo is valid */
                    if (sourceRow != 4) {
                        pushPrevMove(sourceRow, i, deckPtr);
                            /* Move card from Left pile to Right pile */
                        temp = leftPiles.elementAt(sourceRow).elementAt(0);
                        leftPiles.elementAt(sourceRow).remove(0);
                        rightPiles.elementAt(i).add(temp);
                    } else if (sourceRow == 4) {
                        pushPrevMove(sourceRow, i, deckPtr);
                            /* Move card from Left pile to Right pile */
                        temp = leftPiles.elementAt(sourceRow).elementAt(deckPtr);
                        leftPiles.elementAt(sourceRow).remove(deckPtr);
                        rightPiles.elementAt(i).add(temp);
                        decrementDeckPointer();
                    }

                    changeHappened = true;
                    repaint();

                    if (winCheck()) {
                        JOptionPane.showMessageDialog(Board.frame, "Congratulations, you've won!", "Winner!", JOptionPane.INFORMATION_MESSAGE);
                        winState = true;
                        changeHappened = true;
                        repaint();
                    }

                }
            }
        }
    }

    public void updateSourceCard(Point p) {
        sourceRow = getSourceRow(p);
            
            /* Verify source row is valid and source pile isn't empty */
        if (sourceRow >= 0 && sourceRow < 5 && !leftPiles.elementAt(sourceRow).isEmpty()) {
                /* Check for deck, if so pull card from deckPtr */
            if (sourceRow == 4) {
                source = leftPiles.elementAt(sourceRow).elementAt(deckPtr);
            }
                /* Pull first element from piles */
            else {
                source = leftPiles.elementAt(sourceRow).firstElement();
            }
        }
            /* Not valid source */
        else {
            sourceRow = -1;
            source = null;
        }
    }

    public void handleMousePress(Point start) {
        updateSourceCard(start);
    }

    /* Determine source row from Point object 'start' */
    public int getSourceRow(Point start) {
            /* Cycle through hitboxes to determine if  
               the point is within its bounds */
        for (int i = 0; i < 5; i++) {
            if (hitBoxes[i][0].contains(start)) {
                return i;
            }
        }
        return -1;
    }

    /* Determine destination row from Point object 'stop' */
    public int getDestRow(Point stop) {
        for (int i = 0; i < 5; i++) {
            if (hitBoxes[i][1].contains(stop)) {
                return i;
            }
        }
        return -1;
    }

    public boolean winCheck() {
        for (int i = 0; i < 5; i++) {
            if (!leftPiles.elementAt(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void handleMouseRelease(Point stop) {
            /* Determine destination row from 'stop' Point */
        destRow = getDestRow(stop);

            /* Verify destRow and source card is valid */
        if (destRow != -1 && destRow <= 4 && source != null) {
                /* Determine if move is valid */
            if (validPlayStackMove(source, destRow)) {
                GuiCard temp;
                    
                    /* Check sourceRow and destRow combo is valid */
                if (sourceRow < 4 && destRow != 4) {
                    pushPrevMove(sourceRow, destRow, deckPtr);
                        /* Move card from Left pile to Right pile */
                    temp = leftPiles.elementAt(sourceRow).elementAt(0);
                    leftPiles.elementAt(sourceRow).remove(0);
                    rightPiles.elementAt(destRow).add(temp);
                } else if (sourceRow == 4 && destRow == 4) {
                    pushPrevMove(sourceRow, destRow, deckPtr);
                        /* Deck flip */
                    incrementDeckPointer();
                } else if (destRow != 4) {
                    pushPrevMove(sourceRow, destRow, deckPtr);
                        /* Move card from Left pile to Right pile */
                    temp = leftPiles.elementAt(sourceRow).elementAt(deckPtr);
                    leftPiles.elementAt(sourceRow).remove(deckPtr);
                    rightPiles.elementAt(destRow).add(temp);
                    decrementDeckPointer();
                }

                changeHappened = true;
                repaint();
            }
        }
            /* Invalid parameters */
        else {
            destRow = -1;
        }
    }

    /* Decrement deck pointer */
    public void decrementDeckPointer() {
            /* Get deck size */
        int size = leftPiles.elementAt(4).size();
            
            /* Don't decrease past zero */
        if (this.deckPtr != 0) {
            this.deckPtr--;
        }
            /* Make sure there's enough cards to set deckPtr to 2 */
        else if (size >= 3) {
            this.deckPtr = 2;
        }
            /* deckPtr == 0 and deck size is <= 3, set it to end of deck  */
        else {
            this.deckPtr = leftPiles.elementAt(4).size() - 1;
        }
    }

    /* Increment deckPtr */
    public void incrementDeckPointer() {
            /* Get deck size */
        int size = leftPiles.elementAt(4).size();
            
            /* Make sure not to set deckPtr out of bounds */
        if ((deckPtr + 3) <= (size - 1)) {
            this.deckPtr += 3;
        }
            /* Reset deckPtr to beginning of deck */
        else if (deckPtr == (size - 1) && size >= 3) {
            this.deckPtr = 2;
        }
            /* Deck size is <= 3, set it to end of deck */
        else {
            this.deckPtr = leftPiles.elementAt(4).size() - 1;
        }
    }

    /* Shuffle temp deck */
    public void shuffle() {
        Vector<GuiCard> v = new Vector<GuiCard>();
        while (!this.tempDeck.isEmpty()) {
            v.add(this.tempDeck.remove(0));
        }
        while (!v.isEmpty()) {
            GuiCard c = v.elementAt((int) (Math.random() * v.size()));
            this.tempDeck.add(c);
            v.removeElement(c);
        }
    }

    @Override
    public boolean contains(Point p) {
        Rectangle rect = new Rectangle(_x, _y, GuiCard.CARD_WIDTH + 10, GuiCard.CARD_HEIGHT * 3);
        return (rect.contains(p));
    }

    public void setPosition(int x, int y) {
        _x = x;
        _y = y;
        setBounds(_x, _y, Board.table.getWidth(), Board.table.getHeight());
    }

    public Point getPosition() {
        return new Point(_x, _y);
    }

    public void handleWin() {
        JOptionPane.showMessageDialog(Board.frame, "Congratulations, you've won!", "Winner!", JOptionPane.INFORMATION_MESSAGE);
        winState = true;
        changeHappened = true;
        repaint();
    }

    private class winAnimation {
        double a = .8, theta = 0, thetaChange = 1.5;
        boolean toggle = false;

        public void play() {
            if (theta == 0) {
                removeAll();
            }

            int startX = (Board.table.getWidth() - GuiCard.CARD_WIDTH - 5) / 2;
            int startY = (Board.table.getHeight() - GuiCard.CARD_HEIGHT - 5) / 2;

            GuiCard tempC = null;

            try {
                tempC = new GuiCard();
                if (toggle) {
                    tempC.setFaceUp();
                } else {
                    tempC.setFaceDown();
                }
                toggle = !toggle;
            } catch (IOException ex) {
                Logger.getLogger(BlueMoon.class.getName()).log(Level.SEVERE, null, ex);
            }

            double tempX = a * theta * cos(theta);
            double tempY = a * theta * sin(theta);

                /* Set paint coordinates for card */
            if (tempC != null) {
                tempC.setPosition(new Point((int) tempX + startX, (int) tempY + startY));
                add(Board.moveCard(tempC, (int) tempX + startX, (int) tempY + startY));
                tempC.setWhereAmI(getPosition());
                theta += thetaChange;
                Board.table.repaint();
            }

            if ((int) tempX > Board.frame.getWidth() / 2 && (int) tempY > Board.frame.getHeight() / 2) {
                theta = 0;
                thetaChange += .1;
                if (thetaChange > 5) {
                    thetaChange = 3;
                }
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
                Logger.getLogger(BlueMoon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public class paintManager {
        public void paintLeftPiles() {
                /* Cycle through the four Left piles */
            for (int i = 0; i < 4; i++) {
                Vector<GuiCard> outer = leftPiles.elementAt(i);
                    /* Cycle through each left pile */
                for (int j = 0; j < outer.size(); j++) {
                    GuiCard c = outer.elementAt(j);

                        /* If it's the top, set it faceup */
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
                Vector<GuiCard> rp = rightPiles.elementAt(i);
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
            if (!leftPiles.elementAt(4).isEmpty()) {
                GuiCard c_temp = leftPiles.elementAt(4).elementAt(deckPtr);
                c_temp.setFaceUp();
                c_temp.setPosition(new Point(pilePos[4].x, pilePos[4].y));
                add(Board.moveCard(c_temp, pilePos[4].x, pilePos[4].y));
                c_temp.setWhereAmI(getPosition());
            }

                /* Don't actually know why I need this outer loop,
                       but it doesn't work without it. */
            for (int i = 0; i < 5; i++) {
                    /* Cycle through deck pile */
                for (int j = 0; j < leftPiles.elementAt(4).size(); j++) {
                    GuiCard c = leftPiles.elementAt(4).elementAt(j);

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

    @Override
    protected void paintComponent(Graphics g) {
        if (changeHappened) {
            super.paintComponent(g);
            this.setPosition(_x, _y);

            if (winState) {
                animation.play();
            } else {
                removeAll();

                deckPainter.paintLeftPiles();
                deckPainter.paintRightPiles();
                deckPainter.paintDeckPile();

                changeHappened = false;
            }
        }
    }
}