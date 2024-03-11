package edu.university.ecs.lab.semantics.services;

import com.github.javaparser.utils.Pair;
import edu.university.ecs.lab.semantics.models.Method;
import edu.university.ecs.lab.semantics.models.RestCall;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DependencyService {
  public static List<Pair<RestCall, Method>> getRawDependencies() {
    List dependencies = new ArrayList<>();
    Pair<Method, RestCall> pair;

    dependencies =
        CachingService.getCache().getRestCallList().stream()
            .filter(restCall -> restCall.getApiEndpoint() != null)
            .map(
                restCall -> {
                  Optional<Method> m =
                      CachingService.getCache().getMethodList().stream()
                          .filter(method -> method.getApiEndpoint() != null)
                          .filter(
                              method -> method.getApiEndpoint().equals(restCall.getApiEndpoint()))
                          .findFirst();

                  if (!m.isPresent()) {
                    return new Pair(restCall, null);
                  }
                  return new Pair(restCall, m.get());
                })
            .collect(Collectors.toList());

    return dependencies;
  }

  public static LinkedHashSet<String> getDependencies(
      List<Pair<RestCall, Method>> rawDependencies) {
    LinkedHashSet<String> returnSet = new LinkedHashSet<>();

    for (Pair<RestCall, Method> p : rawDependencies) {
      if (p.b != null) {
        returnSet.add(p.a.getId().getProject() + " " + p.b.getId().getProject());
      }
    }

    return returnSet;
  }
}
