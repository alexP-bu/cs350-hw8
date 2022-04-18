import java.util.List;
import java.util.Set;

public class Worker implements Runnable{
    
    private String hash;
    private Integer result;
    private List<Integer> crackedHashes;
    private List<Generator> generators;
    private Set<String> uncrackedHashes;
    private boolean done;
    private Long timeout;

    Worker(String hash, Long timeout, List<Generator> generators, Set<String> uncracked, List<Integer> cracked){
        this.hash = hash;
        this.uncrackedHashes = uncracked;
        this.crackedHashes = cracked;
        this.done = false;
        this.generators = generators;
        this.timeout = timeout;
    }

    private void getResult(){
        if(result == null)
            uncrackedHashes.add(hash);
        else
            crackedHashes.add(result);
    }

    public void finish(){
        this.done = true;
    }

    public boolean isDone(){
        return done;
    }

    private Integer attemptUpdateResult() {
        //let's try some fancy lambda stuff for fun :)
        //this lambda takes the list of generators, filters the ones which contain the hash,
        //finds the first one, maps the hash to the Integer or maps null to it and returns
        return generators
                    .stream()
                    .filter(generator -> generator.getDictionary().containsKey(hash))
                    .findFirst()
                    .map(generator -> generator.getDictionary().get(hash))
                    .orElse(null);
    }

    @Override
    public void run(){
        long endTime = System.currentTimeMillis() + timeout;
        while ((System.currentTimeMillis() < endTime) && (result == null)) {
            result = attemptUpdateResult();
        }
        this.getResult();
    }
}
