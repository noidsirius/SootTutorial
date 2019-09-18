public class NullPointerExample {

    public void methodA(){
        Data d = new Data("Something");
        use(d);
        d = null;
        use(d);
    }

    public void methodB(Data param){
        use(param);
    }


    public void methodC(){
        Data mayNullData = null;
        Data notNull = null;
        Data mustNull = new Data("Must Be null");
        use(mayNullData);
        use(notNull);
        use(mustNull);
        Object o = new Object();
        if(o.hashCode() % 2 == 0) {
            mayNullData = new Data("I'm not null anymore");
            notNull = new Data("Me neither");
            mustNull = null;
        }
        else {
            notNull = new Data("Not even in this branch");
            mustNull = null;
        }
        use(mayNullData);
        use(notNull);
        use(mustNull);
    }

    public void methodD(){
        Data nullData = getNullString();
        use(nullData);
        Data helloWorldData = getHelloWorld();
        use(helloWorldData);
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

    class Data {
        String message;
        public Data(String message){
            this.message = message;
        }
    }

}

