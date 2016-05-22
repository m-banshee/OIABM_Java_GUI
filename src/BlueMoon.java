/*!******************************************************************************
  * @file    BlueMoon.java
  * @author  Mark Banchy
  * @date    May 20, 2016
  * @brief   Java class for implementing "Once In A Blue Moon".\n 
  *          Implements logic for painting and moving cards.
  *****************************************************************************/

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;
import java.io.*;

import javax.swing.JComponent;

/* This is GUI component with a embedded
 * data structure. This structure is a mixture
 * of a queue and a stack
 */
class BlueMoon extends JComponent
{
        protected int SPREAD = 8;
        public static int ROW_START_X = 150;
        public static int DEST_ROW_WIDTH = 300;
        public static int BASE_X = 5, BASE_Y = 5;
        public static int ROW_BOX_X = BASE_X + ROW_START_X;
        public static int ROW_HEIGHT = 100;
	public static final Point DECK_POS = new Point(5, 5);
    
        public static final Point[] pilePos = new Point[5];
        public Vector<Vector<Card>> L_piles = new Vector<Vector<Card>>();
        public Vector<Vector<Card>> R_piles = new Vector<Vector<Card>>();
        
	protected final int NUM_CARDS = 52;
	public Vector<Card> tempDeck;
	
        public Rectangle[][] hitBoxes = new Rectangle[5][2];

	protected int _x = 0;
	protected int _y = 0;

        public int deckPtr = 2;

	public BlueMoon()
	{
            /* Initialize array of points to determine where to paint piles. */
            pilePos[0] = new Point(BASE_X, 0*ROW_HEIGHT);
            pilePos[1] = new Point(BASE_X, 1*ROW_HEIGHT);
            pilePos[2] = new Point(BASE_X, 2*ROW_HEIGHT);
            pilePos[3] = new Point(BASE_X, 3*ROW_HEIGHT);
            pilePos[4] = new Point(BASE_X, 4*ROW_HEIGHT);

            this.setXY(BASE_X, BASE_Y);

            this.setLayout(null);
            tempDeck = new Vector<Card>();
            L_piles = new Vector<Vector<Card>>();
            
            /* Setup 5 Left Piles */
            L_piles.add(new Vector<Card>());
            L_piles.add(new Vector<Card>());
            L_piles.add(new Vector<Card>());
            L_piles.add(new Vector<Card>());
            L_piles.add(new Vector<Card>());
            
            /* Setup 4 Right Piles */
            R_piles.add(new Vector<Card>());
            R_piles.add(new Vector<Card>());
            R_piles.add(new Vector<Card>());
            R_piles.add(new Vector<Card>());
            
            /* Populate deck */
            for (Card.Suit suit : Card.Suit.values()) 
            {
                for (Card.Value value : Card.Value.values()) 
                {
                    try 
                    {
                        tempDeck.add(new Card(suit, value));
                    } 
                    catch (IOException e) 
                    {
                        e.printStackTrace();
                    }
                }
            }
            
            /* Shuffle deck */
            this.shuffle();
            
            /* Populate left piles */
            for(int i = 0; i < 4; i++)
            {
                for(int j = 0; j < 5; j++)
                {
                    Vector<Card> outer = L_piles.elementAt(i);
                    outer.add(tempDeck.remove((int) (Math.random() * tempDeck.size())));
                }
                
            }
            
            /* Populate right piles */
            R_piles.elementAt(0).add(tempDeck.remove(0));
            
            /* Populate draw deck */
            while(!tempDeck.isEmpty())
            {
                L_piles.elementAt(4).add(tempDeck.remove(0));
            }
            
            /* Initialize hitboxes for piles */
            hitBoxes[0][0] = new Rectangle(BASE_X, BASE_Y, BASE_X + 5*SPREAD + Card.CARD_WIDTH, Card.CARD_HEIGHT);
            hitBoxes[0][1] = new Rectangle(BASE_X + ROW_START_X, BASE_Y, DEST_ROW_WIDTH, Card.CARD_HEIGHT);
            
            hitBoxes[1][0] = new Rectangle(BASE_X, BASE_Y + Card.CARD_HEIGHT, BASE_X + 5*SPREAD + Card.CARD_WIDTH, Card.CARD_HEIGHT);
            hitBoxes[1][1] = new Rectangle(BASE_X + ROW_START_X, BASE_Y + Card.CARD_HEIGHT, DEST_ROW_WIDTH, Card.CARD_HEIGHT);
            
            hitBoxes[2][0] = new Rectangle(BASE_X, BASE_Y + 2*Card.CARD_HEIGHT, BASE_X + 5*SPREAD + Card.CARD_WIDTH, Card.CARD_HEIGHT);
            hitBoxes[2][1] = new Rectangle(BASE_X + ROW_START_X, BASE_Y + 2*Card.CARD_HEIGHT, DEST_ROW_WIDTH, Card.CARD_HEIGHT);
            
            hitBoxes[3][0] = new Rectangle(BASE_X, BASE_Y + 3*Card.CARD_HEIGHT, BASE_X + 5*SPREAD + Card.CARD_WIDTH, Card.CARD_HEIGHT);
            hitBoxes[3][1] = new Rectangle(BASE_X + ROW_START_X, BASE_Y + 3*Card.CARD_HEIGHT, DEST_ROW_WIDTH, Card.CARD_HEIGHT);
            
            hitBoxes[4][0] = new Rectangle(BASE_X, BASE_Y + 4*Card.CARD_HEIGHT, BASE_X + 5*SPREAD + Card.CARD_WIDTH + DEST_ROW_WIDTH, Card.CARD_HEIGHT);
            hitBoxes[4][1] = new Rectangle(BASE_X, BASE_Y + 4*Card.CARD_HEIGHT, BASE_X + 5*SPREAD + Card.CARD_WIDTH + DEST_ROW_WIDTH, Card.CARD_HEIGHT);
	}
                
