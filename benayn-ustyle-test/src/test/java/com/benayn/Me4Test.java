package com.benayn;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.benayn.ustyle.Dater;
import com.benayn.ustyle.Decision;
import com.benayn.ustyle.JsonR;
import com.benayn.ustyle.JsonW;
import com.benayn.ustyle.Objects2;
import com.benayn.ustyle.Objects2.FacadeObject;
import com.benayn.ustyle.Randoms;
import com.benayn.ustyle.Reflecter;
import com.benayn.ustyle.Resolves;
import com.benayn.ustyle.Retryer;
import com.benayn.ustyle.Retryer.BlockStrategy;
import com.benayn.ustyle.Retryer.RetryException;
import com.benayn.ustyle.Retryer.WaitStrategy;
import com.benayn.ustyle.TypeRefer.TypeDescrib;
import com.benayn.ustyle.string.Betner;
import com.benayn.ustyle.string.Finder;
import com.benayn.ustyle.string.Indexer;
import com.benayn.ustyle.string.Replacer;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.UncheckedTimeoutException;



public class Me4Test extends Me3Test {
    
    @Test public void testUsage() {

        //set random value to user properties for test
        User user = Randoms.get(User.class);

        Map<Byte, List<Float>> testMapValue = user.getTestMap();
        //set testMap property null
        user.setTestMap(null);

        FacadeObject<User> userWrap = FacadeObject.wrap(user);

        //log as formatted JSON string, see below
        /*
         {
            "birth" : "2015-05-18 02:07:07",
            "testMap" : null,
            "address" : {
               "detail" : "fb232c0cca432c4b11c82f4cf4069405c6f4",
               "lonlat" : {
                  "lon" : 0.46031046103583306,
                  "lat" : 0.23163925851477474
               },
               "code" : -886743908
            },
            "age" : -397182609,
            "name" : "7f9c2734c2965c49fac9788c8dda8a2ace31"
         } 
         */
        userWrap.info();
        assertEquals(JsonW.of(user).asJson(), userWrap.getJson());

        //{"birth":1425988977384,"address":{"detail":"moon",
        //"lonlat":{"lon":0.12,"lat":0.10},"code":30},"age":18,"name":"jack"}
        String json = "{\"birth\":1425988977384,\"address\":{\"detail\":\"moon\",\"lonlat\":"
                + "{\"lon\":0.12,\"lat\":0.10},\"code\":30},\"age\":18,\"name\":\"jack\"}";

        Map<String, Object> jsonMap = JsonR.of(json).deepTierMap();
        //same as jsonMap.get("lon")
        assertEquals(0.12, jsonMap.get("address.lonlat.lon"));  

        //same as jsonMap.get("address.lonlat.lat")
        assertEquals(0.10, jsonMap.get("lat"));                 

        //date
        String dateStr = "2015-03-10 20:02:57";
        long dateMs = (Long) jsonMap.get("birth");
        assertEquals(dateStr, Dater.of(dateMs).asText());

        //populate with map
        User user2 = Reflecter.from(User.class).populate(jsonMap).get();
        assertFalse(Objects2.isEqual(user, user2));             //deeply compare

        //populate with JSON
        userWrap.populate(json);
        assertTrue(Objects2.isEqual(user, user2));
        assertTrue(user.getAddress().getLonlat().getLon() 
               == user2.getAddress().getLonlat().getLon());

        //modify user.address.lonlat.lat
        user.getAddress().getLonlat().setLat(0.2);
        assertFalse(Objects2.isEqual(user, user2));

        //type
        TypeDescrib testMapType = userWrap.getType("testMap");
        assertTrue(testMapType.isPair());
        assertEquals(Byte.class, testMapType.next().rawClazz());
        //nextPairType() same as next(1)
        assertEquals(List.class, testMapType.nextPairType().rawClazz());    
        assertEquals(Float.class, testMapType.next(1).next().rawClazz());
        
        assertEquals(double.class, userWrap.getType("address.lonlat.lon").rawClazz());
        assertEquals(Integer.class, userWrap.getType("address.code").rawClazz());
        
        assertEquals(user.getAddress().getLonlat().getLat(), 
                     userWrap.getValue("address.lonlat.lat"));
        assertEquals(user.getAddress().getDetail(), userWrap.getValue("address.detail"));
        assertEquals(user.getBirth().getTime(), userWrap.<Date>getValue("birth").getTime());

        //resolve type
        Object resolveObj = Resolves.get(userWrap.getField("testMap"), testMapValue);
        assertTrue(resolveObj instanceof Map);
        Map<?, ?> resolveMap = (Map<?, ?>) resolveObj;
        assertTrue(resolveMap.size() > 0);

        for (Object key : resolveMap.keySet()) {
            assertTrue(key instanceof Byte);
            assertTrue(resolveMap.get(key) instanceof List);

            List<?> list = (List<?>) resolveMap.get(key);
            for (Object listVal : list) {
                assertTrue(listVal instanceof Float);
            }
        }

        //some string test
        String str = "helloworld@test.com";

        assertEquals("hello*****@test.com", 
                Replacer.ctx(str).afters(5).befores("@").with('*'));
        assertEquals("*****world*********", 
                Replacer.of(str).after(5).negates().before("@").negates().with('*'));

        assertEquals("test", Finder.of(str).afters("@").befores(".").get());        
        assertEquals("world@test", Indexer.of(str).between(5, -4));
        assertEquals("test", Betner.of(str).between("@", ".").first());
    }
    
