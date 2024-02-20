package edu.university.ecs.lab.common.config;

import lombok.Getter;

@Getter
public class Microservice {
    private String repoUrl;
    private String baseCommit;
    private String endCommit;
    private String[] paths;
}
