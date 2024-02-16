package edu.university.ecs.lab.radsource.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RestFlow {
    private RestCall client;
    private RestEndpoint endpoint;
}