    //see https://github.com/rholder/guava-retrying
    
    public void testRetryerStrategies() throws RetryException, ExecutionException {
        Retryer<Boolean> retry = Retryer.strategy();
        WaitStrategy noWait = retry.getWaitStrategy();
        assertEquals(0L, noWait.computeSleepTime(18, 9879L));
        
        WaitStrategy fixedWait = retry.reset().fixedWait(1000L).getWaitStrategy();
        assertEquals(1000L, fixedWait.computeSleepTime(12, 6546L));
        
        WaitStrategy incrementingWait = retry.reset()
                .incrementingWait(500L, TimeUnit.MILLISECONDS, 100L, TimeUnit.MILLISECONDS).getWaitStrategy();
        assertEquals(500L, incrementingWait.computeSleepTime(1, 6546L));
        assertEquals(600L, incrementingWait.computeSleepTime(2, 6546L));
        assertEquals(700L, incrementingWait.computeSleepTime(3, 6546L));
        
        WaitStrategy randomWait = retry.reset()
                .randomWait(1000L, TimeUnit.MILLISECONDS, 2000L, TimeUnit.MILLISECONDS).getWaitStrategy();
        Set<Long> times = Sets.newHashSet();
        times.add(randomWait.computeSleepTime(1, 6546L));
        times.add(randomWait.computeSleepTime(1, 6546L));
        times.add(randomWait.computeSleepTime(1, 6546L));
        times.add(randomWait.computeSleepTime(1, 6546L));
        assertTrue(times.size() > 1); // if not, the random is not random
        for (long time : times) {
            assertTrue(time >= 1000L);
            assertTrue(time <= 2000L);
        }
        
        randomWait = retry.reset().randomWait(2000L, TimeUnit.MILLISECONDS).getWaitStrategy();
        times = Sets.newHashSet();
        times.add(randomWait.computeSleepTime(1, 6546L));
        times.add(randomWait.computeSleepTime(1, 6546L));
        times.add(randomWait.computeSleepTime(1, 6546L));
        times.add(randomWait.computeSleepTime(1, 6546L));
        assertTrue(times.size() > 1); // if not, the random is not random
        for (long time : times) {
            assertTrue(time >= 0L);
            assertTrue(time <= 2000L);
        }
        
        WaitStrategy exponentialWait = retry.reset().exponentialWait().getWaitStrategy();
        assertTrue(exponentialWait.computeSleepTime(1, 0) == 2);
        assertTrue(exponentialWait.computeSleepTime(2, 0) == 4);
        assertTrue(exponentialWait.computeSleepTime(3, 0) == 8);
        assertTrue(exponentialWait.computeSleepTime(4, 0) == 16);
        assertTrue(exponentialWait.computeSleepTime(5, 0) == 32);
        assertTrue(exponentialWait.computeSleepTime(6, 0) == 64);
        
        exponentialWait = retry.reset().exponentialWait(40, TimeUnit.MILLISECONDS).getWaitStrategy();
        assertTrue(exponentialWait.computeSleepTime(1, 0) == 2);
        assertTrue(exponentialWait.computeSleepTime(2, 0) == 4);
        assertTrue(exponentialWait.computeSleepTime(3, 0) == 8);
        assertTrue(exponentialWait.computeSleepTime(4, 0) == 16);
        assertTrue(exponentialWait.computeSleepTime(5, 0) == 32);
        assertTrue(exponentialWait.computeSleepTime(6, 0) == 40);
        assertTrue(exponentialWait.computeSleepTime(7, 0) == 40);
        assertTrue(exponentialWait.computeSleepTime(Integer.MAX_VALUE, 0) == 40);
        
        exponentialWait = retry.reset().exponentialWait(1000, 50000, TimeUnit.MILLISECONDS).getWaitStrategy();
        assertTrue(exponentialWait.computeSleepTime(1, 0) == 2000);
        assertTrue(exponentialWait.computeSleepTime(2, 0) == 4000);
        assertTrue(exponentialWait.computeSleepTime(3, 0) == 8000);
        assertTrue(exponentialWait.computeSleepTime(4, 0) == 16000);
        assertTrue(exponentialWait.computeSleepTime(5, 0) == 32000);
        assertTrue(exponentialWait.computeSleepTime(6, 0) == 50000);
        assertTrue(exponentialWait.computeSleepTime(7, 0) == 50000);
        assertTrue(exponentialWait.computeSleepTime(Integer.MAX_VALUE, 0) == 50000);
        
        WaitStrategy fibonacciWait = retry.reset().fibonacciWait().getWaitStrategy();
        assertTrue(fibonacciWait.computeSleepTime(1, 0L) == 1L);
        assertTrue(fibonacciWait.computeSleepTime(2, 0L) == 1L);
        assertTrue(fibonacciWait.computeSleepTime(3, 0L) == 2L);
        assertTrue(fibonacciWait.computeSleepTime(4, 0L) == 3L);
        assertTrue(fibonacciWait.computeSleepTime(5, 0L) == 5L);
        assertTrue(fibonacciWait.computeSleepTime(6, 0L) == 8L);
        
        fibonacciWait = retry.reset().fibonacciWait(10L, TimeUnit.MILLISECONDS).getWaitStrategy();
        assertTrue(fibonacciWait.computeSleepTime(1, 0L) == 1L);
        assertTrue(fibonacciWait.computeSleepTime(2, 0L) == 1L);
        assertTrue(fibonacciWait.computeSleepTime(3, 0L) == 2L);
        assertTrue(fibonacciWait.computeSleepTime(4, 0L) == 3L);
        assertTrue(fibonacciWait.computeSleepTime(5, 0L) == 5L);
        assertTrue(fibonacciWait.computeSleepTime(6, 0L) == 8L);
        assertTrue(fibonacciWait.computeSleepTime(7, 0L) == 10L);
        assertTrue(fibonacciWait.computeSleepTime(Integer.MAX_VALUE, 0L) == 10L);
        
        fibonacciWait = retry.reset().fibonacciWait(1000L, 50000L, TimeUnit.MILLISECONDS).getWaitStrategy();
        assertTrue(fibonacciWait.computeSleepTime(1, 0L) == 1000L);
        assertTrue(fibonacciWait.computeSleepTime(2, 0L) == 1000L);
        assertTrue(fibonacciWait.computeSleepTime(3, 0L) == 2000L);
        assertTrue(fibonacciWait.computeSleepTime(4, 0L) == 3000L);
        assertTrue(fibonacciWait.computeSleepTime(5, 0L) == 5000L);
        assertTrue(fibonacciWait.computeSleepTime(6, 0L) == 8000L);
        assertTrue(fibonacciWait.computeSleepTime(7, 0L) == 13000L);
        assertTrue(fibonacciWait.computeSleepTime(Integer.MAX_VALUE, 0L) == 50000L);
        
        assertFalse(retry.reset().getStopStrategy().shouldStop(3, 6546L));
        
        assertFalse(retry.reset().stopAfterAttempt(3).getStopStrategy().shouldStop(2, 6546L));
        assertTrue(retry.reset().stopAfterAttempt(3).getStopStrategy().shouldStop(3, 6546L));
        assertTrue(retry.reset().stopAfterAttempt(3).getStopStrategy().shouldStop(4, 6546L));
        
        assertFalse(retry.reset().stopAfterDelay(1000L).getStopStrategy().shouldStop(2, 999L));
        assertTrue(retry.reset().stopAfterDelay(3).getStopStrategy().shouldStop(2, 1000L));
        assertTrue(retry.reset().stopAfterDelay(3).getStopStrategy().shouldStop(2, 1001L));
        
        Retryer<Void> r = Retryer.<Void>strategy().timeout(1, TimeUnit.SECONDS);
        try {
            r.call(new SleepyOut(0L));
        } catch (ExecutionException e) {
            Assert.fail("Should not timeout");
        }

        try {
            r.call(new SleepyOut(10 * 1000L));
            Assert.fail("Expected timeout exception");
        } catch (ExecutionException e) {
            // expected
            Assert.assertEquals(UncheckedTimeoutException.class, e.getCause().getClass());
        }
        
        Callable<Boolean> callable = notNullAfter5Attempts();
        Retryer<Boolean> retryer = Retryer.of(callable)
                .fixedWait(50L, TimeUnit.MILLISECONDS).retryIfResult(new Decision<Boolean>() {
                    
                    @Override public boolean apply(Boolean input) {
                        return Predicates.<Boolean>isNull().apply(input);
                    }
                });
        long start = System.currentTimeMillis();
        boolean result = retryer.call();
        assertTrue(System.currentTimeMillis() - start >= 250L);
        assertTrue(result);
        
        testWithMoreThanOneWaitStrategyOneBeingFixed();
    }
    
