package edu.university.ecs.lab.semantics.entity.graph;

import lombok.Data;

import java.util.Arrays;

@Data
public class MsId {
    private String path;
    //first six
    private String directoryName;

    public MsId(String path) {
        this.setPath(path);
    }

    public void setPath(String path) {
//    	System.err.println(path);
    	this.path = path;
        String[] split = path.split("/");
    	try {
        String[] subarr = Arrays.asList(split)
                .subList(0, 6)
                .toArray(new String[0]);
//        directoryName = String.join("/", subarr);
    	} catch (IndexOutOfBoundsException e) {
    		System.err.println("Non-src folder");
    		String[] subarr = Arrays.asList(split)
                    .subList(0, split.length - 1)
                    .toArray(new String[0]);
            directoryName = String.join("/", subarr);
    	}
//    	System.err.println(directoryName);
//    	System.err.println("----");
//        System.out.printf("");
    }
}
