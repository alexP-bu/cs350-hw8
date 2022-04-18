import java.util.List;
import java.util.Set;

public class TreasureGnome implements Runnable {

    private Hash hasher;
    private Set<Integer> crackedHints;
    private Set<String> uncrackedHashes;
    private int start;
    private int end;

    public TreasureGnome(Set<Integer> crackedHints, int start, int end, Set<String> uncrackedHashes){
        this.hasher = new Hash();
        this.crackedHints = crackedHints;
        this.start = start;
        this.end = end;
        this.uncrackedHashes = uncrackedHashes;
    }

    //THE TREASURE GNOME DEPARTS ITS DEN TO FIND A HASH FOR THE PIRATE
    @Override
    public void run() {
        for (int j = start + 1; j < end; j++) {
            String currHash = hasher.hash(start + ";" + j + ";" + end);
            if (uncrackedHashes.contains(currHash)) {
                uncrackedHashes.remove(currHash);
                crackedHints.add(j); 
            }
        }
    }

}
