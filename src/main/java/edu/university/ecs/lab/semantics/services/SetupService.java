package edu.university.ecs.lab.semantics.services;

import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.config.InputConfig;
import edu.university.ecs.lab.common.config.InputRepository;
import edu.university.ecs.lab.intermediate.create.services.GitCloneService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SetupService {

  public static InputConfig loadConfig(String configPath) throws Exception {
    InputConfig config = ConfigUtil.validateConfig(configPath);
    initConfigPaths(config);
    return ConfigUtil.validateConfig(configPath);
  }

  private static void initConfigPaths(InputConfig inputConfig) {
    File outputFilePath = new File(inputConfig.getOutputPath());
    File clonePathFile = new File(inputConfig.getClonePath());

    if (!outputFilePath.exists()) {
      boolean success = outputFilePath.mkdirs();
      if (!success) {
        throw new RuntimeException("Failed to create directory " + outputFilePath);
      }
    }

    if (!clonePathFile.exists()) {
      boolean success = clonePathFile.mkdirs();
      if (!success) {
        throw new RuntimeException("Failed to create directory " + clonePathFile);
      }
    }
  }

  public static void cloneRepositories(InputConfig inputConfig) throws Exception {
    GitCloneService cloneService = new GitCloneService(inputConfig.getClonePath());

    for (InputRepository inputRepository : inputConfig.getRepositories()) {
      cloneService.cloneRemote(inputRepository);
    }
  }

  // The local repo is what we will use for the base commit for now. It's current state is the base
  // and we will just commit it to the current state of the remote counterpart
//  public static Map<String, List<String>> detectChangedFiles(Repository localRepo, String next) throws Exception {
//    Map<String, List<String>> commitAndChangedFiles = null;
//
//    try (Git git = new Git(localRepo)) {
////      git.fetch().call();
//
//      ObjectId localMainHead = localRepo.resolve(Constants.HEAD);
//      ObjectId nextCommit = localRepo.resolve(next);
////      ObjectId remoteMainHead = localRepo.resolve("refs/remotes/origin/main");
//
//      AbstractTreeIterator oldTreeParser = new CanonicalTreeParser(null, localRepo.newObjectReader(), parent.getTree().getId());
//      AbstractTreeIterator newTreeParser = new CanonicalTreeParser(null, localRepo.newObjectReader(), commit.getTree().getId());
//
//      // List changed files between this commit and its parent
//      List<DiffEntry> diffs = git.diff()
//              .setOldTree(oldTreeParser)
//              .setNewTree(newTreeParser)
//              .call();
//
//      commitAndChangedFiles = new HashMap<>();
//      RevWalk revWalk = new RevWalk(localRepo);
//      RevCommit parent = revWalk.parseCommit(commit.getParent(0).getId());
//
//      try (DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
//        commitAndChangedFiles.put(commit.getName(), new ArrayList<>());
//        System.out.println("Diffs for Commit " + commit.getName() + " from " + parent.getName());
//        formatter.setRepository(localRepo);
//        for (DiffEntry entry : diffs) {
//          commitAndChangedFiles.get(commit.getName()).add(entry.getNewPath());
//          System.out.println("Changed file: " + entry.getNewPath());
//        }
//      }

      // Check if local 'main' is ahead of remote 'main'
//      List<RevCommit> localCommits = new ArrayList<>();
//      try (RevWalk walk = new RevWalk(localRepo)) {
//        walk.markStart(walk.parseCommit(localMainHead));
////        walk.markUninteresting(walk.parseCommit(remoteMainHead));
//        for (RevCommit commit : walk) {
//          localCommits.add(commit);
//        }
//      }

//      if (!localCommits.isEmpty()) {
//        throw new RuntimeException("Error: Local 'main' is ahead of remote 'main'.");
//      }

      // List files changed between local HEAD and remote HEAD
//      try (RevWalk revWalk = new RevWalk(localRepo)) {
//        RevCommit localHead = revWalk.parseCommit(localMainHead);
////        RevCommit remoteHead = revWalk.parseCommit(remoteMainHead);
//
//        revWalk.markStart(remoteHead);
//        revWalk.markUninteresting(localHead);
//        List<RevCommit> commitsToCheck = new ArrayList<>();
//        for (RevCommit commit : revWalk) {
//          commitsToCheck.add(commit); // Collect commits that are in local but not in remote
//        }
//
//        // We will analyze change starting at the oldest (1st after the base commit in local)
//        Collections.reverse(commitsToCheck);
//
//        for (RevCommit commit : commitsToCheck) {
//          System.out.println("Commit: " + commit.getId().getName() + " - " + commit.getShortMessage());
//          if (commit.getParentCount() > 0) {
//            // Compare with the first parent, as we are interested in the changes introduced by this commit
//            RevCommit parent = revWalk.parseCommit(commit.getParent(0).getId());
//
//            AbstractTreeIterator oldTreeParser = new CanonicalTreeParser(null, localRepo.newObjectReader(), parent.getTree().getId());
//            AbstractTreeIterator newTreeParser = new CanonicalTreeParser(null, localRepo.newObjectReader(), commit.getTree().getId());
//
//            // List changed files between this commit and its parent
//            List<DiffEntry> diffs = git.diff()
//                    .setOldTree(oldTreeParser)
//                    .setNewTree(newTreeParser)
//                    .call();
//
//            commitAndChangedFiles = new HashMap<>();
//
//            try (DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
//              commitAndChangedFiles.put(commit.getName(), new ArrayList<>());
//              System.out.println("Diffs for Commit " + commit.getName() + " from " + parent.getName());
//              formatter.setRepository(localRepo);
//              for (DiffEntry entry : diffs) {
//                commitAndChangedFiles.get(commit.getName()).add(entry.getNewPath());
//                System.out.println("Changed file: " + entry.getNewPath());
//              }
//            }
//          } else {
//            System.out.println("This is the first commit in the repository.");
//          }
//          System.out.println(); // Separate the log for each commit
//        }
//      }

//    } catch (IOException | GitAPIException e) {
//      e.printStackTrace();
//    }
//
//    return commitAndChangedFiles;
//
//  }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, ObjectId objectId) throws IOException {
      try (RevWalk walk = new RevWalk(repository)) {
        RevCommit commit = walk.parseCommit(objectId);
        ObjectId treeId = commit.getTree().getId();
        try (ObjectReader reader = repository.newObjectReader()) {
          return new CanonicalTreeParser(null, reader, treeId);
        }
      }
    }

    // Example of parsing where old is the head and new is the next commit
    public static List<String> changes(Repository repository, String commitHash) {
      List<String> changedFiles = new ArrayList<>();
      try (RevWalk walk = new RevWalk(repository)) {

        ObjectId headId = repository.resolve("HEAD");
        ObjectId commitId = ObjectId.fromString(commitHash);

        RevCommit headCommit = walk.parseCommit(headId);
        RevCommit laterCommit = walk.parseCommit(commitId);

        try (Git git = new Git(repository)) {
          List<DiffEntry> diffs = git.diff()
                  .setOldTree(prepareTreeParser(repository, headCommit))
                  .setNewTree(prepareTreeParser(repository, laterCommit))
                  .call();

          try (DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            formatter.setRepository(repository);
            formatter.setContext(0); // Adjust context lines as needed
            for (DiffEntry entry : diffs) {
              changedFiles.add(entry.getNewPath());
            }
          }
        }

      } catch (IOException | GitAPIException e) {
        e.printStackTrace();
      }

      return changedFiles;
    }

}
