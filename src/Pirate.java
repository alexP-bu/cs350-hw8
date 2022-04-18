import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

public class Pirate {
    
    private Dispatcher dispatcher;
    private String ciphertextPath;
    private Set<Integer> crackedHints;
    private List<Thread> threads;
    private final PrintWriter printer;

    public Pirate(String path, Long timeout){
        this.dispatcher = new Dispatcher(timeout);
        ciphertextPath = path;
        this.crackedHints = Collections.synchronizedSet(new TreeSet<>());
        this.threads = new Vector<>(1500, 250);
        this.printer = new PrintWriter(System.out);
    }
    
    public void findTreasure(String path) throws IOException{
        //run first pass
        dispatcher.unhashFromFile(path);
        //sort cracked hashes in order of lowest to highest
        dispatcher.getCrackedHashes().forEach(hint -> crackedHints.add(hint));
        //run phase 2 until all hints are cracked
        while(!dispatcher.getUncrackedHashes().isEmpty()){
            phaseTwo(crackedHints, dispatcher.getUncrackedHashes());
            finishThreads();
        }

        //ensure threads are finished before decryption
        //decrypt ciphertext
        decrypt(ciphertextPath, crackedHints);
    }

    //this method decrypts the ciphertext using the cracked hints
    private void decrypt(String ciphertextPath, Set<Integer> crackedHints2) 
                                            throws IOException {
        //read ciphertext as bytes
        byte[] arr = readCiphertext(ciphertextPath);
        //convert to a string
        String ciphertext = new String(arr, StandardCharsets.UTF_8);
        for(int i : crackedHints2){
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

    //this method ensures all threads are finished
    //before moving on in the main thread
    private void finishThreads(){
        threads.stream().forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        });
    }
    
    //this runs the phase two operation of unhashing i;i+1 to j - 1
    private void phaseTwo(Set<Integer> crackedHints2, Set<String> uncrackedHashes){
        //List<Integer> nextPhaseHints = new CopyOnWriteArrayList();
        crackedHints2.forEach(hint1 -> crackedHints2.stream()
                                                .filter(hint2 -> (hint1 < hint2))
                                                .forEach(hint2 -> {
                                                    Thread thread = new Thread(new TreasureGnome(
                                                        crackedHints2, hint1, hint2, uncrackedHashes));
                                                    thread.start();
                                                    threads.add(thread);
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
