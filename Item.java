package examples.JADEAuction;

import jade.core.AID;
import java.util.ArrayList;
import java.util.List;
import examples.JADEAuction.ItemType;
import examples.JADEAuction.ItemState;
import java.io.Serializable;


public class Item implements Serializable{
    public String name;
    public ItemType type;
    public ItemState state;
    public int initialPrice;
    public int price;
    public boolean isFake;
    public AID bestBuyer;

    public Item (ItemType type, ItemState state, int initialPrice, int price, boolean isFake){
        this.type = type;
        this.state = state;
        this.initialPrice = initialPrice;
        this.price = price;
        this.isFake = isFake;
    }
}
