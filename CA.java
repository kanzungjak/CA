import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.io.*;
//import java.math.random;
import myCA.H;

class CA {

	int[] state1;
	int[] state2;
	int[] nextState;
	int[] plaintext;

	/*Granularitätszustand*/
	int[] gState;

	/*Die letzten beiden Zustände nach einer Berechnung (Verschlüsselung, Entschlüsselung, Angriffsversuch ...)*/
	int[] lastState;
	int[] nextToLastState;

	/*Die letzten beiden Zustände nach der Verschlüsselung/ Chiffrate*/
	int[] lastStateDec;
	int[] nextToLastStateDec;

	int[] gState_Dec;

	int rounds;
	int configLength;

	String bulkRule;
	String fineRule;

	int[] fineRuleset;
	int[] bulkRuleset;

	static int[] usedKeysB = new int[32];
	static int[] usedKeysF = new int[32];

	FileWriter fw; // = new FileWriter("./out1.txt");
	BufferedWriter bw; // = new BufferedWriter (f_out1);

	//constructor with random keys
	CA (int [] state1, int[] state2, int[] gState, int rounds) {
		this.state1 = Arrays.copyOf(state1, state1.length);
		this.state2 = Arrays.copyOf(state2, state2.length);
		this.gState = Arrays.copyOf(gState, gState.length);
		this.configLength = state1.length;

		this.fineRuleset = new int[32];
		this.bulkRuleset = new int[16]; //should be long enough
		for (int i=0;i<32;i++) {
			this.fineRuleset[i] = (int)(Math.random()*2 );
		}
		for(int i=0; i<16; i++) {
			this.bulkRuleset[i] = (int)(Math.random()*3 );
		}

		this.rounds = rounds;
	}

	//constructor with used defined keys
	CA (int[] state1, int[] state2, int[] gState, int[] bulkRuleset, int[] fineRuleset, int rounds) {
		this.state1 = Arrays.copyOf(state1, state1.length);
		this.state2 = Arrays.copyOf(state2, state2.length);
		this.gState = Arrays.copyOf(gState, gState.length);
		this.configLength = state1.length;

		this.fineRuleset = Arrays.copyOf(fineRuleset, fineRuleset.length);
		this.bulkRuleset = Arrays.copyOf(bulkRuleset, bulkRuleset.length);

		this.rounds = rounds;

		try {
			this.fw = new FileWriter("./out.txt");
			this.bw = new BufferedWriter(fw);
		} catch(IOException ex) {

		}
	}

	//constructor with user defined keys and file-output name
	CA (int[] state1, int[] state2, int[] gState, int[] bulkRuleset, int[] fineRuleset, int rounds, String filename_output) {
		this.state1 = Arrays.copyOf(state1, state1.length);
		this.state2 = Arrays.copyOf(state2, state2.length);
		this.gState = Arrays.copyOf(gState, gState.length);
		this.configLength = state1.length;

		this.fineRuleset = Arrays.copyOf(fineRuleset, fineRuleset.length);
		this.bulkRuleset = Arrays.copyOf(bulkRuleset, bulkRuleset.length);

		this.rounds = rounds;

		try {
			this.fw = new FileWriter("./" + filename_output + ".txt");
			this.bw = new BufferedWriter(fw);
		} catch(IOException ex) {

		}
	}

	public void encrypt(boolean output) {
		int round = 0;

		if (output) {
			System.out.println("Encryption: ");
			H.printState(state1);								//C_-1
			H.printState(state2, gState);						//C_0
		}

		try {
			bw.write( H.stateToString(state1) );
			bw.write( H.stateToString(state2, gState) );
		} catch (IOException ex) {	}

		nextState = Arrays.copyOf(state2, state2.length);   //C_0

		while (round < rounds) {

			/*State Change*/
			if (round % 2 == 0) {
				nextState = H.globalstep(state1, state2, gState, fineRuleset, bulkRuleset);
				state1 = Arrays.copyOf(nextState, nextState.length);

				lastState 		= Arrays.copyOf(state1, state1.length);
				nextToLastState = Arrays.copyOf(state2, state2.length);
			} else {
				nextState = H.globalstep(state2, state1, gState, fineRuleset, bulkRuleset);
				state2 = Arrays.copyOf(nextState, nextState.length);

				lastState 		= Arrays.copyOf(state2, state2.length);
				nextToLastState = Arrays.copyOf(state1, state1.length);
			}

			/*Granularity Change*/
			gState = H.gState_step(gState, nextState);

			round++;

			if(output) {
				//jeden Zustand ausgeben
				System.out.print(round);
				H.printState(nextState, gState);
			}

			try {
				bw.write( H.stateToString(nextState, gState) );
			} catch (IOException ex) {	}


		}

		/*Die letzten beiden Zustände nach der Verschlüsselung sichern aka. die Chiffrate*/
		lastStateDec = Arrays.copyOf(lastState, lastState.length);
		nextToLastStateDec = Arrays.copyOf(nextToLastState, nextToLastState.length);
		gState_Dec = Arrays.copyOf(gState, gState.length);

		if(false) {
			System.out.println("last lines");
			H.printState(nextToLastStateDec);
			H.printState(lastStateDec);

		}
	}

