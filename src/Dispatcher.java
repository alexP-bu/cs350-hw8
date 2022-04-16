import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Solution to HW-6 as the hw states - however, this is slow. 
 * This is because each worker is generating its own brute force method until it finds a hash.
 * Can we speed this up with a hashmap so we dont have to recompute values?
 * Or maybe have different workers that generate different parts of that hashmap?
 */

public class Dispatcher{

    private BlockingQueue<String> workQueue;
    private List<Thread> threads;
    private List<Integer> crackedHashes;
    private Set<String> uncrackedHashes;
    private Long timeout;
    private int totCPUs;

    public Dispatcher(){
        this.workQueue = new LinkedBlockingQueue<>();
        this.threads = new Vector<>();
        this.crackedHashes = new Vector<>();
        this.uncrackedHashes = new CopyOnWriteArraySet<>();
    }

    /** 
     * @param path
     */
    //read lines from file and dispatch them to the queue
    public void unhashFromFile(String path){
        try(BufferedReader br = new BufferedReader(new FileReader(new File(path)))){
            br.lines().forEach(this::dispatch);
        } catch(Exception e){
          e.printStackTrace();
        }

        threads.stream().forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        });
    }

    /** 
     * @param hash
     */
    //add unit of work to work queue 
    public void dispatch(String hash){
        workQueue.add(hash);
        //if there are jobs in the queue but not available workers, keep running until there 
        //are no jobs left in the queue (workers aren't capped)
        while(!workQueue.isEmpty()){
            if(true){
                Thread t = new Thread(new Worker(workQueue.poll(), timeout, uncrackedHashes, crackedHashes));
                t.start();
                threads.add(t);
            }
        }
    }

    /** 
     * @param timeout
     */
    public void setTimeout(Long timeout){
        this.timeout = timeout;
    }

    public void setNumCPUS(int cpus){
        this.totCPUs = cpus;
    }

    public void listCracked(){
        crackedHashes.stream().forEach(System.out::println);
    }

    public void listUncracked(){
        uncrackedHashes.stream().forEach(System.out::println);
    }

    public void sortCrackedHashes(){
        crackedHashes = crackedHashes.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    }

    public List<Integer> getCrackedHashes(){
        return crackedHashes;
    }
    
    public Set<String> getUncrackedHashes() {
        return uncrackedHashes;
    }

    /** 
     * @param args[0] file path
     * @param args[1] num cpus
     * @param args[2] OPTIONAL timeout
     */
    public static void main(String[] args) {
        //initialize dispatcher
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setNumCPUS(Integer.valueOf(args[1]));
        
        //the submission portal is kinda buggy with the second argument
        if(args.length > 2){
            dispatcher.setTimeout(Long.valueOf(args[2]));
        }

        //import hashes into dispatcher
        dispatcher.unhashFromFile(args[0]);
    }
}
