## Chaos Injection Lambda
This blog demonstrates an approach in injecting chaos in Lambda functions without making any change to the Lambda function code. This blog uses the AWS Fault Injection Simulator (FIS) service to create experiments that inject disruptions for Lambda based serverless applications. The sample code in this blog introduces random disruptions to existing Lambda functions - like an increase in response times (latency) or random failures. 

This sample creates an FIS experiment that uses Lambda layers to inject disruptions. The Lambda layer contains the fault injection tooling. It is invoked prior to invocation of the Lambda function and injects random latencies or error. Injecting random latency simulates real world unpredictable conditions. The FIS experiment configures the Lambda function to use the fault injection layer using an AWS System Manager Document using an aws:ssm:start-automation-execution action. Once the experiment is complete, another AWS System Manager document rolls back the layer’s attachment to the Lambda function. This design allows for the chaos experiment to be used for existing serverless applications, without changing the existing Lambda function. 

In the sample code provided here , Lambda layers are available for Python, Node.js and Java runtimes. 

The Java layer uses the in-process JAVA_TOOL_OPTIONS mechanism to invoke the layer. This layer uses Java’s premain method and the Byte Buddy library for modifying the lambda function’s Java class during runtime. When the Lambda function is invoked, the JVM uses the class specified with the javaagent parameter and invokes its premain method before the lambda function’s handler invocation. The Java premain method allows for layer invocation each time prior to the Lambda function’s invocation phase. The addition of layer and setting up environment variables is done by the FIS experiment. 

In Python and Node.js layers, the Lambda function’s handler is replaced with the handler of the respective layers by the FIS aws:ssm:start-automation-execution action. The automation changes the Lambda function handler to a function in the layer. The layer function has the logic to inject chaos. At runtime, the layer function is invoked, injecting chaos in the Lambda. The layer function then invokes the original Lambda function’s handler, so that original functionality is fulfilled. 

The result is invocation of original Lambda function with latency or error injected. The observed changes are random latency or failure, which are logged. 


## Getting started
This code provides a way to inject chaos in Lambda functions using Lambda layers and AWS FIS. 

### Prerequisites 
To deploy this code first 
- Clone this repository. 
- Create a S3 bucket in the region you want to deploy the code. 

### Build
- To build the sample Java lambda function, and the associated Lambda layer:
  - run ``` mvn clean package ``` in each of the below paths: 
    - ./lambdaFunctions/java
    - ./layers/java

This will download the required dependencies to compile the Java code. This would create two jars - ``` javaFunction.jar ``` and  ``` chaosLambdaLayer.jar ```. Once the jars have been built, for the Lambda layer jar, compress the jar into a zip archive 
  - run: ``` zip javaLayer.zip chaosLambdaLayer.jar ``` 

- Also zip the Node.Js and Python Lambda functions:
  - change directory to the ./lambdaFunctions/node path and run: ``` zip node.zip index.js ``` 
  - change directory to the ./lambdaFunctions/python path and run: ``` zip python.zip lambda_function.py ```

- Similarly zip the Node.Js and Python Lambda functions Layers
  - change directory to the ./layers/node path and run: ``` zip nodeLayer.zip layer.js ```
  - In case of python, the zip file needs to contain the directory, hence change directory to the ./layers/ path and 
    - run: ``` zip -r pythonLayer.zip python ```

### Copy to the S3 bucket
- Upload these six files including the one .jar and five .zip files to the s3 bucket:
  - ``` javaFunction.jar ```
  - ``` javaLayer.zip ```
  - ``` node.zip ```
  - ``` nodeLayer.zip ```
  - ``` python.zip ```
  - ``` pythonLayer.zip ```
- ***Note - upload only the files and not the folder as well. Your S3 bucket should have these 6 files*** 

### Run CloudFormation
- Run the CloudFormation file 
  - Change the parameters in the runCfn.sh file 
    - Change the bucket name 
    - Change the stack name if needed 
    - run the command ``` ./runCfn.sh ``` file. 

```
aws cloudformation create-stack --stack-name myChaosStack --template-body file://cfnChaos.yml --parameters ParameterKey=JavaFileName,ParameterValue=javaFunction.jar ParameterKey=JavaLayerFileName,ParameterValue=javaLayer.zip ParameterKey=PythonFileName,ParameterValue=python.zip ParameterKey=PythonLayerFileName,ParameterValue=pythonLayer.zip ParameterKey=NodeFileName,ParameterValue=node.zip ParameterKey=NodeLayerFileName,ParameterValue=nodeLayer.zip ParameterKey=LambdaS3Bucket,ParameterValue=mychaosbucket ParameterKey=UpdateLambdaWithSSMAutomationRoleParam,ParameterValue=UpdateLambdaWithSSMAutomationRoleChaos ParameterKey=DeploySampleLambda,ParameterValue=Yes ParameterKey=FISRoleName,ParameterValue=FISRoleChaos ParameterKey=LambdaExecutionRoleName,ParameterValue=SampleLambdaExecutionRoleChaos ParameterKey=ChaosDocumentName,ParameterValue=InjectLambdaChaos --capabilities CAPABILITY_NAMED_IAM

```
Wait for CloudFormation to provision all resources. 

### Run the FIS experiment 
By default, the experiment is configured to inject chaos in Java sample Lambda function. To change it to Python or Node.js follow steps below 
- Copy the output value of the “PythonChaosInjectionParam” from the CloudFormation stack that you created. 
- Edit the FIS experiment template:
  - Open the AWS FIS console at https://console.aws.amazon.com/fis/
  - Select experiment template “fisChaosInjection” and from actions choose Update Experiment Template. Edit action InjectChaos.
  - Paste the value of the “PythonChaosInjectionParam” parameter copied in the previous step in the “Document Parameters - optional” field.  Click Save, and then click "Update experiment template”.
  This will inject chaos in the sample Python function. For Node function, the parameter is "NodeChaosInjectionParam"
- Start the Experiment - On the details page for the template “fisChaosInjection”, choose Actions, Start. Choose Start experiment. When prompted for confirmation, enter start and choose Start experiment.
- Wait till the experiment state changes to “Completed”. 

### Run your application testing experiment 

At this stage, all elements are in place to inject chaos into your Lambda function. Execute Lambda functions and observe application’s behaviour. Invoke the lambda functions using the below command: 

```
aws lambda invoke --function-name NodeChaosInjectionExampleFn out --log-type Tail --query 'LogResult' --output text | base64 -d
```

### To roll back the experiment
- Navigate to the Systems Manager Console, go to documents menu option and locate the document called ChaosDocument-Rollback. 

- Click on the document, and click on Execute Automation. Provide the value for the parameter FunctionName for the lambda function from which to remove the chaos injection layer. Click on Execute. This rolls back the Lambda function to the state before chaos injection. 

### To roll back the experiment
 Cleaning up
To avoid incurring future charges, delete all resources.
•	Clean up resources created by the CloudFormation template by running the below AWS CLI command. Update the stack name to the name you provided when creating the stack. 
```
aws cloudformation delete-stack --stack-name myChaosStack
```

## Conclusion
This sample code provides an approach for testing reliability and resilience in Lambda functions using chaos engineering. The approach allows for injecting chaos in Lambda functions without changing Lambda function code, with clear segregation of chaos injection and functionality. It provides a way for developers to focus on building business functionality using Lambda functions. The Lambda layers that inject chaos can be developed and managed separately. This approach leverages AWS FIS to run experiments that inject chaos using Lambda layers and test serverless application’s performance and resiliency. The insights from FIS experiment can be used to finding, fixing, or documenting the risks that surface in the application.
