package org.testd.fakerpp.core.util.earclipping;

import java.util.*;

@SuppressWarnings("WeakerAccess")
public final class PolygonTriangulator {

    static final class Vertex {

        Point point;
        int index;
        boolean isConvex;

        Vertex(Point point, int index, boolean isConvex) {
            this.point = point;
            this.index = index;
            this.isConvex = isConvex;
        }
    }

    private static final class HoleData {

        final int holeIndex;
        final int bridgeIndex;
        final Point bridgePoint;

        HoleData(int holeIndex, int bridgeIndex, Point bridgePoint) {
            this.holeIndex = holeIndex;
            this.bridgeIndex = bridgeIndex;
            this.bridgePoint = bridgePoint;
        }
    }

    public static List<Triangle> triangulate(Polygon polygon) {
        final DoublyLinkedList<Vertex> vertices = generateVertexList(polygon);
        final Triangle[] triangles = new Triangle[((polygon.numPoints + (2 * polygon.numHoles)) - 2)];

        int triIndex = 0;
        while (vertices.getCount() >= 3) {
            boolean hasRemovedEarThisIteration = false;
            DoublyLinkedList.Node<Vertex> vertexNode = vertices.getFirst();
            for (int i = 0; i < vertices.getCount(); i++) {
                DoublyLinkedList.Node<Vertex> prevVertexNode = (vertexNode.getPrevious() != null)? vertexNode.getPrevious() : vertices.getLast();
                DoublyLinkedList.Node<Vertex> nextVertexNode = (vertexNode.getNext() != null)? vertexNode.getNext() : vertices.getFirst();

                if (vertexNode.getValue().isConvex) {
                    if (!triangleContainsVertex(vertices, prevVertexNode.getValue(), vertexNode.getValue(), nextVertexNode.getValue())) {
                        // check if removal of ear makes prev/next vertex convex (if was previously reflex)
                        if (!prevVertexNode.getValue().isConvex) {
                            DoublyLinkedList.Node<Vertex> prevOfPrev = (prevVertexNode.getPrevious() != null)? prevVertexNode.getPrevious() : vertices.getLast();

                            prevVertexNode.getValue().isConvex = Maths2D.isConvex(prevOfPrev.getValue().point, prevVertexNode.getValue().point, nextVertexNode.getValue().point);
                        }
                        if (!nextVertexNode.getValue().isConvex) {
                            DoublyLinkedList.Node<Vertex> nextOfNext = (nextVertexNode.getNext() != null)? nextVertexNode.getNext() : vertices.getFirst();
                            nextVertexNode.getValue().isConvex = Maths2D.isConvex(prevVertexNode.getValue().point, nextVertexNode.getValue().point, nextOfNext.getValue().point);
                        }

                        Triangle triangle = new Triangle(nextVertexNode.getValue().point, vertexNode.getValue().point, prevVertexNode.getValue().point);
                        triangles[triIndex] = triangle;
                        triIndex += 1;

                        hasRemovedEarThisIteration = true;
                        vertices.remove(vertexNode);
                        break;
                    }
                }

                vertexNode = nextVertexNode;
            }

            if (!hasRemovedEarThisIteration) {
                return null;
            }
        }

        return Arrays.asList(triangles);
    }

