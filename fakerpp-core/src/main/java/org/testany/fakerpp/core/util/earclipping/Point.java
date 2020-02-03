package org.testany.fakerpp.core.util.earclipping;

public final class Point {

    //geo生成器中约定x是纬度, y是经度
    public double x, y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Point)) {
            return false;
        }

        final Point point = (Point) obj;
        return (Math2.approximatelyEqual(this.x, point.x) && Math2.approximatelyEqual(this.y, point.y));
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}