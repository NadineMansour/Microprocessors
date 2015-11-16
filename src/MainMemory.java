
public class MainMemory {
	int memory_size =  (int)Math.pow(2, 16);
	int access_time;
	int l;
	/*
	 * For now we are only caring about the instructions so the content of the main memory will be stored in an array of strings
	 * later we should divide this array into 2 half (data and instructions)  
	 * */
	public String[] content;
	
	public MainMemory(int at , int l){
		this.access_time = at;
		this.l = l;
		content = new String[memory_size];
		//originally the main memory is empty
		for (int i = 0; i < content.length; i++) {
			content[i] = "";
		}
	}
	
	public void load_program(String[]program, int start){
		// Hadeel + Mogh + Badr
		// save the given code in the main memory starting from index "start"
		// dont forget to handle the case where one line can have more than one instruction
	}
	
	public String read(int index){
		// Hadeel + Mogh + Badr
		// we have to read the entire block and not the specific byte in address = index 
		return null;
	}
	
	public void write (int index , String block){
		
	}
	
	

}
