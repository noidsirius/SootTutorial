public class A {
    public static void main(String[] args){
        InnerA1 innerInstance = new InnerA1();
        innerInstance.init("Hello");
        String a = innerInstance.getStringField();
    }
}

class InnerA1{
    private String stringField;
    private int intField;
    public void init(String input){
        this.stringField = input;
        intField = 0;
    }

    public String getStringField() {
        return stringField;
    }
}
