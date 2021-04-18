/*=======================================================================

This program was designed to implement the Needleman-Wunsch algorithm using
object oriented programming strategies such as polymorphic design and use
of lambda expressions. It also displays dynamic programming techniques
which are used in order to optimize a recursive solution. This program 
was written from scratch by Uriya Sabah.

This program should be ran by typing: 
	Javac DynamicProgramming.java 
into the terminal followed by: 
	Java DynamicProgramming string1 string2 matchScore mismatchScore 
	penaltyScore edgePenaltyScore
The CMD-line arguments are optional (the program will also run when only 
inputting two strings or just the scoring system, and order of input 
doesn't matter).

=========================================================================*/

/*Interface ScoreAlg====================================================== 

Allows an instance of ScoreAlg to be created using lambda expressions.
Score(int x, int y) defines the ruleset to be used for the Needleman-Wunsch
algorithm.

========================================================================*/
interface ScoreAlg{ 
	//scoring algorithm, takes 2 characters and compares them.
	//the return value depends on the scoring algorithm chosen
	int Score(char c, char d); 
}//interface ScoreAlg

/*Class StringMatch======================================================== 

The recursive implementation of the algorithm shouldn't be implemented as
it's widely inefficient for bigger inputs with an exponential runtime of 
O(3**n). However, the algorithm is a candidate for the dynamic programming
approach, and could be adapted to have a polynomial runtime, which will be
implemented in the StringMatch class.

========================================================================*/
class StringMatch{
	protected ScoreAlg scoreAlg = (c, d)->{//default scoring algorithm
		if(c==d) return 1; else return 0; 
	};
 
	protected int penalty = 0; //can be + or - depending on scoring type
	//represents the penalty for using a "gap"

	protected int edgePenalty = 0; //If a gap is on the outside, the 
	//algorithm may want to score it differently

	protected Integer SM[][]; //Score Matrix, keeps track of score at each 
	//point. Initialized as Integer to track unvisited spots as null
	protected int TBM[][]; 
	/*TraceBack Matrix, *remembers* how each value in
	the scoring matrix was calculated. Holds 3 values: 1 for if score
	was calculated from the scoreAlg + alg(i-1,k-1), 2 for if score
	was calculated from alg(i-1,k) + penalty, 3 for if score was
	calculated from alg(i,k-1) + penalty. */
	
	protected String strA, strB; //Strings to be compared
	protected String finalStrA, matchStr, finalStrB; //optimized strings 
	//and string which on output visibly connects matches between
	//finalStrA and finalStrB.
	
	//set the penalty for the algorithm, edge penalty will be same as 
	//unless explicitly overloaded
	public void setPenalty(int p){ penalty = p; edgePenalty = p; }
	//overload in case algorithm wants to score edge gaps differently
	public void setPenalty(int p, int ep){ penalty = p; edgePenalty = ep; }
	//return current penalty
	public int getPenalty(){ return penalty; }
	//return current edge gap penalty
	public int getEdgePenalty(){ return edgePenalty; }
	//return current score for match 
	public int getMatchScore(){ return scoreAlg.Score('a','a'); }
	//return current score for mismatch
	public int getMismatchScore(){ return scoreAlg.Score('a','b'); }

	//set the scoreAlg for the function
	public void setScoreAlg(ScoreAlg s){ scoreAlg = s; }
	//return current score function
	public ScoreAlg getScoreAlg(){ return scoreAlg; }

	//set the strings to be compared and format them if necessary
	//since the algorithm requires '.' in beginning of each string
	public void setStrings(String s1, String s2){
		if(s1.equals("") || s2.equals("")){
			strA="."; strB="."; return;
		}
		if(s1.charAt(0)!='.' && s2.charAt(0)!='.')
			{ strA="."+s1; strB="."+s2; }
		else if(s1.charAt(0)!='.'){ strA="."+s1; strB=s2; }
		else if(s2.charAt(0)!='.'){ strA=s1; strB="."+s2; }
		else { strA = s1; strB = s2; }
	}
	//return the strings that will be compared without '.'
	public String getStrings()
	{ return "String A: "+strA.substring(1)+"\nString B: "+
	strB.substring(1); }

	//constructor which sets up strings to compare
	public StringMatch(String s1, String s2){ setStrings(s1,s2); }

	//calls the algorithmic functions and formats the output to the user
	public void report(){
		//get strings being compared if they aren't too big
		if(strA.length() < 40 && strB.length() < 40) System.out.println(getStrings());
		System.out.println("Match Score: "+getMatchScore()
			+"\nMismatch Score: "+getMismatchScore()+
			"\nPenalty: "+getPenalty()+
			"\nEdge Gap Penalty: "+getEdgePenalty());
		//get the score of the match
		int score = comparisonScore(); 
		//depending on size, prints the matrix
		printmatrix(SM, strA, strB);
		traceback();
		//print alignment if strings aren't too long
		if(matchStr.length()<40) System.out.println(finalStrA+"\n"+matchStr+"\n"+finalStrB);
		System.out.println("Optimal Alignment Score: "+score);	
	}//report

