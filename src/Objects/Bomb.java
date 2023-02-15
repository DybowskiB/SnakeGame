import Heroes.Snake;

import java.awt.*;

public class Bomb {

    protected Point point;
    protected Color color;

    protected int size;

    public Bomb(Point point) {
        this.point = point;
        this.color = Color.BLACK;
        this.size = 20;
    }

    public Point getPoint() {
        return point;
    }

    public int getSize() {
        return size;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public void draw(int x_left, int x_right, int y_up, int y_down, Graphics g){
        if(x_left <= point.x && point.x <= x_right && y_up <= point.y && point.y <= y_down){
            g.setColor(color);
            g.fillOval(point.x - x_left, point.y - y_up, size, size);
        }
    }
}
