import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Solution to HW-6 as the hw states - however, this is slow. 
 * This is because each worker is generating its own brute force method until it finds a hash.
 * Can we speed this up with a hashmap so we dont have to recompute values?
 * Or maybe have different workers that generate different parts of that hashmap?
 */

public class Dispatcher{

    public static final int NUM_GENS = 20; //number of generators generating hashmap subsets
    private List<Generator> generators; 
    private List<Thread> threads;
    private List<Integer> crackedHashes;
    private Set<String> uncrackedHashes;
    private Long timeout;

    public Dispatcher(long timeout){
        this.uncrackedHashes = new CopyOnWriteArraySet<>();
        this.threads = new Vector<>(100, 10);
        this.generators = new Vector<>(NUM_GENS);
        this.crackedHashes = new Vector<>(50);
        this.timeout = timeout;
    }

    /** 
     * @param path
     */
    //read lines from file and dispatch them to the queue
    public void unhashFromFile(String path){
        
        try(BufferedReader br = new BufferedReader(new FileReader(new File(path)))){
            br.lines().parallel().forEach(this::dispatch);
        } catch(Exception e){
          e.printStackTrace();
        }

        completeThreads();
    }

    /** 
     * @param hash
     */
    //add unit of work to work queue 
    public void dispatch(String hash){
        Thread t = new Thread(new Worker(
            hash, timeout, uncrackedHashes, crackedHashes));
        t.start();
        threads.add(t);
    }

    private void initGenerators(int numGensInit) {
        AtomicInteger a = new AtomicInteger(0);
        this.generators = Stream
                            .generate(() -> new Generator(a.getAndIncrement()))
                            .limit(numGensInit++)
                            .collect(Collectors.toList());

        generators
            .parallelStream()
            .forEach(generator -> {
                        Thread t = new Thread(generator);
                        t.start();
                    }
            );
    }

    private void completeThreads() {
        threads
            .parallelStream()
            .forEach(thread -> {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        });

        generators
            .parallelStream()
            .forEach(Generator::stop);
    }

    public List<Integer> getCrackedHashes(){
        return crackedHashes;
    }
    
    public Set<String> getUncrackedHashes() {
        return uncrackedHashes;
    }
}
