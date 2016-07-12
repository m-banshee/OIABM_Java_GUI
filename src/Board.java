/*!******************************************************************************
  * @file    Board.java
  * @author  Mark Banchy
  * @date    May 20, 2016
  * @brief   Board class for java "Once In A Blue Moon" solitaire variant.\n
  *          Designed to contain a solitaire class, in this case BlueMoon.
  *****************************************************************************/

import oiabm.gui.GuiCard;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

public class Board implements ItemListener {
    // CONSTANTS
    public static final int TABLE_HEIGHT = GuiCard.CARD_HEIGHT * 6;
    public static final int TABLE_WIDTH = (GuiCard.CARD_WIDTH * 8) + 100;
    public static final int NUM_FINAL_DECKS = 4;
    public static final int NUM_PLAY_DECKS = 4;

    public static final Point DECK_POS = new Point(5, 5);

    private static BlueMoon deck;

    // GUI COMPONENTS (top level)
    public static final JFrame frame = new JFrame("Once In A Blue Moon");
    protected static final JPanel table = new JPanel();

    private class undoAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            deck.popPrevMove();
            table.removeAll();
            table.add(deck);
            table.repaint();
        }
    }

    private class newGame extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            playNewGame();
        }
    }

    public JMenuBar makeMenu() {
        table.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "undo");
        table.getActionMap().put("undo", new undoAction());

        table.getInputMap().put(KeyStroke.getKeyStroke("N"), "New Game");
        table.getActionMap().put("New Game", new newGame());

        JMenuBar menuBar;
        JMenu menu;
        JMenuItem menuItem;

        menuBar = new JMenuBar();
        menu = new JMenu("Game");

        menu.setMnemonic(KeyEvent.VK_G);
        menu.getAccessibleContext().setAccessibleDescription(
                "The only menu in this program that has menu items");

        menuBar.add(menu);
        menuItem = new JMenuItem("New Game",
                KeyEvent.VK_N);
        menu.add(menuItem);
        menuItem.addActionListener(new newGame());

        menuBar.add(menu);
        menuItem = new JMenuItem("Undo Move",
                KeyEvent.VK_U);
        menu.add(menuItem);
        menuItem.addActionListener(new undoAction());

        menu = new JMenu("Help");
        menuBar.add(menu);

        frame.setJMenuBar(menuBar);
        return menuBar;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        System.out.println("item state change");
    }


    // moves a card to abs location within a component
    protected static GuiCard moveCard(GuiCard c, int x, int y) {
        c.setBounds(new Rectangle(new Point(x, y), new Dimension(GuiCard.CARD_WIDTH, GuiCard.CARD_HEIGHT)));
        c.setPosition(new Point(x, y));
        return c;
    }

    /*
* This class handles all of the logic of moving the gui.GuiCard components as well
* as the game logic. This determines where Cards can be moved according to
* the rules of OIABM.
*/
    private static class CardMovementManager extends MouseAdapter {
        MouseEvent startE, stopE;
        boolean released = false;
        private Timer timer = new Timer(200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (released) {
                    // timer has gone off, so treat as a single click
                    deck.handleMousePress(startE.getPoint());
                    deck.handleMouseRelease(startE.getPoint());
                }
                timer.stop();
                table.repaint();
            }
        });

        @Override
        public void mousePressed(MouseEvent e) {
            startE = e;
            released = false;

            if (timer.isRunning()) {
                timer.stop();
                deck.handleDoubleClick(e.getPoint());
            } else {
                deck.updateSourceCard(e.getPoint());
                timer.restart();
            }

            table.repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            released = true;
            if (!timer.isRunning()) {
                stopE = e;
                if (deck.isDragMove(startE.getPoint(), e.getPoint())) {
                    deck.handleMouseRelease(e.getPoint());
                }
            }

            if (deck.winCheck()) {
                deck.handleWin();
            }

            table.repaint();
        }
    }

    private static void playNewGame() {
        deck = new BlueMoon();

        table.removeAll();
        table.add(deck);

        table.repaint();
    }

    public static void main(String[] args) {
        Container contentPane;

        Board demo = new Board();
        frame.setJMenuBar(demo.makeMenu());

        frame.setSize(TABLE_WIDTH, TABLE_HEIGHT);
        table.setSize(TABLE_WIDTH, TABLE_HEIGHT);

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