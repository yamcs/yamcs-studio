/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.csstudio.swt.widgets.util;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.ButtonModel;
import org.eclipse.draw2d.ChangeEvent;
import org.eclipse.draw2d.ChangeListener;
import org.eclipse.draw2d.EventListenerList;
import org.eclipse.swt.widgets.Display;

/**
 * A model for buttons containing several properties, including enabled, pressed, selected, rollover enabled and
 * mouseover.
 */
public class RapButtonModel extends ButtonModel {

    private EventListenerList eventListeners = new EventListenerList();
    //
    // /**
    // * Listens to button state transitions and fires action performed events
    // * based on the desired behavior ({@link #DEFAULT_FIRING_BEHAVIOR} or
    // * {@link #REPEAT_FIRING_BEHAVIOR}).
    // */
    protected ButtonStateTransitionListener firingBehavior;

    /**
     * Registers the given listener as a ButtonStateTransitionListener.
     *
     * @param listener
     *            The ButtonStateTransitionListener to add
     * @since 2.0
     */
    public void addStateTransitionListener(
            ButtonStateTransitionListener listener) {
        if (listener == null)
            throw new IllegalArgumentException();
        if (eventListeners == null)
            eventListeners = new EventListenerList();
        eventListeners.addListener(ButtonStateTransitionListener.class, listener);
    }

    /**
     * Notifies any ActionListeners on this ButtonModel that an action has been performed.
     *
     * @since 2.0
     */
    @Override
    protected void fireActionPerformed() {
        super.fireActionPerformed();
        Iterator<?> iter = eventListeners.getListeners(ActionListener.class);
        ActionEvent action = new ActionEvent(this);
        while (iter.hasNext())
            ((ActionListener) iter.next()).actionPerformed(action);
    }

    /**
     * Notifies any listening ButtonStateTransitionListener that this button has been pressed.
     *
     * @since 2.0
     */
    @Override
    protected void firePressed() {
        super.firePressed();
        Iterator<?> iter = eventListeners
                .getListeners(ButtonStateTransitionListener.class);
        while (iter.hasNext())
            ((ButtonStateTransitionListener) iter.next()).pressed();
    }

    /**
     * Notifies any listening ButtonStateTransitionListener that this button has been released.
     *
     * @since 2.0
     */
    @Override
    protected void fireReleased() {
        super.fireReleased();
        Iterator<?> iter = eventListeners
                .getListeners(ButtonStateTransitionListener.class);
        while (iter.hasNext())
            ((ButtonStateTransitionListener) iter.next()).released();
    }

    /**
     * Notifies any listening ButtonStateTransitionListener that this button has been cancelled.
     *
     * @since 2.0
     */
    @Override
    protected void fireCanceled() {
        super.fireCanceled();
        Iterator<?> iter = eventListeners
                .getListeners(ButtonStateTransitionListener.class);
        while (iter.hasNext())
            ((ButtonStateTransitionListener) iter.next()).canceled();
    }

    /**
     * Notifies any listening ChangeListeners that this button's state has changed.
     *
     * @param property
     *            The name of the property that changed
     * @since 2.0
     */
    @Override
    protected void fireStateChanged(String property) {
        super.fireStateChanged(property);
        Iterator<?> iter = eventListeners.getListeners(ChangeListener.class);
        ChangeEvent change = new ChangeEvent(this, property);
        while (iter.hasNext())
            ((ChangeListener) iter.next()).handleStateChanged(change);
    }

    /**
     * Removes the given ButtonStateTransitionListener.
     *
     * @param listener
     *            The ButtonStateTransitionListener to remove
     * @since 2.0
     */
    public void removeStateTransitionListener(
            ButtonStateTransitionListener listener) {
        if (eventListeners == null)
            eventListeners = new EventListenerList();
        eventListeners.removeListener(ButtonStateTransitionListener.class, listener);
    }

    /**
     * Sets the firing behavior for this button. {@link #DEFAULT_FIRING_BEHAVIOR} is the default behavior, where action
     * performed events are not fired until the mouse button is released. {@link #REPEAT_FIRING_BEHAVIOR} causes action
     * performed events to fire repeatedly until the mouse button is released.
     *
     * @param type
     *            The firing behavior type
     * @since 2.0
     */
    @Override
    public void setFiringBehavior(int type) {
        if (firingBehavior != null)
            removeStateTransitionListener(firingBehavior);
        switch (type) {
        case REPEAT_FIRING_BEHAVIOR:
            firingBehavior = new RepeatFiringBehavior();
            break;
        default:
            firingBehavior = new DefaultFiringBehavior();
        }
        addStateTransitionListener(firingBehavior);
    }

    class DefaultFiringBehavior extends ButtonStateTransitionListener {
        @Override
        public void released() {
            fireActionPerformed();
        }
    }

    class RepeatFiringBehavior extends ButtonStateTransitionListener {
        protected static final int INITIAL_DELAY = 250, STEP_DELAY = 40;

        protected int stepDelay = INITIAL_DELAY, initialDelay = STEP_DELAY;

        protected Timer timer;

        @Override
        public void pressed() {
            fireActionPerformed();
            if (!isEnabled())
                return;

            timer = new Timer();
            TimerTask runAction = new Task(timer);

            timer.scheduleAtFixedRate(runAction, INITIAL_DELAY, STEP_DELAY);
        }

        @Override
        public void canceled() {
            suspend();
        }

        @Override
        public void released() {
            suspend();
        }

        @Override
        public void resume() {
            timer = new Timer();

            TimerTask runAction = new Task(timer);

            timer.scheduleAtFixedRate(runAction, STEP_DELAY, STEP_DELAY);
        }

        @Override
        public void suspend() {
            if (timer == null)
                return;
            timer.cancel();
            timer = null;
        }
    }

    class Task extends TimerTask {

        private Timer timer;

        private Display display;

        public Task(Timer timer) {
            this.timer = timer;
            this.display = Display.getCurrent();
        }

        @Override
        public void run() {
            display.syncExec(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (!isEnabled())
                                timer.cancel();
                            fireActionPerformed();
                        }
                    });
        }
    }
}
