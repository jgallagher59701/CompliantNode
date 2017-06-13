import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

	boolean[] followees;		// Graph connection; these nodes send this node txs
	Set<Transaction> validTxs;	// This nodes starting set; assumed to be valid
	
	// Set<Transaction> allTxs;	// The set of all transactions

	HashMap<Transaction, Set<Integer>> candidatesFromFollowee; // all Txs received from which followees
	
	HashMap<Integer, Set<Transaction>> followeeCandidates;	// all followees and the Txs they have sent

	final double p_graph;
	final double p_malicious;
	final double p_txDistribution;
	final int numRounds;
	
	final int nodes = 100;
	
	int round;
	int numFollowees;
	int roundsThreshold;
	
	boolean[] malicious;
	
    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
    	this.p_graph = p_graph;
    	this.p_malicious = p_malicious;
    	this.p_txDistribution = p_txDistribution;

    	this.numRounds = numRounds;
    	
    	round = 0;
    	numFollowees = 0;
    	roundsThreshold = 0;
    	
    	candidatesFromFollowee = new HashMap<Transaction, Set<Integer>>();
    	followeeCandidates = new HashMap<Integer, Set<Transaction>>();
    	
    	malicious = new boolean[nodes];
    	for (int i = 0; i < nodes; ++i)
    		malicious[i] = false;
    	
    	if (p_graph == 0.1)
    		roundsThreshold = 4;
    	else if (p_graph == 0.2) {
    		if (p_txDistribution == 0.01)
    			roundsThreshold = 4;
    		else
    			roundsThreshold = 3;
    	}
    	// TODO: Evaluate different values for this. 
    	// TODO: Maybe make this a function of the actual number of followees?
    	roundsThreshold = 4;
    }

    /** {@code followees[i]} is true if and only if this node follows node {@code i} */
    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
    	this.followees = followees;
    	
    	for (boolean f: followees) {
    		if (f)
    			++numFollowees;
    	}
    	
    	System.err.println("Followees: " + numFollowees);
    }

    /** initialize proposal list of transactions */
    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
    	validTxs = pendingTransactions; // TODO allTxs not used
    }

    /**
     * @return proposals to send to my followers. REMEMBER: After final round, behavior of
     *         {@code getProposals} changes and it should return the transactions upon which
     *         consensus has been reached.
     */
    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
    	return validTxs;
    }

    /** receive candidates from other nodes. */
    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
    	++round;
    	    	
    	// Alg2. Identify and ignore malicious nodes   	
    	for (Candidate c: candidates) {
    		if (!followees[c.sender] || malicious[c.sender])
    			continue;
    		    		
    		// Record all of the senders for each cacndidate transaction
    		Set<Transaction> txs = followeeCandidates.get(c.sender);
			if (txs == null) {
				txs = new HashSet<Transaction>();
				txs.add(c.tx);
				followeeCandidates.put(c.sender, txs);
			} else if (!txs.contains(c.tx)) {
				txs.add(c.tx);
				followeeCandidates.put(c.sender, txs);
			}
			
			// Record all inbound Txs along with the nodes that have sent them
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
    	
    	// Heuristic tests for malicious nodes
    	// Test not impl'd: Nodes communicating transactions randomly

    	for (int i = 0; i < nodes; ++i) {
    		if (followees[i]) {
    			// Nodes not communicating at all - done
    			// Nodes communicating only at the final round - done
    			// Nodes communicating only at even or odd rounds - done
    			// These three are really subsets of 'Fails to send any transactions
    			// on any give round, where the first round is the most telling'
    			Set<Transaction> txs  = followeeCandidates.get(i);
    			if (txs == null)
    				malicious[i] = true;
    			
    			// Nodes communicating only its own initial transactions
    			// look up all the txs that node i has sent. Are they all from node i?
    			// Test this only after the nth round (since all nodes send only their
    			// own Txs during the first round), where n is a function of p_graph.
    			// 
    			// This seems dependent on p_graph, p_txdist and p_malicious
    			if (round > roundsThreshold && txs != null) {
					for (Transaction tx : txs) {
						Set<Integer> senders = candidatesFromFollowee.get(tx);
						if (senders.contains(i) && senders.size() == 1)
							malicious[i] = true;
					}
    			}
    		}
    	}
    	
    	for (Candidate c: candidates) {
    		if (followees[c.sender] && !malicious[c.sender])
    			validTxs.add(c.tx);	
    	}
	}
    
}
