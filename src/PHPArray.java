import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

// Note: This class does not guarantee that it will be iterated through in the order it was added
// After a deletion, a portion of the table will be rehashed, and sent to the bottom of the underlying Linked List

public class PHPArray <V> implements Iterable <V> {
    // Use Nodes for the internal representation
    // Maps a String to a V

    // Use for iterator, to hold the data
    public static class Pair<V> implements Comparable<Pair<V>> {
        public String key;
        public V value;
        private Pair prev = null;
        private Pair next = null;
        private boolean inuse = true; // Will be set to false by the delete function. Once deleted, unrecoverable.

        @Override
        public String toString(){
            return "Key: " + key + " Value: " + value;
        }

        @Override
        public int compareTo(Pair<V> pair) {
            int returner = ((Comparable)value).compareTo(((Comparable)pair.value));
            return returner;
        }

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

        private boolean isInuse() {return inuse;}

        public String getKey() {return key;}
        public V getvalue() {return value;}

    }

    // Allows the node to just slip out of the array, closing the hole behind it
    private void dropfromLL(Pair p){
        if(p.prev != null && p.next != null){
            // Closes the hole
            p.prev.setNext(p.next);
            p.next.setPrev(p.prev);
        }
        else if(p.prev != null){
            // Deals with the tail
            p.prev.setNext(null);
            bottom = p.prev;
        }
        else if(p.next != null){
            // Deals with the head
            p.next.setPrev(null);
            head = p.next;
        }
        // Otherwise it's just a lonely node
    }

    // Creating my own iterator
    public class PHPArrayIterator implements Iterator {
        private Pair currPair;

        public boolean hasNext(){
            return (currPair != null && currPair.getNext() != null);
        }

        public V next(){
            currPair = currPair.getNext();
            return (V)currPair.getvalue();
        }
    }


    PHPArray.Pair[] pairarray;  // The array that will keep track of your linked list Pairs
    int items_in_list = 0; // Stored to cut down on a linear time operation
    PHPArray.Pair head = null; // The very first element in the list
    PHPArray.Pair bottom = null; // The very last element added to the list
    float alpha = 0;
    Pair eachpair;

    // To make the foreach signifier work
    // Use a buffer head so that you can actually loop through all of the data
    public PHPArrayIterator iterator(){
        PHPArrayIterator it = new PHPArrayIterator();
        Pair<V> itHead = new Pair<V>(null, null);
        itHead.setNext(head);
        it.currPair = itHead;
        return it;
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
        System.out.println("\t\tSize: " + Integer.toString(items_in_list) + " -- resizing array " +
                "from " + Integer.toString(pairarray.length) + " to " + Integer.toString(pairarray.length * 2 + 1));
        Pair[] newpairarray = new PHPArray.Pair[pairarray.length * 2 + 1];
        // Need to rehash all the members of pairarray
        Pair saved = eachpair;
        eachpair = head;
        Pair myPair;
        while((myPair = each()) != null){
            int index = Math.abs(myPair.key.hashCode()) % newpairarray.length;
            // While we see something real in the table, index forward
            while(newpairarray[index] != null && newpairarray[index].isInuse()){
                index ++;
                if(index == newpairarray.length) index = 0;
            }
            newpairarray[index] = myPair;

            myPair = myPair.getNext();
        }

        pairarray = newpairarray;
        eachpair = head;
    }

    // Puts a value into the array, calling put(String, data)
    public void put(int key, V datum){
        put(Integer.toString(key), datum);
    }

    // Puts a value into the array, using Pairs instead of actual data
    private void put(Pair a){
        put(a.getKey(), (V)a.getvalue());
    }

