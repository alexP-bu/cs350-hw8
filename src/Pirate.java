import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.Vector;
public class Pirate {
    
    private Dispatcher dispatcher;
    private String ciphertextPath;
    private List<Thread> threads;
    private List<Integer> crackedHints;
    private final PrintWriter printer;
    
    public Pirate(String path, Long timeout){
        this.dispatcher = new Dispatcher(timeout);
        this.threads = new Vector<>();
        this.crackedHints = new Vector<>();
        this.printer = new PrintWriter(System.out);
        ciphertextPath = path;
    }
    
    public void findTreasure(String path) throws FileNotFoundException, IOException{
        //run first pass
        dispatcher.unhashFromFile(path);
        //sort cracked hashes in order of lowest to highest
        dispatcher.getCrackedHashes().forEach(hint -> crackedHints.add(hint));
        //run phase 2 until all hints are cracked
        while(!dispatcher.getUncrackedHashes().isEmpty()){
            crackedHints.sort((o1, o2) -> o1.compareTo(o2));
            phaseTwo(crackedHints, dispatcher.getUncrackedHashes());
            finishThreads();
        }
        //ensure threads are finished before decryption
        finishThreads();
        //decrypt ciphertext
        decrypt(ciphertextPath, crackedHints);
    }

    //this method decrypts the ciphertext using the cracked hints
    private void decrypt(String ciphertextPath, List<Integer> crackedHints) 
                                            throws FileNotFoundException, IOException {
        //read ciphertext as bytes
        byte[] arr = readCiphertext(ciphertextPath);
        //convert to a string
        String ciphertext = new String(arr, StandardCharsets.UTF_8);
        crackedHints.sort((o1, o2) -> o1.compareTo(o2));
        for(int i : crackedHints){
            printer.write(ciphertext.charAt(i));
        }
    }

    //this method reads ciphertext from file given path
    private byte[] readCiphertext(String ciphertextPath) throws FileNotFoundException, IOException {
        File cipherTextFile = new File(ciphertextPath);
        byte[] arr = new byte[(int)cipherTextFile.length()];
        try (FileInputStream fs = new FileInputStream(cipherTextFile)) {
            fs.read(arr);
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
    
    private void phaseTwo(List<Integer> crackedHints, Set<String> uncrackedHashes){
        for(int i = 0; i < crackedHints.size(); i++){
            for(int k = i + 1; k < crackedHints.size(); k++){
                int startPoint = crackedHints.get(i);
                int endPoint = crackedHints.get(k);
                Thread thread = new Thread(() -> {
                    Hash hasher = new Hash();
                    for(int j = startPoint + 1; j < endPoint; j++){
                        Integer middle = j;
                        String currHash = hasher.hash(startPoint + ";" + middle + ";" + endPoint);
                        if(uncrackedHashes.contains(currHash)){
                            crackedHints.add(middle);
                            uncrackedHashes.remove(currHash);
                        }
                    }
                });
                threads.add(thread);
                thread.start();
            }
        }
    }

    public void printOuput(){
        printer.flush();
    }
 
    public static void main(String[] args) throws IOException {
        Pirate pirate = new Pirate(args[3], Long.valueOf(args[2]));
        pirate.findTreasure(args[0]);
        pirate.printOuput();
    }
}
