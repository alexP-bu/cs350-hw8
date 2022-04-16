import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

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

    public Dispatcher(long timeout){
        this.workQueue = new LinkedBlockingQueue<>();
        this.uncrackedHashes = new CopyOnWriteArraySet<>();
        this.threads = new Vector<>();
        this.crackedHashes = new Vector<>();
        this.timeout = timeout;
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

    public List<Integer> getCrackedHashes(){
        return crackedHashes;
    }
    
    public Set<String> getUncrackedHashes() {
        return uncrackedHashes;
    }
}
