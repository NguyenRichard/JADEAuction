/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A.

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation,
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
 *****************************************************************/

package examples.JADEAuction;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Random;

public class JINBuyerAgent extends Agent {
	// The title of the book to buy
	private List<Item> itemsToBuy;

	private float newItemRatio; //Ratio price from Initial price when the state is new.

	private float goodItemRatio; //Ratio price from Initial price when the state is good.

	private float usedItemRatio; //Ratio price from Initial price when the state is used.

	private float probabilityPayMore; //Probability to pay more

	private float satisfaction; //Value from 0 to 100 to describe the satisfaction of the user.


	// Put agent initializations here
	protected void setup() {
		// Printout a welcome message
		System.out.println("Dimanche matin, 11h, dans la charmante bourgade d'Evry.");

		Random rnd = new Random();


		newItemRatio = 0.8f + (float)rnd.nextInt(20)/100;
		goodItemRatio = 0.6f + (float)rnd.nextInt(20)/100;
		usedItemRatio = 0.4f + (float)rnd.nextInt(20)/100;

		probabilityPayMore = (float)rnd.nextInt(10)/100;

		System.out.println(getAID().getName()+"("+newItemRatio+","+goodItemRatio+","+usedItemRatio+","+probabilityPayMore+")"+" est la car il veut faire de bonnes affaires.");

		//Ajouter la liste des objets
		itemsToBuy = new ArrayList<Item>();

		Item item = new Item("La joconde", ItemType.PAINTING, ItemState.GOOD, 1000, 0, false);
		itemsToBuy.add(item);

		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		dfd.setName(getAID());
		sd.setType("selling");
		sd.setName("JADEAuction");
		dfd.addServices(sd);
		try{
			DFService.register(this,dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		addBehaviour(new RequestPerformer());
	}


	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Buyer-agent "+getAID().getName()+" terminating.");
	}

	/**
		return price proposal or -1
	**/
	private int bid(Item item) {
		//TODO
		Random rnd = new Random();

		int maxPrice = item.initialPrice;

		switch(item.state){
			case NEW:
				maxPrice = Math.round(maxPrice*newItemRatio);
			break;
			case GOOD:
				maxPrice = Math.round(maxPrice*goodItemRatio);
			break;
			case USED:
				maxPrice = Math.round(maxPrice*usedItemRatio);
			break;
			default:
			break;
		}

		if(item.currentBestPriceProposed < maxPrice){
			return rnd.nextInt(maxPrice - item.currentBestPriceProposed) + item.currentBestPriceProposed; //Pour ne pas proposer en dessous du prix de la meilleure offre

		}else{
			if((float)rnd.nextInt(100)/100 < probabilityPayMore){
				float overpriceRatio = (float)rnd.nextInt(10)/100;
				int overprice = maxPrice + Math.round(maxPrice*overpriceRatio);
				if(overprice > item.currentBestPriceProposed){
					System.out.println(getAID().getName()+" joue le tout pour le tout! Il mise "+overpriceRatio*100+"% de plus que sa limite!");
					if(overprice <= item.initialPrice){
						return overprice;
					}
					else{
						return item.initialPrice;
					}
				}
			}
		}

		return -1;

		//return rnd.nextInt(1000 - item.currentBestPriceProposed) + item.currentBestPriceProposed;
		//return (int)(Math.random() * 100) + 1;
	}

	/**
	   Inner class RequestPerformer.
	   This is the behaviour used by Book-buyer agents to request seller
	   agents the target book.
	 */
	private class RequestPerformer extends CyclicBehaviour {
		private MessageTemplate mt; // The template to receive replies

		public void action() {

			//mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
			ACLMessage msg = myAgent.receive(mt);


			if(msg != null){

				if(msg.getPerformative() == ACLMessage.PROPOSE){

					Item object = null;
					try{
						object = (Item) msg.getContentObject();
					}
					catch(Exception e){}

					if(object != null){

						for(Item i : itemsToBuy){

							if(i.type == object.type){ //A CHANGER
								boolean canPropose = true;
								if (object.bestBuyer != null){
									if (object.bestBuyer.getName().compareTo(getAID().getName()) == 0){
										canPropose = false;
									}
								}
								if (canPropose){
									int price = bid(object);
									ACLMessage reply = msg.createReply();
									reply.setContent(Integer.toString(price));
									myAgent.send(reply);
									if(price > 0){
										try{
											Thread.sleep(200);
											//Simule un temps d'attente
										} catch (Exception e){

										}
									}
								}

								return;
							}
						}

						//if the agent is not interested

						int price = -1; //send not interested to seller.
						ACLMessage reply = msg.createReply();
						reply.setContent(Integer.toString(price));
						myAgent.send(reply);

					}
				}
				else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
					Item object = null;
					try{
						object = (Item) msg.getContentObject();
					}
					catch(Exception e){}
					if(object != null){

						for(Item i : itemsToBuy){
							if(i.type == object.type){
								itemsToBuy.remove(i);
								if(itemsToBuy.size() <= 0){
								 	System.out.println(getAID().getName()+" a acheté tout les objets qu'il voulait.");
								}	
								return;
							}
						}
					}
				}
			}
			else{
				block();
			}
		}

	}  // End of inner class RequestPerformer
}
