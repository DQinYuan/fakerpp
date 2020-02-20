package org.testd.fakerpp.core.util.earclipping;

public final class Polygon {

    private final Point[] mHullPoints;
    private final Point[][] mHolePoints;

    //顺时针排布的多边形点  外点 + 岛洞点
    final Point[] points;
    final int numPoints;
    //外点的数目
    final int numHullPoints;

    final int numHoles;
    final int numPointsPerHole[];
    final int holeStartIndices[];

    public Polygon(Point[] hull) {
        this(hull, new Point[0][]);
    }

    /**
     *
     * @param hull  多边形的外顶点
     * @param holes  多边形的岛洞
     */
    @SuppressWarnings("WeakerAccess")
    public Polygon(Point[] hull, Point[][] holes) {
        mHullPoints = hull;
        mHolePoints = holes;

        numHullPoints = hull.length;
        numHoles = holes.length;

        //存储每个岛洞的顶点数
        numPointsPerHole = new int[numHoles];
        holeStartIndices = new int[numHoles];
        //所有岛洞的顶点数总和
        int numHolePointsSum = 0;

        for (int i = 0; i < holes.length; i++) {
            numPointsPerHole[i] = holes[i].length;
            holeStartIndices[i] = numHullPoints + numHolePointsSum;
            numHolePointsSum += numPointsPerHole[i];
        }

        //多边形点的总数，包括外点和岛洞
        numPoints = numHullPoints + numHolePointsSum;
        points = new Point[numHullPoints + numHolePointsSum];

        //判断所给外点的顺序是顺时针还是逆时针
        boolean reverseHullPointsOrder = !pointsAreCounterClockwise(hull);
        for (int i = 0; i < numHullPoints; i++) {
            //points是按顺时针排布的多边形外点，如果hull是逆时针的，则要倒序放入
            points[i] = hull[(reverseHullPointsOrder)? (numHullPoints - 1 - i) : i];
        }

        // 添加岛洞点
        for (int i = 0; i < holes.length; i++) {
            boolean reverseHolePointsOrder = pointsAreCounterClockwise(holes[i]);
            for (int j = 0; j < holes[i].length; j++) {
                points[indexOfPointInHole(j, i)] = holes[i][(reverseHolePointsOrder)? (holes[i].length - j - 1) : j];
            }
        }
    }

    int indexOfPointInHole(int index, int holeIndex)
    {
        return holeStartIndices[holeIndex] + index;
    }

    Point getHolePoint(int index, int holeIndex)
    {
        return points[holeStartIndices[holeIndex] + index];
    }

    /**
     * 判断所给多边形的点是逆时针还是顺时针
     *
     * 参考：https://stackoverflow.com/questions/1165647/how-to-determine-if-a-list-of-polygon-points-are-in-clockwise-order
     * @param points
     * @return
     */
    private static boolean pointsAreCounterClockwise(Point[] points) {
        double area = 0;
        for (int i = 0; i < points.length; i++) {
            int nextIndex = (i + 1) % points.length;
            area += (points[nextIndex].x - points[i].x) * (points[nextIndex].y + points[i].y);
        }
        return (area < 0);
    }
}