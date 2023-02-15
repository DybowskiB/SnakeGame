package Heroes;

import java.awt.*;
import java.util.Random;

public class SnakeEnemy extends Snake{

    private int level;

    private Random random;

    public SnakeEnemy(int level, int positionX, int positionY, Color color, Color eyesColor, Color stripesColor){
        super(positionX, positionY, color, eyesColor, stripesColor);
        this.level = level;
    }

    @Override
    public void preparePositions() {

        positions.add(new Point(positionX, positionY));
        int i = 1;
        for(int j = 1; j <= 3; ++j) {
            int r = random.nextInt(4);
            switch (r) {
                case 0:
                    for (; i < j * length / 3; ++i)
                        positions.add(new Point(positionX, positionY + i));
                    break;
                case 1:
                    for (; i < j * length / 3; ++i)
                        positions.add(new Point(positionX, positionY - i));
                    break;
                case 2:
                    for (; i < j * length / 3; ++i)
                        positions.add(new Point(positionX + i, positionY));
                    break;
                case 3:
                    for (; i < j * length / 3; ++i)
                        positions.add(new Point(positionX - i, positionY));
                    break;
            }
        }
    }

    @Override
    public void move() {
        super.move();
    }
}
