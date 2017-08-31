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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com._17od.upm.util.Translator;
import com.csvreader.CsvReader;

public class AccountsCSVMarshaller {

    public void marshal(ArrayList accounts, File file) throws ExportException {

//        if (file.exists()) {
//            throw new ExportException("The file to export to already exists");
//        }


//            FileWriter writer = new FileWriter(file);
    
//            CsvWriter csvWriter = new CsvWriter(writer, ',');
            for (int i=0; i<accounts.size(); i++) {
                AccountInformation temp = (AccountInformation) accounts.get(i);

//                csvWriter.writeRecord(
//                        getAccountAsStringArray(
//                                (AccountInformation) accounts.get(i)
//                        )
//                );
            }
//            csvWriter.close();


    }

    public ArrayList<AccountInformation> unmarshal(File file) throws ImportException {
        ArrayList<AccountInformation> accounts = new ArrayList<>();

        try {
            CsvReader csvReader = new CsvReader(new FileReader(file));
            while (csvReader.readRecord()) {
                if (csvReader.getColumnCount() != 5) {
                    throw new ImportException(
                            Translator.translate("notCSVFileError", 
                                    new Object[] {file.getAbsoluteFile(), 
                                    new Long(csvReader.getCurrentRecord() + 1)})); 
                }
                accounts.add(new AccountInformation(
                        csvReader.get(0),
                        csvReader.get(1),
                        csvReader.get(2),
                        csvReader.get(3),
                        csvReader.get(4)));
            }
        } catch (IOException e) {
            throw new ImportException(e);
        }

        return accounts;
    }

    private String[] getAccountAsStringArray(AccountInformation account) {
        String[] arr = new String[5];
        arr[0] = account.getAccountName();
        arr[1] = account.getUserId();
        arr[2] = account.getPassword();
        arr[3] = account.getUrl();
        arr[4] = account.getNotes();
        return arr;
    }

}
