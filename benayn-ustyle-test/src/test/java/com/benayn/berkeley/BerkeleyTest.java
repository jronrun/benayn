package com.benayn.berkeley;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.benayn.berkeley.Berkeley.BerkeleyAccess;
import com.benayn.berkeley.Berkeley.BerkeleyDB;
import com.benayn.berkeley.Berkeley.BerkeleyEnv;
import com.benayn.berkeley.Berkeley.BerkeleyIndex;
import com.benayn.berkeley.Berkeley.BerkeleyIndexCursor;
import com.benayn.berkeley.Berkeley.BerkeleyQueue;
import com.benayn.berkeley.Berkeley.BerkeleyStore;
import com.benayn.berkeley.Berkeley.DBEntry;
import com.benayn.berkeley.Berkeley.DBEntryVisit;
import com.benayn.berkeley.Berkeley.GenericKeyCreator;
import com.benayn.ustyle.Dater;
import com.benayn.ustyle.Objects2;
import com.benayn.ustyle.Pair;
import com.benayn.ustyle.Reflecter;
import com.benayn.ustyle.Scale62;
import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.google.common.base.Stopwatch;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.IndexNotAvailableException;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.model.EntityModel;
import com.sleepycat.persist.raw.RawObject;
import com.sleepycat.persist.raw.RawStore;

public class BerkeleyTest extends Assert {
    
    /**
     * 
     */
    static Log log = Loggers.from(BerkeleyTest.class);

    static String home = null;
    static BerkeleyEnv env = null;
    static BerkeleyQueue<QueueEntity> theQueue = null;
    static BerkeleyStore store = null;
    static BerkeleyDB bdb = null;
    QueueEntityAccess access = null;

    class QueueEntityAccess extends BerkeleyAccess<Long, QueueEntity> {

        public QueueEntityAccess(BerkeleyStore berkeleyStore) {
            super(berkeleyStore);
        }
        
    }
    
    @Before
    public void setup() {
        //Framewroks.enableConfigurations();
        home = "/app/data/berkeley/"; //Configurations.getProperty("database.home");
        env = newEnv("general");
        theQueue = env.queue("test-queue", QueueEntity.class);
        store = env.connection("test-store", null);
        access = new QueueEntityAccess(store);
        bdb = newEnv("bdb").connection("test-bdb", null, null);
    }
    
    @Test
    public void testTmp() {
        long num = 18;
        String num62 = Scale62.get(num, 18);
        log.info("number: " + num + ", scale 62: " + num62);
        assertEquals(num, Scale62.get(num62));
    }
    