    // Puts a value into the hashmap, using key as the key and datum as the... well
    public void put(String key, V datum){
        PHPArray.Pair myPair = new PHPArray.Pair(key, datum);
        int index;

        // First need to recalculate alpha, and if it's too high, resize the table
        calculateAlpha();
        if(alpha > 0.5){
            growAndRehashTable(); // Cause you know you have to
            calculateAlpha(); // Just to be safe
        }

        index = Math.abs(key.hashCode()) % pairarray.length;

        // After that, deal with plugging the new Pair into the linked list
        if(items_in_list == 0){
            head = myPair;
            bottom = myPair;
            eachpair = myPair;
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

        // Now to insert into the Pair array
        // But also, linear probing
        if(pairarray[index] == null || !pairarray[index].isInuse()){
            if(pairarray[index] != null){
                dropfromLL(pairarray[index]);
                // Don't need to unlink
            }
            // Sweet! No hashing needed
            pairarray[index] = myPair;
        }
        else{
            // Now we have to linearly address
            // We can guarantee we'll always find an empty spot
            // Checks if it's filled, the pair is in use, and it's not the same key
            // If it is the same key, overwrites the pair
            while(pairarray[index] != null && pairarray[index].isInuse() && !pairarray[index].getKey().equals(myPair.getKey())){
                index ++;
                if(index == pairarray.length) index = 0;
            }
            if(pairarray[index] != null && pairarray[index].getKey().equals(key)) dropfromLL(pairarray[index]);
            pairarray[index] = myPair;
        }

        // So then the issue is what happens when you insert something and its spot was empty though not when you
        //      hashed the previous value with the same key. In other words - delete any node after this that has the
        //      same key
        index ++;
        if(index == pairarray.length) index = 0;
        while(pairarray[index] != null){
            if(pairarray[index].getKey().equals(key)){
                if(index == 0) head = pairarray[index].getNext();
                System.out.println("Tried to remove key: " + key + " from index: " + index);
                dropfromLL(pairarray[index]);
                pairarray[index].decomission();
                return;
            }
            index ++;
            if(index == pairarray.length) index = 0;
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
        System.out.println("\tRaw Hash Table Contents:");

        for(int i = 0; i < pairarray.length; i ++){
            StringBuilder ln = new StringBuilder(Integer.toString(i) + ": ");
            Pair place = pairarray[i];
            if(place == null || !place.isInuse()){
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

    // Returns a Pair, calls another method
    public V get(int k){
        return get(Integer.toString(k));
    }

    // Returns a Pair, looking for it inside the Array
    public V get(String key){
        int index = Math.abs(key.hashCode()) % pairarray.length;

        while(pairarray[index] != null){
            Pair mypair = pairarray[index];
            // Have to make sure it's the right node
            if(mypair.getKey().equals(key) && mypair.isInuse()){
                return (V)mypair.getvalue();
            }
            index ++;
            if(index == pairarray.length) index = 0;
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
        int index = Math.abs(key.hashCode()) % pairarray.length;
        while(pairarray[index] != null){
            Pair mypair = pairarray[index];

            // If we found it, we kill it
            if(mypair.getKey().equals(key) && mypair.isInuse()){
                dropfromLL(mypair); // Remove it from the Linked List (Steal Away)
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
    // Creating a generic type that is also Comparable for... nefarious purposes
    //    In terms of analysis, the sort is nlogn, and the grabbing and putting are both N, so it's overall O(nlogn)
    public <T extends Comparable> void sort(){

        eachpair = head;
        ArrayList<V> thepairs = new ArrayList<V>();

        // Grabs all the nodes
        while(eachpair != null){
            thepairs.add((V)eachpair.getvalue());
            eachpair = eachpair.getNext();
        }

        // This is the single worst line of code I've written in my life. I despise its existence.
        Collections.sort((ArrayList<T>)thepairs);

        // And now add all of them as new Pairs back into the array list, resetting it
        head = null;
        bottom = null;
        items_in_list = 0;

        pairarray = new PHPArray.Pair[pairarray.length];
        int i = 0;

        for(V thisT : thepairs){
            put(i, thisT);
            i++;
        }

        // Should be good!
        // Had to reset it, sorry old list
        eachpair = head;
    }

    // asort sorts, but preserving the keys... so I guess it sorts the linked list?
    // It does, Sherif mentioned it in class
    public <T extends Comparable> void asort(){
        eachpair = head;
        ArrayList<Pair<V>> thepairs = new ArrayList<Pair<V>>();

        // Grab all the nodes
        while(eachpair != null){
            thepairs.add(eachpair);
            eachpair = eachpair.getNext();
        }

        // Now we need to sort them
        // Wow, that, like, actually works. Awesome.
        Collections.sort(thepairs);

        // And now add all of them as new Pairs back into the array list, resetting it
        head = null;
        bottom = null;
        items_in_list = 0;
        pairarray = new Pair[pairarray.length];

        for(int i = 0; i < thepairs.size(); i++){
            Pair thispair = thepairs.get(i);
            put(thispair.getKey(), (V)thispair.getvalue());
        }

        // And reset eachpair, and... we're clear and free!
        eachpair = head;
    }

    // array_flip makes the values into keys and the keys into values. Not a mutator, returns a new Array
    // Only works if V is of type String. Otherwise, we'll throw a ClassCast Exception
    public PHPArray<String> array_flip(){
        PHPArray<String> returner = new PHPArray<String>(pairarray.length);
        Pair<V> save = eachpair;
        eachpair = head;
        Pair<V> mypair;

        // Pairs overwrite each other in add, so the last non-unique one with Value 'v' will have key 'v' in the new array
        while((mypair = each()) != null){
            // Man, casting is just the worst. It hurts me to keep doing this.
            Pair<V> newpair = new Pair<V>((String)mypair.getvalue(), (V)mypair.getKey());
            returner.put(newpair);
        }

        // Resetting eachpair
        eachpair = save;
        return returner;
    }

    public void printLinked(){
        Pair save = eachpair;
        eachpair = head;

        Pair thispair;
        while((thispair = each()) != null){
            System.out.println("The next Node has key: " + thispair.getKey() + " and value " + thispair.getvalue());
        }

        eachpair = save;
    }
}
