package gitlet;

import ucb.junit.textui;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Yunsu Ha
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        System.exit(textui.runClasses(UnitTest.class));
    }

    private static final File GIT_DIRECTORY = new File(".gitlet");
    private static final File COMMIT_DIR = new File(".gitlet/commits");


    /** A dummy test to avoid complaint. */
    @Test
    public void initTest() {
        Repo r = new Repo();
        r.init();
        assertEquals(true, GIT_DIRECTORY.exists());
        assertEquals(true, COMMIT_DIR.exists());
        Commit tempCommit = new Commit();
        assertEquals("initial commit", tempCommit.getMsg());
        r.restart();
        assertEquals(false, GIT_DIRECTORY.exists());
    }
}
