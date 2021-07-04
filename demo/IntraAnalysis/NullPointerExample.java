public class NullPointerExample {
    Object rObj = new Object();

    public void methodA(){
        Data d = new Data("Something");
        use(d);
        d = null;
        use(d);
    }

    public void methodB(){
        Data mayNullData = getData("mayNullData");
        if(rObj.hashCode() % 2 == 0) {
            mayNullData = null;
        }
        else{
            mayNullData.hashCode();
        }
        mayNullData.hashCode();
    }

    public void methodC(){
        Data nullData = getData("nullData");
        Data willBeNullData = getData("willBeNullData");
        for(int i=0; i<rObj.hashCode()%2; i++){
            willBeNullData = nullData;
            willBeNullData.hashCode();
            nullData = null;
        }
        willBeNullData.hashCode();
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

    public Data getData(String s){
        return new Data(s);
    }

    class Data {
        String message;
        public Data(String message){
            this.message = message;
        }
    }

}

