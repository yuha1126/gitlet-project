package gitlet;

import java.io.Serializable;

public class Branch implements Serializable {

    /** The _name of the branch. */
    private String _name;

    /** The SHA of the commit. */
    private String _commitSHA;

    public Branch(String name, String commitSHA) {
        _name = name;
        _commitSHA = commitSHA;
    }

    /* Returns the commit SHA that this branch currently points to. */
    public String getCommitSHA() {
        return _commitSHA;
    }

    /* Edits the commit this branch is pointing to. */
    public void editCommitSHA(String sha) {
        _commitSHA = sha;
    }

    public String getName() {
        return _name;
    }

}
