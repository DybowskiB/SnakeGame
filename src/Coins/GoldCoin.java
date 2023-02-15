package Coins;

import Heroes.Snake;

import java.awt.*;

public class GoldCoin extends Coin{

    public static int ADDITIONAL_LENGTH = 5;
    public GoldCoin(Point p){
        super(ADDITIONAL_LENGTH, p, Color.yellow, 15);
    }

    @Override
    public void grow(Snake snake)
    {
        snake.grow(additionalLength);
    }
}
