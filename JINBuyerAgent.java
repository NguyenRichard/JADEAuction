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
		System.out.println("Hallo! Buyer-agent "+getAID().getName()+" is ready.");

		//Ajouter la liste des objets
		itemsToBuy = new ArrayList<Item>();

		Item item = new Item("La joconde", ItemType.PAINTING, ItemState.GOOD, 0, 0, false);
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
		return 0;
	}

	/**
	   Inner class RequestPerformer.
	   This is the behaviour used by Book-buyer agents to request seller
	   agents the target book.
	 */
	private class RequestPerformer extends CyclicBehaviour {
		private MessageTemplate mt; // The template to receive replies

		public void action() {

			mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
			ACLMessage msg = myAgent.receive(mt);


			if(msg != null){
				Item object = null;
				try{
					object = (Item) msg.getContentObject();
				}
				catch(Exception e){}

				if(object != null){
					for(Item i : itemsToBuy){
						if(i.type == object.type){
							int price = bid(object);
							if(price > 0){
								ACLMessage reply = msg.createReply();
								reply.setContent(Integer.toString(price));
								myAgent.send(reply);
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
