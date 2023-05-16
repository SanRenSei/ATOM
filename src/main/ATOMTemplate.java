package main;

import java.io.File;
import java.util.Scanner;

public class ATOMTemplate {

    public String source = "";
    int currentIndex=0;

    public ATOMTemplate() {}

    public ATOMTemplate(String source) {
        this.source = "(" + source + ")";
    }

    // BUNDLER: BEGIN IGNORE
    public void importFile(File file) throws Exception {
        Scanner in = new Scanner(file);
        while (in.hasNext()) {
            source += in.nextLine()+'\n';
        }
        source = "(" + source + "\n)";
    }
    // BUNDLER: END IGNORE

    public char currentValidChar() {
        char c = source.charAt(currentIndex);
        while (c==' ' || c=='\n' || c=='#') {
            if (c=='#') {
                while (c!='\n') {
                    currentIndex++;
                    c = source.charAt(currentIndex);
                }
            }
            currentIndex++;
            c = source.charAt(currentIndex);
        }
        return c;
    }

}
