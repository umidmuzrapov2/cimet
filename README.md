# Semantic Code Clone Detection for Enterprise Applications

This project detect semantic code clones in enterprise applications.

## Prerequisites

* Maven 3.6+
* Java 11+

## Reproducing results

Running from the source:

Change directory in the console into the cloned project.

Run on Linux:

```shell script
./mvnw compile quarkus:dev -Dquarkus.args='/path/to/app/under/test,/path/to/output' -Ddebug=true
```

Run on Windows:

```shell script
mvnw.cmd compile quarkus:dev -Dquarkus.args='/path/to/app/under/test,/path/to/output' -Ddebug=true
```

Run on MacOS:

```shell script
sh ./mvnw compile quarkus:dev -Dquarkus.args='/path/to/app/under/test,/path/to/output' -Ddebug=true
```

There are 2 arguments specified in `-dDquarkus.args` separated by a comma. 

First is a path to the application that we want to analyze. We utilized project Train Ticket that can be accessed
here: [https://github.com/FudanSELab/train-ticket](https://github.com/FudanSELab/train-ticket)

Second argument is a path to a directory where the output of the application is going to be stored.

Note: do not set second path inside this project, it will not compile

## Results

Results are stored under /data directory.

It contains:

* module-pair-clones.txt: data on how the microservices are similar to the others
* per module clones.txt: data on how many CFGs are contained within the microservice and what percentage
  of those CFGs are clones