	/* Variables for game logic checks */
	private Card source = null;
        private int destRow = -1;
        private int sourceRow = -1;

        /* Determines if Card c can be placed in dRow (destination row) */
        private boolean isCardEligible(Card c, int dRow)
        {
            /* Check if it's a deck flip */
            if(dRow == 4 && sourceRow == 4)
            {
                return true;
            }
            /* There's no destination row 4 */
            else if(dRow == 4)
            {
                return false;
            }
                    
            /* Verify the suit hasn't been played before */
            for(int i = 0; i < dRow; i++)
            {
                /* Verify previous rows aren't empty */
                if(R_piles.elementAt(i).isEmpty())
                {
                    return false;
                }
                /* Verify suit hasn't been played before */
                else if(R_piles.elementAt(i).firstElement().getSuit() == c.getSuit())
                {
                    return false;
                }
            }
                    
            /* Handle first row check */
            if (dRow == 0 && (R_piles.elementAt(0).firstElement().getSuit() == c.getSuit())) 
            {
                return true;
            } 
            /* If destination row is empty, the new
               card has to match prev row first card.*/
            else if (R_piles.elementAt(dRow).isEmpty()) 
            {
                /* Verify card value is the same as the first row first entry value */
                if (c.getValue() == R_piles.elementAt(0).firstElement().getValue()) 
                {
                    return true;
                } 
                else 
                {
                    return false;
                }
            } 
            /* Verify row is valid */
            else if (dRow <= 3 && dRow > 0) 
            {
                /* Check to make sure the value is in the previous row */
                Vector<Card> prevRow = R_piles.elementAt(dRow - 1);
                for (int j = 0; j < prevRow.size(); j++) 
                {
                    Card tempC = prevRow.elementAt(j);
                    /* Verify value and suit match */
                    if (tempC.getValue() == c.getValue() && 
                        R_piles.elementAt(dRow).firstElement().getSuit() == c.getSuit()) 
                    {
                        return true;
                    }
                }
            }

            return false;
        }
                
        /* Takes a card and destination row and determines if it's a valid move */
	private boolean validPlayStackMove(Card source, int destRow) 
        {
            /* Verify card is valid */
            if (source == null) 
            {
                return false;
            }
            
            /* Verify card is eligible to move to destRow */
            return isCardEligible(source, destRow);
        }

        public boolean isDragMove(Point p1, Point p2)
        {
            return getSourceRow(p1) != getSourceRow(p2);
        }
        
        public void handleDoubleClick(Point p)
        {
            updateSourceCard(p);
            
            if(sourceRow != -1)
            {
                
                for(int i = 0; i < 4; i++)
                {
                    /* Determine if move is valid */
                    if (validPlayStackMove(source, i)) 
                    {
                        Card temp;

                        /* Check sourceRow and destRow combo is valid */
                        if (sourceRow != 4) 
                        {
                            /* Move card from Left pile to Right pile */
                            temp = L_piles.elementAt(sourceRow).elementAt(0);
                            L_piles.elementAt(sourceRow).remove(0);
                            R_piles.elementAt(i).add(temp);
                        } 
                        else if (sourceRow == 4) 
                        {
                            /* Move card from Left pile to Right pile */
                            temp = L_piles.elementAt(sourceRow).elementAt(deckPtr);
                            L_piles.elementAt(sourceRow).remove(deckPtr);
                            R_piles.elementAt(i).add(temp);
                            decDeckPtr();
                        }
                    }
                }
            }
        }
        
