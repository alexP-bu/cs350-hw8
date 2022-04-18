import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;


public class Pirate {
    
    private Dispatcher dispatcher;
    private String ciphertextPath;
    private Set<Integer> crackedHints;
    private Set<String> uncrackedHashes;
    private final PrintWriter printer;

    public Pirate(String path, Long timeout){
        this.dispatcher = new Dispatcher(timeout);
        ciphertextPath = path;
        //lets use a concurrentskiplistset to maintain order in our set which we import hints into
        //(concurrent version of a treeset)
        this.crackedHints = new ConcurrentSkipListSet<>();
        this.printer = new PrintWriter(System.out);
        this.uncrackedHashes = new CopyOnWriteArraySet<>();
    }
    
    public void findTreasure(String path) throws IOException{
        //run first pass
        dispatcher.unhashFromFile(path);
        //import cracked hints from dispatcher
        crackedHints.addAll(dispatcher.getCrackedHashes());
        //import uncracked hashes from dipatcher
        uncrackedHashes.addAll(dispatcher.getUncrackedHashes());
        
        //run phase 2; the two concurrentskiplists are for the following operations
        //I wonder if there is a more memory efficient way to write this...
        Set<Integer> nextPhaseHints = new ConcurrentSkipListSet<>();
        crackHints(nextPhaseHints, crackedHints, uncrackedHashes);
        
        //now run phase 2 over and over until it cracks everything
        Set<Integer> nextNextPhaseHints = new ConcurrentSkipListSet<>();
        while(!uncrackedHashes.isEmpty()){
            crackHints(nextNextPhaseHints, nextPhaseHints, uncrackedHashes);
            nextPhaseHints.addAll(nextNextPhaseHints);
            nextNextPhaseHints.clear();
        }

        //ensure threads are finished before decryption
        //decrypt ciphertext
        decrypt(crackedHints);
    }

    //this method decrypts the ciphertext using the cracked hints
    private void decrypt(Set<Integer> crackedHints) throws IOException {
        //convert to a string
        String ciphertext = new String(readCiphertext(ciphertextPath), StandardCharsets.UTF_8);
        for(int i : crackedHints){
            printer.write(ciphertext.charAt(i));
        }
    }

    //this method reads ciphertext from file given path
    private byte[] readCiphertext(String ciphertextPath) throws IOException {
        File cipherTextFile = new File(ciphertextPath);
        byte[] arr = new byte[(int)cipherTextFile.length()];
        try (FileInputStream fs = new FileInputStream(cipherTextFile)) {
            arr = fs.readAllBytes();
        } 
        return arr;
    }
    
    //this runs the phase two operation of unhashing i;i+1 to j - 1
    //the pipeline filters hints so that i and j are in proper order from all subsets of hints
    private void crackHints(Set<Integer> nextPhaseHints, 
                            Set<Integer> hintsToCrack, 
                            Set<String> uncrackedHashes){
        hintsToCrack.forEach(hint1 
            -> hintsToCrack.parallelStream()
                            .filter(hint2 -> (hint1 < hint2))
                            .forEach(hint2 -> {
                                Thread thread = new Thread(new TreasureGnome(
                                    nextPhaseHints, this.crackedHints, hint1, hint2, uncrackedHashes));
                                thread.start();
                            })); 
    }

    public void printOuput(){
        printer.flush();
    }
 
    public static void main(String[] args) throws IOException {
        Long start = System.currentTimeMillis();
        Pirate pirate = new Pirate(args[3], Long.valueOf(args[2]));
        pirate.findTreasure(args[0]);
        pirate.printOuput();
        System.out.println("\n" + "RUNTIME: " + (System.currentTimeMillis() - start));
    }
}
