public class A {
    public void doNothing(){

    }

    public String getHello(){
        return "Hello";
    }

    public int getSumOneToN(int n){
        int result = 0;
        for(int i=1; i<=n; i++)
            result += i;
        return result;
    }

    public int multiply(int q){
        if (q == 0)
            return 0;
        int v = q;
        for(int i=0; i< 10; i++){
            for(int j=0; j< v; j++)
                v -= j;
            v += i*10;
        }
        return multiply(v-1);
    }

    public String concatHello(String arg){
        String result = getHello();
        result += arg;
        return result;
    }
    public static void main(String[] args){
        A iA = new A();
        System.out.println(iA.concatHello("World"));
        int a = iA.multiply(3);
    }
}

