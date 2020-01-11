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

import examples.JADEAuction.ItemType;
import examples.JADEAuction.ItemState;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;


public class JINSellerAgent extends Agent {
	// The catalogue of books for sale (maps the title of a book to its price)
	private Hashtable catalogue;
	// The GUI by means of which the user can add books in the catalogue
	//private BookSellerGui myGui;

	Item itemToSell;

	private AID[] buyerAgents; // A terme inutile, remplacé par interestedBuyers
	private List<AID> interestedBuyers = new ArrayList<AID>();

	private boolean isAuctionStarted = false;

	// Put agent initializations here
	protected void setup() {
		//initialiser l'objet à vendre.
		itemToSell = new Item("La joconde", ItemType.PAINTING, ItemState.GOOD, 1000, 500, true);

		addBehaviour(new TickerBehaviour(this, 1000) {
			protected void onTick() {


				// Update the list of seller agents
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();

				sd.setType("selling");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);

					//Message de setup
					if (!isAuctionStarted){
						System.out.println(getAID().getName() + " est ici pour essayer de vendre " + itemToSell.name + ".");
						System.out.println("Le vendeur a trouvé " + result.length + " personnes sur le reseau");
						isAuctionStarted = true;
					}


					buyerAgents = new AID[result.length];
					for (int i = 0; i < result.length; ++i) {
						buyerAgents[i] = result[i].getName();
						interestedBuyers.add(result[i].getName());
					}
				}
				catch (FIPAException fe) {
					fe.printStackTrace();
				}

				// Perform the request
				myAgent.addBehaviour(new RequestPerformer());
			}
		});
	}

	// Put agent clean-up operations here
	/*protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Close the GUI
		//myGui.dispose();
		// Printout a dismissal message
		System.out.println("Seller-agent "+getAID().getName()+" terminating.");
	}*/
	/**
	   Inner class RequestPerformer.
	   This is the behaviour used by Book-buyer agents to request seller
	   agents the target book.
	 */
	private class RequestPerformer extends Behaviour {
		private AID bestBuyer; // The agent who provides the best offer
		private int bestPrice;  // The best offered price
		private boolean auctionIsOn; // The auction is currently running if true
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;
		private int repliesCnt = 0; // The counter of replies from interested agents.

		public void action(){

			switch (step) {
			case 0:
				auctionIsOn = true;
				// Send the propose to all buyers
				ACLMessage propose = new ACLMessage(ACLMessage.PROPOSE);
				for (AID buyer : interestedBuyers) {
					propose.addReceiver(buyer);
				}

				try {
					propose.setContentObject(itemToSell);
				} catch (Exception e){

				}

				propose.setConversationId("item-auction");
				propose.setReplyWith("propose"+System.currentTimeMillis()); // Unique value
				myAgent.send(propose);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("item-auction"),
						MessageTemplate.MatchInReplyTo(propose.getReplyWith()));


				step = 1;

				//Thread qui gère la durée d'une enchère
				Thread t = new Thread(new Runnable(){
					@Override public void run(){
						try{
							Thread.sleep(5000);
							auctionIsOn = false;
						} catch (Exception e){

						}
					}
				});
				t.start();

				break;
			case 1:
				if(!auctionIsOn){
					step = 3;
				}
				// Receive all proposals from buyer agents
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						// This is an offer
						int price = Integer.parseInt(reply.getContent());
						if(price < 0){
							interestedBuyers.remove(reply.getSender());
						}
						if (price > bestPrice) {
							// This is the best offer at present
							bestPrice = price;
							itemToSell.currentBestPriceProposed = price;
							bestBuyer = reply.getSender();
							itemToSell.bestBuyer = bestBuyer;
							System.out.println("C'est " + bestBuyer.getName() + " qui fait actuellement la meilleure offre a " + bestPrice + " euros");
						}
					}

					repliesCnt++;

					if (repliesCnt >= interestedBuyers.size()) {
						step = 2;
					}

				}
				else {
					block();
				}
				break;
			case 2:
				repliesCnt = 0;

				propose = new ACLMessage(ACLMessage.PROPOSE);
				for (AID buyer : interestedBuyers){
					propose.addReceiver(buyer);
				}

				try {
					propose.setContentObject(itemToSell);
				} catch (Exception e){

				}

				propose.setConversationId("item-auction");
				propose.setReplyWith("propose"+System.currentTimeMillis()); // Unique value
				myAgent.send(propose);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("item-auction"),
						MessageTemplate.MatchInReplyTo(propose.getReplyWith()));

				if(!auctionIsOn || interestedBuyers.size() <= 1){
					step = 3;
				}
				else{
					step = 1;
				}
				break;
			case 3:
				System.out.println("3 .. 2 .. 1 .. " + itemToSell.bestBuyer.getName() + " a remporte " + itemToSell.name);
				myAgent.doDelete();
				break;
			}
		}

		public boolean done() {
			if (step == 2 && bestBuyer == null) {
				System.out.println("Attempt failed: nobody wants to buy " + itemToSell.name);
			}
			return ((step == 2 && bestBuyer == null) || step == 4);
		}
	}
}
