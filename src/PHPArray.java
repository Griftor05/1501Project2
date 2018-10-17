import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

// Note: This class does not guarantee that it will be iterated through in the order it was added
// After a deletion, a portion of the table will be rehashed, and sent to the bottom of the underlying Linked List

public class PHPArray<V> implements Iterable<V> {
    // Use Nodes for the internal representation
    // Maps a String to a V

    // Use for iterator, to hold the data
    public static class Pair<V> {
        public String key;
        public V value;
        private Pair prev = null;
        private Pair next = null;
        private boolean inuse = true; // Will be set to false by the delete function. Once deleted, unrecoverable.

        public Pair(String k, V d){
            key = k;
            value = d;
        }

        private Pair getPrev() {return this.prev;}
        private Pair getNext() {return this.next; }

        private void setPrev(Pair prev) {
            this.prev = prev;
        }

        private void setNext(Pair next) {
            this.next = next;
        }

        private void decomission(){inuse = false;}
<<<<<<< HEAD

        // Allows the node to just slip out of the array, closing the hole behind it
        private void dropfromLL(){
            if(prev != null && next != null){
                // Closes the hole
                prev.next = next;
                next.prev = prev;
            }
            else if(prev != null){
                // Deals with the tail
                prev.next = null;
            }
            else if(next != null){
                // Deals with the head
                next.prev = null;
            }
            // Otherwise, there's nothing to be done
        }

=======
>>>>>>> d7baa2d96dccbd3e612627f871685b6652f423c4
        private boolean isInuse() {return inuse;}

        public String getKey() {return key;}
        public V getvalue() {return value;}
    }

    // Creating my own iterator
    public class PHPArrayIterator implements Iterator {
        private PHPArray.Pair currPair;

        public boolean hasNext(){
            return (currPair != null && currPair.getNext() != null);
        }

        public Pair next(){
            currPair = currPair.getNext();
            return currPair.getPrev();
        }
    }


    PHPArray.Pair[] pairarray;  // The array that will keep track of your linked list Pairs
    int items_in_list = 0; // Stored to cut down on a linear time operation
    PHPArray.Pair head = null; // The very first element in the list
    PHPArray.Pair bottom = null; // The very last element added to the list
    float alpha = 0;
    Pair eachpair;
    PHPArrayIterator theiterator;

    // To make the foreach signifier work
    public PHPArrayIterator iterator(){
        theiterator.currPair = head;
        return theiterator;
    }

    // Alpha is the load factor of the underlying hash map
    public void calculateAlpha(){
        alpha = (float)items_in_list / (float)pairarray.length;
    }

    // Constructor
    public PHPArray(int s){
        pairarray = new PHPArray.Pair[s];
    }

    // Used when the load factor gets too high. Transitions to a larger array.
    private void growAndRehashTable(){
        Pair[] newpairarray = new PHPArray.Pair[pairarray.length * 2 + 1];
        // Need to rehash all the members of pairarray
        Pair myPair = head;
        while(myPair != null){
            int index = myPair.key.hashCode() % newpairarray.length;
            // While we see something real in the table, index forward
            while(newpairarray[index] != null && newpairarray[index].isInuse()){
                index ++;
                if(index == newpairarray.length) index = 0;
            }
            newpairarray[index] = myPair;

            myPair = myPair.getNext();
        }

        pairarray = newpairarray;
    }

    // Puts a value into the array, calling put(String, data)
    public void put(int key, V datum){
        put(Integer.toString(key), datum);
    }

    // Puts a value into the hashmap, using key as the key and datum as the... well
    public void put(String key, V datum){
        PHPArray.Pair myPair = new PHPArray.Pair(key, datum);
        int index = key.hashCode() % pairarray.length;

        // First deal with plugging the new Pair into the linked list
        if(items_in_list == 0){
            head = myPair;
            bottom = myPair;
            eachpair = myPair;
            theiterator.currPair = myPair;
            items_in_list ++;
        }
        else if(items_in_list == 1){
            bottom = myPair;
            head.setNext(myPair);
            myPair.setPrev(head);
            items_in_list ++;
        }
        else{
            bottom.setNext(myPair);
            myPair.setPrev(bottom);
            bottom = myPair;
            items_in_list ++;
        }

        // Need to recalculate alpha, and if it's too high, resize the table
        calculateAlpha();
        if(alpha > 0.5){
            growAndRehashTable(); // Cause you know you have to
            calculateAlpha(); // Just to be safe
        }

        // Now to insert into the Pair array
        // But also, linear probing
        if(pairarray[index] == null || !pairarray[index].isInuse()){
            // Sweet! No hashing needed
            pairarray[index] = myPair;
        }
        else{
            // Now we have to linearly address
            // We can guarantee we'll always find an empty spot
            while(pairarray[index] != null && pairarray[index].isInuse()){
                index ++;
                if(index == pairarray.length) index = 0;
            }
            pairarray[index] = myPair;
        }
    }

