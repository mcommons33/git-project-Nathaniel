import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
public class Git{
    public static void main (String [] args){
        testRepoInit();
    }

    public static void initGitRepo () {
        int pathExistsCounter=0;
        //Creates the "git" directory
        File git = new File ("git");
        if (!git.exists()){
            try {
                Files.createDirectory(Paths.get("git"));
            } catch (IOException e) {
                e.printStackTrace();
                pathExistsCounter++;
            }
        }
        else{
            System.out.println ("git directory already exists.");
        }
        //Creates the "objects" directory inside in "git" directory
        File objects = new File ("git/objects");
        if (!objects.exists()){
            try {
                Files.createDirectory(Paths.get("git/objects"));
            } catch (IOException e) {
                e.printStackTrace();
                pathExistsCounter++;
            }
        }
        else{
            System.out.println ("objects directory already exists.");
        }

        //Creates the "index" file in the "git" folder
        File index = new File ("git/index");
        if (!index.exists()){
            try {
                Files.createFile(Paths.get("git/index"));
            } catch (IOException e) {
                e.printStackTrace();
                pathExistsCounter++;
            }
        }
        else{
            System.out.println ("index file already exists.");
        }

        //Created the "HEAD" file in the "git" folder
        File HEAD = new File ("git/HEAD");
        if (!HEAD.exists()){
            try {
                Files.createFile(Paths.get(HEAD.getPath()));
            } catch (IOException e) {
                e.printStackTrace();
                pathExistsCounter++;
            }
        }
        else{
            System.out.println ("HEAD file already exists.");
        }

        //Prints custom message if all paths already exist
        if (pathExistsCounter>=4) {
            System.out.println("Git Repository already exists");
        }
    }
    public static void commit (String author, String message) throws IOException, NoSuchAlgorithmException, ObjectsDirectoryNotFoundException{
        String hashOfCommit, hashOfCurrentTree, hashOfLastCommit;
        StringBuilder commitData = new StringBuilder();
        File head = new File ("./git/HEAD");
        hashOfLastCommit = new String (Blob.readFileContent(head.getPath()), StandardCharsets.UTF_8);
        hashOfCurrentTree = Blob.generateSha1(Blob.createTree("./direct"));
        //making commit file data
        commitData.append("tree: " + hashOfCurrentTree + "\nparent: " + hashOfLastCommit + "\nauthor: " 
        + author + "\ndate: " + LocalDate.now() + "\nmessage: " + message);
     
        // backing up the commit file in the objects folder
        hashOfCommit = Blob.generateSha1(commitData.toString());
        Files.createFile(Paths.get("./git/objects" + hashOfCommit));
    }
    public static void stage (String filePath){

    }
    //Tests main methods
    private static void testRepoInit() {
        //Testing file creation
        initGitRepo();
        //Checks for created files
        System.out.println("git dir exists: " + doesPathExist("git"));
        System.out.println("git/objects dir exists: " + doesPathExist("git/objects"));
        System.out.println("git/index file exists: " + doesPathExist("git/index"));
        //Testing custom already created message
        initGitRepo();
    }

    //Checks if a path exists, returns the boolean answer
    private static boolean doesPathExist (String path) {
        return Files.exists(Paths.get(path));
    }

    //Deletes chosen path, returns true if path deleted, false if path did not exist
    private static boolean deletePath(String path) {
        try {
            Path targetPath = Paths.get(path);
            //Case for nonexistent path
            if (Files.notExists(targetPath)) {
                return false;
            }
            //If the path is a regular file, just deletes it
            if (Files.isRegularFile(targetPath)) {
                Files.delete(targetPath);
                return true;
            }
            //Recursively deletes elements in the file tree
            Files.walkFileTree(targetPath, new SimpleFileVisitor<Path>() {
                @Override //This is just used to help catch errors
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }//This basically says, whenever you encounter a file while walking through the tree, delete it
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }//Ensures that the now-empty directory is deleted
            });
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}