	//computes and returns score for the algorithm
	//calls either bottom-up or top-down implementation
	public int comparisonScore(){
		//make sure strings are formatted
		if(strA.charAt(0)!='.' || strB.charAt(0)!='.') 
			throw new RuntimeException("ERROR - Strings have not "+
			  "been formatted properly.");
		//initialize matrices and account for "." at beginningi
		SM = new Integer[strA.length()][strB.length()];
		TBM = new int[strA.length()][strB.length()];
		/*=================================================================
		Both the top-down and the bottom-up are implemented as examples in 
		this program. Both exhibit roughly O(n*m) time complexity where 
		n = strA.length() and m = strB.length(). In this example, the 
		traceback is recorded in both, but bottom-up is used.
		=================================================================*/
		
		//return scoresTD(strA.length()-1, strB.length()-1);
		return scoresBU(strA.length()-1, strB.length()-1);
	}//comparisonScore

	//Top-Down implementation to calculate score
	public int scoresTD(int i, int k){
		if(SM[i][k]==null){
			//base case
			if(i<=0 && k<=0) return SM[i][k]=0;
			//initialize a,b,c to keep track of best scores
			int a,b,c;
			//set them to the lowest possible int (since they may not
			//be updated before they are compared)
			a=b=c=0x80000000;
			//match
			if(i>0 && k>0){
				int score = scoreAlg.Score(strA.charAt(i), strB.charAt(k));
				a = scoresTD(i-1,k-1) + score;
			}
			//gap for strB
			if(i>0) b = scoresTD(i-1,k) + penalty;
			
			//gap for strA
			if(k>0) c = scoresTD(i,k-1) + penalty;
			
			SM[i][k] = Math.max(a,Math.max(b,c)); //return largest of a,b,c
			//Update Traceback matrix
			if(SM[i][k]==a) TBM[i][k] = 1;
			else if(SM[i][k]==b) TBM[i][k] = 2;
			else TBM[i][k] = 3;
		}
		return SM[i][k];
	}//scoresTD

	//Bottom-Up implementation to calculate score
	public int scoresBU(int x, int y){
		for(int i = 0; i<=x; i++){
			for(int k = 0; k<=y; k++){
				if(i==0 && k==0) SM[i][k]=0; 
				else{
					int a, b, c;
					a=b=c=0x80000000;
					//score for match
					if (i>0 && k>0){
						int score=scoreAlg.Score(strA.charAt(i),strB.charAt(k));
						a = SM[i-1][k-1] + score; 
					}
					//gap on strB
					if (i>0){
						//edge gap
						if(k==0 || k==strB.length()-1) 
							b = SM[i-1][k] + edgePenalty;
						else b = SM[i-1][k] + penalty;
					}
					//gap on strA
					if (k>0){
						if(i==0 || i==strA.length()-1) 
							c = SM[i][k-1] + edgePenalty;
						else c = SM[i][k-1] + penalty;
					}
					//point in matrix = biggest of a, b, and c
			        SM[i][k] = Math.max(a,Math.max(b,c));
					//update traceback matrix for traceback stage
					if(SM[i][k]==a) TBM[i][k] = 1;
					else if(SM[i][k]==b) TBM[i][k] = 2;
					else TBM[i][k] = 3;
				}//else
			}//2nd loop
		}//1st loop
		return SM[x][y];
	}//scoresBU

	//traces through one optimal solution
	public void traceback(){
		if(TBM==null) return;
		int i = strA.length()-1; int k = strB.length()-1;
		//initialize strings
		finalStrA=""; finalStrB=""; matchStr="";
		while(i>0 || k>0){
			int path = TBM[i][k];
			//Characters either matched or path gave best score
			if(TBM[i][k]==1){
				finalStrA = strA.charAt(i) + finalStrA; 
				finalStrB = strB.charAt(k) + finalStrB;
				//confirm whether they match. "x|32" to eliminate concern over upper and lower-case
				if((strA.charAt(i)|32)==(strB.charAt(k)|32))
					matchStr = "|" + matchStr;
				else matchStr = " " + matchStr;
				i-=1; k-=1;
			}
			//Gap for strB
			else if (TBM[i][k]==2){
				finalStrA = strA.charAt(i) + finalStrA; 
				finalStrB = "_" + finalStrB;
				matchStr = " " + matchStr;
				i-=1;
			}
			//Gap for strA
			else{
				finalStrA = "_" + finalStrA; 
				finalStrB = strB.charAt(k) + finalStrB;
				matchStr = " " + matchStr;
				k-=1;
			}
		}//while
	}//traceback

