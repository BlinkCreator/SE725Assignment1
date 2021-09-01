# SE725Assignment1 
UPI: moco657
UOA ID: 601131649
<br>
## Project Overview:
The following code is an implementation of a simple file transfered specified in A [RFC913](https://datatracker.ietf.org/doc/html/rfc913)
The server/client were deemed to be working if all inputs/outputs are described in [RFC913](https://datatracker.ietf.org/doc/html/rfc913)
## Project Structure:
The required files in the directory for all tests to function are: SE725Assignment1/<br>

```
|
├── README.md 
│              
└── src
    ├── client
    │   ├── ClientRunner.java
    │   ├── TCPClient.java
    │   └── dir
    │       ├── 1.txt
    │       └── OneOne.txt
    ├── server
    │   ├── Accounts.java
    │   ├── ServerInstance.java
    │   ├── TCPServer.java
    │   └── dir
    │       ├── 1.txt
    │       ├── OneOne.txt
    │       └── subDir
    │           ├── 1.txt
    │           └── OneOne.txt
    └── testScripts
        ├── AutomatedClientTest.sh
        ├── ClientTest.sh
        └── ServerTest.sh
```


## Using Server and Client:
To use navigate to the directory src/testScripts/ <br>
1. Open terminal in this folder.
2. Run ServerTest with `./ServerTest.sh` in console
3. Run ClientTest with `./ClientTest.sh` in console
4. Perform any commands according to [RFC913](https://datatracker.ietf.org/doc/html/rfc913) documentation.

### Command extraction from [RFC913](https://datatracker.ietf.org/doc/html/rfc913)
The all inputs are case sensitive according to the RFC913 documentation.

If the cmd sent is not listed in the RFC913 documentation seen below, the server will respond with: '-Invalid Command' or '-Please Login' if the USER is not logged in.

All commands except USER, ACCT, PASS and DONE require the Client to be authenticated via Loggin any attempt to input any command other than those 4 will result in the server responding '-Please Login'.
The typical com
```
      <command> : = <cmd> [<SPACE> <args>] <NULL>

      <cmd> : =  USER ! ACCT ! PASS ! TYPE ! LIST ! CDIR
                 KILL ! NAME ! DONE ! RETR ! STOR

      <response> : = <response-code> [<message>] <NULL>

      <response-code> : =  + | - |   | !

      <message> can contain <CRLF>
```

## Testing:
To test navigate to the directory src/testScripts/ <br>
1. Open terminal in this folder.
2. Run ServerTest with `./ServerTest.sh` in console
3. Run AutomatedClientTest with `./AutomatedClientTest.sh` in console
### The following test cases are covered by running the AutoClientTest.sh
1. Successful log in then DONE
2. Failed log in, then true log in, then change user, then DONE
3. Attempt to change TYPE before log in, then log in, then change TYPE, then DONE
4. Log in, LIST Formated base dir, then LIST Verbose subDir, then LIST Formated current dir
5. Log in, CDIR to subDir, LIST Verbose, try to name doesnotexist, rename 1.txt with NAME TOBE OneOne.txt, LIST Verbose, RETR OneOne.txt, KILL OneOne.txt, STOR NEW OneOne.txt, STOR APP OneOne.TXT LIST V, STOR NEW 1.txt, KILL OneOne.txt, LIST V, DONE
The above tests automatically feed in the commands and if the server/client is working correctly no errors will be thrown and "Press any key to exit" will appear on the client.
### Notes for testing/use
1. Since the server is multithreaded the server instance will not need to be closed while running tests or inputing user. 
2. Because client is not multithreaded the client instance will need to be closed before running an additional client instance.
3. Closing the client instance without using the DONE command will result in an error being thrown on server side for "Interupted Socket"



## USER DETAILS:
|  USER   |  ACCT  |  PASS  |
| ------- |:------:| ------:|
| Alex1   |account1|1       |
| The22   |account2|22      |
| Great333|account3|333     |
<br>
