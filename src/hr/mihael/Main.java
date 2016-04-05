package hr.mihael;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        final String inputFolderPath = "/Users/Mihael/Documents/Five/rosetta/RSCourse - 0.5.0/src";
        final String outputFolderPath = "/Users/Mihael/Desktop/Pojos";

        final ObjcParser parser = new ObjcParser(inputFolderPath, outputFolderPath, FileType.JAVA);

        try {
            parser.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