    private void testWithMoreThanOneWaitStrategyOneBeingFixed() throws ExecutionException, RetryException {
        Retryer<Boolean> retryer = Retryer.<Boolean>strategy().fixedWait(50L)
                .fibonacciWait(10, Long.MAX_VALUE, TimeUnit.MILLISECONDS).retryIfResult(Predicates.<Boolean>isNull());
        long start = System.currentTimeMillis();
        boolean result = retryer.call(notNullAfter5Attempts());
        assertTrue(System.currentTimeMillis() - start >= 370L);
        assertTrue(result);
    }
    
    private void testWithMoreThanOneWaitStrategyOneBeingIncremental() throws ExecutionException, RetryException {
        Callable<Boolean> callable = notNullAfter5Attempts();
        Retryer<Boolean> retryer = Retryer.<Boolean>strategy()
                .incrementingWait(10L, TimeUnit.MILLISECONDS, 10L, TimeUnit.MILLISECONDS)
                .fibonacciWait(10, Long.MAX_VALUE, TimeUnit.MILLISECONDS)
                .retryIfResult(Predicates.<Boolean>isNull());
        
        long start = System.currentTimeMillis();
        boolean result = retryer.call(callable);
        assertTrue(System.currentTimeMillis() - start >= 270L);
        assertTrue(result);
    }
    
