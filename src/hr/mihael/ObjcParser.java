package hr.mihael;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class ObjcParser {

    private final FileType outputFileType;

    private final Map<String, String> objectTypeMap;

    private String inputFolderPath;
    private String outputFolderPath;

    private ObjcParser(String inputFolderPath, String outputFolderPath, FileType outputFileType, Map<String, String> objectTypeMap) {
        this.inputFolderPath = inputFolderPath;
        this.outputFolderPath = outputFolderPath;
        this.outputFileType = outputFileType;
        this.objectTypeMap = objectTypeMap;
    }

    public ObjcParser(String inputFolderPath, String outputFolderPath, FileType outputFileType) {
        this(inputFolderPath, outputFolderPath, outputFileType, createObjectTypeMap(outputFileType));
    }

    public ObjcParser(FileType outputFileType) {
        this("", "", outputFileType, createObjectTypeMap(outputFileType));
    }

    public void parse() throws IOException {
        parse(inputFolderPath, outputFolderPath);
    }

    public void parse(String inputFolderPath, String outputFolderPath) throws UnsupportedOperationException {
        ArgsUtils.throwIf(TextUtils.isNullOrEmpty(inputFolderPath)
                || TextUtils.isNullOrEmpty(outputFolderPath), "Folder paths are not good!");

        File inputFolder = new File(inputFolderPath);

        Stream.of(inputFolder.list())
                .filter(filename -> filename.contains(FileType.OBJ_C_HEADER.fileExtension))
                .forEach(filename -> {
                    try {
                        generateFileForFilename(filename, inputFolderPath, outputFolderPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void generateFileForFilename(String filename, String inputFolderPath, String outputFolderPath) throws IOException {
        switch (outputFileType) {
            case KOTLIN:
                generatePocoFromObjcHeaderFile(filename, inputFolderPath, outputFolderPath);
                break;
            case JAVA:
                generatePojoFromObjcHeaderFile(filename, inputFolderPath, outputFolderPath);
                break;
            default:
                throw new UnsupportedOperationException("Unimplemented outputFileType parsing: " + outputFileType);
        }
    }

    private void generatePocoFromObjcHeaderFile(String filename, String fileRoot, String outputFolder) throws IOException {
        final String className = filename.substring(0, filename.indexOf("."));
        System.out.println("Creating class -> " + className);

        final String inputFilePath = fileRoot + "/" + className + FileType.OBJ_C_HEADER.fileExtension;
        final String outputFilePath = outputFolder + "/" + className + FileType.KOTLIN.fileExtension;

        final File inputFile = new File(inputFilePath);

        BufferedReader inputFileReader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(outputFilePath, false));

        outputFileWriter.write("public data class " + className + "(");
        outputFileWriter.newLine();

        boolean isFirstProperty = true;

        String line = null;
        String objectType = "";
        String objectName = "";
        while ((line = inputFileReader.readLine()) != null) {
            if (line.startsWith("@property")) {
                if (!isFirstProperty) {
                    outputFileWriter.write(",");
                    outputFileWriter.newLine();
                } else {
                    isFirstProperty = false;
                }
                String[] lineParts = line.split("\\s+");
                if (lineParts.length == 6) {     // Object types with pointer *
                    objectType = lineParts[3];
                    objectName = lineParts[5];
                } else if (lineParts.length == 5) {   // Primitive types
                    objectType = lineParts[3];
                    objectName = lineParts[4];
                }
            } else {
                continue;
            }

            objectType = objectTypeMap.containsKey(objectType) ? objectTypeMap.get(objectType) : objectType;
            objectName = objectName.replaceAll(";", "");
            outputFileWriter.write("                " + "public val " + objectName + ": " + objectType);
        }

        outputFileWriter.newLine();
        outputFileWriter.write(")");

        inputFileReader.close();
        outputFileWriter.flush();
        outputFileWriter.close();

        System.out.println("DONE");
    }

    private void generatePojoFromObjcHeaderFile(String filename, String fileRoot, String outputFolder) throws IOException {
        final String className = filename.substring(0, filename.indexOf("."));
        System.out.println("Creating class -> " + className);

        final String inputFilePath = fileRoot + "/" + className + FileType.OBJ_C_HEADER.fileExtension;
        final String outputFilePath = outputFolder + "/" + className + FileType.JAVA.fileExtension;

        final File inputFile = new File(inputFilePath);

        BufferedReader inputFileReader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(outputFilePath, false));

        outputFileWriter.write("public final class " + className + " {");
        outputFileWriter.newLine();
        outputFileWriter.newLine();

        String line = null;
        String objectType = "";
        String objectName = "";
        while ((line = inputFileReader.readLine()) != null) {
            if (line.startsWith("@end")) {
                break;
            }
            if (line.startsWith("@property")) {
                String[] lineParts = line.split("\\s+");
                if (lineParts.length == 6) {     // Object types with pointer *
                    objectType = lineParts[3];
                    objectName = lineParts[5];
                } else if (lineParts.length == 5) {   // Primitive types
                    objectType = lineParts[3];
                    objectName = lineParts[4];
                }
            } else {
                continue;
            }

            objectType = objectTypeMap.containsKey(objectType) ? objectTypeMap.get(objectType) : objectType;
            objectName = objectName.replaceAll(";", "");
            outputFileWriter.write("    " + "private final " + objectType + " " + objectName + ";");
            outputFileWriter.newLine();
        }

        outputFileWriter.newLine();
        outputFileWriter.write("}");

        inputFileReader.close();
        outputFileWriter.flush();
        outputFileWriter.close();

        System.out.println("DONE");
    }

    private static Map<String, String> createObjectTypeMap(FileType outputFileType) {
        final Map<String, String> objectTypeMap = new HashMap<>();
        switch (outputFileType) {
            case KOTLIN:
                objectTypeMap.put("NSDecimalNumber", "Double");
                objectTypeMap.put("NSString", "String");
                objectTypeMap.put("NSSet", "Set");
                objectTypeMap.put("NSNumber", "Number");
                objectTypeMap.put("BOOL", "Boolean");
                objectTypeMap.put("NSUInteger", "Int");
                objectTypeMap.put("NSRange", "Range");  // Kreirat Range klasu
                objectTypeMap.put("NSAttributedString", "String");
                objectTypeMap.put("NSData", "ByteArray");       // Check
                break;
            case JAVA:
                objectTypeMap.put("NSDecimalNumber", "double");
                objectTypeMap.put("NSString", "String");
                objectTypeMap.put("NSSet", "Set<TODO>");
                objectTypeMap.put("NSNumber", "Number");
                objectTypeMap.put("BOOL", "boolean");
                objectTypeMap.put("NSUInteger", "int");
                objectTypeMap.put("NSRange", "Range");  // Kreirat Range klasu
                objectTypeMap.put("NSAttributedString", "String");
                objectTypeMap.put("NSData", "ByteBuffer");       // Check
                break;
            default:
                throw new UnsupportedOperationException("Unimplemented object type map creation for output file: " + outputFileType);
        }
        return objectTypeMap;
    }
}
