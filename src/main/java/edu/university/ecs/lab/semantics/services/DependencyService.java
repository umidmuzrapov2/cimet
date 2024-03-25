package edu.university.ecs.lab.semantics.services;

import com.github.javaparser.utils.Pair;
import edu.university.ecs.lab.semantics.models.Method;
import edu.university.ecs.lab.semantics.models.RestCall;

import java.util.*;
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

  public static LinkedHashSet<String> getDependenciesList(
      List<Pair<RestCall, Method>> rawDependencies) {
    LinkedHashSet<String> returnSet = new LinkedHashSet<>();

    for (Pair<RestCall, Method> p : rawDependencies) {
      if (p.b != null) {
        returnSet.add(p.a.getId().getProject() + " " + p.b.getId().getProject());
      }
    }

    return returnSet;
  }

  public static Map<String, Set<String>> getDependenciesMap(
      List<Pair<RestCall, Method>> rawDependencies) {
    Map<String, Set<String>> returnMap = new HashMap<>();

    for (Pair<RestCall, Method> p : rawDependencies) {
      if (p.b != null) {
        if (!returnMap.containsKey(p.a.getId().getProject())) {
          returnMap.put(
              p.a.getId().getProject(), new LinkedHashSet<>(Set.of(p.b.getId().getProject())));
        } else {
          returnMap.get(p.a.getId().getProject()).add(p.b.getId().getProject());
        }

        if (!returnMap.containsKey(p.b.getId().getProject())) {
          returnMap.put(
              p.b.getId().getProject(), new LinkedHashSet<>(Set.of(p.a.getId().getProject())));
        } else {
          returnMap.get(p.b.getId().getProject()).add(p.a.getId().getProject());
        }
      }
    }

    return returnMap;
  }

  public static void compareTwoMaps(Map<String, Set<String>> map1, Map<String, Set<String>> map2) {
    //        Map<String, Set<String>> returnMap = new HashMap<>();

    for (Map.Entry<String, Set<String>> e1 : map1.entrySet()) {
      if (!map2.containsKey(e1.getKey())) {
        System.out.println("New Dependency realized: " + e1.getKey() + " All vals?");
      }

      for (String valueOf1 : e1.getValue()) {
        if (!map2.containsKey(valueOf1)) {
          System.out.println("New Dependency realized: " + e1.getKey() + " " + valueOf1);
        } else if (!map2.get(valueOf1).contains(e1.getKey())) {
          System.out.println("New Dependency realized: " + e1.getKey() + " " + valueOf1);
        }
      }
    }

    //        return returnMap;
  }
}
