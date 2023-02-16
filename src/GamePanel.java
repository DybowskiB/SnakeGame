import Coins.Coin;
import Coins.GoldCoin;
import Coins.StandardCoin;
import Coins.WrongCoin;
import Heroes.Snake;
import Heroes.SnakeEnemy;
import Heroes.SnakeHero;
import Objects.Bomb;
import Objects.MapObject;
import org.w3c.dom.*;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class GamePanel extends JPanel implements ActionListener {

    private final int SCREEN_WIDTH = 800;
    private final int SCREEN_HEIGHT = 600;
    private final int MAP_WIDTH = 5000;
    private final int MAP_HEIGHT = 5000;
    private final int UNIT_SIZE = 50;
    private final int MIN_NUM_OF_COINS = 5000;
    private final int MIN_NUM_OF_BOMBS = 1000;
    private int numberOfBombs = 500;
    private final Timer timer;
    private Random random;
    private int level = 0;

    private Snake hero;
    private ArrayList<Snake> enemies;
    private int enemiesCount;
    private ArrayList<Coin> coins;
    private ArrayList<Bomb> bombs;
    private boolean[][] hashTableMapObjects;

    Color resultColor;
    private int previousResult;
    
    enum GameState {NEW_HIGH_SCORE, LOST, PLAY, START, PAUSE}
    private GameState gamestate = GameState.START;

    private int instructionPeriod = 0;
    private int moveMapPosition = 0;

    private int highScore;
    private String resultHistoryFileName = "./results.xml";

    Semaphore mutex;

    public GamePanel(){
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.gray);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        this.mutex = new Semaphore(1);

        final int DELAY = 10;
        this.timer = new Timer(DELAY, this);
        timer.start();

        ArrayList<Result> results = loadResults();
        results.sort(Collections.reverseOrder());
        if(results.size() > 0)
            highScore = results.get(0).getLength();
    }

    public void prepareGame()
    {
        this.random = new Random();
        hero = new SnakeHero(MAP_WIDTH / 2, MAP_HEIGHT / 2, Color.magenta, Color.GRAY, Color.blue);
        prepareCoins();
        if(level > 1) numberOfBombs = (level + 1) * MIN_NUM_OF_BOMBS;
        else numberOfBombs = MIN_NUM_OF_BOMBS;
        prepareBombs();
        enemiesCount = (level + 1) * 2;
        prepareEnemies();

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
                if (x_right <= SCREEN_WIDTH) x_right = SCREEN_WIDTH;
                if (y_down <= SCREEN_HEIGHT) y_down = SCREEN_HEIGHT;

                drawMap(x_left, y_up, g);
                drawBorders(x_left, x_right, y_up, y_down, g);
                drawCoins(x_left, x_right, y_up, y_down, g);
                drawBombs(x_left, x_right, y_up, y_down, g);
                drawHero(x_left, x_right, y_up, y_down, g);
                drawEnemies(x_left, y_up, g);
                drawResult(g);
                break;
            case LOST:
                drawMovingMap(g);
                g.setColor(new Color(220, 80, 80, 127));
                g.fillRoundRect(200, 80, 400, 330, 30, 30);
                g.setColor(new Color(255, 20, 20));
                Font info = new Font ("Courier New", Font.BOLD, 65);
                g.setFont(info);
                g.drawString("You lost", 245, 150);

                drawScore(g, hero.getLength());
                drawInstruction(g, "Press space to play again");
                drawSecondInstruction(g, "Press shift to return to menu");
                break;
            case START:
                drawMovingMap(g);
                g.setColor(Color.BLUE);
                Font title = new Font ("Courier New", Font.BOLD, 65);
                g.setFont(title);
                g.drawString("Snake Game", 200, 150);

                Snake snake = new SnakeHero(MAP_WIDTH / 2, MAP_HEIGHT / 2, Color.magenta,
                        Color.GRAY, Color.blue);
                snake.draw(g, SCREEN_WIDTH / 2 - snake.getThickness() / 2,
                        SCREEN_HEIGHT / 2 - snake.getThickness() / 2, mutex);

                drawInstruction(g, "Press space to start game");
                drawLevels(g);
                drawHighScores(g);

                break;
            case NEW_HIGH_SCORE:
                drawMovingMap(g);
                g.setColor(new Color(95, 192, 63, 127));
                g.fillRoundRect(190, 80, 420, 330, 30, 30);
                g.setColor(new Color(10, 130, 10));
                Font info2 = new Font ("Courier New", Font.BOLD, 65);
                g.setFont(info2);
                g.drawString("High score!", 205, 150);

                drawScore(g, hero.getLength());
                drawInstruction(g, "Press space to play again");
                drawSecondInstruction(g, "Press shift to return to menu");
                break;

            case PAUSE:
                drawMovingMap(g);
                g.setColor(new Color(200, 200, 200));
                Font info3 = new Font ("Courier New", Font.BOLD, 100);
                g.setFont(info3);
                g.drawString("Pause", 245, 200);
                drawInstruction(g, "Press space to restart game");
                drawSecondInstruction(g, "Press shift to return to menu");
        }
    }

    public void drawScore(Graphics g, int result)
    {
        // score
        g.setColor(new Color(230, 230, 230));
        Font font = new Font ("Courier New", Font.BOLD, 95);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);
        String text = Integer.toString(result);
        Rectangle rect = new Rectangle(200, 280, 400, 10);
        g.drawString( text, rect.x + (rect.width - metrics.stringWidth(text)) / 2, 280);

        // level stars
        g.setColor(Color.yellow);
        g.setFont(new Font ("Console", Font.BOLD, 80));
        String stars = "";
        for(int i = 0; i < level + 1; ++i) stars += "\u2605";
        switch(level){
            case 0:
                g.drawString(stars, 365, 380);
                break;
            case 1:
                g.drawString(stars, 330, 380);
                break;
            case 2:
                g.drawString(stars, 296, 380);
                break;
            default:
                g.drawString(stars, 260, 380);
                break;
        }
    }

    public void drawInstruction(Graphics g, String info)
    {
        if(instructionPeriod % 80 < 40) {
            g.setColor(Color.cyan);
            Font instruction = new Font("Courier New", Font.BOLD, 35);
            g.setFont(instruction);
            FontMetrics metrics = g.getFontMetrics(instruction);
            Rectangle rect = new Rectangle(0, 470, SCREEN_WIDTH, 10);
            g.drawString( info, rect.x + (rect.width - metrics.stringWidth(info)) / 2, 470);
        }
        else if(instructionPeriod == 79) instructionPeriod = 0;
        instructionPeriod++;
    }

    public void drawSecondInstruction(Graphics g, String info)
    {
        g.setColor(new Color(200, 200, 200));
        Font instruction = new Font("Courier New", Font.BOLD, 20);
        g.setFont(instruction);
        g.drawString(info, 225, 530);
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
        g.setFont(new Font ("Console", Font.BOLD, 13));
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
        ArrayList<Result> results = loadResults();
        results.sort(Collections.reverseOrder());

        g.setColor(new Color(173, 216, 230));
        g.fill3DRect(SCREEN_WIDTH - 262, 218, 150, 150, true);

        // Results
        g.setFont(new Font ("Console", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("Top 3 scores", SCREEN_WIDTH - 230, 240);
        g.setFont(new Font ("Console", Font.BOLD, 13));
        if(results.size() > 0)
            highScore = results.get(0).getLength();
        for(int i = 0; i < results.size() && i < 3; ++i)
        {
            g.drawString((i + 1) + ".  " + results.get(i).getLength(), SCREEN_WIDTH - 250, 268 + i * 30);
        }

        // Average
        int average = 0;
        for(var r : results) average += r.getLength();
        if(results.size() > 0) average = average / results.size();
        else average = 0;
        g.setFont(new Font ("Console", Font.BOLD, 14));
        g.drawString("Average: " + average, SCREEN_WIDTH - 250, 268 + 3 * 30);

        // Level stars
        g.setColor(Color.yellow);
        g.setFont(new Font ("Console", Font.PLAIN, 15));
        for(int i = 0; i < results.size() && i < 3; ++i)
        {
            String stars = "";
            for(int j = 0; j < results.get(i).getLevel() + 1; ++j) stars += " \u2605";
            g.drawString(stars, SCREEN_WIDTH - 190, 268 + i * 30);
        }
    }

    private void drawMap(int x_left, int y_up, Graphics g)
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
    }

    public void drawMovingMap(Graphics g)
    {
        int y = MAP_HEIGHT - SCREEN_HEIGHT - moveMapPosition;
        drawMap(UNIT_SIZE, y, g);
        ++moveMapPosition;
        if(moveMapPosition == 2 * UNIT_SIZE) moveMapPosition = 0;
    }

    public void drawBorders(int x_left, int x_right, int y_up, int y_down, Graphics g)
    {
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
        coins.parallelStream().forEach(c -> c.draw(x_left, x_right, y_up, y_down, g, mutex));
    }

    private void drawBombs(int x_left, int x_right, int y_up, int y_down, Graphics g)
    {
        bombs.parallelStream().forEach(c -> c.draw(x_left, x_right, y_up, y_down, g, mutex));
    }

    int x, y;
    private void drawHero(int x_left, int x_right, int y_up, int y_down, Graphics g)
    {
        x = SCREEN_WIDTH / 2 - hero.getThickness() / 2;
        y = SCREEN_HEIGHT / 2 - hero.getThickness() / 2;
        if(x_left == 0)
            x = hero.positionX - hero.getThickness() / 2;
        if(x_right == MAP_WIDTH)
            x = SCREEN_WIDTH - (x_right - hero.positionX) - hero.getThickness() / 2;
        if(y_up == 0)
            y = hero.positionY - hero.getThickness() / 2;
        if(y_down == MAP_HEIGHT)
            y = SCREEN_HEIGHT - (y_down - hero.positionY) - hero.getThickness() / 2;
        Thread thread = new Thread(() -> hero.draw(g, x, y, mutex));
        thread.start();
        try {thread.join();}
        catch(Exception e){e.printStackTrace();}
    }

    public void drawEnemies(int x_left, int y_up, Graphics g)
    {
        enemies.parallelStream().forEach(r -> r.draw(g, x_left, y_up, mutex));
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
        enemies.parallelStream().forEach(Snake::move);
    }

    public void checkCollisions()
    {
        if(hero.checkCollisions(coins, hashTableMapObjects, UNIT_SIZE, MAP_WIDTH, MAP_HEIGHT, enemies, bombs))
            endGame();
        for(int i = 0; i < enemies.size(); ++i)
        {
            Snake enemy = enemies.get(i);
            ArrayList<Snake> snakes = new ArrayList<>();
            snakes.add(hero);
            for(int j = 0; j < enemies.size(); ++j) {
                if(i != j)
                    snakes.add(enemies.get(j));
            }
            if(enemy.checkCollisions(coins, hashTableMapObjects, UNIT_SIZE, MAP_WIDTH, MAP_HEIGHT, snakes, bombs))
            {
                enemies.remove(enemy);
            }
        }
    }

    private Coin generateCoin(Point p)
    {
        int r = random.nextInt(100);
        if(r < 75) return new StandardCoin(p);
        if(r < 90) return new WrongCoin(p);
        return new GoldCoin(p);
    }

    private Point getFreePlace()
    {
        int x, y;
        do {
            x = random.nextInt(MAP_WIDTH - 2 * UNIT_SIZE) + UNIT_SIZE;
            y = random.nextInt(MAP_HEIGHT - 2 * UNIT_SIZE) + UNIT_SIZE;
        }while(hashTableMapObjects[x][y]);
        hashTableMapObjects[x][y] = true;
        return new Point(x, y);
    }

    private void setPoint(MapObject mapObject, Point point, int xx, int yy)
    {
        if(xx > MAP_WIDTH - UNIT_SIZE)
            mapObject.setPoint(new Point(point.x - (xx - (MAP_WIDTH - UNIT_SIZE)), point.y));
        if(yy > MAP_HEIGHT - UNIT_SIZE)
            mapObject.setPoint(new Point(point.x, point.y - (yy - (MAP_HEIGHT - UNIT_SIZE))));
    }
    private void addCoin(){
        Point point = getFreePlace();
        Coin coin = generateCoin(point);
        int xx = coin.getPoint().x + coin.getSize();
        int yy = coin.getPoint().y + coin.getSize();
        setPoint(coin, point, xx, yy);
        coins.add(coin);
    }

    private void addBomb()
    {
        Point point = getFreePlace();
        Bomb bomb = new Bomb(point);
        int xx = bomb.getPoint().x + Bomb.getSize();
        int yy = bomb.getPoint().y + Bomb.getSize();
        setPoint(bomb, point, xx, yy);
        bombs.add(bomb);
    }

    private void prepareCoins(){

        hashTableMapObjects = new boolean[MAP_WIDTH][MAP_HEIGHT];
        coins = new ArrayList<>();
        for(int i = 0; i < MIN_NUM_OF_COINS; i++)
            addCoin();
    }

    private void prepareBombs(){
        bombs = new ArrayList<>();
        for(int i = 0; i < numberOfBombs; i++)
            addBomb();
    }

    private void prepareEnemies()
    {
        enemies = new ArrayList<>();
        for(int i = 0; i < enemiesCount; ++i)
        {
            generateEnemy();
        }
    }

    private void generateEnemy() {
        int x = UNIT_SIZE + random.nextInt(MAP_WIDTH - 2 * UNIT_SIZE);
        int y = UNIT_SIZE + random.nextInt(MAP_HEIGHT - 2 * UNIT_SIZE);
        enemies.add(new SnakeEnemy(x, y, Color.BLACK, Color.WHITE, null, UNIT_SIZE,
                SCREEN_WIDTH, SCREEN_HEIGHT, MAP_WIDTH, MAP_HEIGHT, level));
    }

    public void updateCoins()
    {
        if(coins.size() >= MIN_NUM_OF_COINS) return;
        for(int i = MIN_NUM_OF_COINS - coins.size(); i > 0; --i)
            addCoin();
    }

    private void updateBombs()
    {
        if(bombs.size() >= numberOfBombs) return;
        for(int i = numberOfBombs - coins.size(); i > 0; --i)
            addBomb();
    }

    public void updateEnemies()
    {
        if(enemies.size() >= enemiesCount) return;
        for(int i = enemiesCount - enemies.size(); i > 0; --i)
        {
            generateEnemy();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(gamestate == GameState.PLAY)
        {
            if(hero != null && hero.getLength() <= 0) {
                hero.setLength(0);
                endGame();
            }
            if(hero != null) {
                Thread thread = new Thread(() -> hero.move());
                thread.start();
                try {thread.join();}
                catch(Exception exc){exc.printStackTrace();}
            }
            moveEnemies();
            checkCollisions();
            updateCoins();
            updateBombs();
            updateEnemies();
        }
        repaint();
    }

    private void endGame(){
        int currentResult = hero.getLength();
        if(currentResult > highScore) {
            gamestate = GameState.NEW_HIGH_SCORE;
            highScore = currentResult;
        }
        else {
            gamestate = GameState.LOST;
        }
        Result newResult = new Result(hero.getLength(), level);
        saveResult(newResult);
    }

    public void saveResult(Result result)
    {
        try {
            File file = new File(resultHistoryFileName);
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document;
            Element root;
            try {
                document = documentBuilder.parse(file);
                document.getDocumentElement().normalize();
                root = document.getDocumentElement();
            }
            catch(Exception e) {
                document = documentBuilder.newDocument();

                root = document.createElement("root");
                document.appendChild(root);
            }

            // result element
            Element res = document.createElement("result");
            root.appendChild(res);

            // level element
            Element level = document.createElement("level");
            level.appendChild(document.createTextNode(Integer.toString(result.getLevel())));
            res.appendChild(level);

            // length element
            Element length = document.createElement("length");
            length.appendChild(document.createTextNode(Integer.toString(result.getLength())));
            res.appendChild(length);

            // create the xml file
            //transform the DOM Object to an XML File
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(resultHistoryFileName));

            transformer.transform(domSource, streamResult);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Result> loadResults()
    {
        ArrayList<Result> results = new ArrayList<>();
        try
        {
            File file = new File(resultHistoryFileName);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("result");

            for (int itr = 0; itr < nodeList.getLength(); itr++)
            {
                Node node = nodeList.item(itr);
                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element element = (Element) node;

                    Node node1 = element.getElementsByTagName("level").item(0);
                    String level = node1.getTextContent();

                    Node node2 = element.getElementsByTagName("length").item(0);
                    String length = node2.getTextContent();

                    results.add(new Result(Integer.parseInt(length), Integer.parseInt(level)));
                }
            }
        }
        catch(Exception e){
            System.out.println("There is no result history file or there is problem with parsing results.xml.");
        }
        return results;
    }

    public class MyKeyAdapter extends KeyAdapter{

        @Override
        public void keyPressed(KeyEvent event)
        {
            if(gamestate != GameState.PLAY && gamestate != GameState.PAUSE){

                switch(event.getKeyCode())
                {
                    case KeyEvent.VK_SPACE:
                        prepareGame();
                        startGame();
                        break;
                    case KeyEvent.VK_SHIFT:
                        gamestate = GameState.START;
                        break;
                }
            }

            if(gamestate == GameState.PLAY)
            {
                switch(event.getKeyCode())
                {
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        if(hero.direction != Snake.Direction.RIGHT)
                            hero.direction = Snake.Direction.LEFT;
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        if(hero.direction != Snake.Direction.LEFT)
                            hero.direction = Snake.Direction.RIGHT;
                        break;
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        if(hero.direction != Snake.Direction.DOWN)
                            hero.direction = Snake.Direction.UP;
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        if(hero.direction != Snake.Direction.UP)
                            hero.direction = Snake.Direction.DOWN;
                        break;
                    case KeyEvent.VK_ENTER:
                        gamestate = GameState.PAUSE;
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

            if(gamestate == GameState.PAUSE)
            {
                switch(event.getKeyCode())
                {
                    case KeyEvent.VK_SPACE:
                        gamestate = GameState.PLAY;
                        break;
                    case KeyEvent.VK_SHIFT:
                        gamestate = GameState.START;
                        break;
                }
            }
        }
    }
}
