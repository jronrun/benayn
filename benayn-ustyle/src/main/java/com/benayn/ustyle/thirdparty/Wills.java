package com.benayn.ustyle.thirdparty;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.AbstractListeningExecutorService;
import com.google.common.util.concurrent.ForwardingListenableFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

/**
 * @see https://github.com/avarabyeu/wills
 */
public class Wills {
    
    /**
     * Creates chain from provided Wills
     */
    public static <A> Will<List<A>> when(@SuppressWarnings("unchecked") Will<? extends A>... wills) {
        return when(asList(wills));
    }

    /**
     * Creates chain from provided Wills
     */
    public static <A> Will<List<A>> when(Iterable<? extends Will<? extends A>> wills) {
        return forListenableFuture(Futures.<A>allAsList(wills));
    }

    /**
     * Creates successful {@link Will} from provided object
     */
    public static <A> Will<A> of(A value) {
        return forListenableFuture(Futures.immediateFuture(value));
    }

    /**
     * Creates failed {@link Will} using provided {@link Throwable}
     */
    public static <A> Will<A> failedWill(Throwable throwable) {
        return forListenableFuture(Futures.<A>immediateFailedFuture(throwable));
    }

    /**
     * Creates Will object from Guava's {@link ListenableFuture}
     */
    public static <A> Will<A> forListenableFuture(ListenableFuture<A> future) {
        return new Of<A>(future);
    }

    /**
     * Creates Will object from JKS's {@link java.util.concurrent.Future}
     */
    public static <A> Will<A> forFuture(Future<A> future) {
        return forListenableFuture(JdkFutureAdapters.listenInPoolThread(future));
    }


    /**
     * Creates Guava's {@link FutureCallback} from provided Actions
     */
    public static <A> FutureCallback<A> futureCallback(final Action<A> success, final Action<Throwable> failure) {
        return new FutureCallback<A>() {
            @Override public void onSuccess(A result) {
                checkNotNull(success).apply(result);
            }

            @Override public void onFailure(Throwable t) {
                checkNotNull(failure).apply(t);
            }
        };
    }


    /**
     * Creates Guava's {@link FutureCallback} with success callback defined
     */
    public static <A> FutureCallback<A> onSuccessDo(final Action<A> action) {
        return futureCallback(action, Actions.<Throwable>nothing());
    }


    /**
     * Creates Guava's {@link FutureCallback} with failure callback defined
     */
    public static <A> FutureCallback<A> onFailureDo(final Action<Throwable> action) {
        return futureCallback(Actions.<A>nothing(), action);
    }

    /**
     * Default {@link Will} implementation
     * Based on Guava's {@link ForwardingListenableFuture.SimpleForwardingListenableFuture}
     *
     * @param <A>
     */
    private static final class Of<A> extends ForwardingListenableFuture.SimpleForwardingListenableFuture<A> implements Will<A> {
        
        @Override public A obtain() {
            try {
                return delegate().get();
            } catch (InterruptedException e) {
                throw Throwables.propagate(e);
            } catch (ExecutionException e) {
                throw Throwables.propagate(e.getCause());
            }
        }

        @Override public Will<A> whenSuccessful(Action<A> e) {
            callback(onSuccessDo(e));
            return this;
        }

        @Override public Will<A> whenFailed(Action<Throwable> e) {
            callback(Wills.<A>onFailureDo(e));
            return this;
        }

        @Override public Will<A> whenDone(final Action<Boolean> action) {
            checkNotNull(action, "Action cannot be null");
            
            callback(new FutureCallback<A>() {
                @Override public void onSuccess(A result) {
                    action.apply(true);
                }

                @Override public void onFailure(Throwable t) {
                    action.apply(false);
                }
            });
            
            return this;
        }

        @Override public Will<A> callback(FutureCallback<A> callback) {
            Futures.addCallback(delegate(), callback);
            return this;
        }

        @Override public <R> Will<R> map(Function<? super A, ? extends R> function) {
            return forListenableFuture(Futures.transform(this, function));
        }

        @Override public Will<A> replaceFailed(FutureFallback<? extends A> fallback) {
            return new Of<A>(Futures.withFallback(delegate(), fallback));
        }

        @Override public Will<A> replaceFailed(final ListenableFuture<A> future) {
            return replaceFailed(new FutureFallback<A>() {
                @Override public ListenableFuture<A> create(Throwable t) throws Exception {
                    return future;
                }
            });
        }

        @Override public Will<A> replaceFailed(final Will<A> future) {
            return replaceFailed((ListenableFuture<A>) future);
        }

        @Override public <B> Will<B> flatMap(final Function<? super A, Will<B>> f) {
            final SettableFuture<B> result = SettableFuture.create();
            
            final Action<Throwable> failResult = new Action<Throwable>() {
                @Override public void apply(Throwable t) {
                    result.setException(t);
                }
            };

            whenSuccessful(new Action<A>() {
                
                @Override public void apply(A v) {
                    try {
                        Will<B> next = f.apply(v);
                        checkNotNull(next, "Created Will cannot be null").whenSuccessful(new Action<B>() {
                            @Override public void apply(B t) {
                                result.set(t);
                            }
                        }).whenFailed(failResult);
                    } catch (Throwable t) {
                        result.setException(t);
                    }
                }
            }).whenFailed(failResult);
            
            return new Of<B>(result);
        }

        public Of(ListenableFuture<A> delegate) {
            super(delegate);
        }

    }
    
    /**
     * Decorates JDK's {@link java.util.concurrent.ExecutorService}
     */
    public static WillExecutorService willDecorator(ExecutorService delegate) {
        return willDecorator(MoreExecutors.listeningDecorator(delegate));
    }
    
