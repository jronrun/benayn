/**
 * Copyright (c) Zhejiang WANLIJOJU Automation Technology Co., Ltd All rights reserved.
 */
package com.benayn.berkeley;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.sleepycat.bind.tuple.TupleBinding.getPrimitiveBinding;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.benayn.ustyle.Pair;
import com.benayn.ustyle.TypeRefer;
import com.benayn.ustyle.TypeRefer.TypeDescrib;
import com.benayn.ustyle.behavior.StructBehaviorAdapter;
import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.benayn.ustyle.string.Strs;
import com.google.common.base.Optional;
import com.google.common.collect.ForwardingObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import com.google.common.primitives.Primitives;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.collections.StoredSortedMap;
import com.sleepycat.je.CacheMode;
import com.sleepycat.je.CheckpointConfig;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DatabaseExistsException;
import com.sleepycat.je.DatabaseNotFoundException;
import com.sleepycat.je.DatabaseStats;
import com.sleepycat.je.DeleteConstraintException;
import com.sleepycat.je.DiskOrderedCursor;
import com.sleepycat.je.DiskOrderedCursorConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentMutableConfig;
import com.sleepycat.je.EnvironmentStats;
import com.sleepycat.je.JoinConfig;
import com.sleepycat.je.JoinCursor;
import com.sleepycat.je.LockConflictException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.PreloadConfig;
import com.sleepycat.je.PreloadStats;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import com.sleepycat.je.Sequence;
import com.sleepycat.je.SequenceConfig;
import com.sleepycat.je.SequenceExistsException;
import com.sleepycat.je.SequenceNotFoundException;
import com.sleepycat.je.StatsConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;
import com.sleepycat.je.TransactionStats;
import com.sleepycat.je.VerifyConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.EntityJoin;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.ForwardCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.evolve.EvolveConfig;
import com.sleepycat.persist.evolve.EvolveStats;
import com.sleepycat.persist.evolve.Mutations;
import com.sleepycat.persist.impl.Store;
import com.sleepycat.persist.model.EntityModel;
import com.sleepycat.persist.model.SecondaryKey;
import com.sleepycat.persist.raw.RawStore;

/**
 * @ClassName: Berkeley
 * @Description: 
 * @author: paulo.ye
 * 
 */
public abstract class Berkeley<M> extends ForwardingObject {
    
    /**
     * 
     */
    protected static final Log log = Loggers.from(Berkeley.class);
    
    /**
     * Returns a new {@link BerkeleyDB} instance, {@link Environment} with {@link Berkeley#defaultEnvironmentConfig() 
     * and {@link Database} with {@link Berkeley#defaultDatabaseConfig()}
     */
    public static BerkeleyDB from(String envHomePath, String databaseName) {
        return env(envHomePath).connection(databaseName, null, null);
    }
    
    /**
     * Returns a new {@link BerkeleyStore} instance, {@link Environment} with {@link Berkeley#defaultEnvironmentConfig() 
     * and {@link EntityStore} with {@link Berkeley#defaultStoreConfig()}
     */
    public static BerkeleyStore of(String envHomePath, String storeName) {
        return env(envHomePath).connection(storeName, null);
    }
    
    /**
     * Returns a new {@link BerkeleyQueue} instance, default is cache all
     * @see AbstractQueue
     */
    public static <E extends Serializable> BerkeleyQueue<E> queue(String envHomePath, String queueName, Class<E> valueClass) {
        return queue(envHomePath, queueName, valueClass, -1);
    }
    
    /**
     * Returns a new {@link BerkeleyQueue} instance with given cache size
     * @param cacheSize Queue cache size - number of element operations it is allowed to loose in case of system crash.
     * @see AbstractQueue
     */
    public static <E extends Serializable> BerkeleyQueue<E> queue(String envHomePath, String queueName, Class<E> valueClass, int cacheSize) {
        return env(envHomePath).queue(queueName, valueClass, cacheSize);
    }
    
    /**
     * Returns a new {@link BerkeleyEnv} instance with given path and default configuration
     * @see Berkeley#defaultEnvironmentConfig()
     */
    public static BerkeleyEnv env(String envHomePath) {
        return env(envHomePath, null);
    }
    
    /**
     * Returns a new {@link BerkeleyEnv} instance with given file and default configuration
     * @see Berkeley#defaultEnvironmentConfig()
     */
    public static BerkeleyEnv env(File envHome) {
        return env(envHome, null);
    }
    
    /**
     * Returns a new {@link BerkeleyEnv} instance with given path and configuration or default configuration if null
     * @see Berkeley#defaultEnvironmentConfig()
     */
    public static BerkeleyEnv env(String envHomePath, EnvironmentConfig environmentConfig) {
        return env(new File(checkNotNull(envHomePath, "The berkeley home path cannot be null.")), environmentConfig);
    }
    
    /**
     * Returns a new {@link BerkeleyEnv} instance with given file and configuration or default configuration if null
     * @see Berkeley#defaultEnvironmentConfig()
     */
    public static BerkeleyEnv env(File envHome, EnvironmentConfig environmentConfig) {
        return new BerkeleyEnv(envHome, environmentConfig);
    }
    
    /**
     * Returns the sub instance of the {@link Berkeley}
     * 
     * @return
     */
    protected abstract M THIS();
    
    /**
     * Lightweight Fast Persistent Queue Using Berkeley DB JE
     * @see Queue
     * @see AbstractQueue
     */
    public static abstract class BerkeleyQueue<E> extends AbstractQueue<E> {
        
        /**
         * Flushes any cached information for this {@link BerkeleyQueue} to database
         * @see Database#sync()
         */
        public abstract void sync();
        
        /**
         * Flushes any cached information for this {@link BerkeleyQueue} to database and Discards the database handle
         * @see Database#sync()
         * @see Database#close()
         */
        public abstract void close();
        
        /**
         * Returns the {@link BerkeleyDB} instance for this {@link BerkeleyQueue} 
         * @return
         */
        public abstract BerkeleyDB getQueueDB();
        
        /**
         * Returns the {@link StoredMap} instance for this {@link BerkeleyQueue}
         * @return
         */
        public abstract StoredMap<Long, E> getQueueMap();
        
    }
    
    /**
     * 
     */
    private static class BindingBehavior extends StructBehaviorAdapter<EntryBinding<?>> {
        
        public BindingBehavior(Object delegate) {
            super(delegate);
        }
        
        public BindingBehavior update(Object delegate) {
            this.delegate = delegate;
            return this;
        }

        @Override protected EntryBinding<?> noneMatched() {
            if (this.clazz == String.class) {
                return defaultBehavior();
            }
            
            return null;
        }

        @Override protected EntryBinding<?> defaultBehavior() {
            return (EntryBinding<?>) getPrimitiveBinding(Primitives.wrap(this.clazz));
        }
        
    }
    
    /**
     * 
     */
    public static class EntityJoinQuery<PK, E> extends EntityJoin<PK, E> {
        
        public EntityJoinQuery(PrimaryIndex<PK, E> index) {
            super(index);
        }
        
        public <SK> EntityJoinQuery<PK, E> join(SecondaryIndex<SK, PK, E> index, SK key) {
            if (null != index && null != key) {
                addCondition(index, key);
            }
            return this;
        }
        
    }
    
    /**
     * 
     */
    public static class DefaultBerkeleyQueue<E extends Serializable> extends BerkeleyQueue<E> implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 4578701790841592245L;
        
        public DefaultBerkeleyQueue(BerkeleyEnv berkeleyEnv) {
            headIndex = new AtomicLong(0);
            tailIndex = new AtomicLong(0);
            this.berkeleyEnv = checkNotNull(berkeleyEnv, "Berkeley environment cannot be null.");
        }
        
        /**
         * Returns a new {@link BerkeleyQueue} with given parameters
         */
        public BerkeleyQueue<E> connection(String queueName, Class<E> valueClass, int cacheSize) {
            DatabaseConfig databaseConfig = 
                    defaultDatabaseConfig()
                    .setTransactional(false)
                    .setDeferredWrite(true);
            
            queueDB = berkeleyEnv.connection(queueName, databaseConfig, null);
            
            theCacheSize = cacheSize;
            headIdxEntry = queueDB.getEntry(headIdx, getPrimitiveBinding(String.class));
            tailIdxEntry = queueDB.getEntry(tailIdx, getPrimitiveBinding(String.class));
            
            Long tmpHeadIndex = queueDB.get(headIdx, headIdxValBinding = getPrimitiveBinding(Long.class));
            Long tmpTailIndex = queueDB.get(tailIdx, tailIdxValBinding = getPrimitiveBinding(Long.class));
            if (null != tmpHeadIndex && null != tmpTailIndex) {
                headIndex = new AtomicLong(tmpHeadIndex);
                tailIndex = new AtomicLong(tmpTailIndex);
            }
            
            checkNotNull(valueClass, "The queue value class cannot be null.");
            queueMap = queueDB.getStoredSortedMap(
                    getPrimitiveBinding(Long.class), queueDB.getBinding(valueClass), true);
            
            return this;
        }
        
        @Override public boolean offer(E e) {
            synchronized (tailIndex) {
                queueMap.put(tailIndex.getAndIncrement(), e); // from tail insert
            }
            return true;
        }

        @Override public E poll() {
            synchronized (headIndex) {
                E headItem = peek();
                if (headItem != null) {
                    queueMap.remove(headIndex.getAndIncrement());
                    peekItem = null;
                    cacheCheck();
                    return headItem;
                }
            }
            return null;
        }

        @Override public E peek() {
            synchronized (headIndex) {
                if (peekItem != null) {
                    return peekItem;
                }
                E headItem = null;
                while (headItem == null && headIndex.get() < tailIndex.get()) { //in range
                    headItem = queueMap.get(headIndex.get());
                    if (headItem != null) {
                        peekItem = headItem;
                        continue;
                    }
                    headIndex.incrementAndGet(); //head pointer backward
                }
                cacheCheck();
                return headItem;
            }
        }

        @Override public Iterator<E> iterator() {
            return queueMap.values().iterator();
        }

        @Override public int size() {
            synchronized (tailIndex) {
                synchronized (headIndex) {
                    return (int) (tailIndex.get() - headIndex.get());
                }
            }
        }
        
        @Override public void sync() {
            if (null != queueDB) {
                synchronized (tailIndex) {
                    synchronized (headIndex) {
                        queueDB.put(null, headIdxEntry, queueDB.getEntry(headIndex.get(), headIdxValBinding));
                        queueDB.put(null, tailIdxEntry, queueDB.getEntry(tailIndex.get(), tailIdxValBinding));
                        queueDB.sync();
                    }
                }
            }
        }
        
        private void cacheCheck() {
            opsCounter++;
            if (theCacheSize >= 0 && opsCounter >= theCacheSize) {
                sync();
                opsCounter = 0;
            }
        }
        
        @Override public void close() {
            try {
                if (null != queueDB) {
                    sync();
                    queueDB.close();
                }
            } catch (DatabaseException e) {
                e.printStackTrace();
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
            }
        }
        
