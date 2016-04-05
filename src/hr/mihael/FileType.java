package hr.mihael;

public enum FileType {
    OBJ_C_HEADER(".h"),
    KOTLIN(".kt"),
    JAVA(".java");

    public final String fileExtension;

    FileType(String fileExtension) {
        this.fileExtension = fileExtension;
    }
}
