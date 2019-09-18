public class UsageExample {

    public void methodA(int n){
        String s = String.valueOf(n);
        System.out.println(s);
    }

    public void methodB(int n){
        String s = String.valueOf(n);
        Printer p = new Printer();
        p.println(s);
    }

    class Printer {
        public void println(String line){
            // do nothing
        }
    }
}

