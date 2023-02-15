import Heroes;
import Coins;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {

    private final int SCREEN_WIDTH = 800;
    private final int SCREEN_HEIGHT = 600;
    private final int MAP_WIDTH = 10000;
    private final int MAP_HEIGHT = 10000;
    private final int UNIT_SIZE = 50;
    private final int DELAY = 1;
    private final int MIN_NUM_OF_COINS = 10000;
    private Timer timer;
    private Random random;
    private int level = 0;

    private Snake hero;
    private ArrayList<Snake> enemies;
    private ArrayList<Coin> coins;
    private boolean[][] hashTableCoins;

    Color resultColor;
    private int previousResult;
    
    enum GameState {NEW_HIGH_SCORE, LOST, PLAY, START}
    private GameState gamestate = GameState.START;

    private int instructionPeriod = 0;

    public GamePanel(){
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.gray);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        this.timer = new Timer(DELAY, this);
        timer.start();
    }

    public void prepareGame()
    {
        this.random = new Random();
        hero = new SnakeHero(MAP_WIDTH / 2, MAP_HEIGHT / 2, Color.magenta, Color.GRAY, Color.blue);
        prepareCoins();

        resultColor = Color.lightGray;
        previousResult = hero.getLength();
    }

    public void startGame() {
        gamestate = GameState.PLAY;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {

        switch(gamestate) {

            case PLAY:
                int x_left = Math.max(hero.positionX - SCREEN_WIDTH / 2, 0);
                int x_right = Math.min(hero.positionX + SCREEN_WIDTH / 2, MAP_WIDTH);
                int y_up = Math.max(hero.positionY - SCREEN_HEIGHT / 2, 0);
                int y_down = Math.min(hero.positionY + SCREEN_HEIGHT / 2, MAP_HEIGHT);

                if (x_right == MAP_WIDTH) x_left = MAP_WIDTH - SCREEN_WIDTH;
                if (y_down == MAP_HEIGHT) y_up = MAP_HEIGHT - SCREEN_HEIGHT;
                if (x_right < SCREEN_WIDTH) x_right = SCREEN_WIDTH;
                if (y_down < SCREEN_HEIGHT) y_down = SCREEN_HEIGHT;

                drawMap(x_left, x_right, y_up, y_down, g);
                drawCoins(x_left, x_right, y_up, y_down, g);
                drawHero(x_left, x_right, y_up, y_down, g);
                drawResult(g);
                break;
            case LOST:
                drawMap(UNIT_SIZE, SCREEN_WIDTH + UNIT_SIZE, UNIT_SIZE, SCREEN_HEIGHT + UNIT_SIZE, g);
                break;
            case START:
                drawMap(UNIT_SIZE, SCREEN_WIDTH + UNIT_SIZE, UNIT_SIZE, SCREEN_HEIGHT + UNIT_SIZE, g);
                g.setColor(Color.BLUE);
                Font title = new Font ("Courier New", Font.BOLD, 65);
                g.setFont(title);
                g.drawString("Snake Game", 200, 150);

                Snake snake = new SnakeHero(MAP_WIDTH / 2, MAP_HEIGHT / 2, Color.magenta,
                        Color.GRAY, Color.blue);
                snake.draw(g, SCREEN_WIDTH / 2 - snake.getThickness() / 2,
                        SCREEN_HEIGHT / 2 - snake.getThickness() / 2);

                drawInstruction(g);
                drawLevels(g);
                drawHighScores(g);

                break;
            case NEW_HIGH_SCORE:
                drawMap(UNIT_SIZE, SCREEN_WIDTH + UNIT_SIZE, UNIT_SIZE, SCREEN_HEIGHT + UNIT_SIZE, g);
                break;
        }
    }

    public void drawInstruction(Graphics g)
    {
        if(instructionPeriod % 80 < 40) {
            g.setColor(Color.cyan);
            Font instruction = new Font("Courier New", Font.BOLD, 35);
            g.setFont(instruction);
            g.drawString("Press space to start game", 140, 450);
        }
        else if(instructionPeriod == 79) instructionPeriod = 0;
        instructionPeriod++;
    }

    public void drawLevels(Graphics g)
    {
        g.setColor(new Color(173, 216, 230));
        g.fill3DRect(112, 218, 150, 150, true);

        g.setColor(new Color(160, 180, 200));
        g.fillRect(112, 248 + level * 30, 149, 30);

        g.setFont(new Font ("Console", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("Choose level \u25B2 \u25BC", 120, 240);
        g.setFont(new Font ("Console", Font.PLAIN, 13));
        g.drawString("Easy", 120, 268);
        g.drawString("Medium", 120, 298);
        g.drawString("Hard", 120, 328);
        g.drawString("Deadly", 120, 358);

        g.setColor(Color.yellow);
        g.setFont(new Font ("Console", Font.PLAIN, 15));
        g.drawString("\u2605", 190, 270);
        g.drawString("\u2605 \u2605", 190, 300);
        g.drawString("\u2605 \u2605 \u2605", 190, 330);
        g.drawString("\u2605 \u2605 \u2605 \u2605", 190, 360);
    }

    public void drawHighScores(Graphics g)
    {
        g.setColor(new Color(173, 216, 230));
        g.fill3DRect(SCREEN_WIDTH - 262, 218, 150, 150, true);

        g.setFont(new Font ("Console", Font.PLAIN, 14));
        g.setColor(Color.WHITE);
        g.drawString("Top 3 scores", SCREEN_WIDTH - 230, 240);
        g.setFont(new Font ("Console", Font.PLAIN, 13));
        g.drawString("1. ", SCREEN_WIDTH - 256, 268);
        g.drawString("2. ", SCREEN_WIDTH - 256, 298);
        g.drawString("3. ", SCREEN_WIDTH - 256, 328);
        g.drawString("4. ", SCREEN_WIDTH - 256, 358);
    }

    private void drawMap(int x_left, int x_right, int y_up, int y_down, Graphics g)
    {
        for(int x = 0; x < MAP_WIDTH; x += UNIT_SIZE)
        {
            int xDiff = x - x_left;
            if(xDiff < -UNIT_SIZE || xDiff > SCREEN_WIDTH) continue;
            for(int y = 0; y < MAP_HEIGHT; y += UNIT_SIZE)
            {
                int yDiff = y - y_up;
                if(yDiff < -UNIT_SIZE || yDiff > SCREEN_HEIGHT) continue;

                if(((x / UNIT_SIZE) % 2 == 0 && (y / UNIT_SIZE) % 2 == 0) ||
                        ((x / UNIT_SIZE) % 2 == 1 && (y / UNIT_SIZE) % 2 == 1))
                    g.setColor(Color.gray);
                else g.setColor(Color.white);

                g.fillRect(Math.max(xDiff, 0), Math.max(yDiff, 0), Math.min(xDiff + UNIT_SIZE, UNIT_SIZE),
                        Math.min(yDiff + UNIT_SIZE, UNIT_SIZE));
            }
        }

        g.setColor(Color.orange);
        if(x_left <= UNIT_SIZE)
            g.fillRect(0, 0, UNIT_SIZE - x_left, SCREEN_HEIGHT);
        else if (x_right >= MAP_WIDTH - UNIT_SIZE) {
            int width = x_right - (MAP_WIDTH - UNIT_SIZE);
            g.fillRect(SCREEN_WIDTH - width, 0, width, SCREEN_HEIGHT);
        }

        if(y_up <= UNIT_SIZE)
            g.fillRect(0, 0, SCREEN_WIDTH, UNIT_SIZE - y_up);
        else if (y_down >= MAP_HEIGHT - UNIT_SIZE) {
            int height = y_down - (MAP_HEIGHT - UNIT_SIZE);
            g.fillRect(0, SCREEN_HEIGHT - height, SCREEN_WIDTH, height);
        }
    }

    private void drawCoins(int x_left, int x_right, int y_up, int y_down, Graphics g)
    {
        coins.forEach(c -> c.draw(x_left, x_right, y_up, y_down, g));
    }

    private void drawHero(int x_left, int x_right, int y_up, int y_down, Graphics g)
    {
        int x = SCREEN_WIDTH / 2 - hero.getThickness() / 2;
        int y = SCREEN_HEIGHT / 2 - hero.getThickness() / 2;
        if(x_left == 0)
            x = hero.positionX;
        if(x_right == MAP_WIDTH)
            x = SCREEN_WIDTH - (x_right - hero.positionX);
        if(y_up == 0)
            y = hero.positionY;
        if(y_down == MAP_HEIGHT)
            y = SCREEN_HEIGHT - (y_down - hero.positionY);
        hero.draw(g, x, y);
    }

    public void drawEnemies(int x_left, int x_right, int y_up, int y_down, Graphics g)
    {

    }

    public void drawResult(Graphics g)
    {
        int result = hero.getLength();
        Font font = new Font ("Courier New", Font.BOLD, 50);
        g.setFont(font);
        if(result - previousResult == GoldCoin.ADDITIONAL_LENGTH)
            resultColor = Color.yellow;
        else if(result - previousResult == StandardCoin.ADDITIONAL_LENGTH)
            resultColor = Color.green;
        else if(result < previousResult)
            resultColor = Color.black;
        g.setColor(resultColor);
        previousResult = result;

        String text = Integer.toString(result);
        var textWidth = g.getFontMetrics().stringWidth(text);
        int x = (SCREEN_WIDTH / 2) - (textWidth / 2);
        int y = 60;
        g.drawString(text, x, y);
    }
    public void moveEnemies()
    {

    }

    public void checkCollisions()
    {
        if(hero.checkCollisions(coins, hashTableCoins, UNIT_SIZE, MAP_WIDTH, MAP_HEIGHT))
            endGame();
    }

    private Coin generateCoin(Point p)
    {
        int r = random.nextInt(100);
        if(r < 75) return new StandardCoin(p);
        if(r < 90) return new WrongCoin(p);
        return new GoldCoin(p);
    }

    private void addCoin(){
        int x, y;
        do {
            x = random.nextInt(MAP_WIDTH - 2 * UNIT_SIZE) + UNIT_SIZE;
            y = random.nextInt(MAP_HEIGHT - 2 * UNIT_SIZE) + UNIT_SIZE;
        }while(hashTableCoins[x][y]);
        hashTableCoins[x][y] = true;
        Coin coin = generateCoin(new Point(x, y));
        int xx = coin.getPoint().x + coin.getSize();
        int yy = coin.getPoint().y + coin.getSize();
        if(xx > MAP_WIDTH - UNIT_SIZE)
            coin.setPoint(new Point(x - (xx - (MAP_WIDTH - UNIT_SIZE)), y));
        if(yy > MAP_HEIGHT - UNIT_SIZE)
            coin.setPoint(new Point(x, y - (yy - (MAP_HEIGHT - UNIT_SIZE))));
        coins.add(coin);
    }

    private void prepareCoins(){

        hashTableCoins = new boolean[MAP_WIDTH][MAP_HEIGHT];
        coins = new ArrayList<>();
        for(int i = 0; i < MIN_NUM_OF_COINS; i++)
            addCoin();
    }

    public void updateCoins()
    {
        if(coins.size() >= MIN_NUM_OF_COINS) return;
        for(int i = MIN_NUM_OF_COINS - coins.size(); i > 0; --i)
            addCoin();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(gamestate == GameState.PLAY)
        {
            hero.move();
            moveEnemies();
            checkCollisions();
            updateCoins();
        }
        repaint();
    }

    private void endGame(){
        timer.stop();
        gamestate = GameState.LOST;
        Result result = new Result(hero.getLength(), level);

    }

    public class MyKeyAdapter extends KeyAdapter{

        @Override
        public void keyPressed(KeyEvent event)
        {
            if(gamestate != GameState.PLAY && event.getKeyCode() == KeyEvent.VK_SPACE){
                prepareGame();
                startGame();
            }

            if(gamestate == GameState.PLAY)
            {
                switch(event.getKeyCode())
                {
                    case KeyEvent.VK_LEFT:
                        if(hero.direction != Snake.Direction.RIGHT)
                            hero.direction = Snake.Direction.LEFT;
                        break;
                    case KeyEvent.VK_RIGHT:
                        if(hero.direction != Snake.Direction.LEFT)
                            hero.direction = Snake.Direction.RIGHT;
                        break;
                    case KeyEvent.VK_UP:
                        if(hero.direction != Snake.Direction.DOWN)
                            hero.direction = Snake.Direction.UP;
                        break;
                    case KeyEvent.VK_DOWN:
                        if(hero.direction != Snake.Direction.UP)
                            hero.direction = Snake.Direction.DOWN;
                        break;
                }
            }

            if(gamestate == GameState.START){
                switch(event.getKeyCode())
                {
                    case KeyEvent.VK_UP:
                        level = --level < 0 ? 3 : level;
                        break;
                    case KeyEvent.VK_DOWN:
                        level = ++level > 3 ? 0 : level;
                        break;
                }
            }

        }
    }
}
