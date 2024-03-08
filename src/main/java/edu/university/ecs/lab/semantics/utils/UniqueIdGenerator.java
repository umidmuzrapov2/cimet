package edu.university.ecs.lab.semantics.utils;

import java.util.concurrent.atomic.AtomicLong;

public class UniqueIdGenerator {
  private static final AtomicLong counter = new AtomicLong(0);

  public static long getUniqueID() {
    return counter.incrementAndGet();
  }
}
