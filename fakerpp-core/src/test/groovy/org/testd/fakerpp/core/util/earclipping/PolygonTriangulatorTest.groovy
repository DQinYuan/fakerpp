package org.testd.fakerpp.core.util.earclipping

import spock.lang.Specification

class PolygonTriangulatorTest extends Specification {

    def "test triangulate a polygon to triangle"() {
        given:
        Polygon polygon = new Polygon([
                new Point(113.8171111, 22.2170556),
                new Point(113.8263889, 22.2392778),
                new Point(113.839, 22.2677222),
                new Point(113.8473889, 22.2731111),
                new Point(113.8499792, 22.2802937),
                new Point(113.850068, 22.28054),
                new Point(113.8501034, 22.2806381),
                new Point(113.8501035, 22.2806385),
                new Point(113.8501547, 22.2807803),
                new Point(113.8501837, 22.2808609),
                new Point(113.8502671, 22.2810917),
                new Point(113.8503489, 22.281318),
                new Point(113.8657707, 22.3239836),
                new Point(113.9946779, 22.3247801),
                new Point(113.994543, 22.3142273),
                new Point(114.0065633, 22.3140348),
                new Point(114.0067456, 22.3142539),
                new Point(114.0071108, 22.3143182),
                new Point(114.0074671, 22.3145139),
                new Point(114.007976, 22.3146896),
                new Point(114.009118, 22.3148652),
                new Point(114.0091674, 22.3139445),
                new Point(114.0106424, 22.313916),
                new Point(114.0727654, 22.3127148),
                new Point(114.109501, 22.3127347),
                new Point(114.109386, 22.298358),
                new Point(114.1094278, 22.2730087),
                new Point(114.1094777, 22.2625686),
                new Point(114.1993579, 22.1931272),
                new Point(114.3091353, 22.1920941),
                new Point(114.3091398, 22.1484722),
                new Point(114.284, 22.1484722),
                new Point(114.2551667, 22.1385556),
                new Point(114.236, 22.1484722),
                new Point(113.9395556, 22.1484722),
                new Point(113.9223889, 22.1367222),
                new Point(113.8965556, 22.1425278),
                new Point(113.8323889, 22.1838611),
                new Point(113.8171111, 22.2170556),
        ].toArray(new Point[0]))

        when:
        def triangles = PolygonTriangulator.triangulate(polygon)


        then:
        triangles == [
                new Triangle(new Point(113.8965556, 22.1425278), new Point(113.8323889, 22.1838611), new Point(113.8171111, 22.2170556)),
                new Triangle(new Point(113.9395556, 22.1484722), new Point(113.9223889, 22.1367222), new Point(113.8965556, 22.1425278)),
                new Triangle(new Point(113.9395556, 22.1484722), new Point(113.8965556, 22.1425278), new Point(113.8171111, 22.2170556)),
                new Triangle(new Point(114.236, 22.1484722), new Point(113.9395556, 22.1484722), new Point(113.8171111, 22.2170556)),
                new Triangle(new Point(114.284, 22.1484722), new Point(114.2551667, 22.1385556), new Point(114.236, 22.1484722)),
                new Triangle(new Point(114.284, 22.1484722), new Point(114.236, 22.1484722), new Point(113.8171111, 22.2170556)),
                new Triangle(new Point(114.3091398, 22.1484722), new Point(114.284, 22.1484722), new Point(113.8171111, 22.2170556)),
                new Triangle(new Point(114.1993579, 22.1931272), new Point(114.3091353, 22.1920941), new Point(114.3091398, 22.1484722)),
                new Triangle(new Point(114.0727654, 22.3127148), new Point(114.109501, 22.3127347), new Point(114.109386, 22.298358)),
                new Triangle(new Point(114.0727654, 22.3127148), new Point(114.109386, 22.298358), new Point(114.1094278, 22.2730087)),
                new Triangle(new Point(114.0727654, 22.3127148), new Point(114.1094278, 22.2730087), new Point(114.1094777, 22.2625686)),
                new Triangle(new Point(114.0106424, 22.313916), new Point(114.0727654, 22.3127148), new Point(114.1094777, 22.2625686)),
                new Triangle(new Point(114.0106424, 22.313916), new Point(114.1094777, 22.2625686), new Point(114.1993579, 22.1931272)),
                new Triangle(new Point(114.0091674, 22.3139445), new Point(114.0106424, 22.313916), new Point(114.1993579, 22.1931272)),
                new Triangle(new Point(114.007976, 22.3146896), new Point(114.009118, 22.3148652), new Point(114.0091674, 22.3139445)),
                new Triangle(new Point(114.007976, 22.3146896), new Point(114.0091674, 22.3139445), new Point(114.1993579, 22.1931272)),
                new Triangle(new Point(114.0074671, 22.3145139), new Point(114.007976, 22.3146896), new Point(114.1993579, 22.1931272)),
                new Triangle(new Point(114.0071108, 22.3143182), new Point(114.0074671, 22.3145139), new Point(114.1993579, 22.1931272)),
                new Triangle(new Point(114.0067456, 22.3142539), new Point(114.0071108, 22.3143182), new Point(114.1993579, 22.1931272)),
                new Triangle(new Point(114.0065633, 22.3140348), new Point(114.0067456, 22.3142539), new Point(114.1993579, 22.1931272)),
                new Triangle(new Point(113.994543, 22.3142273), new Point(114.0065633, 22.3140348), new Point(114.1993579, 22.1931272)),
                new Triangle(new Point(113.8657707, 22.3239836), new Point(113.9946779, 22.3247801), new Point(113.994543, 22.3142273)),
                new Triangle(new Point(113.8657707, 22.3239836), new Point(113.994543, 22.3142273), new Point(114.1993579, 22.1931272)),
                new Triangle(new Point(113.8657707, 22.3239836), new Point(114.1993579, 22.1931272), new Point(114.3091398, 22.1484722)),
                new Triangle(new Point(113.8503489, 22.281318), new Point(113.8657707, 22.3239836), new Point(114.3091398, 22.1484722)),
                new Triangle(new Point(113.8502671, 22.2810917), new Point(113.8503489, 22.281318), new Point(114.3091398, 22.1484722)),
                new Triangle(new Point(113.8501837, 22.2808609), new Point(113.8502671, 22.2810917), new Point(114.3091398, 22.1484722)),
                new Triangle(new Point(113.8501547, 22.2807803), new Point(113.8501837, 22.2808609), new Point(114.3091398, 22.1484722)),
                new Triangle(new Point(113.8501035, 22.2806385), new Point(113.8501547, 22.2807803), new Point(114.3091398, 22.1484722)),
                new Triangle(new Point(113.8501034, 22.2806381), new Point(113.8501035, 22.2806385), new Point(114.3091398, 22.1484722)),
                new Triangle(new Point(113.850068, 22.28054), new Point(113.8501034, 22.2806381), new Point(114.3091398, 22.1484722)),
                new Triangle(new Point(113.8499792, 22.2802937), new Point(113.850068, 22.28054), new Point(114.3091398, 22.1484722)),
                new Triangle(new Point(113.8473889, 22.2731111), new Point(113.8499792, 22.2802937), new Point(114.3091398, 22.1484722)),
                new Triangle(new Point(113.8473889, 22.2731111), new Point(114.3091398, 22.1484722), new Point(113.8171111, 22.2170556)),
                new Triangle(new Point(113.8263889, 22.2392778), new Point(113.839, 22.2677222), new Point(113.8473889, 22.2731111)),
                new Triangle(new Point(113.8263889, 22.2392778), new Point(113.8473889, 22.2731111), new Point(113.8171111, 22.2170556)),
                new Triangle(new Point(113.8171111, 22.2170556), new Point(113.8263889, 22.2392778), new Point(113.8171111, 22.2170556)),]
    }

}
