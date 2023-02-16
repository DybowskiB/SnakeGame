package Coins;

import Heroes.Snake;
import Objects.MapObject;

import java.awt.*;
import java.util.concurrent.Semaphore;

public abstract class Coin extends MapObject {

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

    @Override
    public void setPoint(Point point) {
        this.point = point;
    }
    public abstract void grow(Snake snake);

    @Override
    public void draw(int x_left, int x_right, int y_up, int y_down, Graphics g, Semaphore mutex){
        if (x_left <= point.x && point.x <= x_right && y_up <= point.y && point.y <= y_down) {
            try {
                mutex.acquire();
                g.setColor(color);
                g.fillOval(point.x - x_left, point.y - y_up, size, size);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mutex.release();
            }
        }
    }
}
