package Coins;

import Heroes.Snake;

import java.awt.*;

public class StandardCoin extends Coin{

    public static int ADDITIONAL_LENGTH = 1;
    public StandardCoin(Point p){
        super(ADDITIONAL_LENGTH, p, Color.green, 8);
    }

    @Override
    public void grow(Snake snake) {
        snake.grow(additionalLength);
    }
}
