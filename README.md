# Change Impact Microservice Evolution Tool

This project tracks microservice system evolution changes across repositories.


## Prerequisites

* Maven 3.6+
* Java 11+ (11 Recommended) 

## To Compile:
    ``mvn clean install -DskipTests``

## Extracting an Intermediate Representation:
- Run or compile the main method of ``IntermediateExtraction.java`` in the IDE of your choice or via the command line.
- Required parameters: ``/path/to/clone/folder 'comma,separated,list,of,remote,microservices' /path/to/output``

Sample output produced:
```json
{
  "systemName": "train-ticket-system",
  "version": "0.0.1",
  "services": [
    {
      "msName": "../repos/train-ticket-microservices",
      "endpoints": [
        {
          "api": "/api/v1/auth",
          "source-file": "../repos/train-ticket-microservices/ts-admin-service/src/main/java/com/cloudhubs/trainticket/adminservice/controller/AuthController.java",
          "type": "@RequestMapping"
        },
        {
          "api": "/hello",
          "source-file": "../repos/train-ticket-microservices/ts-admin-service/src/main/java/com/cloudhubs/trainticket/adminservice/controller/AuthController.java",
          "type": "@GetMapping"
        },
        ...
      ],
      "dependencies": [
        {
          "api": "/api/v1/auth",
          "source-file": "../repos/train-ticket-microservices/ts-assurance-service/src/main/java/com/cloudhubs/trainticket/assurance/service/impl/UserServiceImpl.java",
          "call-dest": "../repos/train-ticket-microservices/ts-admin-service/src/main/java/com/cloudhubs/trainticket/adminservice/controller/AuthController.java",
          "call-method": "exchange()"
        },
        {
          "api": "/users",
          "source-file": "../repos/train-ticket-microservices/ts-assurance-service/src/main/java/com/cloudhubs/trainticket/assurance/service/impl/UserServiceImpl.java",
          "call-dest": "../repos/train-ticket-microservices/ts-auth-service/src/main/java/com/cloudhubs/trainticket/auth/controller/AuthUserController.java",
          "call-method": "exchange()"
        },
        ...
      ]
    }
  ]
}
```

## Extracting a Delta Change Impact:
- Run or compile the main method of ``DeltaExtraction.java`` in the IDE of your choice or via the command line.
- Command line args list containing ``/path/to/repo(s)``

Sample output produced:
```json
{
    "local-file": "../train-ticket-microservices/.../AuthController.java",
    "remote-api": "https://api.github.com/repos/cloudhubs/train-ticket-microservices/contents/.../AuthController.java",
    "changes": [
      "line 5",
      ...
    ]
}
```