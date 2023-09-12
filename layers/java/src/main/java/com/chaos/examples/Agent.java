package com.chaos.examples;

import java.lang.instrument.Instrumentation;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.amazonaws.services.lambda.runtime.RequestHandler;

import net.bytebuddy.agent.builder.AgentBuilder.Default;
import net.bytebuddy.agent.builder.AgentBuilder.InitializationStrategy.SelfInjection.Eager;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

public class Agent {
    public static void premain(String arguments,Instrumentation instrumentation) {
    	DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS Z");
    	
    	System.out.println(" ******* Agent started at " + dateFormat.format(new Date(System.currentTimeMillis())) + " ******** ");
		new Default().with(new Eager())
		.type(ElementMatchers.isSubTypeOf(RequestHandler.class))
				.transform((builder, typeDescription, classLoader, module) ->
				//builder.method(ElementMatchers.any())
				builder.method(ElementMatchers.nameContains("handleRequest"))
				.intercept(Advice.to(ChaosInstrumentation.class)))
		        .installOn(instrumentation);
		System.out.println(" ******* Agent ended at " + dateFormat.format(new Date(System.currentTimeMillis())) + " ******** ");
		
}
}