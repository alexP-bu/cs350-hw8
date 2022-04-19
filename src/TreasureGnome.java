import java.util.Set;

public class TreasureGnome implements Runnable {

    private final Hash hasher;
    private Set<Integer> nextPhaseHints;
    private Set<Integer> allCrackedHints;
    private Set<String> uncrackedHashes;
    private int start;
    private int end;

    public TreasureGnome(Set<Integer> nextPhaseHints, 
                         Set<Integer> allHints, 
                         int start, int end, 
                         Set<String> uncrackedHashes){
        this.hasher = new Hash();
        this.allCrackedHints = allHints;
        this.start = start;
        this.end = end;
        this.uncrackedHashes = uncrackedHashes;
        this.nextPhaseHints = nextPhaseHints;
    }

    //THE TREASURE GNOME DEPARTS ITS DEN TO FIND A HASH FOR THE PIRATE
    @Override
    public void run() {
        for (int j = start + 1; j < end; j++) {
            String currHash = hasher.hash(start + ";" + j + ";" + end);
            if (uncrackedHashes.contains(currHash)) {
                System.out.println("FOUND: " + currHash + " : " + j);
                uncrackedHashes.remove(currHash);
                nextPhaseHints.add(j);
                allCrackedHints.add(j); 
            }
        }
    }

}
