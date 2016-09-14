/*!******************************************************************************
  * @file    Card.java
  * @author  Mark Banchy
  * @date    May 20, 2016
  * @brief   Card class for Java GUI
  *****************************************************************************/
package oiabm.gui;

import oiabm.core.Card;
import oiabm.core.CardSuit;
import oiabm.core.CardValue;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Rectangle;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import javax.swing.JPanel;

public class GuiCard extends JPanel {
    private Card card;
    private Boolean isFaceUp;
    private final Point location; // location relative to container
    private Point whereAmI; // used to create absolute position rectangle for contains  TODO : there has to be a better name for this, mark
    private int positionX; // used for relative positioning within CardStack Container
    private int positionY;

    final static public int CARD_HEIGHT = 97;
    final static public int CARD_WIDTH = 73;

    final BufferedImage image;
    final BufferedImage back;

    public GuiCard(CardSuit suit, CardValue value) throws IOException {
        this.card = new Card(suit, value);
        isFaceUp = false;
        location = new Point();
        positionX = 0;
        positionY = 0;
        location.x = positionX;
        location.y = positionY;
        whereAmI = new Point();
        File imageFile = new File("cards", value + "-" + suit + ".gif");
        this.image = ImageIO.read(imageFile);
        imageFile = new File("cards", "b.gif");
        this.back = ImageIO.read(imageFile);
    }

    GuiCard() throws IOException {
        this.card = new Card(CardSuit.getRandom(), CardValue.getRandom());
        isFaceUp = false;
        location = new Point();
        positionX = 0;
        positionY = 0;
        location.x = positionX;
        location.y = positionY;
        whereAmI = new Point();
        File imageFile = new File("cards", card.getValue() + "-" + card.getSuit() + ".gif");
        this.image = ImageIO.read(imageFile);
        imageFile = new File("cards", "b.gif");
        this.back = ImageIO.read(imageFile);
    }

    public CardSuit getSuit() {
        return card.getSuit();
    }

    public CardValue getValue() {
        return card.getValue();
    }

    public void setWhereAmI(Point p) {
        whereAmI = p;
    }

    public Point getWhereAmI() {
        return whereAmI;
    }

    public Point getPosition() {
        return new Point(positionX, positionY);
    }

    public Boolean getFaceStatus() {
        return isFaceUp;
    }

    public void setPosition(Point p) {
        positionX = p.x;
        positionY = p.y;
    }

    public GuiCard setFaceUp() {
        isFaceUp = true;
        return this;
    }

    public GuiCard setFaceDown() {
        isFaceUp = false;
        return this;
    }

    @Override
    public boolean contains(Point p) {
        Rectangle rect = new Rectangle(whereAmI.x, whereAmI.y, GuiCard.CARD_WIDTH, GuiCard.CARD_HEIGHT);
        return (rect.contains(p));
    }

    @Override
    public void paintComponent(Graphics g) {
        if (isFaceUp) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, this);
        } else {
            super.paintComponent(g);
            g.drawImage(back, 0, 0, this);
        }
    }

}