package Heroes;

import Coins.Coin;
import Objects.Bomb;

import java.awt.*;
import java.util.ArrayList;

public class SnakeHero extends Snake{

    public SnakeHero(int positionX, int positionY, Color color, Color eyesColor, Color stripesColor){
        super(positionX, positionY, color, eyesColor, stripesColor);
        this.direction = Direction.UP;
        this.setStripesThickness(4);
    }

    @Override
    public void preparePositions() {
        positions = new ArrayList<>();
        for(int i = 0; i < length; ++i)
            positions.add(new Point(positionX, positionY + i * speed));
    }

    @Override
    public void grow(int additionalLenght)
    {
        super.grow(additionalLenght);
    }

    @Override
    public void move(){

        switch(direction){
            case LEFT:
                positionX -= speed;
                break;
            case RIGHT:
                positionX += speed;
                break;
            case UP:
                positionY -= speed;
                break;
            case DOWN:
                positionY += speed;
                break;
        }

        for(int i = length - 1; i > 0; --i)
            positions.set(i, positions.get(i - 1));
        positions.set(0, new Point(positionX, positionY));
    }

    @Override
    public void draw(Graphics g, int x, int y) {

        drawTongue(g, x, y);
        g.setColor(color);
        g.fillOval(x, y, thickness, thickness);
        Point startPoint = positions.get(0);
        int t = thickness;
        for (int i = 0; i < length; ++i) {
            if(hasStripes && i % stripesThickness < stripesThickness / 2) g.setColor(color);
            else g.setColor(stripesColor);
            Point position = positions.get(i);
            int diffX = position.x - startPoint.x;
            int diffY = position.y - startPoint.y;
            int xx = x + diffX;
            int yy = y + diffY;
            // narrowing the snake with coefficient 1.8 (tail)
            if(i > length - (int) (1.8 * thickness) && i % 2 == 0) --t;
            int xCoordinate = xx + (thickness - t) / 2;
            int yCoordinate = yy + (thickness - t) / 2;
            g.fillOval(xCoordinate, yCoordinate, t, t);
        }
        drawEyes(g, x, y);
    }

    @Override
    public boolean checkCollisions(ArrayList<Coin> coins, boolean[][] hashTableCoins, int UNIT_SIZE,
                                   int MAP_WIDTH, int MAP_HEIGHT, ArrayList<Snake> enemies, ArrayList<Bomb> bombs){

        checkCoinsCollisions(coins, hashTableCoins);
        return checkOwnCollisions() || checkSnakesCollisions(enemies) ||
                checkBorderCollisions(UNIT_SIZE, MAP_WIDTH, MAP_HEIGHT) || checkBombCollisions(bombs, hashTableCoins);
    }
}
