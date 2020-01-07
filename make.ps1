Write-Output "Compilation starts"
cd ../../
javac examples\JADEAuction\JINBuyerAgent.java
javac .\examples\JADEAuction\JINSellerAgent.java
javac .\examples\JADEAuction\ItemState.java
javac .\examples\JADEAuction\ItemType.java
javac .\examples\JADEAuction\Item.java
cd examples\JADEAuction\
Write-Output "FINISH !"