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
            m_path = calculatePath(world, source, target, m_terminateDist);
        }
    }
    public Point2D[] getPath() {
        return m_path;
    }
    public static Point2D[] calculatePath(World world, Point2D source, Point2D target, int terminateDist) {
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
                if (node.x < 0 || node.x >= mapWidth || closed[index] || map[index] != 0) continue;
                node.prev = parent;
                node.g += costNormal;
                node.h = costNormal * heuristic(node.x, targetX, node.y, targetY);
                if (node.h > terminateDist) continue;
                open.add(node);
            }
            // walk vertically
            for (int y = -1; y <= 1; y += 2) {
                PathNode node = parent.clone();
                node.y += y;
                int index = node.y * mapWidth + node.x;
                if (node.y < 0 || node.y >= mapHeight || closed[index] || map[index] != 0) continue;
                node.prev = parent;
                node.g += costNormal;
                node.h = costNormal * heuristic(node.x, targetX, node.y, targetY);
                if (node.h > terminateDist) continue;
                open.add(node);
            }
        }

        return null;
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
}