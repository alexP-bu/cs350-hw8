public class UnHash {
    
    private Hash hasher;
    private Long timeout; 
    private int cur;

    public UnHash(Long timeout){
        this.timeout = timeout;
        this.hasher = new Hash();
    }

    /** 
     * @param hash
     * @return Integer
     */
    //unhash any hash for any algorithm tbh
    public Integer unhash(String hash){
        if(timeout == null){
            while(!hasher.hash(cur).equals(hash)){
                cur++;
            }
        }else{
            long endTime = System.currentTimeMillis() + timeout;
            while(!hasher.hash(cur).equals(hash) && (System.currentTimeMillis() < endTime)){
                cur++;
            }
        }

        if(hasher.hash(cur).equals(hash)){
            return cur;
        }else{
            return null;
        }
    }
}