package com.chaos.examples;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

// Handler value: example.HandlerString
public class App implements RequestHandler<Object, String>{
	
	DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS Z"); 
  
	@Override
	public String handleRequest(Object input, Context context)
	{
		LambdaLogger logger = context.getLogger();
		// process event
		logger.log("EVENT: " + input);
		logger.log("EVENT TYPE: " + input.getClass().toString());
		return "Hello! main lambda completed at " + dateFormat.format(new Date(System.currentTimeMillis()));
	}
}