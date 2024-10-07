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
        hashOfLastCommit = new String (Blob.readFileContent(head.getPath()), StandardCharsets.UTF_8); //grabbing hash of last commit from head file
        //getting the hash of the current tree; adding index file data to previous index file data
        BufferedReader cr = new BufferedReader(new FileReader("./git/objects/" + hashOfLastCommit));
        String treeOfPreviousCommit = cr.readLine().split(" ")[1];
        cr.close();
        StringBuilder snapShot = new StringBuilder();
        if(!treeOfPreviousCommit.equals("")){
            BufferedReader tr = new BufferedReader(new FileReader ("./git/objects/" + treeOfPreviousCommit));
            while (tr.ready()){
                snapShot.append(tr.readLine());
            }
            tr.close();
        }
        BufferedReader ir = new BufferedReader(new FileReader("./git/index"));
        while (ir.ready()){
            snapShot.append(ir.readLine());
        }
        ir.close();
        hashOfCurrentTree = Blob.generateSha1(snapShot.toString());
        
        //making commit file data
        commitData.append("tree: " + hashOfCurrentTree + "\nparent: " + hashOfLastCommit + "\nauthor: " 
        + author + "\ndate: " + LocalDate.now() + "\nmessage: " + message);
     
        // backing up the commit file in the objects folder
        hashOfCommit = Blob.generateSha1(commitData.toString());
        File thisCommit = new File ("./git/objects" + hashOfCommit);
        thisCommit.createNewFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter (thisCommit));
        bw.write(commitData.toString());
        bw.close();

        //updating head file
        BufferedWriter br = new BufferedWriter(new FileWriter("./git/HEAD"));
        br.write(hashOfCommit);

        //clearing index file
        File index = new File ("./git/index");
        index.delete();
        index.createNewFile();
    }
  
    //question for theiss, what does the first commit file data look like, and subsequent ones; how do i get the current tree file
    
    //Prepares a file to 
    public static void stage (String filePath) throws NoSuchAlgorithmException, IOException, ObjectsDirectoryNotFoundException{
        File fileToStage = new File (filePath);
        if (!fileToStage.exists())
            throw new FileNotFoundException();
        if (fileToStage.isDirectory()){

        }

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