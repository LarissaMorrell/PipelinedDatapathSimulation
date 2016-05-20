/**
 * Owner: Larissa Morrell-Perkins
 * Copyright 2015, All rights reserved
 *
 */

package disassembler;

import java.util.Scanner;
import java.lang.Integer;
import java.io.*;

public class Console {

	//project instructions
		private int instr[] = {0xa1020000, 0x810AFFFC, 0x00831820, 0x01263820, 0x01224820,  
				0x81180000, 0x81510010, 0x00624022, 0x00000000, 0x00000000, 	                          
				0x00000000, 0x00000000};
		


	public static void main(String[] args) {

		Console prgm = new Console();
		prgm.run();
	}


	private void run(){


		Datapath path = new Datapath();

		for(int i = 0; i < instr.length; i++){
			//		for(int i = 0; i < 7; i++){
			System.out.println("CYCLE " + (i+1) + " -----------------------------------");
			path.if_stage(i);
			path.print_out_everything();
			path.copy_write_to_read();

			System.out.println();
		}



	}

	public int getInstruction(int index){
		return instr[index];
	}

}



