package com.benayn.ustyle.thirdparty;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Function;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * @see https://github.com/toonetown/guava-ext
 */
public class Threads {
    
    /**
     * Returns a ListeningExecutorService based off of the global ThreadFactory
     */
    public static ListeningExecutorService executor() {
        final ExecutorService svc = Executors.newCachedThreadPool(MoreExecutors.platformThreadFactory());
        return MoreExecutors.listeningDecorator(svc);
    }
    
    /**
     * Returns a ListenableFuture for the given Async object
     */
    public static <T> ListenableFuture<T> future(final Async<T> async) {
        return executor().submit(async);
    }
    
    /**
     * Returns a CheckedFuture for the given CheckedAsync object
     */
    public static <T, X extends Exception> CheckedFuture<T, X> checkedFuture(final CheckedAsync<T, X> async) {
        return Futures.makeChecked(future(async), async);
    }
    
    /**
     * A class which can be extended in order to return ListenableFutures. 
     */
    public static abstract class Async<T> implements Callable<T> { }
    
    /**
     * A class which can be extended in order to return CheckedFutures
     */
    public static abstract class CheckedAsync<T, X> extends Async<T> implements Function<Exception, X> {
        private final Class<X> exceptionClass;
        
        public CheckedAsync(Class<X> exceptionClass) {
        	this.exceptionClass = exceptionClass;
        }
        
        @Override public X apply(final Exception e) {
            Throwable t = e;
            while (t != null && t instanceof ExecutionException) {
                t = t.getCause();
            }
            
            return exceptionClass.cast(t);
        }
    }
    
    /**
     * Returns a new {@link AsyncRunner} instance with given {@link Runnable}
     */
    public static AsyncRunner of(Runnable runnable) {
    	return new AsyncRunner(runnable);
    }
    
    /**
     * 
     */
    public static class AsyncRunner {

        /** The runnable that we will actually run */
        private final Runnable runnable;

        /** An atomic reference that we can synchronize on */
        private final AtomicReference<ListenableFuture<?>> pending = new AtomicReference<>();

        /**
         * Returns whether or not this async runner is currently running.
         *
         * @return true if this runner is still in process
         */
        public boolean isRunning() {
            return (pending.get() != null);
        }

        /**
         * Runs the given runnable.  If this runner is already running something, the new runnable will NOT be run.
         *
         * @return true if the runner was started.  False if it was not.
         */
        public boolean runAsync() {
            if (!isRunning()) {
                /* Synchronize on it - and see if we *really* aren't running */
                synchronized (pending) {
                    if (!isRunning()) {
                        /* Still not running - so run */
                        pending.set(Threads.executor().submit(new Runnable() {
                            @Override public void run() {
                                runnable.run();
                                pending.set(null);
                            }
                        }));
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Waits until our function completes.  
         * You can call this function even if it is not running, and it will return immediately.
         */
        public void waitForCompletion() {
            final ListenableFuture<?> future = pending.get();
            if (future != null) {
                Futures.getUnchecked(future);
            }
        }

        /**
         * Runs the given runnable, and if it is started, then it will wait until completion
         */
        public void runAndWait() {
            if (runAsync()) {
                waitForCompletion();
            }
        }
        
        private AsyncRunner(Runnable runnable) {
        	this.runnable = runnable;
        }

    }
    
    private Threads() {}
}
