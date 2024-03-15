package edu.university.ecs.lab.intermediate.merger.models;

import com.google.gson.annotations.SerializedName;
import edu.university.ecs.lab.rest.calls.models.MsModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
public class MsSystem {
  private String systemName;
  private String version;

  @SerializedName("services")
  private List<MsModel> msList;
}
