package examples.JADEAuction;

import jade.core.AID;
import java.util.ArrayList;
import java.util.List;
import examples.JADEAuction.ItemType;
import examples.JADEAuction.ItemState;
import java.io.Serializable;


public class Item implements Serializable{
    public ItemType type;
    public ItemState state;
    public float initialPrice;
    public float price;
    public boolean isFake;
    public AID bestOffer;
    public List<AID> interestedAgents;

    public Item (ItemType type, ItemState state, float initialPrice, float price, boolean isFake){
        this.type = type;
        this.state = state;
        this.initialPrice = initialPrice;
        this.price = price;
        this.isFake = isFake;

        interestedAgents = new ArrayList<AID>();
    }
}
