package Heroes;

import Coins.Coin;
import Objects.Bomb;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public abstract class Snake {
    public int positionX;
    public int positionY;
    public ArrayList<Point> positions;

    protected int length = 50;
    private final int MIN_THICKNESS = 10;
    private final int MAX_THICKNESS = 30;
    protected int thickness = MIN_THICKNESS;
    protected final int speed = 2;
    public enum Direction {LEFT, RIGHT, UP, DOWN}
    public Direction direction;

    private int tongue;
    protected Color color;
    protected Color eyesColor;
    protected boolean hasStripes = false;
    protected Color stripesColor;
    protected int stripesThickness;

    public int getLength() {
        return length;
    }

    public void setLength(int length){this.length = length; }

    public void setStripesThickness(int stripesThickness) {
        this.stripesThickness = stripesThickness;
    }

    public Snake(int positionX, int positionY, Color color, Color eyesColor, Color stripesColor)
    {
        this.positionX = positionX;
        this.positionY = positionY;
        preparePositions();
        this.color = color;
        this.eyesColor = eyesColor;
        if(stripesColor != null) {
            this.hasStripes = true;
            this.stripesColor = stripesColor;
            this.stripesThickness = thickness;
        }
    }

    // Abstract methods
    public abstract void preparePositions();
    public abstract void move();
    public abstract void draw(Graphics g, int x, int y);
    public void grow(int additionalLength){
        for(int i = 0; i < additionalLength; ++i)
            positions.add(new Point(positions.get(positions.size() - 1)));
        this.length += additionalLength;
        increaseThickness();
    }

    // Other methods
    public int getThickness() {
        return thickness;
    }
    public void increaseThickness()
    {
        thickness = (int)Math.min(Math.max(Math.floor(length / (float)MIN_THICKNESS), MIN_THICKNESS), MAX_THICKNESS);
    }

    public Point getHeadPoint()
    {
        Point position0 = positions.get(0);
        Point headPoint;
        switch(direction){
            case DOWN:
                headPoint = new Point(position0.x + thickness / 2, position0.y + thickness);
                break;
            case RIGHT:
                headPoint = new Point(position0.x + thickness, position0.y + thickness / 2);
                break;
            case LEFT:
                headPoint = new Point(position0.x, position0.y + thickness / 2);
                break;
            default:
                headPoint = new Point(position0.x + thickness / 2, position0.y);
                break;
        }
        return headPoint;
    }

    public void drawEyes(Graphics g, int x, int y) {
        int eye = thickness / 5;
        g.setColor(eyesColor);
        switch (direction) {
            case UP:
                g.fillOval(x + eye, y + 3 * eye, eye, eye);
                g.fillOval(x + 3 * eye, y + 3 * eye, eye, eye);
                break;
            case DOWN:
                g.fillOval(x + eye, y + eye, eye, eye);
                g.fillOval(x + 3 * eye, y + eye, eye, eye);
                break;
            case LEFT:
                g.fillOval(x + 3 * eye, y + eye, eye, eye);
                g.fillOval(x + 3 * eye, y + 3 * eye, eye, eye);
                break;
            case RIGHT:
                g.fillOval(x + eye, y + 3 * eye, eye, eye);
                g.fillOval(x + eye, y + eye, eye, eye);
                break;
        }
    }

    public void drawTongue(Graphics g, int x, int y) {
        Random rand = new Random();
        // tongue appears with 1% probability and for 20 time units
        if (rand.nextInt() % 100 == 1) {
            tongue = 20;
        }
        int tongueX;
        int tongueY;
        if (tongue > 0) {
            g.setColor(Color.red);
            switch (direction) {
                case UP:
                    tongueX = thickness / 3;
                    tongueY = 2 * thickness;
                    g.fillOval(x + tongueX, y - thickness, tongueX, tongueY);
                    break;
                case DOWN:
                    tongueX = thickness / 3;
                    tongueY = 2 * thickness;
                    g.fillOval(x + tongueX, y, tongueX, tongueY);
                    break;
                case LEFT:
                    tongueX = 2 * thickness;
                    tongueY = thickness / 3;
                    g.fillOval(x - thickness, y + tongueY , tongueX, tongueY);
                    break;
                case RIGHT:
                    tongueX = 2 * thickness;
                    tongueY = thickness / 3;
                    g.fillOval(x, y + tongueY, tongueX, tongueY);
                    break;
            }
            --tongue;
        }
    }

    public abstract boolean checkCollisions(ArrayList<Coin> coins, boolean[][] hashTableCoins, int UNIT_SIZE,
                                   int MAP_WIDTH, int MAP_HEIGHT, ArrayList<Snake> enemies, ArrayList<Bomb> bombs);

    public boolean checkBombCollisions(ArrayList<Bomb> bombs, boolean[][] hashTableCoins)
    {
        int r = thickness / 2;
        int x = positionX;
        int y = positionY;

        for(int i = 0; i < bombs.size(); ++i)
        {
            Bomb b = bombs.get(i);
            int br = Bomb.getSize() / 2;
            Point p = b.getPoint();
            int bx = p.x + br;
            int by = p.y + br;
            if(circleIntersection(x, y, bx, by, r, br)){
                hashTableCoins[p.x][p.y] = false;
                bombs.remove(b);
                return true;
            }
        }
        return false;
    }
    protected void checkCoinsCollisions(ArrayList<Coin> coins, boolean[][] hashTableCoins){

        int r = thickness / 2;
        int x = positionX;
        int y = positionY;

        for(int i = 0; i < coins.size(); ++i)
        {
            Coin c = coins.get(i);
            int cr = c.getSize() / 2;
            Point p = c.getPoint();
            int cx = p.x + cr;
            int cy = p.y + cr;
            if(circleIntersection(x, y, cx, cy, r, cr / 2)){
                this.grow(c.getAdditionalLength());
                hashTableCoins[p.x][p.y] = false;
                coins.remove(c);
            }
        }
    }

    protected boolean checkOwnCollisions()
    {
        Point headPoint  = getHeadPoint();
        int t = thickness;
        for (int i = 1; i < length; ++i) {
            Point position = positions.get(i);
            // narrowing the snake with coefficient 1.8 (tail)
            if(i > length - (int) (1.8 * thickness) && i % 2 == 0) --t;
            int radius = t / 2;
            int x = position.x + radius;
            int y = position.y + radius;
            if(pointInsideCircle(headPoint, x, y, radius))
                return true;
        }
        return false;
    }

    protected boolean checkBorderCollisions(int UNIT_SIZE, int MAP_WIDTH, int MAP_HEIGHT)
    {
        Point headPoint = getHeadPoint();
        return headPoint.x <= UNIT_SIZE || headPoint.x >= MAP_WIDTH - UNIT_SIZE ||
                headPoint.y <= UNIT_SIZE || headPoint.y >= MAP_HEIGHT - UNIT_SIZE;
    }

    protected boolean checkSnakesCollisions(ArrayList<Snake> enemies)
    {
        for(int i = 0; i < enemies.size(); ++i)
        {
            Snake enemy = enemies.get(i);
            Point headPoint  = getHeadPoint();
            int tt = enemy.getThickness();
            int t = tt;
            int enemyLength = enemy.getLength();
            for (int j = 0; j < enemyLength; ++j) {
                // narrowing the snake with coefficient 1.8 (tail)
                if(i > enemyLength - (int) (1.8 * tt) && i % 2 == 0) --t;
                int radius = t / 2;
                int x = enemy.positions.get(j).x + radius;
                int y = enemy.positions.get(j).y + radius;
                if(pointInsideCircle(headPoint, x, y, radius)) {
                    enemy.grow(length);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean circleIntersection(int x1, int y1, int x2, int y2, int r1, int r2)
    {
        double d = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        return d <= r1 + r2;
    }

    private static boolean pointInsideCircle(Point point, int xCenter, int yCenter, int radius)
    {
        int dx = Math.abs(point.x - xCenter);
        if (dx > radius) return false;
        int dy = Math.abs(point.y - yCenter);
        if (dy > radius) return false;
        if (dx + dy <= radius) return true;
        return ( Math.pow(dx, 2) + Math.pow(dy, 2) <= Math.pow(radius, 2));
    }
}