    static DoublyLinkedList<Vertex> generateVertexList(Polygon polygon) {
        //新建一个双向链表
        final DoublyLinkedList<Vertex> vertexList = new DoublyLinkedList<>();

        // 将所有的外点添加进去
        for (int i = 0; i < polygon.numHullPoints; i++) {
            int prevPointIndex = (i - 1 + polygon.numHullPoints) % polygon.numHullPoints;
            int nextPointIndex = (i + 1) % polygon.numHullPoints;

            boolean vertexIsConvex = Maths2D.isConvex(polygon.points[prevPointIndex], polygon.points[i], polygon.points[nextPointIndex]);
            Vertex currentHullVertex = new Vertex(polygon.points[i], i, vertexIsConvex);

            vertexList.addToLast(currentHullVertex);
        }

        final ArrayList<HoleData> holeDataList = new ArrayList<>();
        for (int holeIndex = 0; holeIndex < polygon.numHoles; holeIndex++)
        {
            // Find index of rightmost point in hole. This 'bridge' point is where the hole will be connected to the hull.
            Point holeBridgePoint = new Point(Double.MIN_VALUE, 0);
            int holeBridgeIndex = 0;
            for (int i = 0; i < polygon.numPointsPerHole[holeIndex]; i++)
            {
                if (polygon.getHolePoint(i, holeIndex).x > holeBridgePoint.x)
                {
                    holeBridgePoint = polygon.getHolePoint(i, holeIndex);
                    holeBridgeIndex = i;
                }
            }
            holeDataList.add(new HoleData(holeIndex, holeBridgeIndex, holeBridgePoint));
        }
        Collections.sort(holeDataList, new Comparator<HoleData>() {
            @Override
            public int compare(HoleData o1, HoleData o2) {
                return (o1.bridgePoint.x > o2.bridgePoint.x)? -1 : 1;
            }
        });

        for (HoleData holeData : holeDataList) {
            // Find first edge which intersects with rightwards ray originating at the hole bridge point.
            Point rayIntersectPoint = new Point(Double.MAX_VALUE, holeData.bridgePoint.y);
            ArrayList<DoublyLinkedList.Node<Vertex>> hullNodesPotentiallyInBridgeTriangle = new ArrayList<>();
            DoublyLinkedList.Node<Vertex> initialBridgeNodeOnHull = null;
            DoublyLinkedList.Node<Vertex> currentNode = vertexList.getFirst();

            while (currentNode != null) {
                DoublyLinkedList.Node<Vertex> nextNode = (currentNode.getNext() == null) ? vertexList.getFirst() : currentNode.getNext();
                Point p0 = currentNode.getValue().point;
                Point p1 = nextNode.getValue().point;

                // at least one point must be to right of holeData.bridgePoint for intersection with ray to be possible
                if (p0.x > holeData.bridgePoint.x || p1.x > holeData.bridgePoint.x) {
                    // one point is above, one point is below
                    if (p0.y > holeData.bridgePoint.y != p1.y > holeData.bridgePoint.y) {
                        double rayIntersectX = p1.x; // only true if line p0,p1 is vertical
                        if (!Math2.approximatelyEqual(p0.x, p1.x)) {
                            double intersectY = holeData.bridgePoint.y;
                            double gradient = (p0.y - p1.y) / (p0.x - p1.x);
                            double c = p1.y - gradient * p1.x;
                            rayIntersectX = (intersectY - c) / gradient;
                        }

                        // intersection must be to right of bridge point
                        if (rayIntersectX > holeData.bridgePoint.x) {
                            DoublyLinkedList.Node<Vertex> potentialNewBridgeNode = (p0.x > p1.x) ? currentNode : nextNode;
                            // if two intersections occur at same x position this means is duplicate edge
                            // duplicate edges occur where a hole has been joined to the outer polygon
                            boolean isDuplicateEdge = Math2.approximatelyEqual(rayIntersectX, rayIntersectPoint.x);

                            // connect to duplicate edge (the one that leads away from the other, already connected hole, and back to the original hull) if the
                            // current hole's bridge point is higher up than the bridge point of the other hole (so that the new bridge connection doesn't intersect).
                            boolean connectToThisDuplicateEdge = holeData.bridgePoint.y > potentialNewBridgeNode.getPrevious().getValue().point.y;

                            if (!isDuplicateEdge || connectToThisDuplicateEdge) {
                                // if this is the closest ray intersection thus far, set bridge hull node to point in line having greater x pos (since def to right of hole).
                                if (rayIntersectX < rayIntersectPoint.x || isDuplicateEdge) {
                                    rayIntersectPoint.x = rayIntersectX;
                                    initialBridgeNodeOnHull = potentialNewBridgeNode;
                                }
                            }
                        }
                    }
                }

                // Determine if current node might lie inside the triangle formed by holeBridgePoint, rayIntersection, and bridgeNodeOnHull
                // We only need consider those which are reflex, since only these will be candidates for visibility from holeBridgePoint.
                // A list of these nodes is kept so that in next step it is not necessary to iterate over all nodes again.
                if (currentNode != initialBridgeNodeOnHull) {
                    if (!currentNode.getValue().isConvex && p0.x > holeData.bridgePoint.x) {
                        hullNodesPotentiallyInBridgeTriangle.add(currentNode);
                    }
                }
                currentNode = currentNode.getNext();
            }

            // Check triangle formed by hullBridgePoint, rayIntersection, and bridgeNodeOnHull.
            // If this triangle contains any points, those points compete to become new bridgeNodeOnHull
            DoublyLinkedList.Node<Vertex> validBridgeNodeOnHull = initialBridgeNodeOnHull;
            for (DoublyLinkedList.Node<Vertex> nodePotentiallyInTriangle : hullNodesPotentiallyInBridgeTriangle)
            {
                if (nodePotentiallyInTriangle.getValue().index == initialBridgeNodeOnHull.getValue().index) {
                    continue;
                }
                // if there is a point inside triangle, this invalidates the current bridge node on hull.
                if (Maths2D.pointInTriangle(holeData.bridgePoint, rayIntersectPoint, initialBridgeNodeOnHull.getValue().point, nodePotentiallyInTriangle.getValue().point)) {
                    // Duplicate points occur at hole and hull bridge points.
                    boolean isDuplicatePoint = validBridgeNodeOnHull.getValue().point == nodePotentiallyInTriangle.getValue().point;

                    // if multiple nodes inside triangle, we want to choose the one with smallest angle from holeBridgeNode.
                    // if is a duplicate point, then use the one occurring later in the list
                    double currentDstFromHoleBridgeY = Math.abs(holeData.bridgePoint.y - validBridgeNodeOnHull.getValue().point.y);
                    double pointInTriDstFromHoleBridgeY = Math.abs(holeData.bridgePoint.y - nodePotentiallyInTriangle.getValue().point.y);

                    if (pointInTriDstFromHoleBridgeY < currentDstFromHoleBridgeY || isDuplicatePoint) {
                        validBridgeNodeOnHull = nodePotentiallyInTriangle;
                    }
                }
            }

            // Insert hole points (starting at holeBridgeNode) into vertex list at validBridgeNodeOnHull
            currentNode = validBridgeNodeOnHull;
            for (int i = holeData.bridgeIndex; i <= polygon.numPointsPerHole[holeData.holeIndex] + holeData.bridgeIndex; i++)
            {
                int previousIndex = currentNode.getValue().index;
                int currentIndex = polygon.indexOfPointInHole(i % polygon.numPointsPerHole[holeData.holeIndex], holeData.holeIndex);
                int nextIndex = polygon.indexOfPointInHole((i + 1) % polygon.numPointsPerHole[holeData.holeIndex], holeData.holeIndex);

                if (i == polygon.numPointsPerHole[holeData.holeIndex] + holeData.bridgeIndex) // have come back to starting point
                {
                    nextIndex = validBridgeNodeOnHull.getValue().index; // next point is back to the point on the hull
                }

                boolean vertexIsConvex = Maths2D.isConvex(polygon.points[previousIndex], polygon.points[currentIndex], polygon.points[nextIndex]);
                Vertex holeVertex = new Vertex(polygon.points[currentIndex], currentIndex, vertexIsConvex);
                currentNode = vertexList.addAfter(currentNode, holeVertex);
            }

            // Add duplicate hull bridge vert now that we've come all the way around. Also set its concavity
            Point nextVertexPos = (currentNode.getNext() == null) ? vertexList.getFirst().getValue().point : currentNode.getNext().getValue().point;
            boolean isConvex = Maths2D.isConvex(holeData.bridgePoint, validBridgeNodeOnHull.getValue().point, nextVertexPos);
            Vertex repeatStartHullVert = new Vertex(validBridgeNodeOnHull.getValue().point, validBridgeNodeOnHull.getValue().index, isConvex);
            vertexList.addAfter(currentNode, repeatStartHullVert);

            //Set concavity of initial hull bridge vert, since it may have changed now that it leads to hole vert
            DoublyLinkedList.Node<Vertex> nodeBeforeStartBridgeNodeOnHull = (validBridgeNodeOnHull.getPrevious() == null) ? vertexList.getLast() : validBridgeNodeOnHull.getPrevious();
            DoublyLinkedList.Node<Vertex> nodeAfterStartBridgeNodeOnHull = (validBridgeNodeOnHull.getNext() == null) ? vertexList.getFirst() : validBridgeNodeOnHull.getNext();
            validBridgeNodeOnHull.getValue().isConvex = Maths2D.isConvex(nodeBeforeStartBridgeNodeOnHull.getValue().point, validBridgeNodeOnHull.getValue().point, nodeAfterStartBridgeNodeOnHull.getValue().point);
        }

        return vertexList;
    }

    private static boolean triangleContainsVertex(DoublyLinkedList<Vertex> vertices, Vertex v0, Vertex v1, Vertex v2) {
        DoublyLinkedList.Node<Vertex> vertexNode = vertices.getFirst();
        for (int i = 0; i < vertices.getCount(); i++) {
            if (!vertexNode.getValue().isConvex) { // convex verts will never be inside triangle
                Vertex vertexToCheck = vertexNode.getValue();
                if (vertexToCheck.index != v0.index && vertexToCheck.index != v1.index && vertexToCheck.index != v2.index) { // dont check verts that make up triangle
                    if (Maths2D.pointInTriangle(v0.point, v1.point, v2.point, vertexToCheck.point)) {
                        return true;
                    }
                }
            }
            vertexNode = vertexNode.getNext();
        }

        return false;
    }
}