        public void updateSourceCard(Point p)
        {
            sourceRow = getSourceRow(p);
            
            if(sourceRow != -1 && !L_piles.elementAt(sourceRow).isEmpty())
            {
                if(sourceRow == 4)
                    source = L_piles.elementAt(sourceRow).elementAt(deckPtr);
                else
                    source = L_piles.elementAt(sourceRow).firstElement();
            }
            else
            {
                sourceRow = -1;
            }
        }
        
	public void handleMousePress(Point start)
	{     
            /* Determine row hitbox for source card */
            sourceRow = getSourceRow(start);
            
            /* Verify source row is valid and source pile isn't empty */
            if (sourceRow >= 0 && sourceRow < 5 && !L_piles.elementAt(sourceRow).isEmpty()) 
            {
                /* Check for deck, if so pull card from deckPtr */
                if (sourceRow == 4) 
                {
                    source = L_piles.elementAt(sourceRow).elementAt(deckPtr);
                }
                /* Pull first element from piles */
                else 
                {
                    source = L_piles.elementAt(sourceRow).firstElement();
                }
            }
            /* Not valid source */
            else 
            {
                sourceRow = -1;
                source = null;
            }
        }

        /* Determine source row from Point object 'start' */
        public int getSourceRow(Point start) 
        {
            /* Cycle through hitboxes to determine if  
               the point is within its bounds */
            for (int i = 0; i < 5; i++) 
            {
                if (hitBoxes[i][0].contains(start)) 
                {
                    return i;
                }
            }
            return -1;
        }
                
        /* Determine destination row from Point object 'stop' */
        public int getDestRow(Point stop) {
            for (int i = 0; i < 5; i++) 
            {
                if (hitBoxes[i][1].contains(stop)) 
                {
                    return i;
                }
            }
            return -1;
        }
                
        public void handleMouseRelease(Point stop) 
        {
            /* Determine destination row from 'stop' Point */
            destRow = getDestRow(stop);

            /* Verify destRow and source card is valid */
            if (destRow != -1 && destRow <= 4 && source != null) 
            {
                /* Determine if move is valid */
                if (validPlayStackMove(source, destRow)) 
                {
                    Card temp = null;
                    
                    /* Check sourceRow and destRow combo is valid */
                    if (sourceRow < 4 && destRow != 4) 
                    {
                        /* Move card from Left pile to Right pile */
                        temp = L_piles.elementAt(sourceRow).elementAt(0);
                        L_piles.elementAt(sourceRow).remove(0);
                        R_piles.elementAt(destRow).add(temp);
                    } 
                    else if (sourceRow == 4 && destRow == 4) 
                    {
                        /* Deck flip */
                        incDeckPtr();
                    } 
                    else if (destRow != 4) 
                    {
                        /* Move card from Left pile to Right pile */
                        temp = L_piles.elementAt(sourceRow).elementAt(deckPtr);
                        L_piles.elementAt(sourceRow).remove(deckPtr);
                        R_piles.elementAt(destRow).add(temp);
                        decDeckPtr();
                    }
                }
            } 
            /* Invalid parameters */
            else 
            {
                destRow = -1;
            }
        }
        
        /* Decrement deck pointer */
        public void decDeckPtr()
        {
            /* Get deck size */
            int size = L_piles.elementAt(4).size();
            
            /* Don't decrease past zero */
            if(this.deckPtr != 0)
            {
                this.deckPtr--;
            }
            /* Make sure there's enough cards to set deckPtr to 2 */
            else if(size >= 3)
            {
                this.deckPtr = 2;
            }
            /* deckPtr == 0 and deck size is <= 3, set it to end of deck  */
            else
            {
                this.deckPtr = L_piles.elementAt(4).size() - 1;
            }
        }
        
        /* Increment deckPtr */
        public void incDeckPtr()
        {
            /* Get deck size */
            int size = L_piles.elementAt(4).size();
            
            /* Make sure not to set deckPtr out of bounds */
            if((deckPtr + 3) <= (size - 1))
            {
                this.deckPtr += 3;
            }
            /* Reset deckPtr to beginning of deck */
            else if(deckPtr == (size - 1) && size >= 3)
            {
                this.deckPtr = 2;
            }
            /* Deck size is <= 3, set it to end of deck */
            else
            {
                this.deckPtr = L_piles.elementAt(4).size() - 1;
            }
        }

