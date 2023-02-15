package Coins;

import Heroes.Snake;

import java.awt.*;

public class WrongCoin extends Coin{

    public static int ADDITIONAL_LENGTH = -10;
    public WrongCoin(Point p){
        super(ADDITIONAL_LENGTH, p, Color.black, 12);
    }

    @Override
    public void grow(Snake snake) {
        snake.grow(additionalLength);
    }
}
