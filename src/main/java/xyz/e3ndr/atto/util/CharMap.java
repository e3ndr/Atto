package xyz.e3ndr.atto.util;

import java.util.Arrays;

public class CharMap {
    private char[][] map = new char[0][];

    public void ensureCapacity(int x, int y) {
        if (this.map.length < (y + 1)) {
            this.map = Arrays.copyOf(this.map, y + 1);
        }

        if (this.map[y] == null) {
            this.map[y] = new char[x];

            Arrays.fill(this.map[y], ' ');
        } else if (this.map[y].length < x) {
            char[] newX = new char[x];
            char[] oldX = this.map[y];

            for (int i = 0; i != x; i++) {
                if (i < oldX.length) {
                    newX[i] = oldX[i];
                } else {
                    newX[i] = ' ';
                }
            }

            this.map[y] = newX;
        }
    }

    public void setLine(int y, char[] line) {
        this.ensureCapacity(line.length, y);

        this.map[y] = line;
    }

    public void set(int x, int y, char c) {
        this.ensureCapacity(x, y);

        this.map[y][x] = c;
    }

    public void insert(int x, int y, char c) {
        this.ensureCapacity(x, y);
        this.ensureCapacity(this.map[y].length + 1, y);

        this.map[y] = addPos(this.map[y], x, c);
    }

    public void remove(int x, int y) {
        this.ensureCapacity(x, y);
        this.ensureCapacity(this.map[y].length + 1, y);

        this.map[y] = removePos(this.map[y], x);
    }

    public void clearLine(int y) {
        this.map[y] = new char[0];
    }

    public int height() {
        return this.map.length;
    }

    public int getLength(int y) {
        return (y >= this.map.length) ? 0 : this.map[y].length;
    }

    public String[] string(int left, int top, int height, int width, boolean fileOut) {
        String[] lines = new String[height];

        int num = 0;
        for (int y = top; y != height + top; y++) {
            StringBuilder line = new StringBuilder();

            if (y < this.map.length) {
                char[] lineArr = this.map[y];

                for (int x = left; x != width + left; x++) {
                    if ((lineArr == null) || (x >= lineArr.length)) {
                        if (!fileOut) {
                            line.append(' ');
                        } else {
                            break;
                        }
                    } else {
                        line.append(lineArr[x]);
                    }
                }
            } else if (!fileOut) {
                for (int x = 0; x != width + left; x++) {
                    line.append(' ');
                }
            }

            lines[num] = fileOut ? line.toString().replaceFirst("\\s++$", "") : line.toString();

            num++;
        }

        return lines;
    }

    public static char[] addPos(char[] arr, int index, char c) {
        char[] result = new char[arr.length];

        System.arraycopy(arr, 0, result, 0, index);
        System.arraycopy(arr, index, result, index + 1, arr.length - index - 1);

        result[index] = c;

        return result;
    }

    public static char[] removePos(char[] arr, int index) {
        char[] result = new char[arr.length];

        int newIndex = 0;
        for (int i = 0; i != arr.length; i++) {
            if (i != index) {
                result[newIndex++] = arr[i];
            }
        }

        result[newIndex] = ' ';

        return result;
    }

}
