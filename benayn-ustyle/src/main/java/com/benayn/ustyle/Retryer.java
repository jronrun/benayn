/**
 *
 */
package com.benayn.ustyle;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.alwaysFalse;
import static com.google.common.base.Predicates.or;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;


/**
 * @see https://github.com/rholder/guava-retrying
 */
public final class Retryer<R> {
    
    /**
     *
     */
    protected static final Log log = Loggers.from(Retryer.class);
    
    /**
     * Returns a new {@link Retryer} instance with given {@link Callable}
     * 
     * @param target
     * @return
     */
    public static <R> Retryer<R> of(Callable<R> target) {
        return new Retryer<R>(target);
    }
    
    /**
     * Returns a new {@link Retryer} instance to configuration retry strategy
     * 
     * @return
     */
    public static <R> Retryer<R> strategy() {
        return new Retryer<R>(null);
    }
    
    /**
     * Timing out after the given duration {@link TimeUnit#SECONDS} time limit
     *
     * @param duration
     * @return
     */
    public Retryer<R> timeout(long duration) {
        return timeout(duration, TimeUnit.SECONDS);
    }
    
    /**
     * Timing out after the specified time limit
     *
     * @param duration
     * @param timeUnit
     * @return
     */
    public Retryer<R> timeout(long duration, TimeUnit timeUnit) {
        return timeout(duration, timeUnit, null);
    }
    
    /**
     * Timing out after the specified time limit with {@link ExecutorService}
     *
     * @param duration
     * @param timeUnit
     * @param executor
     * @return
     */
    public Retryer<R> timeout(long duration, TimeUnit timeUnit, ExecutorService executor) {
        return timeout(null == executor
                ? new SimpleTimeLimiter() : new SimpleTimeLimiter(executor), duration, timeUnit);
    }
    
    /**
     * Timing out after the specified time limit with {@link TimeLimiter}
     *
     * @param timeLimiter
     * @param duration
     * @param timeUnit
     * @return
     */
    public Retryer<R> timeout(final TimeLimiter timeLimiter, final long duration, final TimeUnit timeUnit) {
        return withTryTimeout(new TryTimeout<R>() {

            @Override public R call(Callable<R> callable) throws Exception {
                return checkNotNull(timeLimiter, "TimeLimiter cannot be null")
                        .callWithTimeout(callable, duration, checkNotNull(timeUnit), true);
            }
        });
    }
    
    /**
     * Retry if an exception (any <code>Exception</code> or subclass of <code>Exception</code>) is thrown
     * 
     * @return
     */
    public Retryer<R> retryIfException() {
        return retryIfException(Exception.class);
    }
    
    /**
     * Retry if a runtime exception (any <code>RuntimeException</code> or subclass of <code>RuntimeException</code>) is thrown
     * 
     * @return
     */
    public Retryer<R> retryIfRuntimeException() {
        return retryIfException(RuntimeException.class);
    }
    
    /**
     * Retry if an given exception type is thrown
     * 
     * @return
     */
    public Retryer<R> retryIfException(Class<? extends Throwable> target) {
        return retryIfExPredicate(checkNotNull(target, "Exception class cannot be null"));
    }
    
    /**
     * Retry if an exception satisfying the given {@link Predicate} is thrown
     * 
     * @param decision
     * @return
     */
    public Retryer<R> retryIfException(final Predicate<Throwable> decision) {
        rejection = or(rejection, new Predicate<Tried<R>>() {

            @Override public boolean apply(Tried<R> input) {
                return input.hasCause() ? decision.apply(input.getCause()) : false;
            }
        });
        
        return this;
    }
    
    /**
     * Retry if the result satisfies the given {@link Predicate}
     * 
     * @param decision
     * @return
     */
    public Retryer<R> retryIfResult(final Predicate<R> decision) {
        rejection = or(rejection, new Predicate<Tried<R>>() {

            @Override public boolean apply(Tried<R> input) {
                return input.hasResult() ? decision.apply(input.getResult()) : false;
            }
        });
        
        return this;
    }
    
    /**
     * Executes the given callable quietly
     * 
     * @param callable
     * @return
     */
    public R quietCall(Callable<R> callable) {
        this.delegate(callable);
        return quietCall();
    }
    
    /**
     * Executes the given callable
     * 
     * @param callable
     * @return
     * @throws ExecutionException
     * @throws RetryException
     */
    public R call(Callable<R> callable) throws ExecutionException, RetryException {
        this.delegate(callable);
        return call();
    }
    
