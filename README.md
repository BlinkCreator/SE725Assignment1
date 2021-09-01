# SE725Assignment1 
UPI: moco657
UOA ID: 601131649
<br>
## SE725 Assignment 1
The following code is an implementation of a simple file transfered specified in A [RFC913](https://datatracker.ietf.org/doc/html/rfc913)

<br>





## Using Server and Client
To use navigate to the directory src/testScripts/ <br>
1. Open terminal in this folder.
2. Run ServerTest with ./ServerTest.sh in console
3. Run ClientTest with ./ClientTest.sh in console
4. Perform any commands according to [RFC913](https://datatracker.ietf.org/doc/html/rfc913) documentation.
<br>
## Testing
To test navigate to the directory src/testScripts/ <br>
1. Open terminal in this folder.
2. Run ServerTest with ./ServerTest.sh in console
3. Run AutomatedClientTest with ./AutomatedClientTest.sh in console
<br>

### The following test cases are covered by running the AutoClientTest.sh
1. Successful log in then DONE
2. Failed log in, then true log in, then change user, then DONE
3. Attempt to change TYPE before log in, then log in, then change TYPE, then DONE
4. Log in, LIST Formated base dir, then LIST Verbose subDir, then LIST Formated current dir
5. Log in, CDIR to subDir, LIST Verbose, try to name doesnotexist, rename 1.txt with NAME TOBE OneOne.txt, LIST Verbose, RETR OneOne.txt, KILL OneOne.txt, STOR NEW OneOne.txt, STOR APP OneOne.TXT LIST V, STOR NEW 1.txt, KILL OneOne.txt, LIST V, DONE
