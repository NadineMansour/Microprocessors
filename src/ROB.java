
public class ROB {
	int head;
	int tail;
	int size;
	int contains;
	ROBEntry[] entries;
	
	
	public ROB(int size){
		this.size = size;
		head = 0;
		tail = 0;
		contains=0;
		entries = new ROBEntry[size];
	}
	
	int add_entry(String type , int destination){
		entries[tail] = new ROBEntry(type, destination);
		int result = tail;
		tail++;
		contains++;
		if(tail==size)
			tail=0;
		return result;
	}
	
	void update_value(int index , int value){
		entries[index].value = value;
		entries[index].ready = true;
	}
	
	int[] commit(int x){
		
		if(x == head && entries[head].ready){
			int[] result = {entries[head].destination, entries[head].value};
			//remove the entry from the ROB
			entries[head].destination = -10000;
			entries[head].ready = false;
			entries[head].type = null;
			entries[head].value = -10000;
			head++;
			contains--;
			if(head==size)
				head=0;
			return result;
		}
		return null;
	}
	
	void print_ROB(){
		System.out.println("ROB");
		System.out.println("Head:"+head+" Tail:"+tail);
		for (int i = 0; i < entries.length; i++) {
			if(entries[i]!=null)
				System.out.println("Type:"+entries[i].type+" Destination:"+entries[i].destination+" Value:"+entries[i].value+" Ready:"+entries[i].ready);
		}
	}
}
