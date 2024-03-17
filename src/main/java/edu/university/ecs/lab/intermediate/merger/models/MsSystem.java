package edu.university.ecs.lab.intermediate.merger.models;

import com.google.gson.annotations.SerializedName;
import edu.university.ecs.lab.rest.calls.models.MsModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class MsSystem {
  private String systemName;
  private String version;

  @SerializedName("services")
  private List<MsModel> msList;

  public Map<String, MsModel> getServiceMap() {
    Map<String, MsModel> msMap = new LinkedHashMap<>();

    for (MsModel model : msList) {
      msMap.put(model.getId(), model);
    }

    return msMap;
  }
}
