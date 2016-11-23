#SpringBatchSample

This is a simple sample project created as a playground for using Spring's Batch [FlowBuilder](http://docs.spring.io/spring-batch/apidocs/org/springframework/batch/core/job/builder/FlowBuilder.html) and [SplitBuilder](http://docs.spring.io/spring-batch/apidocs/org/springframework/batch/core/job/builder/FlowBuilder.SplitBuilder.html) it tests a flow that has a single entry point and later splits into two parallel flows. Having had hard time to find some examples for named builders thought it could be of use to someone else.

This is draft code, meaning by it's not something it might run in production nor has been deeply tested, rather it's a spring batch playground where I'll add different snippets related to the spring's batch stack.


## Technologies

The repository employs the following technologies:

- Java 8
- Spring Batch 
- HSQLDB
- JUnit
- Mockito
- Gradle

## Run

This project is not aimed to produce a deployable build, the only supported command is:

``gradle test`` 