    @Test 
    public void testBerkeleySecondaryDB() throws UnsupportedEncodingException {
        
        Stopwatch w = Stopwatch.createStarted();
        
        BerkeleyIndex fullnameIndex = bdb.openIndex("fullname", null, new GenericKeyCreator<String, Person>() {

            @Override protected String createSecondaryKey(Person data) {
                log.info(data);
                return data.getFirstName() + " " + data.getLastName();
            }
        });
        
        BerkeleyIndex birthdayIndex = bdb.openIndex("birthday", null, new GenericKeyCreator<Long, Person>() {

            @Override protected Long createSecondaryKey(Person data) {
                return data.getBirthday().getTime();
            }
        });
        
        
        int count = 20;
        intlPersonData(count);
        
        log.info("secondaryDatabase fullname name: " + fullnameIndex.getDatabaseName());
        assertEquals("fullname", fullnameIndex.getDatabaseName());
        
        log.info("secondaryDatabase fullname count: " + fullnameIndex.count());
        assertEquals(count, fullnameIndex.count());
        
        for (int i = 0; i < count; i++) {
            assertTrue(Objects2.isEqual(fullnameIndex.get(
                    "first" + i + " last" + i, Person.class), bdb.get(Long.valueOf(i + 1), Person.class)));
        }
        
        for (int i = 0; i < count; i++) {
            assertTrue(Objects2.isEqual(fullnameIndex.get(
                    "first" + i + " last" + i), bdb.get(Long.valueOf(i + 1), Person.class)));
        }
        
        bdb.put("test-other-data", 3L);
        assertEquals(3L, bdb.get("test-other-data", Long.class));
        
        //birthday index
        log.info("secondaryDatabase birthday name: " + birthdayIndex.getDatabaseName());
        assertEquals("birthday", birthdayIndex.getDatabaseName());
        
        log.info("secondaryDatabase birthday count: " + birthdayIndex.count());
        assertEquals(count, birthdayIndex.count());
        
        for (int i = 0; i < count; i++) {
            Person tmp = bdb.get(Long.valueOf(i + 1), Person.class);
            log.info(tmp.getBirthday().getTime());
            assertTrue(Objects2.isEqual(birthdayIndex.get(tmp.getBirthday().getTime()), tmp));
        }
        
        BerkeleyIndexCursor bic = birthdayIndex.opensCursor(null, null);
        
        bic.pageVisit(0, count, new DBEntryVisit<Long, Person>() {
            long c = 1;
            
            @Override protected void apply(Long key, Person data) {
                assertTrue(Objects2.isEqual(bdb.get(c, Person.class), data));
                c++;
            }
        });
        
        final int skip = 8;
        bic.pageVisit(skip, count, new DBEntryVisit<Long, Person>() {
            long c = skip + 1;
            
            @Override protected void apply(Long key, Person data) {
                log.info("asc c: " + c + ", key: " + key);
                log.info(data);
                assertTrue(Objects2.isEqual(bdb.get(c, Person.class), data));
                c++;
            }
        });
        
        final int records2 = count;
        bic.pageVisit(skip, count, new DBEntryVisit<Long, Person>(false) {
            long c = records2 - skip;
            
            @Override protected void apply(Long key, Person data) {
                log.info("desc c: " + c + ", key: " + key);
                log.info(data);
                assertTrue(Objects2.isEqual(bdb.get(c, Person.class), data));
                c--;
            }
        });
        
        final int records = count;
        bic.pageVisit(0, count, new DBEntryVisit<Long, Person>(false) {
            long c = records;
            
            @Override protected void apply(Long key, Person data) {
                assertTrue(Objects2.isEqual(bdb.get(c, Person.class), data));
                c--;
            }
        });
        
        Pair<DBEntry, DBEntry> kv = null;
        long theDescSortId = count;
        boolean firstGet = true;
        while ((kv = firstGet ? bic.getLast() : bic.getPrev()) != null) {
            if (firstGet) {
                firstGet = false;
            }
            Person ap = kv.getR().asObject(Person.class);
            log.info("key=" + kv.getL().asObject(Long.class) + ", data: " + ap);
            assertTrue(Objects2.isEqual(bdb.get(theDescSortId, Person.class), ap));
            theDescSortId--;
        }
        
        long theEscSortId = 1;
        firstGet = true;
        while ((kv = firstGet ? bic.getFirst() : bic.getNext()) != null) {
            if (firstGet) {
                firstGet = false;
            }
            Person ap = kv.getR().asObject(Person.class);
            log.info("key=" + kv.getL().asObject(Long.class) + ", data: " + ap);
            assertTrue(Objects2.isEqual(bdb.get(theEscSortId, Person.class), ap));
            theEscSortId++;
        }
        
        bic.close();
        
        //test desc order
        DatabaseEntry keyEntry = new DatabaseEntry();
        DatabaseEntry dataEntry = new DatabaseEntry();
        SecondaryCursor secCur = birthdayIndex.openCursor(null, null);
        theDescSortId = count;
        firstGet = true;
        while (firstGet 
                ? secCur.getLast(keyEntry, dataEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS 
                : secCur.getPrev(keyEntry, dataEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            if (firstGet) {
                firstGet = false;
            }
            Person ap = (Person) bdb.getBinding(Person.class).entryToObject(dataEntry);
            log.info("key=" + LongBinding.entryToLong(keyEntry) + ", data: " + ap);
            assertTrue(Objects2.isEqual(bdb.get(theDescSortId, Person.class), ap));
            theDescSortId--;
        }
        
        //test esc order
        theEscSortId = 1;
        firstGet = true;
        while (firstGet 
                ? secCur.getFirst(keyEntry, dataEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS 
                : secCur.getNext(keyEntry, dataEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            if (firstGet) {
                firstGet = false;
            }
            Person ap = (Person) bdb.getBinding(Person.class).entryToObject(dataEntry);
            log.info("key=" + LongBinding.entryToLong(keyEntry) + ", data: " + ap);
            assertTrue(Objects2.isEqual(bdb.get(theEscSortId, Person.class), ap));
            theEscSortId++;
        }
        
        secCur.close();
        
        log.info(w.elapsed(TimeUnit.MILLISECONDS));
    }
    
    void intlPersonData(int count) {
        for (int i = 0; i < count; i++) {
            Person p = new Person();
            p.setId(Long.valueOf(i + 1));
            p.setAddress("address" + i);
            p.setFirstName("first" + i);
            p.setLastName("last" + i);
            p.setBirthday(Dater.now().add().year(-1).add().month(i).get());
            bdb.put(Long.valueOf(i + 1), p);
        }
    }
    
    public void testDump() {
        RawStore rs = access.getRawStore();
        
        EntityModel model = rs.getModel();
        
        for (String clsName : model.getKnownClasses()) {
            if (model.getEntityMetadata(clsName) != null) {
                final PrimaryIndex<Object,RawObject> index;
                try {
                    index = rs.getPrimaryIndex(clsName);
                } catch (IndexNotAvailableException e) {
                    System.err.println("Skipping primary index that is " +
                            "not yet available: " + clsName);
                    continue;
                }
                EntityCursor<RawObject> entities = index.entities();
                for (RawObject entity : entities) {
                    System.out.println(entity);
                }
                entities.close();
            }
        }
    }
    
    @Test
    public void tests() throws Exception {
        //fillData();
        
        log.info(access.getEntityStore().getStoreName() + " has " + access.count() + " records.");
        
        QueueEntity qe = access.get(1001L);
        if (null == qe) {
            fillData();
            qe = access.get(1001L);
        }
        assertNotNull(qe);
        
        QueueEntity qe2 = access.join("name", qe.getName()).join("address", qe.getAddress()).join("date", qe.getDate()).get();
        assertNotNull(qe2);
        
        log.info(qe);
        log.info(qe2);
        assertTrue(Objects2.isEqual(qe, qe2));
        
        QueueEntity qe3 = access.get(88888L);
        QueueEntity qe4 = Reflecter.from(qe3).clones();
        
        log.info(qe3);
        log.info(qe4);
        assertTrue(Objects2.isEqual(qe3, qe4));
        
        qe4.setName(qe4.getName() + "update");
        qe4.setAddress(qe4.getAddress() + "update");
        access.update(qe4);
        log.info(access.get(88888L));
        assertFalse(Objects2.isEqual(qe3, access.get(88888L)));
        
        List<QueueEntity> list = access.gets(1001L, 1002L, 1003L, 1004L, 1005L, 1006L);
        log.info(list);
        assertEquals(6, list.size());
        for (QueueEntity item : list) {
            assertTrue(access.contains(item.getId()));
        }
        
        List<QueueEntity> list2 = access.findRange(20000L, true, 20009L, true);
        log.info(list2);
        assertEquals(10, list2.size());
        
        QueueEntity qe5 = access.get(20001L);
        assertTrue(access.delete(20001L));
        assertNull(access.get(20001L));
        
        List<QueueEntity> list3 = access.findRange(20000L, true, 20009L, true);
        log.info(list3);
        assertEquals(9, list3.size());
        access.save(qe5);

        List<QueueEntity> list4 = access.findRange(20000L, true, 20009L, true);
        log.info(list4);
        assertEquals(10, list4.size());
        
        List<QueueEntity> list5 = access.findPrefix("address", "addr5979");
        log.info(list5);
        assertTrue(list5.size() >= 10);
        
        List<QueueEntity> list6 = access.findRange("name", "name0", true, "name10", true);
        log.info(list6);
        assertTrue(list6.size() > 0);
        
        Date begin = access.get(1001L).getDate();
        Date end = Dater.of(begin).add().millisecond(10).get();
        List<QueueEntity> list7 = access.findRange("date", begin, true, end, true);
        log.info(list7);
        assertTrue(list7.size() > 0);
        log.info(Dater.of(begin).asText());
        log.info(Dater.of(end).asText());
    }
    
    public void fillData() throws InterruptedException {
        Stopwatch w = Stopwatch.createStarted();
        
        int threadNum = 10;
        int recordEachThread = 10000;
        CountDownLatch runningThreadNum = new CountDownLatch(threadNum);
        System.out.println(Thread.currentThread().getName()+"-start");
        
        //创建多个子线程
        for (int i = 0; i < threadNum; i++) {
            new SubThread(runningThreadNum, i, recordEachThread).start();
        }
        
        //等待子线程都执行完了再执行主线程剩下的动作
        runningThreadNum.await();
        System.out.println(Thread.currentThread().getName()+"-end " + w.elapsed(TimeUnit.MILLISECONDS) + " ms");
    }
    
    public class SubThread extends Thread{
        
        //子线程记数器,记载着运行的线程数
        private CountDownLatch runningThreadNum;
        private int threadNum;
        private int recordEachThread;
     
        public SubThread(CountDownLatch runningThreadNum, int threadNum, int recordEachThread){
            this.threadNum = threadNum;
            this.runningThreadNum = runningThreadNum;
            this.recordEachThread = recordEachThread;
        }
         
        @Override
        public void run() {
            Stopwatch w = Stopwatch.createStarted();
            System.out.println(Thread.currentThread().getName()+"-start");
            
            for (int i = threadNum * recordEachThread; i < (threadNum * recordEachThread + recordEachThread); i++) {
              QueueEntity qe = new QueueEntity();
              qe.setId(1000 + access.autoincreaseID());
              qe.setDate(new Date());
              qe.setName("name" + i);
              qe.setAddress("addr" + i);
              access.save(qe);
          }
            
            System.out.println(Thread.currentThread().getName()+"-end " + w.elapsed(TimeUnit.MILLISECONDS) + " ms");
            runningThreadNum.countDown();//正在运行的线程数减一
        }
    }
    
    private BerkeleyEnv newEnv(String envDir) {
        File f = new File(home + envDir + "/");
        if (!f.exists()) {
            f.mkdir();
        }
        return Berkeley.env(f);
    }
    
    @Test
    public void testMultiThreadedPush() throws Throwable {
        final BerkeleyQueue<String> queue = newEnv("multipush").queue("multipush-queue", String.class, 1);
        try {
            int threadCount = 20;

            final CountDownLatch startLatch = new CountDownLatch(threadCount);
            final CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                new Thread(Integer.toString(i)) {
                    public void run() {
                        try {
                            startLatch.countDown();
                            startLatch.await();

                            queue.add(getName());
                            latch.countDown();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }

            latch.await(5, TimeUnit.SECONDS);

            assert queue.size() == threadCount;
        } finally {
            queue.close();
        }
    }
    
    @Test @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testMultiThreadedPoll() throws Throwable {
        final BerkeleyQueue<String> queue = newEnv("multipoll").queue("multipoll-queue", String.class, 1);
        try {
            int threadCount = 20;
            for (int i = 0; i < threadCount; i++)
                queue.add(Integer.toString(i));

            final Set set = Collections.synchronizedSet(new HashSet());
            final CountDownLatch startLatch = new CountDownLatch(threadCount);
            final CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                new Thread() {
                    public void run() {
                        try {
                            startLatch.countDown();
                            startLatch.await();

                            String val = queue.poll();
                            if (val != null) {
                                set.add(val);
                            }
                            latch.countDown();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }

            latch.await(5, TimeUnit.SECONDS);

            assert set.size() == threadCount;
        } finally {
            queue.close();
        }
    }
    
    @Test
    public void testQueueSurviveReopen() throws Throwable {
        BerkeleyQueue<String> queue = newEnv("survive").queue("survive-queue", String.class, 3);
        try {
            queue.add("5");
        } finally {
            queue.close();
        }

        queue = newEnv("survive").queue("survive-queue", String.class, 3);
        try {
            String head = queue.poll();

            assert head.equals("5");
        } finally {
            queue.close();
        }
    }
    
    @Test
    public void testQueuesOrder() throws IOException {
        int count = 100000;
        int cacheSize = 1000;
        
        BerkeleyQueue<String> manualQueue = newEnv("manual").queue("manual-queue", String.class, cacheSize);
        
        Stopwatch w1 = Stopwatch.createStarted();
        for (int i = 0; i < count; i++) {
            manualQueue.add(Integer.toString(i));
        }

        System.out.println(manualQueue.size());
        manualQueue.close();
        
        manualQueue = newEnv("manual").queue("manual-queue", String.class, cacheSize);
        
        for (int i = 0; i < count; i++) {
            String element = manualQueue.poll();
            if (!Integer.toString(i).equals(element)) {
                throw new AssertionError("Expected element " + i + ", but got " + element);
            }
        }
        
        System.out.println(manualQueue.size());
        manualQueue.close();
        System.out.println(w1.elapsed(TimeUnit.MILLISECONDS) + " ms");
        
      //11472 ms 305922 ms
//        File queueDir = TestUtils.createTempSubdir("test-queue");
//        final TheQueue queue = new TheQueue(queueDir.getPath(), "test-queue", count);
//        Stopwatch w2 = Stopwatch.createStarted();
//        for (int i = 0; i < count; i++) {
//            queue.push(Integer.toString(i));
//        }
//
//        for (int i = 0; i < count; i++) {
//            String element = queue.poll();
//            if (!Integer.toString(i).equals(element)) {
//                throw new AssertionError("Expected element " + i + ", but got " + element);
//            }
//        }
//        System.out.println(w2.elapsed(TimeUnit.MILLISECONDS) + " ms");
    }

}
