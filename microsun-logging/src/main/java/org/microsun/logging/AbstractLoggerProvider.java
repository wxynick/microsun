package org.microsun.logging;

import java.util.ArrayDeque;

abstract class AbstractLoggerProvider {

    private final ThreadLocal<ArrayDeque<Entry>> ndcStack = new ThreadLocal<ArrayDeque<Entry>>();

    public void clearNdc() {
        ArrayDeque<Entry> stack = ndcStack.get();
        if (stack != null)
            stack.clear();
    }

    public String getNdc() {
        ArrayDeque<Entry> stack = ndcStack.get();
        return stack == null || stack.isEmpty() ? null : stack.peek().merged;
    }

    public int getNdcDepth() {
        ArrayDeque<Entry> stack = ndcStack.get();
        return stack == null ? 0 : stack.size();
    }

    public String peekNdc() {
        ArrayDeque<Entry> stack = ndcStack.get();
        return stack == null || stack.isEmpty() ? "" : stack.peek().current;
    }

    public String popNdc() {
        ArrayDeque<Entry> stack = ndcStack.get();
        return stack == null || stack.isEmpty() ? "" : stack.pop().current;
    }

    public void pushNdc(String message) {
        ArrayDeque<Entry> stack = ndcStack.get();
        if (stack == null) {
            stack = new ArrayDeque<Entry>();
            ndcStack.set(stack);
        }
        stack.push(stack.isEmpty() ? new Entry(message) : new Entry(stack.peek(), message));
    }

    public void setNdcMaxDepth(int maxDepth) {
        final ArrayDeque<Entry> stack = ndcStack.get();
        if (stack != null) while (stack.size() > maxDepth) stack.pop();
    }

    private static class Entry {

        private String merged;
        private String current;

        Entry(String current) {
            merged = current;
            this.current = current;
        }

        Entry(Entry parent, String current) {
            merged = parent.merged + ' ' + current;
            this.current = current;
        }
    }
}
