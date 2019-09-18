public class AliasExample {

    Data instanceData = new Data("InstanceField");
    static Data staticData = new Data("StaticField");

    public void methodA(){
        Data data1 = new Data("S");
        Data data2;
        Object o = new Object();
        if(o.hashCode() % 2 == 0) {
            data2 = data1;
        }
    }

    public void methodB(){
        staticData = instanceData;
    }

    public void methodC(){
        Data data = staticData;
        data.message = "Test";
        use(data);
    }

    public void main(){
        methodA();
        methodB();
        methodC();
    }


    public void use(Data d){
        System.out.println(d.message);
    }

    public Data getNullString(){
        return null;
    }

    public Data getHelloWorld(){
        return new Data("HelloWorld");
    }

    static class Data {
        String message;
        public Data(String message){
            this.message = message;
        }
    }

}

