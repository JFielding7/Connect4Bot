package connect4bot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DatabaseManager {

    public static void main(String[] args) {
//        splitDatabase("upperBoundDatabase", 4);
//        splitDatabase("lowerBoundDatabase", 4);
        // mergeDatabase("upperBoundDatabase", 4);
    }

    static void splitDatabase(String file, int partitions) {
        try (FileInputStream in = new FileInputStream(file + ".bin")) {
            byte[] bytes = in.readAllBytes();
            int sectionLength = bytes.length / partitions;
            for (int i = 0; i < partitions; i++) {
                FileOutputStream out = new FileOutputStream(file + i + ".bin");
                if (i < partitions - 1) out.write(bytes, i * sectionLength, sectionLength);
                else out.write(bytes, i * sectionLength, sectionLength + bytes.length % partitions);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void mergeDatabase(String file, int partitions) {
        try (FileOutputStream out = new FileOutputStream(file + "Merged.bin")) {
            for (int i = 0; i < partitions; i++) {
                FileInputStream in = new FileInputStream(file + i + ".bin");
                out.write(in.readAllBytes());
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
