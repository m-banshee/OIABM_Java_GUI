/*!******************************************************************************
  * @file    Board.java
  * @author  Mark Banchy
  * @date    May 20, 2016
  * @brief   Board class for java "Once In A Blue Moon" solitaire variant.\n
  *          Designed to contain a solitaire class, in this case BlueMoon.
  *****************************************************************************/

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Board
{
	// CONSTANTS
	public static final int TABLE_HEIGHT = Card.CARD_HEIGHT * 6;
	public static final int TABLE_WIDTH = (Card.CARD_WIDTH * 8) + 100;
	public static final int NUM_FINAL_DECKS = 4;
	public static final int NUM_PLAY_DECKS = 4;

	public static final Point DECK_POS = new Point(5, 5);

	private static BlueMoon deck;

	// GUI COMPONENTS (top level)
	private static final JFrame frame = new JFrame("Once In A Blue Moon");
	protected static final JPanel table = new JPanel();

	// moves a card to abs location within a component
	protected static Card moveCard(Card c, int x, int y)
	{
		c.setBounds(new Rectangle(new Point(x, y), new Dimension(Card.CARD_WIDTH, Card.CARD_HEIGHT)));
		c.setXY(new Point(x, y));
		return c;
	}
        
       	/*
	 * This class handles all of the logic of moving the Card components as well
	 * as the game logic. This determines where Cards can be moved according to
	 * the rules of OIABM.
	 */
	private static class CardMovementManager extends MouseAdapter
	{
           
                MouseEvent startE, stopE;
                boolean released = false;
                private Timer timer = new Timer(300, new ActionListener() 
                {
                    @Override
                    public void actionPerformed(ActionEvent e) 
                    {
                        if(released)
                        {
                            // timer has gone off, so treat as a single click
                            System.out.println("single!");
                            deck.handleMousePress(startE.getPoint());
                            deck.handleMouseRelease(startE.getPoint());
                        }
                        timer.stop();
                        table.repaint();

                    }
                });
            
		@Override
		public void mousePressed(MouseEvent e)
		{
                    startE = e;
                    released = false;
                    if(timer.isRunning())
                    {
                        timer.stop();
                        System.out.println("double click!");
                        deck.handleDoubleClick(e.getPoint());
                    }
                    else
                    {
                        deck.updateSourceCard(e.getPoint());
                        timer.restart();
                    }
                    table.repaint();
		}
                
		@Override
		public void mouseReleased(MouseEvent e)
		{
                    released = true;
                    if (!timer.isRunning())
                    {
                        stopE = e;
                        if(deck.isDragMove(startE.getPoint(), e.getPoint()))
                        {
                            deck.handleMouseRelease(e.getPoint());
                        }
                    }
                    table.repaint();
		}
	} 

	private static void playNewGame()
	{
		deck = new BlueMoon(); // deal 52 cards

		table.removeAll();
                table.add(deck);
                
		table.repaint();
	}

	public static void main(String[] args)
	{
		Container contentPane;
                
		frame.setSize(TABLE_WIDTH, TABLE_HEIGHT);

		table.setLayout(null);
		table.setBackground(new Color(0, 180, 0));

		contentPane = frame.getContentPane();
		contentPane.add(table);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		playNewGame();

		table.addMouseListener(new CardMovementManager());
		table.addMouseMotionListener(new CardMovementManager());

		frame.setVisible(true);
	}
}