    public void testWithStopStrategy() throws ExecutionException {
        Callable<Boolean> callable = notNullAfter5Attempts();
        Retryer<Boolean> retryer = Retryer.<Boolean>strategy()
                .stopAfterAttempt(3)
                .retryIfResult(Predicates.<Boolean>isNull());
        try {
            retryer.call(callable);
            fail("RetryException expected");
        } catch (RetryException e) {
            assertEquals(3, e.getNumberOfFailedAttempts());
        }
    }
    
    public void testWithBlockStrategy() throws ExecutionException, RetryException {
        Callable<Boolean> callable = notNullAfter5Attempts();
        final AtomicInteger counter = new AtomicInteger();
        BlockStrategy blockStrategy = new BlockStrategy() {
            @Override
            public void block(long sleepTime) throws InterruptedException {
                counter.incrementAndGet();
            }
        };

        Retryer<Boolean> retryer = Retryer.<Boolean>strategy()
                .withBlockStrategy(blockStrategy)
                .retryIfResult(Predicates.<Boolean>isNull());
        final int retryCount = 5;
        boolean result = retryer.call(callable);
        assertTrue(result);
        assertEquals(counter.get(), retryCount);
    }
    
    public void testRetryIfException() throws ExecutionException, RetryException {
        Callable<Boolean> callable = noIOExceptionAfter5Attempts();
        Retryer<Boolean> retryer = Retryer.<Boolean>strategy()
                .retryIfException();
        boolean result = retryer.call(callable);
        assertTrue(result);

        callable = noIOExceptionAfter5Attempts();
        retryer = Retryer.<Boolean>strategy()
                .retryIfException().stopAfterAttempt(3);
        try {
            retryer.call(callable);
            fail("RetryException expected");
        } catch (RetryException e) {
            assertEquals(3, e.getNumberOfFailedAttempts());
            assertTrue(e.getLastFailedAttempt().hasCause());
            assertTrue(e.getLastFailedAttempt().getCause() instanceof IOException);
            assertTrue(e.getCause() instanceof IOException);
        }

        callable = noIllegalStateExceptionAfter5Attempts();
        retryer = Retryer.<Boolean>strategy()
                .retryIfException().stopAfterAttempt(3);
        try {
            retryer.call(callable);
            fail("RetryException expected");
        } catch (RetryException e) {
            assertEquals(3, e.getNumberOfFailedAttempts());
            assertTrue(e.getLastFailedAttempt().hasCause());
            assertTrue(e.getLastFailedAttempt().getCause() instanceof IllegalStateException);
            assertTrue(e.getCause() instanceof IllegalStateException);
        }
    }
    
