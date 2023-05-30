import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


// a java 8 stream is a Monad
// it differs from InputStream/OutputStream entirely
// they can use functional programming:
//  a monad is a structure that that represents computation as a sequence of steps
//  eg multiple functions are called in a row.  Intermediate functions can be called in a list, and then a terminal operation must be last


//Interfaces for lambda expressions
//  the lambda expressions will implement an interface.  The interface MUST only have 1 single method
//  when using an interface and a lambda implementation of it, the lambda expression will implement an abstract method in the interface
//  the interface must therefore only have ONE abstract method.  it can have other methods with implementations though
interface IntFace {
    int comp(int x, int y);
}

interface StringCat {
    String cat(String s, String x, String z);
}

interface SayWhat {
    String what="what";
    void sayWhat();
}

class SayX {
    int x;
    SayX(int x){
        this.x=x;
    }
    void setX(int x){
        this.x=x;
    }
    void speak() {
        System.out.println("X=" + this.x);
    }
}

public class Main {
    public static void main(String[] args) {
        //we require an interface for this lambda expression to implement.
        //essentially this replaces the bulky code used when making an anonymous class.
        IntFace anonCompare = new IntFace(){
            @Override
            public int comp(int x, int y) {
                return Integer.compare(x,y);
            }
        };

        IntFace lambdaCompare = (int x, int y) -> { return Integer.compare(x,y); } ;    //this is a statement lambda.  below is an expression lambda, a clear format
        IntFace lambdaCompareInferred = (x, y) -> Integer.compare(x,y);     //the types of the parameters are implied by the interface!
        IntFace cleanerCompareInferred = Integer::compare;                //we dont even need a lot of the excess syntax.  we can use the "method reference" format too
        StringCat sc = (str, str2, str3) -> { return str+str2+str3; };
        System.out.println(sc.cat("hey","there","mate"));
        //cleanerCompareInferred.comp(5,10);
        // if the lambda expression is implementing a method with zero parameters, just use empty brackets and then call it
        SayWhat whatLambda = () -> System.out.println("WHAT");

        /*System.out.println("Int compare:\t" + anonCompare.comp(5,10));
        System.out.println("Int compare:\t" + lambdaCompare.comp(5,10));
        System.out.println("void lambda:");*/
        whatLambda.sayWhat();

        // implementation of a new threaded task
        Runnable testRunner = () -> { System.out.println("runnable!");};
        testRunner.run();






        //INTERMEDIATE OPERATIONS ONLY EXECUTED WHEN A TERMINAL OPERATION IS PRESENT

        //create an anonymous stream of strings and find the first object in the stream
        // then if something exists at this index, print it
        Stream.of("a1","b2","c3")
                .findFirst()
                .ifPresent(System.out::println);


        //create a list of string objects and then get the stream of these objects
        //then filter them with a given lambda function (s.startsWith)
        // then sort and loop over each one in the resulting list, printing it
        List<String> aList = Arrays.asList("A1", "C3", "B1", "C1", "C2", "C0");
        aList.stream()
                .filter(s -> s.startsWith("C"))     //BONUS:  s -> s.startsWith("C") is a lambda expression.  left of the arrow is the parameters, right is the functionality we want to perform
                .map(String::toUpperCase)           //        so this lambda passes each object in the list and we get back the result of "startsWith("C")" on that object
                .sorted()
                .forEach(System.out::println);


        //parallelStream can be created on lists and sets, and these can be interacted with my multiple threads


        System.out.println("Int streams:");
        //useful for replacing old fashioned for loops.  rangeClosed instead of range gives us the last number inclusive
        IntStream.rangeClosed(1,5)
                .forEach(System.out::println);

        //this allows us to replicate a for (int i=0; i < 5; i+=2) loop
        //iterate() creates an infinite stream, so limit() must be used to stop it after X iterations
        IntStream.iterate(0, i -> i + 2)
                .limit(3)
                .forEach(System.out::println);

        //create a stream of runnable objects
        //the objects are returned by mapping the intstream objects in a range to objects
        //the objects are generated by a lambda expression which creates a runnable using the integer from the range
        Stream<Runnable> runnables = IntStream.rangeClosed(1,3)
                .mapToObj(i -> (Runnable) () -> System.out.println("RUN: " + i));
        runnables.forEach(Runnable::run);

        //use mapToObj to map the value of i onto an anonymous constructor of the class SayX
        //then add this SayX instance to the stream.
        //finally, use sx.forEach to call 'speak' for each object in the stream
        Stream<SayX> sx =
                IntStream.rangeClosed(1,5)
                .mapToObj(SayX::new); //was originally "new SayX(i)".  this method reference format is shorter
        sx.forEach(SayX::speak);    //STREAMS CAN ONLY BE REUSED LIKE THIS IF THEY HAVE NOT ALREADY HAD A TERMINAL OPERATION DONE ON THEM

        System.out.println("array of ints stream:");
        //with lambda functions:
        Arrays.stream(new int[] {1,2,3,4,5})
                .map(n -> 2 * (n + 1))    //map this function onto each value, so the array becomes 4, 6, 8, 10, 12
                .average()              //average all values in the stream
                .ifPresent(System.out::println);    //checks for a return val from .average?


        System.out.println("String to int stream:");
        //sometimes we may want to modify the primitive type of a stream
        //  this stream + lambda expression allow us to test if objects in a stream are integers or not
        //  if they are, it returns the integer.  Otherwise, -1
        Stream.of("1", "2", "3", "hey")
                .map(s -> s.substring(0))       //we grab the first character of each object
                .mapToInt(s -> {{               //we map a lambda function to the mapToInt function
                        try {
                            return Integer.parseInt(s);     //we attempt to parse each string to an int
                        } catch(Exception e) { return -1; }  //if we can't, we return -1
                    }
                })
                .forEach(System.out::println);


        //the below prints out OUT of order (the order we might have expected)
        // this is because we execute filter AND forEach, for each object in the stream
        //rather than filtering the whole stream, then forEaching it
        // *** changing this to only return true for "a2" in the filter lambda leads to the 'foreach' part
        // *** only executing for THAT stream object.
        // the filter intermediate method allows us to define circumstances under which the object is or is not carried forward
        // in the execution of the stream.
        Stream.of("d2", "a2", "b1", "b3", "c")
                .filter(s -> {
                    System.out.println("filter: " + s);
                    if (s == "a2") {
                        return true;
                    }else{
                        return false;
                    }
                })
                .forEach(s -> System.out.println("forEach: " + s));

        //THE ORDER OF INTERMEDIATE OPERATIONS MATTERS!
        // if there was a map() and a filter(), then putting the filter before the map prevents us executing the map code
        // unnecessarily on objects which fill ultimately be filtered out

        //split a string into words.  then filter it (for no reason) so that only 'my' is carried forward
        //then map all succeeding strings so that they are replaced by a regex
        //then print

        //UNLIKE FOREACH, map is only called ONCE.  This is to make it operate faster for large numbers of objects
        //for this reason it is best to filter before mapping
        String ss = "hello my name is ross";
        Stream.of(ss.split(" "))
                .filter(s -> {
                    return s.equals("my");
                })
                .map(s -> s.replaceAll("m", s + "X"))
                .forEach(System.out::println);


        //create a simple class
        class Person {
            String name; int age;
            Person(String n, int a) {
                this.name=n; this.age=a;
            }
            void setName(String n) { this.name = n; }
            void setAge(int a) { this.age = a; }
            String getName() { return name; }
            Integer getAge() { return age; }
        }

        //make a list of Person objects
        List<Person> people = Arrays.asList(
                new Person("Ross", 29),
                new Person("Dave", 30),
                new Person("Alan", 50),
                new Person("Chris", 29),
                new Person("Ross", 30)
        );

        //make a new list by streaming the existing one, filtering it, making a collection, and then converting it to a new list
        List<Person> pplOver30 = people.stream()
                .filter(p -> p.name.equals("Ross"))     //also works as "p -> p.age > 30".  THIS IS FUNCTIONAL PROGRAMMING?
                .collect(Collectors.toList());

        pplOver30.forEach((p) -> System.out.println(p.getName() + " is " + p.getAge())); //terminal method

        //now lets group these objects by certain criteria
        // here we return more than just 1 object type.  we are returning a list of {integer, <Person>}
        Map<Integer, List<Person>> groupedAges = people.stream()
                .collect(Collectors.groupingBy(p -> p.age));

        groupedAges.forEach((age, p) -> System.out.format("age %s: %s\n", age, p));


        //get only people over the age of 29, and map their name to the stream going forward
        // then use collect and .joining to join each object with the given prefix - delimiter - suffix
        String phrase = people.stream()
                .filter(p -> p.age > 29)
                .map(p -> p.name)
                .collect(Collectors.joining(" and ", "", " are over 29"));    //the prefix begins the result and the suffix ends it.  the delimiter is placed between every
                                                                                                    // object in the stream - exclusively.  the delimiter string does not go before first or after last object

        //System.out.println(phrase);

        //begin with a list of Person objects
        //map their names to a string and continue
        //collect those names into a Set.
        //a set contains only unique values, so the "Ross" duplicate is dropped
        Set<String> s1 = people.stream()
                .map(p -> p.name)   // since we specified that it's  set of Strings, we have to map from a stream of Person objects to a stream of strings
                .collect(Collectors.toSet());


        //collectors can be used to calculate aggregate functions such as the average of ints
        Double averageAge = people.stream()
                .collect(Collectors.averagingInt(p -> p.age));
        //System.out.println("avg age: " + averageAge);


        //    KEY     VALUE
        Map<Integer, String> madMaps = people.stream()
                .collect(Collectors.toMap(
                        p -> p.age,
                        p -> p.name,
                        (name1, name2) -> name1 + "," + name2   // this pattern is repeated for any time 1 age has multiple names against it
                        //this is called a MERGE FUNCTION and is an optional parameter that prevents an IllegalStateException
                        //doesn't matter what "name1/name2" are called.  they are just the duplicate VALUES in the map
                        //the KEY doesn't need anything doing with it
                ));
        System.out.println(madMaps);    //{50=Alan, 29=Ross,Chris, 30=Dave,Ross}
    }
}