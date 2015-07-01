import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.math.BigInteger;
//import java.math.random;

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

	CA (String startConfig, String granularity, String fineRule, String bulkRule, int rounds) {
		System.out.println("Plaintext length: " + startConfig.length() );
		this.state1 = new int[startConfig.length()];
		this.state2 = new int[startConfig.length()];
		this.gState = new int[startConfig.length()]; 
		this.nextState = new int[startConfig.length()];
		this.configLength = startConfig.length();

		for (int i=0; i < configLength; i++) {
			this.state1[i] = 0;
			this.gState[i] = 0;
			this.state2[i] = Integer.parseInt( "" + startConfig.charAt(i) );
		}

		if (granularity == "bulk") {
			Arrays.fill(this.gState, 0);

		} else if(granularity == "fine") {

			for (int i=0; i<configLength; i++) {
				if (i%2 == 0)
					this.gState[i] = 1;
				else
					this.gState[i] = 0;
			}

		} else {
			System.out.println("Wrong/Missing granularity parameter");
		}

		this.plaintext = Arrays.copyOf(this.state2, this.state2.length);

		this.fineRuleset = createRuleset(fineRule, 2, 2); //Radius=2, #Zustände=2
		this.bulkRuleset = createRuleset(bulkRule, 1, 4); //Radius=1, #Zustände=4

		this.bulkRule = bulkRule;
		this.fineRule = fineRule;

		//System.out.println(Arrays.toString(bulkRuleset) + bulkRuleset.length);
		this.rounds = rounds;
		//System.out.println(Arrays.toString(fineRuleset) + fineRuleset.length);
	}

	CA (String startConfig, int[] gState, String fineRule, String bulkRule, int rounds) {
		System.out.println("Plaintext length: " + startConfig.length() );
		this.state1 = new int[startConfig.length()];
		this.state2 = new int[startConfig.length()];
		this.gState = new int[startConfig.length()]; 
		this.nextState = new int[startConfig.length()];
		this.configLength = startConfig.length();

		for (int i=0; i < configLength; i++) {
			this.state1[i] = 0;
			this.gState[i] = gState[i];
			this.state2[i] = Integer.parseInt( "" + startConfig.charAt(i) );
		}
		
		this.fineRuleset = createRuleset(fineRule, 2, 2); //Radius=2, #Zustände=2
		this.bulkRuleset = createRuleset(bulkRule, 1, 4); //Radius=1, #Zustände=4

		this.bulkRule = bulkRule;
		this.fineRule = fineRule;

		//System.out.println(Arrays.toString(bulkRuleset) + bulkRuleset.length);
		this.rounds = rounds;
		//System.out.println(Arrays.toString(fineRuleset) + fineRuleset.length);
	}

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

	CA (int[] state1, int[] state2, int[] gState, int[] bulkRuleset, int[] fineRuleset, int rounds) {
		this.state1 = Arrays.copyOf(state1, state1.length);
		this.state2 = Arrays.copyOf(state2, state2.length);
		this.gState = Arrays.copyOf(gState, gState.length);
		this.configLength = state1.length;
		
		this.fineRuleset = Arrays.copyOf(fineRuleset, fineRuleset.length);
		this.bulkRuleset = Arrays.copyOf(bulkRuleset, bulkRuleset.length);

		this.rounds = rounds;
	}

	//wandelt eine Dezimalzahl in einen binären Regelsatz um, auf eine sehr murksige Art und Weise
	static int[] createRuleset(long decimal, int radius, int numStates) {
		//Die Werte sollten mit Null initiert sein
		//Anzahl Regeln = |numStates|**(2r+1)
		int numRules = (int)Math.pow(numStates, 2*radius+1) ;
		int[] ruleset = new int[numRules];

		String s = Long.toBinaryString(decimal);

		for(int i=0; i < s.length(); i++) {
			ruleset[ numRules - s.length() + i ] = Integer.parseInt( "" + s.charAt(i) );
		}
//		System.out.println( Arrays.toString(ruleset) );
		return ruleset;
	}

	//für große, große Zahlen
	static int[] createRuleset(String decimal, int radius, int numStates) {
		//Die Werte sollten mit Null initiert sein
		//Anzahl Regeln = |numStates|**(2r+1)
		int numRules = (int)Math.pow(numStates, 2*radius+1) ;
		int[] ruleset = new int[numRules];
		BigInteger num = new BigInteger(decimal, 10);

		//numStates gibt an, als was für eine Zahl es intepretiert werden soll (binär, 4-när ...)
		String s = num.toString(numStates);
		
		for(int i=0; i < s.length(); i++) {
			ruleset[ numRules - s.length() + i ] = Integer.parseInt( "" + s.charAt(i) );
		}
//		System.out.println( Arrays.toString(ruleset) + " Length: " + ruleset.length);
		return ruleset;
	}

	public static int getBit(int n, int k) {
    	return (n >> k) & 1;
	}

	public static void printState(int[] state) {
		for(int i=0; i < state.length; i++) {
			if(state[i] == 1) {
				System.out.print("#");
			} else if(state[i] == 0) {
				System.out.print("_");
			} else {
				//ups, da sollten aber nur Bits raus kommen!
				System.out.print(state[i]);
			}
		}
		System.out.println("");
	}

	public static void printState(int[] state, int[] state2) {
		for(int i=0; i < state.length; i++) {
			if(state[i] == 1) {
				System.out.print("#");
			} else if(state[i] == 0) {
				System.out.print("_");
			} else {
				//ups, da sollten aber nur Bits raus kommen!
				System.out.print(state[i]);
			}
		}

		System.out.print(" ");

		for(int i=0; i < state2.length; i++) {
			if(state2[i] == 1) {
				System.out.print("#");
			} else if(state2[i] == 0) {
				System.out.print("_");
			} else {
				//ups, da sollten aber nur Bits raus kommen!
				System.out.print(state2[i]);
			}
		}
		System.out.println("");
	}

	public static boolean compareArrays(int[] a, int[] b) {
		boolean same = true;
		if(a == null || b == null) {
			same = false;
		} else {
			if(a.length != b.length) {
				same = false;
			} else {
				for(int i=0; i<a.length; i++) {
					if(a[i] != b[i]) {
						same = false;
					}
				}
			}
		}
		return same;
	}

	//r=1, Zustände = {0,1,2,3}
	/*
	Input: Zustand bei t-1 für Zellen index + index+1
		   Zustand bei t
	Output: Zustand bei t+1 für Zelle index und index +1
	index: Das Element, was betrachtet werden soll
	*/

	//Braucht 32 Bits
	public static int[] localstep_bulk (int[] state1, int[] state2, int index, int[] ruleset) {
		int[] nextState = new int[2];
		int l = state1.length;

		//int[] ruleset = createRuleset(rule, 1, 4); // |Zustände|**|Anzahl Elemente| => 4**3 = 64 bits
		//der Zustand einer bulky Zelle besteht aus 2-Bits
		//deshalb müssen wir erst 2-bits als Dezimalzahl interpretieren (eigentlich 4-näre Zahl)
		//und nach noch einen Wert weiter schauen, umso die richtige lokale Nachbarschaft zu erhalten
		int ownState =  Integer.parseInt( "" + state2[index] + state2[ index+1 ], 2);
		String nb = "" + state2[ (index-1+l)%l ] + ownState + state2[ (index+2+l)%l ];
		int ruleIndex = Integer.parseInt(nb, 4);

		usedKeysB[ruleIndex] = 1;

		int firstBit  = getBit(ruleset[ruleIndex], 0); //LSB
		int secondBit = getBit(ruleset[ruleIndex], 1);

		nextState[0] = (firstBit  + state1[ index   ]) % 2;
		nextState[1] = (secondBit + state1[ index+1 ]) % 2;
		
		return nextState; 
	}

	//braucht nur 16 Einträge, da wir aber c={0,1,2,3} erhalten können ist die 
	//effektive Schlüssellänge in Bits: 4**16 = 2**32 Möglichkeiten => 32 Bits
	public static int[] localstep_bulk_alternative (int[] state1, int[] state2, int index, int[] ruleset) {
		int[] nextState = new int[2];
		int l = state1.length;

		String nb = "" + state2[(index-1+l)%l] + state2[index] + state2[index+1] + state2[ (index+2+l)%2 ];
		int ruleIndex = Integer.parseInt(nb, 2);

		usedKeysB[ruleIndex] = 1;

		int firstBit  = getBit(ruleset[ruleIndex], 0); //LSB
		int secondBit = getBit(ruleset[ruleIndex], 1);

		nextState[0] = (firstBit  + state1[ index   ]) % 2;
		nextState[1] = (secondBit + state1[ index+1 ]) % 2;

		return nextState;

	}

	public static int localstep_fine (int[] state1, int[] state2, int index, int[] ruleset) {
		int l = state1.length;

		String nb = "";
		for (int i=-2; i<=2; i++) {
			nb += state2[ (index+i+l) % l ];
		}

		//String nb = "" + state2[index-2] + state2[index-1] + state2[index] + state2[index+1] + state2[index+2];
		int ruleIndex = Integer.parseInt(nb, 2);

		usedKeysF[ruleIndex] = 1;

		return (ruleset[ruleIndex] + state1[index]) % 2;
	}

	public static int[] globalstep(int[] state1, int[] state2, int[] gState, int[] fineRuleset, int[] bulkRuleset) {
		int[] newState = new int[state1.length];

		for(int i=0; i<state1.length; i++) {
			/*Sondefall: wenn die letzte Zelle fette Granularität haben sollte, dann interpretiere sie nur als dünne Zelle*/
			if (i == state1.length-1 ){
				newState[i] = localstep_fine(state1, state2, i, fineRuleset);

			/*bulky cells*/
			} else if ( (gState[i] == 1 && gState[i+1] == 1) || (gState[i] == 0 && gState[i+1] == 0) ) { 
				int[] tmp = localstep_bulk_alternative(state1, state2, i , bulkRuleset);
				newState[ i ] = tmp[0];
				newState[i+1] = tmp[1];
				i++; 

			/*fine cell*/
			} else { 
				newState[i] = localstep_fine(state1, state2, i, fineRuleset);
			}
		}

		return newState;
	}

	public static int[] gState_step (int[] gState, int[] state) {
		int[] newState = new int[gState.length];

		if (gState.length != state.length) {
			System.out.println("gState_step: unequal length");
			Arrays.fill(newState, -1);
			return newState;
		}

		for(int i=0; i<gState.length; i++) {
			newState[i] = (gState[i] + state[i]) % 2;
		}
		return newState;
	}

	public void encrypt(boolean output) {
		int round = 0;

		if (output) {
			System.out.println("Encryption: ");
			printState(state1);								//C_-1
			printState(state2, gState);						//C_0
		}

		nextState = Arrays.copyOf(state2, state2.length);   //C_0

		while (round < rounds) {

			/*State Change*/
			if (round % 2 == 0) {
				nextState = globalstep(state1, state2, gState, fineRuleset, bulkRuleset);
				state1 = Arrays.copyOf(nextState, nextState.length);

				lastState 		= Arrays.copyOf(state1, state1.length);
				nextToLastState = Arrays.copyOf(state2, state2.length);
			} else {
				nextState = globalstep(state2, state1, gState, fineRuleset, bulkRuleset);
				state2 = Arrays.copyOf(nextState, nextState.length);

				lastState 		= Arrays.copyOf(state2, state2.length);
				nextToLastState = Arrays.copyOf(state1, state1.length);
			}

			/*Granularity Change*/
			gState = gState_step(gState, nextState);

			round++;

			if(output) {
				//jeden Zustand ausgeben
				System.out.print(round);
				printState(nextState, gState);
			}
		}

		/*Die letzten beiden Zustände nach der Verschlüsselung sichern aka. die Chiffrate*/
		lastStateDec = Arrays.copyOf(lastState, lastState.length);
		nextToLastStateDec = Arrays.copyOf(nextToLastState, nextToLastState.length);
		gState_Dec = Arrays.copyOf(gState, gState.length);

		if(false) {
			System.out.println("last lines");
			printState(nextToLastStateDec);
			printState(lastStateDec);

		}
		
	}

	public void decrypt() {
		System.out.println("Decryption:");

		int round = 0;

		/*Chiffrate einfügen*/
		state1 = Arrays.copyOf(lastStateDec, lastStateDec.length); 				//C_n
		state2 = Arrays.copyOf(nextToLastStateDec, nextToLastStateDec.length);	//C_n-1
		gState = Arrays.copyOf(gState_Dec, gState_Dec.length);					//G_n
		int[] gState2 = gState_step(gState, lastStateDec);						//G_n-1
		int[] preState;

		printState(state1, gState);		//C_n,   G_n
		printState(state2, gState2);	//C_n-1, G_n-1

		nextState = Arrays.copyOf(state2, state2.length); //C_n-1

		//gState == G_n
		gState = gState_step(gState_Dec, lastStateDec);
		//gState == G_n-1

		while (round < rounds - 1) {
			/*State Change*/
			preState = Arrays.copyOf(nextState, nextState.length);

			if (round % 2 == 0) {
				nextState = globalstep(state1, state2, gState, fineRuleset, bulkRuleset);
				state1 	  = Arrays.copyOf(nextState, nextState.length);

				lastState 		= Arrays.copyOf(state1, state1.length);
				nextToLastState = Arrays.copyOf(state2, state2.length);
			} else {
				nextState = globalstep(state2, state1, gState, fineRuleset, bulkRuleset);
				state2 	  = Arrays.copyOf(nextState, nextState.length);

				lastState 		= Arrays.copyOf(state2, state2.length);
				nextToLastState = Arrays.copyOf(state1, state1.length);
			}

			/*Granularity Change*/
			gState = gState_step(gState, preState);

			round++;

			printState(nextState, gState);
		}

		System.out.println("Plaintext:");
		printState(nextState);
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
					input0[x-3] = getBit(c, 5); //MSB
					input0[x-2] = getBit(c, 4); 
					input0[x-1] = getBit(c, 3);
					input0[ x ] = getBit(c, 2);
					input0[x+1] = getBit(c, 1);
					input0[x+2] = getBit(c, 0); //LSB
	
					input1[x-3] = getBit(c, 5); //MSB
					input1[x-2] = getBit(c, 4); 
					input1[x-1] = getBit(c, 3);
					input1[ x ] = getBit(c, 2);
					input1[x+1] = getBit(c, 1);
					input1[x+2] = getBit(c, 0); //LSB
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


			if ( compareArrays(cas0[c].nextToLastState, lastState) == true ) {
				candidates.add(c);
				System.out.println("Kandidat " + c);
			}
			if ( compareArrays(cas1[c].nextToLastState, lastState) == true ) {
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
					input0[x-2] = getBit(c, 4); //MSB
					input0[x-1] = getBit(c, 3);
					input0[ x ] = getBit(c, 2);
					input0[x+1] = getBit(c, 1);
					input0[x+2] = getBit(c, 0); //LSB

					input1[x-2] = getBit(c, 4); //MSB
					input1[x-1] = getBit(c, 3);
					input1[ x ] = getBit(c, 2);
					input1[x+1] = getBit(c, 1);
					input1[x+2] = getBit(c, 0); //LSB
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
			if ( compareArrays(cas0[c].nextToLastState, lastState) == true ) {
				candidates.add(c);
				System.out.println("Kandidat " + c);
			}

			if ( compareArrays(cas1[c].nextToLastState, lastState) == true ) {
				candidates.add(c);
				System.out.println("Kandidat " + c);
			}

		}

	}

	public static void main(String[] args) {
		String plaintext = "0000000000000000000000000000100000000000000000000000000000000000";
		String br = "340282366920938463463374607431768211455"; //[0-4**64) "volle" Regel
		String fr = "4294967295"; //[0-2**32) volle Regel
		int rounds = 12;
	
		//br = "18446744073709551615"; //2**64 - 1
		br = "34028233592093843463374605431768211455"; 
		fr = "4274967295";
		fr = "4204967295";
		//br = "0";
		//fr = "0";

		/*
		//encryption and attack with bulk granularity
		CA ca_b = new CA(plaintext,"bulk", fr, br, rounds);
		ca_b.encrypt(true);
		int[] granularity_zero = new int[ca_b.configLength]; //should be implicit zero
		attack_bulk(ca_b.plaintext, ca_b.lastState, granularity_zero, ca_b.bulkRuleset, ca_b.fineRuleset, ca_b.rounds);


		//fine granularity case
		plaintext = "0000000000000000000000000000100000000000000000000000000000000000";
		CA ca_f = new CA(plaintext,"fine", fr, br, rounds);
		int[] granularityState_fine = new int[ca_f.configLength];
		for(int i=0; i<granularityState_fine.length; i = i+2) {
			granularityState_fine[i] = 1;
			granularityState_fine[i+1] = 0;
		}
		ca_f.encrypt(true);
		attack_fine(ca_f.plaintext, ca_f.lastState, granularityState_fine, ca_f.bulkRuleset, ca_f.fineRuleset, ca_f.rounds);
	*/
		//Init
		int length = 32;
		int[] state1 = new int[length];
		int[] state2 = new int[length];
		int[] gState = new int[length];
		for (int i=0; i<length; i++) {
			state1[i] = 0;
			state2[i] = 0;

			if(i%3==0) {
				state2[i] = 1;
			}
			else {
				//state1[i] = 1;
				state2[i] = 0;
			}

			//state2[16] = 1;


			if(i%2==0)
				gState[i] = 1;
			else
				gState[i] = 0;

		} //state2[(int)(length/2)] = 1;

		//random key is used here
		CA test_ca = new CA(state1, state2, gState, 16);
		test_ca.encrypt(true);

		System.out.println(Arrays.toString(test_ca.fineRuleset));
		System.out.println(Arrays.toString(test_ca.bulkRuleset));

		System.out.println("used keys fine: ");
		for (int i=0;i<32 ;i++ ) {
			if(usedKeysF[i]==1)

				System.out.print(i + ", ");
		}

		System.out.println("used keys bulk: ");
		for (int i=0;i<32 ;i++ ) {
			if(usedKeysB[i]==1)
				System.out.print(i + ", ");
		}





		//anderes Init
		for (int i=0; i<length; i++) {
			state1[i] = 0;
			gState[i] = 0;



		} 

		state2[ (int)(length/2) ] = 1;



		//secret keys
		/*
		int[] fineRuleset = {1,0,1,1,1,1,0,1,1,1,1,1,0,0,0,0,0,1,1,1,1,1,0,0,1,0,0,0,1,1,0,0};
		int[] bulkRuleset = {1,2,1,2,1,1,0,1,2,1,1,1,0,0,1,1};

		CA test2_ca = new CA(state1, state2, gState, bulkRuleset, fineRuleset, 12);
		test2_ca.encrypt(true);

		System.out.println("Input:");
		System.out.println(Arrays.toString(state1));
		System.out.println(Arrays.toString(state2));

		System.out.println("Output:");
		System.out.println(Arrays.toString(test2_ca.nextToLastStateDec));
		System.out.println(Arrays.toString(test2_ca.lastStateDec));

		//slide attacks aus dem Symmetrische Skript
		int[] attFineruleset = new int[32];
		int[] attBulkruleset = new int[32];
		for(int i=0; i<(int)(Math.pow(2,16)); i++ ) {
			for (int j=0; j<32; j++) {
				attBulkruleset[j] = (int)(Math.random()*3);
				attFineruleset[j] = (int)(Math.random()*2);
			}
			CA attack_ca = new CA (state1, state2, gState, attBulkruleset, attFineruleset, 12);
			//attack_ca
		}

		*/
	}


}