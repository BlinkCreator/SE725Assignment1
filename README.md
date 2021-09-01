# SE725Assignment1 
UPI: moco657
UOA ID: 601131649
<br>
## SE725 Assignment 1
The following code is an implementation of a simple file transfered specified in A [RFC913](https://datatracker.ietf.org/doc/html/rfc913)

<br>







The following test cases are covered by running the AutoClientTest.sh
<br>
1. Successful log in then DONE
2. Failed log in, then true log in, then change user, then DONE
3. Attempt to change TYPE before log in, then log in, then change TYPE, then DONE
4. Log in, LIST Formated base dir, then LIST Verbose subDir, then LIST Formated current dir
5. Log in, CDIR to subDir, LIST Verbose, rename 1.txt with NAME TOBE OneOne.txt, LIST Verbose, RETR OneOne.txt, KILL OneOne.txt, STOR NEW OneOne.txt, STOR APP OneOne.TXT LIST V, DONE
