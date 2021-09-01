#!/bin/bash
echo "Setting up Client..."
cd ..
javac client/*.java
echo "Client Compiled..."

CYAN='\033[0;36m'
L_CYAN='\033[1;36m'
PURPLE='\033[0;35m'
L_PURPLE='\033[1;35m'
RESET='\033[0m'

echo -e "1. Successful log in then DONE"
echo -e "${L_CYAN}-----= [COMMANDS] =-----${CYAN}"
cat << EOF
USER Alex1
PASS 1
ACCT account1
DONE
EOF
echo -e ""
echo -e "${L_PURPLE}------- [OUTPUT] -------${PURPLE}"

java client/ClientRunner << EOF
USER Alex1
PASS 1
ACCT account1
DONE
EOF
echo -e "${RESET}"

echo -e "2. Failed log in, then true log in, then change user, then DONE"
echo -e "${L_CYAN}-----= [COMMANDS] =-----${CYAN}"
cat << EOF
USER Alex2
USER The22
PASS 22
ACCT account2
USER Alex1
PASS 1
ACCT account2
ACCT account1
DONE
EOF
echo -e ""
echo -e "${L_PURPLE}------- [OUTPUT] -------${PURPLE}"

java client/ClientRunner << EOF
USER Alex2
USER The22
PASS 22
ACCT account2
USER Alex1
PASS 1
ACCT account2
ACCT account1
DONE
EOF
echo -e "${RESET}"

echo -e "3. Attempt to change TYPE before log in, then log in, then change TYPE, then DONE"
echo -e "${L_CYAN}-----= [COMMANDS] =-----${CYAN}"
cat << EOF
TYPE B
USER The22
PASS 22
ACCT account2
TYPE B
DONE
EOF
echo -e ""
echo -e "${L_PURPLE}------- [OUTPUT] -------${PURPLE}"

java client/ClientRunner << EOF
TYPE B
USER The22
PASS 22
ACCT account2
TYPE B
DONE
EOF
echo -e "${RESET}"

echo -e "4. Log in, LIST Formated base dir, then LIST Verbose subDir, then LIST Formated current dir"
echo -e "${L_CYAN}-----= [COMMANDS] =-----${CYAN}"
cat << EOF
USER The22
PASS 22
ACCT account2
LIST F
LIST V subDir
LIST V
DONE
EOF
echo -e ""
echo -e "${L_PURPLE}------- [OUTPUT] -------${PURPLE}"

java client/ClientRunner << EOF
TYPE B
USER The22
PASS 22
ACCT account2
PASS 22
ACCT account2
LIST F
LIST V subDir
LIST V
DONE
EOF
echo -e "${RESET}"

echo -e "5. Log in, CDIR to subDir, LIST Verbose, try to name doesnotexist, renames 1.txt with NAME TOBE OneOne.txt, LIST Verbose, RETR OneOne.txt, KILL OneOne.txt, STOR NEW OneOne.txt, STOR APP OneOne.TXT LIST V, STOR NEW 1.txt, KILL OneOne.txt, LIST V, DONE"
echo -e "${L_CYAN}-----= [COMMANDS] =-----${CYAN}"
cat << EOF
USER The22
PASS 22
ACCT account2
CDIR subDir
NAME doesnotexist.txt
NAME 1.txt
TOBE OneOne.txt
LIST V
RETR OneOne.txt
SEND
STOR NEW OneOne.txt
SIZE 3000
STOR APP OneOne.txt
SIZE 3
LIST V
STOR NEW 1.txt
NAME doesnotexist.txt
KILL OneOne.txt
DONE
EOF
echo -e ""
echo -e "${L_PURPLE}------- [OUTPUT] -------${PURPLE}"

java client/ClientRunner << EOF
USER The22
PASS 22
ACCT account2
CDIR subDir
NAME doesnotexist.txt
NAME 1.txt
TOBE OneOne.txt
LIST V
RETR OneOne.txt
SEND
STOR NEW OneOne.txt
SIZE 3000
STOR APP OneOne.txt
SIZE 3
LIST V
STOR NEW 1.txt
SIZE 200
KILL OneOne.txt
DONE
EOF
echo -e "${RESET}"

echo "Press any key to exit"
read -r response