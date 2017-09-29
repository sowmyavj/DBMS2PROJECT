package stevens.cs562.esqlcompiler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @version 1.10 07 April 2017
 * @authors SOWMYA VIJAYAKUMAR, KISHAN GAJJAR
 */
public class ESQLCompiler {

	/*
	 * instance variables for storing inputs from file or console
	 */
	private File input_file;
	private HashMap<String, String> input = new HashMap<String, String>();
	private LinkedHashMap<String, String> mf_structure_grouping_attributes = new LinkedHashMap<>();
	private LinkedHashMap<String, String> mf_structure_f_vect = new LinkedHashMap<>();

	/**
	 * main function is starting point for the program
	 * @param args
	 */
	public static void main(String[] args) {

		ESQLCompiler esql = new ESQLCompiler();
		Scanner sc = new Scanner(System.in);

		/* 
		 * option to read from file or console
		 */
		boolean input_flag = false;
		try {
			do {

				System.out.println("1. Read input from console");
				System.out.println("2. Read input from file");
				System.out.print("Select source of input [enter option number]: ");

				switch (Integer.parseInt(sc.nextLine())) {

				case 1:
					input_flag = false;
					esql.readConsole(sc);
					break;

				case 2:
					input_flag = false;
					esql.readFile();
					break;

				default:
					input_flag = true;
					System.out.println("Invalid input. Please try again");
				}

			} while (input_flag);

			sc.close();
			esql.mapMfStructure();
			
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage() + " Please restart the program");
		}

	}

	/**
	 * readConsole reads input from console
	 * @param sc
	 */
	public void readConsole(Scanner sc) {

		String temp_input = "";

		System.out.print("Enter Select Attributes Separated by ',' : ");
		temp_input = sc.nextLine();
		input.put("select_attributes", temp_input);

		System.out.print("Enter Where Condition : ");
		temp_input = sc.nextLine();
		input.put("where_condition", temp_input);

		System.out.print("Enter Number of Grouping Variables : ");
		temp_input = sc.nextLine();
		input.put("grouping_variables", temp_input);

		System.out.print("Enter Grouping Attributes Separated by ',' : ");
		temp_input = sc.nextLine();
		input.put("grouping_attributes", temp_input);

		System.out.print("Enter F-Vect Separated by ',' : ");
		temp_input = sc.nextLine();
		input.put("f_vect", temp_input);

		System.out.print("Enter Select Condition-Vect Separated by ',' : ");
		temp_input = sc.nextLine();
		input.put("condition_vect", temp_input);

		System.out.print("Enter Having Condition Separated by ',' : ");
		temp_input = sc.nextLine();
		input.put("having_condition", temp_input);
	}

	/**
	 * readFile reads input from given file 
	 * @param sc
	 */
	public void readFile() throws Exception {

		Scanner sc = new Scanner(System.in);
		System.out.print("Enter the file name with extension: ");
		input_file = new File("inputs//" + sc.nextLine().trim());
		sc.close();

		Scanner read = new Scanner(input_file);
		String temp_input = "";
		temp_input = (read.hasNextLine()) ? read.nextLine() : "";
		input.put("select_attributes", temp_input);
		temp_input = (read.hasNextLine()) ? read.nextLine() : "";
		input.put("where_condition", temp_input);
		temp_input = (read.hasNextLine()) ? read.nextLine() : "";
		input.put("grouping_variable", temp_input);
		temp_input = (read.hasNextLine()) ? read.nextLine() : "";
		input.put("grouping_attributes", temp_input);
		temp_input = (read.hasNextLine()) ? read.nextLine() : "";
		input.put("f_vect", temp_input);
		temp_input = (read.hasNextLine()) ? read.nextLine() : "";
		input.put("condition_vect", temp_input);
		temp_input = (read.hasNextLine()) ? read.nextLine() : "";
		input.put("having_condition", temp_input);
		read.close();

	}

	/**
	 * mapMFStructure parses grouping attributes and f_vect to generate MFStructure
	 */
	public void mapMfStructure() {

		/* convert grouping attributes and f_vect to array of string */
		String[] grouping_attributes = input.get("grouping_attributes").split(",");
		String[] f_vect = input.get("f_vect").split(",");
		
		Connection dbconnection = DatabaseConnection.getDBInstance();
		
		/* query to get data_types from information_schema database */ 
		String get_columns_datatype = "select column_name, ordinal_position, data_type from information_schema.columns where table_name = 'sales'";

		try {

			Statement st = dbconnection.createStatement();
			ResultSet rs = st.executeQuery(get_columns_datatype);

			while (rs.next()) {

				/*
				 * logic to convert PostgresSQL data_types to Java data_types 
				 */
				for (String sattr : grouping_attributes) {

					sattr = sattr.trim();
					
					if (rs.getString("column_name").equals(sattr)) {

						/* convert character varying data_type to String */
						if (rs.getString("data_type").equals("character varying")) {
							mf_structure_grouping_attributes.put(sattr,	"String");
						}
						
						/* convert character data_type to char */
						if (rs.getString("data_type").equals("character")) {
							mf_structure_grouping_attributes.put(sattr, "char");
						}

						/* convert integer data_type to int */
						if (rs.getString("data_type").equals("integer")) {
							mf_structure_grouping_attributes.put(sattr, "int");
						}
					}
				}
			}
			
			/*
			 * logic to convert PostgresSQL aggregates function for MFStructure class
			 */
			if (f_vect.length > 0) {

				for (String fcols : f_vect) {

					fcols = fcols.trim();
					String substr = fcols.substring(0, 3);

					/* convert aggregate function average to sum and count */
					if (substr.equalsIgnoreCase("avg")) {

						String[] temp = fcols.split("_");
						mf_structure_f_vect.put("sum_" + temp[1] + "_" + temp[2], "int");
						mf_structure_f_vect.put("count_" + temp[1] + "_" + temp[2], "int");
					}

					/* putting all other function as it is */
					else {
						
						mf_structure_f_vect.put(fcols, "int");
						
					}
				}
			}

			/* 
			 * call generateMfStructureJavaFile function to generate MFStructure class file
			 */
			boolean MFJavaFile = GenerateJavaCode.generateMfStructureJavaFile(mf_structure_grouping_attributes, mf_structure_f_vect);
			
			/*
			 * if file generate then call generateQueryProcessor function to generate algorithm file
			 */
			if (MFJavaFile) {
				
				System.out.println("MFStructure file generated");
				System.out.println("Generating QueryProcessor file......");
				
				boolean QueryProcessorFile = GenerateJavaCode.generateQueryProcessorJavaFile(input, mf_structure_grouping_attributes, mf_structure_f_vect);
			
				if(QueryProcessorFile) {
					System.out.println("QueryProcessor file generated");
				}
				
				else {
					System.out.println("Error creating QueryProcessor file");
				}
			}
			
			else {
				System.out.println("Error creating MFStructure file. Please try again");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			
		} finally {
			try {
				dbconnection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}