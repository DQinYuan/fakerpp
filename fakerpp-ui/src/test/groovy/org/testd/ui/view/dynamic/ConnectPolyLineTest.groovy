package org.testd.ui.view.dynamic

import com.google.common.collect.Range
import spock.lang.Specification

class ConnectPolyLineTest extends Specification {

    class Point {
        Double x, y

        boolean equals(o) {
            if (this.is(o)) return true
            if (!(o instanceof Point)) return false

            Point point = (Point) o

            if (x != point.x) return false
            if (y != point.y) return false

            return true
        }

        int hashCode() {
            int result
            result = x.hashCode()
            result = 31 * result + y.hashCode()
            return result
        }


        @Override
        String toString() {
            return "Point{" +
                    "x=" + x +
                    ", y=" + y +
                    '}'
        }
    }

    Point ofPoint(double x, double y) {
        return new Point(x: x, y: y)
    }

    Set<Point> toPointSet(List<Double> doubles) {
        List<Point> ps = new ArrayList<>()
        doubles.inject(new ArrayList<Point>(), { acc, val ->
            if (acc.isEmpty() || acc.last().y != null) {
                def p = new Point()
                p.x = val
                acc.add(p)
            } else {
                acc.last().y = val
            }
            acc
        }).toSet()
    }


    def "test connect"(double sourceX, double sourceY,
                       double targetX, double targetY, Set<Point> expect) {
        given:
        double width = 10.0
        double height = 10.0

        def sourceXRange = Range.closed(sourceX, sourceX + width)
        def sourceYRange = Range.closed(sourceY, sourceY + height)

        def targetXRange = Range.closed(targetX, targetX + width)
        def targetYRange = Range.closed(targetY, targetY + height)

        when:
        def points = ConnectPolyLine.connect(sourceXRange, sourceYRange,
                targetXRange, targetYRange, 10.0)

        then:
        toPointSet(points) == expect

        where:
        sourceX | sourceY | targetX | targetY | expect
        10.0    | 10.0    | 50.0    | 10.0    | [ofPoint(20.0, 15.0),
                                                 ofPoint(35.0, 15.0), ofPoint(50.0, 15.0)]
        10.0    | 10.0    | 19.0    | 19.0    | []
        10.0    | 10.0    | 10.0    | 30.0    | [ofPoint(10, 15), ofPoint(0, 15),
                                                 ofPoint(0, 35), ofPoint(10, 35)]
        10.0    | 10.0    | 11.0    | 30.0    | [ofPoint(10, 15), ofPoint(0, 15),
                                                 ofPoint(0, 35), ofPoint(11, 35)]
        10.0    | 10.0    | 9.0     | 30.0    | [ofPoint(20.0, 15.0), ofPoint(30.0, 15.0),
                                                 ofPoint(30.0, 35.0), ofPoint(19.0, 35.0)]
    }


    def "test load img"() {
        expect:
        def url = getClass().getResource("/img/rightarrow.png")
        println(url)
    }


}
