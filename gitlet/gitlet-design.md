# Gitlet Design Document
author: Yunsu Ha

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. Your explanations in
this section should be as concise as possible. Leave the full
explanation to the following sections. You may cut this section short
if you find your document is too wordy.

1. Main.java - Where all the user inputs are interpreted as commands.
   1. main(String.. args)
2. Repo.java - Where the command functions are written and where the file directories are kept / edited.
   1. Repo variables
      1. GIT_DIRECTORY - the .gitlet directory
      2. CWD - central working directory
      3. COMMIT_DIR - directory where commits are stored
      4. STAGED_DIR - directory where staged files are stored
      5. BLOB_DIR - directory where blobs are stored
      6. BRANCHES_DIR - directory where branches are stored
      7. HEAD_DIR - directory where the current HEAD is stored
   2. Repo methods
      1. Repo() - constructor for repo
      2. init() - starts the init command
      3. add(String) - does the add command
      4. commit(string) - does the commit command
      5. checkout(string) / checkout(String, String) - does the checkout command
      6. log() - does the log command
3. Commit.java
   1. Commit variables
      1. _msg - the message of a commit
      2. _parent - the SHA id of the parent commit
      3. _time - the date when the commit was made
      4. _SHA - the sha of the commit
      5. _blobs - the hashmap that contains the file name and SHA of the blobs
   2. Commit methods
      1. commit() - creates the initial commit
      2. commit(String, String) - constructs a commit
      3. getDateFormatter() - returns the date in string format
      4. commitContains(String) - checks if this commit contains a file with the given name
      5. commitContainsExact(String) - checks if this commit contains this exact file
      6. getFileBlob(String) - gets the blob of the given file of this commit
      7. getParentBlobs() - gets the _blobs of the parent commit
      8. addBlob() - adds the blob to the commit
4. Blob.java - The contents of the files
   1. Blob variables
      1. _blobFile - the file that the blob points to
      2. _name - name of the file
      3. _SHA - SHA of this blob
      4. _contents - contents of the file
   2. Blob methods
      1. Blob(String) - constructs a Blob given the file name
      2. getSHA() - returns the sha of the Blob
      3. getContents() - gets the contents of the blob
5. Branch.java - 


## 2. Algorithms

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

   * Checking if a merge is necessary.
   * Determining which files (if any) have a conflict.
   * Representing the conflict in the file.
  
* Try to clearly mark titles or names of classes with white space or
  some other symbols.

1. Main.java methods
   1. init() - creates a new Gitlet version control system in the current directory
   2. add(File file) - adds a file to the staging area.
   3. commit(File commitFile, String msg) - checks the commitFile to see if it's added and commits it
   4. rm(File rmFile) - unstages a file
   5. log(boolean global) - prints all the commits in the _commit list. If global, displays info for all commits ever made
   6. find(String commitMsg) - searches through the commit hashmap to find which commit has the corresponding msg.
   7. status() - displays what files have been staged for addition or removal
   8. checkout(File fileName, String ID)
      1. checkout(File fileName)
      2. checkout(Commit branch)
   9. branch(String name) - creates a new branch
   10. rm(Commit branch, String) - removes the branch name
   11. reset(String commitID) - restores the commit of the given id
   12. commit(File commitFile) - commits a file, storing them into the list of committed files
2. Main.java fields
   1. static final Commit initialCommit - the initial commit that is made when init is called
   2. static final File currDir - current working directory
   
3. Commit.java methods
   1. getID() - returns the ID of this commit
   2. checkCommit(String ID) - returns true if the ID matches, else false
   3. checkCommitMsg(String msg) - returns true if the msg matches the commit message
   4. returnID() - returns this commit's ID
   5. returnMsg() - returns this commit's message
   6. Commit(Commit parent, String msg, String ID) - constructs a Commit instance

5. Blob.java methods
   1. Blob(File[] Files) - contains all the files that are needed in the blob
   2. addFile(File file) - adds the file to the blob
   3. removeFile(File file) - removes the file from the blob
   4. retrieve(String fileName) - retrieves the file with the fileName
   5. retrieve(File file) - retrieves the file from the blob
   

## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
       `java gitlet.Main add wug.txt`,
  on the next execution of
       `java gitlet.Main commit -m “modify wug.txt”`, 
  the correct commit will be made.
  
* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.
  
* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.

1. commits - for each time something is committed, save the commit to the commit list in Main and write the contents for the changes/additions made in the directory.
2. add - for each add add the corresponding file to the list in Main (_staged) and remove it from _unstaged.
3. rm - cycle through the staged files and remove the corresponding one.

subdirectories:
1. testCommit - tests if commits work
2. testBlob - checks if files are stored properly in blobs
3. testStages - tests if files are staged properly

## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

