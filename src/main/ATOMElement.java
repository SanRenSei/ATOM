package main;

import java.io.File;
import java.util.Scanner;

public abstract class ATOMElement {

    public ATOMScope parent;

    public static ATOMElement fromTemplate(ATOMTemplate template) {
        char c = template.currentValidChar();
        for (ATOMOperation op : ATOMOperation.operations) {
            for (String opStr : op.commands) {
                if (c==opStr.charAt(0) && template.source.startsWith(opStr, template.currentIndex)) {
                    template.currentIndex += opStr.length();
                    return op;
                }
            }
        }
        if (c=='(') {
            return new ATOMScope(template, ATOMScopeType.PARENTHESIS);
        }
        if (c=='[') {
            return new ATOMScope(template, ATOMScopeType.ARRAY);
        }
        if (c=='{') {
            return new ATOMScope(template, ATOMScopeType.OBJECT);
        }
        // BUNDLER: BEGIN IGNORE
        if (c=='@') {
            int startIndex = template.currentIndex;
            int endIndex = template.currentIndex+1;
            while (isFileChar(template.source.charAt(endIndex))) {
                endIndex++;
                if (endIndex == template.source.length()) {
                    break;
                }
            }
            template.currentIndex = endIndex;
            try {
                File f = new File(template.source.substring(startIndex + 1, endIndex));
                if (f.getName().indexOf(".atom")>0) {
                    return ATOMRuntime.processFile(f);
                } else {
                    String fileContent = "";
                    Scanner in = new Scanner(f);
                    while (in.hasNext()) {
                        fileContent += in.nextLine()+'\n';
                    }
                    return new ATOMValue(fileContent);
                }
            } catch (Exception e) {
                return ATOMValue.NULL();
            }
        }
        // BUNDLER: END IGNORE
        if (c=='"') {
            int startIndex = template.currentIndex;
            int endIndex = template.currentIndex+1;
            while (template.source.charAt(endIndex)!='"') {
                if (template.source.charAt(endIndex)=='\\' && template.source.charAt(endIndex+1)=='"') {
                    endIndex+=2;
                } else {
                    endIndex++;
                }
            }
            template.currentIndex = endIndex+1;
            return new ATOMValue(template.source.substring(startIndex+1, endIndex)
                    .replaceAll("\\\\\"", "\"")
                    .replaceAll("\\\\n", "\n"));
        }
        if (c=='$') {
            int startIndex = template.currentIndex+1;
            int endIndex = template.currentIndex+1;
            boolean isString = false;
            c = template.source.charAt(endIndex);
            while ((c>='0' && c<='9') || isAlphabet(c)) {
                if (isAlphabet(c)) {
                    isString = true;
                }
                endIndex ++;
                c = template.source.charAt(endIndex);
            }
            template.currentIndex = endIndex;
            if (isString) {
                return new ATOMValueDynamic(template.source.substring(startIndex, endIndex), true);
            }
            return new ATOMValueDynamic(Integer.parseInt(template.source.substring(startIndex, endIndex)));
        }
        if (isAlphabet(c)) {
            int startIndex = template.currentIndex;
            int endIndex = template.currentIndex;
            while (isAlphabet(template.source.charAt(endIndex))) {
                endIndex++;
            }
            template.currentIndex = endIndex;
            return new ATOMValueDynamic(template.source.substring(startIndex, endIndex));
        }
        if (c>='0' && c<='9') {
            int startIndex = template.currentIndex;
            int endIndex = template.currentIndex;
            while (template.source.charAt(endIndex)>='0' && template.source.charAt(endIndex)<='9') {
                endIndex++;
            }
            template.currentIndex = endIndex;
            return new ATOMValue(Integer.parseInt(template.source.substring(startIndex, endIndex)));
        }
        throw new RuntimeException(c+"");
    }

    // Eval is lazy
    // They usually will return the same value, except when called on a function
    // Eval will return the function itself
    // Compute will return the result of the function
    public abstract ATOMValue eval();
    public abstract ATOMValue compute();


    private static boolean isAlphabet(char c) {
        return c >= 'A' && c <= 'Z' || c>='a' && c<='z';
    }

    private static boolean isFileChar(char c) {
        return c >= 'A' && c <= 'Z' || c>='a' && c<='z' || c>='0' && c<='9' || c=='_' || c=='/' || c=='.';
    }

}
