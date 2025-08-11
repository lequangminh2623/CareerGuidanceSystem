package com.lqm.utils;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class CollectionUpdater {

    public static <T> void updateSet(
            Set<T> currentSet,
            Set<T> newSet,
            Consumer<T> addAction,
            Consumer<T> removeAction) {

        Objects.requireNonNull(currentSet, "currentSet cannot be null");

        // Xóa toàn bộ nếu newSet null
        if (newSet == null) {
            for (T item : new LinkedHashSet<>(currentSet)) {
                removeAction.accept(item);
            }
            return;
        }

        // Xóa phần tử không còn
        for (T item : new LinkedHashSet<>(currentSet)) {
            if (!newSet.contains(item)) {
                removeAction.accept(item);
            }
        }

        // Thêm phần tử mới
        for (T item : newSet) {
            if (!currentSet.contains(item)) {
                addAction.accept(item);
            }
        }
    }
}
