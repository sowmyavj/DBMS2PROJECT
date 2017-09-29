package stevens.cs562.esqlcompiler;

public class Utility {

	//Assuming string to be of format avg_qty_1 or qty_1 or avg_qty_0
	static public Integer getGroupingVariable(String str){
		
		int count_underscore= str.length() - str.replace("_", "").length();
		String split_str[]= str.split("_");
		if (count_underscore == 2){ //&& !split_str[2].equals("0")){
			return  Integer.parseInt(split_str[2]);
		}
		else {
			Character grouping_var= split_str[1].charAt(0);
			if(count_underscore == 1 && Character.isDigit(grouping_var)){
				return Integer.parseInt(split_str[1]);
			}
		}
		return -1;
	}
	//Assuming string to be of format avg_qty_1 or qty_1 or avg_qty_0
	static public String getAttribute(String str){
		int count_underscore= str.length() - str.replace("_", "").length();
		String split_str[]= str.split("_");
		if (count_underscore == 2){
			return split_str[1]; //avg_qty_1 or avg_qty_0
		}
		return split_str[0]; // qty_1
		
	}
	//Assuming string to be of format avg_qty_1 or avg_qty
	static public String getAggregate(String str){
		int count_underscore= str.length() - str.replace("_", "").length();
		String split_str[]= str.split("_");
		if (count_underscore == 2){
			if(split_str[0].matches("m\\.")){
				String[] split= split_str[0].split(".");
				return split[1];
				
			}else{
				return split_str[0]; //avg_qty_1 or avg_qty_0
			}
		}
		return "Error";
		
	}
	static public String remove_grouping_var(String str){
		int index_of_underscore= str.indexOf("_");
		if (index_of_underscore == 1){
			return str.substring(index_of_underscore+1);
		}
		else 
			return "Error";
	}
	static public Integer getGroupingVariable_condition(String str){
		String [] c = str.trim().split("");
		return(Integer.parseInt(c[0]));
	}
	
	static public boolean isOperator(String str){
		if(str.matches("\\>") || str.matches("\\<") || str.matches("\\+") || str.matches("\\-")|| str.matches("\\/")
				|| str.matches("\\*") || str.matches("\\=")  	){
			return true;
		}
		return false;
	}
	
	static public boolean containsOperator(String str){
		if(str.contains(">") || str.contains("<") || str.contains("+") || str.contains("-")|| str.contains("/")
				|| str.contains("*") || str.contains("=")  	){
			return true;
		}
		return false;
	}
	
}
