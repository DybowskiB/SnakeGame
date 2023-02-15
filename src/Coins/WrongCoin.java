package Coins;

import Snake;

import java.awt.*;

public class WrongCoin extends Coin{

    public WrongCoin(Point p){
        super(-5, p, Color.black, 12);
    }

    @Override
    public void grow(Snake snake) {
        snake.grow(additionalLength);
    }
}