        /* Shuffle temp deck */
	public void shuffle()
	{
            Vector<Card> v = new Vector<Card>();
            while (!this.tempDeck.isEmpty())
            {
		v.add(this.tempDeck.remove(0));
            }
            while (!v.isEmpty())
            {
		Card c = v.elementAt((int) (Math.random() * v.size()));
		this.tempDeck.add(c);
		v.removeElement(c);
            }
	}

        public void restart()
        {
            
        }
        
	@Override
	public boolean contains(Point p)
	{
		Rectangle rect = new Rectangle(_x, _y, Card.CARD_WIDTH + 10, Card.CARD_HEIGHT * 3);
		return (rect.contains(p));
	}

	public void setXY(int x, int y)
	{
		_x = x;
		_y = y;
		setBounds(_x, _y, Card.CARD_WIDTH + 500, Card.CARD_HEIGHT + 500);
	}

	public Point getXY()
	{
		return new Point(_x, _y);
	}

	@Override
	protected void paintComponent(Graphics g)
	{
            super.paintComponent(g);
            removeAll();
            
            /* Cycle through the four Left piles */
            for (int i = 0; i < 4; i++) 
            {
                Vector<Card> outer = L_piles.elementAt(i);
                /* Cycle through each left pile */
                for (int j = 0; j < outer.size(); j++) 
                {
                    Card c = outer.elementAt(j);
                    
                    /* If it's the top, set it faceup */
                    if (j == 0) 
                    {
                        c.setFaceup();
                    }
                    
                    /* Set paint coordinates for card */
                    c.setXY(new Point(pilePos[i].x + j * SPREAD, pilePos[i].y));
                    add(Board.moveCard(c, pilePos[i].x + j * SPREAD, pilePos[i].y));
                    c.setWhereAmI(getXY());
                }
                
                Vector<Card> rp = R_piles.elementAt(i);
                /* Cycle through each right pile */
                for (int j = rp.size(); j > 0 ; j--) 
                {
                    /* Set them faceup */
                    Card c = rp.elementAt(j-1);
                    c.setFaceup();

                    /* Set Pain coordinates for card */
                    c.setXY(new Point(pilePos[i].x + ROW_START_X + j*(SPREAD*2), pilePos[i].y));
                    add(Board.moveCard(c, pilePos[i].x + ROW_START_X + j*(SPREAD*2), pilePos[i].y));
                    c.setWhereAmI(getXY());
                }
            }    
            
            int spreadVar = 0;

            /* If deck isn't empty, show card at deckPtr */
            if(!L_piles.elementAt(4).isEmpty())
            {
                Card c_temp = L_piles.elementAt(4).elementAt(deckPtr);
                c_temp.setFaceup();
                c_temp.setXY(new Point(pilePos[4].x, pilePos[4].y));
                add(Board.moveCard(c_temp, pilePos[4].x, pilePos[4].y));
                c_temp.setWhereAmI(getXY());
            }
            
            /* Don't actually know why I need this outer loop,
               but it doesn't work without it. */
            for(int i = 0; i < 5; i++)
            {
                /* Cycle through deck pile */
                for (int j = 0; j < L_piles.elementAt(4).size(); j++) 
                {
                    Card c = L_piles.elementAt(4).elementAt(j);
                    
                    /* Set spread variable if it's one card before deckPtr */
                    if (j == deckPtr - 1) 
                    {
                        spreadVar = 80;
                        c.setXY(new Point(pilePos[i].x + (j+1) * SPREAD, pilePos[i].y));
                        add(Board.moveCard(c, pilePos[i].x + (j+1) * SPREAD, pilePos[i].y));
                        c.setWhereAmI(getXY());
                        
                        //c.setFaceup();
                        c.setFacedown();
                    } 
                    /* Spreads deck for single card */
                    else if(j == 0 && deckPtr == 0)
                    {
                        spreadVar = 80;
                    }
                    /* Paint deck card */
                    else if(j != deckPtr)
                    {
                        //c.setFaceup();
                        c.setFacedown();
                        c.setXY(new Point(pilePos[i].x + spreadVar + (j + 1) * SPREAD, pilePos[i].y));
                        add(Board.moveCard(c, pilePos[i].x + spreadVar + (j + 1) * SPREAD, pilePos[i].y));
                        c.setWhereAmI(getXY());
                    }
                }
                spreadVar = 0;
            }    
                
	}
}