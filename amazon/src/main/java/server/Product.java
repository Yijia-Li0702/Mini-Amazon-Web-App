package server;

public class Product {
    long productId;
    String description;
    int count;
    String UPSAccount;
    int x;
    int y;
    public String toString(){
        return this.productId+" "+this.description+" "+this.count;
    }
}
