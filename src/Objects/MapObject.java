package Objects;

import java.awt.*;

public abstract class MapObject {

    public abstract void setPoint(Point point);
    public abstract void draw(int x_left, int x_right, int y_up, int y_down, Graphics g);
}
