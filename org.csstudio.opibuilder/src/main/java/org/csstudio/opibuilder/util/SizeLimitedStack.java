/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.util;

import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * A stack with limited size. If the stack is full, the oldest element will be removed when new element was pushed.
 */
public class SizeLimitedStack<T> {
    private LinkedList<T> list = new LinkedList<>();
    private int sizeLimit;

    /**
     * Constructor
     *
     * @param sizeLimit
     *            the maximum number of elements in the stack. If the stack is full, the oldest element will be removed
     *            when new element was pushed.
     */
    public SizeLimitedStack(int sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    /**
     * Pushes an item onto the top of this stack.
     *
     * @param e
     *            the item to be pushed onto this stack.
     */
    public void push(T e) {
        if (list.size() >= sizeLimit) {
            list.removeFirst();
        }
        list.addLast(e);
    }

    /**
     * Removes the object at the top of this stack and returns that object as the value of this function.
     *
     * @return The object at the top of this stack (the last item of the {@code Vector} object).
     * @throws NoSuchElementException
     *             if this list is empty
     */

    public T pop() {
        return list.removeLast();
    }

    /**
     * Looks at the object at the top of this stack without removing it from the stack.
     *
     * @return the object at the top of this stack (the last item of the {@code Vector} object).
     * @throws NoSuchElementException
     *             if this list is empty
     */
    public T peek() {
        return list.getLast();
    }

    /**
     * Empty the stack.
     */
    public void clear() {
        list.clear();
    }

    /**
     * Return an array of all elements in the stack. The oldest element is the first element of the returned array.
     *
     * @return the array contained all elements in the stack.
     */
    public Object[] toArray() {
        return list.toArray();
    }

    /**
     * Returns the number of elements in this stack.
     *
     * @return the number of elements in this stack
     */
    public int size() {
        return list.size();
    }

    /**
     * Returns true if this stack contains the specified element. More formally, returns true if and only if this stack
     * contains at least one element {@code e} such that {@code (o==null ? e==null : o.equals(e))}.
     *
     * @param o
     *            element whose presence in this stack is to be tested
     * @return true if this list contains the specified element
     */
    public boolean contains(T o) {
        return list.contains(o);
    }
}
