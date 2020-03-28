package org.testd.ui.view.dynamic;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;

import java.util.Collections;
import java.util.List;

public class ConnectPolyLine extends Group {

    private final double evadeInterval;
    private final DoubleProperty sourceXProperty;
    private final DoubleProperty sourceYProperty;
    private final DoubleProperty targetXProperty;
    private final DoubleProperty targetYProperty;
    private final Region sourceRegion;
    private final Region targetRegion;

    private final Line arrow1 = new Line();
    private final Line arrow2 = new Line();

    public ConnectPolyLine(Region source, Region target,
                           DoubleProperty sourceX,
                           DoubleProperty sourceY,
                           DoubleProperty targetX,
                           DoubleProperty targetY,
                           double evadeInterval) {
        this.sourceRegion = source;
        this.targetRegion = target;
        this.sourceXProperty = sourceX;
        this.sourceYProperty = sourceY;
        this.targetXProperty = targetX;
        this.targetYProperty = targetY;
        Polyline polyline = new Polyline();
        ChangeListener<Number> changeListener =
                (observable, oldValue, newValue) -> connect(source, target, polyline);
        sourceX.addListener(changeListener);
        sourceY.addListener(changeListener);
        targetX.addListener(changeListener);
        targetY.addListener(changeListener);
        source.heightProperty().addListener(changeListener);
        target.heightProperty().addListener(changeListener);

        this.evadeInterval = evadeInterval;
        connect(source, target, polyline);

        getChildren().add(polyline);
        getChildren().add(arrow1);
        getChildren().add(arrow2);
    }

    private static final DropShadow borderGlow = new DropShadow() {
        {
            setSpread(0.8);
            setColor(Color.RED);
        }
    };

    public void toggleEffect() {
        if (getEffect() == null) {
            setEffect(borderGlow);
        } else {
            setEffect(null);
        }
    }


    private Range<Double> getXRange(Region region) {
        Double x = region == sourceRegion ? sourceXProperty.get()
                : targetXProperty.get();
        return Range.closed(x,
                x + region.getWidth());
    }

    private Range<Double> getYRange(Region region) {
        Double y = region == sourceRegion ? sourceYProperty.get()
                : targetYProperty.get();
        return Range.closed(y, y + region.getHeight());
    }

    private static final double arrowLength = 20;
    private static final double arrowWidth = 7;

    private void connect(Region source, Region target, Polyline polyline) {

        // source info
        Range<Double> sourceXRange = getXRange(source);
        Range<Double> sourceYRange = getYRange(source);

        // target info
        Range<Double> targetXRange = getXRange(target);
        Range<Double> targetYRange = getYRange(target);

        polyline.getPoints().clear();

        List<Double> newPoints = connect(sourceXRange, sourceYRange,
                targetXRange, targetYRange, evadeInterval);
        polyline.getPoints().addAll(newPoints);

        if (newPoints.size() > 4) {
            double sx = newPoints.get(newPoints.size() - 4);
            double sy = newPoints.get(newPoints.size() - 3);
            double ex = newPoints.get(newPoints.size() - 2);
            double ey = newPoints.get(newPoints.size() - 1);

            drawArrow(sx, sy, ex, ey);
        }
    }

    private void drawArrow(double sx, double sy, double ex, double ey) {

        arrow1.setEndX(ex);
        arrow1.setEndY(ey);
        arrow2.setEndX(ex);
        arrow2.setEndY(ey);

        if (ex == sx && ey == sy) {
            // arrow parts of length 0
            arrow1.setStartX(ex);
            arrow1.setStartY(ey);
            arrow2.setStartX(ex);
            arrow2.setStartY(ey);
        } else {
            double factor = arrowLength / Math.hypot(sx-ex, sy-ey);
            double factorO = arrowWidth / Math.hypot(sx-ex, sy-ey);

            // part in direction of main line
            double dx = (sx - ex) * factor;
            double dy = (sy - ey) * factor;

            // part ortogonal to main line
            double ox = (sx - ex) * factorO;
            double oy = (sy - ey) * factorO;

            arrow1.setStartX(ex + dx - oy);
            arrow1.setStartY(ey + dy + ox);
            arrow2.setStartX(ex + dx + oy);
            arrow2.setStartY(ey + dy - ox);
        }
    }

    private static Runnable pointAddSupplier(List<Double> points, Double... doubles) {
        return () -> {
            for (Double aDouble : doubles) {
                points.add(aDouble);
            }
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

        Runnable startFromRight = pointAddSupplier(points,
                sourceXRange.upperEndpoint(), sourceMidY);

        Runnable startFromLeft = pointAddSupplier(points,
                sourceXRange.lowerEndpoint(), sourceMidY);

        Runnable endFromRight = pointAddSupplier(points,
                targetXRange.upperEndpoint(), targetMidY);

        Runnable endFromLeft = pointAddSupplier(points,
                targetXRange.lowerEndpoint(), targetMidY);

        double lineXMid = (sourceXRange.upperEndpoint() + targetXRange.lowerEndpoint()) / 2;
        Runnable midXLine = pointAddSupplier(points,
                lineXMid, sourceMidY, lineXMid, targetMidY);

        // (target ... source)
        if (sourceXRange.upperEndpoint() < targetXRange.lowerEndpoint()) { // completely right or
            // start
            startFromRight.run();

            // vertical line
            midXLine.run();

            // end
            endFromLeft.run();
        } else if (sourceXRange.lowerEndpoint() > targetXRange.upperEndpoint()) { // completely left
            startFromLeft.run();
            midXLine.run();
            endFromRight.run();
        } else if (sourceXRange.encloses(targetXRange)
                || sourceXRange.contains(targetXRange.lowerEndpoint())) { // completely contain or left border in
            startFromLeft.run();

            points.add(sourceXRange.lowerEndpoint() - evadeInterval);
            points.add(sourceMidY);

            points.add(sourceXRange.lowerEndpoint() - evadeInterval);
            points.add(targetMidY);

            endFromLeft.run();
        } else if (sourceXRange.contains(targetXRange.upperEndpoint())) { // right border in
            startFromRight.run();

            points.add(sourceXRange.upperEndpoint() + evadeInterval);
            points.add(sourceMidY);

            points.add(sourceXRange.upperEndpoint() + evadeInterval);
            points.add(targetMidY);

            endFromRight.run();
        }

        return points;
    }


}
