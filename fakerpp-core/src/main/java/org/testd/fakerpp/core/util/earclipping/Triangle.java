package org.testd.fakerpp.core.util.earclipping;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 三角形内生成随机点的方法，参考： https://www.zhihu.com/question/31706710/answer/53131190
 */
public final class Triangle {

    public final Point a, b, c;

    //向量ab
    private double[] ab;

    //向量ac
    private double[] ac;




    public Triangle(Point a, Point b, Point c) {
        this.a = a;
        this.b = b;
        this.c = c;

        ab = vector(a, b);
        ac = vector(a, c);
    }

    /**
     * 用两个点组成向量
     *
     * @param start  向量起点
     * @param end    向量终点
     * @return
     */
    private double[] vector(Point start, Point end) {
        return new double[] {end.x - start.x,
                end.y - start.y};
    }


    /**
     * 计算三角形面积
     *
     * @param threePoints
     * @return
     */
    private double area(List<Point> threePoints) {
        Point p0 = threePoints.get(0);
        Point p1 = threePoints.get(1);
        Point p2 = threePoints.get(2);

        double area = (p0.x * p1.y + p1.x * p2.y + p2.x * p0.y
                - p0.x * p2.y - p1.x * p0.y - p2.x * p1.y) / 2;

        return area;
    }

    public double[] randomLatLng() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        double x = random.nextDouble();
        double y = random.nextDouble();

        if (x + y > 1) {
            x = 1 - x;
            y = 1 - y;
        }

        double randomLat = a.x + x * ab[0] + y * ac[0];
        double randomLng = a.y + x * ab[1] + y * ac[1];

        return new double[] {randomLat, randomLng};
    }

    @Override
    public String toString() {
        return "Triangle{" +
                "a=" + a +
                ", b=" + b +
                ", c=" + c +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Triangle)) return false;
        Triangle triangle = (Triangle) o;
        return Objects.equals(a, triangle.a) &&
                Objects.equals(b, triangle.b) &&
                Objects.equals(c, triangle.c);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c);
    }
}