    /**
     * Decorates Guava's {@link com.google.common.util.concurrent.ListeningExecutorService}
     */
    public static WillExecutorService willDecorator(ListeningExecutorService delegate) {
        return new WillDecorator(delegate);
    }

    /**
     * 
     */
    public interface Will<T> extends ListenableFuture<T> {

        /**
         * Blocks current thread until future object is availible or some exception thrown
         * Signature-free version of {@link java.util.concurrent.Future#get()},
         * propagates checked exceptions as runtume exceptions
         *
         * @return future result
         * @see {@link com.google.common.util.concurrent.ListenableFuture#isDone()}
         */
        T obtain();

        /**
         * Adds callback to future object. Will be executed if future is successful
         *
         * @param action Action to be performed on future result
         * @return This object
         */
        Will<T> whenSuccessful(Action<T> action);

        /**
         * Adds callback to future object. Will be executed if some exception is thrown
         *
         * @param action Action to be performed on future exception
         * @return This object
         */
        Will<T> whenFailed(Action<Throwable> action);

        /**
         * Adds callback to the future object. Will be executed once future is completed
         *
         * @param action Some action with Boolean type. TRUE in case future is successful
         * @return This object
         */
        Will<T> whenDone(Action<Boolean> action);

        /**
         * Adds {@link com.google.common.util.concurrent.FutureCallback} for future object.
         * Will be executed when future result is availible or some exception thrown
         *
         * @param callback {@link com.google.common.util.concurrent.ListenableFuture} callback
         * @return This object
         */
        Will<T> callback(FutureCallback<T> callback);

        /**
         * Replaces future provided by fallback in case if current Will fails
         * <b>PAY ATTENTION - this method creates new Will instance</b>
         *
         * @param fallback New Future Factory
         * @return <b>NEW</b> Will
         */
        Will<T> replaceFailed(FutureFallback<? extends T> fallback);

        /**
         * Replaces current Will with new one based on ListenableFuture in case of fail
         * <b>PAY ATTENTION - this method creates new Will instance</b>
         *
         * @param future New ListenableFuture
         * @return <b>NEW</b> Will
         */
        Will<T> replaceFailed(ListenableFuture<T> future);

        /**
         * Replaces current will with new one in case of fail
         * <b>PAY ATTENTION - this method creates new Will instance</b>
         *
         * @param future <b>NEW</b> Will
         * @return
         */
        Will<T> replaceFailed(Will<T> future);

        /**
         * Creates new {@link Will} containing transformed result of this {@link Will} result using provided function
         *
         * @param function Transformation Function
         * @param <R>      Type of new Will
         * @return New Will
         */
        <R> Will<R> map(Function<? super T, ? extends R> function);

        /**
         * Creates new {@link Will} containing transformed result of this {@link Will} result using provided function
         *
         * @param function Will of transformation function
         * @param <R>      Type of new Will
         * @return New Will
         */
        <R> Will<R> flatMap(Function<? super T, Will<R>> function);

    }
    
    /**
     * Decorates Guava's Executor service. Make all submit method return {@link com.github.avarabyeu.wills.Will} instead of default {@link com.google.common.util.concurrent.ListenableFuture}
     * Delegates all another methods to provided executor service
     */
    private static class WillDecorator extends AbstractListeningExecutorService implements WillExecutorService {

        private ListeningExecutorService delegate;

        private WillDecorator(ListeningExecutorService delegate) {
            this.delegate = checkNotNull(delegate, "ListeningExecutorService cannot be null");
        }

        @Override
        public Will<?> submit(Runnable task) {
            return forListenableFuture(super.submit(task));
        }

        @Override
        public <T> Will<T> submit(Runnable task, T result) {
            return forListenableFuture(super.submit(task, result));
        }

        @Override
        public <T> Will<T> submit(Callable<T> task) {
            return forListenableFuture(super.submit(task));
        }

        @Override public void shutdown() {
            this.delegate.shutdown();
        }

        @Override public List<Runnable> shutdownNow() {
            return this.delegate.shutdownNow();
        }

        @Override public boolean isShutdown() {
            return this.delegate.isShutdown();
        }

        @Override public boolean isTerminated() {
            return this.delegate.isTerminated();
        }

        @Override public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return this.delegate.awaitTermination(timeout, unit);
        }

        @Override public void execute(Runnable command) {
            this.delegate.execute(checkNotNull(command));
        }
    }
    
    /**
     * Representation of some action which may be applied on object
     *
     * @param <T> Type of object action may be applied to
     */
    public interface Action<T> {

        /**
         * Applies some action on provided object
         *
         * @param t Object action will be applied to
         */
        void apply(T t);
        
    }
    
    /**
     * Executor service which uses {@link Will} instead of Guava's {@link ListenableFuture}
     *
     * @author Andrei Varabyeu
     */
    public interface WillExecutorService extends ListeningExecutorService {

        @Override Will<?> submit(Runnable task);

        @Override <T> Will<T> submit(Callable<T> task);

        @Override <T> Will<T> submit(Runnable task, T result);
    }
    
    /**
     * 
     */
    public static final class Actions {

        /**
         * Object-type Action which does nothing. Just a holder when we need to provide NOP action
         */
        private static Action<Object> NOTHING = new Action<Object>() {
            @Override public void apply(Object t) { }
        };

        /**
         * {@link #NOTHING} with type
         */
        @SuppressWarnings("unchecked") public static <E> Action<E> nothing() {
            return (Action<E>) NOTHING;
        }
        
        private Actions() { }
    }
}
