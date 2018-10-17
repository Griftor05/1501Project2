import java.util.ArrayList;
import java.util.Collections;

public class TestClass {
    public class DummyClass {
    }

    public static void main(String[] args){
        ArrayList<DummyClass> testlist = new ArrayList<DummyClass>();

        try{
            Collections.sort(testlist);
        }
        catch{

        }
    }
}
