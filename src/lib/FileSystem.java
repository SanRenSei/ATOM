package lib;

import main.ATOMRuntime;
import main.ATOMScope;
import main.ATOMValue;
import main.ATOMValueType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

public class FileSystem extends ATOMScope {

    private ATOMValue reader = new ATOMValue(new ATOMScope(){
        public ATOMValue compute() {
            try {
                String filePath = ATOMRuntime.getIndexedVar(0).getStrVal();
                File f = new File(filePath);
                if (!f.exists()) {
                    return ATOMValue.NULL();
                }
                if (f.isDirectory()) {
                    return new ATOMValue(Arrays.stream(f.list()).map(ATOMValue::new).collect(Collectors.toList()));
                }
                Scanner fileReader = new Scanner(new File(filePath));
                StringBuilder fileResult = new StringBuilder();
                while (fileReader.hasNext()) {
                    fileResult.append(fileReader.nextLine()).append('\n');
                }
                return new ATOMValue(fileResult.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ATOMValue.NULL();
        }
    });

    private ATOMValue writer = new ATOMValue(new ATOMScope(){
        public ATOMValue compute() {
            try {
                ATOMScope obj = ATOMRuntime.getIndexedVar(0).getObjVal();
                String filePath = obj.dereference(new ATOMValue("path")).getStrVal();
                String fileContent = obj.dereference(new ATOMValue("content")).getStrVal();
                BufferedWriter out = new BufferedWriter(new FileWriter(new File(filePath)));
                out.write(fileContent);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ATOMValue.NULL();
        }
    });

    public ATOMValue compute() {
        return reader.compute();
    }

    public ATOMValue dereference(ATOMValue key) {
        if (key.getType() != ATOMValueType.STRING) {
            throw new RuntimeException("Incorrect type: " + key.getType());
        }
        if (key.getStrVal().equals("read")) {
            return reader;
        }
        if (key.getStrVal().equals("write")) {
            return writer;
        }
        return ATOMValue.NULL();
    }

}