    public void testRetryIfRuntimeException() throws ExecutionException, RetryException {
        Callable<Boolean> callable = noIOExceptionAfter5Attempts();
        Retryer<Boolean> retryer = Retryer.<Boolean>strategy()
                .retryIfRuntimeException();
        try {
            retryer.call(callable);
            fail("ExecutionException expected");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof IOException);
        }

        callable = noIllegalStateExceptionAfter5Attempts();
        assertTrue(retryer.call(callable));

        callable = noIllegalStateExceptionAfter5Attempts();
        retryer = Retryer.<Boolean>strategy()
                .retryIfRuntimeException().stopAfterAttempt(3);
        try {
            retryer.call(callable);
            fail("RetryException expected");
        } catch (RetryException e) {
            assertEquals(3, e.getNumberOfFailedAttempts());
            assertTrue(e.getLastFailedAttempt().hasCause());
            assertTrue(e.getLastFailedAttempt().getCause() instanceof IllegalStateException);
            assertTrue(e.getCause() instanceof IllegalStateException);
        }
    }
    
    public void testRetryIfExceptionOfType() throws RetryException, ExecutionException {
        Callable<Boolean> callable = noIOExceptionAfter5Attempts();
        Retryer<Boolean> retryer = Retryer.<Boolean>strategy()
                .retryIfException(IOException.class);
        assertTrue(retryer.call(callable));

        callable = noIllegalStateExceptionAfter5Attempts();
        try {
            retryer.call(callable);
            fail("ExecutionException expected");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
        }

        callable = noIOExceptionAfter5Attempts();
        retryer = Retryer.<Boolean>strategy()
                .retryIfException(IOException.class).stopAfterAttempt(3);
        try {
            retryer.call(callable);
            fail("RetryException expected");
        } catch (RetryException e) {
            assertEquals(3, e.getNumberOfFailedAttempts());
            assertTrue(e.getLastFailedAttempt().hasCause());
            assertTrue(e.getLastFailedAttempt().getCause() instanceof IOException);
            assertTrue(e.getCause() instanceof IOException);
        }
    }
    
    public void testRetryIfExceptionWithPredicate() throws RetryException, ExecutionException {
        Callable<Boolean> callable = noIOExceptionAfter5Attempts();
        Retryer<Boolean> retryer = Retryer.<Boolean>strategy()
                .retryIfException(new Predicate<Throwable>() {
                    @Override
                    public boolean apply(Throwable t) {
                        return t instanceof IOException;
                    }
                });
        assertTrue(retryer.call(callable));

        callable = noIllegalStateExceptionAfter5Attempts();
        try {
            retryer.call(callable);
            fail("ExecutionException expected");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
        }

        callable = noIOExceptionAfter5Attempts();
        retryer = Retryer.<Boolean>strategy()
                .retryIfException(new Predicate<Throwable>() {
                    @Override
                    public boolean apply(Throwable t) {
                        return t instanceof IOException;
                    }
                }).stopAfterAttempt(3);
        try {
            retryer.call(callable);
            fail("RetryException expected");
        } catch (RetryException e) {
            assertEquals(3, e.getNumberOfFailedAttempts());
            assertTrue(e.getLastFailedAttempt().hasCause());
            assertTrue(e.getLastFailedAttempt().getCause() instanceof IOException);
            assertTrue(e.getCause() instanceof IOException);
        }
    }
    
    public void testRetryIfResult() throws ExecutionException, RetryException {
        Callable<Boolean> callable = notNullAfter5Attempts();
        Retryer<Boolean> retryer = Retryer.<Boolean>strategy()
                .retryIfResult(Predicates.<Boolean>isNull());
        assertTrue(retryer.call(callable));

        callable = notNullAfter5Attempts();
        retryer = Retryer.<Boolean>strategy()
                .retryIfResult(Predicates.<Boolean>isNull()).stopAfterAttempt(3);
        try {
            retryer.call(callable);
            fail("RetryException expected");
        } catch (RetryException e) {
            assertEquals(3, e.getNumberOfFailedAttempts());
            assertTrue(e.getLastFailedAttempt().hasResult());
            assertNull(e.getLastFailedAttempt().getResult());
            assertNull(e.getCause());
        }
    }
    
    public void testMultipleRetryConditions() throws ExecutionException, RetryException {
        Callable<Boolean> callable = notNullResultOrIOExceptionOrRuntimeExceptionAfter5Attempts();
        Retryer<Boolean> retryer = Retryer.<Boolean>strategy()
                .retryIfResult(Predicates.<Boolean>isNull())
                .retryIfException(IOException.class)
                .retryIfRuntimeException().stopAfterAttempt(3);
        try {
            retryer.call(callable);
            fail("RetryException expected");
        } catch (RetryException e) {
            assertTrue(e.getLastFailedAttempt().hasCause());
            assertTrue(e.getLastFailedAttempt().getCause() instanceof IllegalStateException);
            assertTrue(e.getCause() instanceof IllegalStateException);
        }

        callable = notNullResultOrIOExceptionOrRuntimeExceptionAfter5Attempts();
        retryer = Retryer.<Boolean>strategy()
                .retryIfResult(Predicates.<Boolean>isNull())
                .retryIfException(IOException.class)
                .retryIfRuntimeException();
        assertTrue(retryer.call(callable));
    }
    
    public void testInterruption() throws InterruptedException, ExecutionException {
        final AtomicBoolean result = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Retryer<Boolean> retryer = Retryer.<Boolean>strategy()
                        .fixedWait(1000L, TimeUnit.MILLISECONDS)
                        .retryIfResult(Predicates.<Boolean>isNull());
                try {
                    retryer.call(alwaysNull(latch));
                    fail("RetryException expected");
                } catch (RetryException e) {
                    assertTrue(!e.getLastFailedAttempt().hasCause());
                    assertNull(e.getCause());
                    assertTrue(Thread.currentThread().isInterrupted());
                    result.set(true);
                } catch (ExecutionException e) {
                    fail("RetryException expected");
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
        latch.countDown();
        t.interrupt();
        t.join();
        assertTrue(result.get());
    }

    public void testWhetherBuilderFailsForNullStopStrategy() {
        try {
            Retryer.<Void>strategy()
                    .withStopStrategy(null);
            fail("Exepcted to fail for null stop strategy");
        } catch (NullPointerException exception) {
            assertTrue(exception.getMessage().contains("StopStrategy cannot be null"));
        }
    }

    public void testWhetherBuilderFailsForNullWaitStrategy() {
        try {
            Retryer.<Void>strategy()
                    .withWaitStrategy(null);
            fail("Exepcted to fail for null wait strategy");
        } catch (NullPointerException exception) {
            assertTrue(exception.getMessage().contains("WaitStrategy cannot be null"));
        }
    }

    public void testWhetherBuilderFailsForNullWaitStrategyWithCompositeStrategies() {
        try {
            Retryer.<Void>strategy()
                    .withWaitStrategy(null);
            fail("Exepcted to fail for null wait strategy");
        } catch (NullPointerException exception) {
            assertTrue(exception.getMessage().contains("WaitStrategy cannot be null"));
        }
    }
    
    @Test
    public void testRetryerStrategies2() throws RetryException, ExecutionException, InterruptedException {
        testRetryerStrategies();
        testWithMoreThanOneWaitStrategyOneBeingFixed();
        testWithMoreThanOneWaitStrategyOneBeingIncremental();
        testWithStopStrategy();
        testWithBlockStrategy();
        testRetryIfException();
        testRetryIfRuntimeException();
        testRetryIfExceptionOfType();
        testRetryIfExceptionWithPredicate();
        testRetryIfResult();
        testMultipleRetryConditions();
        testInterruption();
        testWhetherBuilderFailsForNullStopStrategy();
        testWhetherBuilderFailsForNullWaitStrategy();
        testWhetherBuilderFailsForNullWaitStrategyWithCompositeStrategies();
    }
    
    private Callable<Boolean> alwaysNull(final CountDownLatch latch) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                latch.countDown();
                return null;
            }
        };
    }
    
    private Callable<Boolean> notNullResultOrIOExceptionOrRuntimeExceptionAfter5Attempts() {
        return new Callable<Boolean>() {
            int counter = 0;

            @Override
            public Boolean call() throws IOException {
                if (counter < 1) {
                    counter++;
                    return null;
                } else if (counter < 2) {
                    counter++;
                    throw new IOException();
                } else if (counter < 5) {
                    counter++;
                    throw new IllegalStateException();
                }
                return true;
            }
        };
    }
    
    private Callable<Boolean> noIOExceptionAfter5Attempts() {
        return new Callable<Boolean>() {
            int counter = 0;

            @Override
            public Boolean call() throws IOException {
                if (counter < 5) {
                    counter++;
                    throw new IOException();
                }
                return true;
            }
        };
    }
    
    private Callable<Boolean> noIllegalStateExceptionAfter5Attempts() {
        return new Callable<Boolean>() {
            int counter = 0;

            @Override
            public Boolean call() throws Exception {
                if (counter < 5) {
                    counter++;
                    throw new IllegalStateException();
                }
                return true;
            }
        };
    }
    
    static class SleepyOut implements Callable<Void> {

        final long sleepMs;

        SleepyOut(long sleepMs) {
            this.sleepMs = sleepMs;
        }

        @Override
        public Void call() throws Exception {
            Thread.sleep(sleepMs);
            System.out.println("I'm awake now");
            return null;
        }
    }
    
    private Callable<Boolean> notNullAfter5Attempts() {
        return new Callable<Boolean>() {
            int counter = 0;

            @Override
            public Boolean call() throws Exception {
                if (counter < 5) {
                    counter++;
                    return null;
                }
                return true;
            }
        };
    }
    
}
