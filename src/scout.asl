// Agent scout in project RescueMission.mas2j


/* Initial beliefs and rules */

informDoctor.
needsPosition.
waitsForInstruction.

/* Initial goals */

!performMoves.

/* Plans */

//The scout performs a move as directed by the doctor
+!performMoves : position(scout,X,Y) & not informDoctor & not waitsForInstruction & nextMove(M)[source(doctor)]
	<-.print("Moving ",M,".");
	makeMove(M);
	.send(doctor, tell, readyToMove(scout));
	+waitsForInstruction;
	-nextMove(M)[source(doctor)];
	!performMoves.
+!performMoves.

//The scout sends its initial position to the doctor
+position(scout,X,Y)[source(percept)] : true
	<-.print("Localization finished. Position: ",X,",",Y);
	.send(doctor, tell, position(scout,X,Y));
	.send(doctor, tell, readyToMove(scout)[source(scout)]);
	-informDoctor;
	!performMoves.
+!performMoves.

//The scouts needs to localize itself
+needsPosition : not position(scout,X,Y)[source(percept)]
	<-.print("Performing localization.");
	localize;
	-needsPosition;
	!performMoves.
+!performMoves.

//The scout receives movement instructions
+nextMove(M)[source(doctor)]: true
	<-.print("Directions received.");
	-waitsForInstruction;
	!performMoves.
+!performMoves.

//The scout communicates patients' positions to the doctor
+red(XR,YR)[source(percept)]:true & nextMove(M)[source(doctor)]
	<-.print("Critical patient found in: ",XR,",",YR,".");
	.send(doctor, tell, red(XR,YR));
	+waitsForInstruction;
	-nextMove(M)[source(doctor)];
	!performMoves.
+!performMoves.

+green(XG,YG)[source(percept)]:true & nextMove(M)[source(doctor)]
	<-.print("Minor patient found in: ",XG,",",YG,".");
	.send(doctor, tell, green(XG,YG));
	+waitsForInstruction;
	-nextMove(M)[source(doctor)];
	!performMoves.
+!performMoves.

+blue(XB,YB)[source(percept)]:true & nextMove(M)[source(doctor)]
	<-.print("Serious patient found in: ",XB,",",YB,".");
	.send(doctor, tell, blue(XB,YB));
	+waitsForInstruction;
	-nextMove(M)[source(doctor)];
	!performMoves.
+!performMoves.

