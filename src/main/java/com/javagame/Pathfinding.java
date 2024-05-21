package com.javagame;

import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Collections;

final public class Pathfinding {
    public Pathfinding(int terminateDist) {
        m_terminateDist = terminateDist;
        m_path = null;
        m_elapsed = 0;
    }
    public void update(World world, Point2D source, Point2D target, double dt) {
        m_elapsed += dt;
        if (m_elapsed > 1000) {
            m_elapsed = 0;
            m_path = calculatePath(world, source, target, m_terminateDist, m_ignoreBlockedTarget);
        }
    }
    public Point2D[] getPath() {
        return m_path;
    }
    public void ignoreIfTargetIsBlocked(boolean ignore) {
        m_ignoreBlockedTarget = ignore;
    }
    public static Point2D[] calculatePath(World world, Point2D source, Point2D target, int terminateDist, boolean ignoreBlockedTarget) {
        int[] map = world.getMap();
        int mapWidth = world.getMapWidth();
        int mapHeight = world.getMapHeight();
        int sourceX = (int)source.getX();
        int sourceY = (int)source.getY();
        int targetX = (int)target.getX();
        int targetY = (int)target.getY();
        int costNormal = 10;

        // check if in bounds
        if (sourceX < 0 || sourceX >= mapWidth || sourceY < 0 || sourceY >= mapHeight ||
        targetX < 0 || targetX >= mapWidth || targetX < 0 || targetX >= mapHeight) return null;

        boolean[] closed = new boolean[map.length];
        PriorityQueue<PathNode> open = new PriorityQueue<>((a, b) -> (a.g + a.h) - (b.g + b.h));
        open.add(new PathNode(sourceX, sourceY, null, 0, 0));

        while (!open.isEmpty()) {
            PathNode parent = open.poll();

            // recreate path if target hit
            if (parent.x == targetX && parent.y == targetY) {
                ArrayList<Point2D> path = new ArrayList<>();
                PathNode current = parent;
                while (current.prev != null) { // ignore last tile
                    path.add(new Point2D(current.x + 0.5, current.y + 0.5));
                    current = current.prev;
                }
                if (path.isEmpty()) return null;
                Collections.reverse(path); // reverse so it starts from begining
                return path.toArray(new Point2D[0]);
            }
            closed[parent.y * mapWidth + parent.x] = true;

            // walk horizontally
            for (int x = -1; x <= 1; x += 2) {
                PathNode node = parent.clone();
                node.x += x;
                int index = node.y * mapWidth + node.x;
                if (node.x < 0 || node.x >= mapWidth || closed[index]) continue;
                if (ignoreBlockedTarget) {
                    if ((node.x != targetX || node.y != targetY) && map[index] != 0) continue;
                } else if (map[index] != 0) continue;
                node.prev = parent;
                node.g += costNormal;
                node.h = costNormal * heuristic(node.x, targetX, node.y, targetY);
                if (node.h > terminateDist) continue;
                closed[node.y * mapWidth + node.x] = true;
                open.add(node);
            }
            // walk vertically
            for (int y = -1; y <= 1; y += 2) {
                PathNode node = parent.clone();
                node.y += y;
                int index = node.y * mapWidth + node.x;
                if (node.y < 0 || node.y >= mapHeight || closed[index]) continue;
                if (ignoreBlockedTarget) {
                    if ((node.x != targetX || node.y != targetY) && map[index] != 0) continue;
                } else if (map[index] != 0) continue;
                node.prev = parent;
                node.g += costNormal;
                node.h = costNormal * heuristic(node.x, targetX, node.y, targetY);
                if (node.h > terminateDist) continue;
                closed[node.y * mapWidth + node.x] = true;
                open.add(node);
            }
            // walk diagonally
            for (int x = -1; x <= 1; x += 2) {
                for (int y = -1; y <= 1; y += 2) {
                    PathNode node = parent.clone();
                    node.x += x;
                    node.y += y;
                    int index = node.y * mapWidth + node.x;
                    if (node.x < 0 || node.x >= mapWidth || node.y < 0 || node.y >= mapHeight || closed[index]) continue;
                    if (ignoreBlockedTarget) {
                        if ((node.x != targetX || node.y != targetY) && map[index] != 0) continue;
                    } else if (map[index] != 0) continue;
                    if (map[index - x] != 0 && map[index - y * mapWidth] != 0) continue; // skip if blocked
                    node.prev = parent;
                    node.g += costNormal;
                    node.h = costNormal * heuristic(node.x, targetX, node.y, targetY);
                    if (node.h > terminateDist) continue;
                    closed[node.y * mapWidth + node.x] = true;
                    open.add(node);
                }
            }
        }

        return null;
    }
    public static Point2D predictImpactPosition(Point2D targetPos, Point2D targetVel, Point2D originPos, double originSpeed) {
        double a = targetVel.dotProduct(targetVel) - originSpeed * originSpeed;
        double b = 2 * targetVel.dotProduct(targetPos.subtract(originPos));
        double c = targetPos.subtract(originPos).dotProduct(targetPos.subtract(originPos));
        double d = b * b - 4 * a * c;

        if (d >= 0) { // can hit the target
            double t1 = (-b + Math.sqrt(d)) / (2 * a);
            double t2 = (-b - Math.sqrt(d)) / (2 * a);
            double t = Math.min(t1, t2);
            if (t < 0) t = Math.max(t1, t2);
            if (t >= 0) {
                targetPos = targetPos.add(targetVel.multiply(t));
            }
        }
        return targetPos;
    }
    public static double castRay(World world, Point2D pos, Point2D dir) {
        boolean hit = false;
        // setup dda
        int gridX = (int)pos.getX();
        int gridY = (int)pos.getY();
        Point2D deltaDist = new Point2D(dir.getX() == 0 ? Double.MAX_VALUE : Math.abs(1.0 / dir.getX()),
                                        dir.getY() == 0 ? Double.MAX_VALUE : Math.abs(1.0 / dir.getY()));
        int rayStepX = dir.getX() > 0 ? 1 : -1;
        int rayStepY = dir.getY() > 0 ? 1 : -1;
        Point2D sideDist = new Point2D(
            (rayStepX * ((double)gridX - pos.getX()) + (double)rayStepX * 0.5 + 0.5) * deltaDist.getX(),
            (rayStepY * ((double)gridY - pos.getY()) + (double)rayStepY * 0.5 + 0.5) * deltaDist.getY()
        );
        int hitDir = 1;
        // raycast
        int[] map = world.getMap();
        while (gridX >= 0 && gridY >= 0 && gridX < world.getMapWidth() && gridY < world.getMapHeight()) {
            int tileIndex = gridY * world.getMapWidth() + gridX;
            int tile = map[tileIndex];
            if (tile != 0) {
                hit = true;
                break;
            }
            if (sideDist.getX() < sideDist.getY()) {
                sideDist = sideDist.add(deltaDist.getX(), 0);
                gridX += rayStepX;
                hitDir = 1;
            } else {
                sideDist = sideDist.add(0, deltaDist.getY());
                gridY += rayStepY;
                hitDir = 2;
            }
        }

        if (hit) {
            double hitDist = hitDir == 1 ? sideDist.getX() - deltaDist.getX() : sideDist.getY() - deltaDist.getY();
            //hitDist = sideDist.magnitude();
            if (hitDist <= 0) hitDist = 0.001;
            return hitDist;
        }

        return Double.MAX_VALUE;
    }
    private static int heuristic(int x1, int x2, int y1, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
    private static class PathNode {
        public PathNode(int x, int y, PathNode prev, int g, int h) {
            this.x = x;
            this.y = y;
            this.prev = prev;
            this.g = g;
            this.h = h;
        }
        public PathNode clone() {
            return new PathNode(x, y, prev, g, h);
        }
        public int x;
        public int y;
        public PathNode prev;
        public int g;
        public int h;
    };

    private int m_terminateDist;
    private Point2D[] m_path;
    private double m_elapsed;
    private boolean m_ignoreBlockedTarget = false;
}