    /**
     * Executes the delegate callable quietly
     * 
     * @return
     */
    public R quietCall() {
        try {
            return call();
        } catch (ExecutionException e) {
            log.error(e);
        } catch (RetryException e) {
            log.error(e);
        }
        
        return null;
    }
    
    /**
     * Executes the delegate callable. If the rejection predicate accepts the attempt, 
     * the stop strategy is used to decide if a new attempt must be made. 
     * Then the wait strategy is used to decide how much time to sleep and a new attempt is made.
     * 
     * @return
     * @throws ExecutionException
     * @throws RetryException
     */
    public R call() throws ExecutionException, RetryException {
        Stopwatch watch = Stopwatch.createStarted();
        try {
            for (int attempt = 1; ; attempt++) {
                Tried<R> target = null;
                
                try {
                    target = Tried.of(this.getTryTimeout().call(this.delegate.get()));
                } catch (Throwable t) {
                    target = Tried.of(t);
                }
                
                if (!this.rejection.apply(target)) {
                    return target.get();
                }
                
                if (this.getStopStrategy().shouldStop(attempt, watch.elapsed(TimeUnit.MILLISECONDS))) {
                    throw new RetryException(attempt, target);
                } else {
                    long sleepTime = this.getWaitStrategy()
                            .computeSleepTime(attempt, watch.elapsed(TimeUnit.MILLISECONDS));
                    try {
                        this.getBlockStrategy().block(sleepTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RetryException(attempt, target);
                    }
                }
            }
        } finally {
            watch.stop();
        }
    }
    
    /**
     * Configures the {@link Retryer} to limit the duration of any particular attempt by the given duration
     * 
     * @param tryTimeout
     * @return
     */
    public Retryer<R> withTryTimeout(TryTimeout<R> tryTimeout) {
        this.tryTimeout = checkNotNull(tryTimeout, "TryTimeout cannot be null");
        return this;
    }
    
    /**
     * Sets the stop strategy used to decide when to stop retrying. The default strategy is never stop
     * 
     * @param stopStrategy
     * @return
     */
    public Retryer<R> withStopStrategy(StopStrategy stopStrategy) {
        checkState(this.stopStrategy == null, "A stop strategy has already been set %s", this.stopStrategy);
        this.stopStrategy = checkNotNull(stopStrategy, "StopStrategy cannot be null");
        return this;
    }
    
    /**
     * Sets the stop strategy which stops after N failed attempts
     * 
     * @param maxAttemptNumber
     * @return
     */
    public Retryer<R> stopAfterAttempt(final int maxAttemptNumber) {
        checkArgument(maxAttemptNumber >= 1, "maxAttemptNumber must be >= 1 but is %d", maxAttemptNumber);
        return withStopStrategy(new StopStrategy() {
            
            @Override public boolean shouldStop(int previousAttemptNumber, long delaySinceFirstAttemptInMillis) {
                return previousAttemptNumber >= maxAttemptNumber;
            }
        });
    }
    
    /**
     * Sets the stop strategy which stops after a given delay milliseconds
     * 
     * @param delayInMillis
     * @return
     */
    public Retryer<R> stopAfterDelay(final long delayInMillis) {
        checkArgument(delayInMillis >= 0L, "delayInMillis must be >= 0 but is %d", delayInMillis);
        return withStopStrategy(new StopStrategy() {
            
            @Override public boolean shouldStop(int previousAttemptNumber, long delaySinceFirstAttemptInMillis) {
                return delaySinceFirstAttemptInMillis >= delayInMillis;
            }
        });
    }
    
    /**
     * Sets the wait strategy used to decide how long to sleep between failed attempts.
     * The default strategy is to retry immediately after a failed attempt
     * 
     * @param waitStrategy
     * @return
     */
    public Retryer<R> withWaitStrategy(WaitStrategy waitStrategy) {
        (this.waitStrategy = (this.waitStrategy.isPresent() 
                ? this.waitStrategy : Optional.of(new CompositeWaitStrategy())))
                .get().put(checkNotNull(waitStrategy, "WaitStrategy cannot be null"));
        return this;
    }
    
    /**
     * Sets the wait strategy that sleeps a fixed amount milliseconds before retrying
     * 
     * @param sleepTime
     * @param timeUnit
     * @return
     */
    public Retryer<R> fixedWait(long sleepTime) {
        return fixedWait(sleepTime, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Sets the wait strategy that sleeps a fixed amount of time before retrying
     * 
     * @param sleepTime
     * @param timeUnit
     * @return
     */
    public Retryer<R> fixedWait(long sleepTime, TimeUnit timeUnit) {
        withWaitStrategy(fixedWaitStrategy(checkNotNull(timeUnit, "TimeUnit cannot be null").toMillis(sleepTime)));
        return this;
    }
    
    /**
     * Sets the strategy that sleeps a random amount milliseconds before retrying
     * 
     * @param maximum
     * @param timeUnit
     * @return
     */
    public Retryer<R> randomWait(long maximum) {
        return randomWait(maximum, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Sets the strategy that sleeps a random amount of time before retrying
     * 
     * @param maximum
     * @param timeUnit
     * @return
     */
    public Retryer<R> randomWait(long maximum, TimeUnit timeUnit) {
        return randomWait(0L, checkNotNull(timeUnit).toMillis(maximum));
    }
    
    /**
     * Sets the strategy that sleeps a random amount of time before retrying
     * 
     * @param minimum
     * @param minUnit
     * @param maximum
     * @param maxUnit
     * @return
     */
    public Retryer<R> randomWait(long minimum, TimeUnit minUnit, long maximum, TimeUnit maxUnit) {
        return randomWait(checkNotNull(minUnit).toMillis(minimum), checkNotNull(maxUnit).toMillis(maximum));
    }
    
    /**
     * Sets the wait strategy that sleeps a random amount milliseconds before retrying
     * 
     * @param minimum
     * @param maximum
     * @return
     */
    public Retryer<R> randomWait(final long minimum, final long maximum) {
        checkArgument(minimum >= 0, "minimum must be >= 0 but is %d", minimum);
        checkArgument(maximum > minimum, "maximum must be > minimum but maximum is %d and minimum is", maximum, minimum);
        
        final Random random = new Random();
        return withWaitStrategy(new WaitStrategy() {
            @Override public long computeSleepTime(int previousAttemptNumber, long delaySinceFirstAttemptInMillis) {
                long t = Math.abs(random.nextLong()) % (maximum - minimum);
                return t + minimum;
            }
        });
    }
    
    /**
     * Sets the strategy that sleeps a fixed amount of time after the first
     * failed attempt and in incrementing amounts of time after each additional failed attempt.
     * 
     * @param initialSleepTime
     * @param initialSleepUnit
     * @param increment
     * @param incrementUnit
     * @return
     */
    public Retryer<R> incrementingWait(long initialSleepTime, 
            TimeUnit initialSleepUnit, long increment, TimeUnit incrementUnit) {
        return incrementingWait(checkNotNull(initialSleepUnit).toMillis(initialSleepTime), 
                       checkNotNull(incrementUnit).toMillis(increment));
    }
    
    /**
     * Sets the strategy that sleeps a fixed amount milliseconds after the first
     * failed attempt and in incrementing amounts milliseconds after each additional failed attempt.
     * 
     * @param initialSleepTime
     * @param increment
     * @return
     */
    public Retryer<R> incrementingWait(final long initialSleepTime, final long increment) {
        checkArgument(initialSleepTime >= 0L, "initialSleepTime must be >= 0 but is %d", initialSleepTime);
        return withWaitStrategy(new WaitStrategy() {

            @Override public long computeSleepTime(int previousAttemptNumber, long delaySinceFirstAttemptInMillis) {
                long result = initialSleepTime + (increment * (previousAttemptNumber - 1));
                return result >= 0L ? result : 0L;
            }
        });
    }
    
    /**
     * Sets the wait strategy which sleeps for an exponential amount of time after the first failed attempt,
     * and in exponentially incrementing amounts after each failed attempt up to Long.MAX_VALUE.
     *
     * @return
     */
    public Retryer<R> exponentialWait() {
        return exponentialWait(1, Long.MAX_VALUE);
    }
    
    /**
     * Sets the wait strategy which sleeps for an exponential amount of time after the first failed attempt
     * 
     * @param maximumTime
     * @param maximumUnit
     * @return
     */
    public Retryer<R> exponentialWait(long maximumTime, TimeUnit maximumUnit) {
        return exponentialWait(1L, checkNotNull(maximumUnit).toMillis(maximumTime));
    }
    
    /**
     * Sets the wait strategy which sleeps for an exponential amount of time after the first failed attempt
     * 
     * @see Retryer#exponentialWait(long, long)
     * @param multiplier
     * @param maximumTime
     * @param maximumUnit
     * @return
     */
    public Retryer<R> exponentialWait(long multiplier, long maximumTime, TimeUnit maximumUnit) {
        return exponentialWait(multiplier, checkNotNull(maximumUnit).toMillis(maximumTime));
    }
    
    /**
     * Sets the wait strategy which sleeps for an exponential amount of time after the first failed attempt,
     * and in exponentially incrementing amounts after each failed attempt up to the maximumTime.
     * The wait time between the retries can be controlled by the multiplier.
     * nextWaitTime = exponentialIncrement * {@code multiplier}.
     * 
     * @param multiplier
     * @param maximumTime the unit of the maximumTime is {@link TimeUnit#MILLISECONDS}
     * @return
     */
    public Retryer<R> exponentialWait(final long multiplier, final long maximumWait) {
        checkArgument(multiplier > 0L, "multiplier must be > 0 but is %d", multiplier);
        checkArgument(maximumWait >= 0L, "maximumWait must be >= 0 but is %d", maximumWait);
        checkArgument(multiplier < maximumWait, "multiplier must be < maximumWait but is %d", multiplier);
        
        return withWaitStrategy(new WaitStrategy() {
            
            @Override public long computeSleepTime(int previousAttemptNumber, long delaySinceFirstAttemptInMillis) {
                double exp = Math.pow(2, previousAttemptNumber);
                long result = Math.round(multiplier * exp);
                if (result > maximumWait) {
                    result = maximumWait;
                }
                return result >= 0L ? result : 0L;
            }
        });
    }
    
    /**
     * Returns a strategy which sleeps for an increasing amount of time after the first failed attempt,
     * and in Fibonacci increments after each failed attempt up to {@link Long#MAX_VALUE}.
     *
     * @return
     */
    public Retryer<R> fibonacciWait() {
        return fibonacciWait(1, Long.MAX_VALUE);
    }
    
    /**
     * Sets the wait strategy which sleeps for an increasing amount of time after the first failed attempt
     * 
     * @param maximumTime
     * @param maximumUnit
     * @return
     */
    public Retryer<R> fibonacciWait(long maximumTime, TimeUnit maximumUnit) {
        return fibonacciWait(1L, checkNotNull(maximumUnit).toMillis(maximumTime));
    }
    
    /**
     * Sets the wait strategy which sleeps for an increasing amount of time after the first failed attempt,
     * and in Fibonacci increments after each failed attempt up to the {@code maximumTime}.
     * The wait time between the retries can be controlled by the multiplier.
     * nextWaitTime = fibonacciIncrement * {@code multiplier}.
     * 
     * @param multiplier
     * @param maximumTime
     * @param maximumUnit
     * @return
     */
    public Retryer<R> fibonacciWait(long multiplier, long maximumTime, TimeUnit maximumUnit) {
        return fibonacciWait(multiplier, checkNotNull(maximumUnit).toMillis(maximumTime));
    }
    
    /**
     * Sets the wait strategy which sleeps for an increasing amount of time after the first failed attempt,
     * and in Fibonacci increments after each failed attempt up to the {@code maximumTime}.
     * The wait time between the retries can be controlled by the multiplier.
     * nextWaitTime = fibonacciIncrement * {@code multiplier}.
     * 
     * @param multiplier
     * @param maximumWait the unit of the maximumWait is {@link TimeUnit#MILLISECONDS}
     * @return
     */
    public Retryer<R> fibonacciWait(final long multiplier, final long maximumWait) {
        return withWaitStrategy(new WaitStrategy() {
            
            @Override
            public long computeSleepTime(int previousAttemptNumber, long delaySinceFirstAttemptInMillis) {
                long fib = fib(previousAttemptNumber);
                long result = multiplier * fib;

                if (result > maximumWait || result < 0L) {
                    result = maximumWait;
                }

                return result >= 0L ? result : 0L;
            }

            private long fib(long n) {
                if (n == 0L) return 0L;
                if (n == 1L) return 1L;

                long prevPrev = 0L;
                long prev = 1L;
                long result = 0L;

                for (long i = 2L; i <= n; i++) {
                    result = prev + prevPrev;
                    prevPrev = prev;
                    prev = result;
                }

                return result;
            }
        });
    }
    
    /**
     * Sets the block strategy used to decide how to block between retry attempts. 
     * The default strategy is to use {@link Thread#sleep(long)}
     * 
     * @param blockStrategy
     * @return
     */
    public Retryer<R> withBlockStrategy(BlockStrategy blockStrategy) {
        checkState(this.blockStrategy == null, "a block strategy has already been set %s", this.blockStrategy);
        this.blockStrategy = checkNotNull(blockStrategy, "BlockStrategy cannot be null");
        return this;
    }
    
    /**
     *
     */
    public interface TryTimeout<V> {

        /**
         * Invokes a specified {@link Callable}, timing out after the specified time limit
         *
         * @see TimeLimiter#callWithTimeout(Callable, long, java.util.concurrent.TimeUnit, boolean)
         */
        public V call(Callable<V> callable) throws Exception;
        
    }
    
    /**
     *
     */
    public interface StopStrategy {

        /**
         * Returns <code>true</code> if the {@link Retryer} should stop retrying.
         *
         * @param previousAttemptNumber          the number of the previous attempt (starting from 1)
         * @param delaySinceFirstAttemptInMillis the delay since the start of the first attempt, in milliseconds
         * @return <code>true</code> if the retryer must stop
         */
        boolean shouldStop(int previousAttemptNumber, long delaySinceFirstAttemptInMillis);
    }
    
    /**
     *
     */
    public interface WaitStrategy {

        /**
         * Returns the time, in milliseconds, to sleep before retrying.
         *
         * @param previousAttemptNumber          the number, starting from 1, of the previous (failed) attempt
         * @param delaySinceFirstAttemptInMillis the delay since the start of the first attempt, in milliseconds
         * @return the sleep time before next attempt
         */
        long computeSleepTime(int previousAttemptNumber, long delaySinceFirstAttemptInMillis);
    }
    
    public interface BlockStrategy {

        /**
         * Attempt to block for the designated amount of time. Implementations
         * that don't block or otherwise delay the processing from within this
         * method for the given sleep duration can significantly modify the behavior
         * of any configured {@link com.github.rholder.retry.WaitStrategy}. Caution
         * is advised when generating your own implementations.
         *
         * @param sleepTime the computed sleep duration in milliseconds
         * @throws InterruptedException
         */
        void block(long sleepTime) throws InterruptedException;
    }
    
    private Retryer(Callable<R> callable) {
        if (null != callable) {
            this.delegate(callable);
        }
    }
    
    /**
     * 
     */
    private void delegate(Callable<R> callable) {
        this.delegate = Optional.of(callable);
    }
    
    /**
     * Returns current {@link TryTimeout} instance
     * 
     * @return
     */
    public TryTimeout<R> getTryTimeout() {
        return this.tryTimeout = (null == this.tryTimeout ? new TryTimeout<R>() {
            //An implementation which actually does not attempt to limit time at all
            @Override public R call(Callable<R> callable) throws Exception {
                return checkNotNull(callable).call();
            }
        } : this.tryTimeout);
    }
    
    /**
     * Returns current {@link StopStrategy} instance
     * 
     * @return
     */
    public StopStrategy getStopStrategy() {
        return this.stopStrategy = (null == this.stopStrategy ? new StopStrategy() {
            //A stop strategy which never stops retrying
            @Override public boolean shouldStop(int previousAttemptNumber, long delaySinceFirstAttemptInMillis) {
                return false;
            }
        } : this.stopStrategy);
    }
    
    /**
     * Returns current {@link WaitStrategy} instance
     * 
     * @return
     */
    public WaitStrategy getWaitStrategy() {
        return this.waitStrategy.isPresent() ? this.waitStrategy.get() 
                //A wait strategy that doesn't sleep at all before retrying
                : fixedWaitStrategy(0L);
    }
    
    /**
     * Returns current {@link BlockStrategy} instance
     * 
     * @return
     */
    public BlockStrategy getBlockStrategy() {
        return this.blockStrategy = (null == this.blockStrategy ? new BlockStrategy() {
            
            @Override public void block(long sleepTime) throws InterruptedException {
                Thread.sleep(sleepTime);
            }
        } : this.blockStrategy);
    }
    
    protected WaitStrategy fixedWaitStrategy(final long sleepTime) {
        checkArgument(sleepTime >= 0L, "sleepTime must be >= 0 but is %d", sleepTime);
        return new WaitStrategy() {
            
            @Override public long computeSleepTime(int previousAttemptNumber, long delaySinceFirstAttemptInMillis) {
                return sleepTime;
            }
        };
    }
    
    /**
     * 
     */
    public static class Tried<R> {
        
        private Object delegate = null;
        private Boolean isThrowable = null;
        private ExecutionException ex = null;
        
        public static <R> Tried<R> of(Throwable throwable) {
            return new Tried<R>(throwable, true);
        }
        
        public static <R> Tried<R> of(R result) {
            return new Tried<R>(result, false);
        }
        
        private Tried(Object target, boolean isThrowable) {
            this.delegate = target;
            if (this.isThrowable = isThrowable) {
                this.ex = new ExecutionException((Throwable) this.delegate);
            }
        }
        
        @SuppressWarnings("unchecked") public R get() throws ExecutionException {
            if (this.isThrowable) {
                throw ex;
            }
            
            return (R) this.delegate;
        }
        
        public boolean hasResult() {
            return !this.hasCause();
        }
        
        @SuppressWarnings("unchecked") public R getResult() {
            if (!this.hasResult()) {
                throw new IllegalStateException("The Tried instance is not a result");
            }
            
            return (R) this.delegate;
        }
        
        public boolean hasCause() {
            return this.isThrowable;
        }
        
        public Throwable getCause() {
            if (!this.hasCause()) {
                throw new IllegalStateException("The Tried instance is not an exception");
            }
            
            return ex.getCause();
        }
        
    }
    
    private Retryer<R> retryIfExPredicate(final Class<? extends Throwable> target) {
        rejection = or(rejection, new Predicate<Tried<R>>() {

            @Override public boolean apply(Tried<R> input) {
                return input.hasCause() ? target.isAssignableFrom(input.getCause().getClass()) : false;
            }
        });
        
        return this;
    }
    
    /**
     * 
     */
    public static final class RetryException extends Exception {

        /**
         * 
         */
        private static final long serialVersionUID = -8517706111208284662L;
        
        private final int numberOfFailedAttempts;
        private final Tried<?> lastFailedAttempt;

        public RetryException(int numberOfFailedAttempts, Tried<?> lastFailedAttempt) {
            this("Retrying failed to complete successfully after " + 
                    numberOfFailedAttempts + " attempts.", numberOfFailedAttempts, lastFailedAttempt);
        }

        public RetryException(String message, int numberOfFailedAttempts, Tried<?> lastFailedAttempt) {
            super(message, checkNotNull(lastFailedAttempt, 
                    "Last attempt was null").hasCause() ? lastFailedAttempt.getCause() : null);
            this.numberOfFailedAttempts = numberOfFailedAttempts;
            this.lastFailedAttempt = lastFailedAttempt;
        }

        public int getNumberOfFailedAttempts() {
            return numberOfFailedAttempts;
        }

        public Tried<?> getLastFailedAttempt() {
            return lastFailedAttempt;
        }
    }
    
    /**
     * Resets the current {@link Retryer} instance configurations
     */
    public Retryer<R> reset() {
        this.tryTimeout = null;
        this.stopStrategy = null;
        this.blockStrategy = null;
        this.rejection = alwaysFalse();
        this.waitStrategy = Optional.absent();
        return this;
    }
    
    /**
     *
     */
    private static final class CompositeWaitStrategy implements WaitStrategy {
        List<WaitStrategy> waitStrategies = null;

        void put(WaitStrategy waitStrategy) {
            (waitStrategies = (null == waitStrategies 
                    ? Lists.<WaitStrategy>newArrayList() : waitStrategies)).add(waitStrategy);
        }

        @Override public long computeSleepTime(int previousAttemptNumber, long delaySinceFirstAttemptInMillis) {
            long waitTime = 0l;
            for (WaitStrategy waitStrategy : waitStrategies) {
                waitTime += waitStrategy.computeSleepTime(previousAttemptNumber, delaySinceFirstAttemptInMillis);
            }
            return waitTime;
        }
    }
    
    private TryTimeout<R> tryTimeout = null;
    private StopStrategy stopStrategy = null;
    private BlockStrategy blockStrategy = null;
    private Predicate<Tried<R>> rejection = alwaysFalse();
    private Optional<CompositeWaitStrategy> waitStrategy = Optional.absent();
    
    //the callable task to be executed
    private Optional<Callable<R>> delegate = Optional.absent();

}