    // each will return each pair in the linked list, by order added, reset will send it back to the head
    public Pair each(){
        if(eachpair == null) return null;
        // Else
        Pair returner = eachpair;
        eachpair = eachpair.getNext();
        return returner;
    }

    // Resets the each() iterator to the head of the arraylist
    public void reset(){
        eachpair = head;
    }

    // returns an arraylist of all the values in the list
    public ArrayList<V> values(){
        // Save the each iterator
        Pair saved = eachpair;
        reset();
        ArrayList<V> returner = new ArrayList<V>();
        Pair curr;
        while((curr = each()) != null){
            // Y'know, this is the first time casting has ever felt like a really bad idea to me
            // Ah well. It works now, at least.
            returner.add((V)curr.getvalue());
        }
        // Restore your each iterator, friend-o
        eachpair = saved;
        return returner;
    }

    // returns and arraylist of all the keys in the list. Copy and pasted code from above, anyone?
    public ArrayList<String> keys(){
        // Save the each iterator
        Pair saved = eachpair;
        reset();
        ArrayList<String> returner = new ArrayList<String>();
        Pair curr;
        while((curr = each()) != null){
            returner.add(curr.getKey());
        }
        // Restore your each iterator, friend-o
        eachpair = saved;
        return returner;
    }

    // Bet you're happy I kept track of this var now, aren't you?
    public int length(){
        return items_in_list;
    }

    // Prints out the underlying table. Good for encapsulation? No. Good for debugging? Probably.
    public void showTable(){
        for(int i = 0; i < pairarray.length; i ++){
            StringBuilder ln = new StringBuilder(Integer.toString(i) + ": ");
            Pair place = pairarray[i];
            if(place == null){
                ln.append("null");
            }
            else{
                ln.append("Key: ");
                ln.append(place.getKey() + " Value: ");
                ln.append(place.getvalue());
            }

            System.out.println(ln);
        }
    }
<<<<<<< HEAD

    // Returns a Pair, calls another method
    public V get(int k){
        return get(Integer.toString(k));
    }

    // Returns a Pair, looking for it from
    public V get(String key){
        int index = key.hashCode() % pairarray.length;
        while(pairarray[index] != null){
            Pair mypair = pairarray[index];
            // Have to make sure it's the right node
            if(mypair.getKey().equals(key) && mypair.isInuse()){
                return (V)mypair.getvalue();
            }
            index ++;
        }
        // If it's not in there, just return null
        return null;
    }

    // Just the wrapper for the int key
    public void unset(Integer key){
        unset(Integer.toString(key));
    }

    // Going to delete an element, so... hopefully it'll work
    public void unset(String key){
        // Just going to set it as not there inside the array, but gotta find it first
        // Don't have to rehash all below elements because we leave the object there
        int index = key.hashCode() % pairarray.length;
        while(pairarray[index] != null){
            Pair mypair = pairarray[index];

            // If we found it, we kill it
            if(mypair.getKey().equals(key) && mypair.isInuse()){
                mypair.dropfromLL(); // Remove it from the Linked List (Steal Away)
                mypair.decomission(); // Now it's dead
                if(head == mypair){
                    // have to move it down
                    head = mypair.getNext();
                }
                if(bottom == mypair){
                    // have to move it back up
                    bottom = mypair.getPrev();
                }

                return;
            }
            index++;
        }

    }

    // Gonna pull all of the objects out and sort the list
    public void sort(){
        Pair save = eachpair;
        eachpair = head;
        ArrayList<V> thepairs = new ArrayList<V>();

        while(eachpair != null){
            thepairs.add((V)eachpair.getvalue());
        }

        Class<V> type;
        Class<Comparable> ctype;

        if(ctype.isInstance(type)){

        }

    }
}
