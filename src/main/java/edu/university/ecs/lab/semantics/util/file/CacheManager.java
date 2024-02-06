package edu.university.ecs.lab.semantics.util.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import edu.university.ecs.lab.semantics.entity.*;
import edu.university.ecs.lab.semantics.entity.graph.*;
import edu.university.ecs.lab.semantics.util.MsCache;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Manages the reading and writing of MsCache to files */
public class CacheManager {

  private String path = "C:\\git\\data\\";

  /**
   * Writes all cache to files
   *
   * @param path the path to where the data will be written
   */
  public void persistCache(String path) {
    this.path = path;
    writeArrayList("msClassList", MsCache.msClassList);
    writeArrayList("msMethodList", MsCache.msMethodList);
    writeArrayList("msMethodCallList", MsCache.msMethodCallList);
    writeArrayList("msRestCallList", MsCache.msRestCallList);
    writeArrayList("msFieldList", MsCache.msFieldList);
    writeArrayList("msModulesList", MsCache.modules);
    writeArrayList("msFlowList", MsCache.msFlows);
  }

  /**
   * Utility function for writing an arrayList to a file in json
   *
   * @param <T> method template
   * @param name the name of the file that will be written
   * @param list the templated type of data held in arraylist
   */
  public <T> void writeArrayList(String name, List<T> list) {
    System.err.println(name);
    try (FileWriter writer = new FileWriter(path + "/" + name + ".json");
        BufferedWriter bw = new BufferedWriter(writer)) {
      Gson gson =
          new GsonBuilder().serializeSpecialFloatingPointValues().setPrettyPrinting().create();
      String jsonString = gson.toJson(list);
      bw.write(jsonString);
    } catch (IOException e) {
      System.err.format("IOException: %s%n", e);
    }
  }

  /**
   * fuction for parsing saved cache files and loading them back into MsCache
   *
   * @param cachePath the path to where cache files are held
   */
  public void recreateCache(String cachePath) {

    this.path = cachePath;

    Gson gson = new Gson();

    String data = readDataIntoString("msModulesList");
    Type listOfMyClassObject = new TypeToken<ArrayList<String>>() {}.getType();
    List<String> msModulesList = gson.fromJson(data, listOfMyClassObject);
    MsCache.modules = msModulesList;

    data = readDataIntoString("msClassList");
    listOfMyClassObject = new TypeToken<ArrayList<MsClass>>() {}.getType();
    List<MsClass> msClassList = gson.fromJson(data, listOfMyClassObject);
    MsCache.msClassList = msClassList;

    data = readDataIntoString("msMethodList");
    listOfMyClassObject = new TypeToken<ArrayList<MsMethod>>() {}.getType();
    List<MsMethod> msMethodList = gson.fromJson(data, listOfMyClassObject);
    MsCache.msMethodList = msMethodList;

    data = readDataIntoString("msMethodCallList");
    listOfMyClassObject = new TypeToken<ArrayList<MsMethodCall>>() {}.getType();
    List<MsMethodCall> msMethodCallList = gson.fromJson(data, listOfMyClassObject);
    MsCache.msMethodCallList = msMethodCallList;

    data = readDataIntoString("msRestCallList");
    listOfMyClassObject = new TypeToken<ArrayList<MsRestCall>>() {}.getType();
    List<MsRestCall> msRestCallList = gson.fromJson(data, listOfMyClassObject);
    MsCache.msRestCallList = msRestCallList;

    data = readDataIntoString("msFieldList");
    listOfMyClassObject = new TypeToken<ArrayList<MsField>>() {}.getType();
    List<MsField> msFieldList = gson.fromJson(data, listOfMyClassObject);
    MsCache.msFieldList = msFieldList;

    data = readDataIntoString("msFlowList");
    listOfMyClassObject = new TypeToken<ArrayList<MsFlowEntity>>() {}.getType();
    List<MsFlowEntity> msFlowEntities = gson.fromJson(data, listOfMyClassObject);
    MsCache.msFlows = msFlowEntities;
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
      s = Files.readString(Paths.get(path + name + ".json"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return s;
  }
}
