import java.util.List;
import java.util.Set;

public class Worker implements Runnable{
    
    private String hash;
    private UnHash unhasher;
    private Integer result;
    private List<Integer> crackedHashes;
    private Set<String> uncrackedHashes;
    private boolean done;

    Worker(String hash, Long timeout, Set<String> uncracked, List<Integer> cracked){
        this.hash = hash;
        this.unhasher = new UnHash(timeout);
        this.uncrackedHashes = uncracked;
        this.crackedHashes = cracked;
        this.done = false;
    }

    public Integer getResult(){
        return result;
    }

    public String getHash(){
        return hash;
    }

    public void finish(){
        this.done = true;
    }

    public boolean isDone(){
        return done;
    }

    

    @Override
    public void run(){
        result = unhasher.unhash(hash);
        
        if(result == null){
            uncrackedHashes.add(hash);
        }else{
            crackedHashes.add(result);
        }
        
        this.finish();
    }
}
