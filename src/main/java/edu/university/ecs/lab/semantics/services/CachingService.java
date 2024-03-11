package edu.university.ecs.lab.semantics.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import edu.university.ecs.lab.semantics.models.*;
import lombok.AllArgsConstructor;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CachingService {
  // Generally if default path is used, we will read from location we write to
  private static final String DEFAULT_WRITE_PATH = "\\out";
  private static final String PROJECT_PATH = System.getProperty("user.dir");
  private final String writePath, readPath;

  private static final Cache cache = new Cache();

  public CachingService() {
    writePath = readPath = PROJECT_PATH + DEFAULT_WRITE_PATH;
  }

  public <T> void writeArrayList(String name, List<T> list) {
    try (FileWriter writer = new FileWriter(writePath + "\\" + name + ".json");
        BufferedWriter bw = new BufferedWriter(writer)) {
      Gson gson =
          new GsonBuilder().serializeSpecialFloatingPointValues().setPrettyPrinting().create();
      String jsonString = gson.toJson(list);
      bw.write(jsonString);
    } catch (IOException e) {
      System.err.format("IOException: %s%n", e);
    }
  }

  /** Parse saved cache files and loads them back into Cache */
  public void loadCache() {
    Gson gson = new Gson();
    String data;

    data = readDataIntoString("ClassList");

    Type listOfMyClassObject;
    listOfMyClassObject = new TypeToken<ArrayList<JClass>>() {}.getType();
    List<JClass> classList = gson.fromJson(data, listOfMyClassObject);
    CachingService.getCache().setClassList(classList);

    data = readDataIntoString("MethodList");
    listOfMyClassObject = new TypeToken<ArrayList<Method>>() {}.getType();
    List<Method> methodList = gson.fromJson(data, listOfMyClassObject);
    CachingService.getCache().setMethodList(methodList);

    data = readDataIntoString("MethodCallList");
    listOfMyClassObject = new TypeToken<ArrayList<MethodCall>>() {}.getType();
    List<MethodCall> methodCallList = gson.fromJson(data, listOfMyClassObject);
    CachingService.getCache().setMethodCallList(methodCallList);

    data = readDataIntoString("RestCallList");
    listOfMyClassObject = new TypeToken<ArrayList<RestCall>>() {}.getType();
    List<RestCall> restCallList = gson.fromJson(data, listOfMyClassObject);
    CachingService.getCache().setRestCallList(restCallList);

    data = readDataIntoString("FieldList");
    listOfMyClassObject = new TypeToken<ArrayList<Field>>() {}.getType();
    List<Field> fieldList = gson.fromJson(data, listOfMyClassObject);
    CachingService.getCache().setFieldList(fieldList);

    data = readDataIntoString("FlowList");
    listOfMyClassObject = new TypeToken<ArrayList<Flow>>() {}.getType();
    List<Flow> flowList = gson.fromJson(data, listOfMyClassObject);
    CachingService.getCache().setFlowList(flowList);
  }

  /**
   * Utility function for reading json file into string format
   *
   * @param name the name of the file
   * @return string representation of file data
   */
  public String readDataIntoString(String name) {
    String s = null;
    try {
      s = Files.readString(Paths.get(readPath + "\\" + name + ".json"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return s;
  }

  /**
   * Get's the cache instance managed by CachingService
   *
   * @return "Singleton" Cache instance
   */
  public static Cache getCache() {
    return cache;
  }

  public void persistCache() {
    writeArrayList("ClassList", getCache().getClassList());
    writeArrayList("MethodList", getCache().getMethodList());
    writeArrayList("MethodCallList", getCache().getMethodCallList());
    writeArrayList("RestCallList", getCache().getRestCallList());
    writeArrayList("FieldList", getCache().getFieldList());
    //        writeArrayList("ModulesList", MsCache.modules);
    writeArrayList("FlowList", getCache().getFlowList());
    //        writeMap("msDependentsList", MsCache.msDependents);
    //        writeArrayList("msExtendedDependentsList", MsCache.msExtendedDependents);
  }

  public void clearCacheOfFile(String s) {
    // Clear all instances of cache entries that are a changed file...
    // TODO Check if it doesnt exist...
    // TODO this can all be optimized, we are searching all for each file in a loop right now
    getCache()
        .setClassList(
            getCache().getClassList().stream()
                .filter(x -> !Objects.equals(x.getId().getLocation(), s))
                .collect(Collectors.toList()));
    getCache()
        .setMethodList(
            getCache().getMethodList().stream()
                .filter(x -> !Objects.equals(x.getId().getLocation(), s))
                .collect(Collectors.toList()));
    getCache()
        .setMethodCallList(
            getCache().getMethodCallList().stream()
                .filter(x -> !Objects.equals(x.getId().getLocation(), s))
                .collect(Collectors.toList()));
    getCache()
        .setRestCallList(
            getCache().getRestCallList().stream()
                .filter(x -> !Objects.equals(x.getId().getLocation(), s))
                .collect(Collectors.toList()));
    getCache()
        .setFieldList(
            getCache().getFieldList().stream()
                .filter(x -> !Objects.equals(x.getId().getLocation(), s))
                .collect(Collectors.toList()));

    // Clear flows they will be rebuilt
    getCache().setFlowList(new ArrayList<>());
  }
}
