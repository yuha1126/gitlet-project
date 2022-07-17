package gitlet;

import com.sun.tools.internal.ws.wsdl.document.soap.SOAPUse;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayDeque;

public class Repo {
    /** The CWD directory. */
    private static final File CWD = new File(System.getProperty("user.dir"));
    /** The Git directory. */
    private static final File GIT_DIRECTORY = Utils.join(CWD, ".gitlet");
    /** The Commit directory. */
    private static final File COMMIT_DIR = Utils.join(GIT_DIRECTORY, "commits");
    /** The Staged directory. */
    private static final File STAGED_DIR = Utils.join(GIT_DIRECTORY, "staged");
    /** The Removal directory. */
    private static final File REMOVAL_DIR = Utils.join(
            GIT_DIRECTORY, "removal");
    /** The Blob directory. */
    private static final File BLOB_DIR = Utils.join(GIT_DIRECTORY, "blobs");
    /** The Branches directory. */
    private static final File BRANCHES_DIR = Utils.join(
            GIT_DIRECTORY, "branches");
    /** The HEAD directory. */
    private static final File HEAD_DIR = Utils.join(GIT_DIRECTORY, "HEAD");

    /** The Head. */
    private String head;
    /** The Branches. */
    private Branch branches;
    /** The magic number. */
    private int maxLength = 40;

    /* Constructor for the repo class. */
    public Repo() {
        head = "master";
    }

    /* Returns hashmap of branches. */
    public Branch returnBranches() {
        return branches;
    }

