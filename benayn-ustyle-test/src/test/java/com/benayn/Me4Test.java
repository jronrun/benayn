package com.benayn;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.AssertionFailedError;

import org.junit.Assert;
import org.junit.Test;

import com.benayn.pre.ManualTicker;
import com.benayn.ustyle.Decision;
import com.benayn.ustyle.Decisional;
import com.benayn.ustyle.Decisions;
import com.benayn.ustyle.Expiring;
import com.benayn.ustyle.Expiring.ExpiringSet;
import com.benayn.ustyle.Funcs;
import com.benayn.ustyle.Gather;
import com.benayn.ustyle.JsonR;
import com.benayn.ustyle.JsonW;
import com.benayn.ustyle.Objects2;
import com.benayn.ustyle.Objects2.FacadeObject;
import com.benayn.ustyle.Pair;
import com.benayn.ustyle.Randoms;
import com.benayn.ustyle.Reflecter;
import com.benayn.ustyle.Resolves;
import com.benayn.ustyle.Triple;
import com.benayn.ustyle.TypeRefer.TypeDescrib;
import com.benayn.ustyle.string.Betner;
import com.benayn.ustyle.string.Finder;
import com.benayn.ustyle.string.Indexer;
import com.benayn.ustyle.string.Replacer;
import com.benayn.ustyle.thirdparty.EnumLookup;
import com.benayn.ustyle.thirdparty.Language;
import com.benayn.ustyle.thirdparty.NetProtocol;
import com.benayn.ustyle.thirdparty.Retryer;
import com.benayn.ustyle.thirdparty.Retryer.BlockStrategy;
import com.benayn.ustyle.thirdparty.Retryer.RetryException;
import com.benayn.ustyle.thirdparty.Retryer.WaitStrategy;
import com.benayn.ustyle.thirdparty.SizeUnit;
import com.benayn.ustyle.thirdparty.Threads;
import com.benayn.ustyle.thirdparty.Threads.AsyncRunner;
import com.benayn.ustyle.thirdparty.UriEscaper;
import com.benayn.ustyle.thirdparty.Uris;
import com.benayn.ustyle.thirdparty.Wills;
import com.benayn.ustyle.thirdparty.Wills.Action;
import com.benayn.ustyle.thirdparty.Wills.Will;
import com.benayn.ustyle.thirdparty.Wills.WillExecutorService;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.escape.Escaper;
import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
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
    
    @Test
    public void testTheGather() {
    	//testToArray
        check(Lists.newArrayList(Gather.<Integer>empty().asArray(Integer.class)));
        check(Lists.newArrayList(Gather.from(1, 2, 3).asArray(Integer.class)), 1, 2, 3);
        
        //testToList
        check(Gather.<Integer>empty().list());
        check(Gather.from(1, 2, 3).list(), 1, 2, 3);
        
        //testToMap
        check(Gather.<Integer>empty().asMap(plus(10)));
        check(Gather.from(1, 2, 3).asMap(plus(10)), 11, 1, 12, 2, 13, 3);
        
        //testUnique
        check(Gather.<Integer>empty().filter(Decisions.<Integer>unique()).each());
        check(Gather.from(1, 2, 3).filter(Decisions.<Integer>unique()).each(), 1, 2, 3);
        check(Gather.from(1, 1, 3, null, 1, 2, 1, 3, 3, null).filter(Decisions.<Integer>unique()).each(), 1, 3, null, 2);
        
        //testTake
        check(Gather.from().take(0).each());
        check(Gather.from().take(2).each());
        
        check(Gather.from(1, 2, 3).take(0).each());
        check(Gather.from(1, 2, 3).take(2).each(), 1, 2);
        check(Gather.from(1, 2, 3).take(4).each(), 1, 2, 3);
        
        //testTakeDropWhile
        check(Gather.<Integer>empty().takeWhile(odd()).each());
        check(Gather.from(1, 2, 3).takeWhile(odd()).each(), 1);
        check(Gather.from(1, 3, 5, 2, 3).takeWhile(odd()).takeWhile(odd()).each(), 1, 3, 5);
        
        check(Gather.<Integer>empty().dropWhile(odd()).each());
        check(Gather.from(1, 2, 3).dropWhile(odd()).each(), 2, 3);
        check(Gather.from(1, 3, 5, 2, 3).dropWhile(odd()).dropWhile(odd()).each(), 2, 3);
        
        //aTestSplit
        List<List<Integer>> ll = Gather.<Integer>empty().split(odd());
        check(ll.get(0));
        check(ll.get(1));
        
        ll = Gather.from(1, 2, 3, 4, 5).split(odd());
        check(ll.get(0), 1, 3, 5);
        check(ll.get(1), 2, 4);
        
        ll = Gather.from(1, 3, 5).split(odd());
        check(ll.get(0), 1, 3, 5);
        check(ll.get(1));
        
        ll = Gather.from(2, 4, 6).split(odd());
        check(ll.get(0));
        check(ll.get(1), 2, 4, 6);

        //aTestMap
        check(Gather.<Integer>empty().map(plus(10)).each());
        check(Gather.from(1, 2, 3).map(plus(10)).each(), 11, 12, 13);
        
        //aTestLookup
        Map<Integer,Integer> map = Gather.from(1, 2, 3).asMap(plus(10));
        check(Gather.<Integer>empty().map(Funcs.lookup(map)).each());
        check(Gather.from(11, 12, 13).map(Funcs.lookup(map)).each(), 1, 2, 3);
        check(Gather.from(11, 12, 14).map(Funcs.lookup(map, 7)).each(), 1, 2, 7);
        
        //aTestJoin
        assertEquals("", Gather.<Integer>empty().join(""));
        assertEquals("1,2,3", Gather.from(1, 2, 3).join(","));
        assertEquals("1-2-3", Gather.from(1, 2, 3).join(Joiner.on('-')));
    
        //aTestGroupBy
        Multimap<Integer,Integer> m = Gather.<Integer>empty().groupBy(div(3));
        assertEquals(0, m.size());
        m = Gather.from(1, 2, 3, 4, 5).groupBy(div(3));
        check(m.get(0), 1, 2);
        check(m.get(1), 3, 4, 5);
        
        //aTestForeach
        final StringWriter w = new StringWriter();
        Function<Integer,Void> f = new Function<Integer,Void>() {
            public Void apply(Integer i) {
                w.write(i.toString());
                return null;
            };
        };
        
        assertEquals(0, Gather.<Integer>empty().foreach(f));
        assertEquals("", w.toString());
        assertEquals(3, Gather.from(1, 2, 3).foreach(f));
        assertEquals("123", w.toString());
        
        //aTestFinds
        assertEquals(Integer.valueOf(-1), Gather.<Integer>empty().finds(odd()).or(-1));
        assertEquals(Integer.valueOf(1), Gather.from(1, 2, 3).finds(odd()).or(-1));
        assertEquals(Integer.valueOf(3), Gather.from(4, 2, 3).finds(odd()).or(-1));
        assertEquals(Integer.valueOf(-1), Gather.from(4, 2, 0).finds(odd()).or(-1));
        
        //aTestFilter
        check(Gather.<Integer>empty().filter(odd()).each());
        check(Gather.from(1, 2, 3).filter(odd()).each(), 1, 3);
        
        //aTestFactories
        check(Gather.<Integer>empty().each());
        check(Gather.from((List<Integer>) null).each());
        check(Gather.from(1, 2, 3).each(), 1, 2, 3);
        check(Gather.from(newArrayList()).each());
        check(Gather.from(newArrayList(1, 2, 3)).each(), 1, 2, 3);
        check(Gather.from(newArrayList().toArray()).each());
        check(Gather.from(newArrayList(1, 2, 3).toArray()).each(), 1, 2, 3);
    
        //aTestEach
        int n = 1;
        for (Integer ma: Gather.from(1, 2, 3).each()) {
            assertEquals(Integer.valueOf(n++), ma);
        }
    
    	//aTestConcat
        check(Gather.<Integer>empty().concat(Gather.<Integer>empty().each()).each());
        check(Gather.<Integer>empty()
        		.concat(Gather.from(1).each())
        		.concat(Gather.from(2).each())
        		.concat(Gather.from(3).each()).each(), 1, 2, 3);
        check(Gather.<Integer>empty()
        		.concat(Gather.from(1)
        				.concat(Gather.from(2)
        						.concat(Gather.from(3).each()).each()).each()).each(), 1, 2, 3);
    
    	//aTestAnyAll
        assertFalse(Gather.<Integer>empty().any(odd()));
        assertTrue(Gather.<Integer>empty().all(odd()));
        assertTrue(Gather.from(1, 2, 3).any(odd()));
        assertFalse(Gather.from(1, 2, 3).all(odd()));
        assertTrue(Gather.from(1, 3, 5).all(odd()));
    
    	//aTestAddToMap
        Map<Integer,Integer> map2 = Maps.newHashMap();
        Gather.<Integer>empty().foreach(Funcs.addTo(map2, plus(10)));
        check(map2);
        Gather.from(1, 2, 3).foreach(Funcs.addTo(map2, plus(10)));
        check(map2, 11, 1, 12, 2, 13, 3);
    
        //aTestAddTo
		List<Integer> list = Lists.newArrayList();
    	Gather.<Integer>empty().foreach(Funcs.addTo(list));
        check(list);
        Gather.from(1, 2, 3).foreach(Funcs.addTo(list));
        check(list, 1, 2, 3);
	}
	
	private Predicate<Integer> odd() {
        return new Predicate<Integer>() {
            public boolean apply(Integer input) {
                return input % 2 == 1;
            }
        };
    }
    
    private Function<Integer,Integer> plus(final int b) {
        return new Function<Integer,Integer>() {
            public Integer apply(Integer a) {
                return a + b;
            }
        };
    }
    
    private Function<Integer,Integer> div(final int b) {
        return new Function<Integer,Integer>() {
            public Integer apply(Integer a) {
                return a / b;
            }
        };
    }

	static <T> void check(Iterable<T> seq, @SuppressWarnings("unchecked") T... expecteds) {
        check(seq.iterator(), expecteds);
    }
    
    static <T> void check(Iterator<T> seq, @SuppressWarnings("unchecked") T... expecteds) {
        List<T> actuals = newArrayList(seq);
        assertEquals(expecteds.length, actuals.size());
        Iterator<T> actual = actuals.iterator();
        for (T expected: expecteds) {
            assertEquals(expected, actual.next());
        }
        assertFalse(actual.hasNext());
    }
    
	static <T> void check(Map<T,T> map, @SuppressWarnings("unchecked") T... expecteds) {
        assertEquals(expecteds.length / 2, map.size());
        for (int i = 0; i < expecteds.length; i += 2) {
            assertEquals(expecteds[i+1], map.get(expecteds[i]));
        }
    }
	
