package com.benayn.ustyle.thirdparty;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Queues;
import com.google.common.eventbus.EventBus;

/**
 * @see https://github.com/toonetown/guava-ext
 */
public class ThreadsafeEventBus extends EventBus {
	
    /** An enum of possible actions */
    private enum Actions { POST, REGISTER, UNREGISTER, END }

    /** A flag that checks if an action is currently in-progress */
    private final AtomicBoolean actionInProgress = new AtomicBoolean();

    /** The outstanding queue that gets flushed */
    private final Queue<Action> actionQueue = Queues.newConcurrentLinkedQueue();
    
    /**
     * Returns a new {@link ThreadsafeEventBus} instance
     */
    public static ThreadsafeEventBus of(String name) {
    	return new ThreadsafeEventBus(name);
    }

    @Override public synchronized void post(final Object event) {
        if (actionInProgress.getAndSet(true)) {
            actionQueue.add(new Action(Actions.POST, event));
        } else {
            super.post(event);
            actionInProgress.set(false);
            flushQueue();
        }
    }

    @Override public synchronized void register(final Object object) {
        if (actionInProgress.getAndSet(true)) {
            actionQueue.add(new Action(Actions.REGISTER, object));
        } else {
            super.register(object);
            actionInProgress.set(false);
            flushQueue();
        }
    }

    @Override public synchronized void unregister(final Object object) {
        if (actionInProgress.getAndSet(true)) {
            actionQueue.add(new Action(Actions.UNREGISTER, object));
        } else {
            super.unregister(object);
            actionInProgress.set(false);
            flushQueue();
        }
    }
    
    private ThreadsafeEventBus(final String name) {
        super(name);
    }

    /** Flushes the queue */
    private void flushQueue() {
        while (true) {
            final Action act = firstNonNull(actionQueue.poll(), new Action(Actions.END, null));
            switch (act.action) {
                case POST: post(act.object); break;
                case REGISTER: register(act.object); break;
                case UNREGISTER: unregister(act.object); break;
                case END: default: return;
            }
        }
    }

    /** An object to track queued actions */
    private static class Action {
        private final Actions action;
        private final Object object;
        private Action(final Actions action, final Object object) {
            this.action = action;
            this.object = object;
        }
    }
}
