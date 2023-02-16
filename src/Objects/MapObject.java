package Objects;

import java.awt.*;
import java.util.concurrent.Semaphore;

public abstract class MapObject {

    public abstract void setPoint(Point point);
    public abstract void draw(int x_left, int x_right, int y_up, int y_down, Graphics g, Semaphore mutex);
}
