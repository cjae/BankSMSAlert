package com.android.cjae.smsalert.util;

/**
 * Created by osagieomon on 7/27/17.
 */

public class CommonUtil {

    public static final String ADDRESS = "address";
    public static final String DATE = "date";
    public static final String BODY = "body";

    // For testing *****************************************************************
    public static String getSampleText(String body, int index) {
        String b[] = body.split(" ");
        return b[index].trim();
    }

    // General banks ****************************************************************
    public static String getAcctNo(String body, int index) {
        String b[] = body.split(" ");
        return b[index].trim().replaceAll("[^0-9xX*]", "");
    }

    // Diamond Bank *****************************************************************
    public static double getCreditDiamond(String body, int index) {
        String b[] = body.split("\n");
        String amount =  b[index].trim().replaceAll("[^0-9.]", "");

        try {
            return Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static String getAcctNoDiamond(String body, int index) {
        String b[] = body.split(" ");
        return b[index];
    }


    // UBA ****************************************************************************
    public static String getUBAAcctNo(String body, int index) {
        String b[] = body.split(" ");
        return b[index].trim().replaceAll("[^0-9xX*]", "");
    }

    public static boolean checkIfCreditUBAText(String body, int index) {
        String b[] = body.split(" ");
        String mode = b[index].trim();
        return mode.contains("Credit");

    }

    // Check for Credit Text **********************************************************
    public static boolean checkIfCredit(String body, int index) {
        String b[] = body.split(" ");
        String mode = b[index].trim();
        return mode.contains("CR") || mode.contains("Cr");

    }

    public static boolean checkIfCreditShortText(String body, int index) {
        String b[] = body.split(" ");
        String mode = b[index].trim();
        return mode.equalsIgnoreCase("Credit");

    }

    public static boolean checkIfCreditAlertText(String body, int index) {
        String b[] = body.split(" ");
        String mode = b[index].trim();
        return mode.contains("CreditAlert") || mode.contains("Credit Alert");

    }

    public static boolean checkIfCreditFullText(String body, int index) {
        String b[] = body.split(" ");
        String mode = b[index].trim();
        return mode.equalsIgnoreCase("Credited");

    }

    // Credit Balance General **********************************************************
    public static double getCreditBalance(String body, int index) {
        String b[] = body.split(" ");
        String amount =  b[index].trim().replaceAll("[^0-9.]", "");

        try {
            return Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
