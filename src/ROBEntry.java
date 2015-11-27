
public class ROBEntry {
	String type;
	int destination;
	int value;
	boolean ready;
	
	public ROBEntry(String t , int d ){
		this.type = t;
		this.destination = d;
		value = 0;
		ready = false;
	}
}
