import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This dispatcher is from HW7.
 * It cracks the first piece of the puzzle - the initial hashed integers
 * It does this by making generators, which generate subdictionaries
 * Workers then search the dictionaries for a hash
 */

public class Dispatcher{

    public static final int NUM_GENS = 10; //number of generators generating hashmap subsets
    private List<Generator> generators; 
    private List<Thread> threads;
    private List<Integer> crackedHashes;
    private Set<String> uncrackedHashes;
    private Long timeout;

    public Dispatcher(long timeout){
        this.uncrackedHashes = new CopyOnWriteArraySet<>();
        this.generators = new CopyOnWriteArrayList<>();
        this.threads = new Vector<>(100, 10);
        this.crackedHashes = new Vector<>(50);
        this.timeout = timeout;
    }

    /** 
     * @param path
     */
    //read lines from file and dispatch them to the queue
    public void unhashFromFile(String path){
        initGenerators(NUM_GENS);

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
            hash, timeout, generators, uncrackedHashes, crackedHashes));
        t.start();
        threads.add(t);
    }

    private void initGenerators(int numGensInit) {
        AtomicInteger a = new AtomicInteger(0);
        this.generators = Stream
                            .generate(() -> {
                                Generator g = new Generator(a.getAndIncrement());
                                Thread thread = new Thread(g);
                                thread.start();
                                return g;
                            })
                            .limit(numGensInit++)
                            .collect(Collectors.toList());
    }

    private void completeThreads() {
        threads
            .stream()
            .parallel()
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
