import java.io.*;
import java.util.ArrayList;

public class main{

    public static void main(String[] args){

        if(args.length != 4)return;

        try {
            BufferedReader inputReader = new BufferedReader(new FileReader(args[0]));                  /// Argument Initializations
            BufferedWriter outputWriter = new BufferedWriter(new FileWriter("output.txt"));
            double lf1 = Double.parseDouble(args[1]);
            double lf2 = Double.parseDouble(args[2]);
            int searchKey = Integer.parseInt(args[3]);

            ArrayList<String> inputLines = new ArrayList<>();          /// ArrayList used to keep lines of input File, because number of lines is needed

            String line=inputReader.readLine();

            while( (line= inputReader.readLine()) != null)
                inputLines.add(line);

            inputReader.close();
            MyHash1 myHash1 = new MyHash1(inputLines.size(), lf1);      /// Hashtable for Separate Chaining
            MyHash2 myHash2 = new MyHash2(inputLines.size(), lf2);      /// Hashtable for Linear Probing	
            MyHash2 myHash3 = new MyHash2(inputLines.size(), lf2);      /// Hashtable for Double Hashing

            for (String str:inputLines) {
                String[] tokens = str.split(" ");
                String code= tokens[0];
                String NRIC= tokens[1];
                int phoneNumber= Integer.parseInt(tokens[2]);

                MyEmployee employee = new MyEmployee(code, NRIC, phoneNumber);    /// Emmployee is created and put to hashtables 
                myHash1.put(employee);
                myHash2.putLinearProbing(employee);
                myHash3.putDoubleHashing(employee);
            }
            outputWriter.write(args[0].split("\\.")[0]+",LF1="+lf1+",LF2="+lf2+","+searchKey+"\n");
            outputWriter.write("PART1\n");
            myHash1.printTable(outputWriter);
            outputWriter.write("PART2\nHashtable for Linear Probing\n");
            myHash2.printTable(outputWriter);
            outputWriter.write("Hashtable for Double Hashing\n");
            myHash3.printTable(outputWriter);

            double start, end, elapsed;        /// to keep the measured time for searches

            outputWriter.write("Separate Chaining\n");
            start = System.nanoTime();
            int found = myHash1.search(searchKey);
            end = System.nanoTime();
            elapsed = (end-start);
            if(found == -1) outputWriter.write("Key Not Found\n");
            else
                outputWriter.write("Key Found with "+ (found+1)+" comparisons\n");
            outputWriter.write("CPU time taken to search = "+ elapsed+"ns\n");

            outputWriter.write("Linear Probing\n");
            start = System.nanoTime();
            found = myHash2.searchLinear(searchKey);
            end = System.nanoTime();
            elapsed = end-start;
            if(found == -1) outputWriter.write("Key Not Found\n");
            else
                outputWriter.write("Key Found with "+ (found+1)+" comparisons\n");
            outputWriter.write("CPU time taken to search = "+ elapsed+"ns\n");

            outputWriter.write("Double Hashing\n");
            start = System.nanoTime();
            found = myHash3.searchDouble(searchKey);
            end = System.nanoTime();
            elapsed = end-start;
            if(found == -1) outputWriter.write("Key Not Found\n");
            else
                outputWriter.write("Key Found with "+ (found+1)+" comparisons\n");
            outputWriter.write("CPU time taken to search = "+ elapsed +"ns\n");


            outputWriter.flush();
            inputReader.close();
            outputWriter.close();

        }catch (FileNotFoundException fnf){
            System.out.println("File Not Found !!!");
        }catch (IOException io){
            System.out.println("File I/O exception !!!");
        }


    }
}
class MyHash1 {                         /// Hashtable for Separate Chaining 

    private MyLinkedList[] chains;     /// Since number of chains known, array of LinkedList is used
    private int size;

    public MyHash1(int size, double loadFactor){

        this.size = (int)(size / loadFactor);

        chains = new MyLinkedList[this.size];
        for (int i = 0; i <this.size ; i++) {
            chains[i] = new MyLinkedList<>();   /// Every chain is a 'MyLinkedList' list
        }
    }

    public void put(MyEmployee employee){      /// put employee to the end of chain(list)

        int hash = hashFunc(employee.getPhoneNumber());
        chains[hash].add(employee);
    }
    public MyEmployee get(int phoneNumber){         /// get chain index by hash function and linear search that chain to get employee(if exists)

        if(search(phoneNumber) == -1)return null;
        int hash = hashFunc(phoneNumber);
        return chains[hash].search(phoneNumber);
    }

    public int search(int phoneNumber){           /// Once chain index is found, linear search is done to find index of employee

        int hash = hashFunc(phoneNumber);
        return chains[hash].getindex(phoneNumber);

    }
    private int hashFunc(int phoneNumber){

        return (phoneNumber % size);
    }
    public void printTable(BufferedWriter writer) throws IOException {    /// print table to output file which is provided as an argument

        int i = 0;
        for (; i < size; i++){

            writer.write("[Chain "+ i+"]: ");

            MyEmployee current= chains[i].getEmployee();
            if(current != null){

                while(current != null){
                    writer.write(Integer.toString(current.getPhoneNumber()));
                    current = current.getNext();
                    if(current != null)
                        writer.write("---->");
                }
                writer.write("\n");
            }else
                writer.write("Null\n");
        }
    }
}

class MyHash2 {                           /// Hashtable for Linear Probing 

    private int size;
    private MyLinkedList2 list;          /// Different LinkedList used because this time length of list is also known

