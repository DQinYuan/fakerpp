package org.testd.ui.view.dynamic;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import javafx.scene.layout.Region;
import javafx.scene.shape.Polyline;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ConnectPolyLine extends Polyline {

    private final double evadeInterval;

    public ConnectPolyLine(Region source, Region target, double evadeInterval) {
        source.translateXProperty().addListener((observable, oldValue, newValue) -> {
            connect(source, target);
        });

        target.translateXProperty().addListener((observable, oldValue, newValue) -> {
            connect(source, target);
        });

        this.evadeInterval = evadeInterval;
        connect(source, target);
    }

    private Range<Double> getXRange(Region region) {
        return Range.closed(region.getTranslateX(),
                region.getTranslateX() + region.getWidth());
    }

    private Range<Double> getYRange(Region region) {
        return Range.closed(region.getTranslateY(),
                region.getTranslateY() + region.getHeight());
    }



    private void connect(Region source, Region target) {

        // source info
        Range<Double> sourceXRange = getXRange(source);
        Range<Double> sourceYRange = getYRange(source);

        // target info
        Range<Double> targetXRange = getXRange(target);
        Range<Double> targetYRange = getYRange(target);

        getPoints().clear();

        getPoints().addAll(connect(sourceXRange, sourceYRange,
                targetXRange, targetYRange, evadeInterval));
    }

    private static Supplier<Void> pointAddSupplier(List<Double> points, Double... doubles) {
        return () -> {
            for (Double aDouble : doubles) {
                points.add(aDouble);
            }
            return null;
        };
    }

    private static double rangeMid(Range<Double> range) {
        return (range.lowerEndpoint() + range.upperEndpoint()) / 2;
    }

    @VisibleForTesting
    protected static List<Double> connect(Range<Double> sourceXRange,
                                          Range<Double> sourceYRange,
                                          Range<Double> targetXRange,
                                          Range<Double> targetYRange,
                                          double evadeInterval) {
        // overlap
        if (sourceXRange.isConnected(targetXRange)
                && sourceYRange.isConnected(targetYRange)) {
            return Collections.emptyList();
        }

        double sourceMidY = rangeMid(sourceYRange);
        double targetMidY = rangeMid(targetYRange);

        List<Double> points = Lists.newArrayList();

        Supplier<Void> startFromRight = pointAddSupplier(points,
                sourceXRange.upperEndpoint(), sourceMidY);

        Supplier<Void> startFromLeft = pointAddSupplier(points,
                sourceXRange.lowerEndpoint(), sourceMidY);

        Supplier<Void> endFromRight = pointAddSupplier(points,
                targetXRange.upperEndpoint(), targetMidY);

        Supplier<Void> endFromLeft = pointAddSupplier(points,
                targetXRange.lowerEndpoint(), targetMidY);

        double lineXMid = (sourceXRange.upperEndpoint() + targetXRange.lowerEndpoint()) / 2;
        Supplier<Void> midXLine = pointAddSupplier(points,
                lineXMid, sourceMidY, lineXMid, targetMidY);

        // (target ... source)
        if (sourceXRange.upperEndpoint() < targetXRange.lowerEndpoint()) { // completely right or
            // start
            startFromRight.get();

            // vertical line
            midXLine.get();

            // end
            endFromLeft.get();
        } else if (sourceXRange.lowerEndpoint() > targetXRange.upperEndpoint()) { // completely left
            startFromLeft.get();
            midXLine.get();
            endFromRight.get();
        } else if (sourceXRange.encloses(targetXRange)
                || sourceXRange.contains(targetXRange.lowerEndpoint())) { // completely contain or left border in
            startFromLeft.get();

            points.add(sourceXRange.lowerEndpoint() - evadeInterval);
            points.add(sourceMidY);

            points.add(sourceXRange.lowerEndpoint() - evadeInterval);
            points.add(targetMidY);

            endFromLeft.get();
        } else if (sourceXRange.contains(targetXRange.upperEndpoint())) { // right border in
            startFromRight.get();

            points.add(sourceXRange.upperEndpoint() + evadeInterval);
            points.add(sourceMidY);

            points.add(sourceXRange.upperEndpoint() + evadeInterval);
            points.add(targetMidY);

            endFromRight.get();
        }

        return points;
    }


}
