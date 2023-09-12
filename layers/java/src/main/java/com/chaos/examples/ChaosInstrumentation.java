package com.chaos.examples;

import net.bytebuddy.asm.Advice;

public class ChaosInstrumentation {
	    @Advice.OnMethodEnter
	    static long enter(@Advice.Origin String method) throws Exception {
	    	System.out.println(" ******* Starting method chaos injection ******* ");
	    	long chaosDuration = (long)(Math.random() * 3000);
	    	
	    	if (chaosDuration % 4 == 0)
	    	{
	            System.out.println(" ******* Terminating, encountered error (simulated error) *******");
	            System.exit(-1);
	    	}	    		
	    	else 
	    	{
	    		Thread.sleep(chaosDuration);
	    	}
	    	return chaosDuration;
	    }

	    @Advice.OnMethodExit
	    static void exit(@Advice.Origin String method, @Advice.Enter long chaosDuration) throws Exception {
	        System.out.println(" ******* Hello! " + chaosDuration + "ms random chaos added to " + method + " ******* ");
	    	System.out.println(" ******* Method chaos injection completed ******* ");
	    }
}