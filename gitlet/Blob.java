package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {

    /** The name. */
    private String _name;

    /** The contents. */
    private byte[] _contents;

    /** File directory of the blob. */
    private File _blobFile;

    /** The SHA of the blob. */
    private String _SHA;

    /** The contents in string form. */
    private String _contentString;

    /** Constructor of the Blob class. FILENAME and WORKDIR. */
    public Blob(String fileName, File workDir) {
        _blobFile = Utils.join(workDir, fileName);
        _name = fileName;
        _contents = Utils.readContents(_blobFile);
        _contentString = Utils.readContentsAsString(_blobFile);
        _SHA = Utils.sha1(Utils.serialize(this));

    }

    /* Returns the file name of this blob. */
    public String getName() {
        return _name;
    }

    /* Returns the HSA String of this Blob. */
    public String getSHA() {
        return _SHA;
    }

    /* Returns the contents of the Blob. */
    public byte[] getContents() {
        return _contents;
    }

    /* Returns the contents of the Blob as String. */
    public String getContentsString() {
        return _contentString;
    }
}