	public void decrypt() {
		System.out.println("Decryption:");

		int round = 0;

		/*Chiffrate einfügen*/
		state1 = Arrays.copyOf(lastStateDec, lastStateDec.length); 				//C_n
		state2 = Arrays.copyOf(nextToLastStateDec, nextToLastStateDec.length);	//C_n-1
		gState = Arrays.copyOf(gState_Dec, gState_Dec.length);					//G_n
		int[] gState2 = H.gState_step(gState, lastStateDec);						//G_n-1
		int[] preState;

		H.printState(state1, gState);		//C_n,   G_n
		H.printState(state2, gState2);	//C_n-1, G_n-1

		nextState = Arrays.copyOf(state2, state2.length); //C_n-1

		//gState == G_n
		gState = H.gState_step(gState_Dec, lastStateDec);
		//gState == G_n-1

		while (round < rounds - 1) {
			/*State Change*/
			preState = Arrays.copyOf(nextState, nextState.length);

			if (round % 2 == 0) {
				nextState = H.globalstep(state1, state2, gState, fineRuleset, bulkRuleset);
				state1 	  = Arrays.copyOf(nextState, nextState.length);

				lastState 		= Arrays.copyOf(state1, state1.length);
				nextToLastState = Arrays.copyOf(state2, state2.length);
			} else {
				nextState = H.globalstep(state2, state1, gState, fineRuleset, bulkRuleset);
				state2 	  = Arrays.copyOf(nextState, nextState.length);

				lastState 		= Arrays.copyOf(state2, state2.length);
				nextToLastState = Arrays.copyOf(state1, state1.length);
			}

			/*Granularity Change*/
			gState = H.gState_step(gState, preState);

			round++;

			H.printState(nextState, gState);
		}

		System.out.println("Plaintext:");
		H.printState(nextState);
	}

	public static void attack_bulk(int[] plaintext, int[] lastState, int[] granularityState, int[] bulkRuleset, int[] fineRuleset, int rounds) {
		System.out.println("Attack (bulk):");
		Set<Integer> candidates = new HashSet<Integer>();

		CA[] cas0 = new CA[64];
		CA[] cas1 = new CA[64];

		int[] input0 = new int[plaintext.length];
		int[] input1 = new int[plaintext.length];

		for(int c=0; c<cas0.length; c++) {
			/*Init der beiden Eingabewerte*/
			//S1: 	0-----001100-------0
			//S2:   0-----XXXXXX-------0
			for(int j=0; j<input0.length /*+4?*/; j++) {
				//Annahme: nicht beeinflußte Bits seien Null bzw. Eins
				input0[j] = 0;
				input1[j] = 1;
			}

			//"1er" Bereich bruteforcen
			for(int x=0; x<input0.length; x++) {

				if(plaintext[x] == 1) {
					//hmm, das kann aber irgendwie nicht sein
					input0[x-3] = H.getBit(c, 5); //MSB
					input0[x-2] = H.getBit(c, 4);
					input0[x-1] = H.getBit(c, 3);
					input0[ x ] = H.getBit(c, 2);
					input0[x+1] = H.getBit(c, 1);
					input0[x+2] = H.getBit(c, 0); //LSB

					input1[x-3] = H.getBit(c, 5); //MSB
					input1[x-2] = H.getBit(c, 4);
					input1[x-1] = H.getBit(c, 3);
					input1[ x ] = H.getBit(c, 2);
					input1[x+1] = H.getBit(c, 1);
					input1[x+2] = H.getBit(c, 0); //LSB
				}
			}

			/*Der Granularitätszustand muss natürlich auch im einen Schritt weitergerechnet werden.
			  Da wir ihn ganz auf den "0"-String gesetzt haben, ist G_1 = inputX XOR 0 = 0*/
			cas0[c] = new CA(plaintext, input0, input0 /*granularityState*/, bulkRuleset, fineRuleset, rounds);
			cas1[c] = new CA(plaintext, input1, input1 /*granularityState*/, bulkRuleset, fineRuleset, rounds);


			/*Verschlüsselung der Kandidaten*/
			cas0[c].encrypt(false);
			cas1[c].encrypt(false);

			/*Überprüfung ob letzte Zustand der Verschlüsselung mit dem vorletzten Zustand der Kandidaten übereinstimmt*/


			if ( H.compareArrays(cas0[c].nextToLastState, lastState) == true ) {
				candidates.add(c);
				System.out.println("Kandidat " + c);
			}
			if ( H.compareArrays(cas1[c].nextToLastState, lastState) == true ) {
				candidates.add(c);
				System.out.println("Kandidat " + c);
			}
		}
	}

