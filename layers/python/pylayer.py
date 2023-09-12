import imp
import json
import boto3
import os
import random
import time
import sys

def layer_handler(event, context):
    # TODO implement
    # inject delay between 100-3000 ms 
    
    inject_delay()

    # find and invoke the old handler 
    
    function_name  = os.environ['AWS_LAMBDA_FUNCTION_NAME']
    #get parameters
    ssm = boto3.client('ssm')
    old_handler = ssm.get_parameter(Name= '/ChaosInjection/' + function_name + '_handler_ssmparam')['Parameter']['Value'].split('.') 
    #old_handler ='lambda_function.lambda_handler'.split('.')
    filename = old_handler[0]
    handler= old_handler[1]
    
    # find and invoke the old handler 
    file = None
    try:
        file, pathname, description = imp.find_module(filename) 
        module = imp.load_module(filename ,file , pathname, description)
        old_handler = getattr(module, handler)
        return old_handler(event, context)
    finally:
        if file is not None:
            file.close()
    
def inject_delay():
    random_delay = random.randint(1000, 3000)
    if random_delay % 4 == 0:
        sys.exit('Chaos injected failure')
    else:
        print ('Injecting delay for ' + str(random_delay) + ' ms' )
        time.sleep(random_delay/1000)
    
    