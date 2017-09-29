package stevens.cs562.esqlcompiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @version 1.10 07 April 2017
 * @authors SOWMYA VIJAYAKUMAR, KISHAN GAJJAR
 */
public class GenerateJavaCode {
	
	/*
	 * class variable to store path for generated files
	 */
	private static final String FILE_PATH = "src//stevens//cs562//generated//";
	
	/**
	 * generateMfStructureJavaFile generates MFStructure Java class file containing instance variables  
	 * @param mf_structure_grouping_attributes
	 * @param mf_structure_f_vect
	 * @return
	 */
	public static boolean generateMfStructureJavaFile(
			HashMap<String, String> mf_structure_grouping_attributes,
			LinkedHashMap<String, String> mf_structure_f_vect) {

		File file_mf_structure = new File(FILE_PATH + "MfStructure.java");
		PrintWriter writer = null;

		try {

			file_mf_structure.createNewFile();
			writer = new PrintWriter(new BufferedWriter(new FileWriter(file_mf_structure)));

			writer.println("package stevens.cs562.generated;");
			writer.println();
			writer.println("public class MfStructure {");

			/* parse mf_structure_grouping_attributes for writing grouping attributes */
			for (Map.Entry<String, String> entry : mf_structure_grouping_attributes.entrySet()) {
				
				writer.println("\t public " + entry.getValue() + " " + entry.getKey() + ";");
			}

			/* parse mf_structure_f_vect for writing aggregate functions */
			for (Map.Entry<String, String> entry : mf_structure_f_vect.entrySet()) {
				
				writer.println("\t public " + entry.getValue() + " " + entry.getKey() + ";");
			}

			writer.println("}");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
			
		} finally {
			writer.close();
		}

		return true;
	}