        @Override public void clear() {
            try {
                close();
                if (null != queueDB) {
                    queueDB.getEnvironment().removeDatabase(null, queueDB.getDatabaseName());
                }
            } catch (DatabaseNotFoundException e) {
                e.printStackTrace();
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
        
        @Override public BerkeleyDB getQueueDB() {
            return queueDB;
        }
        
        @Override public StoredMap<Long, E> getQueueMap() {
            return queueMap;
        }
        
        // The head pointer
        private AtomicLong headIndex;
        // The tail pointer
        private AtomicLong tailIndex;
        // Current value
        private transient E peekItem = null;
        // Persist map, Key is pointer position, Value is value, no need serial
        private transient StoredMap<Long, E> queueMap = null;
        private BerkeleyDB queueDB = null;
        private BerkeleyEnv berkeleyEnv = null;
        private String headIdx = "headIdx", tailIdx = "tailIdx";
        
        private DatabaseEntry headIdxEntry = null, tailIdxEntry = null;
        private TupleBinding<Long> headIdxValBinding = null, tailIdxValBinding = null;
        //Queue operation counter, which is used to sync the queue database to disk periodically.
        private int opsCounter, theCacheSize;

    }
    
    /**
     * Default {@link SequenceConfig}, {allowCreate: true, initialValue: 1}
     */
    public static SequenceConfig defaultSequenceConfig() {
        return new SequenceConfig()
            .setAllowCreate(true)
            .setInitialValue(1L);
    }
    
    /**
     * Default {@link StoreConfig}, {allowCreate: true, transactional: true}
     */
    public static StoreConfig defaultStoreConfig() {
        return new StoreConfig()
            .setAllowCreate(true)
            .setTransactional(true);
    }
    
    /**
     * Default {@link EnvironmentConfig}, {allowCreate: true, transactional: true}
     */
    public static EnvironmentConfig defaultEnvironmentConfig() {
        return new EnvironmentConfig()
            .setAllowCreate(true)
            .setTransactional(true);
    }
    
    /**
     * Default {@link DatabaseConfig}, {allowCreate: true, transactional: true}
     */
    public static DatabaseConfig defaultDatabaseConfig() {
        return new DatabaseConfig()
            .setAllowCreate(true)
            .setTransactional(true);
    }
    
    /**
     * Default {@link SecondaryConfig}, {allowCreate: true, sortedDuplicates: true} with given {@link SecondaryKeyCreator}
     * @param keyCreator
     * @return
     */
    public static SecondaryConfig defaultSecondaryConfig(SecondaryKeyCreator keyCreator) {
        SecondaryConfig cfg = new SecondaryConfig();
        cfg.setAllowCreate(true);
        //Duplicates are frequently required for secondary databases
        cfg.setSortedDuplicates(true);
        cfg.setTransactional(true);
        cfg.setKeyCreator(checkNotNull(keyCreator, "SecondaryKeyCreator cannot be null"));
        return cfg;
    }
    
    /**
     * @see Environment
     */
    public static class BerkeleyEnv extends ForwardingObject {

        /**
         * @see Environment#close()
         */
        public synchronized void close() throws DatabaseException {
            delegate().close();
        }

        /**
         * @see Environment#openDatabase(Transaction, String, DatabaseConfig)
         */
        public synchronized Database openDatabase(Transaction txn, String databaseName, DatabaseConfig dbConfig)
                throws DatabaseNotFoundException, DatabaseExistsException, IllegalArgumentException, IllegalStateException {
            return delegate().openDatabase(txn, databaseName, dbConfig);
        }

        /**
         * @see Environment#openSecondaryDatabase(Transaction, String, Database, SecondaryConfig)
         */
        public synchronized SecondaryDatabase openSecondaryDatabase(Transaction txn, String databaseName, 
                Database primaryDatabase, SecondaryConfig dbConfig) throws DatabaseNotFoundException, DatabaseExistsException,
                DatabaseException, IllegalArgumentException, IllegalStateException {
            return delegate().openSecondaryDatabase(txn, databaseName, primaryDatabase, dbConfig);
        }

        /**
         * @see Environment#removeDatabase(Transaction, String)
         */
        public void removeDatabase(final Transaction txn, final String databaseName) throws DatabaseNotFoundException {
            delegate().removeDatabase(txn, databaseName);
        }

        /**
         * @see Environment#renameDatabase(Transaction, String, String)
         */
        public void renameDatabase(final Transaction txn, 
                final String databaseName, final String newName) throws DatabaseNotFoundException {
            delegate().renameDatabase(txn, databaseName, newName);
        }

        /**
         * @see Environment#truncateDatabase(Transaction, String, boolean)
         */
        public long truncateDatabase(final Transaction txn, 
                final String databaseName, final boolean returnCount) throws DatabaseNotFoundException {
            return delegate().truncateDatabase(txn, databaseName, returnCount);
        }

        /**
         * @see Environment#getHome()
         */
        public File getHome() throws DatabaseException {
            return delegate().getHome();
        }

        /**
         * @see Environment#beginTransaction(Transaction, TransactionConfig)
         */
        public Transaction beginTransaction(Transaction parent, TransactionConfig txnConfig) 
                throws DatabaseException, IllegalArgumentException {
            return delegate().beginTransaction(parent, txnConfig);
        }

        /**
         * @see Environment#checkpoint(CheckpointConfig)
         */
        public void checkpoint(CheckpointConfig ckptConfig) throws DatabaseException {
            delegate().checkpoint(ckptConfig);
        }

        /**
         * @see Environment#sync()
         */
        public void sync() throws DatabaseException {
            delegate().sync();
        }

        /**
         * @see Environment#flushLog(boolean)
         */
        public void flushLog(boolean fsync) {
            delegate().flushLog(fsync);
        }

        /**
         * @see Environment#cleanLog()
         */
        public int cleanLog() throws DatabaseException {
            return delegate().cleanLog();
        }

        /**
         * @see Environment#evictMemory()
         */
        public void evictMemory() throws DatabaseException {
            delegate().evictMemory();
        }

        /**
         * @see Environment#compress()
         */
        public void compress() throws DatabaseException {
            delegate().compress();
        }

        /**
         * @see Environment#preload(Database[], PreloadConfig)
         */
        public PreloadStats preload(final Database[] databases, final PreloadConfig config) throws DatabaseException {
            return delegate().preload(databases, config);
        }

        /**
         * @see Environment#getConfig()
         */
        public EnvironmentConfig getConfig() throws DatabaseException {
            return delegate().getConfig();
        }

        /**
         * @see Environment#setMutableConfig(EnvironmentMutableConfig)
         */
        public synchronized void setMutableConfig( EnvironmentMutableConfig mutableConfig) throws DatabaseException {
            delegate().setMutableConfig(mutableConfig);
        }

        /**
         * @see Environment#getMutableConfig()
         */
        public EnvironmentMutableConfig getMutableConfig() throws DatabaseException {
            return delegate().getMutableConfig();
        }

        /**
         * @see Environment#getStats(StatsConfig)
         */
        public EnvironmentStats getStats(StatsConfig config) throws DatabaseException {
            return delegate().getStats(config);
        }

        /**
         * @see Environment#getTransactionStats(StatsConfig)
         */
        public TransactionStats getTransactionStats(StatsConfig config) throws DatabaseException {
            return delegate().getTransactionStats(config);
        }

        /**
         * @see Environment#getDatabaseNames()
         */
        public List<String> getDatabaseNames() throws DatabaseException {
            return delegate().getDatabaseNames();
        }

        /**
         * @see Environment#verify(VerifyConfig, PrintStream)
         */
        public boolean verify(VerifyConfig config, PrintStream out) throws DatabaseException {
            return delegate().verify(config, out);
        }

        /**
         * @see Environment#getThreadTransaction()
         */
        public Transaction getThreadTransaction() throws DatabaseException {
            return delegate().getThreadTransaction();
        }

        /**
         * @see Environment#setThreadTransaction(Transaction)
         */
        public void setThreadTransaction(Transaction txn) {
            delegate().setThreadTransaction(txn);
        }

        /**
         * @see Environment#isValid()
         */
        public boolean isValid() {
            return delegate().isValid();
        }

        /**
         * @see Environment#printStartupInfo(PrintStream)
         */
        public void printStartupInfo(PrintStream out) {
            delegate().printStartupInfo(out);
        }

        /**
         * @see Environment#checkHandleIsValid()
         */
        public void checkHandleIsValid() throws DatabaseException {
            delegate().checkHandleIsValid();
        }
        
        public Environment getEnvironment() {
            return environment;
        }
        
        /**
         * @see BerkeleyStore#connection(String, StoreConfig)
         */
        public BerkeleyStore connection(String storeName, StoreConfig storeConfig) {
            return new BerkeleyStore(this).connection(storeName, storeConfig);
        }
        
        /**
         * @see BerkeleyDB#connection(String, DatabaseConfig, Transaction)
         */
        public BerkeleyDB connection(String databaseName, DatabaseConfig databaseConfig, Transaction transaction) {
            return new BerkeleyDB(this).connection(databaseName, databaseConfig, transaction);
        }
        
        /**
         * @see DefaultBerkeleyQueue#connection(String, Class, Long)
         */
        public <E extends Serializable> BerkeleyQueue<E> queue(String queueName, Class<E> valueClass) {
            return queue(queueName, valueClass, -1);
        }
        
        /**
         * @see DefaultBerkeleyQueue#connection(String, Class, Long)
         */
        public <E extends Serializable> BerkeleyQueue<E> queue(String queueName, Class<E> valueClass, int cacheSize) {
            return new DefaultBerkeleyQueue<E>(this).connection(queueName, valueClass, cacheSize);
        }
        
        protected BerkeleyEnv(File envHome, EnvironmentConfig environmentConfig) {
            checkNotNull(envHome, "The berkeley home cannot be null.");
            checkArgument(envHome.exists(), "The berkeley home not exists. %s", envHome.getPath());
            checkArgument(envHome.canRead(), "The berkeley home cannot read. %s", envHome.getPath());
            checkArgument(envHome.canWrite(), "The berkeley home cannot write. %s", envHome.getPath());
            
            this.environment = new Environment(envHome, 
                    environmentConfig = firstNonNull(environmentConfig, defaultEnvironmentConfig()));
            
            if (log.isDebugEnabled()) {
                log.debug("Environment initialized with home path: " + getHome().getPath());
                log.debug("EnvironmentConfig initialized: " + environmentConfig.toString());
            }
        }

        @Override protected Environment delegate() {
            return checkNotNull(environment, "Please initialize environment first.");
        }
        
        private Environment environment = null; 
        
    }
    
    /**
     * @see EntityStore
     */
    public static class BerkeleyStore extends Berkeley<BerkeleyStore> {
        
        public BerkeleyStore(BerkeleyEnv berkeleyEnv) {
            super(berkeleyEnv);
        }

        /**
         * Returns a {@link BerkeleyStore} instance with given parameter
         */
        public BerkeleyStore connection(String storeName) {
            return connection(storeName, null);
        }
        
        /**
         * Returns a {@link BerkeleyStore} instance with given parameter
         */
        public BerkeleyStore connection(String storeName, StoreConfig storeConfig) {
            _store = new EntityStore(getEnvironment(), 
                    checkNotNull(storeName), storeConfig = firstNonNull(storeConfig, defaultStoreConfig()));
            if (log.isDebugEnabled()) {
                log.debug("EntityStore initialized with name: " + storeName);
            }
            return this;
        }
        
        /**
         * Do prefix query, similar to the SQL statement:
         * <blockquote><pre>
         * SELECT * FROM table WHERE col LIKE 'prefix%';
         * </pre></blockquote>
         */
        public <V> EntityCursor<V> query(EntityIndex<String, V> index, String prefix) {
            checkArgument(!Strs.isBlank(prefix));
            
            char[] ca = prefix.toCharArray();
            final int lastCharIndex = ca.length - 1;
            ca[lastCharIndex]++;
            return query(index, prefix, true, String.valueOf(ca), false);
        }
        
        /**
         * Do a "AND" join on a single primary database, similar to the SQL:
         * <blockquote><pre>
         * SELECT * FROM table WHERE col1 = key1 AND col2 = key2;
         * </pre></blockquote>
         */
        public <PK, SK1, SK2, E> ForwardCursor<E> query(PrimaryIndex<PK, E> pk,
                                SecondaryIndex<SK1, PK, E> sk1, SK1 key1,
                                SecondaryIndex<SK2, PK, E> sk2, SK2 key2) {
            return query(pk, sk1, key1, sk2, key2, null, null);
        }
        
        /**
         * Do a "AND" join on a single primary database, similar to the SQL:
         * <blockquote><pre>
         * SELECT * FROM table WHERE col1 = key1 AND col2 = key2 AND col3 = key3;
         * </pre></blockquote>
         */
        public <PK, SK1, SK2, SK3, E> ForwardCursor<E> query(PrimaryIndex<PK, E> pk,
                                SecondaryIndex<SK1, PK, E> sk1, SK1 key1,
                                SecondaryIndex<SK2, PK, E> sk2, SK2 key2,
                                SecondaryIndex<SK3, PK, E> sk3, SK3 key3) {
            return join(pk).join(sk1, key1).join(sk2, key2).join(sk3, key3).entities();
        }
        
        /**
         * Do a "AND" join on a single primary database, similar to the SQL:
         * <blockquote><pre>
         * SELECT * FROM table WHERE col1 = key1 AND ... AND colN = keyN;
         * </pre></blockquote>
         */
        public <PK, E> EntityJoinQuery<PK, E> join(PrimaryIndex<PK, E> pk) {
            return new EntityJoinQuery<PK, E>(checkNotNull(pk));
        }

        /**
         * Do range query, similar to the SQL statement:
         * <blockquote><pre>
         * SELECT * FROM table WHERE col >= fromKey AND col <= toKey;
         * </pre></blockquote>
         */
        public <K, V> EntityCursor<V> query(
                EntityIndex<K, V> index, K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            /* Opens a cursor for traversing entities in a key range. */
            return checkNotNull(index).entities(fromKey, fromInclusive, toKey, toInclusive);
        }
        
        /**
         * @see EntityStore#getConfig()
         */
        public StoreConfig getConfig() {
            return delegate().getConfig();
        }

        /**
         * @see EntityStore#getStoreName()
         */
        public String getStoreName() {
            return delegate().getStoreName();
        }

        /**
         * @see EntityStore#getStoreNames(Environment)
         */
        public static Set<String> getStoreNames(Environment env) throws DatabaseException {
            return Store.getStoreNames(env);
        }
        
        /**
         * @see EntityStore#isReplicaUpgradeMode()
         */
        public boolean isReplicaUpgradeMode() {
            return delegate().isReplicaUpgradeMode();
        }

        /**
         * @see EntityStore#getModel()
         */
        public EntityModel getModel() {
            return delegate().getModel();
        }

        /**
         * @see EntityStore#getMutations()
         */
        public Mutations getMutations() {
            return delegate().getMutations();
        }

        /**
         * @see EntityStore#getPrimaryIndex(Class, Class)
         */
        public <PK, E> PrimaryIndex<PK, E> getPrimaryIndex(
                Class<PK> primaryKeyClass, Class<E> entityClass) throws DatabaseException {
            return delegate().getPrimaryIndex(primaryKeyClass, entityClass);
        }

        /**
         * @see EntityStore#getSecondaryIndex(PrimaryIndex, Class, String)
         */
        public <SK, PK, E> SecondaryIndex<SK, PK, E> getSecondaryIndex(
                PrimaryIndex<PK, E> primaryIndex, Class<SK> keyClass, String keyName) throws DatabaseException {
            return delegate().getSecondaryIndex(primaryIndex, keyClass, keyName);
        }

        /**
         * @see EntityStore#getSubclassIndex(PrimaryIndex, Class, Class, String)
         */
        public <SK, PK, E1, E2 extends E1> SecondaryIndex<SK, PK, E2>
            getSubclassIndex(PrimaryIndex<PK, E1> primaryIndex,
                             Class<E2> entitySubclass, Class<SK> keyClass,
                             String keyName) throws DatabaseException {
            return delegate().getSubclassIndex(primaryIndex, entitySubclass, keyClass, keyName);
        }

        /**
         * @see EntityStore#evolve(EvolveConfig)
         */
        public EvolveStats evolve(EvolveConfig config) throws DatabaseException {
            return delegate().evolve(config);
        }

        /**
         * @see EntityStore#truncateClass(Class)
         */
        public void truncateClass(Class<?> entityClass) throws DatabaseException {
            delegate().truncateClass(entityClass);
        }

        /**
         * @see EntityStore#truncateClass(Transaction, Class)
         */
        public void truncateClass(Transaction txn, Class<?> entityClass) throws DatabaseException {
            delegate().truncateClass(txn, entityClass);
        }

        /**
         * @see EntityStore#sync()
         */
        public void sync() throws DatabaseException {
            delegate().sync();
        }
        
        /**
         * @see EntityStore#closeClass(Class)
         */
        public void closeClass(Class<?> entityClass) throws DatabaseException {
            delegate().closeClass(entityClass);
        }

        /**
         * @see EntityStore#close()
         */
        public void close() throws DatabaseException {
            delegate().close();
        }

        /**
         * @see EntityStore#getSequence(String) 
         */
        public Sequence getSequence(String name) throws DatabaseException {
            return delegate().getSequence(name);
        }

        /**
         * @see EntityStore#getSequenceConfig(String)
         */
        public SequenceConfig getSequenceConfig(String name) {
            return delegate().getSequenceConfig(name);
        }

        /**
         * @see EntityStore#setSequenceConfig(String, SequenceConfig)
         */
        public void setSequenceConfig(String name, SequenceConfig config) {
            delegate().setSequenceConfig(name, config);
        }

        /**
         * @see EntityStore#getPrimaryConfig(Class)
         */
        public DatabaseConfig getPrimaryConfig(Class<?> entityClass) {
            return delegate().getPrimaryConfig(entityClass);
        }

        /**
         * @see EntityStore#setPrimaryConfig(Class, DatabaseConfig)
         */
        public void setPrimaryConfig(Class<?> entityClass, DatabaseConfig config) {
            delegate().setPrimaryConfig(entityClass, config);
        }

        /**
         * @see EntityStore#getSecondaryConfig(Class, String)
         */
        public SecondaryConfig getSecondaryConfig(Class<?> entityClass, String keyName) {
            return delegate().getSecondaryConfig(entityClass, keyName);
        }

        /**
         * @see EntityStore#setSecondaryConfig(Class, String, SecondaryConfig)
         */
        public void setSecondaryConfig(Class<?> entityClass, String keyName, SecondaryConfig config) {
            delegate().setSecondaryConfig(entityClass, keyName, config);
        }
        
        @Override protected EntityStore delegate() {
            return checkNotNull(_store, "Please connect an EntityStore first.");
        }

        @Override protected BerkeleyStore THIS() {
            return this;
        }

        @Override public void doClose() {
            if (null != _store) {
                close();
            }
        }
        
        private EntityStore _store;
        
    }
    
   /**
    * @see SecondaryKeyCreator
    */
   public static abstract class GenericKeyCreator<SK, D> implements SecondaryKeyCreator {
       
       @Override public boolean createSecondaryKey(SecondaryDatabase secondary,
                                                   DatabaseEntry key, DatabaseEntry data, DatabaseEntry result) {
           D d = null;
           this.primaryKey = key;
           this.secondary = secondary;
           try {
               d = dataBinding.entryToObject(data);
           } catch (Exception e) {
               return false;
           }
           secKeyBinding.objectToEntry(createSecondaryKey(d), result);
           return true;
       }
       
       /**
        * @see SecondaryKeyCreator#createSecondaryKey(SecondaryDatabase, DatabaseEntry, DatabaseEntry, DatabaseEntry)
        */
       protected abstract SK createSecondaryKey(D data);
       
       protected void initBinding(ClassCatalog catalog) {
           if (null != this.secKeyBinding && null != this.dataBinding) {
               return;
           }
           
           Pair<Class<SK>, Class<D>> clazzPair = getGenericSuperclass(getClass().getGenericSuperclass());
           this.secKeyBinding = getBinding(clazzPair.getL(), checkNotNull(catalog, "ClassCatalog cannot be null"));
           this.dataBinding = getBinding(clazzPair.getR(), catalog);
       }
       
       public EntryBinding<D> getDataBinding() {
           return dataBinding;
       }
       
       public EntryBinding<SK> getSecondaryKeyBinding() {
           return secKeyBinding;
       }
       
       private EntryBinding<D> dataBinding;
       private EntryBinding<SK> secKeyBinding;
       
       protected DatabaseEntry primaryKey;
       protected SecondaryDatabase secondary;
   }
    
   /**
    * @see SecondaryDatabase
    */
    public static class BerkeleyIndex extends BaseBerkeleyDatabase<BerkeleyIndex> {
        
        /**
         * Returns the data with given key.
         * Or throw new UnsupportedOperationException if key creator is not {@link GenericKeyCreator} instance
         */
        public <K, E> E get(K key) {
            if (null != genericKeyCreator) {
                return get(key, genericKeyCreator.getDataBinding());
            }
            
            throw new UnsupportedOperationException(
                    "Operation not allowed with null GenericKeyCreator, using get(K key, Class<?> clazz) instead.");
        }
        
        /**
         * Returns the {@link BerkeleyDB} instance
         */
        public BerkeleyDB getBerkeleyDatabase() {
            return this.berkeleyDB;
        }

        /**
         * @see SecondaryDatabase#sync()
         */
        public void sync() throws DatabaseException, UnsupportedOperationException {
            delegate().sync();
        }

        /**
         * @see SecondaryDatabase#openSequence(Transaction, DatabaseEntry, SequenceConfig)
         */
        public Sequence openSequence(final Transaction txn, final DatabaseEntry key,
                                     final SequenceConfig config) throws SequenceNotFoundException,
                SequenceExistsException {
            return delegate().openSequence(txn, key, config);
        }

        /**
         * @see SecondaryDatabase#removeSequence(Transaction, DatabaseEntry)
         */
        public void removeSequence(final Transaction txn, final DatabaseEntry key) throws DatabaseException {
            delegate().removeSequence(txn, key);
        }

        /**
         * @see SecondaryDatabase#openCursor(DiskOrderedCursorConfig)
         */
        public DiskOrderedCursor openCursor(final DiskOrderedCursorConfig cursorConfig)
                throws DatabaseException, IllegalArgumentException {
            return delegate().openCursor(cursorConfig);
        }

        /**
         * @see SecondaryDatabase#preload(PreloadConfig)
         */
        public PreloadStats preload(final PreloadConfig config) throws DatabaseException {
            return delegate().preload(config);
        }

        /**
         * @see SecondaryDatabase#count()
         */
        public long count() throws DatabaseException {
            return delegate().count();
        }

        /**
         * @see SecondaryDatabase#count(long)
         */
        public long count(long memoryLimit) throws DatabaseException {
            return delegate().count(memoryLimit);
        }

        /**
         * @see SecondaryDatabase#getStats(StatsConfig)
         */
        public DatabaseStats getStats(final StatsConfig config) throws DatabaseException {
            return delegate().getStats(config);
        }

        /**
         * @see SecondaryDatabase#verify(VerifyConfig)
         */
        public DatabaseStats verify(final VerifyConfig config) throws DatabaseException {
            return delegate().verify(config);
        }

        /**
         * @see SecondaryDatabase#getDatabaseName()
         */
        public String getDatabaseName() throws DatabaseException {
            return delegate().getDatabaseName();
        }

        /**
         * @see SecondaryDatabase#getEnvironment()
         */
        public Environment getEnvironment() {
            return delegate().getEnvironment();
        }

        /**
         * @see SecondaryDatabase#compareKeys(DatabaseEntry, DatabaseEntry)
         */
        public int compareKeys(final DatabaseEntry entry1, final DatabaseEntry entry2) {
            return delegate().compareKeys(entry1, entry2);
        }

        /**
         * @see SecondaryDatabase#compareDuplicates(DatabaseEntry,
         *      DatabaseEntry)
         */
        public int compareDuplicates(final DatabaseEntry entry1, final DatabaseEntry entry2) {
            return delegate().compareDuplicates(entry1, entry2);
        }

        private BerkeleyDB berkeleyDB = null;
        private SecondaryDatabase delegate = null;
        private StoredClassCatalog catalog = null;
        private GenericKeyCreator<?, ?> genericKeyCreator = null;

        /**
         * @see SecondaryDatabase#close()
         */
        public synchronized void close() throws DatabaseException {
            delegate().close();
        }

        /**
         * @see SecondaryDatabase#getPrimaryDatabase()
         */
        public Database getPrimaryDatabase() {
            return delegate().getPrimaryDatabase();
        }

        /**
         * @see SecondaryDatabase#getConfig()
         */
        public SecondaryConfig getConfig() throws DatabaseException {
            return delegate().getConfig();
        }
        
        public BerkeleyIndexCursor opensCursor(Transaction txn, CursorConfig cursorConfig) {
            return new BerkeleyIndexCursor(getEnv(), this, openCursor(txn, cursorConfig));
        }

        /**
         * @see SecondaryDatabase#openCursor(Transaction, CursorConfig)
         */
        public SecondaryCursor openCursor(final Transaction txn, final CursorConfig cursorConfig)
                throws DatabaseException {
            return delegate().openCursor(txn, cursorConfig);
        }

        /**
         * @see SecondaryDatabase#delete(Transaction, DatabaseEntry)
         */
        public OperationStatus delete(final Transaction txn, final DatabaseEntry key)
                throws DeleteConstraintException, LockConflictException, DatabaseException,
                UnsupportedOperationException, IllegalArgumentException {
            return delegate().delete(txn, key);
        }

        /**
         * @see SecondaryDatabase#get(Transaction, DatabaseEntry, DatabaseEntry,
         *      LockMode)
         */
        @Override public OperationStatus get(final Transaction txn, final DatabaseEntry key,
                                   final DatabaseEntry data, final LockMode lockMode)
                throws DatabaseException {
            return delegate().get(txn, key, data, lockMode);
        }

        /**
         * @see SecondaryDatabase#get(Transaction, DatabaseEntry, DatabaseEntry,
         *      DatabaseEntry, LockMode)
         */
        public OperationStatus get(final Transaction txn, final DatabaseEntry key,
                                   final DatabaseEntry pKey, final DatabaseEntry data,
                                   LockMode lockMode) throws DatabaseException {
            return delegate().get(txn, key, pKey, data, lockMode);
        }

        /**
         * @see SecondaryDatabase#get(Transaction, DatabaseEntry, DatabaseEntry,
         *      DatabaseEntry, LockMode)
         */
        public OperationStatus getSearchBoth(final Transaction txn, final DatabaseEntry key,
                                             final DatabaseEntry pKey, final DatabaseEntry data,
                                             LockMode lockMode) throws DatabaseException {
            return delegate().getSearchBoth(txn, key, pKey, data, lockMode);
        }

        @Override protected SecondaryDatabase delegate() {
            return checkNotNull(delegate, "The SecondaryDatabase delegate cannot be null");
        }

        @Override protected BerkeleyIndex THIS() {
            return this;
        }

        @Override protected void doClose() {
            delegate().close();
        }

        @Override public StoredClassCatalog getCatalog() {
            return catalog;
        }
        
        protected BerkeleyIndex(BerkeleyEnv berkeleyEnv, BerkeleyDB berkeleyDB, 
                                GenericKeyCreator<?, ?> genericKeyCreator, SecondaryDatabase delegate) {
            super(berkeleyEnv);
            this.delegate = delegate;
            this.berkeleyDB = checkNotNull(berkeleyDB, "BerkeleyDB cannot be null");
            this.catalog = checkNotNull(berkeleyDB.getCatalog(), "StoredClassCatalog cannot be null");
            this.genericKeyCreator = genericKeyCreator;
        }

    }
    
    /**
     * 
     */
    public static abstract class BaseBerkeleyDatabase<M> extends Berkeley<M> {

        protected BaseBerkeleyDatabase(BerkeleyEnv berkeleyEnv) {
            super(berkeleyEnv);
        }
        
        /**
         * Returns the data with given key and data class
         */
        public <K, E> E get(K key, Class<?> clazz) {
            return get(key, clazz, null);
        }
        
        /**
         * Returns the data with given key and data class and {@link Transaction}
         */
        public <K, E> E get(K key, Class<?> clazz, Transaction transaction) {
            return get(key, getBinding(clazz), transaction);
        }
        
        /**
         * Returns the data with given key and data class and {@link Transaction} and {@link LockMode}
         */
        public <K, E> E get(K key, Class<?> clazz, Transaction transaction, LockMode lockMode) {
            return get(key, getBinding(clazz), transaction, lockMode);
        }
 
        /**
         * Returns the data with given key and data {@link EntryBinding}
         */
        public <K, E> E get(K key, EntryBinding<?> binding) {
            return get(key, binding, null);
        }
        
        /**
         * Returns the data with given key and data {@link EntryBinding} and {@link Transaction}
         */
        public <K, E> E get(K key, EntryBinding<?> binding, Transaction transaction) {
            return get(key, binding, transaction, LockMode.DEFAULT);
        }
        
        /**
         * 
         */
        @SuppressWarnings("unchecked") public <K, E> E get(K key, EntryBinding<?> binding, 
                                                           Transaction transaction, LockMode lockMode) {
            //Transaction, key DatabaseEntry, result DatabaseEntry, LockMode
            DatabaseEntry data = new DatabaseEntry();
            OperationStatus status = get(transaction, getEntry(key), data, checkNotNull(lockMode));
            if (OperationStatus.SUCCESS == status) {
                return (E) binding.entryToObject(data);
            }
            
            log.warn(status.toString() + " with key: " + key);
            return null;
        }
        
        /**
         * Returns matched {@link EntryBinding} with given target
         */
        public <T, E> EntryBinding<E> getBinding(T target) {
            return getBinding(target, getCatalog());
        }
        
        /**
         * Returns {@link DatabaseEntry} instance with given target
         */
        public <T> DatabaseEntry getEntry(T target) {
            return getEntry(target, getBinding(target));
        }
        
        /**
         * Returns {@link DatabaseEntry} instance with given target and {@link EntryBinding}
         */
        public <T> DatabaseEntry getEntry(T target, EntryBinding<T> binding) {
            DatabaseEntry entry = new DatabaseEntry();
            checkNotNull(binding, "The EntryBinding cannot be null.").objectToEntry(target, entry);
            return entry;
        }
        
        public <E> E getValue(EntryBinding<E> binding, DatabaseEntry entry) {
            return binding.entryToObject(entry);
        }
        
        /**
         * Returns the {@link StoredClassCatalog} instance
         */
        public abstract StoredClassCatalog getCatalog();
        
        /**
         * 
         */
        public abstract OperationStatus get(final Transaction txn, final DatabaseEntry key,
                                   final DatabaseEntry data, LockMode lockMode)
                                   throws LockConflictException, DatabaseException, IllegalArgumentException;
        
    }
    
    /**
     * 
     */
    public static abstract class DBEntryVisit<K, D> {
        
        public DBEntryVisit() {
            Pair<Class<K>, Class<D>> clazzPair = getGenericSuperclass(getClass().getGenericSuperclass());
            this.keyClazz = clazzPair.getL();
            this.dataClazz = clazzPair.getR();
        }
        
        public DBEntryVisit(boolean ascending) {
            this();
            this.ascending = ascending;
        }
        
        protected abstract void apply(K key, D data);
        
        public Class<K> getKeyClass() {
            return this.keyClazz;
        }
        
        public Class<D> getDataClass() {
            return this.dataClazz;
        }
        
        public boolean isAscending() {
            return ascending;
        }

        public void setAscending(boolean ascending) {
            this.ascending = ascending;
        }

        private boolean ascending = true;
        private Class<K> keyClazz = null;
        private Class<D> dataClazz = null;
    }
    
    /**
     * @see DatabaseEntry
     */
    public static class DBEntry extends DatabaseEntry {

        /**
         * 
         */
        private static final long serialVersionUID = -1093431498750484993L;
        
        public DBEntry(StoredClassCatalog catalog) {
            this.catalog = checkNotNull(catalog);
        }
        
        /**
         * @see DatabaseEntry#entryToObject
         */
        public <E> E asObject(EntryBinding<E> entryBinding) {
            return entryBinding.entryToObject(this);
        }
        
        /**
         * @see DatabaseEntry#entryToObject
         */
        @SuppressWarnings("unchecked") public <E> E asObject(Class<E> entryClass) {
            return (E) getBinding(entryClass, catalog).entryToObject(this);
        }
        
        private StoredClassCatalog catalog = null;
    }
    
    /**
     *
     */
    public static abstract class BaseBerkeleyCursor<M> extends Berkeley<M> {
        
        /**
         * 
         */
        public <K, D> void pageVisit(int skip, int count, DBEntryVisit<K, D> visit) {
            pageVisit(skip, count, null, visit);
        }
        
        /**
         * 
         */
        public <K, D> void pageVisit(int skip, int count, LockMode lockMode, DBEntryVisit<K, D> visit) {
            int pos = 0;
            boolean firstGet = true;
            boolean ascending = visit.isAscending();
            
            Pair<DBEntry, DBEntry> kv = ascending ? this.getFirst(lockMode) : this.getLast(lockMode);
            
            if ((skip = skip - 1) > 0) {
                Pair<DBEntry, DBEntry> tmpKV = ascending ? this.skipNext(skip, lockMode) : this.skipPrev(skip, lockMode);
                kv = firstNonNull(tmpKV, kv);
            }
            
            while (pos < count 
                    && (kv = firstGet ? kv : (ascending ? this.getNextNoDup(lockMode) : this.getPrevNoDup(lockMode))) != null) {
                if (firstGet) {
                    firstGet = false;
                }
                
                try {
                    visit.apply(kv.getL().asObject(visit.getKeyClass()), kv.getR().asObject(visit.getDataClass()));
                    pos++;
                } catch (Exception e) { }
            }
        }
        
        /**
         * Returns {@link DatabaseEntry} instance with given target
         */
        public <T> DatabaseEntry getEntry(T target) {
            return berkeleyDB.getEntry(target);
        }
        
        /**
         * @see Cursor#getCurrent(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> getCurrent() {
            return getCurrent(null);
        }
        
        /**
         * @see Cursor#getCurrent(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> getCurrent(LockMode lockMode) {
            return get('c', lockMode);
        }
        
        /**
         * @see Cursor#getFirst(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> getFirst() {
            return getFirst(null);
        }
        
        /**
         * @see Cursor#getFirst(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> getFirst(LockMode lockMode) {
            return get('f', lockMode);
        }
        
        /**
         * @see Cursor#getLast(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> getLast() {
            return getLast(null);
        }
        
        /**
         * @see Cursor#getLast(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> getLast(LockMode lockMode) {
            return get('l', lockMode);
        }
        
        /**
         * @see Cursor#getNext(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> getNext() {
            return getNext(null);
        }
        
        /**
         * @see Cursor#getNext(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> getNext(LockMode lockMode) {
            return get('n', lockMode);
        }
        
        /**
         * @see Cursor#getNextDup(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> getNextDup() {
            return getNextDup(null);
        }
        
        /**
         * @see Cursor#getNextDup(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> getNextDup(LockMode lockMode) {
            return get('N', lockMode);
        }
        
        /**
         * @see Cursor#getNextNoDup(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> getNextNoDup() {
            return getNextNoDup(null);
        }
        
        /**
         * @see Cursor#getNextNoDup(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> getNextNoDup(LockMode lockMode) {
            return get('O', lockMode);
        }
        
        /**
         * @see Cursor#getPrev(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> getPrev() {
            return getPrev(null);
        }
        
        /**
         * @see Cursor#getPrev(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> getPrev(LockMode lockMode) {
            return get('p', lockMode);
        }
        
        /**
         * @see Cursor#getPrevDup(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> getPrevDup() {
            return getPrevDup(null);
        }
        
        /**
         * @see Cursor#getPrevDup(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> getPrevDup(LockMode lockMode) {
            return get('P', lockMode);
        }
        
        /**
         * @see Cursor#getPrevNoDup(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> getPrevNoDup() {
            return getPrevNoDup(null);
        }
        
        /**
         * @see Cursor#getPrevNoDup(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> getPrevNoDup(LockMode lockMode) {
            return get('Q', lockMode);
        }
        
        /**
         * @see Cursor#skipNext(long, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> skipNext(long maxCount) {
            return skipNext(maxCount, null);
        }
        
        /**
         * @see Cursor#skipNext(long, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> skipNext(long maxCount, LockMode lockMode) {
            return skip('n', maxCount, lockMode);
        }
        
        /**
         * @see Cursor#skipPrev(long, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> skipPrev(long maxCount) {
            return skipPrev(maxCount, null);
        }

        /**
         * @see Cursor#skipPrev(long, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public Pair<DBEntry, DBEntry> skipPrev(long maxCount, LockMode lockMode) {
            return skip('p', maxCount, lockMode);
        }
        
        /**
         * 
         */
        private Pair<DBEntry, DBEntry> skip(char which, long maxCount, LockMode lockMode) {
            long skipped = 0;
            DBEntry key = new DBEntry(catalog);
            DBEntry data = new DBEntry(catalog);
            lockMode = null == lockMode ? LockMode.DEFAULT : lockMode;
            
            switch (which) {
                case 'n': skipped = delegate.skipNext(maxCount, key, data, lockMode); break;
                case 'p': skipped = delegate.skipPrev(maxCount, key, data, lockMode); break;
            }
            
            if (skipped <= 0) {
                return null;
            }
            
            return Pair.of(key, data);
        }
        
        /**
         * 
         */
        private Pair<DBEntry, DBEntry> get(char which, LockMode lockMode) {
            OperationStatus status = null;
            DBEntry key = new DBEntry(catalog);
            DBEntry data = new DBEntry(catalog);
            lockMode = null == lockMode ? LockMode.DEFAULT : lockMode;
            
            switch (which) {
                case 'c':   //getCurrent
                    status = delegate.getCurrent(key, data, lockMode);
                    break;
                case 'f':   //getFirst
                    status = delegate.getFirst(key, data, lockMode);
                    break;
                case 'l':   //getLast
                    status = delegate.getLast(key, data, lockMode);
                    break;
                case 'n':   //getNext
                    status = delegate.getNext(key, data, lockMode);
                    break; 
                case 'N':   //getNextDup
                    status = delegate.getNextDup(key, data, lockMode);
                    break;
                case 'O':   //getNextNoDup
                    status = delegate.getNextNoDup(key, data, lockMode);
                    break;
                case 'p':   //getPrev
                    status = delegate.getPrev(key, data, lockMode);
                    break;
                case 'P':   //getPrevDup
                    status = delegate.getPrevDup(key, data, lockMode);
                    break;
                case 'Q':   //getPrevNoDup
                    status = delegate.getPrevNoDup(key, data, lockMode);
                    break;
            }
            
            if (status == OperationStatus.SUCCESS) {
                return Pair.of(key, data);
            }
            
            return null;
        }
        
        /**
         * Returns the {@link BerkeleyDB} instance
         */
        public BerkeleyDB getBerkeleyDatabase() {
            return berkeleyDB;
        }
        
        protected BaseBerkeleyCursor(BerkeleyEnv berkeleyEnv, BerkeleyDB berkeleyDB, Cursor cursor) {
            super(berkeleyEnv);
            this.delegate = checkNotNull(cursor);
            this.berkeleyDB = checkNotNull(berkeleyDB);
            this.catalog = checkNotNull(berkeleyDB.getCatalog());
        }
        
        private BerkeleyDB berkeleyDB = null;
        protected Cursor delegate = null;
        private StoredClassCatalog catalog = null;
    }
    
    /**
     * @see SecondaryCursor
     */
    public static class BerkeleyIndexCursor extends BaseBerkeleyCursor<BerkeleyIndexCursor> {
        
        /**
         * @see SecondaryCursor#getConfig()
         */
        public CursorConfig getConfig() {
            return delegate().getConfig();
        }

        /**
         * @see SecondaryCursor#getCacheMode()
         */
        public CacheMode getCacheMode() {
            return delegate().getCacheMode();
        }

        /**
         * @see SecondaryCursor#setCacheMode(CacheMode)
         */
        public void setCacheMode(final CacheMode cacheMode) {
            delegate().setCacheMode(cacheMode);
        }

        /**
         * @see SecondaryCursor#close()
         */
        public void close() throws DatabaseException {
            delegate().close();
        }

        /**
         * @see SecondaryCursor#skipNext(long, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public long skipNext(final long maxCount, final DatabaseEntry key,
                              final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().skipNext(maxCount, key, data, lockMode);
        }

        /**
         * @see SecondaryCursor#skipPrev(long, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public long skipPrev(final long maxCount, final DatabaseEntry key, 
                             final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().skipPrev(maxCount, key, data, lockMode);
        }

        /**
         * @see SecondaryCursor#count()
         */
        public int count() {
            return delegate().count();
        }

        /**
         * @see SecondaryCursor#countEstimate()
         */
        public long countEstimate() throws DatabaseException {
            return delegate().countEstimate();
        }
        
        /**
         * Returns the {@link BerkeleyIndex} instance
         */
        public BerkeleyIndex getBerkeleyIndex() {
            return this.berkeleyIndex;
        }
        
        /**
         * @see SecondaryCursor#getDatabase()
         */
        public SecondaryDatabase getDatabase() {
            return delegate().getDatabase();
        }

        /**
         * @see SecondaryCursor#getPrimaryDatabase()
         */
        public Database getPrimaryDatabase() {
            return delegate().getPrimaryDatabase();
        }
        
        /**
         * @see SecondaryCursor#dup(boolean)
         */
        public SecondaryCursor dup(final boolean samePosition) throws DatabaseException {
            return delegate().dup(samePosition);
        }

        /**
         * @see SecondaryCursor#delete()
         */
        public OperationStatus delete() 
                throws LockConflictException, DatabaseException, UnsupportedOperationException, IllegalStateException {
            return delegate().delete();
        }

        /**
         * @see SecondaryCursor#getCurrent(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getCurrent(final DatabaseEntry key, 
                                          final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getCurrent(key, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getCurrent(DatabaseEntry, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getCurrent(final DatabaseEntry key, final DatabaseEntry pKey, 
                                          final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getCurrent(key, pKey, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getFirst(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getFirst(final DatabaseEntry key, 
                                        final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getFirst(key, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getFirst(DatabaseEntry, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getFirst(final DatabaseEntry key, final DatabaseEntry pKey, 
                                        final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getFirst(key, pKey, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getLast(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getLast(final DatabaseEntry key, 
                                       final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getLast(key, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getLast(DatabaseEntry, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getLast(final DatabaseEntry key, final DatabaseEntry pKey, 
                                       final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getLast(key, pKey, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getNext(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getNext(final DatabaseEntry key, 
                                       final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getNext(key, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getNext(DatabaseEntry, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getNext(final DatabaseEntry key, final DatabaseEntry pKey, 
                                       final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getNext(key, pKey, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getNextDup(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getNextDup(final DatabaseEntry key, 
                                          final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getNextDup(key, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getNextDup(DatabaseEntry, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getNextDup(final DatabaseEntry key, final DatabaseEntry pKey, 
                                          final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getNextDup(key, pKey, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getNextNoDup(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getNextNoDup(final DatabaseEntry key, 
                                            final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getNextNoDup(key, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getNextNoDup(DatabaseEntry, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getNextNoDup(final DatabaseEntry key, final DatabaseEntry pKey, 
                                            final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getNextNoDup(key, pKey, data, lockMode);
        }
        

        /**
         * @see SecondaryCursor#getPrev(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getPrev(final DatabaseEntry key,
                                       final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getPrev(key, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getPrev(DatabaseEntry, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getPrev(final DatabaseEntry key, final DatabaseEntry pKey, 
                                       final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getPrev(key, pKey, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getPrevDup(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getPrevDup(final DatabaseEntry key, 
                                          final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getPrevDup(key, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getPrevDup(DatabaseEntry, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getPrevDup(final DatabaseEntry key, final DatabaseEntry pKey, 
                                          final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getPrevDup(key, pKey, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getPrevNoDup(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getPrevNoDup(final DatabaseEntry key, 
                                            final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getPrevNoDup(key, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getPrevNoDup(DatabaseEntry, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getPrevNoDup(final DatabaseEntry key, final DatabaseEntry pKey, 
                                            final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getPrevNoDup(key, pKey, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getSearchKey(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getSearchKey(final DatabaseEntry key, 
                                            final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getSearchKey(key, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getSearchKey(DatabaseEntry, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getSearchKey(final DatabaseEntry key, final DatabaseEntry pKey, 
                                            final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getSearchKey(key, pKey, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getSearchKeyRange(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getSearchKeyRange(final DatabaseEntry key, 
                                                 final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getSearchKeyRange(key, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getSearchKeyRange(DatabaseEntry, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getSearchKeyRange(final DatabaseEntry key, final DatabaseEntry pKey, 
                                                 final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getSearchKeyRange(key, pKey, data, lockMode);
        }
        
        /**
         * @see SecondaryCursor#getSearchBoth(DatabaseEntry, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getSearchBoth(final DatabaseEntry key, final DatabaseEntry pKey, 
                                             final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getSearchBoth(key, pKey, data, lockMode);
        }

        /**
         * @see SecondaryCursor#getSearchBothRange(DatabaseEntry, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getSearchBothRange(final DatabaseEntry key, final DatabaseEntry pKey, 
                                                  final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().getSearchBothRange(key, pKey, data, lockMode);
        }
        
        protected BerkeleyIndexCursor(BerkeleyEnv berkeleyEnv, BerkeleyIndex berkeleyIndex, Cursor cursor) {
            super(berkeleyEnv, checkNotNull(berkeleyIndex, "BerkeleyIndex cannot be null").getBerkeleyDatabase(), cursor);
            this.berkeleyIndex = berkeleyIndex;
        }

        @Override protected BerkeleyIndexCursor THIS() {
            return this;
        }

        @Override protected void doClose() {
            delegate().close();
        }

        @Override protected SecondaryCursor delegate() {
            return (SecondaryCursor) checkNotNull(delegate, "The SecondaryCursor delegate cannot be null");
        }
        
        private BerkeleyIndex berkeleyIndex = null;
        
    }
    
    /**
     * @see Cursor
     */
    public static class BerkeleyCursor extends BaseBerkeleyCursor<BerkeleyCursor> {
        
        /**
         * @see BerkeleyCursor#put(DatabaseEntry, DatabaseEntry)
         */
        public <K, V> OperationStatus puts(K key, V value) {
            return put(getEntry(key), getEntry(value));
        }
        
        /**
         * @see BerkeleyCursor#putNoOverwrite(DatabaseEntry, DatabaseEntry)
         */
        public <K, V> OperationStatus putsNoOverwrite(K key, V value) {
            return putNoOverwrite(getEntry(key), getEntry(value));
        }
        
        /**
         * @see BerkeleyCursor#putNoDupData(DatabaseEntry, DatabaseEntry)
         */
        public <K, V> OperationStatus putsNoDupData(K key, V value) {
            return putNoDupData(getEntry(key), getEntry(value));
        }
        
        /**
         * @see BerkeleyCursor#putCurrent(DatabaseEntry, DatabaseEntry)
         */
        public <V> OperationStatus putsCurrent(V value) {
            return putCurrent(getEntry(value));
        }
        
        /**
         * @see Cursor#getDatabase()
         */
        public Database getDatabase() {
            return delegate().getDatabase();
        }

        /**
         * @see Cursor#getConfig()
         */
        public CursorConfig getConfig() {
            return delegate().getConfig();
        }

        /**
         * @see Cursor#getCacheMode()
         */
        public CacheMode getCacheMode() {
            return delegate().getCacheMode();
        }

        /**
         * @see Cursor#setCacheMode(CacheMode)
         */
        public void setCacheMode(final CacheMode cacheMode) {
            delegate().setCacheMode(cacheMode);
        }

        /**
         * @see Cursor#close()
         */
        public void close() throws DatabaseException {
            delegate().close();
        }

        /**
         * @see Cursor#dup(boolean)
         */
        public Cursor dup(final boolean samePosition) throws DatabaseException {
            return delegate().dup(samePosition);
        }

        /**
         * @see Cursor#delete()
         */
        public OperationStatus delete() throws LockConflictException, DatabaseException, UnsupportedOperationException {
            return delegate().delete();
        }

        /**
         * @see Cursor#put(DatabaseEntry, DatabaseEntry)
         */
        public OperationStatus put( final DatabaseEntry key, final DatabaseEntry data)
            throws DatabaseException, UnsupportedOperationException {
            return delegate().put(key, data);
        }

        /**
         * @see Cursor#putNoOverwrite(DatabaseEntry, DatabaseEntry)
         */
        public OperationStatus putNoOverwrite( final DatabaseEntry key, final DatabaseEntry data)
            throws DatabaseException, UnsupportedOperationException {
            return delegate().putNoOverwrite(key, data);
        }

        /**
         * @see Cursor#putNoDupData(DatabaseEntry, DatabaseEntry)
         */
        public OperationStatus putNoDupData( final DatabaseEntry key, final DatabaseEntry data)
            throws DatabaseException, UnsupportedOperationException {
            return delegate().putNoDupData(key, data);
        }

        /**
         * @see Cursor#putCurrent(DatabaseEntry)
         */
        public OperationStatus putCurrent(final DatabaseEntry data)
            throws DatabaseException, UnsupportedOperationException {
            return delegate().putCurrent(data);
        }

        /**
         * @see Cursor#getCurrent(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getCurrent( final DatabaseEntry key, final DatabaseEntry data, 
                                           final LockMode lockMode) throws DatabaseException {
            return delegate().getCurrent(key, data, lockMode);
        }

        /**
         * @see Cursor#getFirst(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getFirst( final DatabaseEntry key, final DatabaseEntry data, 
                                         final LockMode lockMode) throws DatabaseException {
            return delegate().getFirst(key, data, lockMode);
        }

        /**
         * @see Cursor#getLast(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getLast( final DatabaseEntry key, final DatabaseEntry data, 
                                        final LockMode lockMode) throws DatabaseException {
            return delegate().getLast(key, data, lockMode);
        }

        /**
         * @see Cursor#getNext(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getNext( final DatabaseEntry key, final DatabaseEntry data, 
                                        final LockMode lockMode) throws DatabaseException {
            return delegate().getNext(key, data, lockMode);
        }

        /**
         * @see Cursor#getNextDup(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getNextDup( final DatabaseEntry key, final DatabaseEntry data, 
                                           final LockMode lockMode) throws DatabaseException {
            return delegate().getNextDup(key, data, lockMode);
        }

        /**
         * @see Cursor#getNextNoDup(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getNextNoDup( final DatabaseEntry key, final DatabaseEntry data, 
                                             final LockMode lockMode) throws DatabaseException {
            return delegate().getNextNoDup(key, data, lockMode);
        }

        /**
         * @see Cursor#getPrev(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getPrev( final DatabaseEntry key, final DatabaseEntry data, 
                                        final LockMode lockMode) throws DatabaseException {
            return delegate().getPrev(key, data, lockMode);
        }

        /**
         * @see Cursor#getPrevDup(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getPrevDup( final DatabaseEntry key, final DatabaseEntry data, 
                                           final LockMode lockMode) throws DatabaseException {
            return delegate().getPrevDup(key, data, lockMode);
        }

        /**
         * @see Cursor#getPrevNoDup(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getPrevNoDup( final DatabaseEntry key, final DatabaseEntry data, 
                                             final LockMode lockMode) throws DatabaseException {
            return delegate().getPrevNoDup(key, data, lockMode);
        }

        /**
         * @see Cursor#skipNext(long, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public long skipNext( final long maxCount, final DatabaseEntry key, 
                              final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().skipNext(maxCount, key, data, lockMode);
        }

        /**
         * @see Cursor#skipPrev(long, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public long skipPrev( final long maxCount, final DatabaseEntry key, 
                              final DatabaseEntry data, final LockMode lockMode) throws DatabaseException {
            return delegate().skipPrev(maxCount, key, data, lockMode);
        }

        /**
         * @see Cursor#getSearchKey(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getSearchKey( final DatabaseEntry key, final DatabaseEntry data, 
                                             final LockMode lockMode) throws DatabaseException {
            return delegate().getSearchKey(key, data, lockMode);
        }

        /**
         * @see Cursor#getSearchKeyRange(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getSearchKeyRange( final DatabaseEntry key, final DatabaseEntry data, 
                                                  final LockMode lockMode) throws DatabaseException {
            return delegate().getSearchKeyRange(key, data, lockMode);
        }

        /**
         * @see Cursor#getSearchBoth(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getSearchBoth( final DatabaseEntry key, final DatabaseEntry data, 
                                              final LockMode lockMode) throws DatabaseException {
            return delegate().getSearchBoth(key, data, lockMode);
        }

        /**
         * @see Cursor#getSearchBothRange(DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getSearchBothRange( final DatabaseEntry key, final DatabaseEntry data, 
                                                   final LockMode lockMode) throws DatabaseException {
            return delegate().getSearchBothRange(key, data, lockMode);
        }

        /**
         * @see Cursor#count()
         */
        public int count() throws DatabaseException {
            return delegate().count();
        }
        
        /**
         * @see Cursor#countEstimate()
         */
        public long countEstimate() throws DatabaseException {
            return delegate().countEstimate();
        }
        
        protected BerkeleyCursor(BerkeleyEnv berkeleyEnv, BerkeleyDB berkeleyDB, Cursor cursor) {
            super(berkeleyEnv, berkeleyDB, cursor);
        }
        
        @Override protected BerkeleyCursor THIS() {
            return this;
        }

        @Override protected void doClose() {
            delegate().close();
        }

        @Override protected Cursor delegate() {
            return checkNotNull(delegate, "The Cursor delegate cannot be null");
        }
        
    }
   
    /**
     * @see Database
     */
    public static class BerkeleyDB extends BaseBerkeleyDatabase<BerkeleyDB> {
        
        public BerkeleyDB(BerkeleyEnv berkeleyEnv) {
            super(berkeleyEnv);
        }

        /**
         * Returns a {@link BerkeleyDB} instance with given parameter
         */
        public BerkeleyDB connection(String databaseName) {
            return connection(databaseName, null, null);
        }
        
        /**
         * Returns a {@link BerkeleyDB} instance with given parameter
         */
        public BerkeleyDB connection(String databaseName, DatabaseConfig databaseConfig) {
            return connection(databaseName, databaseConfig, null);
        }
        
        public Database getDatabase() {
            return delegate();
        }
        
        @Override public StoredClassCatalog getCatalog() {
            return storedClassCatalog;
        }
        
        /**
         * Returns a {@link BerkeleyDB} instance with given parameter
         */
        public BerkeleyDB connection(String databaseName, DatabaseConfig databaseConfig, Transaction transaction) {
            _dbName = databaseName; _database = this.getEnv().openDatabase(transaction,
                    checkNotNull(databaseName), databaseConfig = firstNonNull(databaseConfig, defaultDatabaseConfig()));
            if (log.isDebugEnabled()) {
                log.debug("Database initialized with name: " + databaseName);
                log.debug("DatabaseConfig initialized: " + databaseConfig.toString());
                log.debug("Transaction initialized: " + (null != transaction ? transaction.toString() : null));
            }
            
            storedClassCatalog = new StoredClassCatalog(delegate());
            if (log.isDebugEnabled()) {
                log.debug("StoredClassCatalog initialized with database: " + delegate().getDatabaseName());
            }
            return this;
        }
        
        /**
         * @see Environment#openSecondaryDatabase(Transaction, String, Database, SecondaryConfig)
         */
        public <SK, D> BerkeleyIndex openIndex(String databaseName, 
                                                               Transaction txn, GenericKeyCreator<SK, D> genericKeyCreator) {
            checkNotNull(genericKeyCreator, "GenericKeyCreator cannot be null").initBinding(getCatalog());
            return openIndex(databaseName, txn, defaultSecondaryConfig(genericKeyCreator), genericKeyCreator);
        }
        
        /**
         * @see Environment#openSecondaryDatabase(Transaction, String, Database, SecondaryConfig)
         */
        public BerkeleyIndex openIndex(String databaseName, Transaction txn, SecondaryConfig dbConfig) {
            return openIndex(databaseName, txn, dbConfig, null);
        }
                
        /**
         * @see Environment#beginTransaction(Transaction, TransactionConfig)
         */
        public Transaction beginTransaction(Transaction parent,
                TransactionConfig txnConfig) {
            return getEnvironment().beginTransaction(parent, txnConfig);
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public <K, V> StoredSortedMap<K, V> getStoredSortedMap(
                EntryBinding<?> keyBinding, EntryBinding<?> valueBinding, boolean writeAllowed) {
            return new StoredSortedMap(getDatabase(), keyBinding, valueBinding, writeAllowed);
        }
        
        public <K, V> OperationStatus put(K key, V value) {
            return put(key, value, null);
        }
        
        public <K, V> OperationStatus put(K key, V value, Transaction transaction) {
            return put(transaction, getEntry(key), getEntry(value));
        }
        
        /**
         * @see Database#close()
         */
        public void close() throws DatabaseException {
            delegate().close();
        }

        /**
         * @see Database#sync()
         */
        public void sync() throws DatabaseException, UnsupportedOperationException {
            delegate().sync();
        }

        /**
         * @see Database#openSequence(Transaction, DatabaseEntry, SequenceConfig)
         */
        public Sequence openSequence(final Transaction txn,
                final DatabaseEntry key, final SequenceConfig config)
                throws SequenceNotFoundException, SequenceExistsException {
            return delegate().openSequence(txn, key, config);
        }

        /**
         * @see Database#removeSequence(Transaction, DatabaseEntry)
         */
        public void removeSequence(final Transaction txn, final DatabaseEntry key)
                throws DatabaseException {
            delegate().removeSequence(txn, key);
        }
        
        public BerkeleyCursor opensCursor(Transaction txn, CursorConfig cursorConfig) {
            return new BerkeleyCursor(getEnv(), this, openCursor(txn, cursorConfig));
        }

        /**
         * @see Database#openCursor(Transaction, CursorConfig)
         */
        public Cursor openCursor(final Transaction txn,
                final CursorConfig cursorConfig) throws DatabaseException,
                IllegalArgumentException {
            return delegate().openCursor(txn, cursorConfig);
        }

        /**
         * @see Database#openCursor(DiskOrderedCursorConfig)
         */
        public DiskOrderedCursor openCursor(
                final DiskOrderedCursorConfig cursorConfig)
                throws DatabaseException, IllegalArgumentException {
            return delegate().openCursor(cursorConfig);
        }

        /**
         * @see Database#populateSecondaries(DatabaseEntry, int)
         */
        public boolean populateSecondaries(final DatabaseEntry key,
                final int batchSize) {
            return delegate().populateSecondaries(key, batchSize);
        }

        /**
         * @see Database#delete(Transaction, DatabaseEntry)
         */
        public OperationStatus delete(final Transaction txn, final DatabaseEntry key)
                throws DeleteConstraintException, LockConflictException,
                DatabaseException, UnsupportedOperationException,
                IllegalArgumentException {
            return delegate().delete(txn, key);
        }

        /**
         * @see Database#get(Transaction, DatabaseEntry, DatabaseEntry, LockMode)
         */
        @Override public OperationStatus get(final Transaction txn, final DatabaseEntry key,
                final DatabaseEntry data, LockMode lockMode)
                throws LockConflictException, DatabaseException,
                IllegalArgumentException {
            return delegate().get(txn, key, data, lockMode);
        }

        /**
         * @see Database#getSearchBoth(Transaction, DatabaseEntry, DatabaseEntry, LockMode)
         */
        public OperationStatus getSearchBoth(final Transaction txn,
                final DatabaseEntry key, final DatabaseEntry data, LockMode lockMode)
                throws LockConflictException, DatabaseException,
                IllegalArgumentException {
            return delegate().getSearchBoth(txn, key, data, lockMode);
        }

        /**
         * @see Database#put(Transaction, DatabaseEntry, DatabaseEntry)
         */
        public OperationStatus put(final Transaction txn, final DatabaseEntry key,
                final DatabaseEntry data) throws DatabaseException {
            return delegate().put(txn, key, data);
        }

        /**
         * @see Database#putNoOverwrite(Transaction, DatabaseEntry, DatabaseEntry)
         */
        public OperationStatus putNoOverwrite(final Transaction txn,
                final DatabaseEntry key, final DatabaseEntry data)
                throws DatabaseException {
            return delegate().putNoOverwrite(txn, key, data);
        }

        /**
         * @see Database#putNoDupData(Transaction, DatabaseEntry, DatabaseEntry)
         */
        public OperationStatus putNoDupData(final Transaction txn,
                final DatabaseEntry key, final DatabaseEntry data)
                throws DatabaseException {
            return delegate().putNoDupData(txn, key, data);
        }

        /**
         * @see Database#join(Cursor[], JoinConfig)
         */
        public JoinCursor join(final Cursor[] cursors, final JoinConfig config)
                throws DatabaseException, IllegalArgumentException {
            return delegate().join(cursors, config);
        }

        /**
         * @see Database#preload(PreloadConfig)
         */
        public PreloadStats preload(final PreloadConfig config)
                throws DatabaseException {
            return delegate().preload(config);
        }

        /**
         * @see Database#count()
         */
        public long count() throws DatabaseException {
            return delegate().count();
        }

        /**
         * @see Database#getStats(StatsConfig)
         */
        public DatabaseStats getStats(final StatsConfig config)
                throws DatabaseException {
            return delegate().getStats(config);
        }

        /**
         * @see Database#verify(VerifyConfig)
         */
        public DatabaseStats verify(final VerifyConfig config)
                throws DatabaseException {
            return delegate().verify(config);
        }

        /**
         * @see Database#getDatabaseName()
         */
        public String getDatabaseName() throws DatabaseException {
            String dbName = delegate().getDatabaseName();
            if (null == dbName) {
                dbName = _dbName;
            }
            return dbName;
        }

        /**
         * @see Database#getConfig()
         */
        public DatabaseConfig getConfig() {
            return delegate().getConfig();
        }

        /**
         * @see Database#getSecondaryDatabases()
         */
        public List<SecondaryDatabase> getSecondaryDatabases() {
            return delegate().getSecondaryDatabases();
        }

        /**
         * @see Database#compareKeys(DatabaseEntry, DatabaseEntry)
         */
        public int compareKeys(final DatabaseEntry entry1,
                final DatabaseEntry entry2) {
            return delegate().compareKeys(entry1, entry2);
        }

        /**
         * @see Database#compareDuplicates(DatabaseEntry, DatabaseEntry)
         */
        public int compareDuplicates(final DatabaseEntry entry1,
                final DatabaseEntry entry2) {
            return delegate().compareDuplicates(entry1, entry2);
        }
        
        private String _dbName;
        private Database _database;
        private StoredClassCatalog storedClassCatalog = null;
        
        private <SK, D> BerkeleyIndex openIndex(String databaseName,
                                                Transaction txn, SecondaryConfig dbConfig, GenericKeyCreator<SK, D> genericKeyCreator) {
            return new BerkeleyIndex(getEnv(), this, genericKeyCreator, getEnv().openSecondaryDatabase(
                    txn, checkNotNull(databaseName, "databaseName cannot be null"), 
                    getDatabase(), checkNotNull(dbConfig, "SecondaryConfig cannot be null")));
        }

        @Override protected Database delegate() {
            return checkNotNull(_database, "Please connect a database first.");
        }

        @Override protected BerkeleyDB THIS() {
            return this;
        }

        @Override public void doClose() {
            if (null != storedClassCatalog) {
                storedClassCatalog.close();
            }
            if (null != _database) {
                close();
            }
        }
        
    }
    
    @SuppressWarnings("unchecked")
    protected static <F, S> Pair<Class<F>, Class<S>> getGenericSuperclass(Type type) {
        TypeDescrib typeDescrib = TypeRefer.of(checkNotNull(type)).asTypeDesc();
        Class<F> first = checkNotNull((Class<F>) typeDescrib.next().rawClazz());
        Class<S> second = checkNotNull((Class<S>) typeDescrib.next(1).rawClazz());
        return Pair.of(first, second);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected static <T, E> EntryBinding<E> getBinding(T target, ClassCatalog catalog) {
        EntryBinding<?> binding = bindingBehavior.update(target).doDetect();
        if (null == binding) {
            binding = new SerialBinding(checkNotNull(catalog, 
                    "ClassCatalog cannot be null"), (Class) (target instanceof Class ? target : target.getClass()));
        }
        
        return (EntryBinding<E>) binding;
    }
    
    /**
     * Returns the next available element in the sequence and changes the sequence value by 1
     */
    public Long increaseAndGet(String sequenceKey) {
        return increaseAndGet(sequenceKey, 1);
    }
    
    /**
     * @see Sequence#get(Transaction, int)
     */
    public Long increaseAndGet(String sequenceKey, int delta) {
        return increaseAndGet(sequenceKey, null, delta);
    }
    
    /**
     * @see Sequence#get(Transaction, int)
     */
    public Long increaseAndGet(String sequenceKey, Transaction txn, int delta) {
        return getSequence(sequenceKey, null).get(txn, delta);
    }
    
    /**
     * @see Database#removeSequence(Transaction, DatabaseEntry)
     */
    public void removeSequence(String sequenceKey) {
        removeSequence(sequenceKey, null);
    }
    
    /**
     * @see Database#removeSequence(Transaction, DatabaseEntry)
     */
    public void removeSequence(String sequenceKey, Transaction transaction) {
        sequences.remove(sequenceKey);
        sequenceDB().removeSequence(transaction, sequenceDB().getEntry(sequenceKey));
    }
    
    /**
     * Configuration a Sequence instance with frequently-used property
     * @see Berkeley#sequence(String, SequenceConfig)
     */
    public M sequence(String sequenceKey, Long initialValue, Boolean increment) {
        SequenceConfig cfg = defaultSequenceConfig();
        if (null != initialValue) {
            cfg.setInitialValue(initialValue);
        }
        if (null != increment) {
            cfg.setDecrement(!increment);
        }
        getSequence(sequenceKey, cfg);
        return THIS();
    }
    
    /**
     * Configuration a Sequence instance
     * @see Berkeley#getSequence(String, SequenceConfig)
     */
    public M sequence(String sequenceKey, SequenceConfig sequenceConfig) {
        getSequence(sequenceKey, firstNonNull(sequenceConfig, defaultSequenceConfig()));
        return THIS();
    }
    
    /**
     * Returns a {@link Sequence} instance with given configuration or {@link Berkeley#defaultSequenceConfig()} if null
     */
    public Sequence getSequence(String sequenceKey, SequenceConfig sequenceConfig) {
        Sequence sequence = sequences.get(checkNotNull(sequenceKey));
        if (null == sequence) {
            sequence = sequenceDB().openSequence(null, 
                    sequenceDB().getEntry(sequenceKey), firstNonNull(sequenceConfig, defaultSequenceConfig()));
            sequences.put(sequenceKey, sequence);
        }
        
        return sequence;
    }
    
    private BerkeleyDB sequenceDB() {
        if (!uniqueDB.isPresent()) {
            uniqueDB = Optional.of(from(getEnvHome().getPath(), "sequences"));
        }
        return uniqueDB.get();
    }
    
    /**
     * 
     */
    public void closeQuietly(boolean closeEnvironment) {
        try {
            doClose();
            if (closeEnvironment) {
                berkeleyEnv.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected abstract void doClose();
    
    public File getEnvHome() {
        return berkeleyEnv.getHome();
    }
    
    public BerkeleyEnv getEnv() {
        return berkeleyEnv;
    }
    
    public Environment getEnvironment() {
        return berkeleyEnv.getEnvironment();
    }
    
    protected Berkeley(BerkeleyEnv berkeleyEnv) {
        this.berkeleyEnv = checkNotNull(berkeleyEnv, "Berkeley environment cannot be null.");
    }
    
    private BerkeleyEnv berkeleyEnv = null;
    private Optional<BerkeleyDB> uniqueDB = Optional.absent();
    private Map<String, Sequence> sequences = Maps.newHashMap();
    
    /**
     * 
     */
    protected static final BindingBehavior bindingBehavior = new BindingBehavior(null);
    
    /**
     * @see BerkeleyStore
     */
    public static abstract class BerkeleyAccess<PK, E> {
        
        /**
         * 
         */
        protected final Log log = Loggers.from(this.getClass());
        
        /**
         * @see BerkeleyStore#increaseAndGet(String)
         */
        public Long autoincreaseID() {
            return autoincreaseID(entityClass.getSimpleName());
        }
        
        /**
         * @see BerkeleyStore#increaseAndGet(String)
         */
        public Long autoincreaseID(String sequenceName) {
            return store.increaseAndGet(sequenceName);
        }
        
        /**
         * @see BerkeleyStore#query(com.sleepycat.persist.EntityIndex, String)
         */
        public <SK> List<E> findPrefix(String keyName, String prefix) {
            SecondaryIndex<SK, PK, E> secondaryIndex = getRegisterSK(keyName);
            Class<SK> classSK = secondaryIndex.getKeyClass();
            checkArgument(String.class == classSK, "The secondary index class type must be java.lang.String.class, but is %s", classSK);
            
            @SuppressWarnings("unchecked") SecondaryIndex<String, PK, E> idx = (SecondaryIndex<String, PK, E>) secondaryIndex;
            return all(store.query(idx, checkNotNull(prefix)));
        }
        
        /**
         * @see BerkeleyStore#query(com.sleepycat.persist.EntityIndex, Object, boolean, Object, boolean)
         * @see EntityIndex#entities(Object, boolean, Object, boolean)
         */
        public <K, V> List<E> findRange(String keyName, K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            return all(store.query(getRegisterSK(keyName), fromKey, fromInclusive, toKey, toInclusive));
        }
        
        /**
         * @see PrimaryIndex#entities(Object, boolean, Object, boolean)
         */
        public List<E> findRange(PK fromKey, boolean fromInclusive, PK toKey, boolean toInclusive) {
            return all(getPK().entities(fromKey, fromInclusive, toKey, toInclusive));
        }
        
        /**
         * @see SecondaryIndex#subIndex(Object)
         */
        public <SK> List<E> find(String keyName, SK value) {
            return all(getRegisterSK(keyName).subIndex(checkNotNull(value)).entities());
        }
        
        /**
         * @see PrimaryIndex#count() 
         */
        public long count() {
            return getPK().count();
        }
        
        /**
         * @see PrimaryIndex#contains(Object) 
         */
        public boolean contains(PK key) {
            return getPK().contains(key);
        }
        
        /**
         * @see PrimaryIndex#entities()
         */
        public E one(ForwardCursor<E> cursor) {
            Closer closer = closer(checkNotNull(cursor));
            
            try {
                return cursor.next();
            } catch (Exception e) {
                quietRethrow(closer, e);
            } finally {
                quietClose(closer);
            }
            
            return null;
        }
        
        /**
         * @see PrimaryIndex#entities()
         */
        public List<E> all(ForwardCursor<E> cursor) {
            Closer closer = closer(checkNotNull(cursor));
            List<E> items = Lists.newArrayList();
            
            try {
                for (E item : cursor) {
                    items.add(item);
                }
            } catch (Exception e) {
                quietRethrow(closer, e);
            } finally {
                quietClose(closer);
            }
            
            return items;
        }
        
        /**
         *
         */
        public class JoinQuery extends EntityJoinQuery<PK, E> {
            
            public JoinQuery(PrimaryIndex<PK, E> index) {
                super(index);
            }

            public <SK> JoinQuery join(String keyName, SK value) {
                join(getRegisterSK(keyName), value);
                return this;
            }
            
            public E get() {
                return one(entities());
            }
            
            public List<E> find() {
                return all(entities());
            }
            
        }
        
        /**
         * @see BerkeleyStore#join(PrimaryIndex)
         */
        public <SK> JoinQuery join(String keyName, SK value) {
            return new JoinQuery(getPK()).join(keyName, value);
        }
        
        /**
         * @see PrimaryIndex#get
         */
        public E get(PK key) {
            return getPK().get(checkNotNull(key));
        }
        
        /**
         * Returns one item with the given field and key
         * 
         * @see SecondaryIndex#subIndex(Object)
         * @see BerkeleyAccess#one(ForwardCursor)
         */
        public <SK> E get(String keyName, SK key) {
            return one(getRegisterSK(keyName).subIndex(key).entities());
        }
        
        /**
         * Returns the items with given key list
         */
        public List<E> gets(List<PK> keys) {
            PrimaryIndex<PK, E> indexPK = getPK();
            E item = null; List<E> items = Lists.newArrayListWithCapacity(checkNotNull(keys).size());
            
            for (PK pk : keys) {
                if (null == (item = indexPK.get(pk))) {
                    log.warn(String.format("The primary key: %s is none exists.", pk));
                } else {
                    items.add(item);
                }
            }
            
            return items;
        }
        
        /**
         * Returns the items with given key array, one key returns one item
         */
        public List<E> gets(@SuppressWarnings("unchecked") PK... keys) {
            return gets(Arrays.asList(checkNotNull(keys)));
        }
        
        /**
         * Returns the items with given field and key list, one key returns one item
         */
        public <SK> List<E> gets(String keyName, List<SK> keys) {
            SecondaryIndex<SK, PK, E> indexSK = getRegisterSK(keyName);
            E item = null; List<E> items = Lists.newArrayListWithCapacity(checkNotNull(keys).size());
            
            for (SK sk : keys) {
                if (null == (item = one(indexSK.subIndex(sk).entities()))) {
                    log.warn(String.format("The Field %s's key: %s is none exists.", keyName, sk));
                } else {
                    items.add(item);
                }
            }
            
            return items;
        }
        
        /**
         * Returns the items with given field and key array
         */
        public <SK> List<E> gets(String keyName, @SuppressWarnings("unchecked") SK... keys) {
            return gets(keyName, Arrays.asList(checkNotNull(keys)));
        }
        
        /**
         * @see PrimaryIndex#put(Object)
         */
        public E save(E entity) {
            getPK().put(checkNotNull(entity));
            return entity;
        }
        
        /**
         * @see PrimaryIndex#put(Object)
         */
        public E update(E entity) {
            getPK().put(checkNotNull(entity));
            return entity;
        }
        
        /**
         * @see PrimaryIndex#delete(Object)
         */
        public boolean delete(PK key) {
            return getPK().delete(key);
        }
        
        /**
         * @see BerkeleyStore#getPrimaryIndex(Class, Class)
         */
        public PrimaryIndex<PK, E> getPK() {
            return store.getPrimaryIndex(primaryKeyClass, entityClass);
        }
        
        /**
         * @see BerkeleyStore#getSecondaryIndex(PrimaryIndex, Class, String)
         */
        public <SK> SecondaryIndex<SK, PK, E> getSK(Class<SK> keyClass, String keyName) {
            return store.getSecondaryIndex(getPK(), checkNotNull(keyClass), checkNotNull(keyName));
        }
        
        /**
         * @see BerkeleyStore#getSecondaryIndex(PrimaryIndex, Class, String)
         */
        public <SK> SecondaryIndex<SK, PK, E> registerSK(Class<SK> keyClass, String keyName) {
            SecondaryIndex<SK, PK, E> secondaryIdx = getSK(keyClass, keyName);
            secondaryIndexs.put(keyName, secondaryIdx);
            return secondaryIdx;
        }
        
        /**
         * 
         */
        public <SK> SecondaryIndex<SK, PK, E> getRegisterSK(String keyName) {
            @SuppressWarnings("unchecked") SecondaryIndex<SK, PK, E> secondaryIdx = 
                    (SecondaryIndex<SK, PK, E>) secondaryIndexs.get(checkNotNull(keyName));
            return checkNotNull(secondaryIdx, "The secondary index key name: %s has not register, use registerSK first.", keyName);
        }
        
        /**
         * @see Closer#create()
         * @see Closer#register(Closeable)
         */
        protected <C extends Closeable> Closer closer(C closeable) {
            Closer closer = Closer.create();
            closer.register(closeable);
            return closer;
        }
        
        /**
         * @see Closer#rethrow(Throwable)
         */
        protected void quietRethrow(Closer closer, Throwable throwable) {
            try {
                closer.rethrow(throwable);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        
        /**
         * @see Closer#close()
         */
        protected void quietClose(Closer closer) {
            try {
                closer.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        
        public BerkeleyStore getEntityStore() {
            return store;
        }
        
        public RawStore getRawStore() {
            if (rawStore.isPresent()) {
                return rawStore.get();
            }
            
            return (rawStore = Optional.of(new RawStore(store.getEnvironment(), store.getStoreName(), store.getConfig()))).get();
        }
        
        /**
         * 
         */
        public BerkeleyAccess(BerkeleyStore berkeleyStore) {
            this.store = checkNotNull(berkeleyStore);
            
            Pair<Class<PK>, Class<E>> clazzPair = getGenericSuperclass(getClass().getGenericSuperclass());
            this.primaryKeyClass = clazzPair.getL();
            this.entityClass = clazzPair.getR();
            if (log.isDebugEnabled()) {
                log.debug(String.format("EntityStore %s: %s data access initialized.", berkeleyStore.getStoreName(), entityClass.getName()));
            }
            
            //Register a secondary index for a given primary index and secondary key, opening it if necessary. 
            for (Field field : entityClass.getDeclaredFields()) {
                if (null != field.getAnnotation(SecondaryKey.class)) {
                    registerSK(field.getType(), field.getName());
                    if (log.isDebugEnabled()) {
                        log.debug("Auto register a secondary index: " + field.toGenericString());
                    }
                }
            }
        }
        
        private BerkeleyStore store = null;
        private Class<E> entityClass = null;
        private Class<PK> primaryKeyClass = null;
        //Dumps a store or all stores to standard output in raw XML format
        private Optional<RawStore> rawStore = Optional.absent();
        private Map<String, SecondaryIndex<?, ?, ?>> secondaryIndexs = Maps.newHashMap();
        
    }
    
}
