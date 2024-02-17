# Prophet Microservice System Evolution Tool

This project tracks microservice system evolution changes across repositories.


## Prerequisites

* Maven 3.6+
* Java 11+ (11 Recommended) 

## Reproducing results 

- To compile:
    ``mvn clean install -DskipTests``

- You can generate the files two ways, either just a comma seperated list of repositories or you can specify a directory for the cloned files and one for the output files:
    - Either just a comma seperated list of repositories
        - ```java -jar target/semantics-1.0-SNAPSHOT-runner.jar 'https://github.com/<owner>/<repo1>.git,https://github.com/<owner>/<repo2>.git,...,https://github.com/<owner>/<repoN>.git'```
    - Or you can specify a directory for the cloned files and one for the output files
        - ```java -jar target/semantics-1.0-SNAPSHOT-runner.jar 'your-repo-dest-folder/' 'https://github.com/<owner>/<repo1>.git,https://github.com/<owner>/<repo2>.git,...,https://github.com/<owner>/<repoN>.git' 'your-results-folder/'```

- To run the server:
```java -jar target/semantics-1.0-SNAPSHOT-runner.jar```
