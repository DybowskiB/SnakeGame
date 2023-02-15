package Heroes;

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
}
