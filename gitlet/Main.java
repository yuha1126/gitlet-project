package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Yunsu Ha
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        Repo gitRepo = new Repo();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else {
            mainHelper1(args, gitRepo);
        }
    }

    static void mainHelper1(String[] args, Repo gitRepo) {
        switch (args[0]) {
        case "init":
            if (validateNumArguments(args, 1)) {
                gitRepo.init();
            }
            break;
        case "add":
            if (validateNumArguments(args, 2)) {
                gitRepo.add(args[1]);
            }
            break;
        case "commit":
            caseCommit(args, gitRepo);
            break;
        case "checkout":
            caseCheckout(args, gitRepo);
            break;
        case "log":
            if (validateNumArguments(args, 1)) {
                gitRepo.log();
            }
            break;
        case "global-log":
            if (validateNumArguments(args, 1)) {
                gitRepo.logGlobal();
            }
            break;
        case "rm":
            if (validateNumArguments(args, 2)) {
                gitRepo.rm(args[1]);
            }
            break;
        case "find":
            if (validateNumArguments(args, 2)) {
                gitRepo.find(args[1]);
            }
            break;
        default:
            mainHelper2(args, gitRepo);
        }
    }

    static void mainHelper2(String[] args, Repo gitRepo) {
        switch (args[0]) {
        case "branch":
            if (validateNumArguments(args, 2)) {
                gitRepo.branch(args[1]);
            }
            break;
        case "rm-branch":
            if (validateNumArguments(args, 2)) {
                gitRepo.rmBranch(args[1]);
            }
            break;
        case "reset":
            if (validateNumArguments(args, 2)) {
                gitRepo.reset(args[1]);
            }
            break;
        case "status":
            if (validateNumArguments(args, 1)) {
                gitRepo.status();
            }
            break;
        case "merge":
            if (validateNumArguments(args, 2)) {
                gitRepo.merge(args[1]);
            }
            break;
        default:
            System.out.println("No command with that name exists.");
        }
    }

    static boolean validateNumArguments(String[] arguments, int validLength) {
        if (arguments.length != validLength) {
            System.out.println("Incorrect operands.");
            return false;
        } else {
            return true;
        }
    }

    static void caseCommit(String[] args, Repo gitRepo) {
        if (args[1].equals("")) {
            System.out.println("Please enter a commit message.");
        } else if (args.length == 2) {
            gitRepo.commit(args[1]);
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    static void caseCheckout(String[] args, Repo gitRepo) {
        if (args.length == 3 && args[1].equals("--")) {
            gitRepo.checkout(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            gitRepo.checkout(args[1], args[3]);
        } else if (args.length == 2) {
            gitRepo.checkoutBranch(args[1]);
        } else {
            System.out.println("Incorrect operands.");
        }
    }
}
