package edu.university.ecs.lab.semantics.entity.inconsistencies;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class MsInconsistencies {
    private MsDefectCategory msApi;
    private MsDefectCategory msService;
}
