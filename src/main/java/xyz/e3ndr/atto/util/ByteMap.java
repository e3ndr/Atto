package xyz.e3ndr.atto.util;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ByteMap {
    private @Getter byte[] array;

    public void ensureCapacity(int x) {
        if (this.array.length < x) {
            this.array = Arrays.copyOf(this.array, x);
        }
    }

    public void set(int x, byte b) {
        this.ensureCapacity(x + 1);

        this.array[x] = b;
    }

    public void insert(int x, byte b) {
        this.ensureCapacity(x);

        this.array = addPos(this.array, x, b);
    }

    public void remove(int x) {
        this.ensureCapacity(x);

        this.array = removePos(this.array, x);
    }

    public int length() {
        return this.array.length;
    }

    public byte getbyte(int x) {
        if (x < this.array.length) {
            return this.array[x];
        } else {
            return 0;
        }
    }

    public static byte[] addPos(byte[] arr, int index, byte b) {
        byte[] result = new byte[arr.length];

        System.arraycopy(arr, 0, result, 0, index);
        System.arraycopy(arr, index, result, index + 1, arr.length - index - 1);

        result[index] = b;

        return result;
    }

    public static byte[] removePos(byte[] arr, int index) {
        byte[] result = new byte[arr.length];

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