	//***STATIC FUNCTIONS TO BE USED FOR TESTING BELOW***

	//generates a random string to be used for testing the algorithm
	public static String randseq(int n){
		char[] S = new char[n];  // faster than building string char by char
		String DNA = "ACGT";
		for(int i=0;i<n;i++)
		    S[i] = DNA.charAt((int)(Math.random()*4));
		return new String(S); // constructor converts char[] to String
    } // randseq 

 // For debugging only, not to be used on very large matrices.
    // ***This function assumes A, B already have dummy chars in front,
    // **Dimensions of array must match A.length rows, B.length cols. 
    public static void printmatrix(Integer[][] M, String A, String B){
		if (A.length()>30 || B.length()>20) return; // too big to print
		// print first line, with chars in B:
		System.out.print("   ");
		for(int k=0;k<B.length();k++)
		    System.out.printf(" %c ",B.charAt(k));
		System.out.println();
		for(int i=0;i<A.length();i++)
		{
			System.out.printf("%c ",A.charAt(i));
			for(int k=0;k<B.length();k++)
			    System.out.printf("%3d",M[i][k]);
			System.out.println();
		}// for each row
    }//printmatrix

}//class StringMatch


//DynamicProgramming class: implements the algorithm in main
public class DynamicProgramming{

	//checks if a command-line argument is an int
	public static boolean argIsInt(String arg){
		try{
			Integer.parseInt(arg);
			return true;
		}
		catch(Exception e){ return false;  }
	}

	public static void main(String[] args){
		//Main can use command-line arguments to determine the 
		//strings being compared as well as the scoring system
		String S1="", S2=""; //strings being compared
		Integer arg3 = null, //Match score
		arg4 = null, //Mismatch Score
		arg5 = null, // Penalty
		arg6 = null; // Edge Gap Penalty
		int numInt = 0; //keeps track of cmd line arguments
		for (String s:args){
			try{
				int a = Integer.parseInt(s);
				if(numInt==0) arg3 = a;
				else if(numInt==1) arg4 = a;
				else if(numInt==2) arg5 = a;
				else arg6 = a;
				numInt++;
			}
			catch(Exception a){ if(S1.equals("")) S1 = s; else S2 = s; }
		}
		//check if strings were initialized
		if(S2.equals("")){ 
			//String of random length between 5 and 15
			S1 = StringMatch.randseq((int)(Math.random() * (15-5) + 5)); 
			S2 = StringMatch.randseq((int)(Math.random() * (15-5) + 5)); 
		}
		//check if score system was set
		if(arg6==null){
		    //DEFAULT SCORING SYSTEM
			if(arg5==null){ arg3=1; arg4=arg5=0; }
			arg6 = arg5;
		}
		//ints need to be final to use in lambda
		final int MS = arg3, MMS = arg4; 
		int PS = arg5, EPS = arg6;
		//generate random strings if not set by cmd-line args
		StringMatch match = new StringMatch(S1,S2);
		match.setPenalty(PS, EPS); //set the penalties
		match.setScoreAlg((c,d)->{ //change scoring algorithm
			if ((c|32)==(d|32)) return MS; 
			else return MMS;
		});
	
	
		match.report(); //prints match score

	}//main
}//class DynamicProgramming


		/*if (args.length >= 2){
			S1 = args[0]; 
			S2 = args[1];
			//see if cmd-line args were inputted / if so inputted correctly
			try{ 
		   		arg3 = Integer.parseInt(args[2]);
				arg4 = Integer.parseInt(args[3]);
			   	arg5 = Integer.parseInt(args[4]);
				arg6 = Integer.parseInt(args[5]);
			}//try
		   	catch(Exception e1){
		   	    try{
		   	        arg3 = Integer.parseInt(args[0]);
		   	        arg4 = Integer.parseInt(args[1]);
		   	        arg5 = Integer.parseInt(args[2]);
					S1 = StringMatch.randseq((int)(Math.random() * (15-5) + 5)); 
					S2 = StringMatch.randseq((int)(Math.random() * (15-5) + 5)); 
		   	    }//inner try
		   	    //if not, implement the default scoring system
		   	    catch(Exception e2){ arg3=1; arg4=arg5=0; }//inner catch 
		 	}//outer catch
		}//if
		else{
			//String of random length between 5 and 15
			S1 = StringMatch.randseq((int)(Math.random() * (15-5) + 5)); 
			S2 = StringMatch.randseq((int)(Math.random() * (15-5) + 5)); 
		    //DEFAULT SCORING SYSTEM
			arg3=1; arg4=arg5=0;
		}
		*/