    public MyHash2(int size, double loadfactor){

        this.size = (int)(size / loadfactor);              
        list = new MyLinkedList2(this.size);            
    }
    public void putLinearProbing(MyEmployee employee){        /// Starting from index 'hash', put employee to first empty slot

        int hash = hash1(employee.getPhoneNumber());

        MyEmployee current = list.get(hash);
        int i = hash;
        if(current == null){
            list.put(employee, hash);
            return;
        }
        while(list.get(i) != null)
            i = (i+1) % this.size;

        list.put(employee, i);
    }

    public void putDoubleHashing(MyEmployee employee){      /// Try first hash1 function, if slot empty put employee
							    /// Otherwise, calculate index by given hash functions until finding an empty slot	
        int hash = hash1(employee.getPhoneNumber());

        if(list.get(hash) == null){
            list.put(employee, hash);
            return;
        }
        int i = 1;
        while(true){

            hash = (hash1(employee.getPhoneNumber()+ i*hash2(employee.getPhoneNumber()))%this.size);
            if(list.get(hash) == null){
                list.put(employee, hash);
                return;
            }
            ++i;
        }
    }
    public MyEmployee getLinearProbing(int phoneNumber){               /// starting from index provided by 'hash1', search employee until an empty slot

        int hash = hash1(phoneNumber);

        while(list.get(hash) != null){

            if(list.get(hash).getPhoneNumber() == phoneNumber)return list.get(hash);
            hash = (hash+1)%this.size;
        }
        return null;
    }
    public MyEmployee getDoubleHashing(int phoneNumber){            /// calculate index from hash functions, check if employee is in that slot

        int index1 = hash1(phoneNumber);
        int index2 = hash2(phoneNumber);
        int i = 0;
        int hash = (index1 + i*index2)%this.size;
        while(true){

            MyEmployee employee = list.get(hash);
            if(employee == null)return null;
            if(employee.getPhoneNumber() == phoneNumber)return employee;
            ++i;
            hash = (index1 + i*index2)%this.size;
        }
    }

    private int hash1(int phoneNumber){

        return phoneNumber % this.size;

    }
    private int hash2(int phoneNumber){

        return 1+(phoneNumber % (this.size-1));
    }

    public void printTable(BufferedWriter writer) throws IOException {

        MyEmployee[] temp = list.getList();

        for (int i = 0; i < this.size; i++) {

            writer.write("[" + i + "]--->");
            if (temp[i] == null) writer.write("null\n");
            else writer.write(temp[i].getPhoneNumber()+"\n");
        }
    }
    public int searchDouble(int phoneNumber){                       /// like getDoubleHashing function, search employee and return index(or #comparisons)

        int index1 = hash1(phoneNumber);
        int index2 = hash2(phoneNumber);
        int i = 0;
        int hash = (index1 + i*index2)%this.size;

        while(true){

            MyEmployee current = list.get(hash);
            if(current == null)return -1;
            if(current.getPhoneNumber() == phoneNumber)return i;
            ++i;
            hash = (index1 + i*index2)%this.size;
        }
    }
    public int searchLinear(int phoneNumber){			/// like getLinearProbing function, search employee and return index(or #comparisons)

        int hash = hash1(phoneNumber);
        int i=0;
        while(true){

            MyEmployee current = list.get(hash);
            if(current == null)return -1;
            if(current.getPhoneNumber() == phoneNumber)return i;

            hash = (hash+1)%this.size;
            ++i;
        }
    }
}
class MyEmployee { 			/// Employee class to keep emmployees

    private String employeeCode;
    private String NRIC;
    private int phoneNumber;
    private MyEmployee next;

    public MyEmployee(String employeeCode, String NRIC, int phoneNumber) {
        this.employeeCode = employeeCode;
        this.NRIC = NRIC;
        this.phoneNumber = phoneNumber;
        this.next = null;
    }
    public int getPhoneNumber() {
        return phoneNumber;
    }
    public MyEmployee getNext() {
        return next;
    }
    public void setNext(MyEmployee next) {
        this.next = next;
    }
}
class MyLinkedList<T> {   			/// LinkedList for Separate Chaining

    private MyEmployee employee;

    public MyLinkedList(){

        employee = null;
    }
    public MyEmployee getEmployee() {
        return employee;
    }

    public void add(MyEmployee employee){

        if(this.employee == null)
            this.employee = employee;
        else {

            MyEmployee current = this.employee;
            while(current.getNext() != null)current=current.getNext();
            current.setNext(employee);
        }
    }
    public int getindex(int phoneNumber) {

        MyEmployee current = this.employee;
        int count = 0;
        while (current != null){
            if(current.getPhoneNumber() == phoneNumber)
                return count;
            current = current.getNext();
            ++count;
        }
        return -1;
    }
    public MyEmployee search(int phoneNumber){

        int index = getindex(phoneNumber);
        if(index == -1)return null;

        MyEmployee current = this.employee;
        int i = 0;
        while(i++ != index)current = current.getNext();
        return current;
    }
}
class MyLinkedList2 {				/// LinkedList for LinearProbing and DoubleHashing

    private int size;
    private MyEmployee[] list;

    public MyLinkedList2(int size){

        this.size = size;
        list = new MyEmployee[this.size];

        for (int i = 0; i < size ; i++)
            list[i] = null;
    }
    public void put(MyEmployee employee, int n){

        list[n] = employee;
    }
    public MyEmployee get(int n){
        return list[n];
    }
    public MyEmployee[] getList(){
        return this.list;
    }
}

