// Agent doctor in project RescueMission.mas2j


/* Initial beliefs and rules */

//Obstacles positions to be changed here
object1(0,0).
object2(3,1).
object3(4,2).
object4(1,4).
object5(4,4).
object6(1,5).

//Patients positions to be changed here
patient1(5,0).
patient2(1,2).
patient3(3,2).
patient4(2,3).
patient5(5,4).

//Mission is completed when all three patients have been found
found :- red(XR,YR) & blue(XB,YB) & green(XG,YG).

/* Initial goals */

//Find all three patients
!find.


/* Plans */

//When the doctor knows scout's position and not yet have found all three patients
//he checks where the scout should move next
+!find : not found & position(scout,X,Y) & not nextMove(M) & readyToMove(scout)
	<-findMove;
	+nextMove(down);
	-readyToMove(scout);
	!find.
+!find.

//Once the doctor knows where the scout should move next, he sends him a message
-readyToMove(scout):  true & nextMove(M) & next(MM)
	<-.print("Directing scout to move ",MM,".");
	.send(scout, tell, nextMove(MM));
	!find.
+!find.

//When the scout communicates its position, the doctor sends objects and patients coords to the
//environment to perform path searching
+position(scout,X,Y)[source(scout)]: true & object1(A,B) & object2(C,D) & object3(E,F) & object4(G,H) & object5(GG,HH) & object6(GH,HG) & patient1(I,J) & patient2(K,L) & patient3(M,N) & patient4(O,P) & patient5(R,S)
	<-.print("Scout's position received: ",X,",",Y,".");
	communicatePosition(X,Y);
	setUpEnvironment;
	communicateObject(A,B);
	communicateObject(C,D);
	communicateObject(E,F);
	communicateObject(G,H);
	communicateObject(GG,HH);
	communicateObject(GH,HG);
	communicatePatient(I,J);
	communicatePatient(K,L);
	communicatePatient(M,N);
	communicatePatient(O,P);
	communicatePatient(R,S);
	+position(scout,X,Y);
	-needsLocation(scout);
	.print("Searching for the best path.");
	findRoute;
	+readyToMove(scout);
	!find.
+!find.

//When the scout sends a message that it's ready to move
//the doctor know that it should compute next move
+readyToMove(scout)[source(scout)]: true & nextMove(M)
	<-+readyToMove(scout);
	-readyToMove(scout)[source(scout)];
	-nextMove(M);
	!find.
+!find.

//The scout finds a patient
+red(XR,YR)[source(scout)]: true
	<-.print("CRITICAL PATIENT: ",XR,",",YR,".").
+!find.

+blue(XB,YB)[source(scout)]: true
	<-.print("SERIOUS PATIENT: ",XB,",",YB,".").
+!find.

+green(XG,YG)[source(scout)]: true
	<-.print("MINOR PATIENT: ",XG,",",YG,".").
+!find.

