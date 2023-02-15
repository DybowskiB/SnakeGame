package Objects;

import Heroes.Snake;

import java.awt.*;

public class Bomb extends MapObject{

    protected Point point;
    protected Color color;

    protected static int size;

    public Bomb(Point point) {
        this.point = point;
        this.color = Color.BLACK;
        size = 20;
    }

    public Point getPoint() {
        return point;
    }

    public static int getSize() {
        return size;
    }

    @Override
    public void setPoint(Point point) {
        this.point = point;
    }

    @Override
    public void draw(int x_left, int x_right, int y_up, int y_down, Graphics g){
        if(x_left <= point.x && point.x <= x_right && y_up <= point.y && point.y <= y_down){
            g.setColor(color);
            g.fillOval(point.x - x_left, point.y - y_up, size, size);
            g.setColor(new Color(255, 0, 0));
            g.setFont(new Font("Console", Font.BOLD, 10));
            g.drawString("\uD83D\uDC80", point.x - x_left + size / 4, point.y - y_up + 2 * size / 3);
        }
    }
}
