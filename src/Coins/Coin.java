package Coins;

import Heroes.Snake;

import java.awt.*;

public abstract class Coin {

    protected int additionalLength;
    protected Point point;
    protected Color color;

    protected int size;

    public Coin(int additionalLength, Point point, Color color, int size) {
        this.additionalLength = additionalLength;
        this.point = point;
        this.color = color;
        this.size = size;
    }

    public int getAdditionalLength()
    {
        return additionalLength;
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
    public abstract void grow(Snake snake);

    public void draw(int x_left, int x_right, int y_up, int y_down, Graphics g){
        if(x_left <= point.x && point.x <= x_right && y_up <= point.y && point.y <= y_down){
            g.setColor(color);
            g.fillOval(point.x - x_left, point.y - y_up, size, size);
        }
    }
}
