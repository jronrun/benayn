/**
 * 
 */
package com.benayn.ustyle.logger;



/**
 *
 */
public interface Log {
	
	/**
     * <p> Is debug logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than debug. </p>
     *
     * @return true if debug is enabled in the underlying logger.
     */
    public boolean isDebugEnabled();


    /**
     * <p> Is error logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than error. </p>
     *
     * @return true if error is enabled in the underlying logger.
     */
    public boolean isErrorEnabled();


    /**
     * <p> Is fatal logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than fatal. </p>
     *
     * @return true if fatal is enabled in the underlying logger.
     */
    public boolean isFatalEnabled();


    /**
     * <p> Is info logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than info. </p>
     *
     * @return true if info is enabled in the underlying logger.
     */
    public boolean isInfoEnabled();


    /**
     * <p> Is trace logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than trace. </p>
     *
     * @return true if trace is enabled in the underlying logger.
     */
    public boolean isTraceEnabled();


    /**
     * <p> Is warn logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than warn. </p>
     *
     * @return true if warn is enabled in the underlying logger.
     */
    public boolean isWarnEnabled();
    
	/**
	   * Log a message at the TRACE level.
	   *
	   * @param msg the message Object to be logged
	   * @since 1.4
	   */
	  public void trace(Object msg);
	  
	  /**
	   * Log an exception (throwable) at the TRACE level with an
	   * accompanying message.
	   *
	   * @param msg the message accompanying the exception
	   * @param t   the exception (throwable) to log
	   */
	  public void trace(Object msg, Throwable t);

	  /**
	   * Log a message at the DEBUG level.
	   *
	   * @param msg the message Object to be logged
	   */
	  public void debug(Object msg);
	  
	  /**
	   * Log an exception (throwable) at the DEBUG level with an
	   * accompanying message.
	   *
	   * @param msg the message accompanying the exception
	   * @param t   the exception (throwable) to log
	   */
	  public void debug(Object msg, Throwable t);

	  /**
	   * Log a message at the INFO level.
	   *
	   * @param msg the message Object to be logged
	   */
	  public void info(Object msg);
	  
	  /**
	   * Log an exception (throwable) at the INFO level with an
	   * accompanying message.
	   *
	   * @param msg the message accompanying the exception
	   * @param t   the exception (throwable) to log
	   */
	  public void info(Object msg, Throwable t);

	  /**
	   * Log a message at the WARN level.
	   *
	   * @param msg the message Object to be logged
	   */
	  public void warn(Object msg);

	  /**
	   * Log an exception (throwable) at the WARN level with an
	   * accompanying message.
	   *
	   * @param msg the message accompanying the exception
	   * @param t   the exception (throwable) to log
	   */
	  public void warn(Object msg, Throwable t);
	  
	  /**
	   * Log a message at the ERROR level.
	   *
	   * @param msg the message Object to be logged
	   */
	  public void error(Object msg);

	  /**
	   * Log an exception (throwable) at the ERROR level with an
	   * accompanying message.
	   *
	   * @param msg the message accompanying the exception
	   * @param t   the exception (throwable) to log
	   */
	  public void error(Object msg, Throwable t);

	  /**
	   * Log a message at the FATAL level.
	   *
	   * @param msg the message Object to be logged
	   */
	  public void fatal(Object msg);

	  /**
	   * Log an exception (throwable) at the FATAL level with an
	   * accompanying message.
	   *
	   * @param msg the message accompanying the exception
	   * @param t   the exception (throwable) to log
	   */
	  public void fatal(Object msg, Throwable t);
	  
	  /**
	   * Returns the delegate instance
	   * 
	   * @return
	   */
	  public <D> D delegator();
	  
	  /**
       * Log target as JSON
       * 
       * @return
       */
      public Log jsonStyle();
    
      /**
       * Log target as formatted JSON
       * 
       * @return
       */
      public Log humanStyle();
    
      /**
       * Log target as more info string
       * 
       * @return
       */
      public Log infoStyle();
}