	/**
	 * generateQueryProcessorJavaFile generates Query Processor Java class file containing algorithm to generate output 
	 * @param input
	 * @param mf_structure_grouping_attributes
	 * @param mf_structure_f_vect
	 * @return
	 */
	public static boolean generateQueryProcessorJavaFile(HashMap<String, String> input,
			LinkedHashMap<String, String> mf_structure_grouping_attributes,
			LinkedHashMap<String, String> mf_structure_f_vect) {
		
		File file_mf_structure = new File(FILE_PATH + "QueryProcessor.java");
		PrintWriter writer = null;
		
		String query = "SELECT * FROM Sales ";
		boolean MF = true;
		
		/*
		 * append any WHERE condition if any
		 */
		if (input.get("where_condition").length() > 0) {
			query += "WHERE " + input.get("where_condition");
		}
		
		/* generate String array of condition_vect and grouping_attribues */
		String temp_cond_vect[] = input.get("condition_vect").split(",");
		String temp_grp_attr[] = input.get("grouping_attributes").split(",");

		/*
		 * logic to check if input query is MF or EMF
		 */
		for (String c : temp_cond_vect) {

			for (String g : temp_grp_attr) {

				if (c.contains(g)) {
					MF = false;
				}
			}
		}
		
		/*
		 * steps to write to QueryProcessor file
		 */
		try {

			file_mf_structure.createNewFile();
			writer = new PrintWriter(new BufferedWriter(new FileWriter(file_mf_structure)));

			/*
			 * packages and import statements are written here
			 */
			writer.println("package stevens.cs562.generated;");
			writer.println();
			writer.println("import java.sql.Connection;");
			writer.println("import java.sql.ResultSet;");
			writer.println("import java.sql.SQLException;");
			writer.println("import java.sql.Statement;");
			writer.println("import java.util.ArrayList;");
			writer.println("import java.util.List;");

			/*
			 * class, instance variable and class variables are defined here
			 */
			writer.println("public class QueryProcessor {");
			writer.println();
			writer.println("private List<MfStructure> mf_table = new ArrayList<MfStructure>();");
			writer.println();
			writer.println("\t public static void main(String[] args) {");
			writer.println();
			writer.println("\t\t QueryProcessor QP = new QueryProcessor();");
			writer.println("\t\t QP.getTable();");
			writer.println("\t\t QP.displayTable();");
			writer.println();
			writer.println("\t }");
			writer.println("\t public void getTable() {");
			writer.println("\t\t try {");
			writer.println("\t\t Connection dbconnection = DatabaseConnection.getDBInstance();");
			writer.println("\t\t Statement st = dbconnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);");

			writer.println("\t\t ResultSet rs = st.executeQuery(\"" + query + "\");");

			/*
			 * logic to check if there is aggregate function with grouping variable zero
			 */
			boolean zero_grouping_var = false;
			ArrayList<String> grouping_variable_zero = new ArrayList<String>();
			
			for (String f_vect : mf_structure_f_vect.keySet()) {
				
				if (Utility.getGroupingVariable(f_vect) == 0) {
					zero_grouping_var = true;
					grouping_variable_zero.add(f_vect);

				}
			}

			writer.println("\t\t while(rs.next()) {");
			writer.println("\t\t\t boolean isexists = false;");
			writer.println("\t\t\t for (MfStructure current_row : mf_table){");
			
			ArrayList<String> grp_condition = new ArrayList<String>();

			/* 
			 * logic to update in-memory table according to group by attribute
			 */
			for (String grp_attr : mf_structure_grouping_attributes.keySet()) {
				
				if (mf_structure_grouping_attributes.get(grp_attr).equals("String")) {
					grp_condition.add("rs.getString(\"" + grp_attr + "\").equals(current_row." + grp_attr + ")");
				}
				
				if (mf_structure_grouping_attributes.get(grp_attr).equals("int")) {	
					grp_condition.add("rs.getInt(\"" + grp_attr + "\") == current_row." + grp_attr);
				}
			}
			
			for (String s : grp_condition) {
				
				if (grp_condition.indexOf(s) == 0) {
					writer.print("\t\t\t\t if(" + s);
				} else {
					writer.print(" && " + s);
				}
			}
			writer.println("){");
			writer.println("\t\t\t\t\t isexists = true;");
			
			/*
			 * perform aggregates for grouping variable zero
			 */
			if (zero_grouping_var) {
				
				for (String s : grouping_variable_zero) {
					
					/*
					 * implementation for Average function 
					 */
					if (Utility.getAggregate(s).equals("avg")) {
						writer.print("\t\t\t\t\t");
						
						/* update value from database */
						writer.println("current_row.sum_" + Utility.getAttribute(s) + "_0 += rs.getInt(\"" + Utility.getAttribute(s) + "\");");
						writer.print("\t\t\t\t\t");
						writer.println("current_row.count_" + Utility.getAttribute(s) + "_0 ++");
					}
					
					/*
					 * implementation for Sum function 
					 */
					if (Utility.getAggregate(s).equals("sum")) {
						writer.print("\t\t\t\t\t");
						
						/* update value from database */
						writer.println("current_row." + s + " += rs.getInt(\"" + Utility.getAttribute(s) + "\");");
					}
					
					/*
					 * implementation for count function 
					 */
					if (Utility.getAggregate(s).equals("count")) {
						writer.print("\t\t\t\t\t");
						
						/* update value from database */
						writer.println("current_row." + s + " ++;");
					}	
					
					/*
					 * implementation for Maximum function 
					 */
					if (Utility.getAggregate(s).equals("max")) {
						writer.print("\t\t\t\t\t");
						
						/* check maximum condition and update value from database */
						writer.println("if (current_row." + s + " < rs.getInt(\"" + Utility.getAttribute(s) + "\")) {");
						writer.println("\t\t\t\t\t\t current_row." + s + " = rs.getInt(\"" + Utility.getAttribute(s) + "\"); ");
						writer.print("\t\t\t\t\t }");
					}
					
					/*
					 * implementation for Minimum function 
					 */
					if (Utility.getAggregate(s).equals("min")) {
						writer.print("\t\t\t\t\t");
						
						/* check minimum condition and update value from database */
						writer.println("if (current_row." + s + " > rs.getInt(\"" + Utility.getAttribute(s) + "\")) {");
						writer.println("\t\t\t\t\t\t current_row." + s + " = rs.getInt(\"" + Utility.getAttribute(s) + "\"); ");
						writer.print("\t\t\t\t\t }");
					}
				}
			}
			
			writer.println("\t\t\t\t }");
			writer.println("\t\t\t }");
			
			/*
			 * add new row to in-memory table according to group by attribute
			 */
			writer.println("\t\t\t if(isexists == false){");
			writer.println("\t\t\t\t MfStructure mf_struct = new MfStructure();");
			
			/* get attribute from sales table according to grouping attribute */
			for (String key : mf_structure_grouping_attributes.keySet()) {
				
				/* check database and store value */
				if (mf_structure_grouping_attributes.get(key).equals("String")) {
					writer.println("\t\t\t\t mf_struct." + Utility.getAttribute(key) + " = rs.getString(\"" + Utility.getAttribute(key) + "\");");
				}
				
				if (mf_structure_grouping_attributes.get(key).equals("int")) {
					writer.println("\t\t\t\t mf_struct." + Utility.getAttribute(key) + " = rs.getInt(\"" + Utility.getAttribute(key) + "\");");
				}
				
			}
			
			/* get attribute from sales table according to the grouping variable zero f_vect and store value */
			for (String f_vect : mf_structure_f_vect.keySet()) {
				
				if (Utility.getGroupingVariable(f_vect) == 0) {
				
					/* logic to store Average function values in memory */
					if (Utility.getAggregate(f_vect).equals("avg")) {
						writer.println("\t\t\t\t mf_struct.sum_" + Utility.getAttribute(f_vect) + "_0 = rs.getInt("
								+ Utility.getAttribute(f_vect) + "\");");
						writer.println("\t\t\t\t mf_struct.count_" + Utility.getAttribute(f_vect) + "_0 = 1;");
					}
					
					/* logic to store Sum function values in memory */
					if (Utility.getAggregate(f_vect).equals("sum")) {
						writer.println("\t\t\t\t mf_struct." + f_vect + " =  rs.getInt(\""
								+ Utility.getAttribute(f_vect) + "\");");
					}
					
					/* logic to store Count function values in memory */
					if (Utility.getAggregate(f_vect).equals("count")) {
						writer.println("\t\t\t\t mf_struct." + f_vect + " = 1;");
					}
					
					/* logic to store Minimum function values in memory */
					if (Utility.getAggregate(f_vect).equals("max")) {						
						writer.println("\t\t\t\t\t\t mf_struct." + f_vect + " = rs.getInt(\"" + Utility.getAttribute(f_vect) + "\"); ");
					}
					
					/* logic to store Maximum function values in memory */
					if (Utility.getAggregate(f_vect).equals("min")) {
						writer.println("\t\t\t\t\t\t mf_struct." + f_vect + " = rs.getInt(\"" + Utility.getAttribute(f_vect) + "\"); ");
					}
					
				} else {
					writer.println("\t\t\t\t mf_struct." + f_vect + " = 0;");
				}

			}
			
			/* add current mf_struct object to ArrayList mf_table */
			writer.println("\t\t\t\t mf_table.add(mf_struct);");
			
			writer.println("\t\t\t }");
			writer.println("\t\t }");

			/* 
			 * combine all aggregates of respective grouping variable in ArrayList
			 */
			HashMap<Integer, ArrayList<String>> f_vect = new HashMap<Integer, ArrayList<String>>();
			String[] f_vect_split = input.get("f_vect").split(",");
			
			for (String f_vect_current : f_vect_split) {
				
				ArrayList<String> list = new ArrayList<String>();
				Integer key = Utility.getGroupingVariable(f_vect_current);
				
				if (f_vect.keySet().contains(key)) {
					list = f_vect.get(key);
				}
				
				list.add(f_vect_current);
				f_vect.put(key, list);
			}

			String[] condition_vect = input.get("condition_vect").trim().split(",");
			
			/* 
			 * creating number of while loop(s) according to grouping_variable
			 */
			for (int i = 1; i <= Integer.parseInt(input.get("grouping_variable")); i++) {
				
				writer.println("\t\t rs.beforeFirst();");
				writer.println("\t\t while(rs.next()) {");
				writer.println("\t\t\t for (MfStructure current_row : mf_table){");

				/*
				 * loop within group if query is of MF type
				 */
				if (MF) {
					
					ArrayList<String> inner_grp_condition = new ArrayList<String>();

					for (String grp_attr : mf_structure_grouping_attributes.keySet()) {
						
						if (mf_structure_grouping_attributes.get(grp_attr).equals("String")) {
							inner_grp_condition.add("rs.getString(\"" + grp_attr + "\").equals(current_row." + grp_attr + ")");
						}
						
						if (mf_structure_grouping_attributes.get(grp_attr).equals("int")) {
							inner_grp_condition.add("rs.getInt(\"" + grp_attr + "\") == current_row." + grp_attr);
						}
						
					}
					
					/*
					 * combining all the grouping attribute
					 */
					for (String s : inner_grp_condition) {
						
						if (inner_grp_condition.indexOf(s) == 0) {
							writer.print("\t\t\t\tif(" + s);
						} 
						
						else {
							writer.print(" && " + s);
						}
					}
					
					writer.println("){");

				}
				
				/* trim last && from the condition */
				writer.print("\t\t\t\t\t if( " + condition_vect[i - 1].trim());
				writer.println(" ){");
				
				/* parse ArrayList of grouping variable condition */ 
				ArrayList<String> fvect = new ArrayList<String>();
				
				if (f_vect.keySet().contains(i)) {
					fvect = f_vect.get(i);
				}
					
				boolean sum = false, count = false;
				
				/* 
				 * calculate aggregate for respective grouping variable
				 */
				for (String s : fvect) {
					
					s = s.trim();
					
					if (Utility.getAggregate(s).equals("avg")) {
						
						if (sum == false) {
							writer.println("\t\t\t\t\t current_row.sum_" + Utility.getAttribute(s) + "_" + i + " += rs.getInt(\""
									+ Utility.getAttribute(s) + "\");");
							sum = true;
						}
						
						if (count == false) {
							writer.println("\t\t\t\t\t current_row.count_" + Utility.getAttribute(s) + "_" + i + " ++;");
							count = true;
						}
						
					}
					
					if (Utility.getAggregate(s).equals("sum")) {
						
						if (sum == false) {
							writer.println("\t\t\t\t\t current_row." + s + " += rs.getInt(\"" + Utility.getAttribute(s) + "\");");
							sum = true;
						}
					}
					
					if (Utility.getAggregate(s).equals("count")) {
						
						if (count == false) {
							writer.println("\t\t\t\t\t current_row." + s + " ++;");
							count = true;
						}
					}
					
					if (Utility.getAggregate(s).equals("max")) {
							
						writer.println("if (current_row." + s + " < rs.getInt(\"" + Utility.getAttribute(s) + "\") {");
						writer.println("\t\t\t\t\t\t current_row." + s + " = rs.getInt(\"" + Utility.getAttribute(s) + "\"); ");
						writer.print("\t\t\t\t\t }");
					}
					
					if (Utility.getAggregate(s).equals("min")) {
						
						writer.println("if (current_row." + s + " > rs.getInt(\"" + Utility.getAttribute(s) + "\") {");
						writer.println("\t\t\t\t\t\t current_row." + s + " = rs.getInt(\"" + Utility.getAttribute(s) + "\"); ");
						writer.print("\t\t\t\t\t }");
					}
					
				}

				writer.println("\t\t\t\t\t }");
				
				if (MF) {
					writer.println("\t\t\t\t }");
				}

				writer.println("\t\t\t }");
				writer.println("\t\t }");	
			}

			writer.println("\t\t } catch (SQLException e) {");
			writer.println("\t\t\t e.printStackTrace();");
			writer.println("\t\t }");
			writer.println("\t }");
			
			String having_condition = input.get("having_condition").trim();
			
			/*
			 * displayTable function starts from here
			 */
			writer.println("\t public void displayTable() {");
			
			String[] select_attributes = input.get("select_attributes").split(",");
			writer.print("\t\t System.out.println(\"");
			for (String s : select_attributes) {
				writer.print(s + "\\t");
			}
			writer.print("\");");
			writer.println();
			writer.println("\t\t for(MfStructure m : mf_table){");

			if (!having_condition.isEmpty()) {
				writer.println("\t\t\t if (" + having_condition + ") {");
			}
			
			for (String s : select_attributes) {
				s=s.trim();
				if(Utility.containsOperator(s)){
					writer.println("\t\t\t System.out.print(" + s.trim() + "+\"\\t\");");	
					continue;
				}
				
				else if (Utility.getAggregate(s).equals("avg")) {
					writer.println("\t\t\t if(m.count_" + Utility.getAttribute(s) + "_" + Utility.getGroupingVariable(s) + "!=0){");
					writer.println("\t\t\t System.out.print((m.sum_" + Utility.getAttribute(s) + "_"
							+ Utility.getGroupingVariable(s) + "/m.count_" + Utility.getAttribute(s) + "_"
							+ Utility.getGroupingVariable(s) + ")+\"\\t\\t\");");
					writer.println("\t\t\t }");
					writer.println("\t\t\t else{");
					writer.println("\t\t\t System.out.print(\"0\"+\"\\t\\t\");");
					writer.println("\t\t\t }");
				} 
				
				else {
					writer.println("\t\t\t System.out.print(m." + s.trim() + "+\"\\t\");");
				}
			}

			writer.println("\t\t\t System.out.println();");
			
			if (!having_condition.isEmpty()) {
				writer.println("\t\t\t }");
			}

			writer.println("\t\t }");
			writer.println("\t }");
			writer.println("}");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
			
		} finally {
			writer.close();
		}
		
		return true;
	}
}