/*
 * Universal Password Manager
 * Copyright (C) 2005-2013 Adrian Smith
 *
 * This file is part of Universal Password Manager.
 *   
 * Universal Password Manager is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Universal Password Manager is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Universal Password Manager; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com._17od.upm.database;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import com._17od.upm.crypto.CryptoException;
import com._17od.upm.crypto.DESDecryptionService;
import com._17od.upm.crypto.EncryptionService;
import com._17od.upm.crypto.InvalidPasswordException;
import com._17od.upm.util.Util;

/**
 * This factory is used to load or create a PasswordDatabase. Different versions
 * of the database need to be loaded slightly differently so this class takes
 * care of those differences.
 * 
 * Database versions and formats. The items between [] brackets are encrypted.
 *   3     >> MAGIC_NUMBER DB_VERSION SALT [DB_REVISION DB_OPTIONS ACCOUNTS]
 *      (all strings are encoded using UTF-8)
 *   2     >> MAGIC_NUMBER DB_VERSION SALT [DB_REVISION DB_OPTIONS ACCOUNTS]
 *   1.1.0 >> SALT [DB_HEADER DB_REVISION DB_OPTIONS ACCOUNTS]
 *   1.0.0 >> SALT [DB_HEADER ACCOUNTS]
 * 
 *   DB_VERSION = The structural version of the database
 *   SALT = The salt used to mix with the user password to create the key
 *   DB_HEADER = Was used to store the structural version of the database (pre version 2)
 *   DB_OPTIONS = Options relating to the database
 *   ACCOUNTS = The account information
 *   
 *   From version 2 the db version is stored unencrypted at the start of the file.
 *   This allows for cryptographic changes in the database structure. Before this
 *   we had to know how to unencrypt the database before we could find out the version number.
 */
public class PasswordDatabasePersistence {

    private static final String FILE_HEADER = "UPM";
    private static final int DB_VERSION = 3;

    private EncryptionService encryptionService;

    /**
     * Used when we have a password and we want to get an instance of the class
     * so that we can call load(File, char[])  
     */
    public PasswordDatabasePersistence() {
    }

    /**
     * Used when we want to create a new database with the given password
     * @param password
     * @throws CryptoException
     */
    public PasswordDatabasePersistence(char[] password) throws CryptoException {
        encryptionService = new EncryptionService(password);
    }

    public PasswordDatabase load() throws InvalidPasswordException, ProblemReadingDatabaseFile, IOException {
        String url = "http://127.0.0.1:4000/loginmanager";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection)obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();

        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        HashMap accounts = new HashMap();

        //print result
        System.out.println(response.toString());
        String[] accountsArray = response.toString().split("}\\{");
        for(String account : accountsArray){
            account = account.replace("}","").replace("{", "");
            String[] data = account.split(",|:");
//            String id = data[1];
            String websiteName = data[3];
            String url1 = data[5];
            String password = data[7];
            String notes = data[9];
            String userid = data[11];
            AccountInformation tempAccount = new AccountInformation(websiteName, userid,password,url1,notes);
            System.out.println("PUT NEW THING IN ACCOUNTS");
            accounts.put(tempAccount.getAccountName(), tempAccount);
        }

        PasswordDatabase passwordDatabase = new PasswordDatabase(accounts);

        return passwordDatabase;

    }

    public void save(PasswordDatabase database) throws IOException, CryptoException {
        String url = "http://127.0.0.1:4000/loginmanager";

        for (Object o : database.getAccountsHash().values()) {
            AccountInformation ai = (AccountInformation) o;
            String websiteName = ai.getAccountName();
            String website = ai.getUrl();
            String password = ai.getPassword();
            String notes = ai.getNotes();
            String userId = ai.getUserId();
            url = url + "?websiteName="+websiteName+"&website="+website+"&password="+password+"&infoNotes="+notes+ "&userId="+userId;
        }
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection)obj.openConnection();
        con.setRequestMethod("POST");
        int responseCode = con.getResponseCode();

        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
    }

    public EncryptionService getEncryptionService() {
        return encryptionService;
    }

    private byte[] readFile(File file) throws IOException {
        InputStream is;
        try {
            is = new FileInputStream(file);
        } catch (IOException e) {
            throw new IOException("There was a problem with opening the file", e);
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) file.length()];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        
        try {
            while (offset < bytes.length
                    && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }
    
            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
        } finally {
            is.close();
        }

        return bytes;
    }

}
