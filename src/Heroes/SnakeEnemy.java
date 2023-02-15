package Heroes;

import Coins.Coin;
import Objects.Bomb;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class SnakeEnemy extends Snake{

    private final Random random;
    private int step = 0;
    private int dir = 0;

    private final int UNIT_SIZE;
    private final int SCREEN_WIDTH;
    private final int SCREEN_HEIGHT;

    private final int MAP_WIDTH;
    private final int MAP_HEIGHT;

    private final int level;

    public SnakeEnemy(int positionX, int positionY, Color color, Color eyesColor, Color stripesColor, int UNIT_SIZE,
            int SCREEN_WIDTH, int SCREEN_HEIGHT, int MAP_WIDTH, int MAP_HEIGHT, int level){
        super(positionX, positionY, color, eyesColor, stripesColor);
        this.direction = Direction.UP;
        random = new Random();
        this.UNIT_SIZE = UNIT_SIZE;
        this.SCREEN_WIDTH = SCREEN_WIDTH;
        this.SCREEN_HEIGHT = SCREEN_HEIGHT;
        this.MAP_WIDTH = MAP_WIDTH;
        this.MAP_HEIGHT = MAP_HEIGHT;
        this.level = level;
    }

    @Override
    public void preparePositions() {

        positions = new ArrayList<>();
        for(int i = 0; i < length; ++i){
            positions.add(new Point(positionX, positionY + i * speed));
        }
    }

    @Override
    public void grow(int additionalLenght)
    {
        super.grow(Math.abs(2 * (level + 1) * additionalLenght));
    }

    @Override
    public void move() {

        if(step == 0)
        {
            step = random.nextInt(500);
            dir = random.nextInt(2);
            if(direction == Direction.UP || direction == Direction.DOWN)
            {
                switch(dir){
                    case 0:
                        direction = Direction.LEFT;
                        break;
                    case 1:
                        direction = Direction.RIGHT;
                        break;
                }
            }
            else
            {
                switch(dir){
                    case 0:
                        direction = Direction.UP;
                        break;
                    case 1:
                        direction = Direction.DOWN;
                        break;
                }
            }
        }

        --step;
        switch(direction){
            case LEFT:
                positionX -= speed;
                if(positionX < UNIT_SIZE)
                {
                    positionX += speed;
                    if(positionY - speed > UNIT_SIZE) {
                        direction = Direction.UP;
                        positionY -= speed;
                    }
                    else {
                        direction = Direction.DOWN;
                        positionY += speed;
                    }
                }
                break;
            case RIGHT:
                positionX += speed;
                if(positionX > MAP_WIDTH - UNIT_SIZE)
                {
                    positionX -= speed;
                    if(positionY - speed > UNIT_SIZE) {
                        direction = Direction.UP;
                        positionY -= speed;
                    }
                    else {
                        direction = Direction.DOWN;
                        positionY += speed;
                    }
                }
                break;
            case UP:
                positionY -= speed;
                if(positionY < UNIT_SIZE)
                {
                    positionY += speed;
                    if(positionX - speed > UNIT_SIZE) {
                        direction = Direction.LEFT;
                        positionX -= speed;
                    }
                    else {
                        direction = Direction.RIGHT;
                        positionX += speed;
                    }
                }
                break;
            case DOWN:
                positionY += speed;
                if(positionY > MAP_HEIGHT - UNIT_SIZE)
                {
                    positionY -= speed;
                    if(positionX - speed > UNIT_SIZE) {
                        direction = Direction.LEFT;
                        positionX -= speed;
                    }
                    else {
                        direction = Direction.RIGHT;
                        positionX += speed;
                    }
                }
                break;
        }

        for(int i = length - 1; i > 0; --i)
            positions.set(i, positions.get(i - 1));
        positions.set(0, new Point(positionX, positionY));
    }

    @Override
    public void draw(Graphics g, int x, int y)
    {
        int t = thickness;
        for(int i = 0; i < length; ++i)
        {
            int px = positions.get(i).x;
            int py = positions.get(i).y;
            if(i > length - (int) (1.8 * thickness) && i % 2 == 0) --t;
            if(px > x - t && px < x + t + SCREEN_WIDTH && py > y - t && py < y + t + SCREEN_HEIGHT)
            {
                int xCoordinate = px - x + (thickness - t) / 2;
                int yCoordinate = py - y + (thickness - t) / 2;
                if(i == 0) drawTongue(g, px - x, py - y);
                g.setColor(color);
                g.fillOval(xCoordinate, yCoordinate, t, t);
            }
        }
        drawEyes(g, positions.get(0).x - x, positions.get(0).y - y);
    }

    @Override
    public boolean checkCollisions(ArrayList<Coin> coins, boolean[][] hashTableCoins, int UNIT_SIZE, int MAP_WIDTH,
                                   int MAP_HEIGHT, ArrayList<Snake> enemies, ArrayList<Bomb> bombs) {
        checkCoinsCollisions(coins, hashTableCoins);
        if(checkBombCollisions(bombs, hashTableCoins)) {
            super.grow(100);
        }
        return checkSnakesCollisions(enemies);
    }
}
