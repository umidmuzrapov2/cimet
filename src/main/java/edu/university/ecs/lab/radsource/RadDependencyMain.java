package edu.university.ecs.lab.radsource;

import edu.university.ecs.lab.radsource.mscontext.MsModel;
import edu.university.ecs.lab.radsource.utils.Facade;

import java.io.IOException;
import java.util.List;

public class RadDependencyMain {
  public static void main(String[] args) throws IOException {
    MsModel msModel = Facade.getMsModel(List.of(args));
    System.out.println(msModel.toString());
  }
}