//@see https://github.com/toonetown/guava-ext
    
    @Test
    public void testEnumLookup() {
    	assertEquals(IntEnum.find(1), IntEnum.FIRST);
        assertEquals(IntEnum.find(0), IntEnum.NONE);
        
        assertNull(IntEnum.find(3));
        
        assertEquals(IntEnum.all(), Sets.newHashSet(IntEnum.NONE, IntEnum.SECOND, IntEnum.FIRST));
        
        assertEquals(StringEnum.find("MyValue"), StringEnum.MINE);
        assertEquals(StringEnum.find("MYVALUE"), StringEnum.MINE);
        assertEquals(StringEnum.find("myvalue"), StringEnum.MINE);
        
        assertEquals(StringEnum.findCase("MyValue"), StringEnum.MINE);
        
        assertNull(StringEnum.findCase("MYVALUE"));
        
        assertEquals(DoubleEnum.find("MyValue"), DoubleEnum.DOUBLE);
        assertEquals(DoubleEnum.findCase("MyValue"), DoubleEnum.DOUBLE);
        assertEquals(DoubleEnum.find("myvalue"), DoubleEnum.DOUBLE);
        assertEquals(DoubleEnum.find(42), DoubleEnum.DOUBLE);
        
        assertNull(DoubleEnum.find("gone"));
        
        assertNull(DoubleEnum.findCase("myvalue"));
        assertNull(DoubleEnum.find(1));
    }
    
    public static enum StringEnum implements EnumLookup.Keyed<String> {
        MINE("MyValue");
        
        private final String value;
        
        private StringEnum(String value) {
        	this.value = value;
        }
        
        /* All our values */
        private static final EnumLookup<StringEnum, String> $ALL = EnumLookup.of(StringEnum.class);
        public static StringEnum find(final String s) { return $ALL.find(s); }
        public static Set<StringEnum> all() { return $ALL.keySet(); }
        
        /* Case-sensitive lookup */
        private static final EnumLookup<StringEnum, String> $ALL_CASE = EnumLookup.of(StringEnum.class, true);
        public static StringEnum findCase(final String s) { return $ALL_CASE.find(s); }
		
		@Override
		public String getValue() {
			return value;
		}
    }
    
    public static enum DoubleEnum implements EnumLookup.MultiKeyed {
        DOUBLE("MyValue", 42);
        
        private final String stringValue;
        private final Integer intValue;
        
        private DoubleEnum(String stringValue, Integer intValue) {
        	this.stringValue = stringValue;
        	this.intValue = intValue;
        }
        
        @Override public Object[] getValue() { return new Object[] {stringValue, intValue}; }

        /* All our values */
        private static final EnumLookup<DoubleEnum, String> $ALL_BY_STRING = EnumLookup.of(DoubleEnum.class, 0);
        public static DoubleEnum find(final String s) { return $ALL_BY_STRING.find(s); }
        public static Set<DoubleEnum> all() { return $ALL_BY_STRING.keySet(); }
        
        /* Case-sensitive lookup */
        private static final EnumLookup<DoubleEnum, String> $ALL_CASE = EnumLookup.of(DoubleEnum.class, 0, true);
        public static DoubleEnum findCase(final String s) { return $ALL_CASE.find(s); }

        /* Lookup by integer */
        private static final EnumLookup<DoubleEnum, Integer> $ALL_BY_INT = EnumLookup.of(DoubleEnum.class, 1);
        public static DoubleEnum find(final Integer i) { return $ALL_BY_INT.find(i); }
    }
    
    public static enum IntEnum implements EnumLookup.Keyed<Integer> {
        FIRST(1),
        SECOND(2),
        NONE(0);
        
        private final Integer value;
        
        private IntEnum(Integer value) {
        	this.value = value;
        }
        
        private static final EnumLookup<IntEnum, Integer> $ALL = EnumLookup.of(IntEnum.class);
        public static IntEnum find(final Integer i) { return $ALL.find(i); }
        public static Set<IntEnum> all() { return $ALL.keySet(); }
		
		@Override public Integer getValue() {
			return value;
		}
    }
	
    @Test
    public void testSizeUnit() {
    	@SuppressWarnings("unchecked")
		Gather<Pair<SizeUnit, Long>> gather = Gather.from(Pair.of(SizeUnit.BYTE, 1L),
				Pair.of(SizeUnit.KILOBYTE, 1000L),
				Pair.of(SizeUnit.MEGABYTE, 1000L * 1000L),
				Pair.of(SizeUnit.GIGABYTE, 1000L * 1000L * 1000L),
				Pair.of(SizeUnit.TERABYTE, 1000L * 1000L * 1000L * 1000L),
				Pair.of(SizeUnit.KIBIBYTE, 1024L),
				Pair.of(SizeUnit.MEBIBYTE, 1024L * 1024L),
				Pair.of(SizeUnit.GIBIBYTE, 1024L * 1024L * 1024L),
				Pair.of(SizeUnit.TEBIBYTE, 1024L * 1024L * 1024L * 1024L));
    	
    	gather.loop(new Decisional<Pair<SizeUnit,Long>>() {

			@Override protected void decision(Pair<SizeUnit, Long> input) {
				assertDeepEqual(input.getL().getNumBytes(), input.getR());
				assertDeepEqual(SizeUnit.BYTE.convert(1.0, input.getL()), (double) input.getR());
				assertDeepEqual(input.getL().convert((double) input.getR(), SizeUnit.BYTE), 1.0);
			}
		});
    }
    
    @Test
    public void testNetProtocol() {
    	assertDeepEqual(NetProtocol.find(21), NetProtocol.FTP);
    	assertNull(NetProtocol.find(0));
    	assertDeepEqual(NetProtocol.find("imap"), NetProtocol.IMAP);
    	assertDeepEqual(NetProtocol.find("SMTP"), NetProtocol.SMTP);
    	assertNull(NetProtocol.find("file"));
    	assertDeepEqual(NetProtocol.find(21, NetProtocol.HTTP), NetProtocol.FTP);
    	assertDeepEqual(NetProtocol.find(0, NetProtocol.HTTP), NetProtocol.HTTP);
    	assertDeepEqual(NetProtocol.find("imap", NetProtocol.HTTP), NetProtocol.IMAP);
    	assertDeepEqual(NetProtocol.find("SMTP", NetProtocol.HTTP), NetProtocol.SMTP);
    	assertDeepEqual(NetProtocol.find("file", NetProtocol.HTTP), NetProtocol.HTTP);
    }
    
    @Test
    public void testUriEscaper() throws URISyntaxException {
    	//testEscape
    	UriEscaper e = UriEscaper.instance();
        assertEquals(e.escape("http://site.com/my path/?this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                 "http://site.com/my%20path/?this=with+space's&money=$/%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
        assertEquals(e.escape("site.com:8080/a:/b/c"), "site.com:8080/a:/b/c");
        assertEquals(e.escape("http://site.com:8000/a:/b/c"), "http://site.com:8000/a:/b/c");
        assertEquals(e.escape("site.com/?1 2+3%204"), "site.com/?1+2+3%204");
        assertEquals(e.escape("a:b@site.com:8080/a:@/b/c"), "a:b@site.com:8080/a:@/b/c");
        assertEquals(e.escape("http://a:b@site.com:8000/a:@/b/c"), "http://a:b@site.com:8000/a:@/b/c");
        assertEquals(e.escape("http://site.com:8080/a/b/c.html?me=http://test.com:8000/a/b"),
                              "http://site.com:8080/a/b/c.html?me=http://test.com:8000/a/b");
        
        //testEscape_strict
        e = UriEscaper.strictInstance();
        assertEquals(e.escape("http://site.com/my path/?this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                "http://site.com/my%20path/?this=with%20space%27s&money=$%2F%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
       assertEquals(e.escape("site.com:8080/a:/b/c"), "site.com:8080/a%3A/b/c");
       assertEquals(e.escape("http://site.com:8000/a:/b/c"), "http://site.com:8000/a%3A/b/c");
       assertEquals(e.escape("site.com/?1 2+3%204"), "site.com/?1%202%203%204");
       assertEquals(e.escape("a:b@site.com:8080/a:@/b/c"), "a:b@site.com:8080/a%3A%40/b/c");
       assertEquals(e.escape("http://a:b@site.com:8000/a:@/b/c"), "http://a:b@site.com:8000/a%3A%40/b/c");
       assertEquals(e.escape("http://site.com:8080/a/b/c.html?me=http://test.com:8000/a/b"),
                             "http://site.com:8080/a/b/c.html?me=http%3A%2F%2Ftest.com%3A8000%2Fa%2Fb");
       
       //testStrictness
       final Escaper esc = UriEscaper.instance();
       final Escaper strict = UriEscaper.strictInstance();
       final String s = "http://site.com/my path/?this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m";
       final String sStrict = strict.escape(s);
       /* Encoding a strict string should yield the same string */
       assertEquals(esc.escape(sStrict), sStrict);

       final String sEsc = esc.escape(s);
       /* Strictly-encoding a non-strict string does not necessarily yield the same string */
       assertNotEquals(strict.escape(sEsc), sEsc);
       /* But it should be the same as the strict string */
       assertEquals(strict.escape(sEsc), sStrict);
       
       //testEscapePath_strict
       e = UriEscaper.strictInstance();
       assertEquals(e.escapePath("my path/?this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                                "my%20path/?this=with%20space%27s&money=$%2F%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
       
       //testEscapeQuery
       e = UriEscaper.instance();
       assertEquals(e.escapeQuery("this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                                  "this=with+space's&money=$/%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
       assertEquals(e.escapeQuery("?this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                                  "?this=with+space's&money=$/%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
       assertEquals(e.escapeQuery("this=with space's&money?=$/\u20AC&rate=5%apr%26terms%a#b m"),
                                  "this=with+space's&money?=$/%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
       assertEquals(e.escapeQuery("http://test.com:8000/a/b"), "http://test.com:8000/a/b");
       
       //testEscapeQuery_strict
       e = UriEscaper.strictInstance();
       assertEquals(e.escapeQuery("this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                                  "this=with%20space%27s&money=$%2F%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
       assertEquals(e.escapeQuery("?this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                                  "%3Fthis=with%20space%27s&money=$%2F%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
       assertEquals(e.escapeQuery("this=with space's&money?=$/\u20AC&rate=5%apr%26terms%a#b m"),
                                  "this=with%20space%27s&money%3F=$%2F%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
       assertEquals(e.escapeQuery("http://test.com:8000/a/b"), "http%3A%2F%2Ftest.com%3A8000%2Fa%2Fb");
       
       //testEscapeQueryParam
       e = UriEscaper.instance();
       assertEquals(e.escapeQueryParam("this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                    "this%3Dwith+space's%26money%3D$/%E2%82%AC%26rate%3D5%25apr%26terms%25a%23b+m");
       assertEquals(e.escapeQueryParam("?this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                    "?this%3Dwith+space's%26money%3D$/%E2%82%AC%26rate%3D5%25apr%26terms%25a%23b+m");
       assertEquals(e.escapeQueryParam("this=with space's&money?=$/\u20AC&rate=5%apr%26terms%a#b m"),
                    "this%3Dwith+space's%26money?%3D$/%E2%82%AC%26rate%3D5%25apr%26terms%25a%23b+m");
       assertEquals(e.escapeQueryParam("http://test.com:8000/a/b"), "http://test.com:8000/a/b");
       
       //testEscapeQueryParam_strict
       e = UriEscaper.strictInstance();
       assertEquals(e.escapeQueryParam("this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                    "this%3Dwith%20space%27s%26money%3D%24%2F%E2%82%AC%26rate%3D5%25apr%26terms%25a%23b%20m");
       assertEquals(e.escapeQueryParam("?this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                    "%3Fthis%3Dwith%20space%27s%26money%3D%24%2F%E2%82%AC%26rate%3D5%25apr%26terms%25a%23b%20m");
       assertEquals(e.escapeQueryParam("this=with space's&money?=$/\u20AC&rate=5%apr%26terms%a#b m"),
                    "this%3Dwith%20space%27s%26money%3F%3D%24%2F%E2%82%AC%26rate%3D5%25apr%26terms%25a%23b%20m");
       assertEquals(e.escapeQueryParam("http://test.com:8000/a/b"), "http%3A%2F%2Ftest.com%3A8000%2Fa%2Fb");
       
       //testEscapeFragment
       e = UriEscaper.instance();
       assertEquals(e.escapeFragment("b m"), "b%20m");
       assertEquals(e.escapeFragment("#b m"), "%23b%20m");
       assertEquals(e.escapeFragment("b #m"), "b%20%23m");
       
       //testEscapeFragment_strict
       e = UriEscaper.strictInstance();
       assertEquals(e.escapeFragment("b m"), "b%20m");
       assertEquals(e.escapeFragment("#b m"), "%23b%20m");
       assertEquals(e.escapeFragment("b #m"), "b%20%23m");
       
       //testProperEscaping
       /* Tests whether or not we are able to make a URI from any possible character */
       e = UriEscaper.instance();
       /* For speed, we just test *some* of the higher-level characters */
       for (char c = 0x0000; c <= 0x0FFF; c++) {
           assertNotNull(new URI(e.escape("http://site.com/" + c + "/file.html?val=" + c + "#" + c)));
       }
       
       //testEscapeNull
       assertNull(UriEscaper.instance().escape(null));
       
       //testEscapeStrictChars
       // This is the set of characters from the BOSS API.  When we want a plus, it should be encoded, since
       // we lean towards "+" meaning space (relying upon the browser to encode it for us)
       assertEquals(UriEscaper.instance().escapeQueryParam("/?&;:@,$= %\"%2B#*<>{}|[]^\\`()"),
                    "/?%26;:@,$%3D+%25%22%2B%23*%3C%3E%7B%7D%7C%5B%5D%5E%5C%60()");
       assertEquals(UriEscaper.strictInstance().escapeQueryParam("/?&;:@,$= %\"%2B#*<>{}|[]^\\`()"),
                    "%2F%3F%26%3B%3A%40%2C%24%3D%20%25%22%2B%23%2A%3C%3E%7B%7D%7C%5B%5D%5E%5C%60%28%29");
       assertEquals(UriEscaper.strictInstance().escapeQueryParam("/?&;:@,$= %\"+#*<>{}|[]^\\`()"),
                    "%2F%3F%26%3B%3A%40%2C%24%3D%20%25%22%2B%23%2A%3C%3E%7B%7D%7C%5B%5D%5E%5C%60%28%29");
    }
    
    @Test
    public void testUris() throws URISyntaxException {
    	Gather<Triple<Boolean, URI, URI>> gather = newGather(false).concat(newGather(true).each());
    	gather.loop(new Decisional<Triple<Boolean,URI,URI>>() {

			@Override protected void decision(Triple<Boolean, URI, URI> input) {
				//LCR: strict, newUri, expectedUri
				assertEquals(input.getC(), input.getR());
		        try {
					assertNormalized(input.getL(), input.getC());
					assertEncoding(input.getL(), input.getC().toString(), input.getR().toString());
				} catch (URISyntaxException e) {
					throw new AssertionFailedError(e.getMessage());
				}
			}
		});
    	
    	assertEquals(Uris.getDirectory(u("http://site.com/a/b/c.html?this=that")), "/a/b/");
        assertEquals(Uris.getDirectory(u("http://site.com/a/b/c?this=that")), "/a/b/");
        assertEquals(Uris.getDirectory(u("http://site.com/a/b/c/?this=that")), "/a/b/c/");
        assertEquals(Uris.getDirectory(u("http://site.com/")), "/");
        assertEquals(Uris.getDirectory(u("http://site.com")), "/");
        
        assertEquals(Uris.getFile(u("http://site.com/a/b/c.html?this=that")), "c.html");
        assertEquals(Uris.getFile(u("http://site.com/a/b/c?this=that")), "c");
        assertEquals(Uris.getFile(u("http://site.com/a/b/c/?this=that")), "");
        assertEquals(Uris.getFile(u("http://site.com/")), "");
        assertEquals(Uris.getFile(u("http://site.com")), "");
        
        testToDomain(false);
        testToDomain(true);
        
        testToDirectory(false);
        testToDirectory(true);
        
        testToPath(false);
        testToPath(true);
        
        testResolve_absolute(false);
        testResolve_absolute(true);
        
        testResolve_relative(false);
        testResolve_relative(true);
        
        testResolve_relativeDir(false);
        testResolve_relativeDir(true);
        
        testResolve_queryRelative(false);
        testResolve_queryRelative(true);
        
        testResolve_fragment(false);
        testResolve_fragment(true);
        
        testResolveParams(false);
        testResolveParams(true);
        
        testEncoding(false);
        testEncoding(true);
        
        testReplaceHost(false);
        testReplaceHost(true);
        
        testPrependHost(false);
        testPrependHost(true);
    }
    
    public void testPrependHost(final boolean strict) throws URISyntaxException {
        assertEquals(Uris.replaceHost(new URI("http://test.com"), "site.test.com", strict),
                     Uris.newUri("http://site.test.com", strict));
    }
    
    public void testReplaceHost(final boolean strict) throws URISyntaxException {
        assertEquals(Uris.replaceHost(new URI("http://test.com"), "site.com", strict),
                     Uris.newUri("http://site.com", strict));
        assertEquals(Uris.replaceHost(new URI("http://test.com/"), "site.com", strict),
                     Uris.newUri("http://site.com/", strict));
        assertEquals(Uris.replaceHost(new URI("http://test.com:8080"), "site.com", strict),
                     Uris.newUri("http://site.com:8080", strict));
        assertEquals(Uris.replaceHost(new URI("https://test.com"), "site.com", strict),
                     Uris.newUri("https://site.com/", strict));
        assertEquals(Uris.replaceHost(new URI("http://a:b@test.com"), "site.com", strict),
                     Uris.newUri("http://a:b@site.com", strict));
        assertEquals(Uris.replaceHost(new URI("http://test.com/a/b/c.html?this=that"), "site.com", strict),
                     Uris.newUri("http://site.com/a/b/c.html?this=that", strict));
        assertEquals(Uris.replaceHost(new URI("http://test.com/#bm"), "site.com", strict),
                     Uris.newUri("http://site.com/#bm", strict));
        assertEquals(Uris.replaceHost(new URI("http://test.com/a/b/c.html?me=test.com"), "site.com", strict),
                     Uris.newUri("http://site.com/a/b/c.html?me=test.com", strict));
    }
    
    public void testEncoding(final boolean strict) throws URISyntaxException {
        assertEncoding(strict,
                       "site.com/a b/?c=d e's&m=$/\u20AC&r=5%apr%26t%a#b m",
                       strict ?
                           "http://site.com:80/a%20b/?c=d%20e%27s&m=$%2F%E2%82%AC&r=5%25apr%26t%25a#b%20m" :
                           "http://site.com:80/a%20b/?c=d+e's&m=$/%E2%82%AC&r=5%25apr%26t%25a#b%20m");
        assertEncoding(strict,
                       "site.com/a b/?c=d%20e's&m=$/\u20AC&r=5%apr%26t%a#b m",
                       strict ?
                           "http://site.com:80/a%20b/?c=d%20e%27s&m=$%2F%E2%82%AC&r=5%25apr%26t%25a#b%20m" :
                           "http://site.com:80/a%20b/?c=d%20e's&m=$/%E2%82%AC&r=5%25apr%26t%25a#b%20m");
        assertEncoding(strict,
                       "site.com/a b/?c=d+e's&m=$/\u20AC&r=5%apr%26t%a#b m",
                       strict ?
                           "http://site.com:80/a%20b/?c=d%20e%27s&m=$%2F%E2%82%AC&r=5%25apr%26t%25a#b%20m" :
                           "http://site.com:80/a%20b/?c=d+e's&m=$/%E2%82%AC&r=5%25apr%26t%25a#b%20m");
        assertEncoding(strict,
                       "site.com/a%20b/?c=d e%27s&m=$/\u20AC&r=5%apr%26t%a#b m",
                       strict ?
                           "http://site.com:80/a%20b/?c=d%20e%27s&m=$%2F%E2%82%AC&r=5%25apr%26t%25a#b%20m" :
                           "http://site.com:80/a%20b/?c=d+e%27s&m=$/%E2%82%AC&r=5%25apr%26t%25a#b%20m");
    }
    
	private void assertEncoding(final boolean strict, final String url, final String expected) throws URISyntaxException {
		final URI u = Uris.newUri(url, strict);
		assertEquals(u.toString(), expected);
		assertEquals(Uris.toRawString(u, strict), expected);
		assertTrue(Uris.isNormalized(u, strict));
		assertEquals(u, URI.create(expected));
	}
    
    public void testResolveParams(final boolean strict) throws URISyntaxException {
        assertEquals(Uris.resolveParams(u("http://site.com/a/b/c.html?this=that"),
                                        ImmutableMap.of("more", "true"),
                                        strict),
                     Uris.newUri("http://site.com/a/b/c.html?this=that&more=true", strict));
        assertEquals(Uris.resolveParams(u("http://site.com/a/b/c.html"),
                                        ImmutableMap.of("more", "true", "money", "20$ bill&note"),
                                        strict),
                     Uris.newUri("http://site.com/a/b/c.html?more=true&money=20" +
                                 (strict ? "%24" : "$") +
                                 "+bill%26note",
                                 strict));
        assertTrue(Uris.isNormalized(Uris.resolveParams(u("http://site.com/a/b/c.html"),
                                                        ImmutableMap.of("more", "true", "money", "20$ bill&note"),
                                                        strict),
                                     strict));
    }
    public void testResolve_fragment(final boolean strict) throws URISyntaxException {
        assertEquals(Uris.resolve(u("http://site.com/a/b/c.html?this=that"), "#here", strict),
                        Uris.newUri("http://site.com/a/b/c.html?this=that#here", strict));
        assertEquals(Uris.resolve(u("http://site.com/a/b/c.html?this=that#there"), "#here", strict),
                        Uris.newUri("http://site.com/a/b/c.html?this=that#here", strict));
        assertEquals(Uris.resolve(u("http://site.com"), "#here", strict),
                     Uris.newUri("http://site.com/#here", strict));
        assertTrue(Uris.isNormalized(Uris.resolve(u("http://site.com/a/b/c.html?this=that"), "#here", strict),
                                     strict));
    }
    public void testResolve_queryRelative(final boolean strict) throws URISyntaxException {
        assertEquals(Uris.resolve(u("http://site.com/a/b/c.html?this=that"), "&more=true", strict),
                     Uris.newUri("http://site.com/a/b/c.html?this=that&more=true", strict));
        assertEquals(Uris.resolve(u("http://site.com/a/b/c.html"), "&more=true", strict),
                     Uris.newUri("http://site.com/a/b/c.html?more=true", strict));
        assertTrue(Uris.isNormalized(Uris.resolve(u("http://site.com/a/b/c.html?this=that"), "&more=true", strict),
                                     strict));
    }
    
    public void testResolve_relativeDir(final boolean strict) throws URISyntaxException {
        final URI base = u("http://site.com/a/b/c.html?this=that");

        assertEquals(Uris.resolve(base, "./other.html", strict),
                     Uris.newUri("http://site.com/a/b/c.html/other.html", strict));
        assertEquals(Uris.resolve(base, "./d/e/f?more=true", strict),
                     Uris.newUri("http://site.com/a/b/c.html/d/e/f?more=true", strict));
        assertEquals(Uris.resolve(base, "./with space", strict),
                     Uris.newUri("http://site.com/a/b/c.html/with%20space", strict));
        assertTrue(Uris.isNormalized(Uris.resolve(base, "./other.html", strict), strict));
    }
    
    public void testResolve_relative(final boolean strict) throws URISyntaxException {
        final URI base = u("http://site.com/a/b/c.html?this=that");
        
        assertEquals(Uris.resolve(base, "other.html", strict),
                     Uris.newUri("http://site.com/a/b/other.html", strict));
        assertEquals(Uris.resolve(base, "d/e/f?more=true", strict),
                     Uris.newUri("http://site.com/a/b/d/e/f?more=true", strict));
        assertEquals(Uris.resolve(base, "with space", strict),
                     Uris.newUri("http://site.com/a/b/with%20space", strict));
        assertTrue(Uris.isNormalized(Uris.resolve(base, "other.html", strict), strict));
    }
    
    public void testResolve_absolute(final boolean strict) throws URISyntaxException {
        final URI base = u("http://site.com/a/b/c.html?this=that");
        
        assertEquals(Uris.resolve(base, "/other.html", strict),
                     Uris.newUri("http://site.com/other.html", strict));
        assertEquals(Uris.resolve(base, "/d/e/f?more=true", strict),
                     Uris.newUri("http://site.com/d/e/f?more=true", strict));
        assertEquals(Uris.resolve(base, "/?more=true", strict),
                     Uris.newUri("http://site.com/?more=true", strict));
        assertEquals(Uris.resolve(base, "/with space", strict),
                     Uris.newUri("http://site.com/with%20space", strict));
        assertTrue(Uris.isNormalized(Uris.resolve(base, "/other.html", strict), strict));
    }
    
    public void testToPath(final boolean strict) throws URISyntaxException {
        final URI u1 = Uris.newUri("http://site.com/a/b/c.html", strict);
        final URI u2 = Uris.newUri("http://site.com/a/b/c", strict);
        final URI u3 = Uris.newUri("http://site.com/a/b/c/", strict);
        final URI u4 = Uris.newUri("http://site.com/", strict);
        
        assertEquals(Uris.toPath(Uris.newUri("site.com/a/b/c.html?this=that", false), strict), u1);
        assertEquals(Uris.toPath(u("http://site.com/a/b/c.html?this=that"), strict), u1);
        assertEquals(Uris.toPath(u("http://site.com/a/b/c.html?"), strict), u1);
        assertEquals(Uris.toPath(u("http://site.com/a/b/c.html"), strict), u1);
        assertEquals(Uris.toPath(u("http://site.com/a/b/c?this=that"), strict), u2);
        assertEquals(Uris.toPath(u("http://site.com/a/b/c/?this=that"), strict), u3);
        assertEquals(Uris.toPath(u("http://site.com/"), strict), u4);
        assertEquals(Uris.toPath(u("http://site.com"), strict), u4);
        assertEquals(Uris.toPath(Uris.newUri("http://site.com/with space/f s?this=that", false), strict),
                     Uris.newUri("http://site.com/with space/f%20s", strict));
    }
    public void testToDirectory(final boolean strict) throws URISyntaxException {
        final URI u1 = Uris.newUri("http://site.com/a/b/", strict);
        final URI u2 = Uris.newUri("http://site.com/a/b/c/", strict);
        final URI u3 = Uris.newUri("http://site.com/", strict);
        
        assertEquals(Uris.toDirectory(Uris.newUri("site.com/a/b/c.html?this=that", false), strict), u1);
        assertEquals(Uris.toDirectory(u("http://site.com/a/b/c.html?this=that"), strict), u1);
        assertEquals(Uris.toDirectory(u("http://site.com/a/b/c?this=that"), strict), u1);
        assertEquals(Uris.toDirectory(u("http://site.com/a/b/c/?this=that"), strict), u2);
        assertEquals(Uris.toDirectory(u("http://site.com/"), strict), u3);
        assertEquals(Uris.toDirectory(u("http://site.com"), strict), u3);
        assertEquals(Uris.toDirectory(Uris.newUri("http://site.com/with space/file?this=that", false), strict),
                                      Uris.newUri("http://site.com/with space/", strict));
        assertEquals(Uris.toDirectory(Uris.newUri("http://site.com/with space/file?this=that", false), strict),
                                      Uris.newUri("http://site.com/with%20space/", strict));
    }
    
    public void testToDomain(final boolean strict) throws URISyntaxException {
        final URI u = Uris.newUri("http://site.com/", strict);
        
        assertEquals(Uris.toDomain(Uris.newUri("site.com/a/b/c.html?this=that", false), strict), u);
        assertEquals(Uris.toDomain(u("http://site.com/a/b/c.html?this=that"), strict), u);
        assertEquals(Uris.toDomain(u("http://site.com/a/b/c?this=that"), strict), u);
        assertEquals(Uris.toDomain(u("http://site.com/a/b/c/?this=that"), strict), u);
        assertEquals(Uris.toDomain(u("http://site.com/"), strict), u);
        assertEquals(Uris.toDomain(u("http://site.com"), strict), u);
    }
    
    private void assertNormalized(final boolean strict, final URI uri) throws URISyntaxException {
        assertEquals(Uris.getScheme(uri), uri.getScheme());
        assertEquals(Uris.getUserInfo(uri), uri.getUserInfo());
        assertEquals(Uris.getRawUserInfo(uri), uri.getRawUserInfo());
        assertEquals(Uris.getHost(uri), uri.getHost());
        assertEquals(Uris.getPort(uri), uri.getPort());
        assertEquals(Uris.getPath(uri), uri.getPath());
        assertEquals(Uris.getRawPath(uri, strict), uri.getRawPath());
        assertEquals(Uris.getQuery(uri), uri.getQuery());
        assertEquals(Uris.getRawQuery(uri, strict), uri.getRawQuery());
        assertEquals(Uris.getFragment(uri), uri.getFragment());
        assertEquals(Uris.getRawFragment(uri, strict), uri.getRawFragment());
    }
    
    @SuppressWarnings("unchecked")
	Gather<Triple<Boolean, URI, URI>> newGather(boolean strict) throws URISyntaxException {
    	return Gather.from(Triple.of(strict, Uris.newUri("site.com", strict), u("http://site.com:80/")),
    			Triple.of(strict, Uris.newUri("https://site.com", strict), u("https://site.com:443/")),
    			Triple.of(strict, Uris.newUri("http://site.com:8080", strict), u("http://site.com:8080/")),
    			Triple.of(strict, Uris.newUri("http://site.com/", strict), u("http://site.com:80/")),
    			Triple.of(strict, Uris.newUri("http://site.com/path", strict), u("http://site.com:80/path")),
    			Triple.of(strict, Uris.newUri("http://site.com/path/file.html", strict),
                       u("http://site.com:80/path/file.html")),
               Triple.of(strict, Uris.newUri("http://site.com?this=that", strict),
                       u("http://site.com:80/?this=that")),
               Triple.of(strict, Uris.newUri("http://site.com/path?this=that", strict),
                       u("http://site.com:80/path?this=that")),
               Triple.of(strict, Uris.newUri("http://site.com/my path/?this=with space's%26such&things", strict),
                       strict ? u("http://site.com:80/my%20path/?this=with%20space%27s%26such&things") :
                       u("http://site.com:80/my%20path/?this=with+space's%26such&things")),
               Triple.of(strict, Uris.newUri("http://site.com/path?", strict), u("http://site.com:80/path")),
               Triple.of(strict, Uris.newUri("http://user:pass@site.com", strict),
                       u("http://user:pass@site.com:80/")),
               Triple.of(strict, Uris.newUri("http://user:p%40ss@site.com", strict),
                       u("http://user:p%40ss@site.com:80/")));
    }
    
    static URI u(final String u) throws URISyntaxException { return new URI(u); }
    
    @SuppressWarnings("unchecked")
	@Test
    public void testLanguage() {
    	Gather.<Pair<Locale, Language>>from(Pair.of(Locale.ENGLISH, Language.ENGLISH),
    			Pair.of(Locale.FRENCH, Language.FRENCH),
    			Pair.of(Locale.GERMAN, Language.GERMAN),
    			Pair.of(Locale.ITALIAN, Language.ITALIAN),
    			Pair.of(Locale.JAPANESE, Language.JAPANESE),
    			Pair.of(Locale.KOREAN, Language.KOREAN),
    			Pair.of(Locale.SIMPLIFIED_CHINESE, Language.CHINESE_SIMP),
    			Pair.of(Locale.TRADITIONAL_CHINESE, Language.CHINESE_TRAD),
    			Pair.of(Locale.FRANCE, Language.FRENCH),
    			Pair.of(Locale.GERMANY, Language.GERMAN),
    			Pair.of(Locale.ITALY, Language.ITALIAN),
    			Pair.of(Locale.JAPAN, Language.JAPANESE),
    			Pair.of(Locale.KOREA, Language.KOREAN),
    			Pair.of(Locale.CHINA, Language.CHINESE_SIMP),
    			Pair.of(Locale.PRC, Language.CHINESE_SIMP),
    			Pair.of(Locale.TAIWAN, Language.CHINESE_TRAD),
    			Pair.of(Locale.UK, Language.ENGLISH),
    			Pair.of(Locale.US, Language.ENGLISH),
    			Pair.of(Locale.CANADA, Language.ENGLISH),
    			Pair.of(Locale.CANADA_FRENCH, Language.FRENCH),
    			Pair.of(Locale.ROOT, Language.UNKNOWN))
    			
    		.loop(new Decisional<Pair<Locale,Language>>() {

				@Override protected void decision(Pair<Locale, Language> input) {
					Locale locale = input.getL();
					Language language = input.getR();
					
					assertEquals(Language.find(locale), language);
			        assertEquals(Language.find(locale.toLanguageTag()), language);
				}
			});
    	
    	assertEquals(Language.find(new Locale("en", "US")), Language.ENGLISH);
        assertEquals(Language.find(new Locale("EN", "us")), Language.ENGLISH);
        assertEquals(Language.find(new Locale("es")), Language.SPANISH);
        
        assertEquals(Language.find("en-US"), Language.ENGLISH);
        assertEquals(Language.find("es-mx"), Language.SPANISH);
        assertEquals(Language.find("es"), Language.SPANISH);
        
        assertEquals(Language.find("zh-Hant"), Language.CHINESE_TRAD);
        assertEquals(Language.find("zh-hAnS"), Language.CHINESE_SIMP);
        assertEquals(Language.find("zh-Hans-CN"), Language.CHINESE_SIMP);
        assertEquals(Language.find("zh-Hant-CN"), Language.CHINESE_TRAD);
        assertEquals(Language.find("en-cyrl"), Language.ENGLISH);
        
        assertNull(Language.find(new Locale("xx")));
        assertNull(Language.find("xx"));
        assertNull(Language.find(Locale.CHINESE));
    }
    
    @Test
    public void testAsyncRunner() {
    	final AsyncRunner runner = Threads.of(new Runnable() {
            @Override public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Error during test ", e);
                }
            }
        });
    	
        assertFalse(runner.isRunning());
        assertTrue(runner.runAsync());
        assertFalse(runner.runAsync());
        assertTrue(runner.isRunning());
        runner.waitForCompletion();
        assertFalse(runner.isRunning());
    }
    
    @Test
    public void testExpiring() {
    	testTheExpiring();
    	
    	TimeUnit sec = TimeUnit.SECONDS;
    	ManualTicker ticker = new ManualTicker();
    	
    	Set<String> set = Sets.newHashSet("b", "c", "a");
    	ExpiringSet<String> expireSet = null;
    	
    	try {
    		expireSet = Expiring.of(set).handleFormer().buildSet();
		} catch (Exception e) {
			assertEquals(UnsupportedOperationException.class, e.getClass());
			assertTrue(e.getMessage().contains("There is no default expire info, call Expiring#withDefault(long, TimeUnit) first"));
		}
    	
    	expireSet = Expiring.of(set).withTicker(ticker).withDefault(5, sec).buildSet();
    	assertEquals(expireSet, set);
    	
    	ticker.tick(15, sec);
    	assertEquals(expireSet, set);
    	assertFalse(expireSet.hasExpiration("a"));
    	
    	expireSet = Expiring.of(set).withTicker(ticker).withDefault(5, sec).handleFormer().buildSet();
    	assertEquals(expireSet, set);
    	assertTrue(expireSet.hasExpiration("a"));
    	
    	ticker.tick(15, sec);
    	assertEquals(expireSet, Sets.newHashSet());
    }
    
    private void testTheExpiring() {
    	TimeUnit sec = TimeUnit.SECONDS;
    	ManualTicker ticker = new ManualTicker();
    	ExpiringSet<String> set = Expiring.<String>empty().withTicker(ticker).withDefault(5, sec).buildSet();
        assertEquals(set, Sets.<String>newHashSet());
        set.add("a");
        set.add("b");
        set.add("c");
        assertEquals(set, Sets.newHashSet("b", "c", "a"));
        
        ticker.tick(4, sec);
        assertEquals(set, Sets.newHashSet("b", "c", "a"));
        set.add("d");
        assertEquals(set, Sets.newHashSet("b", "c", "a", "d"));

        ticker.tick(1, sec);
        assertTrue(set.contains("d"));
        assertFalse(set.contains("a"));
        assertEquals(set, Sets.newHashSet("d"));
        assertTrue(set.hasExpiration("d"));
        assertFalse(set.hasExpiration("a"));
        ticker.tick(5, sec);
        assertEquals(set, Sets.<String>newHashSet());
        assertFalse(set.hasExpiration("d"));
        
        set.add("a");
        set.add("b");
        set.add("c");
        assertEquals(set, Sets.newHashSet("b", "c", "a"));

        /* Tick some */
        ticker.tick(4, sec);
        assertEquals(set, Sets.newHashSet("b", "c", "a"));
        set.add("d");
        set.remove("b");
        assertEquals(set, Sets.newHashSet("c", "a", "d"));
        
        ticker.tick(11, sec);
        assertEquals(set, Sets.<String>newHashSet());
        set.add("a", 3, sec);
        set.add("b");
        set.add("c");
        assertEquals(set, Sets.newHashSet("b", "c", "a"));

        /* Tick some */
        ticker.tick(4, sec);
        assertEquals(set, Sets.newHashSet("b", "c"));
        set.add("d");
        set.remove("b");
        assertEquals(set, Sets.newHashSet("c", "d"));
        
    }
    
    //
//see https://github.com/avarabyeu/wills
    
    private static final String TEST_STRING = "test";
    static class DemoTask implements Runnable {

        private AtomicInteger counter = new AtomicInteger();

        @Override
        public void run() {
            counter.incrementAndGet();
        }

        public AtomicInteger getCounter() {
            return counter;
        }

        public boolean executed() {
            return counter.get() > 0;
        }
    }   
    
    @Test
    public void testWills() {
        //testGuavaDecorator
        WillExecutorService willExecutorService = Wills.willDecorator(MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1)));
        DemoTask task = new DemoTask();
        Will<?> submit = willExecutorService.submit(task);
        submit.obtain();
        assertTrue(task.executed());
        
        //testJdkDecorator
        willExecutorService = Wills.willDecorator(Executors.newFixedThreadPool(1));
        task = new DemoTask();
        submit = willExecutorService.submit(task);
        submit.obtain();
        assertTrue(task.executed());
        
        //testWill
        Will<String> will = Wills.of(TEST_STRING);
        assertTrue(will.isDone());
        assertFalse(will.isCancelled());
        assertEquals(TEST_STRING, will.obtain());
        
        //testMap
        will = Wills.of(TEST_STRING).map(new Function<String, String>() {

            @Override
            public String apply(String input) {
                assert input != null;
                return input.toUpperCase();
            }
        });
        assertEquals(TEST_STRING.toUpperCase(), will.obtain());
        
        //testwhenSuccessful
        final List<String> results = Lists.newArrayList();
        will = Wills.of(TEST_STRING).whenSuccessful(new Action<String>() {
            @Override public void apply(String s) {
                results.add(s);
            }
        });
        
        /* waits for done */
        will.obtain();
        assertTrue(results.contains(TEST_STRING));
        
        //testWhenFailed
        final List<Throwable> results2 = Lists.newArrayList();
        RuntimeException throwable = new RuntimeException("");
        will = Wills.<String>failedWill(throwable).whenFailed(new Action<Throwable>() {
            @Override public void apply(Throwable throwable) {
                results2.add(throwable);
            }
        });

        try {
            /* waits for done */
            will.obtain();
        } catch (RuntimeException e) {
            assertThat(e, is(throwable));
        }
        assertTrue(results2.contains(throwable));
        
        //testWhenCompletedSuccessful
        final AtomicBoolean result = new AtomicBoolean();
        Wills.of("successful").whenDone(new Action<Boolean>() {
            @Override public void apply(Boolean aBoolean) {
                result.set(aBoolean);
            }
        });
        assertTrue(result.get());
        
        //testWhenCompletedFailed
        final AtomicBoolean result2 = new AtomicBoolean(true);
        Wills.failedWill(new RuntimeException()).whenDone(new Action<Boolean>() {
            @Override public void apply(Boolean aBoolean) {
                result2.set(aBoolean);
            }
        });
        assertFalse(result2.get());
        
        //testReplaceFailed
        final String ok = "OK";
        Will<String> okWillFallback = Wills.<String>failedWill(new RuntimeException()).replaceFailed(new FutureFallback<String>() {
            @Override
            public ListenableFuture<String> create(Throwable t) throws Exception {
                return Futures.immediateFuture(ok);
            }
        });
        assertEquals(ok, okWillFallback.obtain());


        Will<String> okWill = Wills.<String>failedWill(new RuntimeException()).replaceFailed(Wills.of(ok));
        assertEquals(ok, okWill.obtain());

        Will<String> okWillListenableFuture = Wills.<String>failedWill(new RuntimeException()).replaceFailed(Futures.immediateFuture(ok));
        assertEquals(ok, okWillListenableFuture.obtain());
    }
}