	public static void attack_fine(int[] plaintext, int[] lastState, int[] granularityState, int[] bulkRuleset, int[] fineRuleset, int rounds) {
		System.out.println("Attack (fine):");
		Set<Integer> candidates = new HashSet<Integer>();

		//cas= Cellular AutomataS /*TODO bessere Bezeichnung finden*/
		CA[] cas0 = new CA[32];
		CA[] cas1 = new CA[32];
		//custom input, beeinflußbarer Bereich wird brutegeforced, der Rest ist konstant (0 oder 1)
		int[] input0 = new int[plaintext.length];
		int[] input1 = new int[plaintext.length];

		int[] fine_granularity = new int[plaintext.length];
		for (int i=0; i<fine_granularity.length; i++) {
			if(i%2==0)
				fine_granularity[i] = 1;
			else
				fine_granularity[i] = 0;
		}

		for(int c=0; c<cas0.length; c++) {

			/*Init der beiden Eingabewerte*/
			//S1: 	0------010-------0
			//S2:   k-----XXXXX------k mit k={0|1}
			for(int j=0; j<input0.length; j++) {
				//Annahme: nicht beeinflußte Bits seien Null bzw. Eins
				input0[j] = 0;
				input1[j] = 1;
			}

			//"1er" Bereich bruteforcen
			for(int x=0; x<input0.length; x++) {
				if(plaintext[x] == 1) {
					input0[x-2] = H.getBit(c, 4); //MSB
					input0[x-1] = H.getBit(c, 3);
					input0[ x ] = H.getBit(c, 2);
					input0[x+1] = H.getBit(c, 1);
					input0[x+2] = H.getBit(c, 0); //LSB

					input1[x-2] = H.getBit(c, 4); //MSB
					input1[x-1] = H.getBit(c, 3);
					input1[ x ] = H.getBit(c, 2);
					input1[x+1] = H.getBit(c, 1);
					input1[x+2] = H.getBit(c, 0); //LSB
				}
			}

			cas0[c] = new CA(plaintext, input0, fine_granularity, bulkRuleset, fineRuleset, rounds);
			cas1[c] = new CA(plaintext, input1, fine_granularity, bulkRuleset, fineRuleset, rounds);

			/*Verschlüsselung der Kandidaten*/
			boolean output = false;
			if(c == 31)
				output = true;
			cas0[c].encrypt(false);
			cas1[c].encrypt(output);



			/*Überprüfung ob letzte Zustand der Verschlüsselung mit dem vorletzten Zustand der Kandidaten übereinstimmt*/
			if ( H.compareArrays(cas0[c].nextToLastState, lastState) == true ) {
				candidates.add(c);
				System.out.println("Kandidat " + c);
			}

			if ( H.compareArrays(cas1[c].nextToLastState, lastState) == true ) {
				candidates.add(c);
				System.out.println("Kandidat " + c);
			}

		}
	}

	public static int[] attack3States (int[] state1, int[] state2, int[] state3, int[] gState) {
		int size = state1.length;

		int[] rulesBulk = new int[16];
		int[] rulesFine = new int[32];
		
		//nicht ermittelte Regeln werden mit "5" definiert
		Arrays.fill(rulesBulk, 5);
		Arrays.fill(rulesFine, 5);

		for (int i=0; i<size; i++) {

			//bulk Rules
			if (i==size-1) { //exception: last index => fine cell

				String nb = "";
				for (int j=-2; j<=2; j++) {
					nb += state2[ (i+j+size) % size ];
				}
				int ruleIndex = Integer.parseInt(nb, 2);
				rulesFine[ruleIndex] = (state3[i] + state1[i]) % 2;
					
			} else if( (gState[i]==0 && gState[i+1]==0) || (gState[i]==1 && gState[i+1]==1) ) {
				//Welche Nachbarschaft betrachten wir?
				String nb = "" + state2[(i-1+size)%size] + state2[i] + state2[ (i+1+size)%size ] + state2[ (i+2+size)%size ];
				int ruleIndex = Integer.parseInt(nb, 2);

				//Wie lautet der dazugehörige neue Zustand (in Bits)?
				int firstBit  = (state3[ i ] + state1[ i ]) % 2; //LSB
				int secondBit = (state3[i+1] + state1[i+1]) % 2;

				//... und als natürliche Zahl
				String bitString = "" + firstBit + secondBit;
				int newRule = Integer.parseInt(bitString, 2);

				/*>***DEBUG*****/
				/*
				System.out.println("ruleIndex used: " + ruleIndex);

				if (ruleIndex == 14) {
					System.out.println("--------------------------");
					System.out.println("ruleIndex " + ruleIndex);
					System.out.println("i " + i);
					System.out.println("nb " + nb );
					System.out.println("BitString " + bitString);
					System.out.println("state3 " + state3[i] + state3[i+1]);
					System.out.println("state1 " + state1[i] + state1[i+1]);
					System.out.println("index " + ruleIndex + ": " + newRule);
					System.out.println("--------------------------");
				}
				/****DEBUG****
				*/

				//trage den Werte in die Tabelle ein
				rulesBulk[ruleIndex] = newRule;

				//Da es eine bulk Regel ist, können wir eine Zelle überspringen
				i++;

			} else { //fine Rules

				String nb = "";
				for (int j=-2; j<=2; j++) {
					nb += state2[ (i+j+size) % size ];
				}
				int ruleIndex = Integer.parseInt(nb, 2);
				rulesFine[ruleIndex] = (state3[i] + state1[i]) % 2;
				
			}
		}


		//System.out.println(Arrays.toString(rulesFine) );
		//System.out.println(Arrays.toString(rulesBulk) );

		return rulesBulk;
	}

