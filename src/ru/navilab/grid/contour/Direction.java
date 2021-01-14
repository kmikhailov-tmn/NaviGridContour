package ru.navilab.grid.contour;

enum Direction {
    UP (0, -1),
    DOWN (0, 1),
    LEFT (-1, 0),
    RIGHT (1, 0),
    DONE (0, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public Direction reversed() {
        switch (this) {
            case UP: return DOWN;
            case DOWN: return UP;
            case RIGHT: return LEFT;
            case LEFT: return RIGHT;
        }
        return DONE;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }
}
