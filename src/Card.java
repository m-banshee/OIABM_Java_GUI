/*!******************************************************************************
  * @file    Card.java
  * @author  Mark Banchy
  * @date    May 20, 2016
  * @brief   Card class for Java GUI
  *****************************************************************************/

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import javax.swing.JPanel;

class Card extends JPanel
{
	public static enum Value
	{
		ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING;
                
            public static Value getRandom() 
            {
                return values()[(int) (Math.random() * values().length)];
            }
	}

	public static enum Suit
	{
		SPADES, CLUBS, DIAMONDS, HEARTS;
                
            public static Suit getRandom() 
            {
                return values()[(int) (Math.random() * values().length)];
            }
	}

	private Suit _suit;

	private Value _value;

	private Boolean _faceup;

	private final Point _location; // location relative to container

	private Point whereAmI; // used to create abs postion rectangle for contains

	private int x; // used for relative positioning within CardStack Container
	private int y;

	final static public int CARD_HEIGHT = 97;

	final static public int CARD_WIDTH = 73;
        
        final BufferedImage image;
        final BufferedImage back;

	public Card(Suit suit, Value value) throws IOException
	{
		_suit = suit;
		_value = value;
		_faceup = false;
		_location = new Point();
		x = 0;
		y = 0;
		_location.x = x;
		_location.y = y;
		whereAmI = new Point();
                File imageFile = new File("cards", value + "-" + suit + ".gif");
                this.image = ImageIO.read(imageFile);
                imageFile = new File("cards", "b.gif");
                this.back = ImageIO.read(imageFile);
	}

	Card() throws IOException
	{
		_suit = Card.Suit.getRandom();
		_value = Card.Value.getRandom();
		_faceup = false;
		_location = new Point();
		x = 0;
		y = 0;
		_location.x = x;
		_location.y = y;
		whereAmI = new Point();
                File imageFile = new File("cards", _value + "-" + _suit + ".gif");
                this.image = ImageIO.read(imageFile);
                imageFile = new File("cards", "b.gif");
                this.back = ImageIO.read(imageFile);
	}

	public Suit getSuit()
	{
		return this._suit;
	}

	public Value getValue()
	{
		return this._value;
	}

	public void setWhereAmI(Point p)
	{
		whereAmI = p;
	}

	public Point getWhereAmI()
	{
		return whereAmI;
	}

	public Point getXY()
	{
		return new Point(x, y);
	}

	public Boolean getFaceStatus()
	{
		return _faceup;
	}

	public void setXY(Point p)
	{
		x = p.x;
		y = p.y;
	}

	public void setSuit(Suit suit)
	{
		_suit = suit;
	}

	public void setValue(Value value)
	{
		_value = value;
	}

	public Card setFaceup()
	{
		_faceup = true;
		return this;
	}

	public Card setFacedown()
	{
		_faceup = false;
		return this;
	}

	@Override
	public boolean contains(Point p)
	{
		Rectangle rect = new Rectangle(whereAmI.x, whereAmI.y, Card.CARD_WIDTH, Card.CARD_HEIGHT);
		return (rect.contains(p));
	}

	@Override
	public void paintComponent(Graphics g)
	{
            if (_faceup)
            {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, this);
            }
            else
            {
                super.paintComponent(g);
                g.drawImage(back, 0, 0, this);
            }
        }

}