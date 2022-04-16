import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
public class Pirate {
    
    private Dispatcher dispatcher;
    private String ciphertextPath;
    private List<Thread> threads;
    private List<Integer> crackedHints;
    private final PrintWriter printer;
    
    public Pirate(){
        this.dispatcher = new Dispatcher();
        this.threads = new Vector<>();
        this.crackedHints = new ArrayList<>();
        this.printer = new PrintWriter(System.out);
    }
    
    public void findTreasure(String path){
        //run first pass
        dispatcher.unhashFromFile(path);
        //use a priorityqueue to keep the cracked hashes in order of lowest to highest
        dispatcher.getCrackedHashes().forEach(hint -> crackedHints.add(hint));
        //run phase 2 until all hints are cracked
        while(!dispatcher.getUncrackedHashes().isEmpty()){
            crackedHints.sort((o1, o2) -> o1.compareTo(o2));
            phaseTwo(crackedHints, dispatcher.getUncrackedHashes());
        }
        //ensure threads are finished before decryption
        finishThreads();
        //decrypt ciphertext
        decrypt(ciphertextPath, crackedHints);
    }

    //this method decrypts the ciphertext using the cracked hints
    private void decrypt(String ciphertextPath, List<Integer> crackedHints) {
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
    private byte[] readCiphertext(String ciphertextPath) {
        File file = new File(ciphertextPath);
        byte[] arr = new byte[(int)file.length()];
        try (FileInputStream fs = new FileInputStream(file)) {
            fs.read(arr);
        } catch (IOException e) {
            e.printStackTrace();
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
        Hash hasher = new Hash();
        for(int i = 0; i < crackedHints.size(); i++){
            for(int k = i + 1; k < crackedHints.size(); k++){
                int startPoint = crackedHints.get(i);
                int endPoint = crackedHints.get(k);
                    for(int j = startPoint + 1; j < endPoint; j++){
                        String currHash = hasher.hash(startPoint + ";" + j + ";" + endPoint);
                        if(uncrackedHashes.contains(currHash)){
                            uncrackedHashes.remove(currHash);
                            crackedHints.add(j);
                        }
                    }
            }
        }
    }

    public void setNumCPUS(int cpus){
        dispatcher.setNumCPUS(cpus);
    }

    public void setTimeout(Long timeout){
        dispatcher.setTimeout(timeout);
    }

    public void setCiphertext(String ciphertext){
        this.ciphertextPath = ciphertext;
    }

    public void printOuput(){
        printer.flush();
    }
 
    public static void main(String[] args) {
        Pirate pirate = new Pirate();
        pirate.setNumCPUS(Integer.valueOf(args[1]));
        pirate.setTimeout(Long.valueOf(args[2]));
        pirate.setCiphertext(args[3]);
        pirate.findTreasure(args[0]);
        pirate.printOuput();
    }
}
