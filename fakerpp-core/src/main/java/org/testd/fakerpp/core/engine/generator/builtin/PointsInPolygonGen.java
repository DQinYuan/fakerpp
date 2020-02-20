/**
 * Alipay.com Inc. Copyright (c) 2004-2019 All Rights Reserved.
 */
package org.testd.fakerpp.core.engine.generator.builtin;


import org.apache.commons.lang3.StringUtils;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.engine.generator.Generator;
import org.testd.fakerpp.core.engine.generator.builtin.base.DefaultString;
import org.testd.fakerpp.core.engine.generator.builtin.base.GeoBase;
import org.testd.fakerpp.core.util.SeedableThreadLocalRandom;
import org.testd.fakerpp.core.util.earclipping.Point;
import org.testd.fakerpp.core.util.earclipping.Polygon;
import org.testd.fakerpp.core.util.earclipping.PolygonTriangulator;
import org.testd.fakerpp.core.util.earclipping.Triangle;
import org.testd.fakerpp.core.util.earclipping.Point;
import org.testd.fakerpp.core.util.earclipping.Triangle;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于耳切法实现的在多边形内生成随机点的算法
 * <p>
 * 围栏以顺时针或者逆时针给出,无论是geojson格式还wkt格式,多边形围栏必须首尾相接(即第一个点和最后一个点相同)
 * <p>
 * 耳切法参考资料：https://www.geometrictools.com/Documentation/TriangulationByEarClipping.pdf
 * <p>
 * 算法： 先使用耳切法将多边形划分成数个三角形， 之后每次随机选取一个三角形，在选择的三角形中随机生成一个点 三角形中随机点生成的算法来源线性代数：
 * <p>
 * 三角形三个顶点：A, B, C
 * <p>
 * ab = B - A ac = C - A
 * <p>
 * 然后随机生成两个0~1的数字x,y 如果x + y > 1,令x = 1 - x, y = 1 - y 最后，随机点 = A + x * ab + y * ac
 */
public class PointsInPolygonGen implements Generator {

    @DefaultString("${Lat} ${Lng}")
    public String formatter = "${Lat} ${Lng}";

    @DefaultString("geojson")
    public String input = "geojson";

    public String polygon;

    private GeoBase geoBase;
    private List<Poly> polies;
    private int colNum;

    @Override
    public void init(int colNum) throws ERMLException {
        if (StringUtils.isEmpty(polygon)) {
            throw new ERMLException("polygon param can not be empty");
        }

        this.colNum = colNum;
        geoBase = new GeoBase(formatter, input);
        List<List<Point>> points = geoBase.points(polygon);

        polies = points.stream()
                .map(Poly::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> nextData() throws ERMLException {
        double[] latLng = null;
        while (latLng == null) {
            latLng = SeedableThreadLocalRandom.randomItemInList(polies).random();
        }

        return geoBase.result(latLng[0], latLng[1], colNum);
    }

    @Override
    public long dataNum() {
        return 0;
    }

    private static class Poly {

        private List<Triangle> triangles;

        public Poly(List<Point> points) {
            Point[] fence = new Point[points.size()];
            points.toArray(fence);
            Polygon polygon = new Polygon(fence);
            triangles = PolygonTriangulator.triangulate(polygon);
            if (triangles == null) {
                throw new RuntimeException("无法切分的多边形");
            }
        }

        public double[] random() {
            if (triangles == null) {
                return null;
            }
            return SeedableThreadLocalRandom.randomItemInList(triangles).randomLatLng();
        }

    }
}