	public static void main(String[] args) throws IOException {

		FileReader f 	  = new FileReader( System.getProperty("user.dir") + File.separator + "in.txt");
		FileWriter f_out1 = new FileWriter( System.getProperty("user.dir") + File.separator + "out1.txt");
		FileWriter f_out2 = new FileWriter( System.getProperty("user.dir") + File.separator + "out2.txt");

		/*
		if (!f.exists()) {
			System.out.println("Missing file");
		}
		
		if (!f_out1.exists()) {
			f_out1.createNewFile();
		}

		if (!f_out2.exists()) {
			f_out2.createNewFile();
		}*/

		BufferedReader in = new BufferedReader(f);

		String s1 = in.readLine();
		String s2 = in.readLine();
		String g = in.readLine();

		//feste Keys
		int[] fineRuleset = {1,0,1,1,1,1,0,1,1,1,1,1,0,0,0,0,0,1,1,1,1,1,0,0,1,0,0,0,1,1,0,0};
		int[] bulkRuleset = {0,0,2,0,3,1,3,3,3,0,2,2,3,2,2,2};
		System.out.println("bulkLength " + bulkRuleset.length);
		//int[] bulkRuleset = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};


		CA za = new CA( H.StringToInt(s1), H.StringToInt(s2), H.StringToInt(g), bulkRuleset, fineRuleset, 16, "out1");
		//CA za2 = new CA( H.StringToInt(s2), H.StringToInt(s1), H.StringToInt(s2), bulkRuleset, fineRuleset, 16, "out2");

		za.encrypt(true);

		
		//3 hintereinander folgende Zeilen für den Angriff einlesen
		FileReader f_read_3lines = new FileReader( System.getProperty("user.dir") + File.separator + "3lines.txt" );
		BufferedReader read_3lines = new BufferedReader(f_read_3lines);
		String c1 = read_3lines.readLine().replace(",","").replaceAll("\\s","");
		String c2 = read_3lines.readLine().replace(",","").replaceAll("\\s","");
		String c3 = read_3lines.readLine().replace(",","").replaceAll("\\s","");
		String cg = read_3lines.readLine().replace(",","").replaceAll("\\s","");

		int[] c1_int = new int[c1.length()];
		int[] c2_int = new int[c2.length()];
		int[] c3_int = new int[c3.length()];
		int[] cg_int = new int[cg.length()];

		for (int i=0; i<c1.length(); i++) {
			c1_int[i] = Integer.parseInt("" +  c1.charAt(i) );
			c2_int[i] = Integer.parseInt("" +  c2.charAt(i) );
			c3_int[i] = Integer.parseInt("" +  c3.charAt(i) );
			cg_int[i] = Integer.parseInt("" +  cg.charAt(i) );
		}

		int[] guessedKeysBulk = attack3States(c1_int, c2_int, c3_int, cg_int);


		//System.out.println(Arrays.toString(fineRuleset) );
		System.out.println( Arrays.toString(guessedKeysBulk) );
		System.out.println( Arrays.toString(bulkRuleset) );

		int ctr = 0;
		for (int i=0; i<bulkRuleset.length; i++) {
			if (guessedKeysBulk[i]!=5 ) {
				if (guessedKeysBulk[i]!=bulkRuleset[i] ) {
					System.out.println("Missmatch at index " + i);
				} else {
					ctr++;
				}
			}
		}
		System.out.println("Guessed Keys (bulk): " + ctr);



		/******************Aufräumen*****************/
		in.close();
		read_3lines.close();
		za.bw.close();
		//za2.bw.close();
	}

}


