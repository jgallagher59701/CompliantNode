import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

	boolean[] followees;		// Graph connection; these nodes send this node txs
	Set<Transaction> validTxs;	// This nodes starting set; assumed to be valid
	
	Set<Transaction> allTxs;	// The set of all transactions

	HashMap<Transaction, Set<Integer>> candidatesFromFollowee; // all Txs received from which followees
	
	HashMap<Integer, Set<Transaction>> followeeCandidates;	// all followees and the Txs they have sent

	final double p_malicious;
	final double p_txDistribution;
	final int numRounds;
	
	int round;
	int numFollowees;
	int seenThreshold;
	
	boolean[] malicious;
	
    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
    	this.p_malicious = p_malicious;
    	this.p_txDistribution = p_txDistribution;

    	this.numRounds = numRounds;
    	
    	round = 0;
    	numFollowees = 0;
    	seenThreshold = 0;
    	
    	candidatesFromFollowee = new HashMap<Transaction, Set<Integer>>();
    }

    /** {@code followees[i]} is true if and only if this node follows node {@code i} */
    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
    	this.followees = followees;
    	
    	for (boolean f: followees) {
    		if (f)
    			++numFollowees;
    	}
    	
    	seenThreshold = (int)((numFollowees - 1) * p_malicious);	// truncate decimal value
    	
    	System.err.println("Followees: " + numFollowees);
    	System.err.println("Seen Threshold: " + seenThreshold);
    }

    /** initialize proposal list of transactions */
    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
    	allTxs = validTxs = pendingTransactions;
    }

    /**
     * @return proposals to send to my followers. REMEMBER: After final round, behavior of
     *         {@code getProposals} changes and it should return the transactions upon which
     *         consensus has been reached.
     */
    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
    	//return validTxs;
    	
    	if (round < numRounds)
    		return allTxs;
    	else {
			for (Transaction tx : candidatesFromFollowee.keySet()) {
				Set<Integer> senders = candidatesFromFollowee.get(tx);
				if (senders.size() >= seenThreshold) {
					validTxs.add(tx);
				}
			}

    		return validTxs;
    	}
    }

    /** receive candidates from other nodes. */
    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
    	++round;
    	    	
    	// Alg0. Everything is good if it comes from a followee
    	/*
    	for (Candidate c: candidates) {
    		if (followees[c.sender])
    			validTxs.add(c.tx);
    	}
    	*/

    	// Alg1. 
    	// The initial Txs are all valid
    	// A candidate Tx is held in a buffer if it's not on the valid list
    	// A candidate from some threshold number of followees is assumed valid
    	// Only Txs assumed valid are sent to followers
    	
    	for (Candidate c: candidates) {
    		if (!followees[c.sender])
    			continue;
    		
    		allTxs.add(c.tx);		// Send along every tx we get
    		
    		// Record all inbound Txs along with the number of nodes that have sent them
			Set<Integer> senders = candidatesFromFollowee.get(c.tx);
			if (senders == null) {
				senders = new HashSet<Integer>();
				senders.add(c.sender);
				candidatesFromFollowee.put(c.tx, senders);
			} else if (!senders.contains(c.sender)) {
				senders.add(c.sender);
				candidatesFromFollowee.put(c.tx, senders);
			}
		}
    	
    	// Alg2. Identify malicious nodes
    	// A node that does not send any Txs in the first round is malicious
    	
	}
    
}
