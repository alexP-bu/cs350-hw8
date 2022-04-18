import java.util.HashMap;
import java.util.Map;

public class Generator implements Runnable{
    
    private Map<String, Integer> dictionary;
    private final Hash hasher;
    private boolean running;
    private int id;

    public Generator(int id){
        this.id = id;
        this.dictionary = new HashMap<String, Integer>(30_000, 0.95f);
        this.hasher = new Hash();
        this.running = true;
    }

    
    /** 
     * @return Map<String, Integer>
     */
    public Map<String, Integer> getDictionary(){
        return dictionary;
    }

    public void stop(){
        this.running = false;
    }
    
    @Override
    public void run(){
        int i = this.id;
        while(running){
            dictionary.put(hasher.hash(i), i);
            i += Dispatcher.NUM_GENS; //increment by number of generators
        }
    }
}
