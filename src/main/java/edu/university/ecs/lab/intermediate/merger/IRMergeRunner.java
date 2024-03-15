package edu.university.ecs.lab.intermediate.merger;

import edu.university.ecs.lab.intermediate.merger.models.Delta;
import edu.university.ecs.lab.intermediate.merger.models.MsSystem;
import edu.university.ecs.lab.intermediate.merger.utils.IRParserUtils;

import java.io.IOException;
import java.util.List;

public class IRMergeRunner {
  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      System.err.println(
          "Invalid # of args, 2 expected: <path/to/intermediate-json> <path/to/delta-json>");
      return;
    }

    MsSystem msSystem = IRParserUtils.parseIRSystem(args[0]);
    List<Delta> deltas = IRParserUtils.parseDelta(args[1]);

    System.out.println(msSystem);
    System.out.println(deltas);

    // TODO: aggregate delta change objects into list
    // TODO: update IR with change list
    // TODO: increment system version
    // TODO: save new system representation
  }
}
