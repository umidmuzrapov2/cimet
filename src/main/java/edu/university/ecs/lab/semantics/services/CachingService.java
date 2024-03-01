package edu.university.ecs.lab.semantics.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import edu.university.ecs.lab.semantics.models.*;


@AllArgsConstructor
public class CachingService {
    // Generally if default path is used, we will read from location we write to
    private static final String DEFAULT_WRITE_PATH = "\\output";
    private static final String PROJECT_PATH =  System.getProperty("user.dir");
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

    /**
     * Parse saved cache files and loads them back into Cache
     *
     * @param cachePath the path to where cache files are held
     */
//    public void loadCache(String cachePath) {
//
//        this.path = cachePath;
//
//        Gson gson = new Gson();
//
//        String data = readDataIntoString("msModulesList");
//        Type listOfMyClassObject = new TypeToken<ArrayList<String>>() {}.getType();
//        List<String> msModulesList = gson.fromJson(data, listOfMyClassObject);
//        Cache.modules = msModulesList;
//
//        data = readDataIntoString("msClassList");
//        listOfMyClassObject = new TypeToken<ArrayList<Class>>() {}.getType();
//        List<JClass> msClassList = gson.fromJson(data, listOfMyClassObject);
//        Cache.classList = msClassList;
//
//        data = readDataIntoString("msMethodList");
//        listOfMyClassObject = new TypeToken<ArrayList<Method>>() {}.getType();
//        List<Method> msMethodList = gson.fromJson(data, listOfMyClassObject);
//        Cache.msMethodList = msMethodList;
//
//        data = readDataIntoString("msMethodCallList");
//        listOfMyClassObject = new TypeToken<ArrayList<MsMethodCall>>() {}.getType();
//        List<MsMethodCall> msMethodCallList = gson.fromJson(data, listOfMyClassObject);
//        Cache.msMethodCallList = msMethodCallList;
//
//        data = readDataIntoString("msRestCallList");
//        listOfMyClassObject = new TypeToken<ArrayList<RestCall>>() {}.getType();
//        List<RestCall> msRestCallList = gson.fromJson(data, listOfMyClassObject);
//        Cache.msRestCallList = msRestCallList;
//
//        data = readDataIntoString("msFieldList");
//        listOfMyClassObject = new TypeToken<ArrayList<MsField>>() {}.getType();
//        List<MsField> msFieldList = gson.fromJson(data, listOfMyClassObject);
//        MsCache.msFieldList = msFieldList;
//
//        data = readDataIntoString("msFlowList");
//        listOfMyClassObject = new TypeToken<ArrayList<MsFlowEntity>>() {}.getType();
//        List<MsFlowEntity> msFlowEntities = gson.fromJson(data, listOfMyClassObject);
//        MsCache.msFlows = msFlowEntities;
//    }

    /**
     * Utility function for reading json file into string format
     *
     * @param name the name of the file
     * @return string representation of file data
     */
//    public String readDataIntoString(String name) {
//        String s = null;
//        try {
//            s = Files.readString(Paths.get(path + name + ".json"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return s;
//    }

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
//        writeArrayList("FlowList", MsCache.msFlows);
//        writeMap("msDependentsList", MsCache.msDependents);
//        writeArrayList("msExtendedDependentsList", MsCache.msExtendedDependents);
    }
}
