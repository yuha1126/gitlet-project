package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/** The commit class for Gitlet.
 *  @author Yunsu Ha
 */

public class Commit implements Serializable {

    /** The CWD directory. */
    private static final File CWD = new File(System.getProperty("user.dir"));
    /** The Git directory. */
    private static final File GIT_DIRECTORY = Utils.join(CWD, ".gitlet");
    /** The Blob directory. */
    private static final File BLOB_DIR = Utils.join(GIT_DIRECTORY, "blobs");
    /** The Commit directory. */
    private static final File COMMIT_DIR = Utils.join(GIT_DIRECTORY, "commits");
    /** The message of the commit. */
    private String _msg;
    /** The parent of the commit. */
    private String _parent;
    /** The second parent of the commit. */
    private String _secondParent;
    /** The date of the commit. */
    private Date _time;
    /** The blobs of the commit. FileName, then Blob SHA. */
    private HashMap<String, String> _blobs;
    /** The SHA of the commit. */
    private String _SHA;

    /* Creates the initial commit of init. */
    public Commit() {
        _msg = "initial commit";
        _time = new Date(0);
        _parent = null;
        _secondParent = null;
        _blobs = new HashMap<>();
        _SHA = Utils.sha1(Utils.serialize(this));
    }

    /* Constructor of Commit class. */
    public Commit(String msg, String parent) {
        _msg = msg;
        _parent = parent;
        _secondParent = null;
        _time = new Date();
        _blobs = getParentBlobs();
        _SHA = Utils.sha1(Utils.serialize(this));
    }

    /* Constructor for creating a merge Commit. */
    public Commit(String msg, String parent, String secondParent) {
        _msg = msg;
        _parent = parent;
        _secondParent = secondParent;
        _time = new Date();
        _blobs = getParentBlobs();
        _SHA = Utils.sha1(Utils.serialize(this));
    }

    /* Deletes the blob of the given fileName. */
    public void delBlob(String fileName) {
        _blobs.remove(fileName);
    }

    /* Returns the date in the correct format. */
    public String getDateformatted() {
        SimpleDateFormat commitDate = new SimpleDateFormat(
                "E MMM dd HH:mm:ss YYYY Z");
        String stringDate = commitDate.format(_time);
        return stringDate;
    }

    /* Returns whether the commit contains the file
     with the same name as fileName. */
    public boolean commitContains(String fileName) {
        if (_blobs.containsKey(fileName)) {
            return true;
        }
        return false;
    }

    /* Returns whether the commit contains a file of a given commit. */
    public boolean commitContainsExact(String fileName, Commit compared) {
        if (!getBlob().containsKey(fileName)) {
            return false;
        }
        if (!compared.getBlob().containsKey(fileName)) {
            return false;
        }
        String thisFileContents =
                getFileBlob(fileName).getContentsString();
        String comparedFileContents =
                compared.getFileBlob(fileName).getContentsString();
        return thisFileContents.equals(comparedFileContents);
    }

    /* Returns whether the commit contains a file of a given
    value but not the exact same value. */
    public boolean commitContainsNotExact(String fileName, Commit compared) {
        return commitContains(fileName)
                && !commitContainsExact(fileName, compared);
    }

    /* Returns whether the commit contains the exact same file as fileName. */
    public boolean commitContainsExact(String fileName) {
        if (!_blobs.containsKey(fileName)) {
            return false;
        }
        File tempFile = Utils.join(CWD, fileName);
        String getFileString = Utils.readContentsAsString(tempFile);
        return getFileBlob(fileName).getContentsString().equals(getFileString);
    }

    /* Returns whether the commit contains a file but
     not the exact same value. */
    public boolean commitContainsNotExact(String fileName) {
        return commitContains(fileName)
                && !commitContainsExact(fileName);
    }

    /* Returns the blob given the fileName. */
    public Blob getFileBlob(String fileName) {
        String fileBlobSHA = _blobs.get(fileName);
        File blobFile = Utils.join(BLOB_DIR, fileBlobSHA);
        return Utils.readObject(blobFile, Blob.class);
    }

    /* Returns the blobs of the parents. */
    public HashMap<String, String> getParentBlobs() {
        File parentFile = Utils.join(COMMIT_DIR, _parent);
        Commit parentCommit = Utils.readObject(parentFile, Commit.class);
        return parentCommit.getBlob();
    }

    /* Checks if the commit has a second parent. */
    public boolean hasSecondParent() {
        if (_secondParent == null) {
            return false;
        }
        return true;
    }

    /* Returns the second parent of this commit. */
    public Commit getSecondParent() {
        File parentFile = Utils.join(COMMIT_DIR, _secondParent);
        return Utils.readObject(parentFile, Commit.class);
    }

    /* Adds a blob to a _blob. */
    public void addBlob(String fileName, String sha) {
        _blobs.put(fileName, sha);
    }

    /* Returns _msg. */
    public String getMsg() {
        return _msg;
    }

    /* Returns the parent's SHA of this commit. */
    public String getParent() {
        return _parent;
    }

    public Commit getParentCommit() {
        File parentFile = Utils.join(COMMIT_DIR, getParent());
        return Utils.readObject(parentFile, Commit.class);
    }

    /* Returns the date when this commit was made. */
    public Date getTime() {
        return _time;
    }

    /* Returns the blobs of this commit. */
    public HashMap<String, String> getBlob() {
        return _blobs;
    }

    /* Returns the SHA-1 of this commit. */
    public String getSHA() {
        return _SHA;
    }
}
