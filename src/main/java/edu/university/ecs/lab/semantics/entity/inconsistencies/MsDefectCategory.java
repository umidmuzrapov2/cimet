package edu.university.ecs.lab.semantics.entity.inconsistencies;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MsDefectCategory  {
    private String name;
    private List<MsDefectSubcategories> msDefectSubcategories;
}