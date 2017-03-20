package jp.gr.java_conf.tmatz.safeincloud_db;

import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void openDatabase() throws Exception {
        try (InputStream inputStream = new BufferedInputStream(
                new FileInputStream("database/src/androidTest/res/raw/safeincloud.db"))) {
            DatabaseReader databaseReader = new DatabaseReader();
            try (InputStream content = databaseReader.read(inputStream, "password")) {
                DatabaseParser parser = new DatabaseParser();
                Database database = parser.parse(content);

                database.dump(System.out, 0);
            }
        }
    }
}
