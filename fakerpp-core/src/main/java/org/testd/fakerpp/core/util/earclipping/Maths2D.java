package org.testd.fakerpp.core.util.earclipping;

public final class Maths2D {

    public static int sideOfLine(Point a, Point b, Point p) {
        double v = ((p.x - a.x) * (-b.y + a.y) + (p.y - a.y) * (b.x - a.x));
        return ((v < 0)? -1 : 1);
    }

    public static boolean pointInTriangle(Point a, Point b, Point c, Point p) {
        double area = 0.5f * (-b.y * c.x + a.y * (-b.x + c.x) + a.x * (b.y - c.y) + b.x * c.y);
        double s = 1 / (2 * area) * (a.y * c.x - a.x * c.y + (c.y - a.y) * p.x + (a.x - c.x) * p.y);
        double t = 1 / (2 * area) * (a.x * b.y - a.y * b.x + (a.y - b.y) * p.x + (b.x - a.x) * p.y);
        return s >= 0 && t >= 0 && (s + t) <= 1;
    }

    //通过计算向量外积判断是否凸点
    public static boolean isConvex(Point v0, Point v1, Point v2) {
        return (sideOfLine(v0, v2, v1) == -1);
    }
}