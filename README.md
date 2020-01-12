# JADEAuction

To compile all the classes, you can launch make.ps1

This project have 2 different agents: A seller and a buyer.
To launch an auction, you must launch all the buyers first.
At the moment, they will all want to buy one "La Joconde" but you can initialize it differently in JINBuyerAgent.java.
Then, you can launch a seller who will want to sell 2 "La Joconde", same thing, you can change that in JINSellerAgenr.java.
The auction will be directed by the seller who send a message to all the agent on the system.
He will actualize his list of interested buyers during the auction to send less and less message.
Once there is only one interested buyer or the time is out, the winner is the buyer who set the best price at the moment.
The seller will send a message to the buyer so that he removes the item from his wish list.
After that, the seller will once again launch a new auction with his next item until he is out of item.
