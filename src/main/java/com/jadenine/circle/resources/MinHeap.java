package com.jadenine.circle.resources;

import com.jadenine.circle.entity.Bomb;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by linym on 8/11/15.
 */
class MinHeap {
    final ArrayList<Map.Entry<String, LinkedList<Bomb>>> heap;
    final int k;

    MinHeap(ArrayList<Map.Entry<String, LinkedList<Bomb>>> heap) {
        this.heap = heap;
        this.k = heap.size();
        if (k <= 0) {
            throw new InvalidParameterException("K <= 0");
        }
        build();
    }

    private void build() {
        for (int i = k / 2 - 1; i >= 0; i--) {
            heapify(i);
        }
    }

    private void heapify(int pos) {
        int l = pos << 1 + 1;
        int r = pos << 1 + 2;
        int smallestIndex = pos;
        if (l < k && heap.get(l).getValue().size() < heap.get(smallestIndex).getValue().size()) {
            smallestIndex = l;
        }

        if (r < k && heap.get(r).getValue().size() < heap.get(smallestIndex).getValue().size()) {
            smallestIndex = r;
        }

        if (pos == smallestIndex) {
            return;
        }

        swap(pos, smallestIndex);
        heapify(smallestIndex);
    }

    private void swap(int pos, int smallestIndex) {
        Map.Entry<String, LinkedList<Bomb>> tmp = heap.get(pos);
        heap.set(pos, heap.get(smallestIndex));
        heap.set(smallestIndex, tmp);
    }

    public void offer(Map.Entry<String, LinkedList<Bomb>> entry) {
        Map.Entry<String, LinkedList<Bomb>> top = heap.get(0);
        if (entry.getValue().size() > top.getValue().size()) {
            heap.set(0, entry);
            heapify(0);
        }
    }

    public String[] top() {
        String[] topk = new String[k];
        int i = 0;
        for (Map.Entry<String, LinkedList<Bomb>> entry : heap) {
            topk[i++] = entry.getKey();
        }
        return topk;
    }
}
