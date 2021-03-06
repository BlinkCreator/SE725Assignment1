package server;

import java.util.Arrays;
import java.util.List;

public class Accounts {
    static Boolean isUserSelected = false;
    static Boolean isMatchingPassword = false;
    static Boolean isMatchingAccount = false;
    static Boolean isLoggedIn = false;

    String[] users = {"Alex1", "The22", "Great333", "USERDEFAULT"};
    String[] passwords = {"1", "22", "333"};
    String[] accounts = {"account1", "account2", "account3"};

    int userIndex = 3;

    public boolean getIsUserSelected() {
        return isUserSelected;
    }

    public String getUser(){
        return users[userIndex];
    }


    public boolean selectUser(String user){
        List<String> usersList = Arrays.asList(users);
        if(usersList.contains(user)){
            userIndex = usersList.indexOf(user);

            isUserSelected = true;
            isMatchingAccount = false;
            isLoggedIn = false;
            isMatchingPassword = false;

            return true;
        }else{
            isUserSelected = false;
            return false;
        }
    }

    public boolean enterPassword(String password){
        List<String> passwordsList = Arrays.asList(passwords);

        if(passwordsList.contains(password) &&  userIndex == passwordsList.indexOf(password)){
            isMatchingPassword = true;
            return true;
        }else{
            isMatchingPassword = false;
            return false;
        }
    }

    public boolean selectAccount(String account){
        List<String> accountsList = Arrays.asList(accounts);
        if(accountsList.contains(account) && userIndex == accountsList.indexOf(account)){
            isMatchingAccount = true;
            return true;
        }else{
            isMatchingAccount = false;
            return false;
        }
    }

    public boolean checkLogin(){
        if(isUserSelected && isMatchingPassword && isMatchingAccount){
            isLoggedIn = true;
            return true;
        }else{
            isLoggedIn = false;
            return false;
        }
    }
}
