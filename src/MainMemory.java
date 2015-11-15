
public class MainMemory {
	int memory_size =  (int)Math.pow(2, 16);
	int access_time;
	/*
	 * For now we are only caring about the instructions so the content of the main memory will be stored in an array of strings
	 * later we should divide this array into 2 half (data and instructions)  
	 * */
	public String[] content;
	
	public MainMemory(int at){
		this.access_time = at;
		content = new String[memory_size];
		//originally the main memory is empty
		for (int i = 0; i < content.length; i++) {
			content[i] = " ";
		}
	}
	
	public void load_program(String[]program, int start){
		// save the given code in the main memory starting from index "start"
	}
	
	public String read(int index){
		return null;
	}
	
	public void write (int index , String block){
		
	}
	
	

}