    /* Initializes the Repo. */
    public void init() {
        if (GIT_DIRECTORY.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
        } else {
            GIT_DIRECTORY.mkdir();
            CWD.mkdir();
            COMMIT_DIR.mkdir();
            STAGED_DIR.mkdir();
            BLOB_DIR.mkdir();
            BRANCHES_DIR.mkdir();
            HEAD_DIR.mkdir();
            REMOVAL_DIR.mkdir();
            Commit initCommit = new Commit();
            String initCommitSHA = initCommit.getSHA();
            File initCommitFile = Utils.join(COMMIT_DIR, initCommit.getSHA());
            try {
                initCommitFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Utils.writeObject(initCommitFile, initCommit);

            branches = new Branch(head, initCommitSHA);
            File branchFile = Utils.join(BRANCHES_DIR, head);
            try {
                branchFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Utils.writeObject(branchFile, branches);

            File headFile = Utils.join(HEAD_DIR, "head");
            try {
                headFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Utils.writeObject(headFile, (Serializable) head);
        }
    }


    /* Adds a file as it currently exists to the staging area. */
    public void add(String fileName) {

        File toAdd = Utils.join(CWD, fileName);
        File toAddRemoved = Utils.join(REMOVAL_DIR, fileName);
        File toAddStaged = Utils.join(STAGED_DIR, fileName);
        if (getCurrCommit().getMsg().equals("Merged master into given.")) {
            System.out.println(Utils.readContentsAsString(toAdd));
            System.out.println(getCurrCommit().getMsg());
            System.out.println(getCurrCommit().getFileBlob(fileName).getContentsString());
        }
        if (!toAdd.exists() && !toAddRemoved.exists()) {
            System.out.println("File does not exist.");
        } else if (toAddRemoved.exists()) {
            try {
                toAdd.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] toAddBytes = Utils.readContents(toAddRemoved);
            Utils.writeContents(toAdd, toAddBytes);
            toAddRemoved.delete();
        } else if (!getCurrCommit().commitContainsExact(fileName)) {
                byte[] toAddBytes = Utils.readContents(toAdd);
                Utils.writeContents(toAddStaged, toAddBytes);
            } else if (toAddStaged.exists()) {
                toAddStaged.delete();
        }
    }

    /* Saves a snapshot of tracked files in the current commit and staging
    area so they can be restored at a later time, creating a new commit. */
    public void commit(String commitMsg) {

        File[] stagedFiles = STAGED_DIR.listFiles();
        File[] removedFiles = REMOVAL_DIR.listFiles();
        if (stagedFiles.length == 0 && removedFiles.length == 0) {
            System.out.println("No changes added to the commit.");
        } else {
            Branch tempBranch = getCurrentBranch();
            String prevCommitSHA = tempBranch.getCommitSHA();
            Commit newCommit = new Commit(commitMsg, prevCommitSHA);
            for (File f : removedFiles) {
                newCommit.delBlob(f.getName());
            }
            for (File f : stagedFiles) {
                Blob fBlob = new Blob(f.getName(), CWD);
                File tempBlobFile = Utils.join(BLOB_DIR, fBlob.getSHA());
                try {
                    tempBlobFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Utils.writeObject(tempBlobFile, fBlob);
                newCommit.addBlob(f.getName(), fBlob.getSHA());
            }

            String newCommitSHA = newCommit.getSHA();
            File newCommitFile = Utils.join(COMMIT_DIR, newCommitSHA);
            Utils.writeObject(newCommitFile, newCommit);

            File branchFile = Utils.join(BRANCHES_DIR, getHEAD());
            tempBranch.editCommitSHA(newCommitSHA);
            Utils.writeObject(branchFile, tempBranch);

            clearStagedAndRemoved();
        }
    }

    /* Checkout the fileName in the current commit. */
    public void checkout(String fileName) {
        Commit currCommit = getCurrCommit();
        if (currCommit.commitContains(fileName)) {
            File beforeCheckout = Utils.join(CWD, fileName);
            Blob fileBlob = currCommit.getFileBlob(fileName);
            Utils.writeContents(beforeCheckout, fileBlob.getContents());
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    /* Checkout the fileName in the commitID commit. */
    public void checkout(String commitID, String fileName) {
        if (commitID.length() < maxLength) {
            commitID = abbreviatedID(commitID);
        }
        File commitFile = Utils.join(COMMIT_DIR, commitID);
        if (commitFile.exists()) {
            Commit thisCommit = getCommit(commitID);
            if (thisCommit.commitContains(fileName)) {
                File beforeCheckout = Utils.join(CWD, fileName);
                Blob fileBlob = thisCommit.getFileBlob(fileName);
                Utils.writeContents(beforeCheckout, fileBlob.getContents());
            } else {
                System.out.println("File does not exist in that commit.");
            }
        } else {
            System.out.println("No commit with that id exists.");
        }
    }

    /* Returns the full commitID for abbreviated IDs. */
    public String abbreviatedID(String commitID) {
        for (String s : COMMIT_DIR.list()) {
            if (s.startsWith(commitID)) {
                return s;
            }
        }
        return commitID;
    }

    /* Takes all files in the commit at the head of the given branch,
    and puts them in the working directory, overwriting the versions
    of the files that are already there if they exist. */
    public void checkoutBranch(String branchName) {
        boolean untrackedFileExists = false;
        for (String file : Utils.plainFilenamesIn(CWD)) {
            if (!isFileTracked(file)) {
                untrackedFileExists = true;
            }
        }
        if (untrackedFileExists) {
            System.out.println("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
        } else {
            clearStagedAndRemoved();
            File branchFile = Utils.join(BRANCHES_DIR, branchName);
            if (!branchFile.exists()) {
                System.out.println("No such branch exists.");
            } else if (getCurrentBranch().getName().equals(branchName)) {
                System.out.println("No need to checkout the current branch.");
            } else {
                Branch thisBranch = Utils.readObject(branchFile, Branch.class);
                File commitFile = Utils.join(COMMIT_DIR,
                        thisBranch.getCommitSHA());
                Commit thisCommit = Utils.readObject(commitFile, Commit.class);
                HashMap<String, String> blobs = thisCommit.getBlob();

                checkoutFilesInCommit(blobs);

                delMissingStagedinCommit(thisCommit);

                clearStagingArea();
                File headFile = Utils.join(HEAD_DIR, "head");
                Utils.writeObject(headFile, branchName);
            }
        }
    }

    /* Print a list of commits from most recent to earliest. */
    public void log() {
        Commit currCommit = getCurrCommit();
        String parentCommitSHA;
        while (currCommit.getParent() != null) {
            System.out.println("===");
            System.out.println("commit " + currCommit.getSHA());
            System.out.println("Date: " + currCommit.getDateformatted());
            System.out.println(currCommit.getMsg());
            System.out.println();
            parentCommitSHA = currCommit.getParent();
            File parentCommitFile = Utils.join(COMMIT_DIR,
                    parentCommitSHA);
            currCommit = Utils.readObject(parentCommitFile, Commit.class);
        }
        if (currCommit.getParent() == null) {
            System.out.println("===");
            System.out.println("commit " + currCommit.getSHA());
            System.out.println("Date: " + currCommit.getDateformatted());
            System.out.println(currCommit.getMsg());
        }
    }

    /* Prints a list of every commit ever made. */
    public void logGlobal() {
        List<String> commitList = Utils.plainFilenamesIn(COMMIT_DIR);
        Commit currCommit;
        for (String currCommitSHA : commitList) {
            File currCommitDir = Utils.join(COMMIT_DIR, currCommitSHA);
            currCommit = Utils.readObject(currCommitDir, Commit.class);
            System.out.println("===");
            System.out.println("commit " + currCommit.getSHA());
            System.out.println("Date: " + currCommit.getDateformatted());
            System.out.println(currCommit.getMsg());
            System.out.println();
        }
    }

    /* Unstages the file or stages it for removal. */
    public void rm(String fileName) {
        File tempFile = Utils.join(STAGED_DIR, fileName);
        if (tempFile.exists()) {
            tempFile.delete();
        } else if (getCurrCommit().commitContains(fileName)) {
            File workingFile = Utils.join(CWD, fileName);
            File rmFile = Utils.join(REMOVAL_DIR, fileName);
            try {
                rmFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (workingFile.exists()) {
                byte[] workingFileByte = Utils.readContents(workingFile);
                workingFile.delete();
                Utils.writeContents(rmFile, workingFileByte);
            }
        } else {
            System.out.println("No reason to remove the file.");
        }
    }

    /* Prints out the ids of all commits that have
     the given commit message, one per line. */
    public void find(String commitMsg) {
        List<String> commitList = Utils.plainFilenamesIn(COMMIT_DIR);
        Commit currCommit;
        boolean msgExists = false;
        for (String currCommitSHA : commitList) {
            File currCommitDir = Utils.join(COMMIT_DIR, currCommitSHA);
            currCommit = Utils.readObject(currCommitDir, Commit.class);
            if (currCommit.getMsg().equals(commitMsg)) {
                System.out.println(currCommitSHA);
                msgExists = true;
            }
        }
        if (!msgExists) {
            System.out.println("Found no commit with that message.");
        }
    }

    /* Creates a new branch with the given name,
    and points it at the current head node. */
    public void branch(String branchName) {
        File branchFile = Utils.join(BRANCHES_DIR, branchName);
        if (branchFile.exists()) {
            System.out.println("A branch with that name already exists.");
        } else {
            try {
                branchFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Branch newBranch = new Branch(branchName, getCurrCommit().getSHA());
            Utils.writeObject(branchFile, newBranch);
        }
    }

    /* Deletes the branch with the given name. */
    public void rmBranch(String branchName) {
        File branchFile = Utils.join(BRANCHES_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("branch with that name does not exist.");
        } else if (getCurrentBranch().getName().equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
        } else {
            branchFile.delete();
        }
    }

    /* Checks out all the files tracked by the given commit. Removes tracked
     files that are not present in that commit. */
    public void reset(String commitID) {
        if (commitID.length() < maxLength) {
            commitID = abbreviatedID(commitID);
        }
        boolean untrackedFileExists = false;
        for (String file : Utils.plainFilenamesIn(CWD)) {
            if (!isFileTracked(file)) {
                untrackedFileExists = true;
            }
        }
        File commitFile = Utils.join(COMMIT_DIR, commitID);
        if (untrackedFileExists) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
        } else if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
        } else {
            File thisCommitFile = Utils.join(COMMIT_DIR, commitID);
            Commit thisCommit = Utils.readObject(thisCommitFile, Commit.class);
            HashMap<String, String> blobs = thisCommit.getBlob();

            checkoutFilesInCommit(blobs);

            delMissingStagedinCommit(thisCommit);

            Branch currBranch = getCurrentBranch();
            currBranch.editCommitSHA(commitID);
            File currBranchFile = Utils.join(BRANCHES_DIR,
                    currBranch.getName());
            Utils.writeObject(currBranchFile, currBranch);

            clearStagedAndRemoved();
        }
    }

    /* Displays what branches currently exist, and marks
    the current branch with a *. */
    public void status() {
        if (!GIT_DIRECTORY.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        System.out.println("=== Branches ===");
        for (String s: Utils.plainFilenamesIn(BRANCHES_DIR)) {
            if (s.equals(getCurrentBranch().getName())) {
                System.out.println("*" + s);
            } else {
                System.out.println(s);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String s: Utils.plainFilenamesIn(STAGED_DIR)) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String s: Utils.plainFilenamesIn(REMOVAL_DIR)) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }


    /* Merges files from the given branch into the current branch. */
    public void merge(String branchName) {
        boolean mergeConflict = false;
        File givenBranchFile = Utils.join(BRANCHES_DIR, branchName);
        if (mergeSpecialCond(branchName, givenBranchFile))  {
            return;
        }
        Branch givenBranch = Utils.readObject(
                givenBranchFile, Branch.class);
        File givenCommitFile = Utils.join(COMMIT_DIR,
                givenBranch.getCommitSHA());
        Commit givenCommit = Utils.readObject(givenCommitFile,
                Commit.class);
        Commit currCommit = getCurrCommit();
        Commit splitPoint = getSplitPoint(givenCommit, currCommit);

        HashSet<String> everyFile = getEveryUniqueFile(
                currCommit.getBlob().keySet(),
                givenCommit.getBlob().keySet(),
                splitPoint.getBlob().keySet());
        if (mergeSpecialCond2(branchName,
                currCommit, givenCommit, splitPoint)) {
            return;
        }
        for (String s : everyFile) {
            File editFile = Utils.join(CWD, s);
            if (splitPoint.commitContains(s)) {
                mergeConflict = mergeHelper1(s, currCommit,
                        givenCommit, splitPoint, editFile);
            } else {
                mergeConflict = mergeHelper2(s, currCommit,
                        givenCommit, splitPoint, editFile);
            }
        }
        String commitMSG = "Merged " + branchName
                + " into " + getCurrentBranch().getName() + ".";
        mergeCommit(mergeConflict, commitMSG, currCommit, givenCommit);
    }

    /* Commit when there is a merge. */
    public void mergeCommit(boolean isConflict, String commitMSG,
                            Commit currCommit, Commit givenCommit) {
        File[] stagedFiles = STAGED_DIR.listFiles();
        File[] removedFiles = REMOVAL_DIR.listFiles();

        Commit newCommit = new Commit(commitMSG,
                currCommit.getSHA(), givenCommit.getSHA());
        String newCommitSHA = newCommit.getSHA();
        File newCommitFile = Utils.join(COMMIT_DIR, newCommitSHA);
        Utils.writeObject(newCommitFile, newCommit);

        for (String f : Utils.plainFilenamesIn(CWD)) {
            Blob fBlob = new Blob(f, CWD);
            File tempBlobFile = Utils.join(BLOB_DIR, fBlob.getSHA());
            try {
                tempBlobFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Utils.writeObject(tempBlobFile, fBlob);
            newCommit.addBlob(f, fBlob.getSHA());
        }

        for (File f : removedFiles) {
            newCommit.delBlob(f.getName());
        }

        for (File f : stagedFiles) {
            Blob fBlob = new Blob(f.getName(), CWD);
            File tempBlobFile = Utils.join(BLOB_DIR, fBlob.getSHA());
            try {
                tempBlobFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Utils.writeObject(tempBlobFile, fBlob);
            newCommit.addBlob(f.getName(), fBlob.getSHA());
        }

        Branch tempBranch = getCurrentBranch();
        tempBranch.editCommitSHA(newCommitSHA);
        File branchFile = Utils.join(BRANCHES_DIR, getHEAD());
        Utils.writeObject(branchFile, tempBranch);
        if (isConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    public boolean mergeSpecialCond(String branchName, File givenBranchFile) {
        boolean untrackedFileExists = false;
        for (String file : Utils.plainFilenamesIn(CWD)) {
            if (!isFileTracked(file)) {
                untrackedFileExists = true;
            }
        }
        if (untrackedFileExists) {
            System.out.println("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
            return true;
        } else if ((STAGED_DIR.list().length != 0)
                || (REMOVAL_DIR.list().length != 0)) {
            System.out.println("You have uncommitted changes.");
            return true;
        } else if (!givenBranchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return true;
        } else if (givenBranchFile.getName().equals(
                getCurrentBranch().getName())) {
            System.out.println("Cannot merge a branch with itself.");
            return true;
        } else {
            return false;
        }
    }

    public boolean mergeSpecialCond2(String branchName,
                     Commit currCommit, Commit givenCommit, Commit splitPoint) {
        if (splitPoint.getSHA().equals(givenCommit.getSHA())) {
            System.out.println("Given branch is an ancestor"
                    + " of the current branch.");
            return true;
        } else if (splitPoint.getSHA().equals(currCommit.getSHA())) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            return true;
        }
        if (splitPoint.equals(currCommit)) {
            checkoutBranch(branchName);
            return true;
        }
        return false;
    }

    public boolean mergeHelper1(String s, Commit currCommit,
                 Commit givenCommit, Commit splitPoint, File editFile) {
        if (currCommit.commitContainsExact(s, splitPoint)
                && givenCommit.
                commitContainsNotExact(s, splitPoint)) {
            Utils.writeContents(editFile,
                    givenCommit.getFileBlob(s).getContents());
            File tempFile = Utils.join(STAGED_DIR, s);
            try {
                tempFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Utils.writeContents(tempFile,
                    givenCommit.getFileBlob(s).getContents());
            return false;
        } else if (currCommit.commitContainsNotExact(s, splitPoint)
                && !givenCommit.commitContainsExact(s, splitPoint)) {
            String x = "hi";
            return false;
        } else if (currCommit.commitContainsExact(s, givenCommit)
                && currCommit.commitContainsNotExact(s, splitPoint)) {
            String x = "hi";
            return false;
        } else if (splitPoint.commitContains(s)
                && currCommit.commitContainsNotExact(s, splitPoint)
                && givenCommit.commitContainsNotExact(s, splitPoint)
                && currCommit.commitContainsNotExact(s, givenCommit)) {
            mergeFile(s, currCommit, givenCommit, splitPoint);
            return true;
        } else if ((currCommit.commitContainsNotExact(s, splitPoint)
                && !givenCommit.commitContains(s))
                || (!currCommit.commitContains(s)
                && givenCommit.commitContainsNotExact(s, splitPoint))) {
            mergeFile(s, currCommit, givenCommit, splitPoint);
            return true;
        }
        return false;
    }

    public boolean mergeHelper2(String s, Commit currCommit,
                    Commit givenCommit, Commit splitPoint, File editFile) {
        if (!splitPoint.commitContains(s)
                && currCommit.commitContainsNotExact(s, givenCommit)) {
            mergeFile(s, currCommit, givenCommit, splitPoint);
            return true;
        } else if (!splitPoint.commitContains(s)
                && currCommit.commitContains(s)) {
            return false;
        } else if (!splitPoint.commitContains(s)
                && givenCommit.commitContains(s)) {
            checkout(givenCommit.getSHA(), s);
            return false;
        } else if (splitPoint.commitContains(s)
                && currCommit.commitContainsExact(s, splitPoint)
                && !givenCommit.commitContains(s)) {
            editFile.delete();
            return false;
        } else if (splitPoint.commitContains(s)
                && !currCommit.commitContains(s)
                && givenCommit.commitContainsExact(s, splitPoint)) {
            return false;
        }
        return false;
    }


    /* Returns a HashSet with every unique file. */
    public HashSet<String> getEveryUniqueFile(
            Set<String> currFiles, Set<String> givenFiles,
            Set<String> splitPointFiles) {
        HashSet<String> everyFile = new HashSet<>();
        everyFile.addAll(currFiles);
        everyFile.addAll(givenFiles);
        everyFile.addAll(splitPointFiles);
        return everyFile;
    }

    /* Merges the contents of the files. */
    public void mergeFile(String s, Commit currCommit,
                          Commit givenCommit, Commit splitPoint) {
        File tempFile = Utils.join(CWD, s);
        File tempFileStaged = Utils.join(STAGED_DIR, s);
        String tempFileContent;
        if (splitPoint.commitContains(s) && currCommit.
                commitContainsNotExact(s, splitPoint)
                && givenCommit.commitContainsNotExact(s, splitPoint)
                && currCommit.commitContainsNotExact(s, givenCommit)) {
            tempFileContent = "<<<<<<< HEAD\n"
                    + currCommit.getFileBlob(s).getContentsString()
                    + "=======\n" + givenCommit.getFileBlob(s)
                    .getContentsString() + ">>>>>>>\n";
            Utils.writeContents(tempFile, tempFileContent);
        } else if ((currCommit.commitContainsNotExact(s, splitPoint)
                && !givenCommit.commitContains(s))
                || (!currCommit.commitContains(s)
                && givenCommit.commitContainsNotExact(s, splitPoint))) {
            tempFileContent = "<<<<<<< HEAD\n"
                    + currCommit.getFileBlob(s).getContentsString()
                    + "=======\n" + ">>>>>>>\n";
            Utils.writeContents(tempFile, tempFileContent);

        } else {
            tempFileContent = "<<<<<<< HEAD\n"
                    + currCommit.getFileBlob(s).getContentsString()
                    + "=======\n" + givenCommit.getFileBlob(s)
                    .getContentsString() + ">>>>>>>\n";
            Utils.writeContents(tempFile, tempFileContent);
        }
    }

    /* Returns the common ancestor of thisCommit and the current Commit. */
    public Commit getSplitPoint(Commit givenCommit, Commit currCommit) {
        Commit popped;
        Commit poppedParent;
        Commit poppedSecondParent;

        ArrayDeque<Commit> givenCommitTree = new ArrayDeque<>();
        HashSet<String> ancestorCommits = new HashSet<>();
        givenCommitTree.push(givenCommit);
        ancestorCommits.add(givenCommit.getSHA());
        while (!givenCommitTree.isEmpty()) {
            popped = givenCommitTree.pop();
            if (popped.getParent() == null) {
                break;
            }
            poppedParent = popped.getParentCommit();
            ancestorCommits.add(poppedParent.getSHA());
            givenCommitTree.push(popped.getParentCommit());
            if (popped.hasSecondParent()) {
                poppedSecondParent = popped.getSecondParent();
                ancestorCommits.add(poppedSecondParent.getSHA());
                givenCommitTree.push(popped.getSecondParent());
            }
        }
        ArrayDeque<Commit> currCommitTree = new ArrayDeque<>();
        currCommitTree.push(currCommit);
        while (!currCommitTree.isEmpty()) {
            popped = currCommitTree.pop();
            if (popped.getParent() == null) {
                return popped;
            }
            if (ancestorCommits.contains(popped.getSHA())) {
                return popped;
            }
            poppedParent = popped.getParentCommit();
            currCommitTree.push(poppedParent);
            if (popped.hasSecondParent()) {
                currCommitTree.push(popped.getSecondParent());
            }
        }
        return null;
    }


    /* Clears the staging area and removal area. */
    public void clearStagedAndRemoved() {
        for (File f : STAGED_DIR.listFiles()) {
            f.delete();
        }
        for (File f: REMOVAL_DIR.listFiles()) {
            f.delete();
        }
    }

    /* Check if the file is staged for removal. */
    public boolean isRemoved(String fileName) {
        File tempFile = Utils.join(REMOVAL_DIR, fileName);
        return tempFile.exists();
    }

    /* Checks if the file is staged. */
    public boolean isStaged(String fileName) {
        File tempFile = Utils.join(STAGED_DIR, fileName);
        return tempFile.exists();
    }

    /* Creates files in CWD or rewrites their contents in the given commit. */
    public void checkoutFilesInCommit(HashMap<String, String> blobs) {
        for (String fileName : blobs.keySet()) {
            File tempFile = Utils.join(CWD, fileName);
            if (!tempFile.exists()) {
                try {
                    tempFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            File blobFile = Utils.join(BLOB_DIR, blobs.get(fileName));
            Blob fileBlob = Utils.readObject(blobFile, Blob.class);
            Utils.writeContents(tempFile, fileBlob.getContents());
        }
    }

    /* Deletes the files that aren't staged in the given commit. */
    public void delMissingStagedinCommit(Commit thisCommit) {
        for (String file : Utils.plainFilenamesIn(CWD)) {
            if (!thisCommit.commitContains(file)) {
                File delFile = Utils.join(CWD, file);
                delFile.delete();
            }
        }
    }

    /* Clears the staging area. */
    public void clearStagingArea() {
        for (String fileName : Utils.plainFilenamesIn(STAGED_DIR)) {
            File f = Utils.join(STAGED_DIR, fileName);
            f.delete();
        }
    }

    /* Checks if the file is tracked or not. */
    public boolean isFileTracked(String fileName) {
        File stagedFile = Utils.join(STAGED_DIR, fileName);
        if (getCurrCommit().commitContains(fileName)) {
            return true;
        } else if (stagedFile.exists()) {
            return true;
        }
        return false;
    }

    /* Returns the Commit of the given SHA Id. */
    public Commit getCommit(String commitSHA) {
        File commitFile = Utils.join(COMMIT_DIR, commitSHA);
        return Utils.readObject(commitFile, Commit.class);
    }

    /* Returns the current HEAD. */
    public String getHEAD() {
        File headFile = Utils.join(HEAD_DIR, "head");
        return Utils.readObject(headFile, String.class);
    }

    /* Returns the current branch. */
    public Branch getCurrentBranch() {
        File branchFile = Utils.join(BRANCHES_DIR, getHEAD());
        return Utils.readObject(branchFile, Branch.class);
    }

    /* Returns the current commit. */
    public Commit getCurrCommit() {
        String currCommitSHA = getCurrentBranch().getCommitSHA();
        File currCommitFile = Utils.join(COMMIT_DIR, currCommitSHA);
        Commit currCommit = Utils.readObject(currCommitFile, Commit.class);
        return currCommit;
    }

    /* Deletes the Repo for restarting purposes. */
    public void restart() {
        File[] contents = GIT_DIRECTORY.listFiles();
        for (File f : contents) {
            File[] innerContents = f.listFiles();
            for (File innerF : innerContents) {
                innerF.delete();
            }
            f.delete();
        }
        GIT_DIRECTORY.delete();
    }
}
