package com.benayn.ustyle.thirdparty;

import static com.benayn.ustyle.Objects2.wrapObj;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * @see https://github.com/toonetown/guava-ext
 */
public class Events {
	
    /**
     * An interface which indicates that a class can be subscribed to (via EventBus)
     */
    public interface Subscribable {
        /**
         * Returns the EventBus instance associated with this object.  Other objects can use this to subscribe to
         * events that it posts.  In general, it is favorable to use register(Object) or unregister(Object)
         */
        EventBus getEventBus();
        
        /**
         * A fluent function for registering on-the-fly.  Implementations of this interface should return themselves.
         *
         * @param o the object to register
         * @return this object
         */
        Subscribable register(Object o);
        
        /**
         * A fluent function that will unregister on-the-fly.  Implementations of this interface should return themselves.
         *
         * @param o the object to unregister
         * @return this object
         */
        Subscribable unregister(Object o);
        
    }
    
    /**
     * An annotation which is used to indicate a method that will call post for the given method type.  This is an optional 
     * annotation, and is used mainly for documentation purposes.  The annotation is retained for the source only.
     */
    @Retention(RetentionPolicy.SOURCE) @Target(ElementType.METHOD) public @interface Publish {
        /**
         * The events that this method publishes
         */
        Class<? extends Events.Base<Subscribable>>[] value();
    }
    
    /**
     * Returns a new AddEvent instance
     */
    public static <S extends Subscribable, E> AddEvent<S, E> addEvent(final S source, final E element) {
        return new AddEvent<S, E>(source, element);
    }
    
    /**
     * Returns a new RemoveEvent instance
     */
    public static <S extends Subscribable, E> RemoveEvent<S, E> removeEvent(final S source, final E element) {
        return new RemoveEvent<S, E>(source, element);
    }
    
    /**
     * Returns a new ModifyEvent instance
     */
    public static <S extends Subscribable, E> ModifyEvent<S, E> modifyEvent(
    		final S source, final E element, final E oldValue) {
        return new ModifyEvent<S, E>(source, element, oldValue);
    }
    
    /**
     * Returns a new ExceptionEvent instance
     */
    public static <S extends Subscribable, X extends Throwable> ExceptionEvent<S, X> 
    	exceptionEvent(final S source, final X exception) {
        return new ExceptionEvent<S, X>(source, exception);
    }
    
    /**
     * A base event which contains a source.  Most Subscribables should post subclasses of this event.
     */
    public static abstract class Base<S extends Subscribable> {
        /** The source which caused this event */
        public abstract S source();
    }
    
    /**
     * An interface which indicates an event that can regenerate itself with a new source.
     */
    public interface Repostable<T extends Base<N>, N extends Subscribable> {
        /** Creates a new event that is identical to this one with a different source */
        T newSource(final N newSource);
    }
    
    /**
     * An enum which contains types of change events
     */
    public static enum ChangeType {
        /** Change is an addition */
        ADDITION,
        /** Change is a removal */
        REMOVAL,
        /** Change is a modification */
        MODIFICATION
    }
    
    /**
     * A change event (add/remove/modify)
     */
    public static abstract class ChangeEvent<S extends Subscribable, E> extends Base<S> 
    						implements Repostable<ChangeEvent<Subscribable, E>, Subscribable> {
        /** The type of change event */
        public abstract ChangeType type();
        /** The element that was changed */
        public abstract E element();
    }
    
    /**
     * An addition event
     */
    public static class AddEvent<S extends Subscribable, E> extends ChangeEvent<S, E> {
        private final ChangeType type = ChangeType.ADDITION;
        private final S source;
        private final E element;
        
        public AddEvent(S source, E element) {
        	this.source = source;
        	this.element = element;
        }
        
        @Override public AddEvent<Subscribable, E> newSource(final Subscribable newSource) {
            return Events.addEvent(newSource, element);
        }

		@Override public ChangeType type() {
			return this.type;
		}

		@Override public E element() {
			return this.element;
		}

		@Override public S source() {
			return this.source;
		}
    }
    
    /**
     * A removal event
     */
    public static class RemoveEvent<S extends Subscribable, E> extends ChangeEvent<S, E> {
        private final ChangeType type = ChangeType.REMOVAL;
        private final S source;
        private final E element;
        
        public RemoveEvent(S source, E element) {
        	this.source = source;
        	this.element = element;
        }
        
        @Override public RemoveEvent<Subscribable, E> newSource(final Subscribable newSource) {
            return Events.removeEvent(newSource, element);
        }

		@Override public ChangeType type() {
			return this.type;
		}

		@Override public E element() {
			return this.element;
		}

		@Override public S source() {
			return this.source;
		}
    }
    
    /**
     * A modification event
     */
    public static class ModifyEvent<S extends Subscribable, E> extends ChangeEvent<S, E> {
        private final ChangeType type = ChangeType.MODIFICATION;
        private final S source;
        private final E element;
        private final E oldValue;
        
        public ModifyEvent(S source, E element, E oldValue) {
        	this.source = source;
        	this.element = element;
        	this.oldValue = oldValue;
        }
        
        @Override public ModifyEvent<Subscribable, E> newSource(final Subscribable newSource) {
            return Events.modifyEvent(newSource, element, oldValue);
        }

		@Override public ChangeType type() {
			return this.type;
		}

		@Override public E element() {
			return this.element;
		}

		@Override public S source() {
			return this.source;
		}
		
		public E oldValue() {
			return this.oldValue;
		}
    }
    
    /**
     * An exception event
     */
    public static class ExceptionEvent<S extends Subscribable, X extends Throwable> extends Base<S> 
    				implements Repostable<ExceptionEvent<Subscribable, X>, Subscribable> {
        private final S source;
        private final X exception;
        
        public ExceptionEvent(S source, X exception) {
        	this.source = source;
        	this.exception = exception;
        }
        
        @Override public ExceptionEvent<Subscribable, X> newSource(final Subscribable newSource) {
            return Events.exceptionEvent(newSource, exception);
        }

		@Override public S source() {
			return this.source;
		}
		
		public X exception() {
			return this.exception;
		}
    }

    /**
     * A handler which will log dead exception events to the given logger
     */
    public static class UnhandledExceptionLogger {
    	
        @SuppressWarnings("rawtypes")
		@Subscribe public void onDeadEvent(final DeadEvent event) {
            if (event.getEvent() instanceof ExceptionEvent) {
                final ExceptionEvent e = (ExceptionEvent) event.getEvent();
                
                ((Log) Loggers.from(e.source().getClass())).error(String
                		.format("Unhandled ExceptionEvent on %s", wrapObj(e.source())), e.exception());
            }
        }
    }
    
    private Events() {}
}
