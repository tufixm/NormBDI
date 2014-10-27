package jadex.platform.service.cms;

import java.util.logging.Level;
import java.util.logging.LogRecord;


/**
 * 
 */
public class CustomLogRecord extends LogRecord
{
	private boolean needToInferCaller;
	
	/**
	 * 
	 */
	public CustomLogRecord(Level level, String msg)
	{
		super(level, msg);
		needToInferCaller = true;
	}
	
	 /**
     * Get the  name of the class that (allegedly) issued the logging request.
     * <p>
     * Note that this sourceClassName is not verified and may be spoofed.
     * This information may either have been provided as part of the
     * logging call, or it may have been inferred automatically by the
     * logging framework.  In the latter case, the information may only
     * be approximate and may in fact describe an earlier call on the
     * stack frame.
     * <p>
     * May be null if no information could be obtained.
     *
     * @return the source class name
     */  
    public String getSourceClassName() 
    {
		if(needToInferCaller) 
		{
			// sets super.needToInferCaller to false
		    inferCaller();
		}
		return super.getSourceClassName();
    }
    
    /**
     * Get the  name of the method that (allegedly) issued the logging request.
     * <p>
     * Note that this sourceMethodName is not verified and may be spoofed.
     * This information may either have been provided as part of the
     * logging call, or it may have been inferred automatically by the
     * logging framework.  In the latter case, the information may only
     * be approximate and may in fact describe an earlier call on the
     * stack frame.
     * <p>
     * May be null if no information could be obtained.
     *
     * @return the source method name
     */  
    public String getSourceMethodName() 
    {
    	if(needToInferCaller) 
    	{
    		inferCaller();
    	}
    	return super.getSourceMethodName();
    }

	// Private method to infer the caller's class and method names
	private void inferCaller()
	{
		needToInferCaller = false;
		// Get the stack trace.
		StackTraceElement stack[] = (new Throwable()).getStackTrace();
		// First, search back to a method in the Logger class.
		int ix = 0;
		while(ix < stack.length)
		{
			StackTraceElement frame = stack[ix];
			String cname = frame.getClassName();
			if(cname.equals("java.util.logging.Logger") || cname.equals("jadex.base.LoggerWrapper"))
			{
				break;
			}
			ix++;
		}
		// Now search for the first frame before the "Logger" class.
		while(ix < stack.length)
		{
			StackTraceElement frame = stack[ix];
			String cname = frame.getClassName();
			if(!cname.equals("java.util.logging.Logger") && !cname.equals("jadex.base.LoggerWrapper"))
			{
				// We've found the relevant frame.
				setSourceClassName(cname);
				setSourceMethodName(frame.getMethodName());
				return;
			}
			ix++;
		}
		// We haven't found a suitable frame, so just punt. This is
		// OK as we are only committed to making a "best effort" here.
	}
}
