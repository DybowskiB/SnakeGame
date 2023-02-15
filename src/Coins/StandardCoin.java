package Coins;

import Heroes.Snake;

import java.awt.*;

public class StandardCoin extends Coin{

    public StandardCoin(Point p){
        super(4, p, Color.yellow, 5);
    }

    @Override
    public void grow(Snake snake) {
        snake.grow(additionalLength);
    }
}
